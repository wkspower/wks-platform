"""
Norms Save Service
Saves calculated budget values to NormsMonthDetail table after each calculation.

Updates:
- QTY: Generation quantity (same for all materials under same Plant+Utility)
- Quantity: Material consumption = QTY * Norms
- Syncs model-calculated norms to CPPNorms table via stored procedure
"""

from database.connection import get_connection


def _sync_to_cpp_norms(conn, norms_header_fk_id: str, fym_id: str, norms_value: float, modified_by: str = 'PythonModel'):
    """
    Sync updated norms from NormsMonthDetail to CPPNorms table.
    Calls the CPP_UpdateNormsFromPythonModel stored procedure.
    
    Args:
        conn: Database connection
        norms_header_fk_id: NormsHeader FK ID (UUID)
        fym_id: FinancialYearMonth FK ID (UUID)
        norms_value: New norms value
        modified_by: Who modified the norms (default: 'PythonModel')
    
    Returns:
        True if sync succeeded, False otherwise
    """
    try:
        cur = conn.cursor()
        cur.execute('''
            EXEC dbo.CPP_UpdateNormsFromPythonModel 
                @NormsHeaderFkId = ?, 
                @FinancialYearMonthFkId = ?, 
                @Norms = ?, 
                @ModifiedBy = ?
        ''', (norms_header_fk_id, fym_id, norms_value, modified_by))
        
        # Fetch result
        result = cur.fetchone()
        if result and result[0] == 'Success':
            return True
        return False
    except Exception as e:
        print(f"Warning: CPPNorms sync failed for NormsHeader {norms_header_fk_id}: {str(e)}")
        return False


def save_calculated_norms(month: int, year: int, result: dict, dry_run: bool = False) -> dict:
    """
    Save calculated values from budget result to NormsMonthDetail table.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025)
        result: Result dictionary from calculate_budget_with_iteration
        dry_run: If True, only show what would be updated without saving
    
    Returns:
        dict with save status and counts
    """
    if not result or not result.get('overall_success'):
        return {
            'success': False,
            'message': 'Cannot save - calculation did not succeed',
            'updated': 0,
            'same': 0
        }
    
    # Build generation map and norms map from result
    generation_map, norms_map = _build_generation_map(result)
    
    # Connect to database
    conn = get_connection()
    cur = conn.cursor()
    
    # Get FinancialYearMonth ID
    cur.execute("SELECT Id FROM FinancialYearMonth WHERE Month = ? AND Year = ?", (month, year))
    fym_row = cur.fetchone()
    if not fym_row:
        conn.close()
        return {
            'success': False,
            'message': f'FinancialYearMonth not found for {month}/{year}',
            'updated': 0,
            'same': 0
        }
    fym_id = fym_row[0]
    
    # Get all NormsMonthDetail records for this month
    cur.execute('''
        SELECT 
            nmd.Id,
            p.Name as PlantName,
            nh.UtilityName,
            nh.MaterialName,
            nh.IssuingPlantName,
            nmd.QTY,
            nmd.Quantity,
            nmd.Norms,
            nmd.NormsHeader_FK_Id
        FROM NormsMonthDetail nmd
        INNER JOIN NormsHeader nh ON nh.Id = nmd.NormsHeader_FK_Id
        INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
        WHERE nmd.FinancialYearMonth_FK_Id = ? AND nh.IsActive = 1
    ''', (fym_id,))
    
    records = cur.fetchall()
    
    updated_count = 0
    same_count = 0
    cpp_norms_synced = 0
    updates = []
    
    for row in records:
        record_id = row[0]
        plant_name = row[1]
        utility_name = row[2]
        material_name = row[3]
        issueing_plant = row[4]  # IssueingPlant column
        old_qty = float(row[5]) if row[5] else 0
        old_quantity = float(row[6]) if row[6] else 0
        old_norms = float(row[7]) if row[7] else 0
        norms_header_fk_id = str(row[8]) if len(row) > 8 and row[8] else None  # NormsHeader_FK_Id for CPPNorms sync
        
        # Get new QTY from generation map
        key = (plant_name, utility_name)
        new_qty = generation_map.get(key, None)
        
        # Check if there's a new norm value for this record
        # For POWERGEN records, use IssueingPlant to identify which plant (PP1, PP2, PP3, STG)
        if material_name == 'POWERGEN' and issueing_plant:
            # Map IssueingPlant to the material name format used in norms_map
            if 'Power Plant 1' in issueing_plant or 'Power Plant-1' in issueing_plant:
                norms_key = (plant_name, utility_name, 'POWERGEN (PP1)')
            elif 'Power Plant 2' in issueing_plant or 'Power Plant-2' in issueing_plant:
                norms_key = (plant_name, utility_name, 'POWERGEN (PP2)')
            elif 'Power Plant 3' in issueing_plant or 'Power Plant-3' in issueing_plant:
                norms_key = (plant_name, utility_name, 'POWERGEN (PP3)')
            elif 'STG' in issueing_plant:
                norms_key = (plant_name, utility_name, 'POWERGEN (STG)')
            else:
                norms_key = (plant_name, utility_name, material_name)
        else:
            norms_key = (plant_name, utility_name, material_name)
        
        new_norms = norms_map.get(norms_key, None)
        
        # Use new norms if available, otherwise keep old
        norms_to_use = new_norms if new_norms is not None else old_norms
        
        # Skip if not in generation map (keep existing value)
        if new_qty is None:
            same_count += 1
            continue
        
        # Calculate new Quantity = QTY * Norms (even if norm is 0, calculate 0)
        new_quantity = new_qty * norms_to_use
        
        # Check if changed
        qty_changed = abs(new_qty - old_qty) > 0.01
        quantity_changed = abs(new_quantity - old_quantity) > 0.01
        norms_changed = new_norms is not None and abs(new_norms - old_norms) > 0.0001
        
        if qty_changed or quantity_changed or norms_changed:
            updated_count += 1
            updates.append({
                'id': record_id,
                'plant': plant_name,
                'utility': utility_name,
                'material': material_name,
                'old_qty': old_qty,
                'new_qty': new_qty,
                'old_quantity': old_quantity,
                'new_quantity': new_quantity,
                'old_norms': old_norms,
                'new_norms': norms_to_use if new_norms is not None else None,
            })
            
            if not dry_run:
                if new_norms is not None:
                    # Update QTY, Quantity, AND Norms in NormsMonthDetail
                    cur.execute('''
                        UPDATE NormsMonthDetail 
                        SET QTY = ?, Quantity = ?, Norms = ?
                        WHERE Id = ?
                    ''', (new_qty, new_quantity, new_norms, record_id))
                    
                    # Sync model-calculated norms to CPPNorms table
                    if norms_header_fk_id and _sync_to_cpp_norms(conn, norms_header_fk_id, fym_id, new_norms, 'PythonModel'):
                        cpp_norms_synced += 1
                else:
                    # Update only QTY and Quantity
                    cur.execute('''
                        UPDATE NormsMonthDetail 
                        SET QTY = ?, Quantity = ?
                        WHERE Id = ?
                    ''', (new_qty, new_quantity, record_id))
        else:
            same_count += 1
    
    if not dry_run:
        conn.commit()
    
    conn.close()
    
    # Build message with CPPNorms sync info
    message = f'{"DRY RUN: " if dry_run else ""}Updated {updated_count} records, {same_count} unchanged'
    if not dry_run and cpp_norms_synced > 0:
        message += f', {cpp_norms_synced} synced to CPPNorms'
    
    return {
        'success': True,
        'message': message,
        'updated': updated_count,
        'same': same_count,
        'cpp_norms_synced': cpp_norms_synced,
        'total': len(records),
        'dry_run': dry_run,
        'updates': updates if dry_run else []  # Only return details in dry run
    }


def _build_generation_map(result: dict) -> dict:
    """
    Build a map of (Plant, Utility) -> Generation QTY from calculation result.
    
    For Power Plants (GT1, GT2, GT3):
    - POWERGEN: Gross generation in KWH
    - Utility materials (Compressed Air, Cooling Water, Power_Dis): 
      QTY = Plant's gross generation (same for all materials under same plant)
    """
    generation_map = {}
    
    power_result = result.get('power_result', {})
    steam_result = result.get('steam_result', {})
    stg_extraction = result.get('stg_extraction', {})
    utility_consumption = result.get('utility_consumption', {})
    
    # Get STG load-based LP/MP balance (if available)
    final_lp_balance = result.get('final_lp_balance', None)
    final_mp_balance = result.get('final_mp_balance', None)
    
    # ============================================================
    # POWER GENERATION (GT1, GT2, GT3, STG) - in KWH
    # For each power plant, set QTY for POWERGEN and all utility materials
    # ============================================================
    dispatch_plan = power_result.get('dispatchPlan', [])
    
    # Track which plants are operational (use GROSS for POWERGEN QTY, NET for Power_Dis norms)
    gt1_gross_kwh = 0
    gt2_gross_kwh = 0
    gt3_gross_kwh = 0
    stg_gross_kwh = 0
    gt1_net_kwh = 0
    gt2_net_kwh = 0
    gt3_net_kwh = 0
    stg_net_kwh = 0
    
    for asset in dispatch_plan:
        asset_name = asset.get('AssetName', '')
        gross_kwh = asset.get('GrossMWh', 0) * 1000
        net_kwh = asset.get('NetMWh', 0) * 1000
        
        if 'Plant-1' in asset_name:
            gt1_gross_kwh = gross_kwh
            gt1_net_kwh = net_kwh
        elif 'Plant-2' in asset_name:
            gt2_gross_kwh = gross_kwh
            gt2_net_kwh = net_kwh
        elif 'Plant-3' in asset_name:
            gt3_gross_kwh = gross_kwh
            gt3_net_kwh = net_kwh
        elif 'STG' in asset_name:
            stg_gross_kwh = gross_kwh
            stg_net_kwh = net_kwh
    
    # Power Plant 1 (GT1) - All UtilityName categories share same QTY (gross generation)
    # UtilityName values: POWERGEN, Utilities
    generation_map[('NMD - Power Plant 1', 'POWERGEN')] = gt1_gross_kwh
    generation_map[('NMD - Power Plant 1', 'Utilities')] = gt1_gross_kwh
    
    # Power Plant 2 (GT2) - All UtilityName categories share same QTY
    generation_map[('NMD - Power Plant 2', 'POWERGEN')] = gt2_gross_kwh
    generation_map[('NMD - Power Plant 2', 'Utilities')] = gt2_gross_kwh
    
    # Power Plant 3 (GT3) - All UtilityName categories share same QTY
    generation_map[('NMD - Power Plant 3', 'POWERGEN')] = gt3_gross_kwh
    generation_map[('NMD - Power Plant 3', 'Utilities')] = gt3_gross_kwh
    
    # STG Power Plant - POWERGEN only
    generation_map[('NMD - STG Power Plant', 'POWERGEN')] = stg_gross_kwh
    
    # ============================================================
    # HRSG STEAM GENERATION - in MT
    # ============================================================
    hrsg1_shp = utility_consumption.get('shp_from_hrsg1', 0)
    hrsg2_shp = utility_consumption.get('shp_from_hrsg2', 0)
    hrsg3_shp = utility_consumption.get('shp_from_hrsg3', 0)
    
    generation_map[('NMD - Utility Plant', 'HRSG1_SHP STEAM')] = hrsg1_shp
    generation_map[('NMD - Utility Plant', 'HRSG2_SHP STEAM')] = hrsg2_shp
    generation_map[('NMD - Utility Plant', 'HRSG3_SHP STEAM')] = hrsg3_shp
    
    # ============================================================
    # STG EXTRACTION - in MT
    # ============================================================
    lp_from_stg = stg_extraction.get('lp_from_stg', 0)
    mp_from_stg = stg_extraction.get('mp_from_stg', 0)
    
    # Get calculated ratios (for norm updates)
    lp_stg_ratio = stg_extraction.get('lp_stg_ratio', 0.6134)  # Default to legacy if not present
    mp_stg_ratio = stg_extraction.get('mp_stg_ratio', 0.2908)  # Default to legacy if not present
    
    generation_map[('NMD - Utility Plant', 'STG1_LP STEAM')] = lp_from_stg
    generation_map[('NMD - Utility Plant', 'STG1_MP STEAM')] = mp_from_stg
    
    # STG SHP and Condensate quantities (from lookup table - reverse calculated)
    stg_shp_inlet_mt = stg_extraction.get('stg_shp_inlet_mt', 0)
    stg_condensate_m3 = stg_extraction.get('stg_condensate_m3', 0)
    
    # ============================================================
    # PRDS STEAM - in MT
    # Use STG load-based LP/MP balance if available, otherwise fall back to steam_result
    # ============================================================
    hp_balance = steam_result.get('hp_balance', {}) if steam_result else {}
    
    # Prefer STG load-based LP/MP balance (with calculated ratios)
    if final_lp_balance:
        lp_balance = final_lp_balance
    else:
        lp_balance = steam_result.get('lp_balance', {}) if steam_result else {}
    
    if final_mp_balance:
        mp_balance = final_mp_balance
    else:
        mp_balance = steam_result.get('mp_balance', {}) if steam_result else {}
    
    # Get PRDS ratios from LP/MP balance (these are calculated based on STG load)
    lp_prds_ratio = lp_balance.get('lp_prds_ratio', 1 - lp_stg_ratio)
    mp_prds_ratio = mp_balance.get('mp_prds_ratio', 1 - mp_stg_ratio)
    
    # Override STG ratios from LP/MP balance if available (more accurate)
    if 'lp_stg_ratio' in lp_balance:
        lp_stg_ratio = lp_balance['lp_stg_ratio']
    if 'mp_stg_ratio' in mp_balance:
        mp_stg_ratio = mp_balance['mp_stg_ratio']
    
    generation_map[('NMD - Utility Plant', 'HP Steam PRDS')] = hp_balance.get('hp_from_prds', 0)
    generation_map[('NMD - Utility Plant', 'MP Steam PRDS SHP')] = mp_balance.get('mp_from_prds', 0)
    generation_map[('NMD - Utility Plant', 'LP Steam PRDS')] = lp_balance.get('lp_from_prds', 0)
    
    # ============================================================
    # UTILITY PLANTS
    # ============================================================
    bfw = utility_consumption.get('bfw', {})
    dm = utility_consumption.get('dm_water', {})
    cw = utility_consumption.get('cooling_water', {})
    air = utility_consumption.get('compressed_air', {})
    
    generation_map[('NMD - Utility Plant', 'Boiler Feed Water')] = bfw.get('total_m3', 0)
    generation_map[('NMD - Utility Plant', 'D M Water')] = dm.get('total_m3', 0)
    generation_map[('NMD - Utility Plant', 'Cooling Water 1')] = cw.get('cw1_total_km3', 0)
    generation_map[('NMD - Utility Plant', 'Cooling Water 2')] = cw.get('cw2_total_km3', 0)
    generation_map[('NMD - Utility Plant', 'COMPRESSED AIR')] = air.get('total_nm3', 0)
    generation_map[('NMD - Utility Plant', 'Oxygen')] = utility_consumption.get('oxygen_mt', 0)
    generation_map[('NMD - Utility Plant', 'Effluent Treated')] = utility_consumption.get('effluent_m3', 0)
    
    # ============================================================
    # DISTRIBUTION TOTALS
    # ============================================================
    shp_balance = steam_result.get('shp_balance', {}) if steam_result else {}
    total_power_mwh = power_result.get('totalNetGeneration', 0) + power_result.get('importUnits', 0)
    total_demand_mwh = power_result.get('totalDemandUnits', 0)
    
    generation_map[('NMD - Utility/Power Dist', 'Power_Dis')] = total_demand_mwh * 1000  # Use total demand as QTY for Power_Dis
    generation_map[('NMD - Utility/Power Dist', 'SHP Steam_Dis')] = shp_balance.get('shp_total_demand', 0)
    generation_map[('NMD - Utility/Power Dist', 'LP Steam_Dis')] = lp_balance.get('lp_total', 0)
    generation_map[('NMD - Utility/Power Dist', 'MP Steam_Dis')] = mp_balance.get('mp_total', 0)
    generation_map[('NMD - Utility/Power Dist', 'HP Steam_Dis')] = hp_balance.get('hp_total', 0)
    
    # ============================================================
    # SHP STEAM DISTRIBUTION RATIOS
    # Calculate distribution ratios for SHP Steam_Dis
    # ============================================================
    total_shp_supply = hrsg1_shp + hrsg2_shp + hrsg3_shp
    shp_hrsg1_ratio = (hrsg1_shp / total_shp_supply) if total_shp_supply > 0 else 0
    shp_hrsg2_ratio = (hrsg2_shp / total_shp_supply) if total_shp_supply > 0 else 0
    shp_hrsg3_ratio = (hrsg3_shp / total_shp_supply) if total_shp_supply > 0 else 0
    
    # ============================================================
    # CALCULATED NORMS (Ratios) - for updating Norms column
    # These are the calculated distribution ratios based on STG load
    # ============================================================
    
    # Get STG SHP norm from lookup table (sp_steam_power is MT/MWh, convert to MT/KWh)
    # sp_steam_power = SteamForPower / LoadMW (e.g., 50.78 / 15 = 3.385 MT/MWh)
    # Norm for NMD = sp_steam_power / 1000 (to get MT/KWh)
    sp_steam_power = stg_extraction.get('sp_steam_power', 3.56) if stg_extraction else 3.56
    stg_shp_norm = sp_steam_power / 1000  # Convert MT/MWh to MT/KWh
    stg_condensate_norm = stg_extraction.get('stg_condensate_norm', 0.00293) if stg_extraction else 0.00293
    
    # Get HRSG NG reverse-calculated norms (from heat rate lookup)
    natural_gas = utility_consumption.get('natural_gas', {})
    hrsg1_ng_norm = natural_gas.get('hrsg1_ng_norm', 2.8115696)  # Default to legacy
    hrsg2_ng_norm = natural_gas.get('hrsg2_ng_norm', 2.8115696)  # Default to legacy
    hrsg3_ng_norm = natural_gas.get('hrsg3_ng_norm', 2.8115696)  # Default to legacy
    
    # Get GT NG reverse-calculated norms (from heat rate lookup with free steam deduction)
    # Formula: NET GT MMBTU = GROSS MMBTU - FREE STEAM MMBTU
    gt1_ng_norm = natural_gas.get('gt1_ng_norm', 0.0094715)  # Default to legacy GT3 norm
    gt2_ng_norm = natural_gas.get('gt2_ng_norm', 0.0101463)  # Default to legacy GT2 norm
    gt3_ng_norm = natural_gas.get('gt3_ng_norm', 0.0094715)  # Default to legacy GT3 norm
    
    # ============================================================
    # POWER DISTRIBUTION NORMS (Import Power + POWERGEN)
    # Norms = Power Source NET KWh / Total Demand KWh (including U4U)
    # Use NET generation (after auxiliary consumption) for norms calculation
    # ============================================================
    import_power_kwh = power_result.get('mandatoryImportUsed', 0) * 1000
    total_demand_kwh = total_demand_mwh * 1000 if total_demand_mwh > 0 else 1
    
    # Calculate power distribution norms using NET generation
    # Set norm to 0 if plant is not generating (net_kwh <= 0)
    import_power_norm = import_power_kwh / total_demand_kwh if total_demand_kwh > 0 else 0
    pp1_norm = (gt1_net_kwh / total_demand_kwh) if total_demand_kwh > 0 and gt1_net_kwh > 0 else 0
    pp2_norm = (gt2_net_kwh / total_demand_kwh) if total_demand_kwh > 0 and gt2_net_kwh > 0 else 0
    pp3_norm = (gt3_net_kwh / total_demand_kwh) if total_demand_kwh > 0 and gt3_net_kwh > 0 else 0
    stg_norm = (stg_net_kwh / total_demand_kwh) if total_demand_kwh > 0 and stg_net_kwh > 0 else 0
    
    norms_map = {
        # Power Distribution - calculated norms (Import + POWERGEN)
        ('NMD - Utility/Power Dist', 'Power_Dis', 'Power from MEL'): import_power_norm,
        ('NMD - Utility/Power Dist', 'Power_Dis', 'POWERGEN (PP1)'): pp1_norm,
        ('NMD - Utility/Power Dist', 'Power_Dis', 'POWERGEN (PP2)'): pp2_norm,
        ('NMD - Utility/Power Dist', 'Power_Dis', 'POWERGEN (PP3)'): pp3_norm,
        ('NMD - Utility/Power Dist', 'Power_Dis', 'POWERGEN (STG)'): stg_norm,
        
        # SHP Steam Distribution - calculated ratios
        ('NMD - Utility/Power Dist', 'SHP Steam_Dis', 'HRSG1_SHP STEAM'): shp_hrsg1_ratio,
        ('NMD - Utility/Power Dist', 'SHP Steam_Dis', 'HRSG2_SHP STEAM'): shp_hrsg2_ratio,
        ('NMD - Utility/Power Dist', 'SHP Steam_Dis', 'HRSG3_SHP STEAM'): shp_hrsg3_ratio,
        
        # LP Steam Distribution - calculated ratios
        ('NMD - Utility/Power Dist', 'LP Steam_Dis', 'STG1_LP STEAM'): lp_stg_ratio,
        ('NMD - Utility/Power Dist', 'LP Steam_Dis', 'LP Steam PRDS'): lp_prds_ratio,
        
        # MP Steam Distribution - calculated ratios
        ('NMD - Utility/Power Dist', 'MP Steam_Dis', 'STG1_MP STEAM'): mp_stg_ratio,
        ('NMD - Utility/Power Dist', 'MP Steam_Dis', 'MP Steam PRDS SHP'): mp_prds_ratio,
        
        # STG Power Plant - Reverse calculated norms from lookup table
        # SHP Steam_Dis = SHP inlet from STG (MT per KWH)
        ('NMD - STG Power Plant', 'POWERGEN', 'SHP Steam_Dis'): stg_shp_norm,
        
        # Ret steam condensate = Condensate return from STG (M3 per KWH)
        # Note: This is negative (return/credit), but norm is stored as positive
        ('NMD - STG Power Plant', 'POWERGEN', 'Ret steam condensate'): stg_condensate_norm,
        
        # GT Power Plants - Natural Gas norms (MMBTU per KWH)
        # Reverse calculated from heat rate lookup with free steam deduction
        ('NMD - Power Plant 1', 'POWERGEN', 'NATURAL GAS'): gt1_ng_norm,
        ('NMD - Power Plant 2', 'POWERGEN', 'NATURAL GAS'): gt2_ng_norm,
        ('NMD - Power Plant 3', 'POWERGEN', 'NATURAL GAS'): gt3_ng_norm,
        
        # HRSG Natural Gas - Reverse calculated norms from heat rate lookup
        # NG Norm (MMBTU/MT) = Heat Rate (BTU/lb) × 0.00396567
        ('NMD - Utility Plant', 'HRSG1_SHP STEAM', 'NATURAL GAS'): hrsg1_ng_norm,
        ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'NATURAL GAS'): hrsg2_ng_norm,
        ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'NATURAL GAS'): hrsg3_ng_norm,
    }
    
    return generation_map, norms_map


def print_save_summary(save_result: dict):
    """Print a summary of the save operation."""
    print("\n" + "="*70)
    print("NORMS SAVE SUMMARY")
    print("="*70)
    
    if save_result['success']:
        mode = "DRY RUN" if save_result.get('dry_run') else "SAVED"
        print(f"  Status: {mode}")
        print(f"  Total records: {save_result.get('total', 0)}")
        print(f"  Updated: {save_result['updated']}")
        print(f"  Unchanged: {save_result['same']}")
        
        # Show CPPNorms sync count if available
        cpp_synced = save_result.get('cpp_norms_synced', 0)
        if cpp_synced > 0:
            print(f"  CPPNorms synced: {cpp_synced}")
    else:
        print(f"  Status: FAILED")
        print(f"  Message: {save_result['message']}")
    
    print("="*70)
