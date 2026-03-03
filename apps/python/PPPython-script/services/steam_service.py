"""
Steam Balance Calculation Service
Calculates LP, MP, HP, and SHP steam requirements using norm factors.

Flow: LP Balance → MP Balance → HP Balance → SHP Balance

HRSG-GT Linkage:
- HRSG1 availability = GT1 availability
- HRSG2 availability = GT2 availability  
- HRSG3 availability = GT3 availability
"""

from itertools import groupby

# ============================================================
# NORM FACTORS (Hardcoded - will move to DB later)
# ============================================================

# LP Steam Distribution (who supplies LP)
NORM_LP_FROM_PRDS = 0.3866      # 38.66% of LP from PRDS
NORM_LP_FROM_STG = 0.6134       # 61.34% of LP from STG

# MP Steam Distribution (who supplies MP)
NORM_MP_FROM_PRDS = 0.7092      # 70.92% of MP from PRDS SHP
NORM_MP_FROM_STG = 0.2908       # 29.08% of MP from STG

# HP Steam Distribution (who supplies HP)
NORM_HP_FROM_PRDS = 1.0000      # 100% of HP from PRDS

# SHP Steam Distribution (who supplies SHP) - for HRSG allocation
NORM_SHP_FROM_HRSG2 = 0.4934    # 49.34% from HRSG2
NORM_SHP_FROM_HRSG3 = 0.5066    # 50.66% from HRSG3

# Conversion Factors - What each supplier needs
NORM_SHP_PER_LP_STG = 0.4800    # 0.48 MT SHP per MT LP from STG
NORM_MP_PER_LP_PRDS = 0.7500    # 0.75 MT MP per MT LP from PRDS
NORM_BFW_PER_LP_PRDS = 0.2500   # 0.25 M3 BFW per MT LP from PRDS

NORM_SHP_PER_MP_STG = 0.6900    # 0.69 MT SHP per MT MP from STG
NORM_SHP_PER_MP_PRDS = 0.9100   # 0.91 MT SHP per MT MP from PRDS
NORM_BFW_PER_MP_PRDS = 0.0900   # 0.09 M3 BFW per MT MP from PRDS

NORM_SHP_PER_HP_PRDS = 0.9232   # 0.9232 MT SHP per MT HP from PRDS
NORM_BFW_PER_HP_PRDS = 0.0768   # 0.0768 M3 BFW per MT HP from PRDS

# BFW to LP conversion (for UFU)
NORM_LP_PER_BFW = 0.1450        # 0.145 MT LP per M3 BFW

# STG Power Generation Requirements
# To generate 1 KWh from STG, we need:
NORM_STG_POWER_PER_KWH = 0.0020   # 0.0020 KWh of power per 1 KWh generated (aux consumption)
NORM_STG_SHP_PER_KWH = 0.0036     # 0.0036 MT of SHP steam per 1 KWh generated

# Steam to Power Conversion (for excess steam handling)
# 3.56 MT of SHP steam = 1 MWh of power from STG
STEAM_TO_POWER_MT_PER_MWH = 3.56  # MT of SHP steam per MWh of power


# ============================================================
# LP STEAM BALANCE (Legacy - Fixed Ratio)
# ============================================================
def calculate_lp_balance(
    lp_process: float,
    lp_fixed: float,
    bfw_ufu: float = 0.0
) -> dict:
    """
    Calculate LP Steam Balance using FIXED ratios (legacy).
    
    Args:
        lp_process: Process LP requirement (MT)
        lp_fixed: Fixed LP requirement (MT)
        bfw_ufu: BFW for UFU (M3) - generates additional LP demand
    
    Returns:
        dict with LP balance details and downstream requirements
    """
    # Step 1: Calculate LP from UFU (BFW)
    lp_ufu = bfw_ufu * NORM_LP_PER_BFW
    
    # Step 2: Total LP Demand
    lp_total = lp_process + lp_fixed + lp_ufu
    
    # Step 3: Split LP between suppliers (FIXED RATIO)
    lp_from_prds = lp_total * NORM_LP_FROM_PRDS
    lp_from_stg = lp_total * NORM_LP_FROM_STG
    
    # Step 4: Calculate what each supplier needs
    # STG LP needs SHP
    shp_for_stg_lp = lp_from_stg * NORM_SHP_PER_LP_STG
    
    # PRDS LP needs MP and BFW
    mp_for_prds_lp = lp_from_prds * NORM_MP_PER_LP_PRDS
    bfw_for_prds_lp = lp_from_prds * NORM_BFW_PER_LP_PRDS
    
    return {
        "lp_process": round(lp_process, 2),
        "lp_fixed": round(lp_fixed, 2),
        "lp_ufu": round(lp_ufu, 2),
        "lp_total": round(lp_total, 2),
        "lp_from_prds": round(lp_from_prds, 2),
        "lp_from_stg": round(lp_from_stg, 2),
        "shp_for_stg_lp": round(shp_for_stg_lp, 2),
        "mp_for_prds_lp": round(mp_for_prds_lp, 2),
        "bfw_for_prds_lp": round(bfw_for_prds_lp, 2),
        "lp_stg_ratio": round(NORM_LP_FROM_STG, 4),
        "lp_prds_ratio": round(NORM_LP_FROM_PRDS, 4),
    }


# ============================================================
# LP STEAM BALANCE (New - STG Load Based)
# ============================================================
def calculate_lp_balance_stg_based(
    lp_process: float,
    lp_fixed: float,
    bfw_ufu: float,
    stg_lp_extraction_tph: float,
    stg_operating_hours: float
) -> dict:
    """
    Calculate LP Steam Balance using STG extraction lookup.
    
    LP from STG is determined by STG load (from lookup table), not fixed ratio.
    Remaining LP demand comes from PRDS.
    
    Args:
        lp_process: Process LP requirement (MT)
        lp_fixed: Fixed LP requirement (MT)
        bfw_ufu: BFW for UFU (M3) - generates additional LP demand
        stg_lp_extraction_tph: LP extraction rate from STG (TPH) - from lookup table
        stg_operating_hours: STG operating hours for the month
    
    Returns:
        dict with LP balance details and downstream requirements
    """
    # Step 1: Calculate LP from UFU (BFW)
    lp_ufu = bfw_ufu * NORM_LP_PER_BFW
    
    # Step 2: Total LP Demand
    lp_total = lp_process + lp_fixed + lp_ufu
    
    # Step 3: Calculate LP from STG based on extraction rate
    # LP from STG = Extraction Rate (TPH) × Operating Hours
    lp_from_stg_available = stg_lp_extraction_tph * stg_operating_hours
    
    # Cap at demand (can't extract more than needed)
    lp_from_stg = min(lp_from_stg_available, lp_total)
    
    # Track excess (if STG can provide more than demand)
    lp_stg_excess = max(0, lp_from_stg_available - lp_total)
    
    # Remaining LP comes from PRDS
    lp_from_prds = max(0, lp_total - lp_from_stg)
    
    # Calculate actual ratios (for norm storage)
    lp_stg_ratio = lp_from_stg / lp_total if lp_total > 0 else 0
    lp_prds_ratio = lp_from_prds / lp_total if lp_total > 0 else 0
    
    # Step 4: Calculate what each supplier needs
    # STG LP needs SHP
    shp_for_stg_lp = lp_from_stg * NORM_SHP_PER_LP_STG
    
    # PRDS LP needs MP and BFW
    mp_for_prds_lp = lp_from_prds * NORM_MP_PER_LP_PRDS
    bfw_for_prds_lp = lp_from_prds * NORM_BFW_PER_LP_PRDS
    
    return {
        "lp_process": round(lp_process, 2),
        "lp_fixed": round(lp_fixed, 2),
        "lp_ufu": round(lp_ufu, 2),
        "lp_total": round(lp_total, 2),
        "lp_from_prds": round(lp_from_prds, 2),
        "lp_from_stg": round(lp_from_stg, 2),
        "lp_from_stg_available": round(lp_from_stg_available, 2),
        "lp_stg_excess": round(lp_stg_excess, 2),
        "shp_for_stg_lp": round(shp_for_stg_lp, 2),
        "mp_for_prds_lp": round(mp_for_prds_lp, 2),
        "bfw_for_prds_lp": round(bfw_for_prds_lp, 2),
        "lp_stg_ratio": round(lp_stg_ratio, 4),
        "lp_prds_ratio": round(lp_prds_ratio, 4),
        "stg_lp_extraction_tph": round(stg_lp_extraction_tph, 2),
        "stg_operating_hours": round(stg_operating_hours, 2),
    }


# ============================================================
# MP STEAM BALANCE (Legacy - Fixed Ratio)
# ============================================================
def calculate_mp_balance(
    mp_process: float,
    mp_fixed: float,
    mp_for_lp: float = 0.0
) -> dict:
    """
    Calculate MP Steam Balance using FIXED ratios (legacy).
    
    Args:
        mp_process: Process MP requirement (MT)
        mp_fixed: Fixed MP requirement (MT)
        mp_for_lp: MP required for LP PRDS (from LP balance)
    
    Returns:
        dict with MP balance details and downstream requirements
    """
    # Step 1: Total MP Demand
    mp_total = mp_process + mp_fixed + mp_for_lp
    
    # Step 2: Split MP between suppliers (FIXED RATIO)
    mp_from_prds = mp_total * NORM_MP_FROM_PRDS
    mp_from_stg = mp_total * NORM_MP_FROM_STG
    
    # Step 3: Calculate what each supplier needs
    # STG MP needs SHP
    shp_for_stg_mp = mp_from_stg * NORM_SHP_PER_MP_STG
    
    # PRDS MP needs SHP and BFW
    shp_for_prds_mp = mp_from_prds * NORM_SHP_PER_MP_PRDS
    bfw_for_prds_mp = mp_from_prds * NORM_BFW_PER_MP_PRDS
    
    # Total SHP from MP chain
    shp_from_mp_chain = shp_for_stg_mp + shp_for_prds_mp
    
    return {
        "mp_process": round(mp_process, 2),
        "mp_fixed": round(mp_fixed, 2),
        "mp_for_lp": round(mp_for_lp, 2),
        "mp_total": round(mp_total, 2),
        "mp_from_prds": round(mp_from_prds, 2),
        "mp_from_stg": round(mp_from_stg, 2),
        "shp_for_stg_mp": round(shp_for_stg_mp, 2),
        "shp_for_prds_mp": round(shp_for_prds_mp, 2),
        "bfw_for_prds_mp": round(bfw_for_prds_mp, 2),
        "shp_from_mp_chain": round(shp_from_mp_chain, 2),
        "mp_stg_ratio": round(NORM_MP_FROM_STG, 4),
        "mp_prds_ratio": round(NORM_MP_FROM_PRDS, 4),
    }


# ============================================================
# MP STEAM BALANCE (New - STG Load Based)
# ============================================================
def calculate_mp_balance_stg_based(
    mp_process: float,
    mp_fixed: float,
    mp_for_lp: float,
    stg_mp_extraction_tph: float,
    stg_operating_hours: float
) -> dict:
    """
    Calculate MP Steam Balance using STG extraction lookup.
    
    MP from STG is determined by STG load (from lookup table), not fixed ratio.
    Remaining MP demand comes from PRDS.
    
    Args:
        mp_process: Process MP requirement (MT)
        mp_fixed: Fixed MP requirement (MT)
        mp_for_lp: MP required for LP PRDS (from LP balance)
        stg_mp_extraction_tph: MP extraction rate from STG (TPH) - from lookup table
        stg_operating_hours: STG operating hours for the month
    
    Returns:
        dict with MP balance details and downstream requirements
    """
    # Step 1: Total MP Demand
    mp_total = mp_process + mp_fixed + mp_for_lp
    
    # Step 2: Calculate MP from STG based on extraction rate
    # MP from STG = Extraction Rate (TPH) × Operating Hours
    mp_from_stg_available = stg_mp_extraction_tph * stg_operating_hours
    
    # Cap at demand (can't extract more than needed)
    mp_from_stg = min(mp_from_stg_available, mp_total)
    
    # Track excess (if STG can provide more than demand)
    mp_stg_excess = max(0, mp_from_stg_available - mp_total)
    
    # Remaining MP comes from PRDS
    mp_from_prds = max(0, mp_total - mp_from_stg)
    
    # Calculate actual ratios (for norm storage)
    mp_stg_ratio = mp_from_stg / mp_total if mp_total > 0 else 0
    mp_prds_ratio = mp_from_prds / mp_total if mp_total > 0 else 0
    
    # Step 3: Calculate what each supplier needs
    # STG MP needs SHP
    shp_for_stg_mp = mp_from_stg * NORM_SHP_PER_MP_STG
    
    # PRDS MP needs SHP and BFW
    shp_for_prds_mp = mp_from_prds * NORM_SHP_PER_MP_PRDS
    bfw_for_prds_mp = mp_from_prds * NORM_BFW_PER_MP_PRDS
    
    # Total SHP from MP chain
    shp_from_mp_chain = shp_for_stg_mp + shp_for_prds_mp
    
    return {
        "mp_process": round(mp_process, 2),
        "mp_fixed": round(mp_fixed, 2),
        "mp_for_lp": round(mp_for_lp, 2),
        "mp_total": round(mp_total, 2),
        "mp_from_prds": round(mp_from_prds, 2),
        "mp_from_stg": round(mp_from_stg, 2),
        "mp_from_stg_available": round(mp_from_stg_available, 2),
        "mp_stg_excess": round(mp_stg_excess, 2),
        "shp_for_stg_mp": round(shp_for_stg_mp, 2),
        "shp_for_prds_mp": round(shp_for_prds_mp, 2),
        "bfw_for_prds_mp": round(bfw_for_prds_mp, 2),
        "shp_from_mp_chain": round(shp_from_mp_chain, 2),
        "mp_stg_ratio": round(mp_stg_ratio, 4),
        "mp_prds_ratio": round(mp_prds_ratio, 4),
        "stg_mp_extraction_tph": round(stg_mp_extraction_tph, 2),
        "stg_operating_hours": round(stg_operating_hours, 2),
    }


# ============================================================
# HP STEAM BALANCE
# ============================================================
def calculate_hp_balance(
    hp_process: float,
    hp_fixed: float = 0.0
) -> dict:
    """
    Calculate HP Steam Balance.
    
    Args:
        hp_process: Process HP requirement (MT)
        hp_fixed: Fixed HP requirement (MT)
    
    Returns:
        dict with HP balance details and downstream requirements
    """
    # Step 1: Total HP Demand
    hp_total = hp_process + hp_fixed
    
    # Step 2: All HP comes from PRDS
    hp_from_prds = hp_total * NORM_HP_FROM_PRDS
    
    # Step 3: Calculate what PRDS needs
    shp_for_hp_prds = hp_from_prds * NORM_SHP_PER_HP_PRDS
    bfw_for_hp_prds = hp_from_prds * NORM_BFW_PER_HP_PRDS
    
    return {
        "hp_process": round(hp_process, 2),
        "hp_fixed": round(hp_fixed, 2),
        "hp_total": round(hp_total, 2),
        "hp_from_prds": round(hp_from_prds, 2),
        "shp_for_hp_prds": round(shp_for_hp_prds, 2),
        "bfw_for_hp_prds": round(bfw_for_hp_prds, 2),
    }


# ============================================================
# SHP STEAM BALANCE
# ============================================================
def calculate_shp_balance(
    shp_process: float,
    shp_fixed: float,
    shp_for_stg_lp: float,
    shp_from_mp_chain: float,
    shp_for_hp_prds: float,
    stg_shp_power: float = 0.0
) -> dict:
    """
    Calculate SHP Steam Balance.
    
    Args:
        shp_process: Process SHP requirement (MT)
        shp_fixed: Fixed SHP requirement (MT)
        shp_for_stg_lp: SHP required for LP via STG (from LP balance)
        shp_from_mp_chain: SHP required for MP chain (from MP balance)
        shp_for_hp_prds: SHP required for HP via PRDS (from HP balance)
        stg_shp_power: SHP used as inlet steam for STG power generation
    
    Returns:
        dict with SHP balance details
    """
    # SHP from headers (LP + MP + HP)
    shp_from_headers = shp_for_stg_lp + shp_from_mp_chain + shp_for_hp_prds
    
    # Total SHP demand (excluding STG power)
    shp_total_without_power = shp_process + shp_fixed + shp_from_headers
    
    # Total SHP demand (including STG power)
    shp_total_demand = shp_total_without_power + stg_shp_power
    
    # HRSG allocation (for reference)
    shp_from_hrsg2 = shp_total_demand * NORM_SHP_FROM_HRSG2
    shp_from_hrsg3 = shp_total_demand * NORM_SHP_FROM_HRSG3
    
    return {
        "shp_process": round(shp_process, 2),
        "shp_fixed": round(shp_fixed, 2),
        "shp_for_stg_lp": round(shp_for_stg_lp, 2),
        "shp_from_mp_chain": round(shp_from_mp_chain, 2),
        "shp_for_hp_prds": round(shp_for_hp_prds, 2),
        "shp_from_headers": round(shp_from_headers, 2),
        "shp_total_without_power": round(shp_total_without_power, 2),
        "stg_shp_power": round(stg_shp_power, 2),
        "shp_total_demand": round(shp_total_demand, 2),
        "shp_from_hrsg2": round(shp_from_hrsg2, 2),
        "shp_from_hrsg3": round(shp_from_hrsg3, 2),
    }


# ============================================================
# TOTAL BFW REQUIREMENT
# ============================================================
def calculate_total_bfw(
    bfw_for_prds_lp: float,
    bfw_for_prds_mp: float,
    bfw_for_hp_prds: float,
    bfw_ufu: float = 0.0
) -> dict:
    """
    Calculate total BFW requirement from all sources.
    """
    total_bfw = bfw_for_prds_lp + bfw_for_prds_mp + bfw_for_hp_prds + bfw_ufu
    
    return {
        "bfw_for_prds_lp": round(bfw_for_prds_lp, 2),
        "bfw_for_prds_mp": round(bfw_for_prds_mp, 2),
        "bfw_for_hp_prds": round(bfw_for_hp_prds, 2),
        "bfw_ufu": round(bfw_ufu, 2),
        "total_bfw": round(total_bfw, 2),
    }


# ============================================================
# MAIN: COMPLETE STEAM BALANCE CALCULATION
# ============================================================
def calculate_steam_balance(
    lp_process: float,
    lp_fixed: float,
    mp_process: float,
    mp_fixed: float,
    hp_process: float,
    hp_fixed: float,
    shp_process: float,
    shp_fixed: float,
    bfw_ufu: float = 0.0,
    stg_shp_power: float = 0.0
) -> dict:
    """
    Calculate complete steam balance for all headers.
    
    Args:
        lp_process, lp_fixed: LP Steam demands (MT)
        mp_process, mp_fixed: MP Steam demands (MT)
        hp_process, hp_fixed: HP Steam demands (MT)
        shp_process, shp_fixed: SHP Steam demands (MT)
        bfw_ufu: BFW for UFU (M3)
        stg_shp_power: SHP for STG power generation (MT)
    
    Returns:
        dict with complete steam balance for all headers
    """
    # Step 1: LP Balance
    lp = calculate_lp_balance(lp_process, lp_fixed, bfw_ufu)
    
    # Step 2: MP Balance (uses mp_for_lp from LP balance)
    mp = calculate_mp_balance(mp_process, mp_fixed, lp["mp_for_prds_lp"])
    
    # Step 3: HP Balance
    hp = calculate_hp_balance(hp_process, hp_fixed)
    
    # Step 4: SHP Balance (uses outputs from LP, MP, HP)
    shp = calculate_shp_balance(
        shp_process=shp_process,
        shp_fixed=shp_fixed,
        shp_for_stg_lp=lp["shp_for_stg_lp"],
        shp_from_mp_chain=mp["shp_from_mp_chain"],
        shp_for_hp_prds=hp["shp_for_hp_prds"],
        stg_shp_power=stg_shp_power
    )
    
    # Step 5: Total BFW
    bfw = calculate_total_bfw(
        bfw_for_prds_lp=lp["bfw_for_prds_lp"],
        bfw_for_prds_mp=mp["bfw_for_prds_mp"],
        bfw_for_hp_prds=hp["bfw_for_hp_prds"],
        bfw_ufu=bfw_ufu
    )
    
    return {
        "lp_balance": lp,
        "mp_balance": mp,
        "hp_balance": hp,
        "shp_balance": shp,
        "bfw_requirement": bfw,
        "summary": {
            "total_lp_demand": lp["lp_total"],
            "total_mp_demand": mp["mp_total"],
            "total_hp_demand": hp["hp_total"],
            "total_shp_demand": shp["shp_total_demand"],
            "total_bfw_demand": bfw["total_bfw"],
        }
    }


# ============================================================
# HRSG ASSET CONFIGURATION (Now loaded from DB)
# ============================================================
# Default fallback values (used if DB fetch fails)
HRSG_ASSETS_DEFAULT = {
    "HRSG1": {
        "min_capacity_mt": 60.0,
        "max_capacity_mt": 136.0,
        "efficiency": 1.03,
        "steam_type": "SHP",
        "linked_gt": "GT1",
    },
    "HRSG2": {
        "min_capacity_mt": 60.0,
        "max_capacity_mt": 136.0,
        "efficiency": 1.03,
        "steam_type": "SHP",
        "linked_gt": "GT2",
    },
    "HRSG3": {
        "min_capacity_mt": 60.0,
        "max_capacity_mt": 136.0,
        "efficiency": 1.03,
        "steam_type": "SHP",
        "linked_gt": "GT3",
    },
}

# Global HRSG_ASSETS - will be populated from DB
HRSG_ASSETS = HRSG_ASSETS_DEFAULT.copy()
HRSG_ASSETS_LOADED = False  # Track if assets have been loaded from DB


def load_hrsg_assets_from_db():
    """
    Load HRSG assets from SteamGenerationAssets table.
    Updates the global HRSG_ASSETS dictionary.
    
    Returns:
        dict: HRSG assets configuration
    """
    global HRSG_ASSETS, HRSG_ASSETS_LOADED
    
    try:
        from database.connection import get_connection
        conn = get_connection()
        cur = conn.cursor()
        
        # Fetch HRSG assets with linked GT info
        cur.execute("""
            SELECT 
                s.AssetName,
                s.MinCapacityMT,
                s.MaxCapacityMT,
                s.Efficiency,
                s.SteamType,
                s.LinkedPowerAssetId,
                p.AssetName as LinkedGTName
            FROM SteamGenerationAssets s
            LEFT JOIN PowerGenerationAssets p ON s.LinkedPowerAssetId = p.AssetId
            WHERE s.AssetType = 'HRSG'
            ORDER BY s.AssetName
        """)
        
        rows = cur.fetchall()
        conn.close()
        
        if not rows:
            print("  [HRSG] No HRSG assets found in DB, using defaults")
            return HRSG_ASSETS
        
        # Build HRSG_ASSETS from DB
        hrsg_assets = {}
        for row in rows:
            hrsg_name = row[0]  # e.g., "HRSG1"
            linked_gt_name = row[6]  # e.g., "NMD-Power Plant-1"
            
            # Determine linked GT pattern (GT1, GT2, GT3)
            linked_gt = None
            if linked_gt_name:
                if "Plant-1" in linked_gt_name:
                    linked_gt = "GT1"
                elif "Plant-2" in linked_gt_name:
                    linked_gt = "GT2"
                elif "Plant-3" in linked_gt_name:
                    linked_gt = "GT3"
            
            hrsg_assets[hrsg_name] = {
                "min_capacity_mt": float(row[1]) if row[1] else 60.0,
                "max_capacity_mt": float(row[2]) if row[2] else 136.0,
                "efficiency": float(row[3]) if row[3] else 1.03,
                "steam_type": row[4] if row[4] else "SHP",
                "linked_gt": linked_gt or hrsg_name.replace("HRSG", "GT"),
            }
        
        HRSG_ASSETS = hrsg_assets
        HRSG_ASSETS_LOADED = True
        print(f"  [HRSG] Loaded {len(hrsg_assets)} HRSG assets from DB")
        return HRSG_ASSETS
        
    except Exception as e:
        print(f"  [HRSG] Error loading from DB: {e}, using defaults")
        HRSG_ASSETS_LOADED = True  # Mark as loaded even on error to prevent retries
        return HRSG_ASSETS


def get_hrsg_assets():
    """Get HRSG assets, loading from DB if not already loaded."""
    global HRSG_ASSETS_LOADED
    if not HRSG_ASSETS_LOADED:
        load_hrsg_assets_from_db()
    return HRSG_ASSETS

# ============================================================
# NOTE: AUXILIARY BOILERS AND CCPP BOILERS
# ============================================================
# Per client confirmation (Dec 2024):
# - Currently NO Aux Boilers or CCPP Boilers in the plant
# - Only HRSGs are available for SHP steam generation
# - If these assets are added in future, configure them here
# ============================================================


# ============================================================
# GET STG REQUIREMENTS FROM POWER DISPATCH
# ============================================================
def get_stg_requirements_from_dispatch(power_dispatch: list) -> dict:
    """
    Get STG power and SHP steam requirements from power dispatch results.
    
    The STG requirements are now calculated in power_service.py and included
    in the dispatch results as STG_SHP_Required_MT and STG_Power_Required_MWh.
    
    STG Requirements per 1 KWh generated:
    - Power: 0.0020 KWh (auxiliary consumption)
    - SHP Steam: 0.0036 MT
    
    Args:
        power_dispatch: List of dispatch results from power_service
        
    Returns:
        dict with STG requirements
    """
    stg_gross_mwh = 0.0
    stg_shp_required = 0.0
    stg_power_required = 0.0
    
    # Find STG in dispatch and get pre-calculated values
    for asset in power_dispatch:
        asset_name = asset.get("AssetName", "").upper()
        if "STG" in asset_name or "STEAM TURBINE" in asset_name:
            stg_gross_mwh = asset.get("GrossMWh", 0)
            stg_shp_required = asset.get("STG_SHP_Required_MT") or 0.0
            stg_power_required = asset.get("STG_Power_Required_MWh") or 0.0
            break
    
    stg_gross_kwh = stg_gross_mwh * 1000  # Convert MWh to KWh
    
    return {
        "stg_gross_mwh": round(stg_gross_mwh, 2),
        "stg_gross_kwh": round(stg_gross_kwh, 2),
        "power_norm": NORM_STG_POWER_PER_KWH,
        "shp_norm": NORM_STG_SHP_PER_KWH,
        "power_required_kwh": round(stg_power_required * 1000, 2),
        "power_required_mwh": round(stg_power_required, 2),
        "shp_steam_required_mt": round(stg_shp_required, 2),
    }


# ============================================================
# CALCULATE FREE STEAM FROM GT DISPATCH
# ============================================================
def calculate_free_steam_from_dispatch(power_dispatch: list) -> dict:
    """
    Calculate free steam generated by each GT based on dispatch results.
    
    Calculation Steps:
    1. GT Load (MW) = GrossMWh / Hours
    2. FreeSteamFactor = Lookup from CPP_GTHeatRate table based on GT Load
       (This is already done in power_service and passed in dispatch)
    3. Free Steam (MT) = GrossMWh × FreeSteamFactor
    
    Args:
        power_dispatch: List of dispatch results from power_service
        
    Returns:
        dict with free steam details per GT and total
    """
    free_steam_details = {}
    total_free_steam = 0.0
    
    for asset in power_dispatch:
        asset_name = asset.get("AssetName", "")
        gross_mwh = asset.get("GrossMWh", 0)
        hours = asset.get("Hours", 0)
        load_mw = asset.get("LoadMW", 0)
        free_steam_factor = asset.get("FreeSteam")  # FreeSteamFactor from CPP_GTHeatRate
        
        # Only GTs generate free steam (not STG)
        is_gt = any(gt in asset_name.upper() for gt in ["GT1", "GT2", "GT3", "PLANT 1", "PLANT 2", "PLANT 3"])
        
        if is_gt and gross_mwh > 0 and free_steam_factor is not None:
            # Verify load calculation: LoadMW = GrossMWh / Hours
            calculated_load = gross_mwh / hours if hours > 0 else 0
            
            # Free Steam = GrossMWh × FreeSteamFactor
            free_steam_mt = gross_mwh * free_steam_factor
            
            free_steam_details[asset_name] = {
                "gross_mwh": round(gross_mwh, 2),
                "hours": hours,
                "load_mw": round(load_mw, 2),
                "calculated_load_mw": round(calculated_load, 2),
                "free_steam_factor": round(free_steam_factor, 4),
                "free_steam_mt": round(free_steam_mt, 2),
            }
            total_free_steam += free_steam_mt
        elif is_gt:
            # GT not running or no free steam factor
            free_steam_details[asset_name] = {
                "gross_mwh": round(gross_mwh, 2),
                "hours": hours,
                "load_mw": round(load_mw, 2) if load_mw else 0,
                "calculated_load_mw": 0,
                "free_steam_factor": free_steam_factor,
                "free_steam_mt": 0.0,
            }
    
    return {
        "details": free_steam_details,
        "total_free_steam_mt": round(total_free_steam, 2),
    }


# ============================================================
# GET HRSG AVAILABILITY FROM POWER DISPATCH
# ============================================================
def get_hrsg_availability_from_dispatch(power_dispatch: list) -> dict:
    """
    Determine HRSG availability based on GT dispatch results.
    
    Args:
        power_dispatch: List of dispatch results from power_service
        
    Returns:
        dict with HRSG availability and operational hours
    """
    hrsg_availability = {}
    hrsg_assets = get_hrsg_assets()  # Load from DB
    
    for hrsg_name, hrsg_config in hrsg_assets.items():
        linked_gt = hrsg_config["linked_gt"]
        
        # Find the linked GT in dispatch results
        gt_dispatch = None
        for asset in power_dispatch:
            asset_name = asset.get("AssetName", "").upper()
            # Match GT1, GT2, GT3 or Power Plant-1, Plant-2, Plant-3
            # linked_gt is "GT1", "GT2", "GT3" - extract the number
            gt_num = linked_gt[-1]  # "1", "2", "3"
            if (linked_gt in asset_name or 
                f"PLANT-{gt_num}" in asset_name or 
                f"PLANT {gt_num}" in asset_name):
                gt_dispatch = asset
                break
        
        if gt_dispatch and gt_dispatch.get("GrossMWh", 0) > 0:
            # GT is running, HRSG is available
            gross_mwh = gt_dispatch.get("GrossMWh", 0)
            free_steam_factor = gt_dispatch.get("FreeSteam")
            
            # Calculate free steam for this HRSG
            free_steam_mt = 0.0
            if free_steam_factor is not None:
                free_steam_mt = free_steam_factor * gross_mwh
            
            hrsg_availability[hrsg_name] = {
                "is_available": True,
                "operational_hours": gt_dispatch.get("Hours", 0),
                "gt_load_mw": gt_dispatch.get("LoadMW", 0),
                "gt_gross_mwh": gross_mwh,
                "free_steam_factor": free_steam_factor,
                "free_steam_mt": round(free_steam_mt, 2),
                "min_capacity_mt": hrsg_config["min_capacity_mt"],
                "max_capacity_mt": hrsg_config["max_capacity_mt"],
                "efficiency": hrsg_config["efficiency"],
            }
        else:
            # GT not running, HRSG unavailable
            hrsg_availability[hrsg_name] = {
                "is_available": False,
                "operational_hours": 0,
                "gt_load_mw": 0,
                "gt_gross_mwh": 0,
                "free_steam_factor": None,
                "free_steam_mt": 0.0,
                "min_capacity_mt": hrsg_config["min_capacity_mt"],
                "max_capacity_mt": hrsg_config["max_capacity_mt"],
                "efficiency": hrsg_config["efficiency"],
            }
    
    return hrsg_availability


# ============================================================
# CALCULATE SHP GENERATION CAPACITY FROM HRSG
# ============================================================
def calculate_shp_generation_capacity(hrsg_availability: dict) -> dict:
    """
    Calculate total SHP generation capacity from available HRSGs.
    
    SHP GENERATION MODEL (per client flowchart):
    =============================================
    Total SHP = Free Steam + Supplementary Firing
    
    1. FREE STEAM (from GT exhaust):
       - Generated automatically when GT runs
       - Free Steam = GT_Gross_MWh × FreeSteamFactor
       - No extra fuel cost
       
    2. SUPPLEMENTARY FIRING (HRSG capacity):
       - Additional steam from HRSG burners
       - Capacity: Min 60 MT/hr, Max 136 MT/hr per HRSG
       - Requires fuel
       - Monthly capacity = Hours × Max_Capacity × Efficiency
    
    HRSG Utility-for-Utility Requirements (per MT SHP produced):
    - BFW: 1.024 M³
    - LP Steam: -0.050352 MT (negative = produces LP as byproduct)
    - Water: 0.00266 M³
    - Chemicals: Trisodium Phosphate 0.0009 KG
    
    Args:
        hrsg_availability: dict from get_hrsg_availability_from_dispatch
        
    Returns:
        dict with SHP generation capacity details
    """
    total_free_steam = 0.0
    total_supplementary_min = 0.0
    total_supplementary_max = 0.0
    available_hrsgs = []
    unavailable_hrsgs = []
    
    hrsg_details = []
    
    for hrsg_name, hrsg_data in hrsg_availability.items():
        if hrsg_data["is_available"]:
            hours = hrsg_data["operational_hours"]
            
            # 1. FREE STEAM from GT exhaust
            free_steam_mt = hrsg_data.get("free_steam_mt", 0.0)
            total_free_steam += free_steam_mt
            
            # 2. SUPPLEMENTARY FIRING capacity from HRSG
            min_capacity_per_hr = hrsg_data["min_capacity_mt"]  # 60 MT/hr
            max_capacity_per_hr = hrsg_data["max_capacity_mt"]  # 136 MT/hr
            efficiency = hrsg_data["efficiency"]  # 103%
            
            # Monthly supplementary capacity = Hours × Capacity × Efficiency
            supp_min_month = min_capacity_per_hr * hours * efficiency
            supp_max_month = max_capacity_per_hr * hours * efficiency
            
            total_supplementary_min += supp_min_month
            total_supplementary_max += supp_max_month
            available_hrsgs.append(hrsg_name)
            
            hrsg_details.append({
                "name": hrsg_name,
                "is_available": True,
                "hours": hours,
                "gt_load_mw": hrsg_data["gt_load_mw"],
                "gt_gross_mwh": hrsg_data.get("gt_gross_mwh", 0),
                "free_steam_factor": hrsg_data.get("free_steam_factor"),
                "free_steam_mt": round(free_steam_mt, 2),
                "min_capacity_per_hr": min_capacity_per_hr,
                "max_capacity_per_hr": max_capacity_per_hr,
                "efficiency": efficiency,
                "supp_min_mt_month": round(supp_min_month, 2),
                "supp_max_mt_month": round(supp_max_month, 2),
            })
        else:
            unavailable_hrsgs.append(hrsg_name)
            hrsg_details.append({
                "name": hrsg_name,
                "is_available": False,
                "hours": 0,
                "gt_load_mw": 0,
                "gt_gross_mwh": 0,
                "free_steam_factor": None,
                "free_steam_mt": 0.0,
                "min_capacity_per_hr": hrsg_data["min_capacity_mt"],
                "max_capacity_per_hr": hrsg_data["max_capacity_mt"],
                "efficiency": hrsg_data["efficiency"],
                "supp_min_mt_month": 0,
                "supp_max_mt_month": 0,
            })
    
    # Total SHP capacity = Only Supplementary Firing (Free Steam is display only)
    # Free steam is NOT included in total HRSG generation for balance calculation
    total_min_shp = total_supplementary_min  # Exclude free steam
    total_max_shp = total_supplementary_max  # Exclude free steam
    
    return {
        "total_free_steam_mt": round(total_free_steam, 2),
        "total_supplementary_min_mt": round(total_supplementary_min, 2),
        "total_supplementary_max_mt": round(total_supplementary_max, 2),
        "total_min_shp_capacity": round(total_min_shp, 2),
        "total_max_shp_capacity": round(total_max_shp, 2),
        "available_hrsgs": available_hrsgs,
        "unavailable_hrsgs": unavailable_hrsgs,
        "hrsg_details": hrsg_details,
    }


# ============================================================
# CHECK SHP SUPPLY VS DEMAND
# ============================================================
def check_shp_balance(
    shp_demand: float,
    shp_generation_capacity: dict
) -> dict:
    """
    Check if SHP generation capacity can meet demand.
    
    HRSG Capacity Model (per client):
    - HRSG has its own production capacity (Min: 60 MT/hr, Max: 136 MT/hr)
    - Monthly capacity = Hours × Max_Capacity × Efficiency
    - HRSG only runs when linked GT is running
    
    Args:
        shp_demand: Total SHP demand (MT)
        shp_generation_capacity: dict from calculate_shp_generation_capacity
        
    Returns:
        dict with balance check results
    """
    max_capacity = shp_generation_capacity["total_max_shp_capacity"]
    min_capacity = shp_generation_capacity["total_min_shp_capacity"]
    
    surplus = max_capacity - shp_demand
    deficit = shp_demand - max_capacity if shp_demand > max_capacity else 0
    
    # Check if demand can be met
    can_meet_demand = shp_demand <= max_capacity
    
    # Check if demand is above minimum (HRSGs must run at least at min)
    above_minimum = shp_demand >= min_capacity
    
    # Utilization percentage
    utilization = (shp_demand / max_capacity * 100) if max_capacity > 0 else 0
    
    return {
        "shp_demand": round(shp_demand, 2),
        "shp_max_capacity": round(max_capacity, 2),
        "shp_min_capacity": round(min_capacity, 2),
        "surplus": round(surplus, 2) if surplus > 0 else 0,
        "deficit": round(deficit, 2),
        "can_meet_demand": can_meet_demand,
        "utilization_percent": round(utilization, 2),
        "above_minimum": above_minimum,
        "utilization_percent": round((shp_demand / max_capacity) * 100, 2) if max_capacity > 0 else 0,
    }


# ============================================================
# COMPLETE STEAM BALANCE WITH HRSG CAPACITY CHECK
# ============================================================
def calculate_complete_steam_balance(
    lp_process: float,
    lp_fixed: float,
    mp_process: float,
    mp_fixed: float,
    hp_process: float,
    hp_fixed: float,
    shp_process: float,
    shp_fixed: float,
    power_dispatch: list = None,
    bfw_ufu: float = 0.0
) -> dict:
    """
    Calculate complete steam balance with HRSG capacity check.
    
    Now automatically calculates STG SHP requirement from power dispatch.
    
    Args:
        lp_process, lp_fixed: LP Steam demands (MT)
        mp_process, mp_fixed: MP Steam demands (MT)
        hp_process, hp_fixed: HP Steam demands (MT)
        shp_process, shp_fixed: SHP Steam demands (MT)
        power_dispatch: List of dispatch results from power_service (for HRSG availability & STG)
        bfw_ufu: BFW for UFU (M3)
    
    Returns:
        dict with complete steam balance and capacity check
    """
    # Step 1: Get STG requirements from power dispatch (calculated in power_service.py)
    stg_requirements = None
    stg_shp_power = 0.0
    
    if power_dispatch:
        stg_requirements = get_stg_requirements_from_dispatch(power_dispatch)
        stg_shp_power = stg_requirements["shp_steam_required_mt"]
    
    # Step 2: Calculate steam balance (demand side) - includes STG SHP requirement
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
        stg_shp_power=stg_shp_power
    )
    
    # Step 3: Get HRSG availability from power dispatch
    if power_dispatch:
        hrsg_availability = get_hrsg_availability_from_dispatch(power_dispatch)
    else:
        # Default: assume all HRSGs available with 720 hours
        hrsg_assets = get_hrsg_assets()  # Load from DB
        hrsg_availability = {
            hrsg_name: {
                "is_available": True,
                "operational_hours": 720,
                "gt_load_mw": 0,
                "gt_gross_mwh": 0,
                "free_steam_factor": None,
                "free_steam_mt": 0.0,
                "min_capacity_mt": config["min_capacity_mt"],
                "max_capacity_mt": config["max_capacity_mt"],
                "efficiency": config["efficiency"],
            }
            for hrsg_name, config in hrsg_assets.items()
        }
    
    # Step 4: Calculate SHP generation capacity
    shp_generation = calculate_shp_generation_capacity(hrsg_availability)
    
    # Step 5: Check SHP balance
    shp_demand = steam_balance["summary"]["total_shp_demand"]
    shp_balance_check = check_shp_balance(shp_demand, shp_generation)
    
    return {
        **steam_balance,
        "stg_requirements": stg_requirements,
        "hrsg_availability": hrsg_availability,
        "shp_generation_capacity": shp_generation,
        "shp_balance_check": shp_balance_check,
    }


# ============================================================
# TEST WITH EXAMPLE VALUES
# ============================================================
if __name__ == "__main__":
    # Example values based on your plant constraints
    # GT Max Capacity: 22 MW, so max generation = 22 MW × 720 hrs = 15,840 MWh
    # 
    # Example: GT1 generates 6604 MWh in 720 hours
    #   GT Load = 6604 / 720 = 9.17 MW
    #   FreeSteamFactor (from HeatRateLookup at 9.17 MW) = 1.97
    #   Free Steam = 6604 × 1.97 = 13,009.88 MT
    
    mock_power_dispatch = [
        # GT1: Not running (shutdown)
        {"AssetName": "GT1", "GrossMWh": 0, "Hours": 0, "LoadMW": 0, "FreeSteam": None,
         "STG_SHP_Required_MT": None, "STG_Power_Required_MWh": None},
        
        # GT2: Running at 6604 MWh, Load = 6604/720 = 9.17 MW
        {"AssetName": "GT2", "GrossMWh": 6604, "Hours": 720, "LoadMW": 9.17, "FreeSteam": 1.97,
         "STG_SHP_Required_MT": None, "STG_Power_Required_MWh": None},
        
        # GT3: Running at 10000 MWh, Load = 10000/720 = 13.89 MW
        {"AssetName": "GT3", "GrossMWh": 10000, "Hours": 720, "LoadMW": 13.89, "FreeSteam": 1.85,
         "STG_SHP_Required_MT": None, "STG_Power_Required_MWh": None},
        
        # STG: Requires SHP steam to generate power
        # STG generates 8000 MWh = 8,000,000 KWh
        # SHP Required = 8,000,000 × 0.0036 = 28,800 MT SHP
        # Power Required = 8,000,000 × 0.0020 / 1000 = 16 MWh
        {"AssetName": "STG", "GrossMWh": 8000, "Hours": 720, "LoadMW": 11.11, "FreeSteam": None,
         "STG_SHP_Required_MT": 28800.0, "STG_Power_Required_MWh": 16.0},
    ]
    
    # First, show free steam calculation
    print("\n" + "="*60)
    print("FREE STEAM CALCULATION FROM GT DISPATCH")
    print("="*60)
    print("\nFormula:")
    print("  1. GT Load (MW) = GrossMWh / Hours")
    print("  2. FreeSteamFactor = Lookup from HeatRateLookup table based on GT Load")
    print("  3. Free Steam (MT) = GrossMWh × FreeSteamFactor")
    
    free_steam_result = calculate_free_steam_from_dispatch(mock_power_dispatch)
    for gt_name, gt_data in free_steam_result["details"].items():
        print(f"\n{gt_name}:")
        print(f"  GrossMWh: {gt_data['gross_mwh']} MWh")
        print(f"  Hours: {gt_data['hours']} hrs")
        print(f"  GT Load: {gt_data['gross_mwh']} / {gt_data['hours']} = {gt_data['calculated_load_mw']} MW")
        print(f"  FreeSteamFactor (from lookup): {gt_data['free_steam_factor']}")
        print(f"  Free Steam: {gt_data['gross_mwh']} × {gt_data['free_steam_factor']} = {gt_data['free_steam_mt']} MT")
    
    print(f"\n  TOTAL FREE STEAM: {free_steam_result['total_free_steam_mt']} MT")
    
    # Show STG Requirements calculation (now from dispatch)
    print("\n" + "="*70)
    print("STG POWER GENERATION REQUIREMENTS (from power_service dispatch)")
    print("="*70)
    stg_req = get_stg_requirements_from_dispatch(mock_power_dispatch)
    print(f"\nSTG Generation:                   {stg_req['stg_gross_mwh']:>12.2f} MWh")
    print(f"STG Generation:                   {stg_req['stg_gross_kwh']:>12.2f} KWh")
    print(f"\nNorms (per 1 KWh generated):")
    print(f"  Power Required:                 {stg_req['power_norm']:>12.4f} KWh/KWh")
    print(f"  SHP Steam Required:             {stg_req['shp_norm']:>12.4f} MT/KWh")
    print(f"\nCalculation (done in power_service.py):")
    print(f"  Power = {stg_req['stg_gross_kwh']} KWh × {stg_req['power_norm']} = {stg_req['power_required_kwh']:>12.2f} KWh")
    print(f"  Power = {stg_req['power_required_mwh']:>12.2f} MWh")
    print(f"  SHP   = {stg_req['stg_gross_kwh']} KWh × {stg_req['shp_norm']} = {stg_req['shp_steam_required_mt']:>12.2f} MT SHP")
    print(f"\n  ─────────────────────────────────────────────")
    print(f"  STG Power Requirement:          {stg_req['power_required_mwh']:>12.2f} MWh")
    print(f"  STG SHP Steam Requirement:      {stg_req['shp_steam_required_mt']:>12.2f} MT SHP")
    
    result = calculate_complete_steam_balance(
        lp_process=20109.57,
        lp_fixed=5169.51,
        mp_process=14030.00,
        mp_fixed=518.00,
        hp_process=4972.00,
        hp_fixed=0.00, 
        shp_process=20975.00,
        shp_fixed=0.00,
        power_dispatch=mock_power_dispatch,
        bfw_ufu=300.0
    )
    
    # =========================================================
    # FREE STEAM FROM ALL POWER ASSETS
    # =========================================================
    print("\n" + "="*70)
    print("FREE STEAM GENERATED FROM ALL POWER ASSETS")
    print("="*70)
    print(f"{'Asset':<10} {'Generation':<15} {'Hours':<10} {'GT Load':<12} {'Factor':<10} {'Free Steam':<15}")
    print("-"*70)
    
    for asset in mock_power_dispatch:
        name = asset["AssetName"]
        gross = asset["GrossMWh"]
        hours = asset["Hours"]
        load = asset["LoadMW"]
        factor = asset["FreeSteam"]
        
        if factor is not None and gross > 0:
            free_steam = gross * factor
            print(f"{name:<10} {gross:>10.2f} MWh  {hours:>6} hrs  {load:>8.2f} MW  {factor:>8.4f}  {free_steam:>10.2f} MT SHP")
        else:
            print(f"{name:<10} {gross:>10.2f} MWh  {hours:>6} hrs  {load:>8.2f} MW  {'N/A':>8}  {'0.00':>10} MT SHP")
    
    print("-"*70)
    print(f"{'TOTAL FREE STEAM':.<55} {free_steam_result['total_free_steam_mt']:>10.2f} MT SHP")
    
    # =========================================================
    # STEAM BALANCE CALCULATION RESULTS
    # =========================================================
    print("\n" + "="*70)
    print("STEAM BALANCE CALCULATION RESULTS")
    print("="*70)
    
    # LP Balance with UOM
    lp = result["lp_balance"]
    print("\n--- LP STEAM BALANCE ---")
    print(f"  LP Process Demand:              {lp['lp_process']:>12.2f} MT LP")
    print(f"  LP Fixed Demand:                {lp['lp_fixed']:>12.2f} MT LP")
    print(f"  LP for UFU (BFW conversion):    {lp['lp_ufu']:>12.2f} MT LP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  TOTAL LP DEMAND:                {lp['lp_total']:>12.2f} MT LP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  LP from PRDS (38.66%):          {lp['lp_from_prds']:>12.2f} MT LP")
    print(f"  LP from STG (61.34%):           {lp['lp_from_stg']:>12.2f} MT LP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  SHP needed for STG→LP:          {lp['shp_for_stg_lp']:>12.2f} MT SHP")
    print(f"  MP needed for PRDS→LP:          {lp['mp_for_prds_lp']:>12.2f} MT MP")
    print(f"  BFW needed for PRDS→LP:         {lp['bfw_for_prds_lp']:>12.2f} M³ BFW")
    
    # MP Balance with UOM
    mp = result["mp_balance"]
    print("\n--- MP STEAM BALANCE ---")
    print(f"  MP Process Demand:              {mp['mp_process']:>12.2f} MT MP")
    print(f"  MP Fixed Demand:                {mp['mp_fixed']:>12.2f} MT MP")
    print(f"  MP for LP (from LP chain):      {mp['mp_for_lp']:>12.2f} MT MP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  TOTAL MP DEMAND:                {mp['mp_total']:>12.2f} MT MP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  MP from PRDS (70.92%):          {mp['mp_from_prds']:>12.2f} MT MP")
    print(f"  MP from STG (29.08%):           {mp['mp_from_stg']:>12.2f} MT MP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  SHP needed for STG→MP:          {mp['shp_for_stg_mp']:>12.2f} MT SHP")
    print(f"  SHP needed for PRDS→MP:         {mp['shp_for_prds_mp']:>12.2f} MT SHP")
    print(f"  BFW needed for PRDS→MP:         {mp['bfw_for_prds_mp']:>12.2f} M³ BFW")
    print(f"  Total SHP from MP chain:        {mp['shp_from_mp_chain']:>12.2f} MT SHP")
    
    # HP Balance with UOM
    hp = result["hp_balance"]
    print("\n--- HP STEAM BALANCE ---")
    print(f"  HP Process Demand:              {hp['hp_process']:>12.2f} MT HP")
    print(f"  HP Fixed Demand:                {hp['hp_fixed']:>12.2f} MT HP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  TOTAL HP DEMAND:                {hp['hp_total']:>12.2f} MT HP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  HP from PRDS (100%):            {hp['hp_from_prds']:>12.2f} MT HP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  SHP needed for PRDS→HP:         {hp['shp_for_hp_prds']:>12.2f} MT SHP")
    print(f"  BFW needed for PRDS→HP:         {hp['bfw_for_hp_prds']:>12.2f} M³ BFW")
    
    # SHP Balance with UOM
    shp = result["shp_balance"]
    print("\n--- SHP STEAM BALANCE ---")
    print(f"  SHP Process Demand:             {shp['shp_process']:>12.2f} MT SHP")
    print(f"  SHP Fixed Demand:               {shp['shp_fixed']:>12.2f} MT SHP")
    print(f"  SHP for STG→LP:                 {shp['shp_for_stg_lp']:>12.2f} MT SHP")
    print(f"  SHP from MP chain:              {shp['shp_from_mp_chain']:>12.2f} MT SHP")
    print(f"  SHP for PRDS→HP:                {shp['shp_for_hp_prds']:>12.2f} MT SHP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  SHP from Headers:               {shp['shp_from_headers']:>12.2f} MT SHP")
    print(f"  SHP Total (without power):      {shp['shp_total_without_power']:>12.2f} MT SHP")
    print(f"  STG SHP for Power:              {shp['stg_shp_power']:>12.2f} MT SHP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  TOTAL SHP DEMAND:               {shp['shp_total_demand']:>12.2f} MT SHP")
    
    # BFW Requirement with UOM
    bfw = result["bfw_requirement"]
    print("\n--- BFW (BOILER FEED WATER) REQUIREMENT ---")
    print(f"  BFW for PRDS→LP:                {bfw['bfw_for_prds_lp']:>12.2f} M³ BFW")
    print(f"  BFW for PRDS→MP:                {bfw['bfw_for_prds_mp']:>12.2f} M³ BFW")
    print(f"  BFW for PRDS→HP:                {bfw['bfw_for_hp_prds']:>12.2f} M³ BFW")
    print(f"  BFW for UFU:                    {bfw['bfw_ufu']:>12.2f} M³ BFW")
    print(f"  ─────────────────────────────────────────────")
    print(f"  TOTAL BFW DEMAND:               {bfw['total_bfw']:>12.2f} M³ BFW")
    
    # Summary with UOM
    summary = result["summary"]
    print("\n--- DEMAND SUMMARY ---")
    print(f"  Total LP Demand:                {summary['total_lp_demand']:>12.2f} MT LP")
    print(f"  Total MP Demand:                {summary['total_mp_demand']:>12.2f} MT MP")
    print(f"  Total HP Demand:                {summary['total_hp_demand']:>12.2f} MT HP")
    print(f"  Total SHP Demand:               {summary['total_shp_demand']:>12.2f} MT SHP")
    print(f"  Total BFW Demand:               {summary['total_bfw_demand']:>12.2f} M³ BFW")
    
    # STG Requirements (included in SHP demand)
    stg_req_result = result.get("stg_requirements")
    if stg_req_result:
        print("\n" + "="*70)
        print("STG REQUIREMENTS (Included in SHP Demand)")
        print("="*70)
        print(f"  STG Gross Generation:           {stg_req_result['stg_gross_mwh']:>12.2f} MWh")
        print(f"  STG Gross Generation:           {stg_req_result['stg_gross_kwh']:>12.2f} KWh")
        print(f"  ─────────────────────────────────────────────")
        print(f"  Power Norm:                     {stg_req_result['power_norm']:>12.4f} KWh/KWh")
        print(f"  SHP Norm:                       {stg_req_result['shp_norm']:>12.4f} MT/KWh")
        print(f"  ─────────────────────────────────────────────")
        print(f"  Power Required (Aux):           {stg_req_result['power_required_mwh']:>12.2f} MWh")
        print(f"  SHP Steam Required:             {stg_req_result['shp_steam_required_mt']:>12.2f} MT SHP")
    
    # HRSG Availability & Free Steam
    print("\n" + "="*70)
    print("HRSG AVAILABILITY & FREE STEAM GENERATION")
    print("="*70)
    for hrsg_name, hrsg_data in result["hrsg_availability"].items():
        status = "✅ AVAILABLE" if hrsg_data["is_available"] else "❌ UNAVAILABLE"
        print(f"\n  {hrsg_name}: {status}")
        if hrsg_data["is_available"]:
            print(f"    Linked GT Generation:         {hrsg_data['gt_gross_mwh']:>12.2f} MWh")
            print(f"    Operational Hours:            {hrsg_data['operational_hours']:>12} hrs")
            print(f"    GT Load:                      {hrsg_data['gt_load_mw']:>12.2f} MW")
            print(f"    Free Steam Factor:            {hrsg_data['free_steam_factor']:>12.4f}")
            print(f"    Free Steam Generated:         {hrsg_data['free_steam_mt']:>12.2f} MT SHP")
        else:
            print(f"    Status: GT not running, HRSG unavailable")
    
    # SHP Generation Capacity
    shp_gen = result["shp_generation_capacity"]
    print("\n" + "="*70)
    print("SHP GENERATION CAPACITY")
    print("="*70)
    print(f"  Available HRSGs:                {', '.join(shp_gen['available_hrsgs']) or 'None'}")
    print(f"  Unavailable HRSGs:              {', '.join(shp_gen['unavailable_hrsgs']) or 'None'}")
    print(f"  ─────────────────────────────────────────────")
    print(f"  Total Free Steam (from GT):     {shp_gen['total_free_steam_mt']:>12.2f} MT SHP")
    print(f"  Min SHP Capacity (with firing): {shp_gen['total_min_shp_capacity']:>12.2f} MT SHP")
    print(f"  Max SHP Capacity (with firing): {shp_gen['total_max_shp_capacity']:>12.2f} MT SHP")
    
    # SHP Balance Check
    shp_check = result["shp_balance_check"]
    print("\n" + "="*70)
    print("SHP SUPPLY vs DEMAND CHECK")
    print("="*70)
    print(f"  SHP Demand:                     {shp_check['shp_demand']:>12.2f} MT SHP")
    print(f"  Free Steam Available:           {shp_check['free_steam_available']:>12.2f} MT SHP")
    print(f"  Free Steam Sufficient:          {'✅ YES' if shp_check['free_steam_sufficient'] else '❌ NO'}")
    print(f"  Supplementary Firing Needed:    {shp_check['supplementary_firing_needed']:>12.2f} MT SHP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  SHP Max Capacity:               {shp_check['shp_max_capacity']:>12.2f} MT SHP")
    print(f"  SHP Min Capacity:               {shp_check['shp_min_capacity']:>12.2f} MT SHP")
    print(f"  ─────────────────────────────────────────────")
    print(f"  Can Meet Demand:                {'✅ YES' if shp_check['can_meet_demand'] else '❌ NO'}")
    print(f"  Capacity Utilization:           {shp_check['utilization_percent']:>12.2f} %")
    if shp_check['deficit'] > 0:
        print(f"  ⚠️ DEFICIT:                     {shp_check['deficit']:>12.2f} MT SHP")
    else:
        print(f"  Surplus Available:              {shp_check['surplus']:>12.2f} MT SHP")


# ============================================================
# HRSG MIN LOAD CALCULATION AND EXCESS STEAM HANDLING
# ============================================================
def calculate_hrsg_min_load_and_excess_steam(
    power_dispatch: list,
    shp_demand: float
) -> dict:
    """
    Calculate HRSG production at MIN load and determine excess steam.
    
    NEW DISPATCH RULES:
    ===================
    1. HRSG availability follows linked GT availability
    2. HRSG priority follows linked GT priority  
    3. ALL running HRSGs MUST operate at MIN load (60 MT/hr × Hours × Efficiency)
    4. If MIN load produces excess steam, convert to power via STG
    5. Conversion rate: 3.56 MT SHP = 1 MWh power
    
    Args:
        power_dispatch: List of dispatch results from power_service
        shp_demand: Total SHP demand (MT) including STG requirements
        
    Returns:
        dict with HRSG MIN load production, excess steam, and power conversion
    """
    # Get HRSG availability from power dispatch
    hrsg_availability = get_hrsg_availability_from_dispatch(power_dispatch)
    
    # Calculate MIN load production for each available HRSG
    hrsg_min_load_details = []
    total_free_steam = 0.0
    total_min_supp_firing = 0.0
    total_min_shp_production = 0.0
    
    for hrsg_name, hrsg_data in hrsg_availability.items():
        if hrsg_data["is_available"]:
            hours = hrsg_data["operational_hours"]
            min_capacity_per_hr = hrsg_data["min_capacity_mt"]  # 60 MT/hr
            efficiency = hrsg_data["efficiency"]  # 1.03
            
            # Free steam from GT exhaust
            free_steam_mt = hrsg_data.get("free_steam_mt", 0.0)
            total_free_steam += free_steam_mt
            
            # MIN supplementary firing = Hours × Min_Capacity × Efficiency
            min_supp_firing = min_capacity_per_hr * hours * efficiency
            total_min_supp_firing += min_supp_firing
            
            # Total MIN production = Only Supplementary Firing (Free Steam is display only)
            # Free steam is NOT added to total HRSG generation for balance calculation
            min_production = min_supp_firing  # Exclude free steam from total
            total_min_shp_production += min_production
            
            # Get linked GT info for priority
            hrsg_assets = get_hrsg_assets()
            linked_gt = hrsg_assets[hrsg_name]["linked_gt"]
            gt_num = linked_gt[-1]  # "1", "2", "3"
            gt_priority = None
            for asset in power_dispatch:
                asset_name = asset.get("AssetName", "").upper()
                if (linked_gt in asset_name or 
                    f"PLANT-{gt_num}" in asset_name or 
                    f"PLANT {gt_num}" in asset_name):
                    gt_priority = asset.get("Priority")
                    break
            
            hrsg_min_load_details.append({
                "name": hrsg_name,
                "linked_gt": linked_gt,
                "priority": gt_priority,
                "is_available": True,
                "hours": hours,
                "min_capacity_per_hr": min_capacity_per_hr,
                "efficiency": efficiency,
                "free_steam_mt": round(free_steam_mt, 2),
                "min_supp_firing_mt": round(min_supp_firing, 2),
                "min_production_mt": round(min_production, 2),
            })
        else:
            hrsg_assets = get_hrsg_assets()
            hrsg_min_load_details.append({
                "name": hrsg_name,
                "linked_gt": hrsg_assets[hrsg_name]["linked_gt"],
                "priority": None,
                "is_available": False,
                "hours": 0,
                "min_capacity_per_hr": hrsg_data["min_capacity_mt"],
                "efficiency": hrsg_data["efficiency"],
                "free_steam_mt": 0.0,
                "min_supp_firing_mt": 0.0,
                "min_production_mt": 0.0,
            })
    
    # Calculate excess steam
    excess_steam_mt = max(0.0, total_min_shp_production - shp_demand)
    
    # Convert excess steam to power (3.56 MT = 1 MWh)
    excess_power_mwh = excess_steam_mt / STEAM_TO_POWER_MT_PER_MWH if excess_steam_mt > 0 else 0.0
    
    # Log the results
    print("\n" + "="*90)
    print("HRSG MIN LOAD CALCULATION (NEW DISPATCH LOGIC)")
    print("="*90)
    print(f"{'HRSG':<10} {'Linked GT':<12} {'Priority':<10} {'Available':<12} {'Hours':<8} {'Free Steam':<12} {'Min Supp':<12} {'Total MIN':<12}")
    print("-"*90)
    
    for h in hrsg_min_load_details:
        avail_str = "YES" if h["is_available"] else "NO"
        # Handle None and NaN for priority
        pri_val = h.get("priority")
        pri_str = str(int(pri_val)) if pri_val is not None and not (isinstance(pri_val, float) and pri_val != pri_val) else "-"
        print(f"{h['name']:<10} {h['linked_gt']:<12} {pri_str:<10} {avail_str:<12} {h['hours']:<8.0f} {h['free_steam_mt']:>10.2f}   {h['min_supp_firing_mt']:>10.2f}   {h['min_production_mt']:>10.2f}")
    
    print("-"*90)
    print(f"{'TOTAL':<10} {'':<12} {'':<10} {'':<12} {'':<8} {total_free_steam:>10.2f}   {total_min_supp_firing:>10.2f}   {total_min_shp_production:>10.2f}")
    
    print(f"\n  SHP Demand:                     {shp_demand:>12.2f} MT")
    print(f"  Total MIN SHP Production:       {total_min_shp_production:>12.2f} MT")
    print(f"  ─────────────────────────────────────────────")
    
    if excess_steam_mt > 0:
        print(f"  ⚡ EXCESS STEAM:                {excess_steam_mt:>12.2f} MT")
        print(f"  ⚡ EXCESS POWER (@ 3.56 MT/MWh): {excess_power_mwh:>12.2f} MWh")
        print(f"  ─────────────────────────────────────────────")
        print(f"  NOTE: Excess steam can be absorbed by increasing STG generation")
        print(f"        or reducing GT dispatch to maintain power balance")
    else:
        shortfall = shp_demand - total_min_shp_production
        print(f"  Status: MIN load meets demand (no excess)")
        if shortfall > 0:
            print(f"  Additional SHP needed:          {shortfall:>12.2f} MT")
            print(f"  (Will be met by increasing HRSG above MIN load)")
    
    print("="*90 + "\n")
    
    return {
        "hrsg_details": hrsg_min_load_details,
        "total_free_steam_mt": round(total_free_steam, 2),
        "total_min_supp_firing_mt": round(total_min_supp_firing, 2),
        "total_min_shp_production_mt": round(total_min_shp_production, 2),
        "shp_demand_mt": round(shp_demand, 2),
        "excess_steam_mt": round(excess_steam_mt, 2),
        "excess_power_mwh": round(excess_power_mwh, 2),
        "steam_to_power_rate": STEAM_TO_POWER_MT_PER_MWH,
    }


# ============================================================
# DISPATCH HRSG LOAD WITH PRIORITY
# ============================================================
def dispatch_hrsg_load(
    power_dispatch: list,
    shp_demand: float,
    shp_capacity: dict
) -> dict:
    """
    Dispatch HRSG supplementary firing load based on SHP demand with priority.
    
    DISPATCH RULES:
    ===============
    1. Total SHP Supply = Free Steam + HRSG Supp Firing
    2. Free Steam is fixed (from GT exhaust, no control)
    3. HRSG Supp Firing is controllable (MIN to MAX per HRSG)
    4. Priority follows linked GT priority (lower number = higher priority)
    
    CASES:
    ======
    Case A: Demand > MIN Supply (Free Steam + MIN Supp Firing)
        - Increase HRSG supp firing above MIN
        - Increase HIGH priority HRSG first (lower priority number)
        
    Case B: Demand < MIN Supply AND HRSGs above MIN
        - Decrease HRSG supp firing towards MIN
        - Decrease LOW priority HRSG first (higher priority number)
        
    Case C: Demand < MIN Supply AND HRSGs at MIN
        - Cannot reduce HRSG further
        - Excess steam needs STG/GT adjustment (handled in iteration)
    
    Args:
        power_dispatch: List of dispatch results from power_service
        shp_demand: Total SHP demand (MT)
        shp_capacity: SHP capacity dict from calculate_shp_generation_capacity
        
    Returns:
        dict with dispatched HRSG load per unit
    """
    # Get HRSG details from capacity
    hrsg_details = shp_capacity.get("hrsg_details", [])
    total_free_steam = shp_capacity.get("total_free_steam_mt", 0)
    
    # Build list of available HRSGs with priority
    available_hrsgs = []
    hrsg_assets = get_hrsg_assets()
    
    for hrsg_data in hrsg_details:
        if hrsg_data.get("is_available"):
            hrsg_name = hrsg_data.get("name", "")
            linked_gt = hrsg_assets.get(hrsg_name, {}).get("linked_gt", "")
            
            # Get priority from power dispatch
            gt_priority = None
            gt_num = linked_gt[-1] if linked_gt else ""
            for asset in power_dispatch:
                asset_name = asset.get("AssetName", "").upper()
                if (linked_gt in asset_name or 
                    f"PLANT-{gt_num}" in asset_name or 
                    f"PLANT {gt_num}" in asset_name):
                    gt_priority = asset.get("Priority")
                    break
            
            available_hrsgs.append({
                "name": hrsg_name,
                "linked_gt": linked_gt,
                "priority": gt_priority if gt_priority is not None else 999,
                "hours": hrsg_data.get("hours", 0),
                "min_supp_mt": hrsg_data.get("supp_min_mt_month", 0),
                "max_supp_mt": hrsg_data.get("supp_max_mt_month", 0),
                "free_steam_mt": hrsg_data.get("free_steam_mt", 0),
            })
    
    # Calculate total MIN and MAX supp firing capacity
    total_min_supp = sum(h["min_supp_mt"] for h in available_hrsgs)
    total_max_supp = sum(h["max_supp_mt"] for h in available_hrsgs)
    
    # Calculate MIN and MAX total supply (Free Steam is display only, not included in balance)
    min_supply = total_min_supp  # Exclude free steam
    max_supply = total_max_supp  # Exclude free steam
    
    # Calculate required supp firing = Demand (Free Steam not subtracted)
    required_supp_firing = shp_demand  # Full demand must be met by supp firing
    
    # Initialize dispatch result
    dispatch_result = {
        "total_free_steam_mt": round(total_free_steam, 2),
        "total_min_supp_mt": round(total_min_supp, 2),
        "total_max_supp_mt": round(total_max_supp, 2),
        "min_supply_mt": round(min_supply, 2),
        "max_supply_mt": round(max_supply, 2),
        "shp_demand_mt": round(shp_demand, 2),
        "required_supp_firing_mt": round(required_supp_firing, 2),
        "can_meet_demand": True,
        "excess_steam_mt": 0.0,
        "hrsg_dispatch": [],
    }
    
    # Case: No available HRSGs
    if not available_hrsgs:
        dispatch_result["can_meet_demand"] = shp_demand <= 0  # Cannot meet any demand without HRSGs
        dispatch_result["excess_steam_mt"] = 0.0
        return dispatch_result
    
    # =========================================================
    # CASE A: Demand > MIN Supply - Need to INCREASE load
    # =========================================================
    if shp_demand > min_supply:
        # Group HRSGs by priority for equal distribution within same priority
        sorted_hrsgs = sorted(available_hrsgs, key=lambda x: x["priority"])
        
        # Initialize all HRSGs to MIN
        for hrsg in sorted_hrsgs:
            hrsg["dispatched_supp_mt"] = hrsg["min_supp_mt"]
        
        # Calculate additional supp firing needed above MIN
        additional_needed = required_supp_firing - total_min_supp
        
        if additional_needed > 0:
            # Process priority groups in order (lower priority number first)
            priority_groups = []
            for priority, group in groupby(sorted_hrsgs, key=lambda x: x["priority"]):
                priority_groups.append(list(group))
            
            remaining_additional = additional_needed
            
            for group in priority_groups:
                if remaining_additional <= 0:
                    break
                
                # Calculate total available capacity above MIN for this group
                group_available = sum(h["max_supp_mt"] - h["min_supp_mt"] for h in group)
                
                if group_available <= 0:
                    continue
                
                # How much to allocate to this group
                group_allocation = min(remaining_additional, group_available)
                
                # Distribute equally among HRSGs in this group
                # Each HRSG gets proportional share based on its available capacity
                for hrsg in group:
                    hrsg_available = hrsg["max_supp_mt"] - hrsg["min_supp_mt"]
                    if hrsg_available > 0 and group_available > 0:
                        # Equal distribution: each HRSG gets same share
                        hrsg_share = group_allocation / len(group)
                        # But cap at HRSG's available capacity
                        actual_increase = min(hrsg_share, hrsg_available)
                        hrsg["dispatched_supp_mt"] = hrsg["min_supp_mt"] + actual_increase
                
                remaining_additional -= group_allocation
            
            # Check if demand can be met
            if remaining_additional > 0:
                dispatch_result["can_meet_demand"] = False
                dispatch_result["shortfall_mt"] = round(remaining_additional, 2)
    
    # =========================================================
    # CASE B & C: Demand <= MIN Supply - May need to DECREASE load
    # =========================================================
    else:
        # Sort by priority DESCENDING (higher number = lower priority = decrease first)
        sorted_hrsgs = sorted(available_hrsgs, key=lambda x: x["priority"], reverse=True)
        
        # Calculate excess at MIN load
        excess_at_min = min_supply - shp_demand
        
        if excess_at_min > 0:
            # We have excess steam even at MIN load
            # HRSGs stay at MIN, excess needs STG/GT adjustment
            for hrsg in sorted_hrsgs:
                hrsg["dispatched_supp_mt"] = hrsg["min_supp_mt"]
            
            dispatch_result["excess_steam_mt"] = round(excess_at_min, 2)
        else:
            # Demand equals MIN supply exactly
            for hrsg in sorted_hrsgs:
                hrsg["dispatched_supp_mt"] = hrsg["min_supp_mt"]
    
    # Build final dispatch list
    total_dispatched_supp = 0
    for hrsg in available_hrsgs:
        dispatched = hrsg.get("dispatched_supp_mt", hrsg["min_supp_mt"])
        total_dispatched_supp += dispatched
        
        dispatch_result["hrsg_dispatch"].append({
            "name": hrsg["name"],
            "linked_gt": hrsg["linked_gt"],
            "priority": hrsg["priority"],
            "hours": hrsg["hours"],
            "free_steam_mt": round(hrsg["free_steam_mt"], 2),
            "min_supp_mt": round(hrsg["min_supp_mt"], 2),
            "max_supp_mt": round(hrsg["max_supp_mt"], 2),
            "dispatched_supp_mt": round(dispatched, 2),
            "total_shp_mt": round(dispatched, 2),  # HRSG SHP = supp firing only (free steam is separate)
            "hourly_rate_mt_hr": round(dispatched / hrsg["hours"], 2) if hrsg["hours"] > 0 else 0,
        })
    
    dispatch_result["total_dispatched_supp_mt"] = round(total_dispatched_supp, 2)
    # Total SHP supply = Only Dispatched Supp Firing (Free Steam is display only)
    dispatch_result["total_shp_supply_mt"] = round(total_dispatched_supp, 2)  # Exclude free steam
    
    # =========================================================
    # HRSG HOURLY RATE CONSTRAINT VALIDATION
    # =========================================================
    # Check if any HRSG exceeds its maximum hourly capacity (136 MT/hr)
    hrsg_violations = []
    for h in dispatch_result["hrsg_dispatch"]:
        hourly_rate = h["hourly_rate_mt_hr"]
        hours = h["hours"]
        
        # Get max capacity per hour from HRSG assets
        hrsg_name = h["name"]
        hrsg_config = hrsg_assets.get(hrsg_name, {})
        max_capacity_per_hr = hrsg_config.get("max_capacity_mt", 136.0)
        
        if hourly_rate > max_capacity_per_hr:
            violation_pct = ((hourly_rate - max_capacity_per_hr) / max_capacity_per_hr) * 100
            hrsg_violations.append({
                "hrsg_name": hrsg_name,
                "linked_gt": h["linked_gt"],
                "priority": h["priority"],
                "dispatched_mt": h["dispatched_supp_mt"],
                "hours": hours,
                "hourly_rate_mt_hr": hourly_rate,
                "max_capacity_mt_hr": max_capacity_per_hr,
                "excess_mt_hr": round(hourly_rate - max_capacity_per_hr, 2),
                "violation_pct": round(violation_pct, 2)
            })
    
    # Store violations in result
    dispatch_result["hrsg_capacity_violations"] = hrsg_violations
    dispatch_result["has_capacity_violation"] = len(hrsg_violations) > 0
    
    # Print dispatch summary
    print("\n" + "="*100)
    print("HRSG LOAD DISPATCH (Priority-Based)")
    print("="*100)
    print(f"  {'HRSG':<10} {'Linked GT':<12} {'Priority':<10} {'Hours':<8} {'MIN Supp':<12} {'MAX Supp':<12} {'Dispatched':<12} {'Rate':<12} {'Status':<15}")
    print(f"  {'':<10} {'':<12} {'':<10} {'':<8} {'(MT)':<12} {'(MT)':<12} {'(MT)':<12} {'(MT/hr)':<12} {'':<15}")
    print("  " + "-"*107)
    
    for h in dispatch_result["hrsg_dispatch"]:
        pri_str = str(int(h["priority"])) if h["priority"] != 999 else "-"
        dispatched = h["dispatched_supp_mt"]
        min_supp = h["min_supp_mt"]
        max_supp = h["max_supp_mt"]
        hourly_rate = h["hourly_rate_mt_hr"]
        
        if dispatched <= min_supp:
            status = "AT MIN"
        elif dispatched >= max_supp:
            status = "AT MAX"
        else:
            status = "PARTIAL"
        
        print(f"  {h['name']:<10} {h['linked_gt']:<12} {pri_str:<10} {h['hours']:<8.0f} {min_supp:>10.2f}   {max_supp:>10.2f}   {dispatched:>10.2f}   {hourly_rate:>10.2f}   {status:<15}")
    
    print("  " + "-"*107)
    print(f"  {'TOTAL':<10} {'':<12} {'':<10} {'':<8} {dispatch_result['total_min_supp_mt']:>10.2f}   {dispatch_result['total_max_supp_mt']:>10.2f}   {dispatch_result['total_dispatched_supp_mt']:>10.2f}   {'':<12}")
    print("  " + "="*107)
    print(f"\n  SHP BALANCE SUMMARY:")
    print(f"  ├─ Free Steam (display only):   {dispatch_result['total_free_steam_mt']:>12.2f} MT")
    print(f"  ├─ Dispatched Supp Firing:      {dispatch_result['total_dispatched_supp_mt']:>12.2f} MT")
    print(f"  ├─ Total SHP Supply (Supp):     {dispatch_result['total_shp_supply_mt']:>12.2f} MT")
    print(f"  ├─ SHP Demand:                  {dispatch_result['shp_demand_mt']:>12.2f} MT")
    print(f"  └─ Balance:                     {dispatch_result['total_shp_supply_mt'] - dispatch_result['shp_demand_mt']:>12.2f} MT")
    
    # Check for HRSG capacity violations
    if dispatch_result["has_capacity_violation"]:
        print(f"\n  ❌ HRSG CAPACITY VIOLATION DETECTED!")
        print(f"  " + "="*107)
        print(f"  The following HRSG(s) exceed their maximum hourly capacity (136 MT/hr):")
        print(f"\n  {'HRSG':<10} {'Linked GT':<12} {'Priority':<10} {'Hourly Rate':<14} {'Max Capacity':<14} {'Excess':<12} {'Violation %':<12}")
        print(f"  {'-'*90}")
        for v in hrsg_violations:
            print(f"  {v['hrsg_name']:<10} {v['linked_gt']:<12} {v['priority']:<10} {v['hourly_rate_mt_hr']:>12.2f}   {v['max_capacity_mt_hr']:>12.2f}   {v['excess_mt_hr']:>10.2f}   {v['violation_pct']:>10.2f}%")
        print(f"\n  ⚠️  ERROR: Steam demand CANNOT be met within HRSG capacity constraints!")
        print(f"  " + "="*107)
        print(f"\n  RECOMMENDED ACTIONS:")
        print(f"  1. Adjust GT asset priorities to distribute load across more HRSGs")
        print(f"  2. Ensure multiple GTs are dispatched to bring additional HRSGs online")
        print(f"  3. Review asset availability - ensure sufficient HRSGs are operational")
        print(f"  4. Consider reducing steam demand or increasing HRSG capacity")
        print(f"\n  EXAMPLE: If GT-2 (priority 3) and GT-3 (priority 4) are available,")
        print(f"           setting both to priority 3 will distribute load equally,")
        print(f"           bringing both HRSG-2 and HRSG-3 online to share steam generation.")
        print("="*100 + "\n")
        
        # Raise exception to stop execution
        raise ValueError(
            f"HRSG Capacity Violation: {len(hrsg_violations)} HRSG(s) exceed 136 MT/hr maximum capacity. "
            f"Steam demand cannot be met within physical constraints. "
            f"Adjust GT priorities or asset availability to distribute load across more HRSGs."
        )
    elif not dispatch_result["can_meet_demand"]:
        print(f"\n  ⚠️  CANNOT MEET DEMAND - Shortfall: {dispatch_result.get('shortfall_mt', 0):.2f} MT")
    elif dispatch_result["excess_steam_mt"] > 0:
        print(f"\n  ⚡ EXCESS STEAM at MIN load: {dispatch_result['excess_steam_mt']:.2f} MT")
        print(f"     → Needs STG/GT adjustment to balance")
    else:
        print(f"\n  ✓ SHP BALANCED")
    
    print("="*100 + "\n")
    
    return dispatch_result
