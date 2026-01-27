"""
USD Iteration Service
Follows the Utility Automation Flowchart V0 - USD Iteration Loop

FLOWCHART STEPS:
================
1. INPUT: Month, Year, Steam Demands
2. Calculate Power Demand (Plant + Fixed + Utility Aux)
3. Check Power Capacity >= Power Demand
4. If NO -> Check Import -> If still insufficient -> ERROR
5. Dispatch Power by Priority (STG first, then GTs)
6. Calculate Steam Demand (LP, MP, HP, SHP)
7. Calculate HRSG Free Steam (from GT dispatch)
8. Check SHP Capacity >= SHP Demand
9. If NO -> Use Supplementary Firing -> If still insufficient -> Reduce STG
10. ITERATE until converged (0.1% tolerance)

NORMS FROM TABLE (Per Unit):
============================
Power Plants (GT1, GT2, GT3):
- Natural Gas: 0.0094-0.0101 MMBTU per KWH
- Compressed Air: 30,960-31,992 NM3 (fixed per month)
- Cooling Water: 108-112 KM3 (fixed per month)
- Power Distribution: 0.0140 KWH per KWH (aux consumption = 1.4%)

STG Power Plant:
- Compressed Air: 41,040-42,408 NM3 (fixed per month)
- Cooling Water: 2,376-2,455 KM3 (fixed per month)
- Power Distribution: 0.0020 KWH per KWH (aux consumption = 0.2%)
- SHP Steam: 0.0036 MT per KWH generated

HRSG (SHP Steam Generation):
- Natural Gas: 2.8064-2.8209 MMBTU per MT SHP
- BFW: 1.0240 M3 per MT SHP
- Compressed Air: 453,600-468,720 NM3 (fixed per month)
- LP Steam Credit: -0.0504 MT per MT SHP (byproduct)

Utility Norms:
- BFW Power: 9.5 KWH per M3
- DM Water Power: 1.21 KWH per M3
- Cooling Water Power: 250 KWH per KM3
- Compressed Air Power: 0.165 KWH per NM3
- Effluent Treatment Power: 3.54 KWH per M3
- Oxygen Power: 936-968 KWH per MT
"""

from services.power_service import distribute_by_priority, NORM_STG_SHP_PER_KWH
from services.steam_service import (
    calculate_steam_balance,
    calculate_lp_balance,
    calculate_mp_balance,
    calculate_lp_balance_stg_based,
    calculate_mp_balance_stg_based,
    get_hrsg_availability_from_dispatch,
    calculate_shp_generation_capacity,
    check_shp_balance,
    calculate_hrsg_min_load_and_excess_steam,
    dispatch_hrsg_load,
    NORM_LP_FROM_STG,
    NORM_MP_FROM_STG,
    NORM_SHP_PER_LP_STG,
    NORM_SHP_PER_MP_STG,
    STEAM_TO_POWER_MT_PER_MWH,
)
from services.demand_service import (
    calculate_all_demands,
    calculate_u4u_power,
    print_demand_summary,
)
from database.connection import get_connection
from database.import_queries import (
    fetch_import_power_availability,
    fetch_stg_min_operating_capacity
)
from database.power_asset_queries import (
    fetch_stg_extraction_lookup,
    get_stg_extraction_for_load,
    get_stg_operating_hours,
    fetch_hrsg_heat_rate_lookup,
    calculate_hrsg_ng_from_heat_rate,
)


# ============================================================
# NORMS CONSTANTS (From Norms Table)
# ============================================================

# Power Plant Auxiliary Consumption (KWH per KWH generated)
NORM_GT_AUX_PER_KWH = 0.0140      # GT1, GT2, GT3: 1.4% aux consumption
NORM_STG_AUX_PER_KWH = 0.0020     # STG: 0.2% aux consumption

# STG Steam Requirement (MT SHP per KWH generated)
NORM_STG_SHP_PER_KWH = 0.0035600  # 0.00356 MT SHP per KWH

# HRSG Norms (per MT SHP generated)
NORM_HRSG_BFW_PER_MT_SHP = 1.0240           # 1.024 M3 BFW per MT SHP
NORM_HRSG_NG_PER_MT_SHP = 2.8115696         # Average of HRSG2 (2.8063807) and HRSG3 (2.8167584)
NORM_HRSG_LP_CREDIT_PER_MT_SHP = -0.0503520 # -0.050352 MT LP credit per MT SHP

# Utility Power Consumption Norms (KWH per unit)
NORM_BFW_POWER_PER_M3 = 9.5000              # 9.5 KWH per M3 BFW
NORM_DM_POWER_PER_M3 = 1.2100               # 1.21 KWH per M3 DM Water
NORM_CW1_POWER_PER_KM3 = 245.0000           # 245 KWH per KM3 Cooling Water 1
NORM_CW2_POWER_PER_KM3 = 250.0000           # 250 KWH per KM3 Cooling Water 2
NORM_AIR_POWER_PER_NM3 = 0.1650             # 0.165 KWH per NM3 Compressed Air
NORM_EFFLUENT_POWER_PER_M3 = 3.5400         # 3.54 KWH per M3 Effluent
NORM_OXYGEN_POWER_PER_MT = 936.0400         # 936 KWH per MT Oxygen

# BFW Consumption Norms (M3 per unit)
NORM_BFW_PER_MT_SHP = 1.0240                # 1.024 M3 BFW per MT SHP (HRSG)
NORM_BFW_PER_MT_HP_PRDS = 0.0768            # 0.0768 M3 BFW per MT HP PRDS
NORM_BFW_PER_MT_MP_PRDS = 0.0900            # 0.09 M3 BFW per MT MP PRDS
NORM_BFW_PER_MT_LP_PRDS = 0.2500            # 0.25 M3 BFW per MT LP PRDS

# DM Water Consumption Norms (M3 per M3 BFW)
NORM_DM_PER_M3_BFW = 0.8600                 # 0.86 M3 DM per M3 BFW

# Cooling Water Consumption Norms (KM3 per unit)
NORM_CW_PER_MT_SHP_STG = 2.4550             # 2.455 KM3 per 1000 MT SHP for STG (scaled)
NORM_CW_PER_KWH_GT = 0.000112               # 112 KM3 per ~1000 MWH GT (scaled)

# Compressed Air Consumption Norms (NM3 per unit)
NORM_AIR_PER_MT_SHP_HRSG = 9.7440           # ~468720 NM3 per ~48000 MT SHP (scaled)

# Iteration Constants
USD_ITERATION_LIMIT = 50
USD_TOLERANCE = 0.0000001  # 0.0001 KWh = 0.0000001 MWh tolerance for aux power convergence


# ============================================================
# UTILITY CALCULATION FUNCTIONS
# ============================================================

def calculate_utility_consumption(
    stg_gross_kwh: float,
    gt_gross_kwh: float,
    shp_from_hrsg_mt: float,
    hp_from_prds_mt: float,
    mp_from_prds_mt: float,
    lp_from_prds_mt: float,
    cw1_process_km3: float = 15194.0,
    cw2_process_km3: float = 9016.0,
) -> dict:
    """
    Calculate utility consumption based on power and steam generation.
    
    Args:
        stg_gross_kwh: STG gross generation (KWH)
        gt_gross_kwh: Total GT gross generation (KWH)
        shp_from_hrsg_mt: SHP steam from HRSG (MT)
        hp_from_prds_mt: HP steam from PRDS (MT)
        mp_from_prds_mt: MP steam from PRDS (MT)
        lp_from_prds_mt: LP steam from PRDS (MT)
        cw1_process_km3: Cooling Water 1 process demand (KM3)
        cw2_process_km3: Cooling Water 2 process demand (KM3)
    
    Returns:
        dict with utility quantities and power consumption
    """
    # BFW Consumption (M3)
    bfw_for_hrsg = shp_from_hrsg_mt * NORM_BFW_PER_MT_SHP
    bfw_for_hp_prds = hp_from_prds_mt * NORM_BFW_PER_MT_HP_PRDS
    bfw_for_mp_prds = mp_from_prds_mt * NORM_BFW_PER_MT_MP_PRDS
    bfw_for_lp_prds = lp_from_prds_mt * NORM_BFW_PER_MT_LP_PRDS
    total_bfw_m3 = bfw_for_hrsg + bfw_for_hp_prds + bfw_for_mp_prds + bfw_for_lp_prds
    
    # DM Water Consumption (M3)
    total_dm_m3 = total_bfw_m3 * NORM_DM_PER_M3_BFW
    
    # Cooling Water 1 Consumption (KM3) - Process demand
    # CW1 is primarily for process plants
    total_cw1_km3 = cw1_process_km3
    
    # Cooling Water 2 Consumption (KM3) - Power plants + Utility plants + Process
    # STG uses significant cooling water (CW2)
    cw2_for_stg = (stg_gross_kwh / 1000000) * 2.455  # ~2.455 KM3 per 1000 MWH
    cw2_for_gt = (gt_gross_kwh / 1000000) * 0.112    # ~0.112 KM3 per 1000 MWH
    cw2_for_bfw = (total_bfw_m3 / 100000) * 0.112    # Cooling for BFW system
    cw2_for_process = cw2_process_km3               # Process demand
    total_cw2_km3 = cw2_for_stg + cw2_for_gt + cw2_for_bfw + cw2_for_process
    
    # Total Cooling Water (for backward compatibility)
    total_cw_km3 = total_cw1_km3 + total_cw2_km3
    
    # Compressed Air Consumption (NM3)
    air_for_stg = 42408.0  # Fixed monthly for STG
    air_for_gt = 31992.0 * 3  # Fixed monthly for 3 GTs (if all running)
    air_for_hrsg = shp_from_hrsg_mt * NORM_AIR_PER_MT_SHP_HRSG
    total_air_nm3 = air_for_stg + air_for_gt + air_for_hrsg
    
    # Utility Power Consumption (KWH) - Separate CW1 and CW2
    power_for_bfw = total_bfw_m3 * NORM_BFW_POWER_PER_M3
    power_for_dm = total_dm_m3 * NORM_DM_POWER_PER_M3
    power_for_cw1 = total_cw1_km3 * NORM_CW1_POWER_PER_KM3
    power_for_cw2 = total_cw2_km3 * NORM_CW2_POWER_PER_KM3
    power_for_cw = power_for_cw1 + power_for_cw2
    power_for_air = total_air_nm3 * NORM_AIR_POWER_PER_NM3
    
    total_utility_power_kwh = power_for_bfw + power_for_dm + power_for_cw + power_for_air
    total_utility_power_mwh = total_utility_power_kwh / 1000
    
    return {
        "bfw": {
            "for_hrsg_m3": round(bfw_for_hrsg, 2),
            "for_hp_prds_m3": round(bfw_for_hp_prds, 2),
            "for_mp_prds_m3": round(bfw_for_mp_prds, 2),
            "for_lp_prds_m3": round(bfw_for_lp_prds, 2),
            "total_m3": round(total_bfw_m3, 2),
        },
        "dm_water": {
            "total_m3": round(total_dm_m3, 2),
        },
        "cooling_water_1": {
            "process_km3": round(cw1_process_km3, 2),
            "total_km3": round(total_cw1_km3, 2),
        },
        "cooling_water_2": {
            "for_stg_km3": round(cw2_for_stg, 2),
            "for_gt_km3": round(cw2_for_gt, 2),
            "for_bfw_km3": round(cw2_for_bfw, 2),
            "process_km3": round(cw2_for_process, 2),
            "total_km3": round(total_cw2_km3, 2),
        },
        "cooling_water": {
            "cw1_total_km3": round(total_cw1_km3, 2),
            "cw2_total_km3": round(total_cw2_km3, 2),
            "total_km3": round(total_cw_km3, 2),
        },
        "compressed_air": {
            "for_stg_nm3": round(air_for_stg, 2),
            "for_gt_nm3": round(air_for_gt, 2),
            "for_hrsg_nm3": round(air_for_hrsg, 2),
            "total_nm3": round(total_air_nm3, 2),
        },
        "utility_power": {
            "for_bfw_kwh": round(power_for_bfw, 2),
            "for_dm_kwh": round(power_for_dm, 2),
            "for_cw1_kwh": round(power_for_cw1, 2),
            "for_cw2_kwh": round(power_for_cw2, 2),
            "for_cw_kwh": round(power_for_cw, 2),
            "for_air_kwh": round(power_for_air, 2),
            "total_kwh": round(total_utility_power_kwh, 2),
            "total_mwh": round(total_utility_power_mwh, 2),
        },
    }


def calculate_stg_shp_demand(stg_gross_mwh: float) -> float:
    """
    Calculate SHP steam demand for STG power generation.
    
    Formula: SHP (MT) = GrossKWh * 0.0036 MT/KWh
    
    Args:
        stg_gross_mwh: STG gross generation in MWh
    
    Returns:
        SHP steam required in MT
    """
    stg_gross_kwh = stg_gross_mwh * 1000
    return stg_gross_kwh * NORM_STG_SHP_PER_KWH


def calculate_stg_extraction_requirements(lp_total: float, mp_total: float) -> dict:
    """
    Calculate STG extraction requirements based on LP and MP demand.
    LEGACY: Uses fixed ratios (61.34% LP, 29.08% MP from STG)
    
    STG extracts:
    - 61.34% of LP demand from STG
    - 29.08% of MP demand from STG
    
    Each extraction requires SHP steam.
    
    Args:
        lp_total: Total LP demand (MT)
        mp_total: Total MP demand (MT)
    
    Returns:
        dict with extraction quantities and SHP requirements
    """
    # LP from STG (61.34%)
    lp_from_stg = lp_total * NORM_LP_FROM_STG
    shp_for_lp_extraction = lp_from_stg * NORM_SHP_PER_LP_STG
    
    # MP from STG (29.08%)
    mp_from_stg = mp_total * NORM_MP_FROM_STG
    shp_for_mp_extraction = mp_from_stg * NORM_SHP_PER_MP_STG
    
    total_shp_for_extraction = shp_for_lp_extraction + shp_for_mp_extraction
    
    return {
        "lp_from_stg": round(lp_from_stg, 2),
        "mp_from_stg": round(mp_from_stg, 2),
        "shp_for_lp_extraction": round(shp_for_lp_extraction, 2),
        "shp_for_mp_extraction": round(shp_for_mp_extraction, 2),
        "total_shp_for_extraction": round(total_shp_for_extraction, 2),
        "lp_stg_ratio": round(NORM_LP_FROM_STG, 4),
        "mp_stg_ratio": round(NORM_MP_FROM_STG, 4),
        "mode": "legacy_fixed_ratio",
        # Legacy defaults for STG SHP and Condensate norms
        "stg_shp_norm": 0.00356,  # Legacy fixed norm
        "stg_condensate_norm": 0.00293,  # Legacy fixed norm
        "stg_shp_inlet_mt": 0,  # Not calculated in legacy mode
        "stg_condensate_m3": 0,  # Not calculated in legacy mode
    }


def calculate_stg_extraction_requirements_load_based(
    lp_total: float, 
    mp_total: float,
    stg_load_mw: float,
    stg_operating_hours: float,
    stg_extraction_lookup_df=None
) -> dict:
    """
    Calculate STG extraction requirements based on STG load (NEW).
    Uses STG extraction lookup table instead of fixed ratios.
    
    LP/MP from STG is determined by STG load from lookup table.
    Remaining LP/MP demand comes from PRDS.
    
    Args:
        lp_total: Total LP demand (MT)
        mp_total: Total MP demand (MT)
        stg_load_mw: Current STG load in MW
        stg_operating_hours: STG operating hours for the month
        stg_extraction_lookup_df: Pre-fetched lookup DataFrame (optional)
    
    Returns:
        dict with extraction quantities and SHP requirements
    """
    # Get extraction rates from lookup table
    extraction = get_stg_extraction_for_load(stg_load_mw, stg_extraction_lookup_df)
    
    lp_extraction_tph = extraction["lp_extraction_tph"]
    mp_extraction_tph = extraction["mp_extraction_tph"]
    shp_inlet_tph = extraction["shp_inlet_tph"]
    condensing_load_m3hr = extraction["condensing_load_m3hr"]
    # New fields for STG SHP calculation
    steam_for_power_tph = extraction.get("steam_for_power_tph", 0)
    sp_steam_power = extraction.get("sp_steam_power", 0)
    eq_svh_mp_tph = extraction.get("eq_svh_mp_tph", 0)
    eq_svh_lp_tph = extraction.get("eq_svh_lp_tph", 0)
    # Use the actual load from lookup (may be clamped to min/max) for consistent norm calculation
    actual_load_mw = extraction.get("load_mw", stg_load_mw)
    
    # Calculate LP from STG (based on extraction rate × hours)
    lp_from_stg_available = lp_extraction_tph * stg_operating_hours
    lp_from_stg = min(lp_from_stg_available, lp_total)  # Cap at demand
    lp_from_prds = max(0, lp_total - lp_from_stg)
    lp_stg_excess = max(0, lp_from_stg_available - lp_total)
    
    # Calculate MP from STG (based on extraction rate × hours)
    mp_from_stg_available = mp_extraction_tph * stg_operating_hours
    mp_from_stg = min(mp_from_stg_available, mp_total)  # Cap at demand
    mp_from_prds = max(0, mp_total - mp_from_stg)
    mp_stg_excess = max(0, mp_from_stg_available - mp_total)
    
    # Calculate actual ratios
    lp_stg_ratio = lp_from_stg / lp_total if lp_total > 0 else 0
    mp_stg_ratio = mp_from_stg / mp_total if mp_total > 0 else 0
    
    # SHP requirements for extraction
    shp_for_lp_extraction = lp_from_stg * NORM_SHP_PER_LP_STG
    shp_for_mp_extraction = mp_from_stg * NORM_SHP_PER_MP_STG
    total_shp_for_extraction = shp_for_lp_extraction + shp_for_mp_extraction
    
    # ============================================================
    # STG SHP INLET (from lookup table - for reverse norm calculation)
    # SHP Steam_Dis = SVHInletTPH × Operating Hours
    # ============================================================
    stg_shp_inlet_mt = shp_inlet_tph * stg_operating_hours
    
    # ============================================================
    # STG CONDENSATE RETURN (from lookup table - for reverse norm calculation)
    # Ret steam condensate = CondensingLoadM3Hr × Operating Hours
    # Note: Condensate is returned (negative consumption)
    # ============================================================
    stg_condensate_m3 = condensing_load_m3hr * stg_operating_hours
    
    # Calculate STG gross KWH for norm calculation
    # IMPORTANT: Use actual_load_mw from lookup (not input stg_load_mw) to ensure consistency
    # The lookup values (shp_inlet_tph, etc.) are for actual_load_mw, so norm must use same load
    stg_gross_kwh = actual_load_mw * stg_operating_hours * 1000  # MW × hours × 1000 = KWH
    
    # Reverse calculate norms (Quantity / STG_Gross_KWH)
    # These norms are consistent with the lookup table values
    stg_shp_norm = stg_shp_inlet_mt / stg_gross_kwh if stg_gross_kwh > 0 else 0.0
    stg_condensate_norm = stg_condensate_m3 / stg_gross_kwh if stg_gross_kwh > 0 else 0.0
    
    return {
        "lp_from_stg": round(lp_from_stg, 2),
        "lp_from_stg_available": round(lp_from_stg_available, 2),
        "lp_from_prds": round(lp_from_prds, 2),
        "lp_stg_excess": round(lp_stg_excess, 2),
        "mp_from_stg": round(mp_from_stg, 2),
        "mp_from_stg_available": round(mp_from_stg_available, 2),
        "mp_from_prds": round(mp_from_prds, 2),
        "mp_stg_excess": round(mp_stg_excess, 2),
        "shp_for_lp_extraction": round(shp_for_lp_extraction, 2),
        "shp_for_mp_extraction": round(shp_for_mp_extraction, 2),
        "total_shp_for_extraction": round(total_shp_for_extraction, 2),
        "lp_stg_ratio": round(lp_stg_ratio, 4),
        "mp_stg_ratio": round(mp_stg_ratio, 4),
        "lp_extraction_tph": round(lp_extraction_tph, 2),
        "mp_extraction_tph": round(mp_extraction_tph, 2),
        "stg_load_mw": round(stg_load_mw, 2),
        "stg_load_mw_actual": round(actual_load_mw, 2),  # Actual load used from lookup (may be clamped)
        "stg_operating_hours": round(stg_operating_hours, 2),
        "interpolated": extraction.get("interpolated", False),
        "clamped": extraction.get("clamped", None),  # 'min' or 'max' if load was clamped
        "mode": "stg_load_based",
        # STG SHP and Condensate (from lookup table)
        "shp_inlet_tph": round(shp_inlet_tph, 2),
        "stg_shp_inlet_mt": round(stg_shp_inlet_mt, 2),
        "stg_shp_norm": round(stg_shp_norm, 7),  # Norm = MT/KWH
        "condensing_load_m3hr": round(condensing_load_m3hr, 2),
        "stg_condensate_m3": round(stg_condensate_m3, 2),
        "stg_condensate_norm": round(stg_condensate_norm, 7),  # Norm = M3/KWH
        "stg_gross_kwh": round(stg_gross_kwh, 2),
        # New fields for STG SHP Steam_Dis calculation
        "steam_for_power_tph": round(steam_for_power_tph, 4),
        "sp_steam_power": round(sp_steam_power, 6),  # MT/MWh
        "eq_svh_mp_tph": round(eq_svh_mp_tph, 4),
        "eq_svh_lp_tph": round(eq_svh_lp_tph, 4),
    }


# ============================================================
# USD ITERATION MAIN FUNCTION
# ============================================================

def usd_iterate(
    month: int,
    year: int,
    lp_process: float,
    lp_fixed: float,
    mp_process: float,
    mp_fixed: float,
    hp_process: float,
    hp_fixed: float,
    shp_process: float,
    shp_fixed: float,
    bfw_ufu: float = 0.0,
    export_available: bool = False,
) -> dict:
    """
    Execute USD iteration loop to balance power and steam.
    
    POWER-STEAM INTERDEPENDENCY (from Flowchart):
    =============================================
    1. STG generates power BUT needs SHP steam (0.0036 MT/KWh)
    2. More STG power → More SHP demand
    3. SHP is generated by HRSG (linked to GT)
    4. More GT → More Free Steam → Less Supplementary Firing needed
    5. If SHP Capacity < SHP Demand → REDUCE STG → Compensate with Import
    
    ITERATION FLOW:
    1. Dispatch Power (STG priority, then GT)
    2. Calculate STG's SHP requirement (based on STG generation)
    3. Calculate Total SHP Demand (Process + STG Power + Extraction)
    4. Calculate HRSG Free Steam (based on GT generation)
    5. Check: Can HRSG meet SHP Demand?
       - YES → Converged
       - NO → Reduce STG, increase GT/Import, repeat
    
    Args:
        month, year: Financial period
        lp/mp/hp/shp_process/fixed: Steam demands (MT)
        bfw_ufu: BFW for UFU (M3)
        
    Returns:
        dict with iteration results, final dispatch, and steam balance
    """
    
    print("\n" + "="*100)
    print("                              USD ITERATION LOOP")
    print("                    (Power-Steam Interdependency Balancing)")
    print("="*100)
    
    # =========================================================
    # STEP 0: FETCH STG EXTRACTION LOOKUP TABLE (Once at start)
    # =========================================================
    print("\n" + "-"*100)
    print("STEP 0: STG EXTRACTION LOOKUP (Load-Based LP/MP Extraction)")
    print("-"*100)
    
    # Fetch STG extraction lookup table (cached for all iterations)
    stg_extraction_lookup_df = fetch_stg_extraction_lookup()
    stg_op_hours = get_stg_operating_hours(month, year)
    
    if stg_extraction_lookup_df.empty:
        print("  [WARNING] STG Extraction Lookup table is empty - using legacy fixed ratios")
        use_stg_load_based = False
    else:
        use_stg_load_based = True
        print(f"  STG Extraction Lookup: {len(stg_extraction_lookup_df)} load points loaded")
        print(f"  STG Operating Hours: {stg_op_hours:.0f} hrs")
        print(f"  Load Range: {stg_extraction_lookup_df['LoadMW'].min():.1f} - {stg_extraction_lookup_df['LoadMW'].max():.1f} MW")
    
    # =========================================================
    # STEP 0b: FETCH HRSG HEAT RATE LOOKUP TABLE (Once at start)
    # =========================================================
    print("\n" + "-"*100)
    print("STEP 0b: HRSG HEAT RATE LOOKUP (For Natural Gas Reverse Calculation)")
    print("-"*100)
    
    # Fetch HRSG heat rate lookup table (cached for all iterations)
    hrsg_heat_rate_lookup_df = fetch_hrsg_heat_rate_lookup()
    
    if hrsg_heat_rate_lookup_df.empty:
        print("  [WARNING] HRSG Heat Rate Lookup table is empty - using legacy fixed norms")
        use_hrsg_heat_rate_lookup = False
    else:
        use_hrsg_heat_rate_lookup = True
        print(f"  HRSG Heat Rate Lookup: {len(hrsg_heat_rate_lookup_df)} records loaded")
        for hrsg_name in hrsg_heat_rate_lookup_df['EquipmentName'].unique():
            hrsg_data = hrsg_heat_rate_lookup_df[hrsg_heat_rate_lookup_df['EquipmentName'] == hrsg_name]
            heat_rate = hrsg_data['HeatRate'].iloc[0]
            from database.power_asset_queries import BTU_LB_TO_MMBTU_MT
            ng_norm = heat_rate * BTU_LB_TO_MMBTU_MT
            print(f"    {hrsg_name}: Heat Rate = {heat_rate:.2f} BTU/lb → NG Norm = {ng_norm:.7f} MMBTU/MT")
    
    # =========================================================
    # STEP 1: CALCULATE FIXED STEAM DEMANDS
    # =========================================================
    print("\n" + "-"*100)
    print("STEP 1: FIXED STEAM DEMANDS (Input)")
    print("-"*100)
    
    # Initial LP/MP balance using legacy fixed ratios
    # (Will be recalculated in iteration loop with STG load-based extraction)
    lp_balance = calculate_lp_balance(lp_process, lp_fixed, bfw_ufu)
    lp_total = lp_balance["lp_total"]
    
    # Calculate MP balance (needs LP PRDS requirement)
    mp_for_lp = lp_balance["mp_for_prds_lp"]
    mp_balance = calculate_mp_balance(mp_process, mp_fixed, mp_for_lp)
    mp_total = mp_balance["mp_total"]
    
    print(f"  +---------------------------+----------------+")
    print(f"  | Steam Type                | Demand (MT)    |")
    print(f"  +---------------------------+----------------+")
    print(f"  | LP Process                | {lp_process:>14.2f} |")
    print(f"  | LP Fixed                  | {lp_fixed:>14.2f} |")
    print(f"  | LP from BFW UFU           | {lp_balance['lp_ufu']:>14.2f} |")
    print(f"  | LP TOTAL                  | {lp_total:>14.2f} |")
    print(f"  +---------------------------+----------------+")
    print(f"  | MP Process                | {mp_process:>14.2f} |")
    print(f"  | MP Fixed                  | {mp_fixed:>14.2f} |")
    print(f"  | MP for LP PRDS            | {mp_for_lp:>14.2f} |")
    print(f"  | MP TOTAL                  | {mp_total:>14.2f} |")
    print(f"  +---------------------------+----------------+")
    print(f"  | HP Process                | {hp_process:>14.2f} |")
    print(f"  | HP Fixed                  | {hp_fixed:>14.2f} |")
    print(f"  +---------------------------+----------------+")
    print(f"  | SHP Process               | {shp_process:>14.2f} |")
    print(f"  | SHP Fixed                 | {shp_fixed:>14.2f} |")
    print(f"  +---------------------------+----------------+")
    
    # =========================================================
    # NORMS REFERENCE (Display all norms used in calculations)
    # =========================================================
    print("\n" + "-"*100)
    print("NORMS REFERENCE (Values used in calculations)")
    print("-"*100)
    
    print("\n  [POWER PLANT NORMS]")
    print(f"  +----------------------------------------+----------------+----------------+")
    print(f"  | Norm Description                       | Value          | Unit           |")
    print(f"  +----------------------------------------+----------------+----------------+")
    print(f"  | GT Auxiliary Consumption               | {NORM_GT_AUX_PER_KWH:>14.4f} | KWH/KWH        |")
    print(f"  | STG Auxiliary Consumption              | {NORM_STG_AUX_PER_KWH:>14.4f} | KWH/KWH        |")
    print(f"  | STG SHP Steam Requirement              | {NORM_STG_SHP_PER_KWH:>14.4f} | MT/KWH         |")
    print(f"  +----------------------------------------+----------------+----------------+")
    
    print("\n  [HRSG NORMS (per MT SHP generated)]")
    print(f"  +----------------------------------------+----------------+----------------+")
    print(f"  | HRSG BFW Consumption                   | {NORM_HRSG_BFW_PER_MT_SHP:>14.4f} | M3/MT SHP      |")
    print(f"  | HRSG Natural Gas                       | {NORM_HRSG_NG_PER_MT_SHP:>14.4f} | MMBTU/MT SHP   |")
    print(f"  | HRSG LP Steam Credit (byproduct)       | {NORM_HRSG_LP_CREDIT_PER_MT_SHP:>14.4f} | MT LP/MT SHP   |")
    print(f"  +----------------------------------------+----------------+----------------+")
    
    print("\n  [UTILITY POWER CONSUMPTION NORMS]")
    print(f"  +----------------------------------------+----------------+----------------+")
    print(f"  | BFW Power                              | {NORM_BFW_POWER_PER_M3:>14.4f} | KWH/M3         |")
    print(f"  | DM Water Power                         | {NORM_DM_POWER_PER_M3:>14.4f} | KWH/M3         |")
    print(f"  | Cooling Water 1 Power                  | {NORM_CW1_POWER_PER_KM3:>14.4f} | KWH/KM3        |")
    print(f"  | Cooling Water 2 Power                  | {NORM_CW2_POWER_PER_KM3:>14.4f} | KWH/KM3        |")
    print(f"  | Compressed Air Power                   | {NORM_AIR_POWER_PER_NM3:>14.4f} | KWH/NM3        |")
    print(f"  | Effluent Treatment Power               | {NORM_EFFLUENT_POWER_PER_M3:>14.4f} | KWH/M3         |")
    print(f"  | Oxygen Power                           | {NORM_OXYGEN_POWER_PER_MT:>14.4f} | KWH/MT         |")
    print(f"  +----------------------------------------+----------------+----------------+")
    
    print("\n  [BFW CONSUMPTION NORMS]")
    print(f"  +----------------------------------------+----------------+----------------+")
    print(f"  | BFW per MT SHP (HRSG)                  | {NORM_BFW_PER_MT_SHP:>14.4f} | M3/MT SHP      |")
    print(f"  | BFW per MT HP PRDS                     | {NORM_BFW_PER_MT_HP_PRDS:>14.4f} | M3/MT HP       |")
    print(f"  | BFW per MT MP PRDS                     | {NORM_BFW_PER_MT_MP_PRDS:>14.4f} | M3/MT MP       |")
    print(f"  | BFW per MT LP PRDS                     | {NORM_BFW_PER_MT_LP_PRDS:>14.4f} | M3/MT LP       |")
    print(f"  +----------------------------------------+----------------+----------------+")
    
    print("\n  [OTHER NORMS]")
    print(f"  +----------------------------------------+----------------+----------------+")
    print(f"  | DM Water per M3 BFW                    | {NORM_DM_PER_M3_BFW:>14.4f} | M3/M3 BFW      |")
    print(f"  | Cooling Water per MT SHP (STG)         | {NORM_CW_PER_MT_SHP_STG:>14.4f} | KM3/1000 MT    |")
    print(f"  +----------------------------------------+----------------+----------------+")
    
    print("\n  [ITERATION PARAMETERS]")
    print(f"  +----------------------------------------+----------------+----------------+")
    print(f"  | Max Iterations                         | {USD_ITERATION_LIMIT:>14} |                |")
    print(f"  | Convergence Tolerance                  | {USD_TOLERANCE:>14.7f} | MWh            |")
    print(f"  +----------------------------------------+----------------+----------------+")
    
    # =========================================================
    # UTILITY DEMAND SUMMARY (Fixed + Process + U4U)
    # =========================================================
    print("\n" + "-"*100)
    print("UTILITY DEMAND SUMMARY (Fixed + Process + U4U)")
    print("-"*100)
    print("  Note: U4U values shown are estimates. Final U4U calculated during iteration.")
    print(f"\n  +----------------------+--------+---------------+---------------+---------------+---------------+")
    print(f"  | Utility              | Unit   | Fixed         | Process       | U4U (Est.)    | Total (Est.)  |")
    print(f"  +----------------------+--------+---------------+---------------+---------------+---------------+")
    
    # Power (from database)
    from services.demand_service import fetch_fixed_process_demands
    db_demands = fetch_fixed_process_demands(month, year)
    power_fixed = db_demands["power"]["fixed"] if db_demands else 0.0
    power_process = db_demands["power"]["process"] if db_demands else 0.0
    power_u4u_est = 15850.0  # Rough estimate for initial display
    power_total_est = power_fixed + power_process + power_u4u_est
    print(f"  | Power                | MWH    | {power_fixed:>13,.2f} | {power_process:>13,.2f} | {power_u4u_est:>13,.2f} | {power_total_est:>13,.2f} |")
    
    # Steam
    print(f"  | SHP Steam            | MT     | {shp_fixed:>13,.2f} | {shp_process:>13,.2f} | {'(calc)':>13} | {'(calc)':>13} |")
    print(f"  | HP Steam             | MT     | {hp_fixed:>13,.2f} | {hp_process:>13,.2f} | {0:>13,.2f} | {hp_fixed + hp_process:>13,.2f} |")
    print(f"  | MP Steam             | MT     | {mp_fixed:>13,.2f} | {mp_process:>13,.2f} | {mp_for_lp:>13,.2f} | {mp_total:>13,.2f} |")
    print(f"  | LP Steam             | MT     | {lp_fixed:>13,.2f} | {lp_process:>13,.2f} | {lp_balance['lp_ufu']:>13,.2f} | {lp_total:>13,.2f} |")
    
    # Other utilities (process values from defaults)
    bfw_process = 0.0
    dm_process = 54779.0
    cw1_process = 15194.0
    cw2_process = 9016.0
    air_process = 6095102.0
    oxygen_process = 5786.0
    effluent_process = 243000.0
    
    print(f"  | BFW                  | M3     | {0:>13,.2f} | {bfw_process:>13,.2f} | {'(calc)':>13} | {'(calc)':>13} |")
    print(f"  | DM Water             | M3     | {0:>13,.2f} | {dm_process:>13,.2f} | {'(calc)':>13} | {'(calc)':>13} |")
    print(f"  | Cooling Water 1      | KM3    | {0:>13,.2f} | {cw1_process:>13,.2f} | {0:>13,.2f} | {cw1_process:>13,.2f} |")
    print(f"  | Cooling Water 2      | KM3    | {0:>13,.2f} | {cw2_process:>13,.2f} | {'(calc)':>13} | {'(calc)':>13} |")
    print(f"  | Compressed Air       | NM3    | {0:>13,.2f} | {air_process:>13,.2f} | {'(calc)':>13} | {'(calc)':>13} |")
    print(f"  | Oxygen               | MT     | {0:>13,.2f} | {oxygen_process:>13,.2f} | {0:>13,.2f} | {oxygen_process:>13,.2f} |")
    print(f"  | Effluent             | M3     | {0:>13,.2f} | {effluent_process:>13,.2f} | {0:>13,.2f} | {effluent_process:>13,.2f} |")
    print(f"  +----------------------+--------+---------------+---------------+---------------+---------------+")
    
    # =========================================================
    # STEP 2: INITIAL STG EXTRACTION ESTIMATE (Will be recalculated per iteration)
    # =========================================================
    print("\n" + "-"*100)
    if use_stg_load_based:
        print("STEP 2: STG EXTRACTION (Initial Estimate - will be recalculated based on STG load)")
    else:
        print("STEP 2: STG EXTRACTION REQUIREMENTS (Fixed Ratios - Legacy Mode)")
    print("-"*100)
    
    # Initial estimate using legacy fixed ratios
    stg_extraction = calculate_stg_extraction_requirements(lp_total, mp_total)
    
    print(f"  +---------------------------+----------------+----------------+")
    print(f"  | Extraction                | Steam (MT)     | SHP Req (MT)   |")
    print(f"  +---------------------------+----------------+----------------+")
    print(f"  | LP from STG ({stg_extraction['lp_stg_ratio']*100:.2f}%)     | {stg_extraction['lp_from_stg']:>14.2f} | {stg_extraction['shp_for_lp_extraction']:>14.2f} |")
    print(f"  | MP from STG ({stg_extraction['mp_stg_ratio']*100:.2f}%)     | {stg_extraction['mp_from_stg']:>14.2f} | {stg_extraction['shp_for_mp_extraction']:>14.2f} |")
    print(f"  +---------------------------+----------------+----------------+")
    print(f"  | TOTAL SHP for Extraction  |                | {stg_extraction['total_shp_for_extraction']:>14.2f} |")
    print(f"  +---------------------------+----------------+----------------+")
    if use_stg_load_based:
        print(f"  Note: Above values are initial estimates. Actual extraction will be")
        print(f"        calculated based on STG load in each iteration.")
    
    # =========================================================
    # STEP 3: USD ITERATION LOOP
    # =========================================================
    print("\n" + "-"*100)
    print("STEP 3: USD ITERATION LOOP (Power-Steam Balancing)")
    print("-"*100)
    print("  Interdependency: STG Power -> SHP Demand -> HRSG Capacity -> GT Generation")
    print("  Convergence: When Power Aux and SHP Balance both stabilize")
    
    iteration_history = []
    converged = False
    
    # Initial values
    final_dispatch = None
    final_power_result = None
    final_steam_balance = None
    final_shp_capacity = None
    final_hrsg_availability = None
    final_lp_balance = None  # STG load-based LP balance
    final_mp_balance = None  # STG load-based MP balance
    final_hrsg_min_load = None  # HRSG MIN load calculation result (backward compatibility)
    final_hrsg_dispatch = None  # HRSG dispatch result (priority-based load allocation)
    final_hrsg_ng_calculation = None  # HRSG Natural Gas reverse calculation result
    
    # KEY: Start with utility aux power = 0, then iterate
    previous_utility_aux_mwh = 0.0
    current_utility_aux_mwh = 0.0
    
    # STG Reduction tracking (for SHP balance)
    stg_reduction_mwh = 0.0  # How much to reduce STG generation
    import_compensation_mwh = 0.0  # How much import power to use instead
    previous_shp_deficit = None
    
    # Get STG max capacity from database (for calculating reduced limit)
    stg_original_max_mwh = None  # Will be set from first dispatch
    
    # Track STG limit based on steam availability
    stg_steam_limit_mwh = None  # Will be calculated based on available SHP
    
    # NEW: Excess steam balancing tracking
    stg_min_override_mwh = None  # Override STG minimum for excess steam absorption
    gt_reduction_for_balance_mwh = 0.0  # GT reduction to balance power when STG increases
    excess_steam_balancing_active = False  # Flag to track if we're in excess steam balancing phase
    power_initially_converged = False  # Flag to track if power has converged at least once
    
    for iteration in range(1, USD_ITERATION_LIMIT + 1):
        
        print(f"\n  {'='*96}")
        print(f"  === ITERATION {iteration} ===")
        print(f"  {'='*96}")
        print(f"  [Input] Previous Utility Aux Power: {previous_utility_aux_mwh:>12.2f} MWh")
        print(f"  [Input] STG Reduction (SHP):        {stg_reduction_mwh:>12.2f} MWh")
        if stg_steam_limit_mwh is not None:
            print(f"  [Input] STG Steam Limit:            {stg_steam_limit_mwh:>12.2f} MWh")
        
        # Calculate STG limit for this iteration
        # Use the more restrictive of: SHP-based reduction OR steam availability limit
        stg_limit_mwh = None
        if stg_reduction_mwh > 0 and stg_original_max_mwh is not None:
            stg_limit_mwh = max(0, stg_original_max_mwh - stg_reduction_mwh)
            print(f"  [Input] STG Limit (from SHP deficit): {stg_limit_mwh:>12.2f} MWh")
        
        # Apply steam-based limit if available (from previous iteration)
        if stg_steam_limit_mwh is not None:
            if stg_limit_mwh is None:
                stg_limit_mwh = stg_steam_limit_mwh
            else:
                stg_limit_mwh = min(stg_limit_mwh, stg_steam_limit_mwh)
            print(f"  [Input] STG Limit (final):          {stg_limit_mwh:>12.2f} MWh")
        
        # ---------------------------------------------------------
        # STEP 3a: Dispatch Power (with utility aux power as additional demand)
        # Power service will print:
        #   - POWER GENERATION ASSETS - AVAILABILITY & CAPACITY
        #   - IMPORT POWER AVAILABILITY
        #   - POWER DISPATCH RESULT
        # ---------------------------------------------------------
        # Log excess steam balancing inputs
        if stg_min_override_mwh is not None:
            print(f"  [Input] STG Min Override (excess steam): {stg_min_override_mwh:>12.2f} MWh")
        if gt_reduction_for_balance_mwh > 0:
            print(f"  [Input] GT Reduction (power balance):    {gt_reduction_for_balance_mwh:>12.2f} MWh")
        
        power_result = distribute_by_priority(
            month, year, 
            additional_demand_mwh=previous_utility_aux_mwh,
            stg_max_mwh=stg_limit_mwh,
            stg_min_override_mwh=stg_min_override_mwh,
            gt_reduction_mwh=gt_reduction_for_balance_mwh
        )
        
        if power_result.get("insufficientCapacity") or power_result.get("insufficientCapacityAfterImport"):
            print(f"  [ERROR] Power dispatch failed: Insufficient capacity")
            return {
                "success": False,
                "error_type": "POWER_INSUFFICIENT",
                "message": power_result.get("message", "Power capacity insufficient"),
                "power_result": power_result,
                "iteration_history": iteration_history,
                "converged": False,
            }
        
        if "dispatchPlan" not in power_result:
            print(f"  [ERROR] Power dispatch failed: No dispatch plan")
            return {
                "success": False,
                "error_type": "POWER_ERROR",
                "message": power_result.get("message", "Power dispatch failed"),
                "power_result": power_result,
                "iteration_history": iteration_history,
                "converged": False,
            }
        
        current_dispatch = power_result["dispatchPlan"]
        total_gross_mwh = power_result.get("totalGrossGeneration", 0)
        total_net_mwh = power_result.get("totalNetGeneration", 0)
        total_demand_mwh = power_result.get("totalDemandUnits", 0)
        total_aux_consumption = power_result.get("totalAuxConsumption", 0)
        excess_power_for_export = power_result.get("excessPowerForExport", 0)
        
        # ---------------------------------------------------------
        # Extract STG and GT details from dispatch (no duplicate printing)
        # ---------------------------------------------------------
        stg_gross_mwh = 0.0
        stg_aux_mwh = 0.0
        stg_net_mwh = 0.0
        stg_shp_required = 0.0
        gt_details = []
        
        for asset in current_dispatch:
            asset_name = asset.get("AssetName", "Unknown")
            asset_upper = asset_name.upper()
            gross = asset.get("GrossMWh", 0)
            aux = asset.get("AuxMWh", 0)
            net = asset.get("NetMWh", 0)
            hours = asset.get("Hours", 0)
            
            if "STG" in asset_upper or "STEAM TURBINE" in asset_upper:
                stg_gross_mwh = gross
                stg_aux_mwh = aux
                stg_net_mwh = net
                stg_shp_required = calculate_stg_shp_demand(stg_gross_mwh)
            elif "GT" in asset_upper or "POWER PLANT" in asset_upper:
                gt_details.append({
                    "name": asset_name,
                    "gross_mwh": gross,
                    "aux_mwh": aux,
                    "net_mwh": net,
                    "load_mw": asset.get("LoadMW", 0),
                    "free_steam": asset.get("FreeSteam", 0),
                    "hours": hours,
                })
        
        gt_gross_mwh = sum(gt["gross_mwh"] for gt in gt_details)
        
        # Capture original STG max from first iteration
        if stg_original_max_mwh is None and stg_gross_mwh > 0:
            for asset in current_dispatch:
                asset_name = str(asset.get("AssetName", "")).upper()
                if "STG" in asset_name or "STEAM TURBINE" in asset_name:
                    stg_capacity = asset.get("CapacityMW", 0)
                    stg_hours = asset.get("Hours", 0)
                    stg_original_max_mwh = stg_capacity * stg_hours
                    break
        
        # ---------------------------------------------------------
        # STEP 3a.1: RECALCULATE STG EXTRACTION BASED ON STG LOAD (NEW)
        # ---------------------------------------------------------
        # Calculate STG load in MW from dispatch
        stg_load_mw = stg_gross_mwh / stg_op_hours if stg_op_hours > 0 else 0.0
        
        if use_stg_load_based and stg_load_mw > 0:
            # Recalculate extraction based on actual STG load
            stg_extraction = calculate_stg_extraction_requirements_load_based(
                lp_total=lp_total,
                mp_total=mp_total,
                stg_load_mw=stg_load_mw,
                stg_operating_hours=stg_op_hours,
                stg_extraction_lookup_df=stg_extraction_lookup_df
            )
            
            # Also recalculate LP and MP balance with STG load-based extraction
            extraction_data = get_stg_extraction_for_load(stg_load_mw, stg_extraction_lookup_df)
            lp_balance = calculate_lp_balance_stg_based(
                lp_process=lp_process,
                lp_fixed=lp_fixed,
                bfw_ufu=bfw_ufu,
                stg_lp_extraction_tph=extraction_data["lp_extraction_tph"],
                stg_operating_hours=stg_op_hours
            )
            mp_for_lp = lp_balance["mp_for_prds_lp"]
            mp_balance = calculate_mp_balance_stg_based(
                mp_process=mp_process,
                mp_fixed=mp_fixed,
                mp_for_lp=mp_for_lp,
                stg_mp_extraction_tph=extraction_data["mp_extraction_tph"],
                stg_operating_hours=stg_op_hours
            )
            
            print(f"\n  [STG EXTRACTION - Load Based]")
            print(f"    STG Load (dispatch): {stg_load_mw:.2f} MW")
            print(f"    STG Load (lookup):   {stg_extraction.get('stg_load_mw_actual', stg_load_mw):.2f} MW" + (" [CLAMPED]" if stg_extraction.get('clamped') else ""))
            print(f"    STG Gross: {stg_extraction.get('stg_gross_kwh', 0):,.0f} KWH")
            print(f"    LP Extraction: {extraction_data['lp_extraction_tph']:.2f} TPH x {stg_op_hours:.0f} hrs = {stg_extraction['lp_from_stg']:.2f} MT")
            print(f"    MP Extraction: {extraction_data['mp_extraction_tph']:.2f} TPH x {stg_op_hours:.0f} hrs = {stg_extraction['mp_from_stg']:.2f} MT")
            print(f"    LP Ratio: {stg_extraction['lp_stg_ratio']*100:.2f}% (vs legacy 61.34%)")
            print(f"    MP Ratio: {stg_extraction['mp_stg_ratio']*100:.2f}% (vs legacy 29.08%)")
            print(f"    --- STG Reverse Norms (from lookup @ {stg_extraction.get('stg_load_mw_actual', stg_load_mw):.2f} MW) ---")
            print(f"    SHP Inlet: {stg_extraction.get('shp_inlet_tph', 0):.2f} TPH x {stg_op_hours:.0f} hrs = {stg_extraction.get('stg_shp_inlet_mt', 0):.2f} MT")
            print(f"    SHP Norm: {stg_extraction.get('stg_shp_norm', 0):.7f} MT/KWH (vs legacy 0.00356)")
            print(f"    Condensate: {stg_extraction.get('condensing_load_m3hr', 0):.2f} M3/hr x {stg_op_hours:.0f} hrs = {stg_extraction.get('stg_condensate_m3', 0):.2f} M3")
            print(f"    Condensate Norm: {stg_extraction.get('stg_condensate_norm', 0):.7f} M3/KWH (vs legacy 0.00293)")
        else:
            # Use legacy fixed ratios
            stg_extraction = calculate_stg_extraction_requirements(lp_total, mp_total)
        
        # ---------------------------------------------------------
        # STEP 3b: HRSG AVAILABILITY & SHP CAPACITY
        # (Linked to GT dispatch - HRSG available when GT is running)
        # ---------------------------------------------------------
        hrsg_availability = get_hrsg_availability_from_dispatch(current_dispatch)
        shp_capacity = calculate_shp_generation_capacity(hrsg_availability)
        
        print("\n" + "="*90)
        print("HRSG AVAILABILITY & SHP CAPACITY")
        print("="*90)
        print("  (HRSG availability linked to GT dispatch - HRSG available when corresponding GT is running)")
        print(f"\n  +----------------+------------+------------+------------+------------+------------+------------+")
        print(f"  | HRSG           | Available  | Hours      | Free Steam | Supp Min   | Supp Max   | Total Max  |")
        print(f"  +----------------+------------+------------+------------+------------+------------+------------+")
        
        hrsg_details_list = shp_capacity.get("hrsg_details", [])
        for hrsg_detail in hrsg_details_list:
            hrsg_name = hrsg_detail.get("name", "Unknown")
            is_avail = "YES" if hrsg_detail.get("is_available", False) else "NO"
            hours = hrsg_detail.get("hours", 0) or 0
            free_steam = hrsg_detail.get("free_steam_mt", 0) or 0
            supp_min = hrsg_detail.get("supp_min_mt_month", 0) or 0
            supp_max = hrsg_detail.get("supp_max_mt_month", 0) or 0
            total_max = free_steam + supp_max
            print(f"  | {hrsg_name:<14} | {is_avail:>10} | {hours:>10.2f} | {free_steam:>10.2f} | {supp_min:>10.2f} | {supp_max:>10.2f} | {total_max:>10.2f} |")
        
        total_free_steam = shp_capacity["total_free_steam_mt"]
        total_supp_min = shp_capacity["total_supplementary_min_mt"]
        total_supp_max = shp_capacity["total_supplementary_max_mt"]
        min_shp_capacity = shp_capacity["total_min_shp_capacity"]
        max_shp_capacity = shp_capacity["total_max_shp_capacity"]
        
        print(f"  +----------------+------------+------------+------------+------------+------------+------------+")
        print(f"  | TOTAL          |            |            | {total_free_steam:>10.2f} | {total_supp_min:>10.2f} | {total_supp_max:>10.2f} | {max_shp_capacity:>10.2f} |")
        print(f"  +----------------+------------+------------+------------+------------+------------+------------+")
        
        # ---------------------------------------------------------
        # STEP 3c: Calculate Steam Balance
        # ---------------------------------------------------------
        print("\n" + "="*90)
        print("STEAM BALANCE CALCULATION")
        print("="*90)
        
        steam_balance = calculate_steam_balance(
            lp_process=lp_process,
            lp_fixed=lp_fixed,
            mp_process=mp_process,
            mp_fixed=mp_fixed,
            hp_process=hp_process,
            hp_fixed=hp_fixed,
            shp_process=shp_process,
            shp_fixed=shp_fixed,
            bfw_ufu=bfw_ufu,
            stg_shp_power=stg_shp_required
        )
        
        shp_demand = steam_balance["summary"]["total_shp_demand"]
        
        # ---------------------------------------------------------
        # STEP 3d: SHP Balance Analysis
        # (HRSG capacity already printed above)
        # ---------------------------------------------------------
        shp_deficit = shp_demand - max_shp_capacity
        
        if shp_demand > 0:
            deficit_percent = (shp_deficit / shp_demand) * 100
            utilization_percent = (shp_demand / max_shp_capacity) * 100 if max_shp_capacity > 0 else 0
        else:
            deficit_percent = 0.0
            utilization_percent = 0.0
        
        # Calculate supplementary firing needed (Free Steam is display only, not subtracted)
        # Full demand must be met by supplementary firing
        supplementary_firing_needed = shp_demand  # Free steam excluded from balance
        
        print("\n" + "="*90)
        print("SHP BALANCE ANALYSIS")
        print("="*90)
        print(f"  +----------------------------------+----------------+")
        print(f"  | SHP DEMAND                       | Value (MT)     |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | SHP Process Demand               | {shp_process:>14.2f} |")
        print(f"  | SHP Fixed Demand                 | {shp_fixed:>14.2f} |")
        print(f"  | SHP for STG Power (0.0036/KWh)   | {stg_shp_required:>14.2f} |")
        print(f"  | SHP for LP Extraction (STG)      | {steam_balance['lp_balance']['shp_for_stg_lp']:>14.2f} |")
        print(f"  | SHP for MP Extraction (STG)      | {steam_balance['mp_balance']['shp_for_stg_mp']:>14.2f} |")
        print(f"  | SHP for HP PRDS                  | {steam_balance['hp_balance']['shp_for_hp_prds']:>14.2f} |")
        print(f"  | SHP for MP PRDS                  | {steam_balance['mp_balance']['shp_for_prds_mp']:>14.2f} |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | TOTAL SHP DEMAND                 | {shp_demand:>14.2f} |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | SHP SUPPLY                       |                |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | Free Steam (display only)        | {total_free_steam:>14.2f} |")
        print(f"  | Supplementary Firing Needed      | {supplementary_firing_needed:>14.2f} |")
        print(f"  | Supplementary Max Capacity       | {total_supp_max:>14.2f} |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | Total Max SHP Capacity           | {max_shp_capacity:>14.2f} |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | SHP DEFICIT (Demand - Capacity)  | {shp_deficit:>14.2f} |")
        print(f"  | Deficit %                        | {deficit_percent:>13.4f}% |")
        print(f"  | Utilization %                    | {utilization_percent:>13.2f}% |")
        print(f"  +----------------------------------+----------------+")
        
        # Check if SHP can be met
        can_meet_shp = shp_deficit <= 0
        print(f"\n  SHP Status: {'CAN MEET DEMAND' if can_meet_shp else 'CANNOT MEET DEMAND - NEED TO REDUCE STG'}")
        
        # ---------------------------------------------------------
        # STEP 3e: HRSG LOAD DISPATCH (Priority-Based)
        # ---------------------------------------------------------
        # Dispatch HRSG supplementary firing based on demand with priority
        hrsg_dispatch_result = dispatch_hrsg_load(
            power_dispatch=current_dispatch,
            shp_demand=shp_demand,
            shp_capacity=shp_capacity
        )
        
        # Store dispatch result for return
        final_hrsg_dispatch = hrsg_dispatch_result
        
        # Get excess steam (if demand < MIN supply)
        excess_steam_mt = hrsg_dispatch_result.get("excess_steam_mt", 0)
        excess_power_from_steam_mwh = excess_steam_mt / STEAM_TO_POWER_MT_PER_MWH if excess_steam_mt > 0 else 0
        
        # Also calculate MIN load result for backward compatibility
        hrsg_min_load_result = calculate_hrsg_min_load_and_excess_steam(
            power_dispatch=current_dispatch,
            shp_demand=shp_demand
        )
        final_hrsg_min_load = hrsg_min_load_result
        
        # ---------------------------------------------------------
        # STEP 3e-2: HRSG NATURAL GAS REVERSE CALCULATION
        # ---------------------------------------------------------
        # Calculate NG consumption for each HRSG using dispatched supp firing
        if use_hrsg_heat_rate_lookup and hrsg_dispatch_result:
            hrsg_ng_results = []
            hrsg_dispatch_list = hrsg_dispatch_result.get("hrsg_dispatch", [])
            
            print("\n" + "-"*90)
            print("HRSG NATURAL GAS REVERSE CALCULATION (From Heat Rate Lookup)")
            print("-"*90)
            print(f"  {'HRSG':<10} {'Supp Fire':<14} {'Hours':<10} {'Flow TPH':<12} {'Heat Rate':<12} {'NG Norm':<14} {'NG Qty':<14}")
            print(f"  {'Name':<10} {'(MT)':<14} {'(hrs)':<10} {'(MT/hr)':<12} {'(BTU/lb)':<12} {'(MMBTU/MT)':<14} {'(MMBTU)':<14}")
            print("  " + "-"*88)
            
            total_ng_from_hrsg = 0.0
            
            for hrsg_data in hrsg_dispatch_list:
                hrsg_name = hrsg_data.get("name", "")
                dispatched_supp = hrsg_data.get("dispatched_supp_mt", 0.0)
                hours = hrsg_data.get("hours", 0.0)
                
                if dispatched_supp > 0:
                    # Calculate NG using heat rate lookup based on dispatched supp firing
                    ng_result = calculate_hrsg_ng_from_heat_rate(
                        hrsg_name=hrsg_name,
                        shp_production_mt=dispatched_supp,
                        operational_hours=hours,
                        lookup_df=hrsg_heat_rate_lookup_df
                    )
                    
                    hrsg_ng_results.append(ng_result)
                    total_ng_from_hrsg += ng_result.get("ng_quantity_mmbtu", 0.0)
                    
                    print(f"  {hrsg_name:<10} {dispatched_supp:>12.2f}   {hours:>8.0f}   {ng_result['steam_flow_tph']:>10.4f}   {ng_result['heat_rate_btu_lb']:>10.2f}   {ng_result['ng_norm_mmbtu_mt']:>12.7f}   {ng_result['ng_quantity_mmbtu']:>12.2f}")
                else:
                    hrsg_ng_results.append({
                        "hrsg_name": hrsg_name,
                        "shp_production_mt": 0.0,
                        "operational_hours": 0.0,
                        "steam_flow_tph": 0.0,
                        "heat_rate_btu_lb": 0.0,
                        "ng_norm_mmbtu_mt": 0.0,
                        "ng_quantity_mmbtu": 0.0,
                        "interpolated": False
                    })
                    print(f"  {hrsg_name:<10} {'N/A - Not Available':<70}")
            
            print("  " + "-"*88)
            print(f"  {'TOTAL':<10} {'':<14} {'':<10} {'':<12} {'':<12} {'':<14} {total_ng_from_hrsg:>12.2f}")
            print("-"*90)
            
            final_hrsg_ng_calculation = {
                "hrsg_ng_details": hrsg_ng_results,
                "total_ng_mmbtu": round(total_ng_from_hrsg, 2),
                "calculation_method": "heat_rate_lookup"
            }
        else:
            # Fallback to legacy fixed norms
            final_hrsg_ng_calculation = {
                "hrsg_ng_details": [],
                "total_ng_mmbtu": 0.0,
                "calculation_method": "legacy_fixed_norm"
            }
        
        # If there's excess steam, we can potentially increase STG to absorb it
        # This creates additional power that may require reducing GT dispatch
        if excess_steam_mt > 0:
            print("\n" + "="*90)
            print("EXCESS STEAM HANDLING (Needs STG/GT Adjustment)")
            print("="*90)
            print(f"  Excess Steam at MIN Load:           {excess_steam_mt:>12.2f} MT")
            print(f"  Potential Extra Power (STG):        {excess_power_from_steam_mwh:>12.2f} MWh")
            print(f"  Conversion Rate:                    {STEAM_TO_POWER_MT_PER_MWH:>12.2f} MT/MWh")
            print(f"  ─────────────────────────────────────────────")
            print(f"  NOTE: HRSGs are at MIN load. Excess steam will be absorbed by:")
            print(f"        1. Increasing STG generation (consumes more SHP)")
            print(f"        2. Reducing GT dispatch (reduces free steam)")
            print(f"        This will be handled in next iteration.")
            print("="*90 + "\n")
        
        # ---------------------------------------------------------
        # DETAILED CALCULATION BREAKDOWN (Show formulas with norms)
        # ---------------------------------------------------------
        print(f"\n  [CALCULATION DETAILS - Using Norms]")
        print(f"  " + "="*90)
        print(f"  | STG SHP Calculation:")
        print(f"  |   STG Gross = {stg_gross_mwh:,.2f} MWh = {stg_gross_mwh * 1000:,.2f} KWh")
        print(f"  |   STG SHP = {stg_gross_mwh * 1000:,.2f} KWh x {NORM_STG_SHP_PER_KWH} MT/KWh = {stg_shp_required:,.2f} MT")
        print(f"  " + "-"*90)
        print(f"  | Free Steam Calculation (per GT):")
        print(f"  |   Formula: Free Steam = GT_Gross_MWh x FreeSteamFactor (from HeatRateLookup)")
        print(f"  |   Total Free Steam = {total_free_steam:,.2f} MT")
        print(f"  " + "-"*90)
        print(f"  | Supplementary Firing Calculation (per HRSG):")
        print(f"  |   Formula: Supp Max = Hours x Max_Capacity_MT/hr x Efficiency")
        for hrsg_detail in hrsg_details_list:
            if hrsg_detail.get("is_available", False):
                h_name = hrsg_detail.get("name", "")
                h_hours = hrsg_detail.get("hours", 0)
                h_max_cap = hrsg_detail.get("max_capacity_per_hr", 136.0)
                h_eff = hrsg_detail.get("efficiency", 1.03)
                h_supp_max = hrsg_detail.get("supp_max_mt_month", 0)
                print(f"  |   {h_name}: {h_hours:.0f} hrs x {h_max_cap} MT/hr x {h_eff} = {h_supp_max:,.2f} MT")
        print(f"  |   Total Supp Max = {total_supp_max:,.2f} MT")
        print(f"  " + "-"*90)
        print(f"  | Total SHP Capacity = Supp Max (Free Steam is display only)")
        print(f"  |                    = {total_supp_max:,.2f} MT")
        print(f"  " + "-"*90)
        print(f"  | Supplementary Firing Needed = SHP Demand (Free Steam not subtracted)")
        print(f"  |                             = {shp_demand:,.2f} MT")
        print(f"  " + "="*90)
        
        # ---------------------------------------------------------
        # STEP 3f: Calculate FULL U4U Power (Power Aux + Utility Power)
        # ---------------------------------------------------------
        # Power Plant Auxiliary (GT + STG aux)
        power_aux_mwh = power_result.get("totalAuxConsumption", 0)
        
        # Extract GT details for U4U calculation
        gt1_gross = 0.0
        gt2_gross = 0.0
        gt3_gross = 0.0
        for gt in gt_details:
            name_upper = gt["name"].upper()
            if "1" in name_upper or "PP1" in name_upper:
                gt1_gross = gt["gross_mwh"]
            elif "2" in name_upper or "PP2" in name_upper:
                gt2_gross = gt["gross_mwh"]
            elif "3" in name_upper or "PP3" in name_upper:
                gt3_gross = gt["gross_mwh"]
        
        # Count available assets for air calculation
        gt_count = len([gt for gt in gt_details if gt["gross_mwh"] > 0])
        stg_available = stg_gross_mwh > 0
        hrsg_count = sum(1 for h in hrsg_details_list if h.get("is_available", False))
        
        # Calculate utility quantities for U4U power
        # These must match the NMD output calculations
        
        # BFW = HRSG BFW + PRDS BFW + Fixed (300 M3)
        # HRSG BFW based on supplementary firing only (free steam is display only)
        total_shp_from_hrsg = supplementary_firing_needed  # Free steam excluded
        bfw_hrsg = total_shp_from_hrsg * NORM_BFW_PER_MT_SHP
        bfw_hp_prds = hp_process * 0.0768
        bfw_mp_prds = mp_total * 0.09
        bfw_lp_prds = lp_total * 0.25
        bfw_fixed = 300.0  # Fixed BFW consumption
        bfw_total_estimate = bfw_hrsg + bfw_hp_prds + bfw_mp_prds + bfw_lp_prds + bfw_fixed
        
        # DM = 0.86 * BFW + Process DM (54,779 M3)
        dm_for_bfw = bfw_total_estimate * NORM_DM_PER_M3_BFW
        dm_process = 54779.0  # Process DM consumption
        dm_total_estimate = dm_for_bfw + dm_process
        
        # CW1 = Process (fixed)
        cw1_total_estimate = 15194.0  # Process demand
        
        # CW2 = Fixed (Power Plants) + Variable (STG/GT) + Process
        # Fixed CW2: GT = 108 KM3 each when running, STG = 2376 KM3 when running
        cw2_gt_fixed = gt_count * 108.0  # 108 KM3 per GT
        cw2_stg_fixed = 2376.0 if stg_available else 0.0  # 2376 KM3 for STG
        cw2_bfw_fixed = 108.0  # BFW plant CW2
        cw2_air_fixed = 175.0  # Compressed Air plant CW2
        # Variable CW2 based on oxygen production
        cw2_oxygen = 5786.0 * 0.2610  # Oxygen CW2 (0.261 KM3/MT)
        cw2_process = 9016.0  # Process CW2
        cw2_total_estimate = cw2_gt_fixed + cw2_stg_fixed + cw2_bfw_fixed + cw2_air_fixed + cw2_oxygen + cw2_process
        
        # Compressed Air = GT + STG + HRSG + CW1 + CW2 + Process
        air_gt = gt_count * 30960.0
        air_stg = 41040.0 if stg_available else 0.0
        air_hrsg = hrsg_count * 453600.0
        air_cw1 = 1650.0  # CW1 air
        air_cw2 = 1650.0  # CW2 air
        air_process = 6095102.0  # Process air
        air_total_estimate = air_gt + air_stg + air_hrsg + air_cw1 + air_cw2 + air_process
        
        # Oxygen and Effluent (fixed process values)
        oxygen_total = 5786.0
        effluent_total = 243000.0
        
        # Calculate U4U Power from utilities
        u4u_power = calculate_u4u_power(
            gt1_gross_mwh=gt1_gross,
            gt2_gross_mwh=gt2_gross,
            gt3_gross_mwh=gt3_gross,
            stg_gross_mwh=stg_gross_mwh,
            bfw_total_m3=bfw_total_estimate,
            dm_total_m3=dm_total_estimate,
            cw1_total_km3=cw1_total_estimate,
            cw2_total_km3=cw2_total_estimate,
            air_total_nm3=air_total_estimate,
            oxygen_total_mt=oxygen_total,
            effluent_total_m3=effluent_total,
        )
        
        # Total U4U = Power Aux + Utility Power
        utility_power_mwh = u4u_power["utility_power"]["total_mwh"]
        current_utility_aux_mwh = power_aux_mwh + utility_power_mwh
        aux_power_error = abs(current_utility_aux_mwh - previous_utility_aux_mwh)
        
        # Print U4U breakdown
        print("\n" + "="*90)
        print("U4U POWER CALCULATION (Power Aux + Utility Power)")
        print("="*90)
        print(f"  +----------------------------------+----------------+")
        print(f"  | Component                        | Power (MWH)    |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | Power Plant Auxiliary            | {power_aux_mwh:>14.2f} |")
        print(f"  |   - GT1 Aux                      | {u4u_power['power_aux']['gt1_kwh']/1000:>14.2f} |")
        print(f"  |   - GT2 Aux                      | {u4u_power['power_aux']['gt2_kwh']/1000:>14.2f} |")
        print(f"  |   - GT3 Aux                      | {u4u_power['power_aux']['gt3_kwh']/1000:>14.2f} |")
        print(f"  |   - STG Aux                      | {u4u_power['power_aux']['stg_kwh']/1000:>14.2f} |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | Utility Power                    | {utility_power_mwh:>14.2f} |")
        print(f"  |   - BFW Power                    | {u4u_power['utility_power']['bfw_kwh']/1000:>14.2f} |")
        print(f"  |   - DM Power                     | {u4u_power['utility_power']['dm_kwh']/1000:>14.2f} |")
        print(f"  |   - CW1 Power                    | {u4u_power['utility_power']['cw1_kwh']/1000:>14.2f} |")
        print(f"  |   - CW2 Power                    | {u4u_power['utility_power']['cw2_kwh']/1000:>14.2f} |")
        print(f"  |   - Air Power                    | {u4u_power['utility_power']['air_kwh']/1000:>14.2f} |")
        print(f"  |   - Oxygen Power                 | {u4u_power['utility_power']['oxygen_kwh']/1000:>14.2f} |")
        print(f"  |   - Effluent Power               | {u4u_power['utility_power']['effluent_kwh']/1000:>14.2f} |")
        print(f"  +----------------------------------+----------------+")
        print(f"  | TOTAL U4U POWER                  | {current_utility_aux_mwh:>14.2f} |")
        print(f"  +----------------------------------+----------------+")
        
        # Calculate SHP deficit change
        shp_deficit_error = 0.0
        if previous_shp_deficit is not None:
            shp_deficit_error = abs(shp_deficit - previous_shp_deficit)
        
        print("\n" + "="*90)
        print("CONVERGENCE CHECK")
        print("="*90)
        print(f"  +----------------------------------+----------------+----------------+")
        print(f"  | Metric                           | Current        | Previous       |")
        print(f"  +----------------------------------+----------------+----------------+")
        print(f"  | Power Aux (MWh)                  | {current_utility_aux_mwh:>14.4f} | {previous_utility_aux_mwh:>14.4f} |")
        print(f"  | Power Aux Error (MWh)            | {aux_power_error:>14.6f} |                |")
        print(f"  | SHP Deficit (MT)                 | {shp_deficit:>14.2f} | {previous_shp_deficit or 0:>14.2f} |")
        print(f"  | SHP Deficit Error (MT)           | {shp_deficit_error:>14.2f} |                |")
        print(f"  | STG Reduction (MWh)              | {stg_reduction_mwh:>14.2f} |                |")
        print(f"  | Import Compensation (MWh)        | {import_compensation_mwh:>14.2f} |                |")
        print(f"  +----------------------------------+----------------+----------------+")
        print(f"  | Tolerance (MWh)                  | {USD_TOLERANCE:>14.6f} |                |")
        print(f"  +----------------------------------+----------------+----------------+")
        
        # Record iteration
        iteration_record = {
            "iteration": iteration,
            "total_demand_mwh": round(total_demand_mwh, 2),
            "total_gross_mwh": round(total_gross_mwh, 2),
            "total_net_mwh": round(total_net_mwh, 2),
            "stg_gross_mwh": round(stg_gross_mwh, 2),
            "gt_gross_mwh": round(gt_gross_mwh, 2),
            "stg_shp_required_mt": round(stg_shp_required, 2),
            "shp_demand_mt": round(shp_demand, 2),
            "free_steam_mt": round(total_free_steam, 2),
            "supplementary_firing_mt": round(supplementary_firing_needed, 2),
            "max_shp_capacity_mt": round(max_shp_capacity, 2),
            "shp_deficit_mt": round(shp_deficit, 2),
            "deficit_percent": round(deficit_percent, 4),
            "utilization_percent": round(utilization_percent, 2),
            "previous_aux_mwh": round(previous_utility_aux_mwh, 4),
            "current_aux_mwh": round(current_utility_aux_mwh, 4),
            "aux_power_error_mwh": round(aux_power_error, 6),
            "stg_reduction_mwh": round(stg_reduction_mwh, 2),
            "import_compensation_mwh": round(import_compensation_mwh, 2),
            "action": None,
            "status": "PENDING",
        }
        
        # ---------------------------------------------------------
        # STEP 3g: Check Convergence
        # Converged when BOTH:
        # 1. Power Aux stabilized (error <= tolerance)
        # 2. SHP can be met (deficit <= 0)
        # ---------------------------------------------------------
        power_converged = aux_power_error <= USD_TOLERANCE
        shp_converged = can_meet_shp
        
        # ---------------------------------------------------------
        # STEP 3g.1: EXCESS STEAM BALANCING (NEW DISPATCH LOGIC)
        # If HRSG MIN load produces excess steam:
        # 1. Increase STG to absorb excess steam (convert to power)
        # 2. Reduce GT dispatch to maintain power balance
        # IMPORTANT: Only start balancing AFTER power has initially converged
        # ---------------------------------------------------------
        stg_increased = False
        excess_steam_adjustment_mwh = 0.0
        
        # Check if power has initially converged (aux error is small enough)
        if aux_power_error < 10.0:  # Power is close to converged
            power_initially_converged = True
        
        if excess_steam_mt > 0 and power_initially_converged:
            # Calculate how much STG can increase to absorb excess steam
            # Excess power from steam = excess_steam_mt / 3.56 MT/MWh
            potential_stg_increase = excess_power_from_steam_mwh
            
            # Check STG capacity limits
            stg_db_min_mwh = 0.0
            stg_db_max_mwh = 0.0
            stg_current_mwh = stg_gross_mwh
            
            for asset in current_dispatch:
                asset_name = str(asset.get("AssetName", "")).upper()
                if "STG" in asset_name or "STEAM TURBINE" in asset_name:
                    stg_db_min_mwh = asset.get("MinMW", 5.0) * asset.get("Hours", 720)
                    stg_db_max_mwh = asset.get("CapacityMW", 25) * asset.get("Hours", 720)
                    break
            
            # Calculate how much STG can actually increase
            stg_available_increase = stg_db_max_mwh - stg_current_mwh
            actual_stg_increase = min(potential_stg_increase, stg_available_increase)
            
            # Check if GTs can be reduced further (not below MIN)
            gt_available_reduction = 0.0
            for asset in current_dispatch:
                asset_name = str(asset.get("AssetName", "")).upper()
                if "GT" in asset_name or "PLANT" in asset_name:
                    gt_current = asset.get("GrossMWh", 0)
                    gt_min = asset.get("MinMW", 5.0) * asset.get("Hours", 720)
                    gt_available_reduction += max(0, gt_current - gt_min)
            
            # Actual increase is limited by both STG capacity AND GT reduction available
            actual_stg_increase = min(actual_stg_increase, gt_available_reduction)
            
            if actual_stg_increase > 50:  # Only if meaningful (> 50 MWh)
                print("\n" + "="*90)
                print("⚡ EXCESS STEAM BALANCING (ITERATIVE)")
                print("="*90)
                print(f"  Iteration:                          {iteration}")
                print(f"  Excess Steam from HRSG MIN Load:    {excess_steam_mt:>12.2f} MT")
                print(f"  Potential STG Increase:             {potential_stg_increase:>12.2f} MWh")
                print(f"  STG Current:                        {stg_current_mwh:>12.2f} MWh")
                print(f"  STG Max Capacity:                   {stg_db_max_mwh:>12.2f} MWh")
                print(f"  STG Available Increase:             {stg_available_increase:>12.2f} MWh")
                print(f"  GT Available Reduction:             {gt_available_reduction:>12.2f} MWh")
                print(f"  Actual STG Increase:                {actual_stg_increase:>12.2f} MWh")
                print(f"  ─────────────────────────────────────────────")
                
                # Calculate GT reduction needed to maintain power balance
                gt_reduction_needed = actual_stg_increase
                print(f"  GT Reduction Needed:                {gt_reduction_needed:>12.2f} MWh")
                print(f"  ─────────────────────────────────────────────")
                print(f"  ACTION: Will increase STG and reduce GT in next iteration")
                print("="*90 + "\n")
                
                # SET VALUES FOR NEXT ITERATION
                # Calculate target STG (not incremental - direct target)
                target_stg_mwh = stg_current_mwh + actual_stg_increase
                stg_min_override_mwh = min(target_stg_mwh, stg_db_max_mwh)
                
                # Calculate GT reduction needed (fresh calculation, not accumulated)
                # GT reduction = how much STG increased from its natural dispatch
                gt_reduction_for_balance_mwh = actual_stg_increase
                excess_steam_balancing_active = True
                
                # Store adjustment for next iteration
                excess_steam_adjustment_mwh = actual_stg_increase
                stg_increased = True
                
                iteration_record["action"] = f"EXCESS_STEAM_BALANCE_STG+{actual_stg_increase:.2f}_GT-{gt_reduction_needed:.2f}"
                iteration_record["status"] = "EXCESS_STEAM_BALANCING"
            elif excess_steam_mt > 100:
                # Excess steam exists but can't be absorbed (GTs at MIN or STG at MAX)
                print("\n" + "="*90)
                print("⚠️ EXCESS STEAM - CANNOT BE FULLY ABSORBED")
                print("="*90)
                print(f"  Remaining Excess Steam:             {excess_steam_mt:>12.2f} MT")
                print(f"  Equivalent Power:                   {excess_power_from_steam_mwh:>12.2f} MWh")
                print(f"  STG Available Increase:             {stg_available_increase:>12.2f} MWh")
                print(f"  GT Available Reduction:             {gt_available_reduction:>12.2f} MWh")
                print(f"  ─────────────────────────────────────────────")
                if stg_available_increase <= 0:
                    print(f"  REASON: STG at MAX capacity ({stg_db_max_mwh:.2f} MWh)")
                if gt_available_reduction <= 0:
                    print(f"  REASON: All GTs at MIN load")
                print(f"  ─────────────────────────────────────────────")
                print(f"  OPTIONS:")
                print(f"    1. Export excess power ({excess_power_from_steam_mwh:.2f} MWh)")
                print(f"    2. Reduce HRSG supplementary firing (violates MIN rule)")
                print(f"    3. Accept wasted steam ({excess_steam_mt:.2f} MT)")
                print("="*90 + "\n")
                
                iteration_record["action"] = f"EXCESS_STEAM_UNABSORBED_{excess_steam_mt:.2f}_MT"
                iteration_record["status"] = "EXCESS_STEAM_LIMIT_REACHED"
            else:
                # Excess steam is small, no adjustment needed
                print(f"\n  [INFO] Excess steam ({excess_steam_mt:.2f} MT) is small, no balancing needed")
        
        # ---------------------------------------------------------
        # STEP 3g.2: Check if we can INCREASE STG to consume excess steam
        # Per flowchart: "Increase STG Generation to consume excess SHP Steam"
        # This should happen BEFORE declaring convergence
        # ---------------------------------------------------------
        if can_meet_shp and shp_deficit < 0 and stg_reduction_mwh > 0:
            # We have excess SHP AND we previously reduced STG
            # Try to recover some STG generation
            excess_shp = abs(shp_deficit)
            
            # Calculate how much STG we can recover
            potential_stg_recovery = excess_shp / NORM_STG_SHP_PER_KWH / 1000  # MWh
            
            # Apply damping factor (50%) to prevent oscillation
            # Only recover half of what's possible to allow gradual convergence
            damped_recovery = potential_stg_recovery * 0.5
            actual_recovery = min(damped_recovery, stg_reduction_mwh)
            
            if actual_recovery > 0.1:  # Only if meaningful (increased threshold)
                print(f"\n  [ACTION] EXCESS SHP DETECTED - RECOVERING STG!")
                print(f"       Excess SHP Available:     {excess_shp:>14.2f} MT")
                print(f"       Potential STG Recovery:   {potential_stg_recovery:>14.2f} MWh")
                print(f"       Damped Recovery (50%):    {damped_recovery:>14.2f} MWh")
                print(f"       Previous STG Reduction:   {stg_reduction_mwh:>14.2f} MWh")
                print(f"       Actual STG Recovery:      {actual_recovery:>14.2f} MWh")
                
                stg_reduction_mwh -= actual_recovery
                import_compensation_mwh = stg_reduction_mwh
                stg_increased = True
                
                print(f"       New STG Reduction:        {stg_reduction_mwh:>14.2f} MWh")
                
                iteration_record["action"] = f"INCREASE_STG_{actual_recovery:.2f}_MWH"
                iteration_record["status"] = "SHP_EXCESS_RECOVERY"
        
        # Now check for final convergence
        if power_converged and shp_converged and not stg_increased:
            print(f"\n  [CONVERGED] Both Power and Steam balanced!")
            print(f"       Power Aux Error: {aux_power_error:.6f} MWh <= {USD_TOLERANCE} MWh")
            print(f"       SHP Deficit: {shp_deficit:.2f} MT <= 0 (CAN MEET)")
            
            iteration_record["action"] = "CONVERGED"
            iteration_record["status"] = "CONVERGED"
            iteration_history.append(iteration_record)
            converged = True
            
            # Store final values
            final_dispatch = current_dispatch
            final_power_result = power_result
            final_steam_balance = steam_balance
            final_shp_capacity = shp_capacity
            final_hrsg_availability = hrsg_availability
            final_lp_balance = lp_balance  # STG load-based LP balance
            final_mp_balance = mp_balance  # STG load-based MP balance
            break
        
        # ---------------------------------------------------------
        # STEP 3h: If SHP CANNOT be met, REDUCE STG
        # ---------------------------------------------------------
        if not can_meet_shp and shp_deficit > 0:
            # Calculate how much STG to reduce to eliminate SHP deficit
            # SHP deficit = SHP demand - SHP capacity
            # STG SHP = STG_gross_kwh * 0.0036
            # To reduce SHP demand by X MT, reduce STG by X / 0.0036 KWh = X / 0.0036 / 1000 MWh
            
            stg_reduction_for_shp = shp_deficit / NORM_STG_SHP_PER_KWH / 1000  # MWh
            
            print(f"\n  [ACTION] SHP DEFICIT DETECTED - REDUCING STG!")
            print(f"       SHP Deficit:          {shp_deficit:>14.2f} MT")
            print(f"       STG Reduction Needed: {stg_reduction_for_shp:>14.2f} MWh")
            print(f"       (To reduce SHP demand by {shp_deficit:.2f} MT)")
            
            # Check if STG is already at 0
            if stg_gross_mwh <= 0:
                print(f"\n  [ERROR] STG already at 0 but SHP still insufficient!")
                print(f"       This means SHP demand exceeds maximum HRSG capacity.")
                print(f"       SHP Demand:     {shp_demand:>14.2f} MT")
                print(f"       Max SHP Cap:    {max_shp_capacity:>14.2f} MT")
                print(f"       Shortfall:      {shp_deficit:>14.2f} MT")
                print(f"\n       POSSIBLE CAUSES:")
                print(f"       1. Process SHP demand too high ({shp_process:.2f} MT)")
                print(f"       2. GTs not running (Priority too low?)")
                print(f"       3. HRSG supplementary firing at maximum capacity")
                print(f"\n       RECOMMENDATIONS:")
                print(f"       - Check asset priorities (GTs should have priority 1-3)")
                print(f"       - Verify GT operational hours are set correctly")
                print(f"       - Consider reducing process SHP demand")
                
                iteration_record["action"] = "SHP_IMPOSSIBLE"
                iteration_record["status"] = "FAILED"
                iteration_record["failure_reason"] = "SHP_DEFICIT_UNSOLVABLE"
                iteration_record["diagnostic_info"] = {
                    "shp_demand": shp_demand,
                    "shp_capacity": max_shp_capacity,
                    "shp_deficit": shp_deficit,
                    "stg_gross_mwh": stg_gross_mwh,
                    "gt_total_gross_mwh": sum(gt["gross_mwh"] for gt in gt_details),
                }
                iteration_history.append(iteration_record)
                
                # Store final values and exit
                final_dispatch = current_dispatch
                final_power_result = power_result
                final_steam_balance = steam_balance
                final_shp_capacity = shp_capacity
                final_hrsg_availability = hrsg_availability
                final_lp_balance = lp_balance  # STG load-based LP balance
                final_mp_balance = mp_balance  # STG load-based MP balance
                break
            
            # Add to cumulative STG reduction (but cap at original max)
            stg_reduction_mwh += stg_reduction_for_shp
            if stg_original_max_mwh is not None:
                stg_reduction_mwh = min(stg_reduction_mwh, stg_original_max_mwh)
            import_compensation_mwh = stg_reduction_mwh  # Compensate with import
            
            print(f"       Cumulative STG Reduction: {stg_reduction_mwh:>10.2f} MWh")
            print(f"       Cumulative Import Comp:   {import_compensation_mwh:>10.2f} MWh")
            
            iteration_record["action"] = f"REDUCE_STG_{stg_reduction_for_shp:.2f}_MWH"
            iteration_record["status"] = "SHP_DEFICIT"
        
        elif not power_converged:
            print(f"\n  [CONTINUE] Power Aux not stabilized yet...")
            iteration_record["action"] = f"AUX_ERROR_{aux_power_error:.6f}_MWH"
            iteration_record["status"] = "POWER_ITERATING"
        
        iteration_history.append(iteration_record)
        
        # Store current values
        final_dispatch = current_dispatch
        final_power_result = power_result
        final_steam_balance = steam_balance
        final_shp_capacity = shp_capacity
        final_hrsg_availability = hrsg_availability
        final_lp_balance = lp_balance  # STG load-based LP balance
        final_mp_balance = mp_balance  # STG load-based MP balance
        
        # ---------------------------------------------------------
        # Calculate STG limit based on steam availability for NEXT iteration
        # Per flowchart: STG should be limited by available SHP
        # ---------------------------------------------------------
        # Base SHP demand (without STG) = Process + Fixed + PRDS demands
        base_shp_demand = shp_process + shp_fixed
        base_shp_demand += steam_balance['lp_balance'].get('shp_for_stg_lp', 0)
        base_shp_demand += steam_balance['mp_balance'].get('shp_for_stg_mp', 0)
        base_shp_demand += steam_balance['hp_balance'].get('shp_for_hp_prds', 0)
        base_shp_demand += steam_balance['mp_balance'].get('shp_for_prds_mp', 0)
        
        # Available SHP for STG = Max Capacity - Base Demand
        available_shp_for_stg = max_shp_capacity - base_shp_demand
        
        # Max STG power based on steam = Available SHP / 0.0036 MT per KWh
        if available_shp_for_stg > 0:
            max_stg_kwh_from_steam = available_shp_for_stg / NORM_STG_SHP_PER_KWH
            stg_steam_limit_mwh = max_stg_kwh_from_steam / 1000  # Convert to MWh
        else:
            stg_steam_limit_mwh = 0.0
        
        print(f"\n  [STEAM-BASED STG LIMIT CALCULATION]:")
        print(f"       Base SHP Demand (no STG):     {base_shp_demand:>12.2f} MT")
        print(f"       Max SHP Capacity:             {max_shp_capacity:>12.2f} MT")
        print(f"       Available SHP for STG:        {available_shp_for_stg:>12.2f} MT")
        print(f"       Max STG from Steam:           {stg_steam_limit_mwh:>12.2f} MWh")
        print(f"       Current STG Generation:       {stg_gross_mwh:>12.2f} MWh")
        
        # Update for next iteration
        previous_utility_aux_mwh = current_utility_aux_mwh
        previous_shp_deficit = shp_deficit
    
    # =========================================================
    # STEP 4: FINAL RESULTS
    # =========================================================
    print("\n" + "-"*80)
    print("STEP 4: FINAL RESULTS")
    print("-"*80)
    
    # Calculate final SHP balance
    final_shp_balance = None
    if final_steam_balance and final_shp_capacity:
        final_shp_balance = check_shp_balance(
            final_steam_balance["summary"]["total_shp_demand"],
            final_shp_capacity
        )
    
    final_aux_power = current_utility_aux_mwh if iteration_history else 0.0
    
    # Get excess power for export from final power result
    final_excess_power = final_power_result.get("excessPowerForExport", 0) if final_power_result else 0.0
    
    print(f"  Converged:             {'YES' if converged else 'NO'}")
    print(f"  Iterations Used:       {len(iteration_history)}")
    print(f"  Final Power Aux:       {final_aux_power:>12.4f} MWh")
    print(f"  STG Reduction:         {stg_reduction_mwh:>12.2f} MWh")
    print(f"  Import Compensation:   {import_compensation_mwh:>12.2f} MWh")
    
    # Display export power summary
    if final_excess_power > 0:
        print(f"\n  EXPORT POWER SUMMARY:")
        print(f"  Excess Power for Export: {final_excess_power:>12.2f} MWh")
        if export_available:
            print(f"  Export Status:           AVAILABLE")
        else:
            print(f"  Export Status:           NOT AVAILABLE")
            print(f"  WARNING: Excess power generated but export not available!")
    
    # Generate diagnostic message for failed convergence
    failure_diagnostic = None
    if not converged and iteration_history:
        last_iter = iteration_history[-1]
        failure_status = last_iter.get("status", "UNKNOWN")
        
        if failure_status == "FAILED":
            # Check for specific failure reason
            failure_reason = last_iter.get("failure_reason", "UNKNOWN")
            diagnostic_info = last_iter.get("diagnostic_info", {})
            
            if failure_reason == "SHP_DEFICIT_UNSOLVABLE":
                failure_diagnostic = {
                    "type": "SHP_CAPACITY_EXCEEDED",
                    "message": "Steam (SHP) demand exceeds maximum HRSG capacity. Cannot balance steam even with STG at zero.",
                    "details": {
                        "shp_demand_mt": diagnostic_info.get("shp_demand", 0),
                        "shp_capacity_mt": diagnostic_info.get("shp_capacity", 0),
                        "shp_deficit_mt": diagnostic_info.get("shp_deficit", 0),
                        "stg_generation_mwh": diagnostic_info.get("stg_gross_mwh", 0),
                        "gt_total_generation_mwh": diagnostic_info.get("gt_total_gross_mwh", 0),
                    },
                    "possible_causes": [
                        "Process SHP demand is too high for available HRSG capacity",
                        "Gas Turbines (GTs) not running or have low priority (check GT operational hours)",
                        "HRSG supplementary firing already at maximum capacity",
                        "Asset priorities misconfigured (STG dispatched before GTs)"
                    ],
                    "recommendations": [
                        "Check asset priorities: GTs should have priority 1-3, STG should have priority 10+",
                        "Verify GT operational hours are set correctly in database",
                        "Consider reducing process SHP demand if possible",
                        "Ensure HRSG supplementary firing capacity is sufficient"
                    ]
                }
        elif failure_status == "EXCESS_STEAM_LIMIT_REACHED":
            # STG at MAX or GTs at MIN, cannot absorb excess steam
            last_dispatch = final_dispatch if final_dispatch else []
            stg_info = next((a for a in last_dispatch if "STG" in str(a.get("AssetName", "")).upper()), {})
            
            failure_diagnostic = {
                "type": "EXCESS_STEAM_UNABSORBED",
                "message": "Excess steam generated but cannot be absorbed. STG at maximum capacity or GTs at minimum load.",
                "details": {
                    "excess_steam_mt": last_iter.get("excess_steam_mt", 0),
                    "equivalent_power_mwh": last_iter.get("excess_power_mwh", 0),
                    "stg_current_mwh": stg_info.get("GrossMWh", 0),
                    "stg_max_mw": stg_info.get("CapacityMW", 0),
                },
                "possible_causes": [
                    "STG has higher priority than GTs (dispatches first, reaches MAX before GTs load)",
                    "GTs running at minimum load (cannot reduce further to decrease free steam)",
                    "HRSG minimum supplementary firing (60 MT/hr rule) creates excess steam"
                ],
                "recommendations": [
                    "Adjust asset priorities: GTs should dispatch BEFORE STG (lower priority numbers)",
                    "Example: GT1=1, GT2=2, GT3=3, STG=10",
                    "This allows GTs to load first (generate free steam), then STG loads (consumes steam)",
                    "Consider exporting excess power if export is available"
                ]
            }
        else:
            # Generic convergence failure
            failure_diagnostic = {
                "type": "CONVERGENCE_TIMEOUT",
                "message": f"USD iteration did not converge after {len(iteration_history)} iterations",
                "details": {
                    "iterations_used": len(iteration_history),
                    "final_aux_error_mwh": last_iter.get("aux_power_error_mwh", 0),
                    "tolerance_mwh": USD_TOLERANCE,
                },
                "possible_causes": [
                    "Power auxiliary consumption oscillating between iterations",
                    "Steam balance not stabilizing",
                    "Asset priority configuration causing dispatch instability"
                ],
                "recommendations": [
                    "Review iteration history to identify oscillation patterns",
                    "Check asset priorities are set correctly",
                    "Verify operational hours and capacity limits are reasonable"
                ]
            }
    
    return {
        "success": converged,
        "error_type": None if converged else "USD_NOT_CONVERGED",
        "message": "USD iteration converged successfully" if converged else "USD iteration did not converge",
        "failure_diagnostic": failure_diagnostic,  # NEW: Detailed diagnostic information
        "converged": converged,
        "iterations_used": len(iteration_history),
        "final_power_aux_mwh": round(final_aux_power, 4),
        "tolerance_achieved": iteration_history[-1]["aux_power_error_mwh"] if iteration_history else 0.0,
        
        # STG Extraction (FIXED based on steam demand)
        "stg_extraction": stg_extraction,
        
        # Power results
        "power_result": final_power_result,
        "final_dispatch": final_dispatch,
        
        # Steam results
        "final_steam_balance": final_steam_balance,
        "final_hrsg_availability": final_hrsg_availability,
        "final_shp_capacity": final_shp_capacity,
        "final_shp_balance": final_shp_balance,
        
        # STG load-based LP/MP balance (with calculated ratios)
        "final_lp_balance": final_lp_balance,
        "final_mp_balance": final_mp_balance,
        
        # HRSG MIN load calculation (backward compatibility)
        "hrsg_min_load": final_hrsg_min_load,
        
        # HRSG Dispatch (priority-based load allocation)
        "hrsg_dispatch": final_hrsg_dispatch,
        
        # HRSG Natural Gas reverse calculation (from heat rate lookup)
        "hrsg_ng_calculation": final_hrsg_ng_calculation,
        
        # STG Reduction (for SHP balance)
        "stg_reduction_mwh": round(stg_reduction_mwh, 2),
        "import_compensation_mwh": round(import_compensation_mwh, 2),
        
        # Export power
        "excess_power_for_export_mwh": round(final_excess_power, 2),
        "export_available": export_available,
        
        # Iteration history
        "iteration_history": iteration_history,
    }


# ============================================================
# TEST
# ============================================================
if __name__ == "__main__":
    print("="*70)
    print("USD ITERATION SERVICE TEST")
    print("="*70)
    
    # Test with example values
    result = usd_iterate(
        month=4,
        year=2025,
        lp_process=20109.57,
        lp_fixed=5169.51,
        mp_process=14030.00,
        mp_fixed=518.00,
        hp_process=4972.00,
        hp_fixed=0.00,
        shp_process=20975.00,
        shp_fixed=0.00,
        bfw_ufu=300.0,
    )
    
    print(f"\nConverged: {result['converged']}")
    print(f"Iterations Used: {result['iterations_used']}")
    print(f"Tolerance Achieved: {result.get('tolerance_achieved', 0)}")
    print(f"STG Reduction: {result.get('stg_reduction_mwh', 0)} MWh")
    
    if result.get("utility_consumption"):
        utils = result["utility_consumption"]
        print(f"\nUtility Consumption:")
        print(f"  BFW: {utils['bfw']['total_m3']} M3")
        print(f"  DM Water: {utils['dm_water']['total_m3']} M3")
        print(f"  Cooling Water: {utils['cooling_water']['total_km3']} KM3")
        print(f"  Compressed Air: {utils['compressed_air']['total_nm3']} NM3")
        print(f"  Utility Power: {utils['utility_power']['total_mwh']} MWh")
    
    # ============================================================
    # TEST 2: HIGH SHP DEMAND - STG SHOULD BE STEAM-CONSTRAINED
    # ============================================================
    print("\n" + "="*70)
    print("TEST 2: HIGH SHP DEMAND (STG STEAM-CONSTRAINED)")
    print("="*70)
    print("Scenario: Very high SHP process demand (150,000 MT)")
    print("Expected: STG should be limited by available steam")
    print("="*70)
    
    result2 = usd_iterate(
        month=4,
        year=2025,
        lp_process=20109.57,
        lp_fixed=5169.51,
        mp_process=14030.00,
        mp_fixed=518.00,
        hp_process=4972.00,
        hp_fixed=0.00,
        shp_process=150000.00,  # Very high SHP demand!
        shp_fixed=0.00,
        bfw_ufu=300.0,
    )
    
    print(f"\nTEST 2 RESULTS:")
    print(f"  Converged: {result2['converged']}")
    print(f"  Iterations Used: {result2['iterations_used']}")
    print(f"  STG Reduction: {result2.get('stg_reduction_mwh', 0)} MWh")
    print(f"  Import Compensation: {result2.get('import_compensation_mwh', 0)} MWh")
    
    if result2.get("final_shp_capacity"):
        shp_cap = result2["final_shp_capacity"]
        print(f"  Free Steam: {shp_cap.get('total_free_steam_mt', 0)} MT")
        print(f"  Supp Firing Max: {shp_cap.get('total_supplementary_max_mt', 0)} MT")
        print(f"  Max SHP Capacity: {shp_cap.get('total_max_shp_capacity', 0)} MT")
