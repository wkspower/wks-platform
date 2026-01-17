"""
Demand Service - Calculates Total Demand for All Utilities
Total Demand = Fixed + Process + U4U (Utility for Utility)

This service calculates the complete demand breakdown for:
- Power (MWH)
- Steam: SHP, HP, MP, LP (MT)
- BFW (M3)
- DM Water (M3)
- Cooling Water 1 & 2 (KM3)
- Compressed Air (NM3)
- Oxygen (MT)
- Effluent (M3)
"""

from database.connection import get_connection

# ============================================================
# NORMS CONSTANTS (From Norms Table)
# ============================================================

# Power Plant Auxiliary Consumption (KWH per KWH generated)
NORM_GT_AUX_PER_KWH = 0.0140          # GT1, GT2, GT3: 1.4% aux consumption
NORM_STG_AUX_PER_KWH = 0.0020         # STG: 0.2% aux consumption

# Utility Power Consumption Norms (KWH per unit)
NORM_BFW_POWER_PER_M3 = 9.5000        # 9.5 KWH per M3 BFW
NORM_DM_POWER_PER_M3 = 1.2100         # 1.21 KWH per M3 DM Water
NORM_CW1_POWER_PER_KM3 = 245.0000     # 245 KWH per KM3 Cooling Water 1
NORM_CW2_POWER_PER_KM3 = 250.0000     # 250 KWH per KM3 Cooling Water 2
NORM_AIR_POWER_PER_NM3 = 0.1650       # 0.165 KWH per NM3 Compressed Air
NORM_EFFLUENT_POWER_PER_M3 = 3.5400   # 3.54 KWH per M3 Effluent
NORM_OXYGEN_POWER_PER_MT = 968.6500   # 968.65 KWH per MT Oxygen

# BFW Consumption Norms (M3 per MT steam)
NORM_BFW_PER_MT_SHP = 1.0240          # 1.024 M3 BFW per MT SHP (HRSG)
NORM_BFW_PER_MT_HP_PRDS = 0.0768      # 0.0768 M3 BFW per MT HP PRDS
NORM_BFW_PER_MT_MP_PRDS = 0.0900      # 0.09 M3 BFW per MT MP PRDS
NORM_BFW_PER_MT_LP_PRDS = 0.2500      # 0.25 M3 BFW per MT LP PRDS

# DM Water Consumption Norms
NORM_DM_PER_M3_BFW = 0.8600           # 0.86 M3 DM per M3 BFW

# Cooling Water Consumption Norms (KM3 per unit)
NORM_CW2_PER_1000MT_SHP_STG = 2.4550  # 2.455 KM3 per 1000 MT SHP for STG
NORM_CW2_PER_1000MWH_GT = 0.112       # 0.112 KM3 per 1000 MWH GT

# Compressed Air Consumption Norms (NM3 fixed per month per asset)
NORM_AIR_GT_PER_MONTH = 30960.0       # 30,960 NM3 per GT per month
NORM_AIR_STG_PER_MONTH = 41040.0      # 41,040 NM3 per STG per month
NORM_AIR_HRSG_PER_MONTH = 453600.0    # 453,600 NM3 per HRSG per month
NORM_AIR_CW_PER_MONTH = 0.0           # CW air consumption (if any)

# STG Steam Requirement
NORM_STG_SHP_PER_KWH = 0.0036         # 0.0036 MT SHP per KWH STG

# STG Extraction Ratios
NORM_LP_FROM_STG_RATIO = 0.6134       # 61.34% of LP from STG
NORM_MP_FROM_STG_RATIO = 0.2908       # 29.08% of MP from STG
NORM_SHP_PER_LP_STG = 0.48            # 0.48 MT SHP per MT LP extraction
NORM_SHP_PER_MP_STG = 0.69            # 0.69 MT SHP per MT MP extraction

# PRDS Steam Conversion
NORM_SHP_PER_HP_PRDS = 1.0            # 1 MT SHP per MT HP PRDS
NORM_SHP_PER_MP_PRDS = 1.0            # 1 MT SHP per MT MP PRDS (via HP)


def fetch_fixed_process_demands(month: int, year: int) -> dict:
    """
    Fetch Fixed and Process demands from database for all utilities.
    
    Returns dict with structure:
    {
        "power": {"fixed": MWH, "process": MWH},
        "steam_lp": {"fixed": MT, "process": MT},
        "steam_mp": {"fixed": MT, "process": MT},
        "steam_hp": {"fixed": MT, "process": MT},
        "steam_shp": {"fixed": MT, "process": MT},
        "bfw": {"fixed": M3, "process": M3},
        "dm_water": {"fixed": M3, "process": M3},
        "cw1": {"fixed": KM3, "process": KM3},
        "cw2": {"fixed": KM3, "process": KM3},
        "compressed_air": {"fixed": NM3, "process": NM3},
        "oxygen": {"fixed": MT, "process": MT},
        "effluent": {"fixed": M3, "process": M3},
    }
    """
    conn = get_connection()
    cur = conn.cursor()
    
    # Get FYM ID
    cur.execute("SELECT Id FROM FinancialYearMonth WHERE [Month]=? AND [Year]=?", (month, year))
    row = cur.fetchone()
    if not row:
        conn.close()
        return None
    fym_id = row[0]
    
    # Fetch Power demands from CalculatedProcessDemand via process_demand_service
    from services.process_demand_service import get_process_demand_for_month
    process_demands = get_process_demand_for_month(month, year)
    power_process_kwh = process_demands.get("power_process", 0.0)
    # Convert KWH to MWh
    power_process = power_process_kwh / 1000.0
    
    # Fetch Fixed consumption from UtilityFixedConsumption table via fixed_consumption_service
    from services.fixed_consumption_service import get_fixed_consumption_for_month
    fixed_consumption = get_fixed_consumption_for_month(month, year)
    power_fixed_kwh = fixed_consumption.get("power_fixed_kwh", 0.0)
    # Convert KWH to MWh
    power_fixed = power_fixed_kwh / 1000.0
    
    conn.close()
    
    # Return demands (steam and other utilities are passed as parameters for now)
    return {
        "power": {"fixed": power_fixed, "process": power_process},
    }


def calculate_u4u_power(
    # Power generation (for aux calculation)
    gt1_gross_mwh: float = 0.0,
    gt2_gross_mwh: float = 0.0,
    gt3_gross_mwh: float = 0.0,
    stg_gross_mwh: float = 0.0,
    # Utility quantities (for power consumption)
    bfw_total_m3: float = 0.0,
    dm_total_m3: float = 0.0,
    cw1_total_km3: float = 0.0,
    cw2_total_km3: float = 0.0,
    air_total_nm3: float = 0.0,
    oxygen_total_mt: float = 0.0,
    effluent_total_m3: float = 0.0,
) -> dict:
    """
    Calculate U4U Power consumption from all utilities.
    
    Returns breakdown of power consumed by each utility.
    """
    # Power Plant Auxiliary (U4U for power generation)
    gt1_aux_kwh = gt1_gross_mwh * 1000 * NORM_GT_AUX_PER_KWH
    gt2_aux_kwh = gt2_gross_mwh * 1000 * NORM_GT_AUX_PER_KWH
    gt3_aux_kwh = gt3_gross_mwh * 1000 * NORM_GT_AUX_PER_KWH
    stg_aux_kwh = stg_gross_mwh * 1000 * NORM_STG_AUX_PER_KWH
    total_power_aux_kwh = gt1_aux_kwh + gt2_aux_kwh + gt3_aux_kwh + stg_aux_kwh
    
    # Utility Power Consumption (U4U for utilities)
    bfw_power_kwh = bfw_total_m3 * NORM_BFW_POWER_PER_M3
    dm_power_kwh = dm_total_m3 * NORM_DM_POWER_PER_M3
    cw1_power_kwh = cw1_total_km3 * NORM_CW1_POWER_PER_KM3
    cw2_power_kwh = cw2_total_km3 * NORM_CW2_POWER_PER_KM3
    air_power_kwh = air_total_nm3 * NORM_AIR_POWER_PER_NM3
    oxygen_power_kwh = oxygen_total_mt * NORM_OXYGEN_POWER_PER_MT
    effluent_power_kwh = effluent_total_m3 * NORM_EFFLUENT_POWER_PER_M3
    
    total_utility_power_kwh = (bfw_power_kwh + dm_power_kwh + cw1_power_kwh + 
                               cw2_power_kwh + air_power_kwh + oxygen_power_kwh + 
                               effluent_power_kwh)
    
    total_u4u_power_kwh = total_power_aux_kwh + total_utility_power_kwh
    
    return {
        "power_aux": {
            "gt1_kwh": round(gt1_aux_kwh, 2),
            "gt2_kwh": round(gt2_aux_kwh, 2),
            "gt3_kwh": round(gt3_aux_kwh, 2),
            "stg_kwh": round(stg_aux_kwh, 2),
            "total_kwh": round(total_power_aux_kwh, 2),
            "total_mwh": round(total_power_aux_kwh / 1000, 2),
        },
        "utility_power": {
            "bfw_kwh": round(bfw_power_kwh, 2),
            "dm_kwh": round(dm_power_kwh, 2),
            "cw1_kwh": round(cw1_power_kwh, 2),
            "cw2_kwh": round(cw2_power_kwh, 2),
            "air_kwh": round(air_power_kwh, 2),
            "oxygen_kwh": round(oxygen_power_kwh, 2),
            "effluent_kwh": round(effluent_power_kwh, 2),
            "total_kwh": round(total_utility_power_kwh, 2),
            "total_mwh": round(total_utility_power_kwh / 1000, 2),
        },
        "total_u4u_kwh": round(total_u4u_power_kwh, 2),
        "total_u4u_mwh": round(total_u4u_power_kwh / 1000, 2),
    }


def calculate_u4u_bfw(
    shp_from_hrsg_mt: float = 0.0,
    hp_from_prds_mt: float = 0.0,
    mp_from_prds_mt: float = 0.0,
    lp_from_prds_mt: float = 0.0,
) -> dict:
    """
    Calculate U4U BFW consumption from steam generation.
    """
    bfw_hrsg = shp_from_hrsg_mt * NORM_BFW_PER_MT_SHP
    bfw_hp_prds = hp_from_prds_mt * NORM_BFW_PER_MT_HP_PRDS
    bfw_mp_prds = mp_from_prds_mt * NORM_BFW_PER_MT_MP_PRDS
    bfw_lp_prds = lp_from_prds_mt * NORM_BFW_PER_MT_LP_PRDS
    
    total_bfw = bfw_hrsg + bfw_hp_prds + bfw_mp_prds + bfw_lp_prds
    
    return {
        "hrsg_m3": round(bfw_hrsg, 2),
        "hp_prds_m3": round(bfw_hp_prds, 2),
        "mp_prds_m3": round(bfw_mp_prds, 2),
        "lp_prds_m3": round(bfw_lp_prds, 2),
        "total_m3": round(total_bfw, 2),
    }


def calculate_u4u_dm(bfw_total_m3: float = 0.0) -> dict:
    """
    Calculate U4U DM Water consumption from BFW.
    """
    dm_for_bfw = bfw_total_m3 * NORM_DM_PER_M3_BFW
    return {
        "for_bfw_m3": round(dm_for_bfw, 2),
        "total_m3": round(dm_for_bfw, 2),
    }


def calculate_u4u_cw2(
    stg_gross_mwh: float = 0.0,
    gt_gross_mwh: float = 0.0,
    shp_from_stg_mt: float = 0.0,
) -> dict:
    """
    Calculate U4U Cooling Water 2 consumption from power plants.
    """
    cw2_stg = (shp_from_stg_mt / 1000) * NORM_CW2_PER_1000MT_SHP_STG
    cw2_gt = (gt_gross_mwh / 1000) * NORM_CW2_PER_1000MWH_GT
    
    return {
        "stg_km3": round(cw2_stg, 2),
        "gt_km3": round(cw2_gt, 2),
        "total_km3": round(cw2_stg + cw2_gt, 2),
    }


def calculate_u4u_air(
    gt_count: int = 0,
    stg_available: bool = False,
    hrsg_count: int = 0,
) -> dict:
    """
    Calculate U4U Compressed Air consumption from power plants.
    """
    air_gt = gt_count * NORM_AIR_GT_PER_MONTH
    air_stg = NORM_AIR_STG_PER_MONTH if stg_available else 0.0
    air_hrsg = hrsg_count * NORM_AIR_HRSG_PER_MONTH
    
    return {
        "gt_nm3": round(air_gt, 2),
        "stg_nm3": round(air_stg, 2),
        "hrsg_nm3": round(air_hrsg, 2),
        "total_nm3": round(air_gt + air_stg + air_hrsg, 2),
    }


def calculate_u4u_shp(
    stg_gross_mwh: float = 0.0,
    lp_from_stg_mt: float = 0.0,
    mp_from_stg_mt: float = 0.0,
    hp_from_prds_mt: float = 0.0,
    mp_from_prds_mt: float = 0.0,
) -> dict:
    """
    Calculate U4U SHP consumption from STG and PRDS.
    """
    shp_stg_power = stg_gross_mwh * 1000 * NORM_STG_SHP_PER_KWH
    shp_lp_extraction = lp_from_stg_mt * NORM_SHP_PER_LP_STG
    shp_mp_extraction = mp_from_stg_mt * NORM_SHP_PER_MP_STG
    shp_hp_prds = hp_from_prds_mt * NORM_SHP_PER_HP_PRDS
    shp_mp_prds = mp_from_prds_mt * NORM_SHP_PER_MP_PRDS
    
    total_shp = shp_stg_power + shp_lp_extraction + shp_mp_extraction + shp_hp_prds + shp_mp_prds
    
    return {
        "stg_power_mt": round(shp_stg_power, 2),
        "lp_extraction_mt": round(shp_lp_extraction, 2),
        "mp_extraction_mt": round(shp_mp_extraction, 2),
        "hp_prds_mt": round(shp_hp_prds, 2),
        "mp_prds_mt": round(shp_mp_prds, 2),
        "total_mt": round(total_shp, 2),
    }


def calculate_all_demands(
    month: int,
    year: int,
    # Steam demands (Fixed + Process from input)
    lp_process: float = 0.0,
    lp_fixed: float = 0.0,
    mp_process: float = 0.0,
    mp_fixed: float = 0.0,
    hp_process: float = 0.0,
    hp_fixed: float = 0.0,
    shp_process: float = 0.0,
    shp_fixed: float = 0.0,
    # Other utility demands (Fixed + Process from input)
    bfw_process: float = 0.0,
    bfw_fixed: float = 0.0,
    dm_process: float = 54779.0,
    dm_fixed: float = 0.0,
    cw1_process: float = 15194.0,
    cw1_fixed: float = 0.0,
    cw2_process: float = 9016.0,
    cw2_fixed: float = 0.0,
    air_process: float = 6095102.0,
    air_fixed: float = 0.0,
    oxygen_process: float = 5786.0,
    oxygen_fixed: float = 0.0,
    effluent_process: float = 243000.0,
    effluent_fixed: float = 0.0,
    # Dispatch results (for U4U calculation)
    gt1_gross_mwh: float = 0.0,
    gt2_gross_mwh: float = 0.0,
    gt3_gross_mwh: float = 0.0,
    stg_gross_mwh: float = 0.0,
    shp_from_hrsg_mt: float = 0.0,
    hp_from_prds_mt: float = 0.0,
    mp_from_prds_mt: float = 0.0,
    lp_from_prds_mt: float = 0.0,
    lp_from_stg_mt: float = 0.0,
    mp_from_stg_mt: float = 0.0,
    gt_count: int = 0,
    stg_available: bool = False,
    hrsg_count: int = 0,
) -> dict:
    """
    Calculate complete demand breakdown for all utilities.
    
    Returns:
        dict with Fixed, Process, U4U, and Total for each utility
    """
    # Fetch power demands from database
    db_demands = fetch_fixed_process_demands(month, year)
    power_fixed = db_demands["power"]["fixed"] if db_demands else 0.0
    power_process = db_demands["power"]["process"] if db_demands else 0.0
    
    # Calculate U4U for BFW
    u4u_bfw = calculate_u4u_bfw(shp_from_hrsg_mt, hp_from_prds_mt, mp_from_prds_mt, lp_from_prds_mt)
    bfw_u4u = u4u_bfw["total_m3"]
    bfw_total = bfw_fixed + bfw_process + bfw_u4u
    
    # Calculate U4U for DM (depends on total BFW)
    u4u_dm = calculate_u4u_dm(bfw_total)
    dm_u4u = u4u_dm["total_m3"]
    dm_total = dm_fixed + dm_process + dm_u4u
    
    # Calculate U4U for CW2
    shp_for_stg = stg_gross_mwh * 1000 * NORM_STG_SHP_PER_KWH
    u4u_cw2 = calculate_u4u_cw2(stg_gross_mwh, gt1_gross_mwh + gt2_gross_mwh + gt3_gross_mwh, shp_for_stg)
    cw2_u4u = u4u_cw2["total_km3"]
    cw2_total = cw2_fixed + cw2_process + cw2_u4u
    
    # CW1 has no U4U (only process)
    cw1_u4u = 0.0
    cw1_total = cw1_fixed + cw1_process + cw1_u4u
    
    # Calculate U4U for Compressed Air
    u4u_air = calculate_u4u_air(gt_count, stg_available, hrsg_count)
    air_u4u = u4u_air["total_nm3"]
    air_total = air_fixed + air_process + air_u4u
    
    # Oxygen and Effluent have no U4U (only process/fixed)
    oxygen_u4u = 0.0
    oxygen_total = oxygen_fixed + oxygen_process + oxygen_u4u
    effluent_u4u = 0.0
    effluent_total = effluent_fixed + effluent_process + effluent_u4u
    
    # Calculate U4U for Power (depends on all utility totals)
    u4u_power = calculate_u4u_power(
        gt1_gross_mwh, gt2_gross_mwh, gt3_gross_mwh, stg_gross_mwh,
        bfw_total, dm_total, cw1_total, cw2_total, air_total, oxygen_total, effluent_total
    )
    power_u4u_mwh = u4u_power["total_u4u_mwh"]
    power_total = power_fixed + power_process + power_u4u_mwh
    
    # Calculate U4U for SHP
    u4u_shp = calculate_u4u_shp(stg_gross_mwh, lp_from_stg_mt, mp_from_stg_mt, hp_from_prds_mt, mp_from_prds_mt)
    shp_u4u = u4u_shp["total_mt"]
    shp_total = shp_fixed + shp_process + shp_u4u
    
    # LP, MP, HP don't have direct U4U (they drive SHP U4U)
    lp_u4u = 0.0
    lp_total = lp_fixed + lp_process + lp_u4u
    mp_u4u = 0.0
    mp_total = mp_fixed + mp_process + mp_u4u
    hp_u4u = 0.0
    hp_total = hp_fixed + hp_process + hp_u4u
    
    return {
        "power": {
            "fixed": round(power_fixed, 2),
            "process": round(power_process, 2),
            "u4u": round(power_u4u_mwh, 2),
            "total": round(power_total, 2),
            "u4u_detail": u4u_power,
            "unit": "MWH",
        },
        "steam_shp": {
            "fixed": round(shp_fixed, 2),
            "process": round(shp_process, 2),
            "u4u": round(shp_u4u, 2),
            "total": round(shp_total, 2),
            "u4u_detail": u4u_shp,
            "unit": "MT",
        },
        "steam_hp": {
            "fixed": round(hp_fixed, 2),
            "process": round(hp_process, 2),
            "u4u": round(hp_u4u, 2),
            "total": round(hp_total, 2),
            "unit": "MT",
        },
        "steam_mp": {
            "fixed": round(mp_fixed, 2),
            "process": round(mp_process, 2),
            "u4u": round(mp_u4u, 2),
            "total": round(mp_total, 2),
            "unit": "MT",
        },
        "steam_lp": {
            "fixed": round(lp_fixed, 2),
            "process": round(lp_process, 2),
            "u4u": round(lp_u4u, 2),
            "total": round(lp_total, 2),
            "unit": "MT",
        },
        "bfw": {
            "fixed": round(bfw_fixed, 2),
            "process": round(bfw_process, 2),
            "u4u": round(bfw_u4u, 2),
            "total": round(bfw_total, 2),
            "u4u_detail": u4u_bfw,
            "unit": "M3",
        },
        "dm_water": {
            "fixed": round(dm_fixed, 2),
            "process": round(dm_process, 2),
            "u4u": round(dm_u4u, 2),
            "total": round(dm_total, 2),
            "u4u_detail": u4u_dm,
            "unit": "M3",
        },
        "cw1": {
            "fixed": round(cw1_fixed, 2),
            "process": round(cw1_process, 2),
            "u4u": round(cw1_u4u, 2),
            "total": round(cw1_total, 2),
            "unit": "KM3",
        },
        "cw2": {
            "fixed": round(cw2_fixed, 2),
            "process": round(cw2_process, 2),
            "u4u": round(cw2_u4u, 2),
            "total": round(cw2_total, 2),
            "u4u_detail": u4u_cw2,
            "unit": "KM3",
        },
        "compressed_air": {
            "fixed": round(air_fixed, 2),
            "process": round(air_process, 2),
            "u4u": round(air_u4u, 2),
            "total": round(air_total, 2),
            "u4u_detail": u4u_air,
            "unit": "NM3",
        },
        "oxygen": {
            "fixed": round(oxygen_fixed, 2),
            "process": round(oxygen_process, 2),
            "u4u": round(oxygen_u4u, 2),
            "total": round(oxygen_total, 2),
            "unit": "MT",
        },
        "effluent": {
            "fixed": round(effluent_fixed, 2),
            "process": round(effluent_process, 2),
            "u4u": round(effluent_u4u, 2),
            "total": round(effluent_total, 2),
            "unit": "M3",
        },
    }


def print_demand_summary(demands: dict):
    """
    Print detailed demand breakdown table for all utilities.
    """
    print("\n" + "="*100)
    print("UTILITY DEMAND SUMMARY (Fixed + Process + U4U = Total)")
    print("="*100)
    print(f"  {'Utility':<20} {'Unit':<8} {'Fixed':>15} {'Process':>15} {'U4U':>15} {'Total':>15}")
    print(f"  {'-'*88}")
    
    utilities = [
        ("Power", "power"),
        ("SHP Steam", "steam_shp"),
        ("HP Steam", "steam_hp"),
        ("MP Steam", "steam_mp"),
        ("LP Steam", "steam_lp"),
        ("BFW", "bfw"),
        ("DM Water", "dm_water"),
        ("Cooling Water 1", "cw1"),
        ("Cooling Water 2", "cw2"),
        ("Compressed Air", "compressed_air"),
        ("Oxygen", "oxygen"),
        ("Effluent", "effluent"),
    ]
    
    for name, key in utilities:
        d = demands.get(key, {})
        unit = d.get("unit", "")
        fixed = d.get("fixed", 0)
        process = d.get("process", 0)
        u4u = d.get("u4u", 0)
        total = d.get("total", 0)
        print(f"  {name:<20} {unit:<8} {fixed:>15,.2f} {process:>15,.2f} {u4u:>15,.2f} {total:>15,.2f}")
    
    print(f"  {'-'*88}")
    print("="*100)
    
    # Print U4U Power breakdown
    power_u4u = demands.get("power", {}).get("u4u_detail", {})
    if power_u4u:
        print("\n  U4U POWER BREAKDOWN:")
        print(f"  {'-'*60}")
        
        # Power Aux
        aux = power_u4u.get("power_aux", {})
        print(f"    Power Plant Auxiliary:")
        print(f"      GT1 Aux:           {aux.get('gt1_kwh', 0):>15,.2f} KWH")
        print(f"      GT2 Aux:           {aux.get('gt2_kwh', 0):>15,.2f} KWH")
        print(f"      GT3 Aux:           {aux.get('gt3_kwh', 0):>15,.2f} KWH")
        print(f"      STG Aux:           {aux.get('stg_kwh', 0):>15,.2f} KWH")
        print(f"      Subtotal:          {aux.get('total_kwh', 0):>15,.2f} KWH ({aux.get('total_mwh', 0):,.2f} MWH)")
        
        # Utility Power
        util = power_u4u.get("utility_power", {})
        print(f"    Utility Power:")
        print(f"      BFW Power:         {util.get('bfw_kwh', 0):>15,.2f} KWH")
        print(f"      DM Power:          {util.get('dm_kwh', 0):>15,.2f} KWH")
        print(f"      CW1 Power:         {util.get('cw1_kwh', 0):>15,.2f} KWH")
        print(f"      CW2 Power:         {util.get('cw2_kwh', 0):>15,.2f} KWH")
        print(f"      Air Power:         {util.get('air_kwh', 0):>15,.2f} KWH")
        print(f"      Oxygen Power:      {util.get('oxygen_kwh', 0):>15,.2f} KWH")
        print(f"      Effluent Power:    {util.get('effluent_kwh', 0):>15,.2f} KWH")
        print(f"      Subtotal:          {util.get('total_kwh', 0):>15,.2f} KWH ({util.get('total_mwh', 0):,.2f} MWH)")
        
        print(f"  {'-'*60}")
        print(f"    TOTAL U4U POWER:     {power_u4u.get('total_u4u_kwh', 0):>15,.2f} KWH ({power_u4u.get('total_u4u_mwh', 0):,.2f} MWH)")
    
    print("="*100 + "\n")
