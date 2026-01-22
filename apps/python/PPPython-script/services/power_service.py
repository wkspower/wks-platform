import pandas as pd
from database.connection import get_connection


ITERATION_LIMIT = 100
TOLERANCE = 1.0  # MWh acceptable error

# Logging verbosity control
_VERBOSE_LOGGING = True  # Set to False to reduce log output

# STG Power Generation Requirements (per 1 KWh generated)
# To generate 1 KWh from STG, we need:
NORM_STG_POWER_PER_KWH = 0.0020   # 0.0020 KWh of power (auxiliary consumption)
NORM_STG_SHP_PER_KWH = 0.0035600  # 0.00356 MT of SHP steam


# -------------------------------------------------------------
#   HEAT RATE / FREE STEAM LOOKUP HELPERS
# -------------------------------------------------------------
def get_heat_rate_for_load(heat_df: pd.DataFrame, load_mw: float):
    """
    Given the GT load (MW), return (HeatRate, FreeSteamFactor) using:
      - direct match on GTLoad if exists
      - otherwise linear interpolation between nearest GTLoad points
    If heat_df is None/empty or load is invalid => (None, None)
    """
    if heat_df is None or heat_df.empty:
        return None, None

    if load_mw is None:
        return None, None

    df = heat_df

    # Ensure sorted
    df = df.sort_values("GTLoad").reset_index(drop=True)

    min_load = float(df["GTLoad"].iloc[0])
    max_load = float(df["GTLoad"].iloc[-1])

    # Below minimum -> use first row
    if load_mw <= min_load:
        r = df.iloc[0]
        return float(r["HeatRate"]), float(r["FreeSteamFactor"])

    # Above maximum -> use last row
    if load_mw >= max_load:
        r = df.iloc[-1]
        return float(r["HeatRate"]), float(r["FreeSteamFactor"])

    # Try exact match or find surrounding points
    lower = None
    upper = None

    for _, row in df.iterrows():
        val = float(row["GTLoad"])

        if abs(val - load_mw) < 1e-9:
            # Exact match
            return float(row["HeatRate"]), float(row["FreeSteamFactor"])

        if val < load_mw:
            lower = row
        elif val > load_mw and upper is None:
            upper = row
            break

    if lower is None or upper is None:
        # Should not normally happen
        return None, None

    x1 = float(lower["GTLoad"])
    x2 = float(upper["GTLoad"])
    hr1 = float(lower["HeatRate"])
    hr2 = float(upper["HeatRate"])
    fs1 = float(lower["FreeSteamFactor"])
    fs2 = float(upper["FreeSteamFactor"])

    if abs(x2 - x1) < 1e-9:
        # Avoid divide by zero, fallback to lower
        return hr1, fs1

    frac = (load_mw - x1) / (x2 - x1)

    heat = hr1 + frac * (hr2 - hr1)
    steam = fs1 + frac * (fs2 - fs1)

    return heat, steam


# -------------------------------------------------------------
#   RUN ONE DISPATCH EXECUTION FOR A GIVEN GROSS TARGET
#   NEW LOGIC: MIN LOAD FIRST, THEN INCREASE BY PRIORITY
# -------------------------------------------------------------
def _dispatch_once(
    avail_df: pd.DataFrame,
    norms_map: dict,
    demand_units: float,
    heat_df: pd.DataFrame = None
):
    """
    Dispatch power assets based on priority and demand.
    
    NEW DISPATCH RULES (as per discussion):
    ========================================
    1. ALL running assets MUST operate at MIN load first
    2. After MIN load is set, increase assets by PRIORITY to meet remaining demand
    3. Lower priority number = higher dispatch precedence
    4. Only dispatch available assets
    5. If demand < total MIN load, all assets still run at MIN (excess power generated)
    
    PRIORITY NORMALIZATION:
    =======================
    - NULL → 999 (dispatch last)
    - 0 or negative → 1 (convert to minimum valid priority)
    - > 100 → 100 (cap at maximum)
    - Valid range: 1-100 (1 = highest priority)
    
    This ensures:
    - No asset runs below its minimum operating capacity
    - Assets are increased in priority order (not jumped to MAX)
    - Demand is met incrementally, not by maxing out assets
    - Invalid priorities are handled gracefully
    """
    avail = avail_df.copy()
    dispatch = []

    total_gross = 0.0
    total_aux = 0.0
    total_net = 0.0

    def _create_dispatch_entry(r, gross, norms_map, heat_df):
        """Helper to create dispatch entry for an asset."""
        norm = norms_map.get(str(r["AssetId"]).lower(), 0.0)
        aux = gross * norm
        net = gross - aux
        hours = float(r["opHours"])
        asset_name = str(r["AssetName"])
        is_gt = asset_name.upper().startswith("GT") or "PLANT" in asset_name.upper()
        is_stg = "STG" in asset_name.upper() or "STEAM TURBINE" in asset_name.upper()
        
        load_mw = gross / hours if hours > 0 else 0.0
        heat_rate, free_steam = (None, None)
        stg_shp_required, stg_power_required = (None, None)
        
        if is_gt and load_mw > 0:
            heat_rate, free_steam = get_heat_rate_for_load(heat_df, load_mw)
        if is_stg and gross > 0:
            gross_kwh = gross * 1000
            stg_shp_required = gross_kwh * NORM_STG_SHP_PER_KWH
            stg_power_required = gross_kwh * NORM_STG_POWER_PER_KWH / 1000
        
        return {
            "AssetName": asset_name,
            "AssetId": str(r["AssetId"]),
            "Priority": r["Priority"],
            "CapacityMW": float(r["capacity"]),
            "MinMW": float(r["minMW"]),
            "Hours": hours,
            "GrossMWh": round(gross, 6),
            "AuxMWh": round(aux, 6),
            "NetMWh": round(net, 6),
            "AuxFactor": norm,
            "LoadMW": round(load_mw, 6),
            "HeatRate": round(heat_rate, 6) if heat_rate is not None else None,
            "FreeSteam": round(free_steam, 6) if free_steam is not None else None,
            "STG_SHP_Required_MT": round(stg_shp_required, 2) if stg_shp_required is not None else None,
            "STG_Power_Required_MWh": round(stg_power_required, 2) if stg_power_required is not None else None,
        }, aux, net

    # =========================================================
    # STEP 1: ASSIGN MIN LOAD TO ALL AVAILABLE ASSETS
    # =========================================================
    # All running assets MUST operate at minimum load first
    asset_dispatch = {}  # Track dispatch by asset index
    
    for idx, r in avail.iterrows():
        min_energy = float(r["minEnergy"])
        
        # NORMALIZE PRIORITY (handle 0, NULL, negative, out-of-range)
        # Business Rule: Valid priority range is 1-100
        # - NULL → 999 (dispatch last)
        # - 0 or negative → 1 (convert to minimum valid priority)
        # - > 100 → 100 (cap at maximum)
        raw_priority = r["Priority"]
        if pd.isna(raw_priority) or raw_priority is None:
            normalized_priority = 999.0  # NULL → Dispatch last
        elif raw_priority <= 0:
            normalized_priority = 1.0     # 0 or negative → Minimum valid priority
        elif raw_priority > 100:
            normalized_priority = 100.0   # Cap at maximum
        else:
            normalized_priority = float(raw_priority)
        
        asset_dispatch[idx] = {
            "row": r,
            "gross": min_energy,  # Start at MIN load
            "min_energy": min_energy,
            "max_energy": float(r["maxEnergy"]),
            "priority": normalized_priority,  # Use normalized priority
        }
        total_gross += min_energy
    
    # Calculate total MIN load and remaining demand
    total_min_energy = sum(ad["min_energy"] for ad in asset_dispatch.values())
    remaining = float(demand_units) - total_min_energy
    
    # =========================================================
    # STEP 2: INCREASE ASSETS BY PRIORITY TO MEET REMAINING DEMAND
    # =========================================================
    # If remaining > 0, we need to increase some assets above MIN
    # Increase in priority order (lower priority number first)
    # IMPORTANT: Assets with SAME priority should share load EQUALLY
    
    if remaining > 0:
        # Group assets by priority
        from collections import defaultdict
        priority_groups = defaultdict(list)
        for idx, ad in asset_dispatch.items():
            priority_groups[ad["priority"]].append(idx)
        
        # Process priority groups in order (lowest priority number first)
        for priority in sorted(priority_groups.keys()):
            if remaining <= 0:
                break
            
            group_indices = priority_groups[priority]
            
            # Calculate total available increase for this priority group
            group_available = []
            for idx in group_indices:
                ad = asset_dispatch[idx]
                available = ad["max_energy"] - ad["gross"]
                if available > 0:
                    group_available.append((idx, available))
            
            if not group_available:
                continue
            
            # Total available capacity in this group
            total_group_available = sum(avail for _, avail in group_available)
            
            # How much to allocate to this group
            group_allocation = min(remaining, total_group_available)
            
            if len(group_available) == 1:
                # Single asset - give it all
                idx, _ = group_available[0]
                asset_dispatch[idx]["gross"] += group_allocation
                remaining -= group_allocation
            else:
                # Multiple assets with same priority - distribute EQUALLY by loading %
                # Each asset should reach the same loading percentage
                
                # Iteratively increase all assets equally until demand met or one hits MAX
                temp_remaining = group_allocation
                while temp_remaining > 0.1:  # Small threshold to avoid infinite loop
                    # Find assets that can still increase
                    can_increase = []
                    for idx, _ in group_available:
                        ad = asset_dispatch[idx]
                        if ad["gross"] < ad["max_energy"]:
                            can_increase.append(idx)
                    
                    if not can_increase:
                        break
                    
                    # Calculate equal share for each
                    equal_share = temp_remaining / len(can_increase)
                    
                    # Apply increase to each, respecting MAX limit
                    for idx in can_increase:
                        ad = asset_dispatch[idx]
                        available = ad["max_energy"] - ad["gross"]
                        actual_increase = min(equal_share, available)
                        asset_dispatch[idx]["gross"] += actual_increase
                        temp_remaining -= actual_increase
                
                remaining -= (group_allocation - temp_remaining)
    
    # =========================================================
    # STEP 3: CREATE DISPATCH ENTRIES
    # =========================================================
    # Sort by priority for consistent output
    sorted_dispatch = sorted(asset_dispatch.items(), key=lambda x: x[1]["priority"])
    
    total_gross = 0.0
    total_aux = 0.0
    total_net = 0.0
    
    for idx, ad in sorted_dispatch:
        r = ad["row"]
        gross = ad["gross"]
        entry, aux, net = _create_dispatch_entry(r, gross, norms_map, heat_df)
        dispatch.append(entry)
        total_gross += gross
        total_aux += aux
        total_net += net
    
    # Remaining demand (negative means excess generation)
    remaining = max(0.0, float(demand_units) - total_gross)
    
    return dispatch, total_gross, total_aux, total_net, remaining


# -------------------------------------------------------------
#   MAIN DISPATCH FUNCTION (WITH CORRECT CAPACITY CHECK)
#   + HEAT RATE LOOKUP
# -------------------------------------------------------------
def distribute_by_priority(
    month: int, 
    year: int, 
    additional_demand_mwh: float = 0.0, 
    stg_max_mwh: float = None,
    stg_min_override_mwh: float = None,
    gt_reduction_mwh: float = 0.0,
    verbose: bool = None
):
    """
    Dispatch power generation to meet demand.
    
    Args:
        month, year: Financial period
        additional_demand_mwh: Additional demand to add (e.g., utility auxiliary power)
                               This enables the USD iteration loop to converge.
        stg_max_mwh: Maximum STG generation allowed (for SHP balance iteration).
                     If None, no limit is applied.
        stg_min_override_mwh: Override STG minimum generation (for excess steam absorption).
                              If set, STG will run at least this much.
        gt_reduction_mwh: Amount to reduce GT generation by (for power balance when STG increases).
    
    Returns:
        dict with dispatch plan and generation details
    """
    # Use global verbose setting if not specified
    if verbose is None:
        verbose = _VERBOSE_LOGGING
    
    conn = get_connection()
    cur = conn.cursor()

    # Find FYM
    cur.execute("SELECT Id FROM FinancialYearMonth WHERE [Month]=? AND [Year]=?", (month, year))
    row = cur.fetchone()
    if not row:
        conn.close()
        return {"message": f"FYM {month}-{year} not found."}
    fym_id = row[0]

    # NET DEMAND (Plant + Fixed) - Fetch separately for logging
    # Fetch process plant demand from CalculatedProcessDemand
    from services.process_demand_service import get_process_demand_for_month
    process_demands = get_process_demand_for_month(month, year)
    power_process_kwh = process_demands.get("power_process", 0.0)
    # Convert KWH to MWh
    plant_demand = power_process_kwh / 1000.0
    
    # Fetch fixed consumption from UtilityFixedConsumption table via fixed_consumption_service
    from services.fixed_consumption_service import get_fixed_consumption_for_month
    fixed_consumption = get_fixed_consumption_for_month(month, year)
    fixed_demand_kwh = fixed_consumption.get("power_fixed_kwh", 0.0)
    # Convert KWH to MWh
    fixed_demand = fixed_demand_kwh / 1000.0
    
    base_demand = plant_demand + fixed_demand
    
    # U4U power (utility for utility - auxiliary power consumption)
    u4u_power = additional_demand_mwh
    
    # =========================================================
    # POWER DEMAND CALCULATION (CORRECT FLOW)
    # =========================================================
    # Step 1: Base Demand = Process + Fixed
    # Step 2: Total Demand = Base Demand + U4U
    # Step 3: Net Demand (for dispatch) = Total Demand - Import
    # =========================================================
    total_demand = base_demand + u4u_power

    # =========================================================
    # FETCH PLANT ASSETS (NEW DATA STRUCTURE)
    # =========================================================
    # Uses:
    #   - OperationalHours table for hours (availability = hours > 0)
    #   - AssetAvailability table for Priority, MinOperatingCapacity, MaxOperatingCapacity
    #   - PowerGenerationAssets for base asset info (AssetId, AssetName)
    # Note: AssetCapacity is derived from MaxOperatingCapacity or FixedMax
    # =========================================================
    cur.execute("""
        SELECT 
            p.AssetId,
            p.AssetName,
            COALESCE(aa.MaxOperatingCapacity, aa.FixedMax, 22.0) AS AssetCapacity,
            COALESCE(oh.OperationalHours, 0) AS OperationalHours,
            aa.Priority,
            aa.MinOperatingCapacity,
            aa.MaxOperatingCapacity,
            aa.FixedMin,
            aa.FixedMax,
            aa.Id AS AvailabilityId
        FROM PowerGenerationAssets p
        LEFT JOIN OperationalHours oh ON p.AssetId = oh.Asset_FK_Id 
            AND oh.FinancialMonthId = ?
        OUTER APPLY (
            SELECT TOP 1 
                aa2.Id,
                aa2.Priority,
                aa2.MinOperatingCapacity,
                aa2.MaxOperatingCapacity,
                aa2.FixedMin,
                aa2.FixedMax
            FROM AssetAvailability aa2 
            WHERE aa2.AssetId = p.AssetId 
                AND aa2.FinancialYearMonthId = ?
            ORDER BY CASE WHEN aa2.Priority IS NULL THEN 1 ELSE 0 END, aa2.Priority ASC
        ) aa
        WHERE p.AssetType IN ('GT', 'STG')
        ORDER BY aa.Priority ASC
    """, (fym_id, fym_id))
    cols = [c[0] for c in cur.description]
    rows = cur.fetchall()

    if not rows:
        conn.close()
        return {"message": "No assets."}

    df = pd.DataFrame.from_records(rows, columns=cols)
    
    # Asset is available if OperationalHours > 0
    df["IsAvailable"] = df["OperationalHours"].apply(lambda x: 1 if x is not None and float(x) > 0 else 0)
    avail = df[df["IsAvailable"] == 1].copy()

    if avail.empty:
        conn.close()
        return {"message": "No available assets."}

    # Convert numeric plant fields
    # Use MaxOperatingCapacity if available, otherwise fall back to AssetCapacity
    avail["opHours"] = avail["OperationalHours"].fillna(0).astype(float)
    avail["minMW"] = avail["MinOperatingCapacity"].apply(
        lambda x: float(x) if pd.notna(x) else 0.0
    )
    
    # For capacity (Max MW): Use MaxOperatingCapacity if available, else AssetCapacity
    # But ensure capacity is always >= minMW (to avoid invalid Min > Max situations)
    def get_capacity(r):
        if pd.notna(r["MaxOperatingCapacity"]):
            return float(r["MaxOperatingCapacity"])
        else:
            # Fall back to AssetCapacity, but ensure it's at least minMW
            base_cap = float(r["AssetCapacity"]) if pd.notna(r["AssetCapacity"]) else 22.0
            min_mw = float(r["MinOperatingCapacity"]) if pd.notna(r["MinOperatingCapacity"]) else 0.0
            return max(base_cap, min_mw)
    
    avail["capacity"] = avail.apply(get_capacity, axis=1)
    avail["maxEnergy"] = avail["capacity"] * avail["opHours"]
    avail["minEnergy"] = avail["minMW"] * avail["opHours"]  # Minimum energy (MWh)
    avail = avail.reset_index(drop=True)
    
    # Fetch norms early so we can display aux % in asset table
    cur.execute("SELECT Asset_FK_ID, Norms FROM UtilityForUtilityNorms")
    norm_rows = cur.fetchall()
    norms_map = {str(r[0]).lower(): float(r[1]) for r in norm_rows}
    
    # =========================================================
    # POWER GENERATION ASSETS - AVAILABILITY & CAPACITY
    # =========================================================
    total_min_energy = avail["minEnergy"].sum()
    total_max_energy = avail["maxEnergy"].sum()
    excess_power_mwh = 0.0  # Track excess power for export
    
    if verbose:
        print("\n" + "="*100)
        print("  POWER GENERATION ASSETS")
        print("="*100)
        print(f"  {'Asset':<22} {'Avail':<8} {'Pri':<6} {'Min MW':<10} {'Max MW':<10} {'Hours':<8} {'Min MWh':<12} {'Max MWh':<12}")
        print(f"  {'-'*96}")
        
        for _, row in df.iterrows():
            asset_id = str(row["AssetId"]).lower()
            asset_name = row["AssetName"][:21]
            is_available = row["IsAvailable"] == 1
            avail_id = row.get("AvailabilityId", "N/A")
            
            if is_available:
                min_mw = float(row["MinOperatingCapacity"]) if pd.notna(row["MinOperatingCapacity"]) else 0.0
                if pd.notna(row["MaxOperatingCapacity"]):
                    max_mw = float(row["MaxOperatingCapacity"])
                else:
                    max_mw = max(float(row["AssetCapacity"]) if pd.notna(row["AssetCapacity"]) else 22.0, min_mw)
                hours = float(row["OperationalHours"]) if pd.notna(row["OperationalHours"]) else 0.0
                priority = int(row["Priority"]) if pd.notna(row["Priority"]) else 99
                min_energy = min_mw * hours
                max_energy = max_mw * hours
                print(f"  {asset_name:<22} {'YES':<8} {priority:<6} {min_mw:<10.2f} {max_mw:<10.2f} {hours:<8.0f} {min_energy:<12.2f} {max_energy:<12.2f}")
            else:
                print(f"  {asset_name:<22} {'NO':<8} {'-':<6} {0.0:<10.2f} {0.0:<10.2f} {0:<8.0f} {0.0:<12.2f} {0.0:<12.2f}")
        
        print(f"  {'-'*96}")
        print(f"  {'TOTAL':<22} {'':<8} {'':<6} {'':<10} {'':<10} {'':<8} {total_min_energy:<12.2f} {total_max_energy:<12.2f}")
    
    if total_demand < total_min_energy:
        excess_power_mwh = total_min_energy - total_demand
        if verbose:
            print(f"\n  [!] Demand ({total_demand:,.2f}) < Min Energy ({total_min_energy:,.2f}) - Excess: {excess_power_mwh:,.2f} MWh")
    
    # Apply STG limit if specified (for SHP balance iteration)
    if stg_max_mwh is not None:
        for idx, row in avail.iterrows():
            asset_name = str(row["AssetName"]).upper()
            if "STG" in asset_name or "STEAM TURBINE" in asset_name:
                original_max = avail.at[idx, "maxEnergy"]
                avail.at[idx, "maxEnergy"] = min(original_max, max(0, stg_max_mwh))
                if verbose:
                    print(f"  [STG LIMIT] {row['AssetName']}: {original_max:.2f} -> {avail.at[idx, 'maxEnergy']:.2f} MWh")
    
    # Apply STG min override if specified (for excess steam absorption)
    if stg_min_override_mwh is not None and stg_min_override_mwh > 0:
        for idx, row in avail.iterrows():
            asset_name = str(row["AssetName"]).upper()
            if "STG" in asset_name or "STEAM TURBINE" in asset_name:
                original_min = avail.at[idx, "minEnergy"]
                new_min = max(original_min, stg_min_override_mwh)
                avail.at[idx, "minEnergy"] = new_min
                if verbose:
                    print(f"  [STG MIN] {row['AssetName']}: {original_min:.2f} -> {new_min:.2f} MWh")
    
    # Apply GT reduction if specified (for power balance when STG increases)
    if gt_reduction_mwh > 0:
        if verbose:
            print(f"  [GT REDUCTION] Reducing GT capacity by {gt_reduction_mwh:.2f} MWh")
        
        # Get all GT assets
        gt_assets = [(idx, row) for idx, row in avail.iterrows() 
                     if "GT" in str(row["AssetName"]).upper() or "PLANT" in str(row["AssetName"]).upper()]
        
        # Group GTs by priority
        from collections import defaultdict
        priority_groups = defaultdict(list)
        for idx, row in gt_assets:
            priority_groups[row["Priority"]].append((idx, row))
        
        # Process priority groups in reverse order (highest priority number = lowest priority = reduce first)
        remaining_reduction = gt_reduction_mwh
        for priority in sorted(priority_groups.keys(), reverse=True):
            if remaining_reduction <= 0:
                break
            
            group = priority_groups[priority]
            
            # Calculate total available reduction in this group
            group_available = []
            for idx, row in group:
                original_max = avail.at[idx, "maxEnergy"]
                min_energy = avail.at[idx, "minEnergy"]
                available = original_max - min_energy
                if available > 0:
                    group_available.append((idx, row, available))
            
            if not group_available:
                continue
            
            total_group_available = sum(avail for _, _, avail in group_available)
            group_reduction = min(remaining_reduction, total_group_available)
            
            if len(group_available) == 1:
                # Single GT - apply full reduction
                idx, row, _ = group_available[0]
                original_max = avail.at[idx, "maxEnergy"]
                avail.at[idx, "maxEnergy"] = original_max - group_reduction
                remaining_reduction -= group_reduction
            else:
                # Multiple GTs with same priority - distribute EQUALLY
                temp_remaining = group_reduction
                while temp_remaining > 0.1:
                    can_reduce = []
                    for idx, row, _ in group_available:
                        original_max = avail.at[idx, "maxEnergy"]
                        min_energy = avail.at[idx, "minEnergy"]
                        if original_max > min_energy:
                            can_reduce.append((idx, row))
                    
                    if not can_reduce:
                        break
                    
                    equal_share = temp_remaining / len(can_reduce)
                    
                    for idx, row in can_reduce:
                        original_max = avail.at[idx, "maxEnergy"]
                        min_energy = avail.at[idx, "minEnergy"]
                        available = original_max - min_energy
                        actual_reduction = min(equal_share, available)
                        avail.at[idx, "maxEnergy"] = original_max - actual_reduction
                        temp_remaining -= actual_reduction
                
                remaining_reduction -= (group_reduction - temp_remaining)

    # =========================================================
    # IMPORT POWER - From PlantImportMapping Table
    # =========================================================
    # Get import capacity from PlantImportMapping for this month
    # Import MWh = Value (MW) × Operational Hours
    # =========================================================
    operating_hours = float(avail["opHours"].iloc[0]) if not avail.empty else 720.0
    
    cur.execute("""
        SELECT pim.Value, pim.UOM
        FROM PlantImportMapping pim
        WHERE pim.FinancialMonthId = ?
    """, (fym_id,))
    import_row = cur.fetchone()
    
    if import_row and import_row[0]:
        import_capacity_mw = float(import_row[0])
        max_import_mwh = import_capacity_mw * operating_hours
        if verbose:
            print(f"\n  [IMPORT] From PlantImportMapping:")
            print(f"    Capacity: {import_capacity_mw:,.2f} MW")
            print(f"    Hours: {operating_hours:,.0f} hrs")
            print(f"    Available: {max_import_mwh:,.2f} MWh")
        
        # =========================================================
        # RULE: USE IMPORT POWER FIRST
        # But ensure assets run at MINIMUM for steam generation
        # Import = Total Demand - Min Plant Capacity (but not more than available import)
        # =========================================================
        # Assets MUST generate at least their minimum (for HRSG/steam)
        # Import covers the rest
        demand_after_min_plant = max(0, total_demand - total_min_energy)
        actual_import_used_mwh = min(max_import_mwh, demand_after_min_plant)
        if verbose:
            print(f"    Plant MIN: {total_min_energy:,.2f} MWh (must run for steam)")
            print(f"    Using: {actual_import_used_mwh:,.2f} MWh")
    else:
        import_capacity_mw = 0.0
        actual_import_used_mwh = 0.0
        max_import_mwh = 0.0
        if verbose:
            print(f"\n  [IMPORT] No import record found in PlantImportMapping for FYM_Id: {fym_id}")
    
    # =========================================================
    # NET DEMAND FOR DISPATCH = Total Demand - Import Used
    # Assets cover what import doesn't (at least MIN capacity)
    # =========================================================
    net_demand_for_dispatch = total_demand - actual_import_used_mwh
    
    # =========================================================
    # POWER DEMAND SUMMARY
    # =========================================================
    if verbose:
        print("\n" + "="*100)
        print("  POWER DEMAND SUMMARY")
        print("="*100)
        print(f"  Process Plant Demand:     {plant_demand:>12,.2f} MWh")
        print(f"  Fixed Consumption:        {fixed_demand:>12,.2f} MWh")
        print(f"  Base Demand:              {base_demand:>12,.2f} MWh")
        print(f"  U4U Power:                {u4u_power:>12,.2f} MWh")
        print(f"  ---------------------------------------")
        print(f"  TOTAL DEMAND:             {total_demand:>12,.2f} MWh")
        print(f"  (-) Import Power (FIRST): {actual_import_used_mwh:>12,.2f} MWh")
        print(f"  ---------------------------------------")
        print(f"  NET DEMAND (for assets):  {net_demand_for_dispatch:>12,.2f} MWh")

    # HeatRateLookup (same for all GTs)
    cur.execute("""
        SELECT EquipType, CPPUtility, GTLoad, HeatRate, FreeSteamFactor
        FROM HeatRateLookup
    """)
    heat_rows = cur.fetchall()

    heat_df = None
    if heat_rows:
        heat_cols = ["EquipType", "CPPUtility", "GTLoad", "HeatRate", "FreeSteamFactor"]
        heat_df = pd.DataFrame.from_records(heat_rows, columns=heat_cols)

        # Ensure numeric and sorted
        heat_df["GTLoad"] = heat_df["GTLoad"].astype(float)
        heat_df["HeatRate"] = heat_df["HeatRate"].astype(float)
        heat_df["FreeSteamFactor"] = heat_df["FreeSteamFactor"].astype(float)
        heat_df = heat_df.sort_values("GTLoad").reset_index(drop=True)

    conn.close()

    # ----------- HANDLE ZERO/NEGATIVE DEMAND -----------
    if net_demand_for_dispatch <= 0:
        return {
            "month": month,
            "year": year,
            "message": "No demand to fulfill (demand is zero or negative after import).",
            "totalDemandUnits": total_demand,
            "dispatchPlan": [],
            "remainingDemandUnits": 0.0,
            "iterationsUsed": 0,
            "converged": True,
            "totalGrossGeneration": 0.0,
            "totalAuxConsumption": 0.0,
            "totalNetGeneration": 0.0,
            "plantGrossCapabilityUnits": 0.0,
            "importUnits": round(actual_import_used_mwh, 6),
            "totalAvailableUnits": 0.0,
            "iterations": [],
            "iterationDispatch": []
        }

    # ----------- GROSS CAPABILITY CHECK -----------
    plant_gross_capacity = float(avail["maxEnergy"].sum())
    # Check against MAX available capacity (plant + max import)
    total_max_available = plant_gross_capacity + max_import_mwh

    if total_demand > total_max_available + TOLERANCE:
        return {
            "month": month,
            "year": year,
            "message": (
                f"Insufficient capacity: Total Demand={total_demand:.2f} MWh > "
                f"PlantGrossCap={plant_gross_capacity:.2f} MWh + MaxImport={max_import_mwh:.2f} MWh = "
                f"{total_max_available:.2f} MWh."
            ),
            "insufficientCapacity": True,
            "plantGrossCapabilityUnits": plant_gross_capacity,
            "importUnits": max_import_mwh,
            "totalAvailableUnits": total_max_available,
            "dispatchPlan": [],
            "iterations": [],
            "iterationDispatch": []
        }
    
    # Import is already used FIRST (calculated above)
    # Net demand for dispatch = Total Demand - Import Used
    # No recalculation needed here
    total_available_units = plant_gross_capacity + actual_import_used_mwh

    # -------------- DISPATCH ITERATION ----------------
    # Dispatch assets to meet NET DEMAND (Total Demand - Import)
    gross_target = net_demand_for_dispatch
    
    iteration_history = []
    iteration_dispatch_history = []

    final_dispatch = []
    final_total_gross = 0.0
    final_total_aux = 0.0
    final_total_net = 0.0
    converged = False
    iterations_used = 0

    for it in range(1, ITERATION_LIMIT + 1):

        iterations_used = it

        dispatch, total_gross, total_aux, total_net, _ = _dispatch_once(
            avail_df=avail,
            norms_map=norms_map,
            demand_units=gross_target,
            heat_df=heat_df
        )

        # Error is calculated against net_demand_for_dispatch (what assets need to generate NET)
        error = net_demand_for_dispatch - total_net

        iteration_dispatch_history.append({
            "iteration": it,
            "gross_target": round(gross_target, 6),
            "total_gross": round(total_gross, 6),
            "total_aux": round(total_aux, 6),
            "total_net": round(total_net, 6),
            "error": round(error, 6),
            "dispatch": dispatch
        })

        iteration_history.append({
            "iteration": it,
            "gross_target": round(gross_target, 6),
            "total_gross": round(total_gross, 6),
            "total_aux": round(total_aux, 6),
            "total_net": round(total_net, 6),
            "error": round(error, 6),
        })

        final_dispatch = dispatch
        final_total_gross = total_gross
        final_total_aux = total_aux
        final_total_net = total_net

        if abs(error) <= TOLERANCE:
            converged = True
            break
        
        # If we're generating MORE than demand (error < 0), this means assets are running
        # at minCapacity and generating excess. This is expected behavior - mark as converged.
        if error < -TOLERANCE:
            # Excess power generated due to minCapacity constraints
            converged = True
            break

        gross_target += error

        max_possible_gross = avail["maxEnergy"].sum()
        if gross_target > max_possible_gross:
            gross_target = max_possible_gross
        if gross_target < 0:
            gross_target = 0

        if abs(total_gross - max_possible_gross) < 1e-6 and error > 0:
            # Plant is maxed out, but import may still cover the gap
            break

    # Remaining demand after asset generation (should be 0 if converged)
    remaining_after_assets = max(0.0, net_demand_for_dispatch - final_total_net)
    
    # Calculate excess power (generation > demand after import)
    # This happens when assets run at minimum load but demand is low
    excess_power_for_export = max(0.0, final_total_net - net_demand_for_dispatch)
    
    # Total power supplied = Import + Asset Net Generation
    total_power_supplied = actual_import_used_mwh + final_total_net
    
    # =========================================================
    # DISPATCH RESULT SUMMARY
    # =========================================================
    if verbose:
        print("\n" + "="*100)
        print("  POWER DISPATCH RESULT")
        print("="*100)
        print(f"  {'Asset':<20} {'Load MW':<10} {'Gross MWh':<12} {'Aux MWh':<10} {'Net MWh':<12} {'Status':<10}")
        print(f"  {'-'*74}")
        for d in final_dispatch:
            asset_name = d["AssetName"][:19]
            capacity_mw = d["CapacityMW"] if pd.notna(d.get("CapacityMW")) else 0.0
            min_mw = d.get("MinMW", 0.0) if pd.notna(d.get("MinMW")) else 0.0
            load_mw = d["LoadMW"] if pd.notna(d.get("LoadMW")) else 0.0
            gross_mwh = d["GrossMWh"] if pd.notna(d.get("GrossMWh")) else 0.0
            aux_mwh = d["AuxMWh"] if pd.notna(d.get("AuxMWh")) else 0.0
            net_mwh = d["NetMWh"] if pd.notna(d.get("NetMWh")) else 0.0
            
            if gross_mwh == 0:
                status = "OFF"
            elif abs(load_mw - min_mw) < 0.1:
                status = "MIN"
            elif abs(load_mw - capacity_mw) < 0.1:
                status = "MAX"
            else:
                status = "PARTIAL"
            
            print(f"  {asset_name:<20} {load_mw:<10.2f} {gross_mwh:<12.2f} {aux_mwh:<10.2f} {net_mwh:<12.2f} {status:<10}")
        print(f"  {'-'*74}")
        print(f"  {'TOTAL':<20} {'':<10} {final_total_gross:<12.2f} {final_total_aux:<10.2f} {final_total_net:<12.2f}")
    
    # =========================================================
    # POWER BALANCE VERIFICATION
    # =========================================================
    balance_diff = total_power_supplied - total_demand
    if verbose:
        print("\n  POWER BALANCE:")
        print(f"  Demand:  {total_demand:>12,.2f} MWh")
        print(f"  Supply:  {total_power_supplied:>12,.2f} MWh (Import: {actual_import_used_mwh:,.2f} + Net Gen: {final_total_net:,.2f})")
        if abs(balance_diff) < 1:
            print(f"  Status:  BALANCED")
        elif balance_diff > 0:
            print(f"  Status:  EXCESS {balance_diff:,.2f} MWh (for export)")
        else:
            print(f"  Status:  SHORTFALL {abs(balance_diff):,.2f} MWh")

    # With mandatory import, assets only need to cover net_demand_for_dispatch
    # If assets generate enough (or excess), we're converged
    if remaining_after_assets <= TOLERANCE or excess_power_for_export > 0:
        converged = True

    # -----------------------------------------------------------
    # CHECK IF ASSETS CAN COVER REMAINING DEMAND AFTER IMPORT
    # -----------------------------------------------------------
    # Allow deficit up to 1% of demand - the iteration will use actual dispatch values
    # to recalculate U4U, which may reduce demand and allow convergence.
    acceptable_deficit = max(50.0, total_demand * 0.01)  # 50 MWh or 1% of demand
    
    if remaining_after_assets > acceptable_deficit:
        return {
            "month": month,
            "year": year,
            "message": (
                "Demand cannot be fulfilled even after using mandatory import power.\n"
                f"Total Demand: {total_demand:.2f} MWh\n"
                f"Mandatory Import Used: {actual_import_used_mwh:.2f} MWh\n"
                f"Net Demand for Assets: {net_demand_for_dispatch:.2f} MWh\n"
                f"Asset Net Generation: {final_total_net:.2f} MWh\n"
                f"Uncovered deficit: {remaining_after_assets:.2f} MWh\n"
                f"Acceptable deficit: {acceptable_deficit:.2f} MWh"
            ),
            "insufficientCapacityAfterImport": True,
            "remainingNetDemand": remaining_after_assets,
            "importUnits": actual_import_used_mwh,
            "mandatoryImportUsed": actual_import_used_mwh,
            "remainingAfterImport": remaining_after_assets,
            "dispatchPlan": final_dispatch,
            "iterationsUsed": iterations_used,
            "iterationDispatch": iteration_dispatch_history
        }
    
    # Small deficit is acceptable - mark as converged with warning
    if remaining_after_assets > TOLERANCE:
        if verbose:
            print(f"  [!] Small deficit: {remaining_after_assets:.2f} MWh (within tolerance)")
        converged = True  # Accept small deficit

    # -----------------------------------------------------------

    return {
        "month": month,
        "year": year,
        "processDemand": round(plant_demand, 6),
        "fixedDemand": round(fixed_demand, 6),
        "baseDemand": round(base_demand, 6),
        "u4uPower": round(u4u_power, 6),
        "totalDemandUnits": round(total_demand, 6),
        "netDemandForDispatch": round(net_demand_for_dispatch, 6),
        "mandatoryImportUsed": round(actual_import_used_mwh, 6),
        "dispatchPlan": final_dispatch,
        "remainingDemandUnits": 0.0,  # Fully satisfied with import + assets
        "iterationsUsed": iterations_used,
        "converged": converged,
        "totalGrossGeneration": round(final_total_gross, 6),
        "totalAuxConsumption": round(final_total_aux, 6),
        "totalNetGeneration": round(final_total_net, 6),
        "totalPowerSupplied": round(total_power_supplied, 6),
        "plantGrossCapabilityUnits": round(plant_gross_capacity, 6),
        "importUnits": round(actual_import_used_mwh, 6),
        "totalAvailableUnits": round(plant_gross_capacity + actual_import_used_mwh, 6),
        "excessPowerForExport": round(excess_power_for_export, 6),
        "iterations": iteration_history,
        "iterationDispatch": iteration_dispatch_history
    }
