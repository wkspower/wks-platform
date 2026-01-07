"""
Fixed Consumption Service
Fetches fixed consumption demands from database for budget calculations.

Fixed consumption includes:
- LP Steam (MT)
- MP Steam (MT)
- HP Steam (MT) - if applicable
- SHP Steam (MT) - if applicable
- Power (KWH -> converted to MWH for model)
- Compressed Air (NM3)
- DM Water (M3)
- Cooling Water (KM3)
"""

from database.connection import get_connection
from typing import Dict, Optional, Tuple


def get_fixed_consumption_for_month(month: int, year: int) -> Dict[str, float]:
    """
    Fetch aggregated fixed consumption for a specific month.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025)
    
    Returns:
        Dict with fixed consumption values:
        {
            "lp_fixed": float (MT),
            "mp_fixed": float (MT),
            "hp_fixed": float (MT),
            "shp_fixed": float (MT),
            "power_fixed_kwh": float (KWH),
            "air_fixed": float (NM3),
            "dm_fixed": float (M3),
            "cw1_fixed": float (KM3),
            "cw2_fixed": float (KM3),
        }
    """
    conn = get_connection()
    cursor = conn.cursor()
    
    try:
        # Get FinancialYearMonthId
        cursor.execute("""
            SELECT Id FROM FinancialYearMonth WHERE Month = ? AND Year = ?
        """, (month, year))
        fym_row = cursor.fetchone()
        
        if not fym_row:
            print(f"  [FIXED] No FinancialYearMonth found for {month}/{year}, using defaults")
            return get_default_fixed_consumption()
        
        fym_id = fym_row[0]
        
        # Fetch all fixed consumption aggregated by utility type
        cursor.execute("""
            SELECT 
                np.Name AS UtilityName,
                np.UOM,
                SUM(ufc.ConsumptionValue) AS TotalConsumption
            FROM UtilityFixedConsumption ufc
            JOIN NormParameters np ON ufc.NormParameter_FK_Id = np.Id
            JOIN CPPCostCenters cc ON ufc.CostCenter_FK_Id = cc.CostCenterId
            JOIN FixedConsumptionPlantMapping pm ON cc.Plant_FK_Id = pm.Id
            WHERE ufc.FinancialYearMonth_FK_Id = ?
            GROUP BY np.Name, np.UOM
        """, (fym_id,))
        
        rows = cursor.fetchall()
        
        # Initialize with zeros
        result = {
            "lp_fixed": 0.0,
            "mp_fixed": 0.0,
            "hp_fixed": 0.0,
            "shp_fixed": 0.0,
            "power_fixed_kwh": 0.0,
            "air_fixed": 0.0,
            "dm_fixed": 0.0,
            "cw1_fixed": 0.0,
            "cw2_fixed": 0.0,
        }
        
        # Map utility names to result keys
        for row in rows:
            utility_name = row[0]
            value = float(row[2]) if row[2] else 0.0
            
            if utility_name == "LP Steam_Dis":
                result["lp_fixed"] = value
            elif utility_name == "MP Steam_Dis":
                result["mp_fixed"] = value
            elif utility_name == "HP Steam_Dis":
                result["hp_fixed"] = value
            elif utility_name == "SHP Steam_Dis":
                result["shp_fixed"] = value
            elif utility_name == "Power_Dis":
                result["power_fixed_kwh"] = value
            elif utility_name == "COMPRESSED AIR":
                result["air_fixed"] = value
            elif utility_name == "D M Water":
                result["dm_fixed"] = value
            elif utility_name == "Cooling Water 1":
                result["cw1_fixed"] = value
            elif utility_name == "Cooling Water 2":
                result["cw2_fixed"] = value
        
        return result
        
    finally:
        conn.close()


def get_fixed_consumption_for_fy(financial_year: int) -> Dict[Tuple[int, int], Dict[str, float]]:
    """
    Fetch fixed consumption for all 12 months of a financial year.
    
    Args:
        financial_year: Starting year of FY (e.g., 2025 for FY 2025-26)
    
    Returns:
        Dict mapping (month, year) tuple to fixed consumption dict
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
        result[(month, year)] = get_fixed_consumption_for_month(month, year)
    
    return result


def get_default_fixed_consumption() -> Dict[str, float]:
    """
    Return default fixed consumption values (fallback).
    These are the original hardcoded values.
    """
    return {
        "lp_fixed": 5169.51,
        "mp_fixed": 518.00,
        "hp_fixed": 0.0,
        "shp_fixed": 0.0,
        "power_fixed_kwh": 1605142.0,  # KWH
        "air_fixed": 32960.0,
        "dm_fixed": 366.0,
        "cw1_fixed": 0.0,
        "cw2_fixed": 8.0,
    }


def print_fixed_consumption(data: Dict[str, float], month: int = None, year: int = None):
    """Print fixed consumption in a formatted way."""
    header = f"Fixed Consumption for {month}/{year}" if month and year else "Fixed Consumption"
    print(f"\n{header}:")
    print("-" * 50)
    print(f"  LP Steam:       {data['lp_fixed']:>12,.2f} MT")
    print(f"  MP Steam:       {data['mp_fixed']:>12,.2f} MT")
    print(f"  HP Steam:       {data['hp_fixed']:>12,.2f} MT")
    print(f"  SHP Steam:      {data['shp_fixed']:>12,.2f} MT")
    print(f"  Power:          {data['power_fixed_kwh']:>12,.0f} KWH ({data['power_fixed_kwh']/1000:,.2f} MWH)")
    print(f"  Compressed Air: {data['air_fixed']:>12,.0f} NM3")
    print(f"  DM Water:       {data['dm_fixed']:>12,.0f} M3")
    print(f"  Cooling Water 1:{data['cw1_fixed']:>12,.2f} KM3")
    print(f"  Cooling Water 2:{data['cw2_fixed']:>12,.2f} KM3")


# Test function
if __name__ == "__main__":
    print("=" * 60)
    print("FIXED CONSUMPTION SERVICE TEST")
    print("=" * 60)
    
    # Test April 2025
    print("\n--- Testing April 2025 ---")
    april_data = get_fixed_consumption_for_month(4, 2025)
    print_fixed_consumption(april_data, 4, 2025)
    
    # Compare with defaults
    print("\n--- Default Values (for comparison) ---")
    defaults = get_default_fixed_consumption()
    print_fixed_consumption(defaults)
    
    # Show differences
    print("\n--- Differences (Fetched - Default) ---")
    print("-" * 50)
    for key in april_data:
        diff = april_data[key] - defaults[key]
        if abs(diff) > 0.01:
            print(f"  {key:<18}: {diff:>+12,.2f}")
