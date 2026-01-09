"""
Utility Service
Calculates utility consumption and auxiliary power requirements.

UTILITIES AND THEIR POWER CONSUMPTION:
======================================
Each utility plant consumes power to operate. The power consumption
depends on the quantity of utility produced.

From the norms data:
- Boiler Feed Water (BFW): 9.5 KWH per M³
- Compressed Air: 0.165 KWH per NM³
- Cooling Water 1: 245 KWH per KM³
- Cooling Water 2: 250 KWH per KM³
- DM Water: 1.21 KWH per M³
- Effluent Treated: 3.54 KWH per M³
- Oxygen Plant: 968.65 KWH per MT (or 936.04 KWH per MT)

UTILITY CONSUMPTION DEPENDS ON:
===============================
- BFW: Steam generation (HRSG, PRDS) + Process
- Cooling Water: Power plants + Utility plants + Process
- Compressed Air: All plants
- DM Water: BFW makeup + Process
- Effluent: Process water treatment

THE CIRCULAR DEPENDENCY:
========================
1. Power Generation → needs Cooling Water, Compressed Air
2. Steam Generation → needs BFW, which needs DM Water
3. Utilities → need Power (Aux Power)
4. Total Power Demand = Plant Demand + Utility Aux Power
5. But Utility Aux Power depends on Utility Quantities
6. And Utility Quantities depend on Power/Steam Generation
7. Hence we need to ITERATE!
"""

# ============================================================
# UTILITY NORMS - FROM NORMS SHEET (April 2025)
# ============================================================
# These norms define the consumption of utilities per unit of production

# ============================================================
# 1. POWER PLANTS - UTILITY CONSUMPTION
# ============================================================
# GT2 (Power Plant 2) - per KWH generated
NORM_GT2_NATURAL_GAS_MMBTU_PER_KWH = 0.0101463
NORM_GT2_COMPRESSED_AIR_NM3_FIXED = 30960.0  # Fixed per month
NORM_GT2_COOLING_WATER_KM3_FIXED = 108.0     # Fixed per month
NORM_GT2_POWER_DIS_KWH_PER_KWH = 0.0140      # Auxiliary power

# GT3 (Power Plant 3) - per KWH generated
NORM_GT3_NATURAL_GAS_MMBTU_PER_KWH = 0.0094715
NORM_GT3_COMPRESSED_AIR_NM3_FIXED = 30960.0  # Fixed per month
NORM_GT3_COOLING_WATER_KM3_FIXED = 108.0     # Fixed per month
NORM_GT3_POWER_DIS_KWH_PER_KWH = 0.0140      # Auxiliary power

# STG (Steam Turbine Generator) - per KWH generated
NORM_STG_RET_CONDENSATE_M3_PER_KWH = 0.0029300
NORM_STG_COMPRESSED_AIR_NM3_FIXED = 41040.0  # Fixed per month
NORM_STG_COOLING_WATER_KM3_FIXED = 2376.0    # Fixed per month
NORM_STG_POWER_DIS_KWH_PER_KWH = 0.0020      # Auxiliary power
NORM_STG_SHP_STEAM_MT_PER_KWH = 0.0035600       # SHP steam consumption

# ============================================================
# 2. UTILITY PLANTS - UTILITY CONSUMPTION
# ============================================================
# Boiler Feed Water (BFW) - per M³ produced
NORM_BFW_DM_WATER_M3_PER_M3 = 0.86
NORM_BFW_LP_STEAM_MT_PER_M3 = 0.145
NORM_BFW_POWER_KWH_PER_M3 = 9.5
NORM_BFW_COOLING_WATER_KM3_FIXED = 108.0  # Fixed per month

# Compressed Air - per NM³ produced
NORM_AIR_POWER_KWH_PER_NM3 = 0.165
NORM_AIR_COOLING_WATER_KM3_FIXED = 175.0  # Fixed per month

# Cooling Water 1 - per KM³ produced
NORM_CW1_WATER_M3_PER_KM3 = 11.05
NORM_CW1_POWER_KWH_PER_KM3 = 245.0
NORM_CW1_COMPRESSED_AIR_NM3_FIXED = 1650.0  # Fixed per month

# Cooling Water 2 - per KM³ produced
NORM_CW2_WATER_M3_PER_KM3 = 11.5
NORM_CW2_POWER_KWH_PER_KM3 = 250.0
NORM_CW2_COMPRESSED_AIR_NM3_FIXED = 1650.0  # Fixed per month

# DM Water - per M³ produced
NORM_DM_WATER_M3_PER_M3 = 1.05
NORM_DM_POWER_KWH_PER_M3 = 1.21
NORM_DM_COMPRESSED_AIR_NM3_PER_M3 = 0.077
NORM_DM_RET_CONDENSATE_M3_PER_M3 = 0.203

# Effluent Treated - per M³ treated
NORM_EFFLUENT_POWER_KWH_PER_M3 = 3.54
NORM_EFFLUENT_WATER_M3_PER_M3 = 0.0007

# Oxygen - per MT produced
NORM_OXYGEN_POWER_KWH_PER_MT = 968.65
NORM_OXYGEN_COOLING_WATER_KM3_PER_MT = 0.261
NORM_OXYGEN_NITROGEN_BYPRODUCT_NM3_PER_MT = 2448.4  # By-product credit

# ============================================================
# 3. HRSG - UTILITY CONSUMPTION (per MT SHP produced)
# ============================================================
NORM_HRSG2_NATURAL_GAS_MMBTU_PER_MT = 2.8063807  # HRSG2 specific
NORM_HRSG3_NATURAL_GAS_MMBTU_PER_MT = 2.8167584  # HRSG3 specific
NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT = 2.8115696  # Average of HRSG2 and HRSG3
NORM_HRSG_BFW_M3_PER_MT = 1.024
NORM_HRSG_COMPRESSED_AIR_NM3_FIXED = 453600.0  # Fixed per HRSG per month
NORM_HRSG_LP_STEAM_CREDIT_MT_PER_MT = -0.0503520  # Credit (negative)
NORM_HRSG_WATER_M3_PER_MT = 0.0027
NORM_HRSG_TRISODIUM_PHOSPHATE_KG_PER_MT = 0.0009

# ============================================================
# 4. PRDS - UTILITY CONSUMPTION
# ============================================================
# HP Steam PRDS - per MT HP produced
NORM_HP_PRDS_BFW_M3_PER_MT = 0.0768
NORM_HP_PRDS_SHP_MT_PER_MT = 0.9232

# LP Steam PRDS - per MT LP produced
NORM_LP_PRDS_BFW_M3_PER_MT = 0.25
NORM_LP_PRDS_MP_MT_PER_MT = 0.75

# MP Steam PRDS (from SHP) - per MT MP produced
NORM_MP_PRDS_BFW_M3_PER_MT = 0.09
NORM_MP_PRDS_SHP_MT_PER_MT = 0.91

# ============================================================
# 5. STG EXTRACTION - UTILITY CONSUMPTION
# ============================================================
# STG LP Steam extraction - per MT LP extracted
NORM_STG_LP_SHP_MT_PER_MT = 0.48

# STG MP Steam extraction - per MT MP extracted
NORM_STG_MP_SHP_MT_PER_MT = 0.69

# ============================================================
# LEGACY NORMS (for backward compatibility)
# ============================================================
NORM_POWER_PER_BFW_M3 = NORM_BFW_POWER_KWH_PER_M3
NORM_POWER_PER_COMPRESSED_AIR_NM3 = NORM_AIR_POWER_KWH_PER_NM3
NORM_POWER_PER_COOLING_WATER_1_KM3 = NORM_CW1_POWER_KWH_PER_KM3
NORM_POWER_PER_COOLING_WATER_2_KM3 = NORM_CW2_POWER_KWH_PER_KM3
NORM_POWER_PER_DM_WATER_M3 = NORM_DM_POWER_KWH_PER_M3
NORM_POWER_PER_EFFLUENT_M3 = NORM_EFFLUENT_POWER_KWH_PER_M3
NORM_POWER_PER_OXYGEN_MT = NORM_OXYGEN_POWER_KWH_PER_MT

NORM_BFW_PER_HRSG_SHP_MT = NORM_HRSG_BFW_M3_PER_MT
NORM_BFW_PER_HP_PRDS_MT = NORM_HP_PRDS_BFW_M3_PER_MT
NORM_BFW_PER_MP_PRDS_MT = NORM_MP_PRDS_BFW_M3_PER_MT
NORM_BFW_PER_LP_PRDS_MT = NORM_LP_PRDS_BFW_M3_PER_MT

NORM_DM_PER_BFW_M3 = NORM_BFW_DM_WATER_M3_PER_M3

# Calculated norms for cooling water and compressed air
NORM_CW2_PER_GT_KM3_PER_UNIT = 108.0 / 8064000  # KM³ per KWH (GT2 example)
NORM_CW2_PER_STG_KM3_PER_KWH = 2376.0 / 10231200  # KM³ per KWH STG
NORM_CW2_PER_BFW_M3 = 108.0 / 100000  # KM³ per M³ BFW (approximate)
NORM_CW2_PER_COMPRESSED_AIR_NM3 = 175.0 / 7000000  # KM³ per NM³

NORM_AIR_PER_GT_NM3_PER_KWH = 31000.0 / 8000000  # NM³ per KWH GT
NORM_AIR_PER_STG_NM3_PER_KWH = 41040.0 / 10231200  # NM³ per KWH STG
NORM_AIR_PER_HRSG_NM3_PER_MT = 468720.0 / 48000  # NM³ per MT SHP from HRSG


# ============================================================
# GT NATURAL GAS CALCULATION CONSTANTS
# ============================================================
# Formula: NET GT MMBTU = GROSS MMBTU - FREE STEAM MMBTU
#
# GROSS MMBTU = KWH × Heat Rate (KCAL/KWH) × 3.96567 / 1,000,000
# FREE STEAM MMBTU = KWH × FreeSteamFactor × 760.87 × 3.96567 / 1,000,000
#
# Where:
#   - Heat Rate: From HeatRateLookup table (KCAL/KWH)
#   - FreeSteamFactor: From HeatRateLookup table (e.g., 1.97)
#   - 760.87 = Free steam energy (KCAL/kg) = (810 - 110) / 0.92
#   - 3.96567 = KCAL to BTU conversion
#   - 1,000,000 = BTU to MMBTU (1 MMBTU = 1 million BTU)

GT_NG_KCAL_TO_BTU = 3.96567
GT_NG_BTU_TO_MMBTU = 1_000_000
GT_NG_FREE_STEAM_ENERGY_KCAL_KG = 760.87  # (810 - 110) / 0.92 KCAL/kg
GT_NG_SHP_ENTHALPY = 810  # KCAL/kg
GT_NG_HRSG_INLET_ENTHALPY = 110  # KCAL/kg
GT_NG_HRSG_EFFICIENCY = 0.92


def calculate_gt_ng_mmbtu(
    kwh: float,
    heat_rate: float,
    free_steam_factor: float,
    gt_name: str = "GT"
) -> dict:
    """
    Calculate GT Natural Gas MMBTU using heat rate lookup.
    
    Formula:
        NET GT MMBTU = GROSS MMBTU - FREE STEAM MMBTU
        
        GROSS MMBTU = KWH × Heat Rate × 3.96567 / 1,000,000
        FREE STEAM MMBTU = KWH × FreeSteamFactor × 760.87 × 3.96567 / 1,000,000
    
    Args:
        kwh: GT generation in KWH
        heat_rate: Heat rate from HeatRateLookup table (KCAL/KWH)
        free_steam_factor: Free steam factor from HeatRateLookup table
        gt_name: Name of GT for logging (e.g., "GT1", "GT2", "GT3")
    
    Returns:
        dict with:
            - gross_mmbtu: Gross MMBTU before free steam deduction
            - free_steam_mmbtu: Free steam MMBTU to deduct
            - net_mmbtu: Net GT MMBTU (final value)
            - norm: MMBTU per KWH
            - heat_rate: Heat rate used (KCAL/KWH)
            - free_steam_factor: Free steam factor used
    """
    if kwh <= 0 or heat_rate is None or free_steam_factor is None:
        return {
            "gt_name": gt_name,
            "gross_mmbtu": 0.0,
            "free_steam_mmbtu": 0.0,
            "net_mmbtu": 0.0,
            "norm": 0.0,
            "heat_rate": heat_rate,
            "free_steam_factor": free_steam_factor,
            "calculation_method": "unavailable"
        }
    
    # Gross MMBTU = KWH × Heat Rate × 3.96567 / 1,000,000
    gross_mmbtu = kwh * heat_rate * GT_NG_KCAL_TO_BTU / GT_NG_BTU_TO_MMBTU
    
    # Free Steam MMBTU = KWH × FreeSteamFactor × 760.87 × 3.96567 / 1,000,000
    free_steam_mmbtu = kwh * free_steam_factor * GT_NG_FREE_STEAM_ENERGY_KCAL_KG * GT_NG_KCAL_TO_BTU / GT_NG_BTU_TO_MMBTU
    
    # Net MMBTU = Gross - Free Steam
    net_mmbtu = gross_mmbtu - free_steam_mmbtu
    
    # Norm = MMBTU per KWH
    norm = net_mmbtu / kwh if kwh > 0 else 0
    
    return {
        "gt_name": gt_name,
        "gross_mmbtu": round(gross_mmbtu, 2),
        "free_steam_mmbtu": round(free_steam_mmbtu, 2),
        "net_mmbtu": round(net_mmbtu, 2),
        "norm": round(norm, 7),
        "heat_rate": round(heat_rate, 2),
        "free_steam_factor": round(free_steam_factor, 4),
        "calculation_method": "heat_rate_lookup"
    }


# ============================================================
# UTILITY QUANTITY CALCULATIONS
# ============================================================

def calculate_bfw_requirement(
    shp_from_hrsg: float,
    hp_from_prds: float,
    mp_from_prds: float,
    lp_from_prds: float,
    bfw_process: float = 0.0,
    bfw_fixed: float = 0.0,
) -> dict:
    """
    Calculate Boiler Feed Water (BFW) requirement.
    
    BFW is consumed by:
    1. HRSG for SHP steam generation
    2. PRDS for HP/MP/LP steam
    3. Process consumption
    
    Args:
        shp_from_hrsg: SHP steam from HRSG (MT)
        hp_from_prds: HP steam from PRDS (MT)
        mp_from_prds: MP steam from PRDS (MT)
        lp_from_prds: LP steam from PRDS (MT)
        bfw_process: Process BFW consumption (M³)
        bfw_fixed: Fixed BFW consumption (M³)
        
    Returns:
        dict with BFW breakdown
    """
    bfw_for_hrsg = shp_from_hrsg * NORM_BFW_PER_HRSG_SHP_MT
    bfw_for_hp_prds = hp_from_prds * NORM_BFW_PER_HP_PRDS_MT
    bfw_for_mp_prds = mp_from_prds * NORM_BFW_PER_MP_PRDS_MT
    bfw_for_lp_prds = lp_from_prds * NORM_BFW_PER_LP_PRDS_MT
    
    total_bfw = bfw_for_hrsg + bfw_for_hp_prds + bfw_for_mp_prds + bfw_for_lp_prds + bfw_process + bfw_fixed
    
    return {
        "bfw_for_hrsg": round(bfw_for_hrsg, 2),
        "bfw_for_hp_prds": round(bfw_for_hp_prds, 2),
        "bfw_for_mp_prds": round(bfw_for_mp_prds, 2),
        "bfw_for_lp_prds": round(bfw_for_lp_prds, 2),
        "bfw_process": round(bfw_process, 2),
        "bfw_fixed": round(bfw_fixed, 2),
        "total_bfw_m3": round(total_bfw, 2),
    }


def calculate_dm_water_requirement(
    total_bfw_m3: float,
    dm_process: float = 0.0,
    dm_fixed: float = 0.0,
) -> dict:
    """
    Calculate DM Water requirement.
    
    DM Water is consumed by:
    1. BFW plant (makeup water)
    2. Process consumption
    
    Args:
        total_bfw_m3: Total BFW requirement (M³)
        dm_process: Process DM consumption (M³)
        dm_fixed: Fixed DM consumption (M³)
        
    Returns:
        dict with DM Water breakdown
    """
    dm_for_bfw = total_bfw_m3 * NORM_DM_PER_BFW_M3
    total_dm = dm_for_bfw + dm_process + dm_fixed
    
    return {
        "dm_for_bfw": round(dm_for_bfw, 2),
        "dm_process": round(dm_process, 2),
        "dm_fixed": round(dm_fixed, 2),
        "total_dm_m3": round(total_dm, 2),
    }


def calculate_cooling_water_requirement(
    gt_gross_kwh: float,
    stg_gross_kwh: float,
    bfw_m3: float,
    oxygen_mt: float = 0.0,
    cw1_process: float = 15194.0,
    cw2_process: float = 9016.0,
) -> dict:
    """
    Calculate Cooling Water requirement (CW1 and CW2 separately).
    
    Cooling Water 1 (CW1): Primarily for process plants
    Cooling Water 2 (CW2): Power plants + Utility plants + Process
    
    Args:
        gt_gross_kwh: Total GT gross generation (KWH)
        stg_gross_kwh: STG gross generation (KWH)
        bfw_m3: Total BFW (M³)
        oxygen_mt: Oxygen production (MT)
        cw1_process: Cooling Water 1 process demand (KM³)
        cw2_process: Cooling Water 2 process demand (KM³)
        
    Returns:
        dict with Cooling Water breakdown for CW1 and CW2
    """
    # Cooling Water 1 (CW1) - Process plants
    total_cw1 = cw1_process
    
    # Cooling Water 2 (CW2) - Power plants + Utility plants + Process
    cw2_for_gt = gt_gross_kwh * NORM_CW2_PER_GT_KM3_PER_UNIT
    cw2_for_stg = stg_gross_kwh * NORM_CW2_PER_STG_KM3_PER_KWH
    cw2_for_bfw = bfw_m3 * NORM_CW2_PER_BFW_M3
    cw2_for_oxygen = oxygen_mt * 0.261  # 0.261 KM³ per MT Oxygen
    total_cw2 = cw2_for_gt + cw2_for_stg + cw2_for_bfw + cw2_for_oxygen + cw2_process
    
    # Total cooling water
    total_cw = total_cw1 + total_cw2
    
    return {
        "cw1_process": round(cw1_process, 2),
        "cw1_total_km3": round(total_cw1, 2),
        "cw2_for_gt": round(cw2_for_gt, 2),
        "cw2_for_stg": round(cw2_for_stg, 2),
        "cw2_for_bfw": round(cw2_for_bfw, 2),
        "cw2_for_oxygen": round(cw2_for_oxygen, 2),
        "cw2_process": round(cw2_process, 2),
        "cw2_total_km3": round(total_cw2, 2),
        "total_cw_km3": round(total_cw, 2),
    }


def calculate_compressed_air_requirement(
    gt_gross_kwh: float,
    stg_gross_kwh: float,
    shp_from_hrsg: float,
    air_process: float = 0.0,
    air_fixed: float = 0.0,
) -> dict:
    """
    Calculate Compressed Air requirement.
    
    Compressed Air is consumed by:
    1. Power plants (GT, STG instrumentation)
    2. HRSG
    3. Process consumption
    
    Args:
        gt_gross_kwh: Total GT gross generation (KWH)
        stg_gross_kwh: STG gross generation (KWH)
        shp_from_hrsg: SHP from HRSG (MT)
        air_process: Process compressed air (NM³)
        air_fixed: Fixed compressed air (NM³)
        
    Returns:
        dict with Compressed Air breakdown
    """
    air_for_gt = gt_gross_kwh * NORM_AIR_PER_GT_NM3_PER_KWH
    air_for_stg = stg_gross_kwh * NORM_AIR_PER_STG_NM3_PER_KWH
    air_for_hrsg = shp_from_hrsg * NORM_AIR_PER_HRSG_NM3_PER_MT
    
    total_air = air_for_gt + air_for_stg + air_for_hrsg + air_process + air_fixed
    
    return {
        "air_for_gt": round(air_for_gt, 2),
        "air_for_stg": round(air_for_stg, 2),
        "air_for_hrsg": round(air_for_hrsg, 2),
        "air_process": round(air_process, 2),
        "air_fixed": round(air_fixed, 2),
        "total_air_nm3": round(total_air, 2),
    }


# ============================================================
# UTILITY AUXILIARY POWER CALCULATION
# ============================================================

def calculate_utility_aux_power(
    bfw_m3: float,
    dm_m3: float,
    cw_km3: float,
    air_nm3: float,
    oxygen_mt: float = 0.0,
    effluent_m3: float = 0.0,
) -> dict:
    """
    Calculate total Utility Auxiliary Power consumption.
    
    This is the power consumed by utility plants to produce utilities.
    
    Args:
        bfw_m3: Total BFW (M³)
        dm_m3: Total DM Water (M³)
        cw_km3: Total Cooling Water (KM³)
        air_nm3: Total Compressed Air (NM³)
        oxygen_mt: Total Oxygen (MT)
        effluent_m3: Total Effluent Treated (M³)
        
    Returns:
        dict with power consumption breakdown (in KWH)
    """
    power_for_bfw = bfw_m3 * NORM_POWER_PER_BFW_M3
    power_for_dm = dm_m3 * NORM_POWER_PER_DM_WATER_M3
    power_for_cw = cw_km3 * NORM_POWER_PER_COOLING_WATER_2_KM3  # Already in KWH per KM³
    power_for_air = air_nm3 * NORM_POWER_PER_COMPRESSED_AIR_NM3
    power_for_oxygen = oxygen_mt * NORM_POWER_PER_OXYGEN_MT
    power_for_effluent = effluent_m3 * NORM_POWER_PER_EFFLUENT_M3
    
    total_aux_power_kwh = (
        power_for_bfw + power_for_dm + power_for_cw + 
        power_for_air + power_for_oxygen + power_for_effluent
    )
    
    # Convert to MWH for easier comparison with power dispatch
    total_aux_power_mwh = total_aux_power_kwh / 1000
    
    return {
        "power_for_bfw_kwh": round(power_for_bfw, 2),
        "power_for_dm_kwh": round(power_for_dm, 2),
        "power_for_cw_kwh": round(power_for_cw, 2),
        "power_for_air_kwh": round(power_for_air, 2),
        "power_for_oxygen_kwh": round(power_for_oxygen, 2),
        "power_for_effluent_kwh": round(power_for_effluent, 2),
        "total_aux_power_kwh": round(total_aux_power_kwh, 2),
        "total_aux_power_mwh": round(total_aux_power_mwh, 2),
    }


# ============================================================
# COMPLETE UTILITY CALCULATION
# ============================================================

def calculate_all_utilities(
    # Power dispatch results
    gt_gross_kwh: float,
    stg_gross_kwh: float,
    # Steam balance results
    shp_from_hrsg: float,
    hp_from_prds: float,
    mp_from_prds: float,
    lp_from_prds: float,
    # Process/Fixed consumption
    bfw_process: float = 0.0,
    dm_process: float = 0.0,
    cw1_process: float = 15194.0,
    cw2_process: float = 9016.0,
    air_process: float = 0.0,
    oxygen_mt: float = 0.0,
    effluent_m3: float = 0.0,
) -> dict:
    """
    Calculate all utility requirements and auxiliary power.
    
    This is the main function called during USD iteration.
    
    Args:
        gt_gross_kwh: Total GT gross generation (KWH)
        stg_gross_kwh: STG gross generation (KWH)
        shp_from_hrsg: SHP from HRSG (MT)
        hp_from_prds: HP from PRDS (MT)
        mp_from_prds: MP from PRDS (MT)
        lp_from_prds: LP from PRDS (MT)
        bfw_process: Process BFW (M³)
        dm_process: Process DM Water (M³)
        cw1_process: Cooling Water 1 process demand (KM³)
        cw2_process: Cooling Water 2 process demand (KM³)
        air_process: Process Compressed Air (NM³)
        oxygen_mt: Oxygen production (MT)
        effluent_m3: Effluent treated (M³)
        
    Returns:
        dict with all utility calculations and total aux power
    """
    # Step 1: Calculate BFW requirement
    bfw = calculate_bfw_requirement(
        shp_from_hrsg=shp_from_hrsg,
        hp_from_prds=hp_from_prds,
        mp_from_prds=mp_from_prds,
        lp_from_prds=lp_from_prds,
        bfw_process=bfw_process,
    )
    
    # Step 2: Calculate DM Water requirement (depends on BFW)
    dm = calculate_dm_water_requirement(
        total_bfw_m3=bfw["total_bfw_m3"],
        dm_process=dm_process,
    )
    
    # Step 3: Calculate Cooling Water requirement (CW1 and CW2)
    cw = calculate_cooling_water_requirement(
        gt_gross_kwh=gt_gross_kwh,
        stg_gross_kwh=stg_gross_kwh,
        bfw_m3=bfw["total_bfw_m3"],
        oxygen_mt=oxygen_mt,
        cw1_process=cw1_process,
        cw2_process=cw2_process,
    )
    
    # Step 4: Calculate Compressed Air requirement
    air = calculate_compressed_air_requirement(
        gt_gross_kwh=gt_gross_kwh,
        stg_gross_kwh=stg_gross_kwh,
        shp_from_hrsg=shp_from_hrsg,
        air_process=air_process,
    )
    
    # Step 5: Calculate Utility Auxiliary Power
    aux_power = calculate_utility_aux_power(
        bfw_m3=bfw["total_bfw_m3"],
        dm_m3=dm["total_dm_m3"],
        cw_km3=cw["total_cw_km3"],
        air_nm3=air["total_air_nm3"],
        oxygen_mt=oxygen_mt,
        effluent_m3=effluent_m3,
    )
    
    return {
        "bfw": bfw,
        "dm_water": dm,
        "cooling_water": cw,
        "compressed_air": air,
        "aux_power": aux_power,
        "total_utility_aux_power_mwh": aux_power["total_aux_power_mwh"],
    }


# ============================================================
# COMPREHENSIVE UTILITY CALCULATION WITH NORMS
# ============================================================

def calculate_utilities_from_dispatch(
    # Power dispatch results (MWh)
    gt1_gross_mwh: float = 0.0,
    gt2_gross_mwh: float = 0.0,
    gt3_gross_mwh: float = 0.0,
    stg_gross_mwh: float = 0.0,
    # Steam balance results (MT)
    shp_from_hrsg1: float = 0.0,
    shp_from_hrsg2: float = 0.0,
    shp_from_hrsg3: float = 0.0,
    hp_from_prds: float = 0.0,
    mp_from_prds: float = 0.0,
    lp_from_prds: float = 0.0,
    lp_from_stg: float = 0.0,
    mp_from_stg: float = 0.0,
    # Process consumption
    oxygen_mt: float = 0.0,
    effluent_m3: float = 0.0,
    # Process utility consumption (Excel-matched defaults)
    air_process_nm3: float = 6084106.0,   # Compressed Air consumed by process plants
    cw1_process_km3: float = 15194.0,     # Cooling Water 1 consumed by process plants
    cw2_process_km3: float = 9016.0,      # Cooling Water 2 consumed by process plants
    dm_process_m3: float = 55787.0,        # DM Water consumed by process plants
    # Heat rates from power dispatch (KCAL/KWH) - from HeatRateLookup table
    gt1_heat_rate: float = None,           # GT1 Heat Rate from power dispatch
    gt2_heat_rate: float = None,           # GT2 Heat Rate from power dispatch
    gt3_heat_rate: float = None,           # GT3 Heat Rate from power dispatch
    # Free steam factors from power dispatch - from HeatRateLookup table
    gt1_free_steam_factor: float = None,   # GT1 Free Steam Factor
    gt2_free_steam_factor: float = None,   # GT2 Free Steam Factor
    gt3_free_steam_factor: float = None,   # GT3 Free Steam Factor
    # Flags
    gt1_available: bool = True,
    gt2_available: bool = True,
    gt3_available: bool = True,
    hrsg1_available: bool = True,
    hrsg2_available: bool = True,
    hrsg3_available: bool = False,
    # HRSG NG calculation from heat rate lookup (reverse calculation)
    hrsg_ng_calculation: dict = None,
) -> dict:
    """
    Calculate all utility requirements based on power and steam dispatch.
    
    This function uses the exact norms from the norms sheet to calculate:
    1. Natural Gas consumption (MMBTU)
    2. Cooling Water consumption (KM³)
    3. Compressed Air consumption (NM³)
    4. BFW consumption (M³)
    5. DM Water consumption (M³)
    6. Power consumption for each utility (KWH)
    7. Raw Water consumption (M³)
    
    Returns:
        dict with detailed utility breakdown
    """
    # Convert MWh to KWh for calculations
    gt1_kwh = gt1_gross_mwh * 1000
    gt2_kwh = gt2_gross_mwh * 1000
    gt3_kwh = gt3_gross_mwh * 1000
    stg_kwh = stg_gross_mwh * 1000
    total_gt_kwh = gt1_kwh + gt2_kwh + gt3_kwh
    
    # Total SHP from HRSG
    shp_from_hrsg = shp_from_hrsg1 + shp_from_hrsg2 + shp_from_hrsg3
    
    # =========================================================
    # 1. NATURAL GAS CONSUMPTION (using Heat Rate Lookup)
    # =========================================================
    # GT Natural Gas (MMBTU) - calculated using heat rate lookup
    # Formula: NET GT MMBTU = GROSS MMBTU - FREE STEAM MMBTU
    # If heat rate/free steam factor not available, fall back to legacy fixed norms
    
    # GT1 Natural Gas calculation
    if gt1_available and gt1_kwh > 0 and gt1_heat_rate is not None and gt1_free_steam_factor is not None:
        gt1_ng_result = calculate_gt_ng_mmbtu(gt1_kwh, gt1_heat_rate, gt1_free_steam_factor, "GT1")
        ng_gt1 = gt1_ng_result["net_mmbtu"]
        gt1_ng_norm = gt1_ng_result["norm"]
        gt1_ng_method = "heat_rate_lookup"
    else:
        ng_gt1 = gt1_kwh * NORM_GT3_NATURAL_GAS_MMBTU_PER_KWH if gt1_available else 0
        gt1_ng_norm = NORM_GT3_NATURAL_GAS_MMBTU_PER_KWH
        gt1_ng_method = "legacy_fixed_norm"
        gt1_ng_result = None
    
    # GT2 Natural Gas calculation
    if gt2_available and gt2_kwh > 0 and gt2_heat_rate is not None and gt2_free_steam_factor is not None:
        gt2_ng_result = calculate_gt_ng_mmbtu(gt2_kwh, gt2_heat_rate, gt2_free_steam_factor, "GT2")
        ng_gt2 = gt2_ng_result["net_mmbtu"]
        gt2_ng_norm = gt2_ng_result["norm"]
        gt2_ng_method = "heat_rate_lookup"
    else:
        ng_gt2 = gt2_kwh * NORM_GT2_NATURAL_GAS_MMBTU_PER_KWH if gt2_available else 0
        gt2_ng_norm = NORM_GT2_NATURAL_GAS_MMBTU_PER_KWH
        gt2_ng_method = "legacy_fixed_norm"
        gt2_ng_result = None
    
    # GT3 Natural Gas calculation
    if gt3_available and gt3_kwh > 0 and gt3_heat_rate is not None and gt3_free_steam_factor is not None:
        gt3_ng_result = calculate_gt_ng_mmbtu(gt3_kwh, gt3_heat_rate, gt3_free_steam_factor, "GT3")
        ng_gt3 = gt3_ng_result["net_mmbtu"]
        gt3_ng_norm = gt3_ng_result["norm"]
        gt3_ng_method = "heat_rate_lookup"
    else:
        ng_gt3 = gt3_kwh * NORM_GT3_NATURAL_GAS_MMBTU_PER_KWH if gt3_available else 0
        gt3_ng_norm = NORM_GT3_NATURAL_GAS_MMBTU_PER_KWH
        gt3_ng_method = "legacy_fixed_norm"
        gt3_ng_result = None
    
    # HRSG Natural Gas (MMBTU) - for supplementary firing
    # Use reverse calculated values from heat rate lookup if available
    if hrsg_ng_calculation and hrsg_ng_calculation.get("calculation_method") == "heat_rate_lookup":
        # Extract NG values from reverse calculation
        hrsg_ng_details = hrsg_ng_calculation.get("hrsg_ng_details", [])
        ng_hrsg1 = 0.0
        ng_hrsg2 = 0.0
        ng_hrsg3 = 0.0
        hrsg1_ng_norm = NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT  # Default
        hrsg2_ng_norm = NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT
        hrsg3_ng_norm = NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT
        
        for ng_detail in hrsg_ng_details:
            hrsg_name = ng_detail.get("hrsg_name", "")
            ng_qty = ng_detail.get("ng_quantity_mmbtu", 0.0)
            ng_norm = ng_detail.get("ng_norm_mmbtu_mt", NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT)
            
            if "HRSG1" in hrsg_name.upper():
                ng_hrsg1 = ng_qty
                hrsg1_ng_norm = ng_norm
            elif "HRSG2" in hrsg_name.upper():
                ng_hrsg2 = ng_qty
                hrsg2_ng_norm = ng_norm
            elif "HRSG3" in hrsg_name.upper():
                ng_hrsg3 = ng_qty
                hrsg3_ng_norm = ng_norm
    else:
        # Fallback to legacy fixed norms
        ng_hrsg1 = shp_from_hrsg1 * NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT if hrsg1_available else 0
        ng_hrsg2 = shp_from_hrsg2 * NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT if hrsg2_available else 0
        ng_hrsg3 = shp_from_hrsg3 * NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT if hrsg3_available else 0
        hrsg1_ng_norm = NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT
        hrsg2_ng_norm = NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT
        hrsg3_ng_norm = NORM_HRSG_NATURAL_GAS_MMBTU_PER_MT
    
    total_natural_gas = ng_gt1 + ng_gt2 + ng_gt3 + ng_hrsg1 + ng_hrsg2 + ng_hrsg3
    
    # =========================================================
    # 2. COOLING WATER CONSUMPTION (KM³) - CW1 and CW2 separately
    # =========================================================
    # Cooling Water 1 (CW1) - Process plants
    total_cw1 = cw1_process_km3
    
    # Cooling Water 2 (CW2) - Power Plants + Utility Plants + Process
    # Power Plants - Fixed per month when operating
    cw2_gt1 = NORM_GT2_COOLING_WATER_KM3_FIXED if gt1_available and gt1_gross_mwh > 0 else 0
    cw2_gt2 = NORM_GT2_COOLING_WATER_KM3_FIXED if gt2_available and gt2_gross_mwh > 0 else 0
    cw2_gt3 = NORM_GT3_COOLING_WATER_KM3_FIXED if gt3_available and gt3_gross_mwh > 0 else 0
    cw2_stg = NORM_STG_COOLING_WATER_KM3_FIXED if stg_gross_mwh > 0 else 0
    
    # Utility Plants - Fixed per month
    cw2_bfw = NORM_BFW_COOLING_WATER_KM3_FIXED
    cw2_air = NORM_AIR_COOLING_WATER_KM3_FIXED
    
    # Oxygen Plant - per MT produced
    cw2_oxygen = oxygen_mt * NORM_OXYGEN_COOLING_WATER_KM3_PER_MT
    
    # CW2 Plant cooling water (power + utility plants)
    cw2_plant = cw2_gt1 + cw2_gt2 + cw2_gt3 + cw2_stg + cw2_bfw + cw2_air + cw2_oxygen
    
    # Total CW2 = Plant + Process consumption
    total_cw2 = cw2_plant + cw2_process_km3
    
    # Total cooling water = CW1 + CW2
    total_cooling_water = total_cw1 + total_cw2
    
    # =========================================================
    # 3. COMPRESSED AIR CONSUMPTION (NM³)
    # =========================================================
    # Power Plants - Fixed per month when operating
    air_gt1 = NORM_GT2_COMPRESSED_AIR_NM3_FIXED if gt1_available and gt1_gross_mwh > 0 else 0
    air_gt2 = NORM_GT2_COMPRESSED_AIR_NM3_FIXED if gt2_available and gt2_gross_mwh > 0 else 0
    air_gt3 = NORM_GT3_COMPRESSED_AIR_NM3_FIXED if gt3_available and gt3_gross_mwh > 0 else 0
    air_stg = NORM_STG_COMPRESSED_AIR_NM3_FIXED if stg_gross_mwh > 0 else 0
    
    # HRSG - Fixed per month when operating
    air_hrsg1 = NORM_HRSG_COMPRESSED_AIR_NM3_FIXED if hrsg1_available and shp_from_hrsg1 > 0 else 0
    air_hrsg2 = NORM_HRSG_COMPRESSED_AIR_NM3_FIXED if hrsg2_available and shp_from_hrsg2 > 0 else 0
    air_hrsg3 = NORM_HRSG_COMPRESSED_AIR_NM3_FIXED if hrsg3_available and shp_from_hrsg3 > 0 else 0
    
    # Cooling Water Plants - Fixed per month
    air_cw1 = NORM_CW1_COMPRESSED_AIR_NM3_FIXED
    air_cw2 = NORM_CW2_COMPRESSED_AIR_NM3_FIXED
    
    # Plant compressed air (utility plants only)
    air_plant = air_gt1 + air_gt2 + air_gt3 + air_stg + air_hrsg1 + air_hrsg2 + air_hrsg3 + air_cw1 + air_cw2
    
    # Total compressed air = Plant + Process consumption
    total_compressed_air = air_plant + air_process_nm3
    
    # =========================================================
    # 4. BFW CONSUMPTION (M³)
    # =========================================================
    # HRSG BFW
    bfw_hrsg1 = shp_from_hrsg1 * NORM_HRSG_BFW_M3_PER_MT if hrsg1_available else 0
    bfw_hrsg2 = shp_from_hrsg2 * NORM_HRSG_BFW_M3_PER_MT if hrsg2_available else 0
    bfw_hrsg3 = shp_from_hrsg3 * NORM_HRSG_BFW_M3_PER_MT if hrsg3_available else 0
    
    # PRDS BFW
    bfw_hp_prds = hp_from_prds * NORM_HP_PRDS_BFW_M3_PER_MT
    bfw_mp_prds = mp_from_prds * NORM_MP_PRDS_BFW_M3_PER_MT
    bfw_lp_prds = lp_from_prds * NORM_LP_PRDS_BFW_M3_PER_MT
    
    total_bfw = bfw_hrsg1 + bfw_hrsg2 + bfw_hrsg3 + bfw_hp_prds + bfw_mp_prds + bfw_lp_prds
    
    # =========================================================
    # 5. DM WATER CONSUMPTION (M³)
    # =========================================================
    dm_for_bfw = total_bfw * NORM_BFW_DM_WATER_M3_PER_M3
    
    # Total DM water = BFW requirement + Process consumption
    total_dm_water = dm_for_bfw + dm_process_m3
    
    # =========================================================
    # 6. RAW WATER CONSUMPTION (M³)
    # =========================================================
    # Cooling Water Plants (Excel-matched split: CW1=53.12%, CW2=46.88%)
    CW1_RATIO = 0.5312  # 53.12% of total CW from CW1
    CW2_RATIO = 0.4688  # 46.88% of total CW from CW2
    water_cw1 = total_cooling_water * NORM_CW1_WATER_M3_PER_KM3 * CW1_RATIO
    water_cw2 = total_cooling_water * NORM_CW2_WATER_M3_PER_KM3 * CW2_RATIO
    
    # DM Water Plant
    water_dm = total_dm_water * NORM_DM_WATER_M3_PER_M3
    
    # HRSG
    water_hrsg2 = shp_from_hrsg2 * NORM_HRSG_WATER_M3_PER_MT if hrsg2_available else 0
    water_hrsg3 = shp_from_hrsg3 * NORM_HRSG_WATER_M3_PER_MT if hrsg3_available else 0
    
    # Effluent
    water_effluent = effluent_m3 * NORM_EFFLUENT_WATER_M3_PER_M3
    
    total_raw_water = water_cw1 + water_cw2 + water_dm + water_hrsg2 + water_hrsg3 + water_effluent
    
    # =========================================================
    # 7. LP STEAM CONSUMPTION (MT) - for BFW heating
    # =========================================================
    lp_for_bfw = total_bfw * NORM_BFW_LP_STEAM_MT_PER_M3
    
    # LP Steam credit from HRSG
    lp_credit_hrsg2 = shp_from_hrsg2 * abs(NORM_HRSG_LP_STEAM_CREDIT_MT_PER_MT) if hrsg2_available else 0
    lp_credit_hrsg3 = shp_from_hrsg3 * abs(NORM_HRSG_LP_STEAM_CREDIT_MT_PER_MT) if hrsg3_available else 0
    
    # =========================================================
    # 8. POWER CONSUMPTION BY UTILITY PLANTS (KWH)
    # =========================================================
    power_bfw = total_bfw * NORM_BFW_POWER_KWH_PER_M3
    power_dm = total_dm_water * NORM_DM_POWER_KWH_PER_M3
    power_cw1 = total_cooling_water * CW1_RATIO * NORM_CW1_POWER_KWH_PER_KM3  # 53.12% CW1
    power_cw2 = total_cooling_water * CW2_RATIO * NORM_CW2_POWER_KWH_PER_KM3  # 46.88% CW2
    power_air = total_compressed_air * NORM_AIR_POWER_KWH_PER_NM3
    power_oxygen = oxygen_mt * NORM_OXYGEN_POWER_KWH_PER_MT
    power_effluent = effluent_m3 * NORM_EFFLUENT_POWER_KWH_PER_M3
    
    total_utility_power_kwh = power_bfw + power_dm + power_cw1 + power_cw2 + power_air + power_oxygen + power_effluent
    total_utility_power_mwh = total_utility_power_kwh / 1000
    
    # =========================================================
    # 9. RETURN CONDENSATE (M³)
    # =========================================================
    condensate_stg = stg_kwh * NORM_STG_RET_CONDENSATE_M3_PER_KWH
    condensate_dm = total_dm_water * NORM_DM_RET_CONDENSATE_M3_PER_M3
    total_condensate = condensate_stg + condensate_dm
    
    return {
        # Natural Gas
        "natural_gas": {
            "gt1_mmbtu": round(ng_gt1, 2),
            "gt2_mmbtu": round(ng_gt2, 2),
            "gt3_mmbtu": round(ng_gt3, 2),
            "hrsg1_mmbtu": round(ng_hrsg1, 2),
            "hrsg2_mmbtu": round(ng_hrsg2, 2),
            "hrsg3_mmbtu": round(ng_hrsg3, 2),
            "total_mmbtu": round(total_natural_gas, 2),
            # GT NG norms (for saving to NormsMonthDetail) - reverse calculated from heat rate lookup
            "gt1_ng_norm": gt1_ng_norm,
            "gt2_ng_norm": gt2_ng_norm,
            "gt3_ng_norm": gt3_ng_norm,
            "gt1_ng_method": gt1_ng_method,
            "gt2_ng_method": gt2_ng_method,
            "gt3_ng_method": gt3_ng_method,
            # GT NG calculation details (for debugging/logging)
            "gt1_ng_details": gt1_ng_result,
            "gt2_ng_details": gt2_ng_result,
            "gt3_ng_details": gt3_ng_result,
            # HRSG NG norms (for saving to NormsMonthDetail)
            "hrsg1_ng_norm": hrsg1_ng_norm,
            "hrsg2_ng_norm": hrsg2_ng_norm,
            "hrsg3_ng_norm": hrsg3_ng_norm,
            "calculation_method": hrsg_ng_calculation.get("calculation_method", "legacy_fixed_norm") if hrsg_ng_calculation else "legacy_fixed_norm",
        },
        # Cooling Water (CW1 and CW2 separately)
        "cooling_water": {
            "cw1_process_km3": round(cw1_process_km3, 2),
            "cw1_total_km3": round(total_cw1, 2),
            "cw2_gt1_km3": round(cw2_gt1, 2),
            "cw2_gt2_km3": round(cw2_gt2, 2),
            "cw2_gt3_km3": round(cw2_gt3, 2),
            "cw2_stg_km3": round(cw2_stg, 2),
            "cw2_bfw_km3": round(cw2_bfw, 2),
            "cw2_air_km3": round(cw2_air, 2),
            "cw2_oxygen_km3": round(cw2_oxygen, 2),
            "cw2_process_km3": round(cw2_process_km3, 2),
            "cw2_total_km3": round(total_cw2, 2),
            "total_km3": round(total_cooling_water, 2),
        },
        # Compressed Air
        "compressed_air": {
            "gt1_nm3": round(air_gt1, 2),
            "gt2_nm3": round(air_gt2, 2),
            "gt3_nm3": round(air_gt3, 2),
            "stg_nm3": round(air_stg, 2),
            "hrsg1_nm3": round(air_hrsg1, 2),
            "hrsg2_nm3": round(air_hrsg2, 2),
            "hrsg3_nm3": round(air_hrsg3, 2),
            "cw1_nm3": round(air_cw1, 2),
            "cw2_nm3": round(air_cw2, 2),
            "total_nm3": round(total_compressed_air, 2),
        },
        # BFW
        "bfw": {
            "hrsg1_m3": round(bfw_hrsg1, 2),
            "hrsg2_m3": round(bfw_hrsg2, 2),
            "hrsg3_m3": round(bfw_hrsg3, 2),
            "hp_prds_m3": round(bfw_hp_prds, 2),
            "mp_prds_m3": round(bfw_mp_prds, 2),
            "lp_prds_m3": round(bfw_lp_prds, 2),
            "total_m3": round(total_bfw, 2),
        },
        # DM Water
        "dm_water": {
            "for_bfw_m3": round(dm_for_bfw, 2),
            "total_m3": round(total_dm_water, 2),
        },
        # Raw Water
        "raw_water": {
            "cw1_m3": round(water_cw1, 2),
            "cw2_m3": round(water_cw2, 2),
            "dm_m3": round(water_dm, 2),
            "hrsg2_m3": round(water_hrsg2, 2),
            "hrsg3_m3": round(water_hrsg3, 2),
            "effluent_m3": round(water_effluent, 2),
            "total_m3": round(total_raw_water, 2),
        },
        # LP Steam
        "lp_steam": {
            "for_bfw_mt": round(lp_for_bfw, 2),
            "credit_hrsg2_mt": round(lp_credit_hrsg2, 2),
            "credit_hrsg3_mt": round(lp_credit_hrsg3, 2),
        },
        # Utility Power
        "utility_power": {
            "bfw_kwh": round(power_bfw, 2),
            "dm_kwh": round(power_dm, 2),
            "cw1_kwh": round(power_cw1, 2),
            "cw2_kwh": round(power_cw2, 2),
            "air_kwh": round(power_air, 2),
            "oxygen_kwh": round(power_oxygen, 2),
            "effluent_kwh": round(power_effluent, 2),
            "total_kwh": round(total_utility_power_kwh, 2),
            "total_mwh": round(total_utility_power_mwh, 2),
        },
        # Return Condensate
        "condensate": {
            "stg_m3": round(condensate_stg, 2),
            "dm_m3": round(condensate_dm, 2),
            "total_m3": round(total_condensate, 2),
        },
        # Input parameters (for output table)
        "shp_from_hrsg1": shp_from_hrsg1,
        "shp_from_hrsg2": shp_from_hrsg2,
        "shp_from_hrsg3": shp_from_hrsg3,
        "oxygen_mt": oxygen_mt,
        "effluent_m3": effluent_m3,
    }


def print_utility_summary(utilities: dict):
    """Print a formatted summary of utility calculations."""
    print("\n" + "="*100)
    print("UTILITY CONSUMPTION SUMMARY")
    print("="*100)
    
    # Natural Gas
    ng = utilities["natural_gas"]
    print("\n--- NATURAL GAS (MMBTU) ---")
    print(f"  {'Source':<20} {'Quantity':>15}")
    print(f"  {'-'*35}")
    if ng["gt1_mmbtu"] > 0: print(f"  {'GT1':<20} {ng['gt1_mmbtu']:>15,.2f}")
    if ng["gt2_mmbtu"] > 0: print(f"  {'GT2':<20} {ng['gt2_mmbtu']:>15,.2f}")
    if ng["gt3_mmbtu"] > 0: print(f"  {'GT3':<20} {ng['gt3_mmbtu']:>15,.2f}")
    if ng["hrsg2_mmbtu"] > 0: print(f"  {'HRSG2':<20} {ng['hrsg2_mmbtu']:>15,.2f}")
    if ng["hrsg3_mmbtu"] > 0: print(f"  {'HRSG3':<20} {ng['hrsg3_mmbtu']:>15,.2f}")
    print(f"  {'-'*35}")
    print(f"  {'TOTAL':<20} {ng['total_mmbtu']:>15,.2f}")
    
    # Cooling Water (CW1 and CW2 separately)
    cw = utilities["cooling_water"]
    print("\n--- COOLING WATER 1 (KM³) ---")
    print(f"  {'Consumer':<20} {'Quantity':>15}")
    print(f"  {'-'*35}")
    print(f"  {'Process Plants':<20} {cw.get('cw1_process_km3', 0):>15,.2f}")
    print(f"  {'-'*35}")
    print(f"  {'CW1 TOTAL':<20} {cw.get('cw1_total_km3', 0):>15,.2f}")
    
    print("\n--- COOLING WATER 2 (KM³) ---")
    print(f"  {'Consumer':<20} {'Quantity':>15}")
    print(f"  {'-'*35}")
    if cw.get("cw2_gt1_km3", 0) > 0: print(f"  {'GT1':<20} {cw['cw2_gt1_km3']:>15,.2f}")
    if cw.get("cw2_gt2_km3", 0) > 0: print(f"  {'GT2':<20} {cw['cw2_gt2_km3']:>15,.2f}")
    if cw.get("cw2_gt3_km3", 0) > 0: print(f"  {'GT3':<20} {cw['cw2_gt3_km3']:>15,.2f}")
    if cw.get("cw2_stg_km3", 0) > 0: print(f"  {'STG':<20} {cw['cw2_stg_km3']:>15,.2f}")
    if cw.get("cw2_bfw_km3", 0) > 0: print(f"  {'BFW Plant':<20} {cw['cw2_bfw_km3']:>15,.2f}")
    if cw.get("cw2_air_km3", 0) > 0: print(f"  {'Compressed Air':<20} {cw['cw2_air_km3']:>15,.2f}")
    if cw.get("cw2_oxygen_km3", 0) > 0: print(f"  {'Oxygen Plant':<20} {cw['cw2_oxygen_km3']:>15,.2f}")
    if cw.get("cw2_process_km3", 0) > 0: print(f"  {'Process Plants':<20} {cw['cw2_process_km3']:>15,.2f}")
    print(f"  {'-'*35}")
    print(f"  {'CW2 TOTAL':<20} {cw.get('cw2_total_km3', 0):>15,.2f}")
    
    print(f"\n  {'='*35}")
    print(f"  {'TOTAL CW (CW1+CW2)':<20} {cw['total_km3']:>15,.2f}")
    
    # Compressed Air
    air = utilities["compressed_air"]
    print("\n--- COMPRESSED AIR (NM³) ---")
    print(f"  {'Consumer':<20} {'Quantity':>15}")
    print(f"  {'-'*35}")
    if air["gt1_nm3"] > 0: print(f"  {'GT1':<20} {air['gt1_nm3']:>15,.2f}")
    if air["gt2_nm3"] > 0: print(f"  {'GT2':<20} {air['gt2_nm3']:>15,.2f}")
    if air["gt3_nm3"] > 0: print(f"  {'GT3':<20} {air['gt3_nm3']:>15,.2f}")
    if air["stg_nm3"] > 0: print(f"  {'STG':<20} {air['stg_nm3']:>15,.2f}")
    if air["hrsg2_nm3"] > 0: print(f"  {'HRSG2':<20} {air['hrsg2_nm3']:>15,.2f}")
    if air["hrsg3_nm3"] > 0: print(f"  {'HRSG3':<20} {air['hrsg3_nm3']:>15,.2f}")
    if air["cw1_nm3"] > 0: print(f"  {'Cooling Water 1':<20} {air['cw1_nm3']:>15,.2f}")
    if air["cw2_nm3"] > 0: print(f"  {'Cooling Water 2':<20} {air['cw2_nm3']:>15,.2f}")
    print(f"  {'-'*35}")
    print(f"  {'TOTAL':<20} {air['total_nm3']:>15,.2f}")
    
    # BFW
    bfw = utilities["bfw"]
    print("\n--- BOILER FEED WATER (M³) ---")
    print(f"  {'Consumer':<20} {'Quantity':>15}")
    print(f"  {'-'*35}")
    if bfw["hrsg2_m3"] > 0: print(f"  {'HRSG2':<20} {bfw['hrsg2_m3']:>15,.2f}")
    if bfw["hrsg3_m3"] > 0: print(f"  {'HRSG3':<20} {bfw['hrsg3_m3']:>15,.2f}")
    if bfw["hp_prds_m3"] > 0: print(f"  {'HP PRDS':<20} {bfw['hp_prds_m3']:>15,.2f}")
    if bfw["mp_prds_m3"] > 0: print(f"  {'MP PRDS':<20} {bfw['mp_prds_m3']:>15,.2f}")
    if bfw["lp_prds_m3"] > 0: print(f"  {'LP PRDS':<20} {bfw['lp_prds_m3']:>15,.2f}")
    print(f"  {'-'*35}")
    print(f"  {'TOTAL':<20} {bfw['total_m3']:>15,.2f}")
    
    # DM Water
    dm = utilities["dm_water"]
    print("\n--- DM WATER (M³) ---")
    print(f"  {'Consumer':<20} {'Quantity':>15}")
    print(f"  {'-'*35}")
    print(f"  {'For BFW':<20} {dm['for_bfw_m3']:>15,.2f}")
    print(f"  {'-'*35}")
    print(f"  {'TOTAL':<20} {dm['total_m3']:>15,.2f}")
    
    # Raw Water
    rw = utilities["raw_water"]
    print("\n--- RAW WATER (M³) ---")
    print(f"  {'Consumer':<20} {'Quantity':>15}")
    print(f"  {'-'*35}")
    print(f"  {'Cooling Water 1':<20} {rw['cw1_m3']:>15,.2f}")
    print(f"  {'Cooling Water 2':<20} {rw['cw2_m3']:>15,.2f}")
    print(f"  {'DM Water Plant':<20} {rw['dm_m3']:>15,.2f}")
    if rw["hrsg2_m3"] > 0: print(f"  {'HRSG2':<20} {rw['hrsg2_m3']:>15,.2f}")
    if rw["hrsg3_m3"] > 0: print(f"  {'HRSG3':<20} {rw['hrsg3_m3']:>15,.2f}")
    if rw["effluent_m3"] > 0: print(f"  {'Effluent':<20} {rw['effluent_m3']:>15,.2f}")
    print(f"  {'-'*35}")
    print(f"  {'TOTAL':<20} {rw['total_m3']:>15,.2f}")
    
    # Utility Power
    up = utilities["utility_power"]
    print("\n--- UTILITY POWER CONSUMPTION ---")
    print(f"  {'Utility Plant':<20} {'Power (KWH)':>15} {'Power (MWH)':>15}")
    print(f"  {'-'*50}")
    print(f"  {'BFW Plant':<20} {up['bfw_kwh']:>15,.2f} {up['bfw_kwh']/1000:>15,.2f}")
    print(f"  {'DM Water Plant':<20} {up['dm_kwh']:>15,.2f} {up['dm_kwh']/1000:>15,.2f}")
    print(f"  {'Cooling Water 1':<20} {up['cw1_kwh']:>15,.2f} {up['cw1_kwh']/1000:>15,.2f}")
    print(f"  {'Cooling Water 2':<20} {up['cw2_kwh']:>15,.2f} {up['cw2_kwh']/1000:>15,.2f}")
    print(f"  {'Compressed Air':<20} {up['air_kwh']:>15,.2f} {up['air_kwh']/1000:>15,.2f}")
    if up["oxygen_kwh"] > 0: print(f"  {'Oxygen Plant':<20} {up['oxygen_kwh']:>15,.2f} {up['oxygen_kwh']/1000:>15,.2f}")
    if up["effluent_kwh"] > 0: print(f"  {'Effluent Treatment':<20} {up['effluent_kwh']:>15,.2f} {up['effluent_kwh']/1000:>15,.2f}")
    print(f"  {'-'*50}")
    print(f"  {'TOTAL':<20} {up['total_kwh']:>15,.2f} {up['total_mwh']:>15,.2f}")
    
    print("\n" + "="*100)


def generate_utility_output_table(
    utilities: dict,
    power_dispatch: list = None,
    steam_balance: dict = None,
) -> dict:
    """
    Generate structured output table matching the NMD utility structure.
    
    Returns a dict with all utilities organized by Generating Plant and Utility type.
    """
    ng = utilities.get("natural_gas", {})
    cw = utilities.get("cooling_water", {})
    air = utilities.get("compressed_air", {})
    bfw = utilities.get("bfw", {})
    dm = utilities.get("dm_water", {})
    rw = utilities.get("raw_water", {})
    up = utilities.get("utility_power", {})
    lp_steam = utilities.get("lp_steam", {})
    
    # Extract power dispatch values
    gt1_gross = gt2_gross = gt3_gross = stg_gross = 0.0
    gt1_aux = gt2_aux = gt3_aux = stg_aux = 0.0
    gt1_net = gt2_net = gt3_net = stg_net = 0.0
    
    if power_dispatch:
        for asset in power_dispatch:
            name = str(asset.get("AssetName", "")).upper()
            if "GT1" in name or "PLANT-1" in name:
                gt1_gross = asset.get("GrossMWh", 0)
                gt1_aux = asset.get("AuxMWh", 0)
                gt1_net = asset.get("NetMWh", 0)
            elif "GT2" in name or "PLANT-2" in name:
                gt2_gross = asset.get("GrossMWh", 0)
                gt2_aux = asset.get("AuxMWh", 0)
                gt2_net = asset.get("NetMWh", 0)
            elif "GT3" in name or "PLANT-3" in name:
                gt3_gross = asset.get("GrossMWh", 0)
                gt3_aux = asset.get("AuxMWh", 0)
                gt3_net = asset.get("NetMWh", 0)
            elif "STG" in name or "STEAM TURBINE" in name:
                stg_gross = asset.get("GrossMWh", 0)
                stg_aux = asset.get("AuxMWh", 0)
                stg_net = asset.get("NetMWh", 0)
    
    # Extract steam balance values
    hp_prds = mp_prds = lp_prds = 0.0
    hrsg1_shp = hrsg2_shp = hrsg3_shp = 0.0
    stg_lp = stg_mp = 0.0
    
    if steam_balance:
        lp_bal = steam_balance.get("lp_balance", {})
        mp_bal = steam_balance.get("mp_balance", {})
        hp_bal = steam_balance.get("hp_balance", {})
        lp_prds = lp_bal.get("lp_from_prds", 0)
        mp_prds = mp_bal.get("mp_from_prds", 0)
        hp_prds = hp_bal.get("hp_total", 0)
        stg_lp = lp_bal.get("lp_from_stg", 0)
        stg_mp = mp_bal.get("mp_from_stg", 0)
    
    # Build the structured output
    output = {
        # =============================================
        # NMD - Power Plant 1 (GT1) - CORRECT MAPPING
        # =============================================
        "NMD - Power Plant 1": {
            "POWERGEN": {
                "Gross_MWH": gt1_gross,
                "Aux_MWH": gt1_aux,
                "Net_MWH": gt1_net,
                "Natural_Gas_MMBTU": ng.get("gt1_mmbtu", 0),
                "Cooling_Water_KM3": cw.get("gt1_km3", 0),
                "Compressed_Air_NM3": air.get("gt1_nm3", 0),
            }
        },
        # =============================================
        # NMD - Power Plant 2 (GT2)
        # =============================================
        "NMD - Power Plant 2": {
            "POWERGEN": {
                "Gross_MWH": gt2_gross,
                "Aux_MWH": gt2_aux,
                "Net_MWH": gt2_net,
                "Natural_Gas_MMBTU": ng.get("gt2_mmbtu", 0),
                "Cooling_Water_KM3": cw.get("gt2_km3", 0),
                "Compressed_Air_NM3": air.get("gt2_nm3", 0),
            }
        },
        # =============================================
        # NMD - Power Plant 3 (GT3) - CORRECT MAPPING
        # =============================================
        "NMD - Power Plant 3": {
            "POWERGEN": {
                "Gross_MWH": gt3_gross,
                "Aux_MWH": gt3_aux,
                "Net_MWH": gt3_net,
                "Natural_Gas_MMBTU": ng.get("gt3_mmbtu", 0),
                "Cooling_Water_KM3": cw.get("gt3_km3", 0),
                "Compressed_Air_NM3": air.get("gt3_nm3", 0),
            }
        },
        # =============================================
        # NMD - STG Power Plant
        # =============================================
        "NMD - STG Power Plant": {
            "POWERGEN": {
                "Gross_MWH": stg_gross,
                "Aux_MWH": stg_aux,
                "Net_MWH": stg_net,
                "Cooling_Water_KM3": cw.get("stg_km3", 0),
                "Compressed_Air_NM3": air.get("stg_nm3", 0),
            }
        },
        # =============================================
        # NMD - Utility Plant
        # =============================================
        "NMD - Utility Plant": {
            "Boiler Feed Water": {
                "Total_M3": bfw.get("total_m3", 0),
                "Power_KWH": up.get("bfw_kwh", 0),
            },
            "COMPRESSED AIR": {
                "Total_NM3": air.get("total_nm3", 0),
                "Power_KWH": up.get("air_kwh", 0),
            },
            "Cooling Water 1": {
                "Total_KM3": cw.get("cw1_km3", 0),
                "Raw_Water_M3": rw.get("cw1_m3", 0),
                "Power_KWH": up.get("cw1_kwh", 0),
            },
            "Cooling Water 2": {
                "Total_KM3": cw.get("cw2_km3", 0),
                "Raw_Water_M3": rw.get("cw2_m3", 0),
                "Power_KWH": up.get("cw2_kwh", 0),
            },
            "D M Water": {
                "Total_M3": dm.get("total_m3", 0),
                "Raw_Water_M3": rw.get("dm_m3", 0),
                "Power_KWH": up.get("dm_kwh", 0),
            },
            "Effluent Treated": {
                "Total_M3": utilities.get("effluent_m3", 0),
                "Power_KWH": up.get("effluent_kwh", 0),
            },
            "HP Steam PRDS": {
                "Steam_MT": hp_prds,
                "BFW_M3": bfw.get("hp_prds_m3", 0),
            },
            "HRSG1_SHP STEAM": {
                "Steam_MT": hrsg1_shp,
                "Natural_Gas_MMBTU": ng.get("hrsg1_mmbtu", 0),
                "BFW_M3": bfw.get("hrsg1_m3", 0),
            },
            "HRSG2_SHP STEAM": {
                "Steam_MT": utilities.get("shp_from_hrsg2", 0),
                "Natural_Gas_MMBTU": ng.get("hrsg2_mmbtu", 0),
                "BFW_M3": bfw.get("hrsg2_m3", 0),
                "Compressed_Air_NM3": air.get("hrsg2_nm3", 0),
            },
            "HRSG3_SHP STEAM": {
                "Steam_MT": utilities.get("shp_from_hrsg3", 0),
                "Natural_Gas_MMBTU": ng.get("hrsg3_mmbtu", 0),
                "BFW_M3": bfw.get("hrsg3_m3", 0),
                "Compressed_Air_NM3": air.get("hrsg3_nm3", 0),
            },
            "LP Steam PRDS": {
                "Steam_MT": lp_prds,
                "BFW_M3": bfw.get("lp_prds_m3", 0),
            },
            "MP Steam PRDS SHP": {
                "Steam_MT": mp_prds,
                "BFW_M3": bfw.get("mp_prds_m3", 0),
            },
            "Oxygen": {
                "Total_MT": utilities.get("oxygen_mt", 0),
                "Cooling_Water_KM3": cw.get("oxygen_km3", 0),
                "Power_KWH": up.get("oxygen_kwh", 0),
            },
            "STG1_LP STEAM": {
                "Steam_MT": stg_lp,
                "LP_Credit_MT": lp_steam.get("stg_credit_mt", 0),
            },
            "STG1_MP STEAM": {
                "Steam_MT": stg_mp,
            },
            "Treated Spent Caustic": {
                "Total_M3": 0,  # Not calculated yet
            },
        },
        # =============================================
        # NMD - Utility/Power Dist
        # =============================================
        "NMD - Utility/Power Dist": {
            "HP Steam_Dis": {
                "Steam_MT": hp_prds,
            },
            "LP Steam_Dis": {
                "Steam_MT": lp_prds + stg_lp + lp_steam.get("hrsg_credit_mt", 0),
            },
            "MP Steam_Dis": {
                "Steam_MT": mp_prds + stg_mp,
            },
            "Power_Dis": {
                "Total_MWH": up.get("total_mwh", 0),
            },
            "SHP Steam_Dis": {
                "Steam_MT": utilities.get("shp_from_hrsg2", 0) + utilities.get("shp_from_hrsg3", 0),
            },
        },
    }
    
    return output


def print_utility_output_table(output: dict):
    """
    Print the structured utility output table.
    """
    print("\n" + "="*120)
    print(" " * 40 + "UTILITY OUTPUT TABLE")
    print("="*120)
    
    print(f"\n{'Generating Plant':<30} {'Utility':<25} {'Parameter':<25} {'Value':>15} {'UOM':<15}")
    print("-"*120)
    
    for plant, utilities in output.items():
        first_plant = True
        for utility, values in utilities.items():
            first_utility = True
            for param, value in values.items():
                # Determine UOM based on parameter name
                if "MWH" in param.upper():
                    uom = "MWH"
                elif "KWH" in param.upper():
                    uom = "KWH"
                elif "MMBTU" in param.upper():
                    uom = "MMBTU"
                elif "KM3" in param.upper():
                    uom = "KM3"
                elif "NM3" in param.upper():
                    uom = "NM3"
                elif "M3" in param.upper():
                    uom = "M3"
                elif "MT" in param.upper():
                    uom = "MT"
                else:
                    uom = ""
                
                # Format parameter name (remove underscores)
                param_display = param.replace("_", " ")
                
                # Print row
                plant_col = plant if first_plant else ""
                utility_col = utility if first_utility else ""
                
                if value != 0:  # Only print non-zero values
                    print(f"{plant_col:<30} {utility_col:<25} {param_display:<25} {value:>15,.2f} {uom:<15}")
                    first_plant = False
                    first_utility = False
        
        if not first_plant:  # Only print separator if we printed something
            print("-"*120)
    
    print("="*120)


def print_nmd_budget_format(
    utilities: dict,
    power_dispatch: list = None,
    steam_balance: dict = None,
    stg_extraction: dict = None,
):
    """
    Print utility output in exact NMD Budget format with all columns:
    Generating Plant, Utility, Utility ID, UOM, Account, Material, Issuing Plant, UOM, Norms, Quantity, Expected, Difference
    """
    ng = utilities.get("natural_gas", {})
    cw = utilities.get("cooling_water", {})
    air = utilities.get("compressed_air", {})
    bfw = utilities.get("bfw", {})
    dm = utilities.get("dm_water", {})
    rw = utilities.get("raw_water", {})
    up = utilities.get("utility_power", {})
    lp_steam = utilities.get("lp_steam", {})
    condensate = utilities.get("condensate", {})
    
    # Expected values from Excel (April 2025)
    EXPECTED = {
        # Power Plant 1 (GT1)
        "pp1_ng": 0.00,
        "pp1_air": 0.00,
        "pp1_cw": 0.00,
        "pp1_power_dis": 0.00,
        # Power Plant 2 (GT2)
        "pp2_ng": 81819.53,
        "pp2_air": 30960.00,
        "pp2_cw": 108.00,
        "pp2_power_dis": 112896.00,
        # Power Plant 3 (GT3)
        "pp3_ng": 85739.92,
        "pp3_air": 30960.00,
        "pp3_cw": 108.00,
        "pp3_power_dis": 126733.30,
        # STG
        "stg_condensate": -29977.42,
        "stg_air": 41040.00,
        "stg_cw": 2376.00,
        "stg_power_dis": 20462.40,
        "stg_shp": 36423.07,
        # BFW
        "bfw_cw": 108.00,
        "bfw_dm": 86780.29,
        "bfw_lp": 14631.56,
        "bfw_power": 958619.51,
        # Compressed Air
        "air_cw": 175.00,
        "air_power": 1172912.65,
        # Cooling Water 1
        "cw1_water": 167898.34,
        "cw1_air": 1650.00,
        "cw1_power": 3722632.92,
        # Cooling Water 2
        "cw2_water": 154213.64,
        "cw2_air": 1650.00,
        "cw2_power": 3352470.37,
        # DM Water
        "dm_water": 149936.32,
        "dm_air": 10995.33,
        "dm_power": 172783.76,
        "dm_condensate": 28987.69,
        # Effluent
        "effluent_power": 860702.09,
        # HP Steam PRDS
        "hp_bfw": 381.84,
        "hp_shp": 4590.07,
        # HRSG1
        "hrsg1_ng": 0.00,
        "hrsg1_bfw": 0.00,
        "hrsg1_air": 0.00,
        "hrsg1_lp": 0.00,
        # HRSG2
        "hrsg2_ng": 129193.53,
        "hrsg2_bfw": 47140.49,
        "hrsg2_air": 453600.00,
        "hrsg2_lp": -2317.99,
        # HRSG3
        "hrsg3_ng": 133140.10,
        "hrsg3_bfw": 48401.55,
        "hrsg3_air": 453600.00,
        "hrsg3_lp": -2379.99,
        # LP Steam PRDS
        "lp_bfw": 3403.16,
        "lp_mp": 10209.49,
        # MP Steam PRDS
        "mp_bfw": 1580.27,
        "mp_shp": 15978.29,
        # Oxygen
        "oxygen_nitrogen": -14168185.99,
        "oxygen_cw": 1510.33,
        "oxygen_power": 5605298.71,
        # STG1_LP STEAM
        "stg_lp_shp": 10368.00,
        # STG1_MP STEAM
        "stg_mp_shp": 4968.00,
        # HP Steam_Dis
        "hp_dis": 4971.91,
        # LP Steam_Dis
        "lp_prds_dis": 13612.66,
        "lp_stg_dis": 21600.00,
        # MP Steam_Dis
        "mp_prds_dis": 17558.56,
        "mp_stg_dis": 7200.00,
        # Power_Dis
        "power_pp1": 0.00,
        "power_pp2": 8064000.00,
        "power_pp3": 9052378.46,
        "power_stg": 10231200.00,
        # SHP Steam_Dis
        "shp_hrsg1": 0.00,
        "shp_hrsg2": 46035.64,
        "shp_hrsg3": 47267.13,
        # Expected Power (KWH) for formula columns
        "exp_kwh_pp1": 0.00,
        "exp_kwh_pp2": 8064000.00,
        "exp_kwh_pp3": 9052378.46,
        "exp_kwh_stg": 10231200.00,
        # Expected quantities for utilities
        "exp_bfw_total": 100907.00,
        "exp_air_total": 7108652.00,
        "exp_cw1_total": 15194.47,
        "exp_cw2_total": 13409.88,
        "exp_dm_total": 142820.02,
        "exp_effluent": 243136.18,
        "exp_oxygen": 5787.00,
        
        # BFW - Catalyst & Chemicals
        "bfw_cyclohexy": 10.09,
        "bfw_morpholene": 0.20,
        "bfw_watreat": 57.52,
        
        # CW1 - Raw Materials
        "cw1_sulphuric_acid": 2.40,
        
        # CW2 - Raw Materials
        "cw2_sulphuric_acid": 2.12,
        
        # DM Water - Catalyst & Chemicals
        "dm_caustic": 32.27,
        "dm_alum_sulfate": 108.10,
        "dm_sodium_sulphite": 100.10,
        "dm_polyelectrolyte": 82.54,
        "dm_sodium_chloride": 1.43,
        "dm_hcl": 54.26,
        
        # Effluent - Raw Materials & Chemicals
        "effluent_water": 172.63,
        "effluent_urea": 182.25,
        
        # HRSG2 - Raw Materials & Chemicals
        "hrsg2_trisodium": 41.43,
        "hrsg2_furnace_oil": 4.53,
        "hrsg2_water": 122.45,
        
        # HRSG3 - Raw Materials & Chemicals
        "hrsg3_trisodium": 42.54,
        "hrsg3_furnace_oil": 4.65,
        "hrsg3_water": 125.73,
        
        # Power from MEL (Import)
        "power_mel": 18000000.00,
    }
    
    def print_row(calc_val, exp_key, prefix=""):
        """Print calculated, expected, difference, and % difference values"""
        exp_val = EXPECTED.get(exp_key, 0)
        diff = calc_val - exp_val
        # Calculate percentage difference (avoid division by zero)
        if exp_val != 0:
            pct_diff = (diff / exp_val) * 100
            pct_str = f"{pct_diff:>+8.2f}%"
        else:
            pct_str = f"{'N/A':>9}"
        return f"{calc_val:>15,.2f} {exp_val:>15,.2f} {diff:>15,.2f} {pct_str}"
    
    # Extract power dispatch values
    gt1_gross = gt2_gross = gt3_gross = stg_gross = 0.0
    gt1_aux = gt2_aux = gt3_aux = stg_aux = 0.0
    gt1_net = gt2_net = gt3_net = stg_net = 0.0
    gt1_kwh = gt2_kwh = gt3_kwh = stg_kwh = 0.0
    stg_hours = 0.0
    
    if power_dispatch:
        for asset in power_dispatch:
            name = str(asset.get("AssetName", "")).upper()
            if "GT1" in name or "PLANT-1" in name:
                gt1_gross = asset.get("GrossMWh", 0)
                gt1_aux = asset.get("AuxMWh", 0)
                gt1_net = asset.get("NetMWh", 0)
                gt1_kwh = gt1_gross * 1000
            elif "GT2" in name or "PLANT-2" in name:
                gt2_gross = asset.get("GrossMWh", 0)
                gt2_aux = asset.get("AuxMWh", 0)
                gt2_net = asset.get("NetMWh", 0)
                gt2_kwh = gt2_gross * 1000
            elif "GT3" in name or "PLANT-3" in name:
                gt3_gross = asset.get("GrossMWh", 0)
                gt3_aux = asset.get("AuxMWh", 0)
                gt3_net = asset.get("NetMWh", 0)
                gt3_kwh = gt3_gross * 1000
            elif "STG" in name or "STEAM TURBINE" in name:
                stg_gross = asset.get("GrossMWh", 0)
                stg_aux = asset.get("AuxMWh", 0)
                stg_net = asset.get("NetMWh", 0)
                stg_kwh = stg_gross * 1000
                stg_hours = asset.get("Hours", 0)
    
    # Extract steam balance values
    hp_prds = mp_prds = lp_prds = 0.0
    stg_lp = stg_mp = 0.0
    
    if steam_balance:
        lp_bal = steam_balance.get("lp_balance", {})
        mp_bal = steam_balance.get("mp_balance", {})
        hp_bal = steam_balance.get("hp_balance", {})
        lp_prds = lp_bal.get("lp_from_prds", 0)
        mp_prds = mp_bal.get("mp_from_prds", 0)
        hp_prds = hp_bal.get("hp_total", 0)
        stg_lp = lp_bal.get("lp_from_stg", 0)
        stg_mp = mp_bal.get("mp_from_stg", 0)
    
    # HRSG values
    hrsg1_shp = utilities.get("shp_from_hrsg1", 0)
    hrsg2_shp = utilities.get("shp_from_hrsg2", 0)
    hrsg3_shp = utilities.get("shp_from_hrsg3", 0)
    oxygen_mt = utilities.get("oxygen_mt", 0)
    effluent_m3 = utilities.get("effluent_m3", 0)
    
    # Norms
    # Use calculated GT NG norms from heat rate lookup if available, otherwise use legacy
    ng_data = utilities.get("natural_gas", {})
    NORM_NG_GT1 = ng_data.get('gt1_ng_norm', 0.0095)  # Default to legacy GT3 norm
    NORM_NG_GT2 = ng_data.get('gt2_ng_norm', 0.0101)  # Default to legacy GT2 norm
    NORM_NG_GT3 = ng_data.get('gt3_ng_norm', 0.0095)  # Default to legacy GT3 norm
    # Use calculated HRSG NG norms from heat rate lookup if available, otherwise use legacy
    NORM_NG_HRSG1 = ng_data.get('hrsg1_ng_norm', 2.8064)
    NORM_NG_HRSG2 = ng_data.get('hrsg2_ng_norm', 2.8064)
    NORM_NG_HRSG3 = ng_data.get('hrsg3_ng_norm', 2.8168)
    NORM_CW2_GT = 108.0  # Fixed
    NORM_CW2_STG = 2376.0  # Fixed
    NORM_AIR_GT = 30960.0  # Fixed
    NORM_AIR_STG = 41040.0  # Fixed
    NORM_POWER_DIS_GT = 0.0140
    NORM_POWER_DIS_STG = 0.0020
    # STG SHP - Calculate dynamically from stg_extraction lookup (steam_for_power_tph × hours)
    # sp_steam_power is MT/MWh, convert to MT/KWh by dividing by 1000
    steam_for_power_tph = stg_extraction.get('steam_for_power_tph', 0) if stg_extraction else 0
    sp_steam_power = stg_extraction.get('sp_steam_power', 0) if stg_extraction else 0
    stg_shp_from_lookup = steam_for_power_tph * stg_hours  # MT = TPH × Hours
    # Norm = sp_steam_power (MT/MWh) / 1000 = MT/KWh
    # Fallback: calculate from consumption if sp_steam_power is 0
    if sp_steam_power > 0:
        NORM_STG_SHP = sp_steam_power / 1000  # Dynamic norm from lookup
    elif stg_shp_from_lookup > 0 and stg_kwh > 0:
        NORM_STG_SHP = stg_shp_from_lookup / stg_kwh  # Calculate from consumption
    else:
        NORM_STG_SHP = 0.0036  # Default fallback
    
    # STG Condensate - Calculate dynamically from stg_extraction lookup (condensing_load_m3hr × hours)
    condensing_load_m3hr = stg_extraction.get('condensing_load_m3hr', 0) if stg_extraction else 0
    stg_condensate_from_lookup = condensing_load_m3hr * stg_hours  # M3 = M3/hr × Hours
    # Norm = condensate / stg_kwh (M3/KWh)
    if stg_condensate_from_lookup > 0 and stg_kwh > 0:
        NORM_STG_CONDENSATE = stg_condensate_from_lookup / stg_kwh  # Dynamic norm from lookup
    else:
        NORM_STG_CONDENSATE = 0.0029  # Default fallback
    NORM_BFW_HP_PRDS = 0.0768
    NORM_SHP_HP_PRDS = 0.9232
    NORM_BFW_HRSG = 1.0240
    NORM_AIR_HRSG = 453600.0  # Fixed
    NORM_LP_CREDIT_HRSG = -0.0504
    NORM_BFW_LP_PRDS = 0.2500
    NORM_MP_LP_PRDS = 0.7500
    NORM_BFW_MP_PRDS = 0.0900
    NORM_SHP_MP_PRDS = 0.9100
    NORM_BFW_DM = 0.8600
    NORM_LP_BFW = 0.1450
    NORM_POWER_BFW = 9.5000
    NORM_CW2_BFW = 108.0  # Fixed
    NORM_CW2_AIR = 175.0  # Fixed
    NORM_POWER_AIR = 0.1650
    NORM_WATER_CW1 = 11.0500
    NORM_AIR_CW1 = 1650.0  # Fixed
    NORM_POWER_CW1 = 245.0000
    NORM_WATER_CW2 = 11.5000
    NORM_AIR_CW2 = 1650.0  # Fixed
    NORM_POWER_CW2 = 250.0000
    NORM_POWER_DM = 1.2100
    NORM_AIR_DM = 0.0770
    NORM_CONDENSATE_DM = 0.2030
    NORM_WATER_DM = 1.0500
    NORM_POWER_EFFLUENT = 3.5400
    NORM_CW2_OXYGEN = 0.2610
    NORM_POWER_OXYGEN = 968.6500
    NORM_SHP_STG_LP = 0.4800
    NORM_SHP_STG_MP = 0.6900
    
    # Additional Norms - Raw Materials
    NORM_WATER_HRSG = 0.0026600  # M3/MT SHP for HRSG
    NORM_SULPHURIC_ACID_CW1 = 0.0001580  # MT/KM3 CW1
    NORM_SULPHURIC_ACID_CW2 = 0.0001580  # MT/KM3 CW2
    NORM_HCL_DM = 0.0003800  # MT/M3 DM Water
    NORM_FURNACE_OIL_HRSG = 0.0000983  # MMBTU/MT SHP
    NORM_WATER_EFFLUENT = 0.0007100  # M3/M3 Effluent
    
    # Additional Norms - Catalyst & Chemicals
    NORM_CYCLOHEXY_BFW = 0.0001  # KG/M3 BFW
    NORM_MORPHOLENE_BFW = 0.0000020  # MT/M3 BFW (very small)
    NORM_WATREAT_BFW = 0.0005700  # KG/M3 BFW
    NORM_CAUSTIC_DM = 0.0002260  # MT/M3 DM
    NORM_ALUM_SULFATE_DM = 0.0007570  # KG/M3 DM
    NORM_SODIUM_SULPHITE_DM = 0.0007010  # KG/M3 DM
    NORM_POLYELECTROLYTE_DM = 0.0005780  # KG/M3 DM
    NORM_SODIUM_CHLORIDE_DM = 0.0000100  # MT/M3 DM
    NORM_TRISODIUM_PHOSPHATE_HRSG = 0.0009  # KG/MT SHP
    NORM_UREA_EFFLUENT = 0.00075  # KG/M3 Effluent (182.25/243136.18)
    
    # By-product Norms
    NORM_NITROGEN_OXYGEN = 2448.4  # NM3/MT Oxygen (negative/credit)
    
    def calc_pct(calc, exp):
        """Calculate percentage difference"""
        if exp != 0:
            return f"{((calc - exp) / exp) * 100:>+8.2f}%"
        elif calc == 0:
            return f"{'0.00%':>9}"
        else:
            return f"{'N/A':>9}"
    
    def fmt_qty(qty):
        """Format base quantity with thousands separator"""
        return f"{qty:>18,.2f}"
    
    def calc_ref_qty(ref_value, norm):
        """Calculate reference quantity from reference value and norm"""
        if norm != 0:
            return ref_value / norm
        return 0
    
    def fmt_ref_qty(ref_value, norm):
        """Format reference quantity (Reference Value / Norm)"""
        ref_qty = calc_ref_qty(ref_value, norm)
        return f"{ref_qty:>18,.2f}"
    
    print("\n" + "="*270)
    print(" " * 100 + "NMD BUDGET FORMAT - FY 2026 (April 2025)")
    print("="*270)
    
    # Header - QTY (Model) and QTY (Ref) side by side
    print(f"{'Generating Plant':<25} {'Utility':<20} {'Material':<25} {'UOM':<8} {'QTY (Model)':>18} {'QTY (Ref)':>18} {'Norms':>10} {'Calculated (Model)':>20} {'Reference (Prev Yr)':>20} {'Difference':>18} {'% Diff':>10}")
    print("="*270)
    
    # ========================================
    # NMD - Power Plant 1 (GT1)
    # ========================================
    pp1_ng_calc = ng.get('gt1_mmbtu', 0)
    pp1_air_calc = air.get('gt1_nm3', 0)
    pp1_cw_calc = cw.get('cw2_gt1_km3', 0)
    pp1_power_calc = gt1_kwh * NORM_POWER_DIS_GT
    
    # QTY (Ref) is the reference generation quantity - should be constant from database
    pp1_ref_qty = EXPECTED.get('exp_kwh_pp1', 0)
    print(f"\n{'NMD - Power Plant 1':<25} {'POWERGEN':<20} {'':<25} {'KWH':<8}")
    print(f"{'':<25} {'':<20} {'NATURAL GAS':<25} {'MMBTU':<8} {fmt_qty(gt1_kwh)} {pp1_ref_qty:>18,.2f} {NORM_NG_GT3:>10.4f} {pp1_ng_calc:>20,.2f} {EXPECTED['pp1_ng']:>20,.2f} {pp1_ng_calc - EXPECTED['pp1_ng']:>18,.2f} {calc_pct(pp1_ng_calc, EXPECTED['pp1_ng'])}")
    print(f"{'':<25} {'':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(gt1_kwh)} {pp1_ref_qty:>18,.2f} {NORM_AIR_GT:>10.0f} {pp1_air_calc:>20,.2f} {EXPECTED['pp1_air']:>20,.2f} {pp1_air_calc - EXPECTED['pp1_air']:>18,.2f} {calc_pct(pp1_air_calc, EXPECTED['pp1_air'])}")
    print(f"{'':<25} {'':<20} {'Cooling Water 2':<25} {'KM3':<8} {fmt_qty(gt1_kwh)} {pp1_ref_qty:>18,.2f} {NORM_CW2_GT:>10.2f} {pp1_cw_calc:>20,.2f} {EXPECTED['pp1_cw']:>20,.2f} {pp1_cw_calc - EXPECTED['pp1_cw']:>18,.2f} {calc_pct(pp1_cw_calc, EXPECTED['pp1_cw'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(gt1_kwh)} {pp1_ref_qty:>18,.2f} {NORM_POWER_DIS_GT:>10.4f} {pp1_power_calc:>20,.2f} {EXPECTED['pp1_power_dis']:>20,.2f} {pp1_power_calc - EXPECTED['pp1_power_dis']:>18,.2f} {calc_pct(pp1_power_calc, EXPECTED['pp1_power_dis'])}")
    
    # ========================================
    # NMD - Power Plant 2 (GT2)
    # ========================================
    pp2_ng_calc = ng.get('gt2_mmbtu', 0)
    pp2_air_calc = air.get('gt2_nm3', 0)
    pp2_cw_calc = cw.get('cw2_gt2_km3', 0)
    pp2_power_calc = gt2_kwh * NORM_POWER_DIS_GT
    
    # QTY (Ref) is the reference generation quantity - should be constant from database
    pp2_ref_qty = EXPECTED.get('exp_kwh_pp2', 0)
    print(f"\n{'NMD - Power Plant 2':<25} {'POWERGEN':<20} {'':<25} {'KWH':<8}")
    print(f"{'':<25} {'':<20} {'NATURAL GAS':<25} {'MMBTU':<8} {fmt_qty(gt2_kwh)} {pp2_ref_qty:>18,.2f} {NORM_NG_GT2:>10.4f} {pp2_ng_calc:>20,.2f} {EXPECTED['pp2_ng']:>20,.2f} {pp2_ng_calc - EXPECTED['pp2_ng']:>18,.2f} {calc_pct(pp2_ng_calc, EXPECTED['pp2_ng'])}")
    print(f"{'':<25} {'':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(gt2_kwh)} {pp2_ref_qty:>18,.2f} {NORM_AIR_GT:>10.0f} {pp2_air_calc:>20,.2f} {EXPECTED['pp2_air']:>20,.2f} {pp2_air_calc - EXPECTED['pp2_air']:>18,.2f} {calc_pct(pp2_air_calc, EXPECTED['pp2_air'])}")
    print(f"{'':<25} {'':<20} {'Cooling Water 2':<25} {'KM3':<8} {fmt_qty(gt2_kwh)} {pp2_ref_qty:>18,.2f} {NORM_CW2_GT:>10.2f} {pp2_cw_calc:>20,.2f} {EXPECTED['pp2_cw']:>20,.2f} {pp2_cw_calc - EXPECTED['pp2_cw']:>18,.2f} {calc_pct(pp2_cw_calc, EXPECTED['pp2_cw'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(gt2_kwh)} {pp2_ref_qty:>18,.2f} {NORM_POWER_DIS_GT:>10.4f} {pp2_power_calc:>20,.2f} {EXPECTED['pp2_power_dis']:>20,.2f} {pp2_power_calc - EXPECTED['pp2_power_dis']:>18,.2f} {calc_pct(pp2_power_calc, EXPECTED['pp2_power_dis'])}")
    
    # ========================================
    # NMD - Power Plant 3 (GT3)
    # ========================================
    pp3_ng_calc = ng.get('gt3_mmbtu', 0)
    pp3_air_calc = air.get('gt3_nm3', 0)
    pp3_cw_calc = cw.get('cw2_gt3_km3', 0)
    pp3_power_calc = gt3_kwh * NORM_POWER_DIS_GT
    
    # QTY (Ref) is the reference generation quantity - should be constant from database
    pp3_ref_qty = EXPECTED.get('exp_kwh_pp3', 0)
    print(f"\n{'NMD - Power Plant 3':<25} {'POWERGEN':<20} {'':<25} {'KWH':<8}")
    print(f"{'':<25} {'':<20} {'NATURAL GAS':<25} {'MMBTU':<8} {fmt_qty(gt3_kwh)} {pp3_ref_qty:>18,.2f} {NORM_NG_GT3:>10.4f} {pp3_ng_calc:>20,.2f} {EXPECTED['pp3_ng']:>20,.2f} {pp3_ng_calc - EXPECTED['pp3_ng']:>18,.2f} {calc_pct(pp3_ng_calc, EXPECTED['pp3_ng'])}")
    print(f"{'':<25} {'':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(gt3_kwh)} {pp3_ref_qty:>18,.2f} {NORM_AIR_GT:>10.0f} {pp3_air_calc:>20,.2f} {EXPECTED['pp3_air']:>20,.2f} {pp3_air_calc - EXPECTED['pp3_air']:>18,.2f} {calc_pct(pp3_air_calc, EXPECTED['pp3_air'])}")
    print(f"{'':<25} {'':<20} {'Cooling Water 2':<25} {'KM3':<8} {fmt_qty(gt3_kwh)} {pp3_ref_qty:>18,.2f} {NORM_CW2_GT:>10.2f} {pp3_cw_calc:>20,.2f} {EXPECTED['pp3_cw']:>20,.2f} {pp3_cw_calc - EXPECTED['pp3_cw']:>18,.2f} {calc_pct(pp3_cw_calc, EXPECTED['pp3_cw'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(gt3_kwh)} {pp3_ref_qty:>18,.2f} {NORM_POWER_DIS_GT:>10.4f} {pp3_power_calc:>20,.2f} {EXPECTED['pp3_power_dis']:>20,.2f} {pp3_power_calc - EXPECTED['pp3_power_dis']:>18,.2f} {calc_pct(pp3_power_calc, EXPECTED['pp3_power_dis'])}")

    
    # ========================================
    # NMD - STG Power Plant
    # ========================================
    # STG SHP Steam_Dis = steam_for_power_tph × hours (from lookup table)
    stg_shp_consumption = stg_shp_from_lookup if stg_shp_from_lookup > 0 else (stg_kwh * 0.0036)
    # STG Condensate = condensing_load_m3hr × hours (from lookup table)
    stg_condensate = stg_condensate_from_lookup if stg_condensate_from_lookup > 0 else (stg_kwh * 0.0029)
    stg_condensate_neg = -stg_condensate
    stg_air_calc = air.get('stg_nm3', 0)
    stg_cw_calc = cw.get('cw2_stg_km3', 0)
    stg_power_calc = stg_kwh * NORM_POWER_DIS_STG
    
    exp_kwh_stg = EXPECTED.get('exp_kwh_stg', 0)
    # Use expected KWH directly as reference quantity (not derived from negative condensate)
    stg_ref_qty = exp_kwh_stg
    print(f"\n{'NMD - STG Power Plant':<25} {'POWERGEN':<20} {'':<25} {'KWH':<8}")
    print(f"{'':<25} {'':<20} {'Ret steam condensate':<25} {'M3':<8} {fmt_qty(stg_kwh)} {stg_ref_qty:>18,.2f} {NORM_STG_CONDENSATE:>10.4f} {stg_condensate_neg:>20,.2f} {EXPECTED['stg_condensate']:>20,.2f} {stg_condensate_neg - EXPECTED['stg_condensate']:>18,.2f} {calc_pct(stg_condensate_neg, EXPECTED['stg_condensate'])}")
    print(f"{'':<25} {'':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(stg_kwh)} {stg_ref_qty:>18,.2f} {NORM_AIR_STG:>10.0f} {stg_air_calc:>20,.2f} {EXPECTED['stg_air']:>20,.2f} {stg_air_calc - EXPECTED['stg_air']:>18,.2f} {calc_pct(stg_air_calc, EXPECTED['stg_air'])}")
    print(f"{'':<25} {'':<20} {'Cooling Water 2':<25} {'KM3':<8} {fmt_qty(stg_kwh)} {stg_ref_qty:>18,.2f} {NORM_CW2_STG:>10.2f} {stg_cw_calc:>20,.2f} {EXPECTED['stg_cw']:>20,.2f} {stg_cw_calc - EXPECTED['stg_cw']:>18,.2f} {calc_pct(stg_cw_calc, EXPECTED['stg_cw'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(stg_kwh)} {stg_ref_qty:>18,.2f} {NORM_POWER_DIS_STG:>10.4f} {stg_power_calc:>20,.2f} {EXPECTED['stg_power_dis']:>20,.2f} {stg_power_calc - EXPECTED['stg_power_dis']:>18,.2f} {calc_pct(stg_power_calc, EXPECTED['stg_power_dis'])}")
    print(f"{'':<25} {'':<20} {'SHP Steam_Dis':<25} {'MT':<8} {fmt_qty(stg_kwh)} {stg_ref_qty:>18,.2f} {NORM_STG_SHP:>10.4f} {stg_shp_consumption:>20,.2f} {EXPECTED['stg_shp']:>20,.2f} {stg_shp_consumption - EXPECTED['stg_shp']:>18,.2f} {calc_pct(stg_shp_consumption, EXPECTED['stg_shp'])}")
    
    # ========================================
    # NMD - Utility Plant - Boiler Feed Water
    # ========================================
    total_bfw = bfw.get("total_m3", 0)
    bfw_power = total_bfw * NORM_POWER_BFW
    bfw_dm = total_bfw * NORM_BFW_DM
    bfw_lp = total_bfw * NORM_LP_BFW
    exp_bfw = EXPECTED.get('exp_bfw_total', 0)
    # BFW - Catalyst & Chemicals calculations
    bfw_cyclohexy = total_bfw * NORM_CYCLOHEXY_BFW
    bfw_morpholene = total_bfw * NORM_MORPHOLENE_BFW
    bfw_watreat = total_bfw * NORM_WATREAT_BFW
    
    # QTY (Ref) is the reference quantity - should be constant from database
    bfw_ref_qty = EXPECTED.get('exp_bfw_total', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'Boiler Feed Water':<20} {'':<25} {'M3':<8}")
    print(f"{'':<25} {'Catalyst & Chemical':<20} {'CHEM CYCLO HEXY':<25} {'KG':<8} {fmt_qty(total_bfw)} {bfw_ref_qty:>18,.2f} {NORM_CYCLOHEXY_BFW:>10.7f} {bfw_cyclohexy:>20,.2f} {EXPECTED['bfw_cyclohexy']:>20,.2f} {bfw_cyclohexy - EXPECTED['bfw_cyclohexy']:>18,.2f} {calc_pct(bfw_cyclohexy, EXPECTED['bfw_cyclohexy'])}")
    print(f"{'':<25} {'':<20} {'CHEM MORPHOLENE':<25} {'MT':<8} {fmt_qty(total_bfw)} {bfw_ref_qty:>18,.2f} {NORM_MORPHOLENE_BFW:>10.7f} {bfw_morpholene:>20,.2f} {EXPECTED['bfw_morpholene']:>20,.2f} {bfw_morpholene - EXPECTED['bfw_morpholene']:>18,.2f} {calc_pct(bfw_morpholene, EXPECTED['bfw_morpholene'])}")
    print(f"{'':<25} {'':<20} {'KEM WATREAT B 70M':<25} {'KG':<8} {fmt_qty(total_bfw)} {bfw_ref_qty:>18,.2f} {NORM_WATREAT_BFW:>10.7f} {bfw_watreat:>20,.2f} {EXPECTED['bfw_watreat']:>20,.2f} {bfw_watreat - EXPECTED['bfw_watreat']:>18,.2f} {calc_pct(bfw_watreat, EXPECTED['bfw_watreat'])}")
    print(f"{'':<25} {'Utilities':<20} {'Cooling Water 2':<25} {'KM3':<8} {fmt_qty(total_bfw)} {bfw_ref_qty:>18,.2f} {NORM_CW2_BFW:>10.2f} {NORM_CW2_BFW:>20,.2f} {EXPECTED['bfw_cw']:>20,.2f} {NORM_CW2_BFW - EXPECTED['bfw_cw']:>18,.2f} {calc_pct(NORM_CW2_BFW, EXPECTED['bfw_cw'])}")
    print(f"{'':<25} {'':<20} {'D M Water':<25} {'M3':<8} {fmt_qty(total_bfw)} {bfw_ref_qty:>18,.2f} {NORM_BFW_DM:>10.4f} {bfw_dm:>20,.2f} {EXPECTED['bfw_dm']:>20,.2f} {bfw_dm - EXPECTED['bfw_dm']:>18,.2f} {calc_pct(bfw_dm, EXPECTED['bfw_dm'])}")
    print(f"{'':<25} {'':<20} {'LP Steam_Dis':<25} {'MT':<8} {fmt_qty(total_bfw)} {bfw_ref_qty:>18,.2f} {NORM_LP_BFW:>10.4f} {bfw_lp:>20,.2f} {EXPECTED['bfw_lp']:>20,.2f} {bfw_lp - EXPECTED['bfw_lp']:>18,.2f} {calc_pct(bfw_lp, EXPECTED['bfw_lp'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(total_bfw)} {bfw_ref_qty:>18,.2f} {NORM_POWER_BFW:>10.4f} {bfw_power:>20,.2f} {EXPECTED['bfw_power']:>20,.2f} {bfw_power - EXPECTED['bfw_power']:>18,.2f} {calc_pct(bfw_power, EXPECTED['bfw_power'])}")
    
    # ========================================
    # NMD - Utility Plant - COMPRESSED AIR
    # ========================================
    total_air = air.get("total_nm3", 0)
    air_power = total_air * NORM_POWER_AIR
    # QTY (Ref) is the reference quantity - should be constant from database
    air_ref_qty = EXPECTED.get('exp_air_total', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'COMPRESSED AIR':<20} {'':<25} {'NM3':<8}")
    print(f"{'':<25} {'':<20} {'Cooling Water 2':<25} {'KM3':<8} {fmt_qty(total_air)} {air_ref_qty:>18,.2f} {NORM_CW2_AIR:>10.2f} {NORM_CW2_AIR:>20,.2f} {EXPECTED['air_cw']:>20,.2f} {NORM_CW2_AIR - EXPECTED['air_cw']:>18,.2f} {calc_pct(NORM_CW2_AIR, EXPECTED['air_cw'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(total_air)} {air_ref_qty:>18,.2f} {NORM_POWER_AIR:>10.4f} {air_power:>20,.2f} {EXPECTED['air_power']:>20,.2f} {air_power - EXPECTED['air_power']:>18,.2f} {calc_pct(air_power, EXPECTED['air_power'])}")
    
    # ========================================
    # NMD - Utility Plant - Cooling Water 1
    # ========================================
    cw1_km3 = cw.get("cw1_total_km3", 0)
    cw1_water = cw1_km3 * NORM_WATER_CW1
    cw1_power = cw1_km3 * NORM_POWER_CW1
    cw1_sulphuric_acid = cw1_km3 * NORM_SULPHURIC_ACID_CW1
    # QTY (Ref) is the reference quantity - should be constant from database
    cw1_ref_qty = EXPECTED.get('exp_cw1_total', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'Cooling Water 1':<20} {'':<25} {'KM3':<8}")
    print(f"{'':<25} {'Raw Material':<20} {'SULPHURIC ACID':<25} {'MT':<8} {fmt_qty(cw1_km3)} {cw1_ref_qty:>18,.2f} {NORM_SULPHURIC_ACID_CW1:>10.7f} {cw1_sulphuric_acid:>20,.2f} {EXPECTED['cw1_sulphuric_acid']:>20,.2f} {cw1_sulphuric_acid - EXPECTED['cw1_sulphuric_acid']:>18,.2f} {calc_pct(cw1_sulphuric_acid, EXPECTED['cw1_sulphuric_acid'])}")
    print(f"{'':<25} {'':<20} {'Water':<25} {'M3':<8} {fmt_qty(cw1_km3)} {cw1_ref_qty:>18,.2f} {NORM_WATER_CW1:>10.4f} {cw1_water:>20,.2f} {EXPECTED['cw1_water']:>20,.2f} {cw1_water - EXPECTED['cw1_water']:>18,.2f} {calc_pct(cw1_water, EXPECTED['cw1_water'])}")
    print(f"{'':<25} {'Utilities':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(cw1_km3)} {cw1_ref_qty:>18,.2f} {NORM_AIR_CW1:>10.0f} {NORM_AIR_CW1:>20,.2f} {EXPECTED['cw1_air']:>20,.2f} {NORM_AIR_CW1 - EXPECTED['cw1_air']:>18,.2f} {calc_pct(NORM_AIR_CW1, EXPECTED['cw1_air'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(cw1_km3)} {cw1_ref_qty:>18,.2f} {NORM_POWER_CW1:>10.4f} {cw1_power:>20,.2f} {EXPECTED['cw1_power']:>20,.2f} {cw1_power - EXPECTED['cw1_power']:>18,.2f} {calc_pct(cw1_power, EXPECTED['cw1_power'])}")
    
    # ========================================
    # NMD - Utility Plant - Cooling Water 2
    # ========================================
    cw2_km3 = cw.get("cw2_total_km3", 0)
    cw2_water = cw2_km3 * NORM_WATER_CW2
    cw2_power = cw2_km3 * NORM_POWER_CW2
    cw2_sulphuric_acid = cw2_km3 * NORM_SULPHURIC_ACID_CW2
    # QTY (Ref) is the reference quantity - should be constant from database
    cw2_ref_qty = EXPECTED.get('exp_cw2_total', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'Cooling Water 2':<20} {'':<25} {'KM3':<8}")
    print(f"{'':<25} {'Raw Material':<20} {'SULPHURIC ACID':<25} {'MT':<8} {fmt_qty(cw2_km3)} {cw2_ref_qty:>18,.2f} {NORM_SULPHURIC_ACID_CW2:>10.7f} {cw2_sulphuric_acid:>20,.2f} {EXPECTED['cw2_sulphuric_acid']:>20,.2f} {cw2_sulphuric_acid - EXPECTED['cw2_sulphuric_acid']:>18,.2f} {calc_pct(cw2_sulphuric_acid, EXPECTED['cw2_sulphuric_acid'])}")
    print(f"{'':<25} {'':<20} {'Water':<25} {'M3':<8} {fmt_qty(cw2_km3)} {cw2_ref_qty:>18,.2f} {NORM_WATER_CW2:>10.4f} {cw2_water:>20,.2f} {EXPECTED['cw2_water']:>20,.2f} {cw2_water - EXPECTED['cw2_water']:>18,.2f} {calc_pct(cw2_water, EXPECTED['cw2_water'])}")
    print(f"{'':<25} {'Utilities':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(cw2_km3)} {cw2_ref_qty:>18,.2f} {NORM_AIR_CW2:>10.0f} {NORM_AIR_CW2:>20,.2f} {EXPECTED['cw2_air']:>20,.2f} {NORM_AIR_CW2 - EXPECTED['cw2_air']:>18,.2f} {calc_pct(NORM_AIR_CW2, EXPECTED['cw2_air'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(cw2_km3)} {cw2_ref_qty:>18,.2f} {NORM_POWER_CW2:>10.4f} {cw2_power:>20,.2f} {EXPECTED['cw2_power']:>20,.2f} {cw2_power - EXPECTED['cw2_power']:>18,.2f} {calc_pct(cw2_power, EXPECTED['cw2_power'])}")


    
    # ========================================
    # NMD - Utility Plant - D M Water
    # ========================================
    total_dm = dm.get("total_m3", 0)
    dm_water = total_dm * NORM_WATER_DM
    dm_power = total_dm * NORM_POWER_DM
    dm_air = total_dm * NORM_AIR_DM
    dm_condensate = total_dm * NORM_CONDENSATE_DM
    # DM Water - Catalyst & Chemicals
    dm_caustic = total_dm * NORM_CAUSTIC_DM
    dm_alum_sulfate = total_dm * NORM_ALUM_SULFATE_DM
    dm_sodium_sulphite = total_dm * NORM_SODIUM_SULPHITE_DM
    dm_polyelectrolyte = total_dm * NORM_POLYELECTROLYTE_DM
    dm_sodium_chloride = total_dm * NORM_SODIUM_CHLORIDE_DM
    dm_hcl = total_dm * NORM_HCL_DM
    # QTY (Ref) is the reference quantity - should be constant from database
    dm_ref_qty = EXPECTED.get('exp_dm_total', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'D M Water':<20} {'':<25} {'M3':<8}")
    print(f"{'':<25} {'Catalyst & Chemical':<20} {'CAUSTIC SODA LYE':<25} {'MT':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_CAUSTIC_DM:>10.7f} {dm_caustic:>20,.2f} {EXPECTED['dm_caustic']:>20,.2f} {dm_caustic - EXPECTED['dm_caustic']:>18,.2f} {calc_pct(dm_caustic, EXPECTED['dm_caustic'])}")
    print(f"{'':<25} {'':<20} {'CHEM ALUM.SULFATE':<25} {'KG':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_ALUM_SULFATE_DM:>10.7f} {dm_alum_sulfate:>20,.2f} {EXPECTED['dm_alum_sulfate']:>20,.2f} {dm_alum_sulfate - EXPECTED['dm_alum_sulfate']:>18,.2f} {calc_pct(dm_alum_sulfate, EXPECTED['dm_alum_sulfate'])}")
    print(f"{'':<25} {'':<20} {'CHEM SODIUM SULPHITE':<25} {'KG':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_SODIUM_SULPHITE_DM:>10.7f} {dm_sodium_sulphite:>20,.2f} {EXPECTED['dm_sodium_sulphite']:>20,.2f} {dm_sodium_sulphite - EXPECTED['dm_sodium_sulphite']:>18,.2f} {calc_pct(dm_sodium_sulphite, EXPECTED['dm_sodium_sulphite'])}")
    print(f"{'':<25} {'':<20} {'POLYELECTROLYTE':<25} {'KG':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_POLYELECTROLYTE_DM:>10.7f} {dm_polyelectrolyte:>20,.2f} {EXPECTED['dm_polyelectrolyte']:>20,.2f} {dm_polyelectrolyte - EXPECTED['dm_polyelectrolyte']:>18,.2f} {calc_pct(dm_polyelectrolyte, EXPECTED['dm_polyelectrolyte'])}")
    print(f"{'':<25} {'':<20} {'SODIUM CHLORIDE':<25} {'MT':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_SODIUM_CHLORIDE_DM:>10.7f} {dm_sodium_chloride:>20,.2f} {EXPECTED['dm_sodium_chloride']:>20,.2f} {dm_sodium_chloride - EXPECTED['dm_sodium_chloride']:>18,.2f} {calc_pct(dm_sodium_chloride, EXPECTED['dm_sodium_chloride'])}")
    print(f"{'':<25} {'Raw Material':<20} {'HYDRO CHLORIC ACID':<25} {'MT':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_HCL_DM:>10.7f} {dm_hcl:>20,.2f} {EXPECTED['dm_hcl']:>20,.2f} {dm_hcl - EXPECTED['dm_hcl']:>18,.2f} {calc_pct(dm_hcl, EXPECTED['dm_hcl'])}")
    print(f"{'':<25} {'':<20} {'Water':<25} {'M3':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_WATER_DM:>10.4f} {dm_water:>20,.2f} {EXPECTED['dm_water']:>20,.2f} {dm_water - EXPECTED['dm_water']:>18,.2f} {calc_pct(dm_water, EXPECTED['dm_water'])}")
    print(f"{'':<25} {'Utilities':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_AIR_DM:>10.4f} {dm_air:>20,.2f} {EXPECTED['dm_air']:>20,.2f} {dm_air - EXPECTED['dm_air']:>18,.2f} {calc_pct(dm_air, EXPECTED['dm_air'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_POWER_DM:>10.4f} {dm_power:>20,.2f} {EXPECTED['dm_power']:>20,.2f} {dm_power - EXPECTED['dm_power']:>18,.2f} {calc_pct(dm_power, EXPECTED['dm_power'])}")
    print(f"{'':<25} {'':<20} {'Ret steam condensate':<25} {'M3':<8} {fmt_qty(total_dm)} {dm_ref_qty:>18,.2f} {NORM_CONDENSATE_DM:>10.4f} {dm_condensate:>20,.2f} {EXPECTED['dm_condensate']:>20,.2f} {dm_condensate - EXPECTED['dm_condensate']:>18,.2f} {calc_pct(dm_condensate, EXPECTED['dm_condensate'])}")
    
    # ========================================
    # NMD - Utility Plant - Effluent Treated
    # ========================================
    effluent_power = effluent_m3 * NORM_POWER_EFFLUENT
    effluent_water = effluent_m3 * NORM_WATER_EFFLUENT
    effluent_urea = effluent_m3 * NORM_UREA_EFFLUENT
    # QTY (Ref) is the reference quantity - should be constant from database
    effluent_ref_qty = EXPECTED.get('exp_effluent', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'Effluent Treated':<20} {'':<25} {'M3':<8}")
    print(f"{'':<25} {'Raw Material':<20} {'Water':<25} {'M3':<8} {fmt_qty(effluent_m3)} {effluent_ref_qty:>18,.2f} {NORM_WATER_EFFLUENT:>10.4f} {effluent_water:>20,.2f} {EXPECTED['effluent_water']:>20,.2f} {effluent_water - EXPECTED['effluent_water']:>18,.2f} {calc_pct(effluent_water, EXPECTED['effluent_water'])}")
    print(f"{'':<25} {'':<20} {'UREA':<25} {'KG':<8} {fmt_qty(effluent_m3)} {effluent_ref_qty:>18,.2f} {NORM_UREA_EFFLUENT:>10.4f} {effluent_urea:>20,.2f} {EXPECTED['effluent_urea']:>20,.2f} {effluent_urea - EXPECTED['effluent_urea']:>18,.2f} {calc_pct(effluent_urea, EXPECTED['effluent_urea'])}")
    print(f"{'':<25} {'Utilities':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(effluent_m3)} {effluent_ref_qty:>18,.2f} {NORM_POWER_EFFLUENT:>10.4f} {effluent_power:>20,.2f} {EXPECTED['effluent_power']:>20,.2f} {effluent_power - EXPECTED['effluent_power']:>18,.2f} {calc_pct(effluent_power, EXPECTED['effluent_power'])}")
    
    # ========================================
    # NMD - Utility Plant - HP Steam PRDS
    # ========================================
    hp_bfw = hp_prds * NORM_BFW_HP_PRDS
    hp_shp = hp_prds * NORM_SHP_HP_PRDS
    exp_hp_prds = EXPECTED.get('hp_dis', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'HP Steam PRDS':<20} {'':<25} {'MT':<8}")
    hp_ref_qty = calc_ref_qty(EXPECTED['hp_bfw'], NORM_BFW_HP_PRDS) if NORM_BFW_HP_PRDS != 0 else 0
    print(f"{'':<25} {'':<20} {'Boiler Feed Water':<25} {'M3':<8} {fmt_qty(hp_prds)} {hp_ref_qty:>18,.2f} {NORM_BFW_HP_PRDS:>10.4f} {hp_bfw:>20,.2f} {EXPECTED['hp_bfw']:>20,.2f} {hp_bfw - EXPECTED['hp_bfw']:>18,.2f} {calc_pct(hp_bfw, EXPECTED['hp_bfw'])}")
    print(f"{'':<25} {'':<20} {'SHP Steam_Dis':<25} {'MT':<8} {fmt_qty(hp_prds)} {hp_ref_qty:>18,.2f} {NORM_SHP_HP_PRDS:>10.4f} {hp_shp:>20,.2f} {EXPECTED['hp_shp']:>20,.2f} {hp_shp - EXPECTED['hp_shp']:>18,.2f} {calc_pct(hp_shp, EXPECTED['hp_shp'])}")
    
    # ========================================
    # NMD - Utility Plant - HRSG1_SHP STEAM
    # ========================================
    hrsg1_bfw = hrsg1_shp * NORM_BFW_HRSG
    hrsg1_lp_credit = hrsg1_shp * NORM_LP_CREDIT_HRSG
    hrsg1_ng = ng.get("hrsg1_mmbtu", 0)
    hrsg1_air_calc = air.get('hrsg1_nm3', 0)
    exp_hrsg1 = EXPECTED.get('shp_hrsg1', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'HRSG1_SHP STEAM':<20} {'':<25} {'MT':<8}")
    hrsg1_ref_qty = calc_ref_qty(EXPECTED['hrsg1_ng'], NORM_NG_HRSG1) if NORM_NG_HRSG1 != 0 else 0
    print(f"{'':<25} {'':<20} {'NATURAL GAS':<25} {'MMBTU':<8} {fmt_qty(hrsg1_shp)} {hrsg1_ref_qty:>18,.2f} {NORM_NG_HRSG1:>10.4f} {hrsg1_ng:>20,.2f} {EXPECTED['hrsg1_ng']:>20,.2f} {hrsg1_ng - EXPECTED['hrsg1_ng']:>18,.2f} {calc_pct(hrsg1_ng, EXPECTED['hrsg1_ng'])}")
    print(f"{'':<25} {'':<20} {'Boiler Feed Water':<25} {'M3':<8} {fmt_qty(hrsg1_shp)} {hrsg1_ref_qty:>18,.2f} {NORM_BFW_HRSG:>10.4f} {hrsg1_bfw:>20,.2f} {EXPECTED['hrsg1_bfw']:>20,.2f} {hrsg1_bfw - EXPECTED['hrsg1_bfw']:>18,.2f} {calc_pct(hrsg1_bfw, EXPECTED['hrsg1_bfw'])}")
    print(f"{'':<25} {'':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(hrsg1_shp)} {hrsg1_ref_qty:>18,.2f} {NORM_AIR_HRSG:>10.0f} {hrsg1_air_calc:>20,.2f} {EXPECTED['hrsg1_air']:>20,.2f} {hrsg1_air_calc - EXPECTED['hrsg1_air']:>18,.2f} {calc_pct(hrsg1_air_calc, EXPECTED['hrsg1_air'])}")
    print(f"{'':<25} {'':<20} {'LP Steam_Dis':<25} {'MT':<8} {fmt_qty(hrsg1_shp)} {hrsg1_ref_qty:>18,.2f} {NORM_LP_CREDIT_HRSG:>10.4f} {hrsg1_lp_credit:>20,.2f} {EXPECTED['hrsg1_lp']:>20,.2f} {hrsg1_lp_credit - EXPECTED['hrsg1_lp']:>18,.2f} {calc_pct(hrsg1_lp_credit, EXPECTED['hrsg1_lp'])}")
    
    # ========================================
    # NMD - Utility Plant - HRSG2_SHP STEAM
    # ========================================
    hrsg2_bfw = hrsg2_shp * NORM_BFW_HRSG
    hrsg2_lp_credit = hrsg2_shp * NORM_LP_CREDIT_HRSG
    hrsg2_ng = ng.get("hrsg2_mmbtu", 0)
    hrsg2_air_calc = air.get('hrsg2_nm3', 0)
    hrsg2_trisodium = hrsg2_shp * NORM_TRISODIUM_PHOSPHATE_HRSG
    hrsg2_furnace_oil = hrsg2_shp * NORM_FURNACE_OIL_HRSG
    hrsg2_water = hrsg2_shp * NORM_WATER_HRSG
    exp_hrsg2 = EXPECTED.get('shp_hrsg2', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'HRSG2_SHP STEAM':<20} {'':<25} {'MT':<8}")
    hrsg2_ref_qty = calc_ref_qty(EXPECTED['hrsg2_ng'], NORM_NG_HRSG2) if NORM_NG_HRSG2 != 0 else 0
    # Note: Using calculated NG norm from heat rate lookup (2.7759690 for 700 BTU/lb)
    print(f"{'':<25} {'Catalyst & Chemical':<20} {'CHEM TRISODIUM PHOSPHATE':<25} {'KG':<8} {fmt_qty(hrsg2_shp)} {hrsg2_ref_qty:>18,.2f} {NORM_TRISODIUM_PHOSPHATE_HRSG:>10.4f} {hrsg2_trisodium:>20,.2f} {EXPECTED['hrsg2_trisodium']:>20,.2f} {hrsg2_trisodium - EXPECTED['hrsg2_trisodium']:>18,.2f} {calc_pct(hrsg2_trisodium, EXPECTED['hrsg2_trisodium'])}")
    print(f"{'':<25} {'Raw Material':<20} {'FURNACE OIL':<25} {'MMBTU':<8} {fmt_qty(hrsg2_shp)} {hrsg2_ref_qty:>18,.2f} {NORM_FURNACE_OIL_HRSG:>10.7f} {hrsg2_furnace_oil:>20,.2f} {EXPECTED['hrsg2_furnace_oil']:>20,.2f} {hrsg2_furnace_oil - EXPECTED['hrsg2_furnace_oil']:>18,.2f} {calc_pct(hrsg2_furnace_oil, EXPECTED['hrsg2_furnace_oil'])}")
    print(f"{'':<25} {'':<20} {'NATURAL GAS':<25} {'MMBTU':<8} {fmt_qty(hrsg2_shp)} {hrsg2_ref_qty:>18,.2f} {NORM_NG_HRSG2:>10.4f} {hrsg2_ng:>20,.2f} {EXPECTED['hrsg2_ng']:>20,.2f} {hrsg2_ng - EXPECTED['hrsg2_ng']:>18,.2f} {calc_pct(hrsg2_ng, EXPECTED['hrsg2_ng'])}")
    print(f"{'':<25} {'':<20} {'Water':<25} {'M3':<8} {fmt_qty(hrsg2_shp)} {hrsg2_ref_qty:>18,.2f} {NORM_WATER_HRSG:>10.7f} {hrsg2_water:>20,.2f} {EXPECTED['hrsg2_water']:>20,.2f} {hrsg2_water - EXPECTED['hrsg2_water']:>18,.2f} {calc_pct(hrsg2_water, EXPECTED['hrsg2_water'])}")
    print(f"{'':<25} {'Utilities':<20} {'Boiler Feed Water':<25} {'M3':<8} {fmt_qty(hrsg2_shp)} {hrsg2_ref_qty:>18,.2f} {NORM_BFW_HRSG:>10.4f} {hrsg2_bfw:>20,.2f} {EXPECTED['hrsg2_bfw']:>20,.2f} {hrsg2_bfw - EXPECTED['hrsg2_bfw']:>18,.2f} {calc_pct(hrsg2_bfw, EXPECTED['hrsg2_bfw'])}")
    print(f"{'':<25} {'':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(hrsg2_shp)} {hrsg2_ref_qty:>18,.2f} {NORM_AIR_HRSG:>10.0f} {hrsg2_air_calc:>20,.2f} {EXPECTED['hrsg2_air']:>20,.2f} {hrsg2_air_calc - EXPECTED['hrsg2_air']:>18,.2f} {calc_pct(hrsg2_air_calc, EXPECTED['hrsg2_air'])}")
    print(f"{'':<25} {'':<20} {'LP Steam_Dis':<25} {'MT':<8} {fmt_qty(hrsg2_shp)} {hrsg2_ref_qty:>18,.2f} {NORM_LP_CREDIT_HRSG:>10.4f} {hrsg2_lp_credit:>20,.2f} {EXPECTED['hrsg2_lp']:>20,.2f} {hrsg2_lp_credit - EXPECTED['hrsg2_lp']:>18,.2f} {calc_pct(hrsg2_lp_credit, EXPECTED['hrsg2_lp'])}")
    
    # ========================================
    # NMD - Utility Plant - HRSG3_SHP STEAM
    # ========================================
    hrsg3_bfw = hrsg3_shp * NORM_BFW_HRSG
    hrsg3_lp_credit = hrsg3_shp * NORM_LP_CREDIT_HRSG
    hrsg3_ng = ng.get("hrsg3_mmbtu", 0)
    hrsg3_air_calc = air.get('hrsg3_nm3', 0)
    hrsg3_trisodium = hrsg3_shp * NORM_TRISODIUM_PHOSPHATE_HRSG
    hrsg3_furnace_oil = hrsg3_shp * NORM_FURNACE_OIL_HRSG
    hrsg3_water = hrsg3_shp * NORM_WATER_HRSG
    exp_hrsg3 = EXPECTED.get('shp_hrsg3', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'HRSG3_SHP STEAM':<20} {'':<25} {'MT':<8}")
    hrsg3_ref_qty = calc_ref_qty(EXPECTED['hrsg3_ng'], NORM_NG_HRSG3) if NORM_NG_HRSG3 != 0 else 0
    # Note: Using calculated NG norm from heat rate lookup (2.7759690 for 700 BTU/lb)
    print(f"{'':<25} {'Catalyst & Chemical':<20} {'CHEM TRISODIUM PHOSPHATE':<25} {'KG':<8} {fmt_qty(hrsg3_shp)} {hrsg3_ref_qty:>18,.2f} {NORM_TRISODIUM_PHOSPHATE_HRSG:>10.4f} {hrsg3_trisodium:>20,.2f} {EXPECTED['hrsg3_trisodium']:>20,.2f} {hrsg3_trisodium - EXPECTED['hrsg3_trisodium']:>18,.2f} {calc_pct(hrsg3_trisodium, EXPECTED['hrsg3_trisodium'])}")
    print(f"{'':<25} {'Raw Material':<20} {'FURNACE OIL':<25} {'MMBTU':<8} {fmt_qty(hrsg3_shp)} {hrsg3_ref_qty:>18,.2f} {NORM_FURNACE_OIL_HRSG:>10.7f} {hrsg3_furnace_oil:>20,.2f} {EXPECTED['hrsg3_furnace_oil']:>20,.2f} {hrsg3_furnace_oil - EXPECTED['hrsg3_furnace_oil']:>18,.2f} {calc_pct(hrsg3_furnace_oil, EXPECTED['hrsg3_furnace_oil'])}")
    print(f"{'':<25} {'':<20} {'NATURAL GAS':<25} {'MMBTU':<8} {fmt_qty(hrsg3_shp)} {hrsg3_ref_qty:>18,.2f} {NORM_NG_HRSG3:>10.4f} {hrsg3_ng:>20,.2f} {EXPECTED['hrsg3_ng']:>20,.2f} {hrsg3_ng - EXPECTED['hrsg3_ng']:>18,.2f} {calc_pct(hrsg3_ng, EXPECTED['hrsg3_ng'])}")
    print(f"{'':<25} {'':<20} {'Water':<25} {'M3':<8} {fmt_qty(hrsg3_shp)} {hrsg3_ref_qty:>18,.2f} {NORM_WATER_HRSG:>10.7f} {hrsg3_water:>20,.2f} {EXPECTED['hrsg3_water']:>20,.2f} {hrsg3_water - EXPECTED['hrsg3_water']:>18,.2f} {calc_pct(hrsg3_water, EXPECTED['hrsg3_water'])}")
    print(f"{'':<25} {'Utilities':<20} {'Boiler Feed Water':<25} {'M3':<8} {fmt_qty(hrsg3_shp)} {hrsg3_ref_qty:>18,.2f} {NORM_BFW_HRSG:>10.4f} {hrsg3_bfw:>20,.2f} {EXPECTED['hrsg3_bfw']:>20,.2f} {hrsg3_bfw - EXPECTED['hrsg3_bfw']:>18,.2f} {calc_pct(hrsg3_bfw, EXPECTED['hrsg3_bfw'])}")
    print(f"{'':<25} {'':<20} {'COMPRESSED AIR':<25} {'NM3':<8} {fmt_qty(hrsg3_shp)} {hrsg3_ref_qty:>18,.2f} {NORM_AIR_HRSG:>10.0f} {hrsg3_air_calc:>20,.2f} {EXPECTED['hrsg3_air']:>20,.2f} {hrsg3_air_calc - EXPECTED['hrsg3_air']:>18,.2f} {calc_pct(hrsg3_air_calc, EXPECTED['hrsg3_air'])}")
    print(f"{'':<25} {'':<20} {'LP Steam_Dis':<25} {'MT':<8} {fmt_qty(hrsg3_shp)} {hrsg3_ref_qty:>18,.2f} {NORM_LP_CREDIT_HRSG:>10.4f} {hrsg3_lp_credit:>20,.2f} {EXPECTED['hrsg3_lp']:>20,.2f} {hrsg3_lp_credit - EXPECTED['hrsg3_lp']:>18,.2f} {calc_pct(hrsg3_lp_credit, EXPECTED['hrsg3_lp'])}")


    
    # ========================================
    # NMD - Utility Plant - LP Steam PRDS
    # ========================================
    lp_bfw = lp_prds * NORM_BFW_LP_PRDS
    lp_mp = lp_prds * NORM_MP_LP_PRDS
    exp_lp_prds = EXPECTED.get('lp_prds_dis', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'LP Steam PRDS':<20} {'':<25} {'MT':<8}")
    lp_ref_qty = calc_ref_qty(EXPECTED['lp_bfw'], NORM_BFW_LP_PRDS) if NORM_BFW_LP_PRDS != 0 else 0
    print(f"{'':<25} {'':<20} {'Boiler Feed Water':<25} {'M3':<8} {fmt_qty(lp_prds)} {lp_ref_qty:>18,.2f} {NORM_BFW_LP_PRDS:>10.4f} {lp_bfw:>20,.2f} {EXPECTED['lp_bfw']:>20,.2f} {lp_bfw - EXPECTED['lp_bfw']:>18,.2f} {calc_pct(lp_bfw, EXPECTED['lp_bfw'])}")
    print(f"{'':<25} {'':<20} {'MP Steam_Dis':<25} {'MT':<8} {fmt_qty(lp_prds)} {lp_ref_qty:>18,.2f} {NORM_MP_LP_PRDS:>10.4f} {lp_mp:>20,.2f} {EXPECTED['lp_mp']:>20,.2f} {lp_mp - EXPECTED['lp_mp']:>18,.2f} {calc_pct(lp_mp, EXPECTED['lp_mp'])}")
    
    # ========================================
    # NMD - Utility Plant - MP Steam PRDS SHP
    # ========================================
    mp_bfw = mp_prds * NORM_BFW_MP_PRDS
    mp_shp = mp_prds * NORM_SHP_MP_PRDS
    exp_mp_prds = EXPECTED.get('mp_prds_dis', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'MP Steam PRDS SHP':<20} {'':<25} {'MT':<8}")
    mp_ref_qty = calc_ref_qty(EXPECTED['mp_bfw'], NORM_BFW_MP_PRDS) if NORM_BFW_MP_PRDS != 0 else 0
    print(f"{'':<25} {'':<20} {'Boiler Feed Water':<25} {'M3':<8} {fmt_qty(mp_prds)} {mp_ref_qty:>18,.2f} {NORM_BFW_MP_PRDS:>10.4f} {mp_bfw:>20,.2f} {EXPECTED['mp_bfw']:>20,.2f} {mp_bfw - EXPECTED['mp_bfw']:>18,.2f} {calc_pct(mp_bfw, EXPECTED['mp_bfw'])}")
    print(f"{'':<25} {'':<20} {'SHP Steam_Dis':<25} {'MT':<8} {fmt_qty(mp_prds)} {mp_ref_qty:>18,.2f} {NORM_SHP_MP_PRDS:>10.4f} {mp_shp:>20,.2f} {EXPECTED['mp_shp']:>20,.2f} {mp_shp - EXPECTED['mp_shp']:>18,.2f} {calc_pct(mp_shp, EXPECTED['mp_shp'])}")
    
    # ========================================
    # NMD - Utility Plant - Oxygen
    # ========================================
    oxygen_cw = oxygen_mt * NORM_CW2_OXYGEN
    oxygen_power = oxygen_mt * NORM_POWER_OXYGEN
    nitrogen_byproduct = oxygen_mt * 2448.4
    nitrogen_neg = -nitrogen_byproduct
    exp_oxygen = EXPECTED.get('exp_oxygen', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'Oxygen':<20} {'':<25} {'MT':<8}")
    oxygen_ref_qty = calc_ref_qty(EXPECTED['oxygen_nitrogen'], 2448.4) if 2448.4 != 0 else 0
    print(f"{'':<25} {'':<20} {'Nitrogen Gas':<25} {'NM3':<8} {fmt_qty(oxygen_mt)} {oxygen_ref_qty:>18,.2f} {'2448.4000':>10} {nitrogen_neg:>20,.2f} {EXPECTED['oxygen_nitrogen']:>20,.2f} {nitrogen_neg - EXPECTED['oxygen_nitrogen']:>18,.2f} {calc_pct(nitrogen_neg, EXPECTED['oxygen_nitrogen'])}")
    print(f"{'':<25} {'':<20} {'Cooling Water 2':<25} {'KM3':<8} {fmt_qty(oxygen_mt)} {oxygen_ref_qty:>18,.2f} {NORM_CW2_OXYGEN:>10.4f} {oxygen_cw:>20,.2f} {EXPECTED['oxygen_cw']:>20,.2f} {oxygen_cw - EXPECTED['oxygen_cw']:>18,.2f} {calc_pct(oxygen_cw, EXPECTED['oxygen_cw'])}")
    print(f"{'':<25} {'':<20} {'Power_Dis':<25} {'KWH':<8} {fmt_qty(oxygen_mt)} {oxygen_ref_qty:>18,.2f} {NORM_POWER_OXYGEN:>10.4f} {oxygen_power:>20,.2f} {EXPECTED['oxygen_power']:>20,.2f} {oxygen_power - EXPECTED['oxygen_power']:>18,.2f} {calc_pct(oxygen_power, EXPECTED['oxygen_power'])}")
    
    # ========================================
    # NMD - Utility Plant - STG1_LP STEAM
    # ========================================
    stg_lp_shp = stg_lp * NORM_SHP_STG_LP
    exp_stg_lp = EXPECTED.get('lp_stg_dis', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'STG1_LP STEAM':<20} {'':<25} {'MT':<8}")
    stg_lp_ref_qty = calc_ref_qty(EXPECTED['stg_lp_shp'], NORM_SHP_STG_LP) if NORM_SHP_STG_LP != 0 else 0
    print(f"{'':<25} {'':<20} {'SHP Steam_Dis':<25} {'MT':<8} {fmt_qty(stg_lp)} {stg_lp_ref_qty:>18,.2f} {NORM_SHP_STG_LP:>10.4f} {stg_lp_shp:>20,.2f} {EXPECTED['stg_lp_shp']:>20,.2f} {stg_lp_shp - EXPECTED['stg_lp_shp']:>18,.2f} {calc_pct(stg_lp_shp, EXPECTED['stg_lp_shp'])}")
    
    # ========================================
    # NMD - Utility Plant - STG1_MP STEAM
    # ========================================
    stg_mp_shp = stg_mp * NORM_SHP_STG_MP
    exp_stg_mp = EXPECTED.get('mp_stg_dis', 0)
    print(f"\n{'NMD - Utility Plant':<25} {'STG1_MP STEAM':<20} {'':<25} {'MT':<8}")
    stg_mp_ref_qty = calc_ref_qty(EXPECTED['stg_mp_shp'], NORM_SHP_STG_MP) if NORM_SHP_STG_MP != 0 else 0
    print(f"{'':<25} {'':<20} {'SHP Steam_Dis':<25} {'MT':<8} {fmt_qty(stg_mp)} {stg_mp_ref_qty:>18,.2f} {NORM_SHP_STG_MP:>10.4f} {stg_mp_shp:>20,.2f} {EXPECTED['stg_mp_shp']:>20,.2f} {stg_mp_shp - EXPECTED['stg_mp_shp']:>18,.2f} {calc_pct(stg_mp_shp, EXPECTED['stg_mp_shp'])}")
    
    # ========================================
    # NMD - Utility/Power Dist - HP Steam_Dis
    # ========================================
    print(f"\n{'NMD - Utility/Power Dist':<25} {'HP Steam_Dis':<20} {'':<25} {'MT':<8}")
    hp_dis_ref_qty = calc_ref_qty(EXPECTED['hp_dis'], 1.0)
    print(f"{'':<25} {'':<20} {'HP Steam PRDS':<25} {'MT':<8} {fmt_qty(hp_prds)} {hp_dis_ref_qty:>18,.2f} {'1.0000':>10} {hp_prds:>20,.2f} {EXPECTED['hp_dis']:>20,.2f} {hp_prds - EXPECTED['hp_dis']:>18,.2f} {calc_pct(hp_prds, EXPECTED['hp_dis'])}")
    
    # ========================================
    # NMD - Utility/Power Dist - LP Steam_Dis
    # ========================================
    total_lp = lp_prds + stg_lp
    lp_from_prds_ratio = lp_prds / total_lp if total_lp > 0 else 0
    lp_from_stg_ratio = stg_lp / total_lp if total_lp > 0 else 0
    print(f"\n{'NMD - Utility/Power Dist':<25} {'LP Steam_Dis':<20} {'':<25} {'MT':<8}")
    lp_dis_ref_qty = calc_ref_qty(EXPECTED['lp_prds_dis'], lp_from_prds_ratio) if lp_from_prds_ratio > 0 else 0
    print(f"{'':<25} {'':<20} {'LP Steam PRDS':<25} {'MT':<8} {fmt_qty(total_lp)} {lp_dis_ref_qty:>18,.2f} {lp_from_prds_ratio:>10.4f} {lp_prds:>20,.2f} {EXPECTED['lp_prds_dis']:>20,.2f} {lp_prds - EXPECTED['lp_prds_dis']:>18,.2f} {calc_pct(lp_prds, EXPECTED['lp_prds_dis'])}")
    print(f"{'':<25} {'':<20} {'STG1_LP STEAM':<25} {'MT':<8} {fmt_qty(total_lp)} {lp_dis_ref_qty:>18,.2f} {lp_from_stg_ratio:>10.4f} {stg_lp:>20,.2f} {EXPECTED['lp_stg_dis']:>20,.2f} {stg_lp - EXPECTED['lp_stg_dis']:>18,.2f} {calc_pct(stg_lp, EXPECTED['lp_stg_dis'])}")

    
    # ========================================
    # NMD - Utility/Power Dist - MP Steam_Dis
    # ========================================
    total_mp = mp_prds + stg_mp
    mp_from_prds_ratio = mp_prds / total_mp if total_mp > 0 else 0
    mp_from_stg_ratio = stg_mp / total_mp if total_mp > 0 else 0
    print(f"\n{'NMD - Utility/Power Dist':<25} {'MP Steam_Dis':<20} {'':<25} {'MT':<8}")
    mp_dis_ref_qty = calc_ref_qty(EXPECTED['mp_prds_dis'], mp_from_prds_ratio) if mp_from_prds_ratio > 0 else 0
    print(f"{'':<25} {'':<20} {'MP Steam PRDS SHP':<25} {'MT':<8} {fmt_qty(total_mp)} {mp_dis_ref_qty:>18,.2f} {mp_from_prds_ratio:>10.4f} {mp_prds:>20,.2f} {EXPECTED['mp_prds_dis']:>20,.2f} {mp_prds - EXPECTED['mp_prds_dis']:>18,.2f} {calc_pct(mp_prds, EXPECTED['mp_prds_dis'])}")
    print(f"{'':<25} {'':<20} {'STG1_MP STEAM':<25} {'MT':<8} {fmt_qty(total_mp)} {mp_dis_ref_qty:>18,.2f} {mp_from_stg_ratio:>10.4f} {stg_mp:>20,.2f} {EXPECTED['mp_stg_dis']:>20,.2f} {stg_mp - EXPECTED['mp_stg_dis']:>18,.2f} {calc_pct(stg_mp, EXPECTED['mp_stg_dis'])}")
    
    # ========================================
    # NMD - Utility/Power Dist - Power_Dis
    # ========================================
    total_net_power = gt1_net + gt2_net + gt3_net + stg_net
    total_net_kwh = total_net_power * 1000
    gt1_ratio = gt1_net / total_net_power if total_net_power > 0 else 0
    gt2_ratio = gt2_net / total_net_power if total_net_power > 0 else 0
    gt3_ratio = gt3_net / total_net_power if total_net_power > 0 else 0
    stg_ratio = stg_net / total_net_power if total_net_power > 0 else 0
    pp1_power_kwh = gt1_net * 1000
    pp2_power_kwh = gt2_net * 1000
    pp3_power_kwh = gt3_net * 1000
    stg_power_kwh = stg_net * 1000
    print(f"\n{'NMD - Utility/Power Dist':<25} {'Power_Dis':<20} {'':<25} {'KWH':<8}")
    power_dis_ref_qty = calc_ref_qty(EXPECTED['power_pp1'], gt1_ratio) if gt1_ratio > 0 else 0
    print(f"{'':<25} {'':<20} {'POWERGEN (PP1)':<25} {'KWH':<8} {fmt_qty(total_net_kwh)} {power_dis_ref_qty:>18,.2f} {gt1_ratio:>10.4f} {pp1_power_kwh:>20,.2f} {EXPECTED['power_pp1']:>20,.2f} {pp1_power_kwh - EXPECTED['power_pp1']:>18,.2f} {calc_pct(pp1_power_kwh, EXPECTED['power_pp1'])}")
    print(f"{'':<25} {'':<20} {'POWERGEN (PP2)':<25} {'KWH':<8} {fmt_qty(total_net_kwh)} {power_dis_ref_qty:>18,.2f} {gt2_ratio:>10.4f} {pp2_power_kwh:>20,.2f} {EXPECTED['power_pp2']:>20,.2f} {pp2_power_kwh - EXPECTED['power_pp2']:>18,.2f} {calc_pct(pp2_power_kwh, EXPECTED['power_pp2'])}")
    print(f"{'':<25} {'':<20} {'POWERGEN (PP3)':<25} {'KWH':<8} {fmt_qty(total_net_kwh)} {power_dis_ref_qty:>18,.2f} {gt3_ratio:>10.4f} {pp3_power_kwh:>20,.2f} {EXPECTED['power_pp3']:>20,.2f} {pp3_power_kwh - EXPECTED['power_pp3']:>18,.2f} {calc_pct(pp3_power_kwh, EXPECTED['power_pp3'])}")
    print(f"{'':<25} {'':<20} {'POWERGEN (STG)':<25} {'KWH':<8} {fmt_qty(total_net_kwh)} {power_dis_ref_qty:>18,.2f} {stg_ratio:>10.4f} {stg_power_kwh:>20,.2f} {EXPECTED['power_stg']:>20,.2f} {stg_power_kwh - EXPECTED['power_stg']:>18,.2f} {calc_pct(stg_power_kwh, EXPECTED['power_stg'])}")
    
    # ========================================
    # NMD - Utility/Power Dist - SHP Steam_Dis
    # ========================================
    total_shp = hrsg1_shp + hrsg2_shp + hrsg3_shp
    hrsg1_ratio = hrsg1_shp / total_shp if total_shp > 0 else 0
    hrsg2_ratio = hrsg2_shp / total_shp if total_shp > 0 else 0
    hrsg3_ratio = hrsg3_shp / total_shp if total_shp > 0 else 0
    exp_total_shp = EXPECTED['shp_hrsg1'] + EXPECTED['shp_hrsg2'] + EXPECTED['shp_hrsg3']
    print(f"\n{'NMD - Utility/Power Dist':<25} {'SHP Steam_Dis':<20} {'':<25} {'MT':<8}")
    shp_dis_ref_qty = calc_ref_qty(EXPECTED['shp_hrsg1'], hrsg1_ratio) if hrsg1_ratio > 0 else 0
    print(f"{'':<25} {'':<20} {'HRSG1_SHP STEAM':<25} {'MT':<8} {fmt_qty(total_shp)} {shp_dis_ref_qty:>18,.2f} {hrsg1_ratio:>10.4f} {hrsg1_shp:>20,.2f} {EXPECTED['shp_hrsg1']:>20,.2f} {hrsg1_shp - EXPECTED['shp_hrsg1']:>18,.2f} {calc_pct(hrsg1_shp, EXPECTED['shp_hrsg1'])}")
    print(f"{'':<25} {'':<20} {'HRSG2_SHP STEAM':<25} {'MT':<8} {fmt_qty(total_shp)} {shp_dis_ref_qty:>18,.2f} {hrsg2_ratio:>10.4f} {hrsg2_shp:>20,.2f} {EXPECTED['shp_hrsg2']:>20,.2f} {hrsg2_shp - EXPECTED['shp_hrsg2']:>18,.2f} {calc_pct(hrsg2_shp, EXPECTED['shp_hrsg2'])}")
    print(f"{'':<25} {'':<20} {'HRSG3_SHP STEAM':<25} {'MT':<8} {fmt_qty(total_shp)} {shp_dis_ref_qty:>18,.2f} {hrsg3_ratio:>10.4f} {hrsg3_shp:>20,.2f} {EXPECTED['shp_hrsg3']:>20,.2f} {hrsg3_shp - EXPECTED['shp_hrsg3']:>18,.2f} {calc_pct(hrsg3_shp, EXPECTED['shp_hrsg3'])}")
    
    print("\n" + "="*270)
    print(" " * 100 + "END OF NMD BUDGET FORMAT - COMPARISON COMPLETE")
    print("="*270)


def print_utility_output_summary(
    utilities: dict,
    power_dispatch: list = None,
    steam_balance: dict = None,
    stg_extraction: dict = None,
):
    """
    Print a comprehensive utility output summary in table format.
    """
    # First print the NMD budget format
    print_nmd_budget_format(utilities, power_dispatch, steam_balance, stg_extraction)
    
    ng = utilities.get("natural_gas", {})
    cw = utilities.get("cooling_water", {})
    air = utilities.get("compressed_air", {})
    bfw = utilities.get("bfw", {})
    dm = utilities.get("dm_water", {})
    rw = utilities.get("raw_water", {})
    up = utilities.get("utility_power", {})
    lp_steam = utilities.get("lp_steam", {})
    
    # Extract power dispatch values
    gt1_gross = gt2_gross = gt3_gross = stg_gross = 0.0
    gt1_aux = gt2_aux = gt3_aux = stg_aux = 0.0
    gt1_net = gt2_net = gt3_net = stg_net = 0.0
    
    if power_dispatch:
        for asset in power_dispatch:
            name = str(asset.get("AssetName", "")).upper()
            if "GT1" in name or "PLANT-1" in name:
                gt1_gross = asset.get("GrossMWh", 0)
                gt1_aux = asset.get("AuxMWh", 0)
                gt1_net = asset.get("NetMWh", 0)
            elif "GT2" in name or "PLANT-2" in name:
                gt2_gross = asset.get("GrossMWh", 0)
                gt2_aux = asset.get("AuxMWh", 0)
                gt2_net = asset.get("NetMWh", 0)
            elif "GT3" in name or "PLANT-3" in name:
                gt3_gross = asset.get("GrossMWh", 0)
                gt3_aux = asset.get("AuxMWh", 0)
                gt3_net = asset.get("NetMWh", 0)
            elif "STG" in name or "STEAM TURBINE" in name:
                stg_gross = asset.get("GrossMWh", 0)
                stg_aux = asset.get("AuxMWh", 0)
                stg_net = asset.get("NetMWh", 0)
    
    # Extract steam balance values
    hp_prds = mp_prds = lp_prds = 0.0
    stg_lp = stg_mp = 0.0
    
    if steam_balance:
        lp_bal = steam_balance.get("lp_balance", {})
        mp_bal = steam_balance.get("mp_balance", {})
        hp_bal = steam_balance.get("hp_balance", {})
        lp_prds = lp_bal.get("lp_from_prds", 0)
        mp_prds = mp_bal.get("mp_from_prds", 0)
        hp_prds = hp_bal.get("hp_total", 0)
        stg_lp = lp_bal.get("lp_from_stg", 0)
        stg_mp = mp_bal.get("mp_from_stg", 0)
    
    print("\n" + "="*130)
    print(" " * 45 + "FINAL UTILITY OUTPUT TABLE")
    print("="*130)
    
    # =============================================
    # SECTION 1: POWER GENERATION (NMD - Power Plants)
    # =============================================
    print("\n" + "-"*130)
    print("SECTION 1: POWER GENERATION")
    print("-"*130)
    print(f"{'Generating Plant':<25} {'Utility':<15} {'Gross MWH':>12} {'Aux MWH':>12} {'Net MWH':>12} {'NG MMBTU':>15} {'CW KM3':>12} {'Air NM3':>15}")
    print("-"*130)
    
    # Power Plant 1 (GT1) - CORRECT MAPPING
    print(f"{'NMD - Power Plant 1':<25} {'POWERGEN':<15} {gt1_gross:>12,.2f} {gt1_aux:>12,.2f} {gt1_net:>12,.2f} {ng.get('gt1_mmbtu', 0):>15,.2f} {cw.get('cw2_gt1_km3', 0):>12,.2f} {air.get('gt1_nm3', 0):>15,.2f}")
    
    # Power Plant 2 (GT2)
    print(f"{'NMD - Power Plant 2':<25} {'POWERGEN':<15} {gt2_gross:>12,.2f} {gt2_aux:>12,.2f} {gt2_net:>12,.2f} {ng.get('gt2_mmbtu', 0):>15,.2f} {cw.get('cw2_gt2_km3', 0):>12,.2f} {air.get('gt2_nm3', 0):>15,.2f}")
    
    # Power Plant 3 (GT3) - CORRECT MAPPING
    print(f"{'NMD - Power Plant 3':<25} {'POWERGEN':<15} {gt3_gross:>12,.2f} {gt3_aux:>12,.2f} {gt3_net:>12,.2f} {ng.get('gt3_mmbtu', 0):>15,.2f} {cw.get('cw2_gt3_km3', 0):>12,.2f} {air.get('gt3_nm3', 0):>15,.2f}")
    
    # STG Power Plant
    print(f"{'NMD - STG Power Plant':<25} {'POWERGEN':<15} {stg_gross:>12,.2f} {stg_aux:>12,.2f} {stg_net:>12,.2f} {'-':>15} {cw.get('cw2_stg_km3', 0):>12,.2f} {air.get('stg_nm3', 0):>15,.2f}")
    
    print("-"*130)
    total_gross = gt1_gross + gt2_gross + gt3_gross + stg_gross
    total_aux = gt1_aux + gt2_aux + gt3_aux + stg_aux
    total_net = gt1_net + gt2_net + gt3_net + stg_net
    print(f"{'TOTAL':<25} {'':<15} {total_gross:>12,.2f} {total_aux:>12,.2f} {total_net:>12,.2f} {ng.get('total_mmbtu', 0):>15,.2f} {cw.get('total_km3', 0):>12,.2f} {air.get('total_nm3', 0):>15,.2f}")
    
    # =============================================
    # SECTION 2: HRSG & STEAM GENERATION
    # =============================================
    print("\n" + "-"*130)
    print("SECTION 2: HRSG & STEAM GENERATION")
    print("-"*130)
    print(f"{'Generating Plant':<25} {'Utility':<20} {'Steam MT':>12} {'NG MMBTU':>15} {'BFW M3':>12} {'Air NM3':>15}")
    print("-"*130)
    
    # HRSG1
    hrsg1_shp = utilities.get("shp_from_hrsg1", 0)
    print(f"{'NMD - Utility Plant':<25} {'HRSG1_SHP STEAM':<20} {hrsg1_shp:>12,.2f} {ng.get('hrsg1_mmbtu', 0):>15,.2f} {bfw.get('hrsg1_m3', 0):>12,.2f} {air.get('hrsg1_nm3', 0):>15,.2f}")
    
    # HRSG2
    hrsg2_shp = utilities.get("shp_from_hrsg2", 0)
    print(f"{'NMD - Utility Plant':<25} {'HRSG2_SHP STEAM':<20} {hrsg2_shp:>12,.2f} {ng.get('hrsg2_mmbtu', 0):>15,.2f} {bfw.get('hrsg2_m3', 0):>12,.2f} {air.get('hrsg2_nm3', 0):>15,.2f}")
    
    # HRSG3
    hrsg3_shp = utilities.get("shp_from_hrsg3", 0)
    print(f"{'NMD - Utility Plant':<25} {'HRSG3_SHP STEAM':<20} {hrsg3_shp:>12,.2f} {ng.get('hrsg3_mmbtu', 0):>15,.2f} {bfw.get('hrsg3_m3', 0):>12,.2f} {air.get('hrsg3_nm3', 0):>15,.2f}")
    
    print("-"*130)
    
    # =============================================
    # SECTION 3: PRDS STEAM
    # =============================================
    print("\n" + "-"*130)
    print("SECTION 3: PRDS STEAM")
    print("-"*130)
    print(f"{'Generating Plant':<25} {'Utility':<20} {'Steam MT':>12} {'BFW M3':>12}")
    print("-"*130)
    
    print(f"{'NMD - Utility Plant':<25} {'HP Steam PRDS':<20} {hp_prds:>12,.2f} {bfw.get('hp_prds_m3', 0):>12,.2f}")
    print(f"{'NMD - Utility Plant':<25} {'MP Steam PRDS SHP':<20} {mp_prds:>12,.2f} {bfw.get('mp_prds_m3', 0):>12,.2f}")
    print(f"{'NMD - Utility Plant':<25} {'LP Steam PRDS':<20} {lp_prds:>12,.2f} {bfw.get('lp_prds_m3', 0):>12,.2f}")
    
    print("-"*130)
    
    # =============================================
    # SECTION 4: STG EXTRACTION
    # =============================================
    print("\n" + "-"*130)
    print("SECTION 4: STG EXTRACTION")
    print("-"*130)
    print(f"{'Generating Plant':<25} {'Utility':<20} {'Steam MT':>12}")
    print("-"*130)
    
    print(f"{'NMD - Utility Plant':<25} {'STG1_LP STEAM':<20} {stg_lp:>12,.2f}")
    print(f"{'NMD - Utility Plant':<25} {'STG1_MP STEAM':<20} {stg_mp:>12,.2f}")
    
    print("-"*130)
    
    # =============================================
    # SECTION 5: UTILITY PLANTS
    # =============================================
    print("\n" + "-"*130)
    print("SECTION 5: UTILITY PLANTS")
    print("-"*130)
    print(f"{'Generating Plant':<25} {'Utility':<20} {'Quantity':>15} {'UOM':<10} {'Power KWH':>15} {'Raw Water M3':>15}")
    print("-"*130)
    
    print(f"{'NMD - Utility Plant':<25} {'Boiler Feed Water':<20} {bfw.get('total_m3', 0):>15,.2f} {'M3':<10} {up.get('bfw_kwh', 0):>15,.2f} {'-':>15}")
    print(f"{'NMD - Utility Plant':<25} {'D M Water':<20} {dm.get('total_m3', 0):>15,.2f} {'M3':<10} {up.get('dm_kwh', 0):>15,.2f} {rw.get('dm_m3', 0):>15,.2f}")
    print(f"{'NMD - Utility Plant':<25} {'Cooling Water 1':<20} {cw.get('cw1_total_km3', 0):>15,.2f} {'KM3':<10} {up.get('cw1_kwh', 0):>15,.2f} {rw.get('cw1_m3', 0):>15,.2f}")
    print(f"{'NMD - Utility Plant':<25} {'Cooling Water 2':<20} {cw.get('cw2_total_km3', 0):>15,.2f} {'KM3':<10} {up.get('cw2_kwh', 0):>15,.2f} {rw.get('cw2_m3', 0):>15,.2f}")
    print(f"{'NMD - Utility Plant':<25} {'COMPRESSED AIR':<20} {air.get('total_nm3', 0):>15,.2f} {'NM3':<10} {up.get('air_kwh', 0):>15,.2f} {'-':>15}")
    print(f"{'NMD - Utility Plant':<25} {'Oxygen':<20} {utilities.get('oxygen_mt', 0):>15,.2f} {'MT':<10} {up.get('oxygen_kwh', 0):>15,.2f} {'-':>15}")
    print(f"{'NMD - Utility Plant':<25} {'Effluent Treated':<20} {utilities.get('effluent_m3', 0):>15,.2f} {'M3':<10} {up.get('effluent_kwh', 0):>15,.2f} {'-':>15}")
    
    print("-"*130)
    
    # =============================================
    # SECTION 6: UTILITY/POWER DISTRIBUTION
    # =============================================
    print("\n" + "-"*130)
    print("SECTION 6: UTILITY/POWER DISTRIBUTION")
    print("-"*130)
    print(f"{'Generating Plant':<25} {'Utility':<20} {'Quantity':>15} {'UOM':<10}")
    print("-"*130)
    
    total_lp = lp_prds + stg_lp + lp_steam.get("hrsg_credit_mt", 0)
    total_mp = mp_prds + stg_mp
    total_shp = hrsg1_shp + hrsg2_shp + hrsg3_shp
    
    print(f"{'NMD - Utility/Power Dist':<25} {'HP Steam_Dis':<20} {hp_prds:>15,.2f} {'MT':<10}")
    print(f"{'NMD - Utility/Power Dist':<25} {'MP Steam_Dis':<20} {total_mp:>15,.2f} {'MT':<10}")
    print(f"{'NMD - Utility/Power Dist':<25} {'LP Steam_Dis':<20} {total_lp:>15,.2f} {'MT':<10}")
    print(f"{'NMD - Utility/Power Dist':<25} {'SHP Steam_Dis':<20} {total_shp:>15,.2f} {'MT':<10}")
    print(f"{'NMD - Utility/Power Dist':<25} {'Power_Dis':<20} {up.get('total_mwh', 0):>15,.2f} {'MWH':<10}")
    
    print("-"*130)
    print("="*130)


def print_budget_summary(
    power_dispatch: list = None,
    steam_balance: dict = None,
    stg_extraction: dict = None,
    utilities: dict = None,
    shp_capacity: dict = None,
    hrsg_min_load: dict = None,
    hrsg_dispatch: dict = None,
    shp_balance: dict = None,
    import_mwh: float = 0.0,
    total_demand_mwh: float = 0.0,
    process_demand_mwh: float = 0.0,
    fixed_demand_mwh: float = 0.0,
    u4u_power_mwh: float = 0.0,
):
    """
    Print a clear and concise budget summary with power and steam numbers.
    """
    print("\n")
    print("=" * 100)
    print(" " * 30 + "BUDGET CALCULATION SUMMARY")
    print("=" * 100)
    
    # =============================================
    # POWER BALANCE SUMMARY
    # =============================================
    print("\n" + "-" * 100)
    print("POWER BALANCE SUMMARY")
    print("-" * 100)
    
    # Extract power dispatch values
    gt1_gross = gt2_gross = gt3_gross = stg_gross = 0.0
    gt1_aux = gt2_aux = gt3_aux = stg_aux = 0.0
    gt1_net = gt2_net = gt3_net = stg_net = 0.0
    gt1_load = gt2_load = gt3_load = stg_load = 0.0
    
    if power_dispatch:
        for asset in power_dispatch:
            name = str(asset.get("AssetName", "")).upper()
            if "GT1" in name or "PLANT-1" in name:
                gt1_gross = asset.get("GrossMWh", 0)
                gt1_aux = asset.get("AuxMWh", 0)
                gt1_net = asset.get("NetMWh", 0)
                gt1_load = asset.get("LoadMW", 0)
            elif "GT2" in name or "PLANT-2" in name:
                gt2_gross = asset.get("GrossMWh", 0)
                gt2_aux = asset.get("AuxMWh", 0)
                gt2_net = asset.get("NetMWh", 0)
                gt2_load = asset.get("LoadMW", 0)
            elif "GT3" in name or "PLANT-3" in name:
                gt3_gross = asset.get("GrossMWh", 0)
                gt3_aux = asset.get("AuxMWh", 0)
                gt3_net = asset.get("NetMWh", 0)
                gt3_load = asset.get("LoadMW", 0)
            elif "STG" in name or "STEAM TURBINE" in name:
                stg_gross = asset.get("GrossMWh", 0)
                stg_aux = asset.get("AuxMWh", 0)
                stg_net = asset.get("NetMWh", 0)
                stg_load = asset.get("LoadMW", 0)
    
    total_gross = gt1_gross + gt2_gross + gt3_gross + stg_gross
    total_aux = gt1_aux + gt2_aux + gt3_aux + stg_aux
    total_net = gt1_net + gt2_net + gt3_net + stg_net
    
    # Get utility power breakdown
    up = utilities.get("utility_power", {}) if utilities else {}
    utility_power_mwh = up.get("total_mwh", 0)
    
    # Power Demand Table
    print(f"\n  {'POWER DEMAND':<40} {'MWh':>15}")
    print(f"  {'-'*55}")
    print(f"  {'Process Plant Demand':<40} {process_demand_mwh:>15,.2f}")
    print(f"  {'Fixed Consumption':<40} {fixed_demand_mwh:>15,.2f}")
    print(f"  {'U4U Power (Utility for Utility)':<40} {u4u_power_mwh:>15,.2f}")
    print(f"  {'  - Power Plant Auxiliary':<40} {total_aux:>15,.2f}")
    print(f"  {'  - Utility Plant Power':<40} {utility_power_mwh:>15,.2f}")
    print(f"  {'-'*55}")
    print(f"  {'TOTAL DEMAND':<40} {total_demand_mwh:>15,.2f}")
    
    # Power Supply Table
    print(f"\n  {'POWER SUPPLY':<40} {'MWh':>15}")
    print(f"  {'-'*55}")
    print(f"  {'Import Power':<40} {import_mwh:>15,.2f}")
    print(f"  {'Net Generation':<40} {total_net:>15,.2f}")
    print(f"  {'-'*55}")
    print(f"  {'TOTAL SUPPLY':<40} {import_mwh + total_net:>15,.2f}")
    
    # Power Generation Breakdown
    print(f"\n  {'GENERATION BREAKDOWN':<20} {'Load MW':>10} {'Gross MWh':>15} {'Aux MWh':>12} {'Net MWh':>15}")
    print(f"  {'-'*72}")
    if gt1_gross > 0:
        print(f"  {'GT1':<20} {gt1_load:>10,.2f} {gt1_gross:>15,.2f} {gt1_aux:>12,.2f} {gt1_net:>15,.2f}")
    else:
        print(f"  {'GT1':<20} {'OFF':>10} {0:>15,.2f} {0:>12,.2f} {0:>15,.2f}")
    if gt2_gross > 0:
        print(f"  {'GT2':<20} {gt2_load:>10,.2f} {gt2_gross:>15,.2f} {gt2_aux:>12,.2f} {gt2_net:>15,.2f}")
    else:
        print(f"  {'GT2':<20} {'OFF':>10} {0:>15,.2f} {0:>12,.2f} {0:>15,.2f}")
    if gt3_gross > 0:
        print(f"  {'GT3':<20} {gt3_load:>10,.2f} {gt3_gross:>15,.2f} {gt3_aux:>12,.2f} {gt3_net:>15,.2f}")
    else:
        print(f"  {'GT3':<20} {'OFF':>10} {0:>15,.2f} {0:>12,.2f} {0:>15,.2f}")
    if stg_gross > 0:
        print(f"  {'STG':<20} {stg_load:>10,.2f} {stg_gross:>15,.2f} {stg_aux:>12,.2f} {stg_net:>15,.2f}")
    else:
        print(f"  {'STG':<20} {'OFF':>10} {0:>15,.2f} {0:>12,.2f} {0:>15,.2f}")
    print(f"  {'-'*72}")
    print(f"  {'TOTAL':<20} {'':<10} {total_gross:>15,.2f} {total_aux:>12,.2f} {total_net:>15,.2f}")
    
    # =============================================
    # STEAM BALANCE SUMMARY
    # =============================================
    print("\n" + "-" * 100)
    print("STEAM BALANCE SUMMARY")
    print("-" * 100)
    
    # Extract steam balance data from nested structure
    summary = steam_balance.get("summary", {}) if steam_balance else {}
    
    # Get SHP demand from summary
    shp_demand_val = summary.get("total_shp_demand", 0)
    lp_demand_val = summary.get("total_lp_demand", 0)
    mp_demand_val = summary.get("total_mp_demand", 0)
    hp_demand_val = summary.get("total_hp_demand", 0)
    
    # Get SHP capacity data
    free_steam_val = shp_capacity.get("total_free_steam_mt", 0) if shp_capacity else 0
    supp_max_val = shp_capacity.get("total_supplementary_max_mt", 0) if shp_capacity else 0
    shp_capacity_val = shp_capacity.get("total_max_shp_capacity", 0) if shp_capacity else 0
    supp_needed_val = max(0, shp_demand_val - free_steam_val) if shp_demand_val > free_steam_val else 0
    
    # Get HRSG MIN load data (actual dispatch - HRSGs run at MIN load)
    hrsg_min_details = hrsg_min_load.get("hrsg_details", []) if hrsg_min_load else []
    hrsg1_free = hrsg2_free = hrsg3_free = 0.0
    hrsg1_min_supp = hrsg2_min_supp = hrsg3_min_supp = 0.0
    hrsg1_min_prod = hrsg2_min_prod = hrsg3_min_prod = 0.0
    
    for hrsg in hrsg_min_details:
        hrsg_name = hrsg.get("name", "")
        free_steam = hrsg.get("free_steam_mt", 0)
        min_supp = hrsg.get("min_supp_firing_mt", 0)
        min_prod = hrsg.get("min_production_mt", 0)
        if "HRSG1" in hrsg_name:
            hrsg1_free = free_steam
            hrsg1_min_supp = min_supp
            hrsg1_min_prod = min_prod
        elif "HRSG2" in hrsg_name:
            hrsg2_free = free_steam
            hrsg2_min_supp = min_supp
            hrsg2_min_prod = min_prod
        elif "HRSG3" in hrsg_name:
            hrsg3_free = free_steam
            hrsg3_min_supp = min_supp
            hrsg3_min_prod = min_prod
    
    total_min_supp = hrsg1_min_supp + hrsg2_min_supp + hrsg3_min_supp
    total_min_prod = hrsg1_min_prod + hrsg2_min_prod + hrsg3_min_prod
    
    # Get excess steam info
    excess_steam = hrsg_min_load.get("excess_steam_mt", 0) if hrsg_min_load else 0
    excess_power = hrsg_min_load.get("excess_power_mwh", 0) if hrsg_min_load else 0
    
    if steam_balance or shp_capacity or hrsg_min_load:
        print(f"\n  {'SHP STEAM BALANCE':<40} {'MT':>15}")
        print(f"  {'-'*55}")
        print(f"  {'SHP Demand (Total)':<40} {shp_demand_val:>15,.2f}")
        print(f"  {'Free Steam (from GT exhaust)':<40} {free_steam_val:>15,.2f}")
        print(f"  {'Min Supp Firing (60 MT/hr rule)':<40} {total_min_supp:>15,.2f}")
        print(f"  {'Total MIN SHP Production':<40} {total_min_prod:>15,.2f}")
        if excess_steam > 0:
            print(f"  {'-'*55}")
            print(f"  {'EXCESS STEAM (MIN > Demand)':<40} {excess_steam:>15,.2f}")
            print(f"  {'Excess Power via STG (@ 3.56 MT/MWh)':<40} {excess_power:>15,.2f}")
        print(f"  {'-'*55}")
        print(f"  {'Supplementary Max Capacity':<40} {supp_max_val:>15,.2f}")
        
        print(f"\n  {'SHP GENERATION BY HRSG (MIN LOAD)':<25} {'Free Steam':>15} {'Min Supp':>15} {'Total MIN':>15}")
        print(f"  {'-'*70}")
        print(f"  {'HRSG1':<25} {hrsg1_free:>15,.2f} {hrsg1_min_supp:>15,.2f} {hrsg1_min_prod:>15,.2f}")
        print(f"  {'HRSG2':<25} {hrsg2_free:>15,.2f} {hrsg2_min_supp:>15,.2f} {hrsg2_min_prod:>15,.2f}")
        print(f"  {'HRSG3':<25} {hrsg3_free:>15,.2f} {hrsg3_min_supp:>15,.2f} {hrsg3_min_prod:>15,.2f}")
        print(f"  {'-'*70}")
        print(f"  {'TOTAL':<25} {free_steam_val:>15,.2f} {total_min_supp:>15,.2f} {total_min_prod:>15,.2f}")
    
    # STG Extraction
    if stg_extraction:
        lp_from_stg = stg_extraction.get("lp_from_stg", 0)
        mp_from_stg = stg_extraction.get("mp_from_stg", 0)
        stg_shp_inlet = stg_extraction.get("stg_shp_inlet_mt", 0)
        stg_condensate = stg_extraction.get("stg_condensate_m3", 0)
        
        print(f"\n  {'STG EXTRACTION':<40} {'Value':>15} {'Unit':>10}")
        print(f"  {'-'*65}")
        print(f"  {'LP Steam from STG':<40} {lp_from_stg:>15,.2f} {'MT':>10}")
        print(f"  {'MP Steam from STG':<40} {mp_from_stg:>15,.2f} {'MT':>10}")
        print(f"  {'SHP Inlet to STG':<40} {stg_shp_inlet:>15,.2f} {'MT':>10}")
        print(f"  {'Condensate Return':<40} {stg_condensate:>15,.2f} {'M3':>10}")
    
    # Steam Distribution Summary
    if steam_balance or shp_capacity:
        print(f"\n  {'STEAM DISTRIBUTION':<40} {'MT':>15}")
        print(f"  {'-'*55}")
        print(f"  {'HP Steam Distributed':<40} {hp_demand_val:>15,.2f}")
        print(f"  {'MP Steam Distributed':<40} {mp_demand_val:>15,.2f}")
        print(f"  {'LP Steam Distributed':<40} {lp_demand_val:>15,.2f}")
    
    # =============================================
    # DETAILED DEMAND vs SUPPLY TABLE
    # =============================================
    print("\n" + "=" * 120)
    print(" " * 40 + "DEMAND vs SUPPLY SUMMARY")
    print("=" * 120)
    
    gen_pct = (total_net / total_demand_mwh * 100) if total_demand_mwh > 0 else 0
    import_pct = (import_mwh / total_demand_mwh * 100) if total_demand_mwh > 0 else 0
    
    # Get HRSG dispatch details (actual dispatched values)
    # Note: Free steam is display only, NOT included in total SHP supply for balance
    hrsg_dispatch_list = hrsg_dispatch.get("hrsg_dispatch", []) if hrsg_dispatch else []
    total_free_steam = hrsg_dispatch.get("total_free_steam_mt", 0) if hrsg_dispatch else free_steam_val
    total_dispatched_supp = hrsg_dispatch.get("total_dispatched_supp_mt", 0) if hrsg_dispatch else total_min_supp
    # Total SHP supply = Only Dispatched Supp Firing (Free Steam excluded from balance)
    total_shp_supply = hrsg_dispatch.get("total_shp_supply_mt", 0) if hrsg_dispatch else total_dispatched_supp
    
    # Get SHP demand breakdown from steam_balance
    # Note: steam_balance contains the detailed breakdown, shp_balance is just capacity check
    shp_breakdown = steam_balance.get("shp_balance", {}) if steam_balance else {}
    mp_breakdown = steam_balance.get("mp_balance", {}) if steam_balance else {}
    
    shp_process = shp_breakdown.get("shp_process", 0)
    shp_fixed = shp_breakdown.get("shp_fixed", 0)
    shp_for_stg = shp_breakdown.get("stg_shp_power", 0)  # SHP for STG power generation
    shp_for_lp_ext = shp_breakdown.get("shp_for_stg_lp", 0)  # SHP for LP extraction via STG
    shp_for_mp_ext = mp_breakdown.get("shp_for_stg_mp", 0)  # SHP for MP extraction via STG (from mp_balance)
    shp_for_hp_prds = shp_breakdown.get("shp_for_hp_prds", 0)
    shp_for_mp_prds = mp_breakdown.get("shp_for_prds_mp", 0)  # SHP for MP via PRDS (from mp_balance)
    total_shp_demand = shp_breakdown.get("shp_total_demand", shp_demand_val) if shp_breakdown else shp_demand_val
    
    # Power balance status
    power_balance = total_demand_mwh - (import_mwh + total_net)
    power_status = "✓ BALANCED" if abs(power_balance) < 1 else f"⚠ DIFF: {power_balance:,.2f}"
    
    # Steam balance status
    steam_balance_val = total_shp_supply - total_shp_demand
    steam_status = "✓ BALANCED" if abs(steam_balance_val) < 1 else f"⚠ DIFF: {steam_balance_val:,.2f}"
    
    # =============================================
    # POWER TABLE
    # =============================================
    print("\n" + "─" * 120)
    print("  POWER BALANCE")
    print("─" * 120)
    print(f"\n  {'DEMAND':<50} {'SUPPLY':<50}")
    print(f"  {'─'*45}     {'─'*45}")
    
    # Power demand items
    demand_items = [
        ("Process Plant Demand", process_demand_mwh, "MWh"),
        ("Fixed Consumption", fixed_demand_mwh, "MWh"),
        ("U4U Power (Utility for Utility)", u4u_power_mwh, "MWh"),
        ("  - Power Plant Auxiliary", total_aux, "MWh"),
        ("  - Utility Plant Power", up.get("total_mwh", 0), "MWh"),
    ]
    
    # Power supply items
    supply_items = [
        ("Import Power", import_mwh, "MWh", import_pct),
        ("GT2 Net Generation", gt2_net, "MWh", (gt2_net/total_demand_mwh*100) if total_demand_mwh > 0 else 0),
        ("GT3 Net Generation", gt3_net, "MWh", (gt3_net/total_demand_mwh*100) if total_demand_mwh > 0 else 0),
        ("STG Net Generation", stg_net, "MWh", (stg_net/total_demand_mwh*100) if total_demand_mwh > 0 else 0),
    ]
    if gt1_net > 0:
        supply_items.insert(1, ("GT1 Net Generation", gt1_net, "MWh", (gt1_net/total_demand_mwh*100) if total_demand_mwh > 0 else 0))
    
    max_rows = max(len(demand_items), len(supply_items))
    for i in range(max_rows):
        # Demand column
        if i < len(demand_items):
            d_name, d_val, d_unit = demand_items[i]
            demand_str = f"  {d_name:<35} {d_val:>12,.2f} {d_unit}"
        else:
            demand_str = " " * 50
        
        # Supply column
        if i < len(supply_items):
            s_name, s_val, s_unit, s_pct = supply_items[i]
            supply_str = f"{s_name:<35} {s_val:>12,.2f} {s_unit} ({s_pct:>5.1f}%)"
        else:
            supply_str = ""
        
        print(f"{demand_str:<55} {supply_str}")
    
    print(f"  {'─'*45}     {'─'*45}")
    print(f"  {'TOTAL DEMAND':<35} {total_demand_mwh:>12,.2f} MWh     {'TOTAL SUPPLY':<35} {import_mwh + total_net:>12,.2f} MWh")
    print(f"\n  {'POWER STATUS:':<20} {power_status}")
    
    # =============================================
    # STEAM (SHP) TABLE
    # =============================================
    print("\n" + "─" * 120)
    print("  SHP STEAM BALANCE")
    print("─" * 120)
    print(f"\n  {'DEMAND':<50} {'SUPPLY':<50}")
    print(f"  {'─'*45}     {'─'*45}")
    
    # SHP demand items
    shp_demand_items = [
        ("SHP Process Demand", shp_process, "MT"),
        ("SHP Fixed Demand", shp_fixed, "MT"),
        ("SHP for STG Power", shp_for_stg, "MT"),
        ("SHP for LP Extraction (STG)", shp_for_lp_ext, "MT"),
        ("SHP for MP Extraction (STG)", shp_for_mp_ext, "MT"),
        ("SHP for HP PRDS", shp_for_hp_prds, "MT"),
        ("SHP for MP PRDS", shp_for_mp_prds, "MT"),
    ]
    
    # SHP supply items - from HRSG dispatch
    shp_supply_items = [
        ("Free Steam (from GT exhaust)", total_free_steam, "MT"),
    ]
    
    # Add individual HRSG dispatch
    for hrsg in hrsg_dispatch_list:
        hrsg_name = hrsg.get("name", "")
        dispatched = hrsg.get("dispatched_supp_mt", 0)
        if dispatched > 0:
            shp_supply_items.append((f"{hrsg_name} Supp Firing", dispatched, "MT"))
    
    max_rows = max(len(shp_demand_items), len(shp_supply_items))
    for i in range(max_rows):
        # Demand column
        if i < len(shp_demand_items):
            d_name, d_val, d_unit = shp_demand_items[i]
            demand_str = f"  {d_name:<35} {d_val:>12,.2f} {d_unit}"
        else:
            demand_str = " " * 50
        
        # Supply column
        if i < len(shp_supply_items):
            s_name, s_val, s_unit = shp_supply_items[i]
            supply_str = f"{s_name:<35} {s_val:>12,.2f} {s_unit}"
        else:
            supply_str = ""
        
        print(f"{demand_str:<55} {supply_str}")
    
    print(f"  {'─'*45}     {'─'*45}")
    print(f"  {'TOTAL SHP DEMAND':<35} {total_shp_demand:>12,.2f} MT      {'TOTAL SHP SUPPLY':<35} {total_shp_supply:>12,.2f} MT")
    print(f"\n  {'SHP STATUS:':<20} {steam_status}")
    
    # =============================================
    # HRSG DISPATCH DETAILS
    # =============================================
    if hrsg_dispatch_list:
        print("\n" + "─" * 120)
        print("  HRSG DISPATCH DETAILS")
        print("─" * 120)
        print(f"\n  {'HRSG':<12} {'Priority':<10} {'Hours':<10} {'Free Steam':<15} {'Dispatched Supp':<18} {'Total SHP':<15} {'Status':<12}")
        print(f"  {'─'*100}")
        
        for hrsg in hrsg_dispatch_list:
            name = hrsg.get("name", "")
            priority = hrsg.get("priority", 999)
            hours = hrsg.get("hours", 0)
            free_steam_h = hrsg.get("free_steam_mt", 0)
            dispatched = hrsg.get("dispatched_supp_mt", 0)
            total_shp_h = hrsg.get("total_shp_mt", 0)
            min_supp_h = hrsg.get("min_supp_mt", 0)
            max_supp_h = hrsg.get("max_supp_mt", 0)
            
            if dispatched <= min_supp_h:
                status = "AT MIN"
            elif dispatched >= max_supp_h:
                status = "AT MAX"
            else:
                status = "PARTIAL"
            
            pri_str = str(int(priority)) if priority != 999 else "-"
            print(f"  {name:<12} {pri_str:<10} {hours:<10.0f} {free_steam_h:>12,.2f} MT {dispatched:>15,.2f} MT {total_shp_h:>12,.2f} MT {status:<12}")
        
        print(f"  {'─'*100}")
        print(f"  {'TOTAL':<12} {'':<10} {'':<10} {total_free_steam:>12,.2f} MT {total_dispatched_supp:>15,.2f} MT {total_shp_supply:>12,.2f} MT")
    
    # =============================================
    # QUICK SUMMARY BOX
    # =============================================
    print("\n" + "=" * 120)
    print(" " * 45 + "QUICK SUMMARY")
    print("=" * 120)
    
    print(f"""
  ┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
  │  POWER                                                                                                         │
  │  ────────────────────────────────────────────────────────────────────────────────────────────────────────────  │
  │  Total Demand:         {total_demand_mwh:>12,.2f} MWh          Total Supply:         {import_mwh + total_net:>12,.2f} MWh          {power_status:<15}  │
  │  Import Power:         {import_mwh:>12,.2f} MWh ({import_pct:>5.1f}%)    Net Generation:       {total_net:>12,.2f} MWh ({gen_pct:>5.1f}%)                   │
  │  U4U Power:            {u4u_power_mwh:>12,.2f} MWh                                                                               │
  └────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
""")
    
    print(f"""  ┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
  │  STEAM (SHP)                                                                                                   │
  │  ────────────────────────────────────────────────────────────────────────────────────────────────────────────  │
  │  Total Demand:         {total_shp_demand:>12,.2f} MT           Total Supply:         {total_shp_supply:>12,.2f} MT           {steam_status:<15}  │
  │  Free Steam (GT):      {total_free_steam:>12,.2f} MT           Dispatched Supp:      {total_dispatched_supp:>12,.2f} MT                              │
  │  Excess Steam:         {max(0, steam_balance_val):>12,.2f} MT                                                                               │
  └────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
""")
    
    print("=" * 120)


# ============================================================
# TEST
# ============================================================
if __name__ == "__main__":
    print("="*70)
    print("UTILITY SERVICE TEST")
    print("="*70)
    
    # Example values from a typical run (April 2025)
    utilities = calculate_utilities_from_dispatch(
        # Power dispatch
        gt1_gross_mwh=10984.80,
        gt2_gross_mwh=10352.20,
        gt3_gross_mwh=0.0,  # Not available
        stg_gross_mwh=16250.00,
        # Steam balance
        shp_from_hrsg2=54294.05,
        shp_from_hrsg3=55746.58,
        hp_from_prds=4972.0,
        mp_from_prds=15524.59,
        lp_from_prds=9789.71,
        lp_from_stg=15532.87,
        mp_from_stg=6365.69,
        # Process
        oxygen_mt=5786.0,
        effluent_m3=243000.0,
        # Availability
        gt1_available=True,
        gt2_available=True,
        gt3_available=False,
        hrsg1_available=False,
        hrsg2_available=True,
        hrsg3_available=True,
    )
    
    print_utility_summary(utilities)
