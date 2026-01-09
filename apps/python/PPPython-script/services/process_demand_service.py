"""
Process Demand Service
Fetches process demands from database for budget calculations.

Process demands include:
- LP Steam (MT)
- MP Steam (MT)
- HP Steam (MT)
- SHP Steam (MT)
- Compressed Air (NM3)
- Cooling Water 1 (KM3)
- Cooling Water 2 (KM3)
- DM Water (M3)

These are utilities consumed by PROCESS PLANTS (not utility plants).
Previously hardcoded, now fetched dynamically from CalculatedProcessDemand table.
"""

from database.connection import get_connection
from typing import Dict, Optional, Tuple, List

# Month number to column name mapping for CalculatedProcessDemand table
# Column names are lowercase abbreviated in the database
MONTH_TO_COLUMN = {
    1: "jan",
    2: "feb",
    3: "mar",
    4: "apr",
    5: "may",
    6: "jun",
    7: "jul",
    8: "aug",
    9: "sep",
    10: "oct",
    11: "nov",
    12: "dec",
}

# Utility name mapping from DB to model parameter names
# Maps cppUtility names from database to the parameter names used in the model
UTILITY_MAPPING = {
    # Steam utilities
    "LP Steam": "lp_process",
    "LP Steam_Dis": "lp_process",
    "MP Steam": "mp_process",
    "MP Steam_Dis": "mp_process",
    "HP Steam": "hp_process",
    "HP Steam_Dis": "hp_process",
    "SHP Steam": "shp_process",
    "SHP Steam_Dis": "shp_process",
    # Other utilities
    "COMPRESSED AIR": "air_process",
    "Compressed Air": "air_process",
    "D M Water": "dm_process",
    "DM Water": "dm_process",
    "Cooling Water 1": "cw1_process",
    "CW1": "cw1_process",
    "Cooling Water 2": "cw2_process",
    "CW2": "cw2_process",
}


def get_financial_year_string(month: int, year: int) -> str:
    """
    Convert month/year to financial year string format (e.g., "2025-26").
    
    Financial Year runs April to March:
    - April 2025 to March 2026 = FY 2025-26
    
    Args:
        month: Month number (1-12)
        year: Calendar year
    
    Returns:
        Financial year string like "2025-26"
    """
    if month >= 4:  # April to December
        fy_start = year
    else:  # January to March
        fy_start = year - 1
    
    fy_end = fy_start + 1
    return f"{fy_start}-{str(fy_end)[-2:]}"


def get_process_demand_for_month(month: int, year: int) -> Dict[str, float]:
    """
    Fetch aggregated process demand for a specific month from CalculatedProcessDemand table.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025)
    
    Returns:
        Dict with process demand values:
        {
            "lp_process": float (MT),
            "mp_process": float (MT),
            "hp_process": float (MT),
            "shp_process": float (MT),
            "air_process": float (NM3),
            "dm_process": float (M3),
            "cw1_process": float (KM3),
            "cw2_process": float (KM3),
        }
    """
    conn = get_connection()
    cursor = conn.cursor()
    
    try:
        # Get financial year string
        fy_string = get_financial_year_string(month, year)
        month_column = MONTH_TO_COLUMN.get(month)
        
        if not month_column:
            print(f"  [PROCESS] Invalid month: {month}, using defaults")
            return get_default_process_demands()
        
        # Fetch process demand aggregated by utility type
        # Sum across all process plants for each utility
        # Note: Column names are lowercase in the database (apr, may, jun, etc.)
        query = f"""
            SELECT 
                cpp_utility,
                SUM({month_column}) AS TotalDemand
            FROM dbo.CalculatedProcessDemand
            WHERE financial_year = ?
            GROUP BY cpp_utility
        """
        
        print(f"  [PROCESS] Querying CalculatedProcessDemand for FY {fy_string}, month column: {month_column}")
        cursor.execute(query, (fy_string,))
        rows = cursor.fetchall()
        
        if not rows:
            print(f"  [PROCESS] No CalculatedProcessDemand found for FY {fy_string}, using defaults")
            return get_default_process_demands()
        
        # Initialize with zeros
        result = {
            "lp_process": 0.0,
            "mp_process": 0.0,
            "hp_process": 0.0,
            "shp_process": 0.0,
            "air_process": 0.0,
            "dm_process": 0.0,
            "cw1_process": 0.0,
            "cw2_process": 0.0,
        }
        
        # Map utility names to result keys
        print(f"  [PROCESS] Found {len(rows)} utility types for FY {fy_string}")
        for row in rows:
            utility_name = row[0] if row[0] else ""
            value = float(row[1]) if row[1] else 0.0
            
            # Try to find matching key in UTILITY_MAPPING
            param_key = UTILITY_MAPPING.get(utility_name)
            if param_key and param_key in result:
                result[param_key] += value  # Aggregate if multiple matches
                print(f"    - {utility_name}: {value:.2f} -> {param_key}")
            else:
                print(f"    - {utility_name}: {value:.2f} (UNMAPPED)")
        
        print(f"  [PROCESS] Fetched process demands for {month}/{year} (FY {fy_string})")
        
        return result
        
    except Exception as e:
        print(f"  [PROCESS] Error fetching process demand: {e}, using defaults")
        return get_default_process_demands()
    finally:
        conn.close()


def get_process_demand_for_fy(financial_year: int) -> Dict[Tuple[int, int], Dict[str, float]]:
    """
    Fetch process demand for all 12 months of a financial year.
    
    Args:
        financial_year: Starting year of FY (e.g., 2025 for FY 2025-26)
    
    Returns:
        Dict mapping (month, year) tuple to process demand dict
    """
    # FY months: April to March
    fy_months = [
        (4, financial_year),
        (5, financial_year),
        (6, financial_year),
        (7, financial_year),
        (8, financial_year),
        (9, financial_year),
        (10, financial_year),
        (11, financial_year),
        (12, financial_year),
        (1, financial_year + 1),
        (2, financial_year + 1),
        (3, financial_year + 1),
    ]
    
    result = {}
    for month, year in fy_months:
        result[(month, year)] = get_process_demand_for_month(month, year)
    
    return result


def get_default_process_demands() -> Dict[str, float]:
    """
    Return default process demand values (fallback).
    These are the original hardcoded values from the model.
    """
    return {
        "lp_process": 30043.15,
        "mp_process": 14030.65,
        "hp_process": 4971.91,
        "shp_process": 20975.34,
        "air_process": 6095102.0,
        "dm_process": 54779.0,
        "cw1_process": 15194.0,
        "cw2_process": 9016.0,
    }


def print_process_demands(data: Dict[str, float], month: int = None, year: int = None):
    """Print process demands in a formatted way."""
    header = f"Process Demands for {month}/{year}" if month and year else "Process Demands"
    print(f"\n{header}:")
    print("-" * 50)
    print(f"  LP Steam:       {data['lp_process']:>12,.2f} MT")
    print(f"  MP Steam:       {data['mp_process']:>12,.2f} MT")
    print(f"  HP Steam:       {data['hp_process']:>12,.2f} MT")
    print(f"  SHP Steam:      {data['shp_process']:>12,.2f} MT")
    print(f"  Compressed Air: {data['air_process']:>12,.0f} NM3")
    print(f"  DM Water:       {data['dm_process']:>12,.0f} M3")
    print(f"  Cooling Water 1:{data['cw1_process']:>12,.2f} KM3")
    print(f"  Cooling Water 2:{data['cw2_process']:>12,.2f} KM3")


def get_combined_demands_for_month(month: int, year: int, 
                                    use_db_process: bool = True,
                                    use_db_fixed: bool = True,
                                    override_process: Dict[str, float] = None,
                                    override_fixed: Dict[str, float] = None) -> Dict[str, float]:
    """
    Get combined process and fixed demands for a specific month.
    
    This is a convenience function that fetches both process and fixed demands
    and combines them into a single dict ready for the budget calculation.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025)
        use_db_process: If True, fetch process demands from DB; else use defaults
        use_db_fixed: If True, fetch fixed demands from DB; else use defaults
        override_process: Optional dict to override specific process demand values
        override_fixed: Optional dict to override specific fixed demand values
    
    Returns:
        Combined demands dict with all parameters needed for budget calculation
    """
    from services.fixed_consumption_service import get_fixed_consumption_for_month, get_default_fixed_consumption
    
    # Get process demands
    if use_db_process:
        process_demands = get_process_demand_for_month(month, year)
    else:
        process_demands = get_default_process_demands()
    
    # Apply overrides if provided
    if override_process:
        for key, value in override_process.items():
            if key in process_demands and value is not None:
                process_demands[key] = value
    
    # Get fixed demands
    if use_db_fixed:
        fixed_demands = get_fixed_consumption_for_month(month, year)
    else:
        fixed_demands = get_default_fixed_consumption()
    
    # Apply overrides if provided
    if override_fixed:
        for key, value in override_fixed.items():
            if key in fixed_demands and value is not None:
                fixed_demands[key] = value
    
    # Combine into single dict
    combined = {
        # Process demands
        "lp_process": process_demands.get("lp_process", 0.0),
        "mp_process": process_demands.get("mp_process", 0.0),
        "hp_process": process_demands.get("hp_process", 0.0),
        "shp_process": process_demands.get("shp_process", 0.0),
        "air_process": process_demands.get("air_process", 0.0),
        "dm_process": process_demands.get("dm_process", 0.0),
        "cw1_process": process_demands.get("cw1_process", 0.0),
        "cw2_process": process_demands.get("cw2_process", 0.0),
        # Fixed demands
        "lp_fixed": fixed_demands.get("lp_fixed", 0.0),
        "mp_fixed": fixed_demands.get("mp_fixed", 0.0),
        "hp_fixed": fixed_demands.get("hp_fixed", 0.0),
        "shp_fixed": fixed_demands.get("shp_fixed", 0.0),
        # Other parameters
        "bfw_ufu": 0.0,
        "export_available": False,
    }
    
    return combined


# Test function
if __name__ == "__main__":
    print("=" * 60)
    print("PROCESS DEMAND SERVICE TEST")
    print("=" * 60)
    
    # Test April 2025
    print("\n--- Testing April 2025 ---")
    april_data = get_process_demand_for_month(4, 2025)
    print_process_demands(april_data, 4, 2025)
    
    # Compare with defaults
    print("\n--- Default Values (for comparison) ---")
    defaults = get_default_process_demands()
    print_process_demands(defaults)
    
    # Show differences
    print("\n--- Differences (Fetched - Default) ---")
    print("-" * 50)
    for key in april_data:
        diff = april_data[key] - defaults[key]
        if abs(diff) > 0.01:
            print(f"  {key:<18}: {diff:>+12,.2f}")
    
    # Test combined demands
    print("\n--- Testing Combined Demands (April 2025) ---")
    combined = get_combined_demands_for_month(4, 2025)
    print("\nCombined Demands:")
    for key, value in combined.items():
        if isinstance(value, float):
            print(f"  {key:<18}: {value:>12,.2f}")
        else:
            print(f"  {key:<18}: {value}")
