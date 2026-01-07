"""
Save Service - Save Model Calculated Quantities to Database
============================================================
Updates the NormsMonthDetail table with calculated quantities from the model.

Columns updated:
- Quantity: The calculated material quantity (e.g., Natural Gas MMBTU, Power KWH)
- QTY: The base generation quantity (e.g., GT KWH, SHP MT)
"""

from database.connection import get_connection
from decimal import Decimal


def get_fym_id(month: int, year: int) -> str:
    """Get FinancialYearMonth ID for the given month and year."""
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("SELECT Id FROM FinancialYearMonth WHERE Month = ? AND Year = ?", (month, year))
    row = cur.fetchone()
    conn.close()
    if row:
        return str(row[0])
    return None


def get_norms_header_id(plant_name: str, utility_name: str, material_name: str) -> str:
    """
    Get NormsHeader ID for the given plant, utility, and material.
    
    Args:
        plant_name: e.g., 'NMD - Power Plant 2'
        utility_name: e.g., 'POWERGEN', 'Boiler Feed Water'
        material_name: e.g., 'NATURAL GAS', 'Power_Dis'
    
    Returns:
        NormsHeader ID as string, or None if not found
    """
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        SELECT nh.Id
        FROM NormsHeader nh
        INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
        WHERE p.Name = ? 
          AND nh.UtilityName = ? 
          AND nh.MaterialName = ?
          AND nh.IsActive = 1
    """, (plant_name, utility_name, material_name))
    row = cur.fetchone()
    conn.close()
    if row:
        return str(row[0])
    return None


def update_norms_month_detail(
    fym_id: str,
    header_id: str,
    quantity: float = None,
    qty_generation: float = None
) -> bool:
    """
    Update a single NormsMonthDetail record.
    
    Args:
        fym_id: FinancialYearMonth ID
        header_id: NormsHeader ID
        quantity: The calculated material quantity
        qty_generation: The base generation quantity (QTY column)
    
    Returns:
        True if update successful, False otherwise
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # Build dynamic update query based on what's provided
        updates = []
        params = []
        
        if quantity is not None:
            updates.append("Quantity = ?")
            params.append(Decimal(str(round(quantity, 2))))
        
        if qty_generation is not None:
            updates.append("QTY = ?")
            params.append(Decimal(str(round(qty_generation, 2))))
        
        if not updates:
            return False
        
        params.extend([header_id, fym_id])
        
        query = f"""
            UPDATE NormsMonthDetail 
            SET {', '.join(updates)}
            WHERE NormsHeader_FK_Id = ? 
              AND FinancialYearMonth_FK_Id = ?
        """
        
        cur.execute(query, params)
        conn.commit()
        return cur.rowcount > 0
    except Exception as e:
        print(f"Error updating NormsMonthDetail: {e}")
        conn.rollback()
        return False
    finally:
        conn.close()


def save_model_quantities(month: int, year: int, utilities: dict, power_dispatch: dict) -> dict:
    """
    Save all model calculated quantities to the database.
    
    Args:
        month: Month number (e.g., 4 for April)
        year: Year (e.g., 2025)
        utilities: Dictionary from calculate_utility_consumption()
        power_dispatch: Dictionary with power dispatch results
    
    Returns:
        Dictionary with save results (success count, failed count, details)
    """
    fym_id = get_fym_id(month, year)
    if not fym_id:
        return {"success": False, "error": f"FYM not found for {month}/{year}"}
    
    results = {
        "success_count": 0,
        "failed_count": 0,
        "details": []
    }
    
    # Extract values from utilities dict
    ng = utilities.get("natural_gas", {})
    cw = utilities.get("cooling_water", {})
    air = utilities.get("compressed_air", {})
    bfw = utilities.get("bfw", {})
    dm = utilities.get("dm_water", {})
    steam = utilities.get("steam", {})
    condensate = utilities.get("condensate", {})
    
    # Extract power dispatch values
    gt1_kwh = power_dispatch.get("gt1_gross_kwh", 0)
    gt2_kwh = power_dispatch.get("gt2_gross_kwh", 0)
    gt3_kwh = power_dispatch.get("gt3_gross_kwh", 0)
    stg_kwh = power_dispatch.get("stg_gross_kwh", 0)
    
    # HRSG SHP values
    hrsg1_shp = power_dispatch.get("hrsg1_shp_mt", 0)
    hrsg2_shp = power_dispatch.get("hrsg2_shp_mt", 0)
    hrsg3_shp = power_dispatch.get("hrsg3_shp_mt", 0)
    
    # BFW total
    bfw_total = bfw.get("total_m3", 0)
    
    # DM total
    dm_total = dm.get("total_m3", 0)
    
    # CW totals
    cw1_total = cw.get("cw1_total_km3", 0)
    cw2_total = cw.get("cw2_total_km3", 0)
    
    # Air total
    air_total = air.get("total_nm3", 0)
    
    # Oxygen
    oxygen_mt = power_dispatch.get("oxygen_mt", 5786.0)
    
    # Effluent
    effluent_m3 = power_dispatch.get("effluent_m3", 243000.0)
    
    # Steam values
    hp_from_prds = steam.get("hp_from_prds_mt", 0)
    mp_from_prds = steam.get("mp_from_prds_mt", 0)
    lp_from_prds = steam.get("lp_from_prds_mt", 0)
    lp_from_stg = steam.get("lp_from_stg_mt", 0)
    mp_from_stg = steam.get("mp_from_stg_mt", 0)
    
    # Define all mappings: (plant, utility, material, quantity, qty_generation)
    # quantity = calculated material consumption
    # qty_generation = base generation (GT KWH, SHP MT, etc.)
    
    mappings = [
        # ========================================
        # POWER PLANTS
        # ========================================
        # Power Plant 1 (GT1) - Currently 0, but include for completeness
        ("NMD - Power Plant 1", "POWERGEN", "NATURAL GAS", ng.get("gt1_mmbtu", 0), gt1_kwh),
        ("NMD - Power Plant 1", "POWERGEN", "COMPRESSED AIR", air.get("gt1_nm3", 0), gt1_kwh),
        ("NMD - Power Plant 1", "POWERGEN", "Cooling Water 2", cw.get("cw2_gt1_km3", 0), gt1_kwh),
        ("NMD - Power Plant 1", "POWERGEN", "Power_Dis", gt1_kwh * 0.0140, gt1_kwh),
        
        # Power Plant 2 (GT2)
        ("NMD - Power Plant 2", "POWERGEN", "NATURAL GAS", ng.get("gt2_mmbtu", 0), gt2_kwh),
        ("NMD - Power Plant 2", "POWERGEN", "COMPRESSED AIR", air.get("gt2_nm3", 0), gt2_kwh),
        ("NMD - Power Plant 2", "POWERGEN", "Cooling Water 2", cw.get("cw2_gt2_km3", 0), gt2_kwh),
        ("NMD - Power Plant 2", "POWERGEN", "Power_Dis", gt2_kwh * 0.0140, gt2_kwh),
        
        # Power Plant 3 (GT3)
        ("NMD - Power Plant 3", "POWERGEN", "NATURAL GAS", ng.get("gt3_mmbtu", 0), gt3_kwh),
        ("NMD - Power Plant 3", "POWERGEN", "COMPRESSED AIR", air.get("gt3_nm3", 0), gt3_kwh),
        ("NMD - Power Plant 3", "POWERGEN", "Cooling Water 2", cw.get("cw2_gt3_km3", 0), gt3_kwh),
        ("NMD - Power Plant 3", "POWERGEN", "Power_Dis", gt3_kwh * 0.0140, gt3_kwh),
        
        # STG Power Plant
        ("NMD - STG Power Plant", "POWERGEN", "Ret steam condensate", condensate.get("stg_m3", 0), stg_kwh),
        ("NMD - STG Power Plant", "POWERGEN", "COMPRESSED AIR", air.get("stg_nm3", 0), stg_kwh),
        ("NMD - STG Power Plant", "POWERGEN", "Cooling Water 2", cw.get("cw2_stg_km3", 0), stg_kwh),
        ("NMD - STG Power Plant", "POWERGEN", "Power_Dis", stg_kwh * 0.0020, stg_kwh),
        ("NMD - STG Power Plant", "POWERGEN", "SHP Steam_Dis", stg_kwh * 0.0036, stg_kwh),
        
        # ========================================
        # UTILITY PLANT - BFW
        # ========================================
        ("NMD - Utility Plant", "Boiler Feed Water", "D M Water", bfw.get("dm_for_bfw_m3", 0), bfw_total),
        ("NMD - Utility Plant", "Boiler Feed Water", "LP Steam_Dis", bfw.get("lp_steam_mt", 0), bfw_total),
        ("NMD - Utility Plant", "Boiler Feed Water", "Power_Dis", bfw_total * 9.5, bfw_total),
        ("NMD - Utility Plant", "Boiler Feed Water", "Cooling Water 2", 108.0, bfw_total),  # Fixed
        
        # ========================================
        # UTILITY PLANT - COMPRESSED AIR
        # ========================================
        ("NMD - Utility Plant", "COMPRESSED AIR", "Cooling Water 2", 175.0, air_total),  # Fixed
        ("NMD - Utility Plant", "COMPRESSED AIR", "Power_Dis", air_total * 0.165, air_total),
        
        # ========================================
        # UTILITY PLANT - COOLING WATER 1
        # ========================================
        ("NMD - Utility Plant", "Cooling Water 1", "Water", cw1_total * 11.05, cw1_total),
        ("NMD - Utility Plant", "Cooling Water 1", "SULPHURIC ACID", cw1_total * 0.0001580, cw1_total),
        ("NMD - Utility Plant", "Cooling Water 1", "COMPRESSED AIR", 1650.0, cw1_total),  # Fixed
        ("NMD - Utility Plant", "Cooling Water 1", "Power_Dis", cw1_total * 245.0, cw1_total),
        
        # ========================================
        # UTILITY PLANT - COOLING WATER 2
        # ========================================
        ("NMD - Utility Plant", "Cooling Water 2", "Water", cw2_total * 11.50, cw2_total),
        ("NMD - Utility Plant", "Cooling Water 2", "SULPHURIC ACID", cw2_total * 0.0001580, cw2_total),
        ("NMD - Utility Plant", "Cooling Water 2", "COMPRESSED AIR", 1650.0, cw2_total),  # Fixed
        ("NMD - Utility Plant", "Cooling Water 2", "Power_Dis", cw2_total * 250.0, cw2_total),
        
        # ========================================
        # UTILITY PLANT - DM WATER
        # ========================================
        ("NMD - Utility Plant", "D M Water", "Water", dm_total * 1.05, dm_total),
        ("NMD - Utility Plant", "D M Water", "Power_Dis", dm_total * 1.21, dm_total),
        ("NMD - Utility Plant", "D M Water", "Ret steam condensate", dm.get("condensate_m3", 0), dm_total),
        
        # ========================================
        # UTILITY PLANT - EFFLUENT
        # ========================================
        ("NMD - Utility Plant", "Effluent Treated", "Power_Dis", effluent_m3 * 3.54, effluent_m3),
        
        # ========================================
        # UTILITY PLANT - HRSG
        # ========================================
        # HRSG1
        ("NMD - Utility Plant", "HRSG1_SHP STEAM", "NATURAL GAS", ng.get("hrsg1_mmbtu", 0), hrsg1_shp),
        ("NMD - Utility Plant", "HRSG1_SHP STEAM", "Boiler Feed Water", bfw.get("hrsg1_m3", 0), hrsg1_shp),
        ("NMD - Utility Plant", "HRSG1_SHP STEAM", "COMPRESSED AIR", 453600.0 if hrsg1_shp > 0 else 0, hrsg1_shp),
        ("NMD - Utility Plant", "HRSG1_SHP STEAM", "LP Steam_Dis", hrsg1_shp * -0.0504, hrsg1_shp),
        
        # HRSG2
        ("NMD - Utility Plant", "HRSG2_SHP STEAM", "NATURAL GAS", ng.get("hrsg2_mmbtu", 0), hrsg2_shp),
        ("NMD - Utility Plant", "HRSG2_SHP STEAM", "Boiler Feed Water", bfw.get("hrsg2_m3", 0), hrsg2_shp),
        ("NMD - Utility Plant", "HRSG2_SHP STEAM", "COMPRESSED AIR", 453600.0 if hrsg2_shp > 0 else 0, hrsg2_shp),
        ("NMD - Utility Plant", "HRSG2_SHP STEAM", "LP Steam_Dis", hrsg2_shp * -0.0504, hrsg2_shp),
        
        # HRSG3
        ("NMD - Utility Plant", "HRSG3_SHP STEAM", "NATURAL GAS", ng.get("hrsg3_mmbtu", 0), hrsg3_shp),
        ("NMD - Utility Plant", "HRSG3_SHP STEAM", "Boiler Feed Water", bfw.get("hrsg3_m3", 0), hrsg3_shp),
        ("NMD - Utility Plant", "HRSG3_SHP STEAM", "COMPRESSED AIR", 453600.0 if hrsg3_shp > 0 else 0, hrsg3_shp),
        ("NMD - Utility Plant", "HRSG3_SHP STEAM", "LP Steam_Dis", hrsg3_shp * -0.0504, hrsg3_shp),
        
        # ========================================
        # UTILITY PLANT - PRDS
        # ========================================
        # HP PRDS
        ("NMD - Utility Plant", "HP Steam PRDS", "Boiler Feed Water", hp_from_prds * 0.0768, hp_from_prds),
        ("NMD - Utility Plant", "HP Steam PRDS", "SHP Steam_Dis", hp_from_prds * 0.9232, hp_from_prds),
        
        # MP PRDS
        ("NMD - Utility Plant", "MP Steam PRDS SHP", "Boiler Feed Water", mp_from_prds * 0.09, mp_from_prds),
        ("NMD - Utility Plant", "MP Steam PRDS SHP", "SHP Steam_Dis", mp_from_prds * 0.91, mp_from_prds),
        
        # LP PRDS
        ("NMD - Utility Plant", "LP Steam PRDS", "Boiler Feed Water", lp_from_prds * 0.25, lp_from_prds),
        ("NMD - Utility Plant", "LP Steam PRDS", "MP Steam_Dis", lp_from_prds * 0.75, lp_from_prds),
        
        # ========================================
        # UTILITY PLANT - OXYGEN
        # ========================================
        ("NMD - Utility Plant", "Oxygen", "Nitrogen Gas", oxygen_mt * -2448.4, oxygen_mt),  # Byproduct (negative)
        ("NMD - Utility Plant", "Oxygen", "Cooling Water 2", oxygen_mt * 0.261, oxygen_mt),
        ("NMD - Utility Plant", "Oxygen", "Power_Dis", oxygen_mt * 968.65, oxygen_mt),
        
        # ========================================
        # UTILITY PLANT - STG EXTRACTION
        # ========================================
        ("NMD - Utility Plant", "STG1_LP STEAM", "SHP Steam_Dis", lp_from_stg * 0.48, lp_from_stg),
        ("NMD - Utility Plant", "STG1_MP STEAM", "SHP Steam_Dis", mp_from_stg * 0.69, mp_from_stg),
        
        # ========================================
        # UTILITY/POWER DISTRIBUTION
        # ========================================
        ("NMD - Utility/Power Dist", "HP Steam_Dis", "HP Steam PRDS", hp_from_prds, hp_from_prds),
        ("NMD - Utility/Power Dist", "LP Steam_Dis", "LP Steam PRDS", lp_from_prds, lp_from_prds),
        ("NMD - Utility/Power Dist", "LP Steam_Dis", "STG1_LP STEAM", lp_from_stg, lp_from_stg),
        ("NMD - Utility/Power Dist", "MP Steam_Dis", "MP Steam PRDS SHP", mp_from_prds, mp_from_prds),
        ("NMD - Utility/Power Dist", "MP Steam_Dis", "STG1_MP STEAM", mp_from_stg, mp_from_stg),
        ("NMD - Utility/Power Dist", "SHP Steam_Dis", "HRSG1_SHP STEAM", hrsg1_shp, hrsg1_shp),
        ("NMD - Utility/Power Dist", "SHP Steam_Dis", "HRSG2_SHP STEAM", hrsg2_shp, hrsg2_shp),
        ("NMD - Utility/Power Dist", "SHP Steam_Dis", "HRSG3_SHP STEAM", hrsg3_shp, hrsg3_shp),
    ]
    
    # Process each mapping
    print("\n" + "=" * 80)
    print("SAVING MODEL QUANTITIES TO DATABASE")
    print("=" * 80)
    print(f"Month/Year: {month}/{year}")
    print(f"FYM ID: {fym_id}")
    print("-" * 80)
    
    for plant, utility, material, quantity, qty_gen in mappings:
        header_id = get_norms_header_id(plant, utility, material)
        
        if header_id:
            success = update_norms_month_detail(fym_id, header_id, quantity, qty_gen)
            if success:
                results["success_count"] += 1
                results["details"].append({
                    "status": "SUCCESS",
                    "plant": plant,
                    "utility": utility,
                    "material": material,
                    "quantity": quantity,
                    "qty_generation": qty_gen
                })
                print(f"  ✓ {plant} | {utility} | {material}")
            else:
                results["failed_count"] += 1
                results["details"].append({
                    "status": "FAILED",
                    "plant": plant,
                    "utility": utility,
                    "material": material,
                    "error": "Update failed"
                })
                print(f"  ✗ {plant} | {utility} | {material} - Update failed")
        else:
            results["failed_count"] += 1
            results["details"].append({
                "status": "NOT_FOUND",
                "plant": plant,
                "utility": utility,
                "material": material,
                "error": "Header not found"
            })
            print(f"  ? {plant} | {utility} | {material} - Header not found")
    
    print("-" * 80)
    print(f"Total: {results['success_count']} saved, {results['failed_count']} failed")
    print("=" * 80)
    
    return results


def save_budget_results(month: int, year: int, budget_result: dict) -> dict:
    """
    Save budget calculation results to database.
    
    This is a convenience function that extracts the required data from
    the budget result and calls save_model_quantities.
    
    Args:
        month: Month number
        year: Year
        budget_result: Result from calculate_budget_with_usd_iteration()
    
    Returns:
        Save results dictionary
    """
    # Extract utilities from budget result
    utilities = budget_result.get("utilities", {})
    
    # Extract power dispatch info
    usd_result = budget_result.get("usd_result", {})
    final_dispatch = usd_result.get("final_dispatch", [])
    final_steam = usd_result.get("final_steam_balance", {})
    
    # Build power dispatch dict
    power_dispatch = {
        "gt1_gross_kwh": 0,
        "gt2_gross_kwh": 0,
        "gt3_gross_kwh": 0,
        "stg_gross_kwh": 0,
        "hrsg1_shp_mt": 0,
        "hrsg2_shp_mt": 0,
        "hrsg3_shp_mt": 0,
        "oxygen_mt": 5786.0,
        "effluent_m3": 243000.0,
    }
    
    # Extract from dispatch
    for asset in final_dispatch:
        name = str(asset.get("AssetName", "")).upper()
        gross_mwh = asset.get("GrossMWh", 0)
        gross_kwh = gross_mwh * 1000
        
        if "GT1" in name or "POWER PLANT 3" in name:
            power_dispatch["gt1_gross_kwh"] = gross_kwh
        elif "GT2" in name or "POWER PLANT 2" in name:
            power_dispatch["gt2_gross_kwh"] = gross_kwh
        elif "GT3" in name or "POWER PLANT 1" in name:
            power_dispatch["gt3_gross_kwh"] = gross_kwh
        elif "STG" in name:
            power_dispatch["stg_gross_kwh"] = gross_kwh
    
    # Extract HRSG SHP from steam balance
    shp_balance = final_steam.get("shp_balance", {})
    power_dispatch["hrsg1_shp_mt"] = shp_balance.get("shp_from_hrsg1", 0)
    power_dispatch["hrsg2_shp_mt"] = shp_balance.get("shp_from_hrsg2", 0)
    power_dispatch["hrsg3_shp_mt"] = shp_balance.get("shp_from_hrsg3", 0)
    
    return save_model_quantities(month, year, utilities, power_dispatch)
