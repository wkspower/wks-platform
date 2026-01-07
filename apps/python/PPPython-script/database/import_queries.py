"""
Import Power Availability Database Queries
Fetches import power data from AssetImportMapping table for a specific financial month

TABLE STRUCTURE (Existing in your database):
  AssetImportMapping
  ├─ Id (GUID)
  ├─ AssetId (FK to PowerGenerationAssets)
  ├─ FinancialMonthId (FK to FinancialYearMonth)
  ├─ Value (DECIMAL - import capacity in MW/MWh/KW/KWh)
  └─ UOM (VARCHAR - unit of measure)
"""

from database.connection import get_connection


def fetch_import_power_availability(month: int, year: int) -> dict:
    """
    Fetch import power availability for a specific month from AssetImportMapping.
    
    Workflow:
    1. Get FinancialYearMonth Id from month/year
    2. Query AssetImportMapping table for that month
    3. Return all import assets and total capacity
    
    Args:
        month: Financial month (1-12)
        year: Financial year (e.g., 2024)
    
    Returns:
        dict with:
        - success: bool
        - message: str (if error)
        - fym_id: UUID (if found)
        - per_asset: dict of asset_name -> import_capacity (if successful)
        - total_import_capacity: float (if successful)
        - details: list of dicts with asset details (if successful)
        
    Example:
        result = fetch_import_power_availability(1, 2024)
        if result['success']:
            print(f"Total Import: {result['total_import_capacity']} {result['uom']}")
            for asset in result['details']:
                print(f"  {asset['asset_name']}: {asset['import_capacity']} {asset['uom']}")
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # Step 1: Get FinancialYearMonth ID
        cur.execute(
            """
            SELECT Id 
            FROM FinancialYearMonth 
            WHERE [Month] = ? AND [Year] = ?
            """,
            (month, year)
        )
        fym_row = cur.fetchone()
        
        if not fym_row:
            return {
                "success": False,
                "message": f"FinancialYearMonth not found for {month}/{year}",
                "fym_id": None,
                "per_asset": {},
                "total_import_capacity": 0.0,
                "uom": "N/A",
                "details": []
            }
        
        fym_id = fym_row[0]
        
        # Step 2: Query import power from AssetImportMapping
        cur.execute(
            """
            SELECT 
                aim.AssetId,
                a.AssetName,
                aim.Value AS ImportCapacity,
                aim.UOM
            FROM AssetImportMapping aim
            JOIN FinancialYearMonth fym ON aim.FinancialMonthId = fym.Id
            JOIN PowerGenerationAssets a ON a.AssetId = aim.AssetId
            WHERE fym.[Month] = ? AND fym.[Year] = ?
              AND aim.UOM IN ('MW', 'MWh', 'KW', 'KWh')
            ORDER BY a.AssetName
            """,
            (month, year)
        )
        rows = cur.fetchall()
        
        if not rows:
            return {
                "success": True,
                "message": f"No import power data found in AssetImportMapping for {month}/{year}",
                "fym_id": fym_id,
                "per_asset": {},
                "total_import_capacity": 0.0,
                "uom": "MW",
                "details": []
            }
        
        # Build result structures
        per_asset = {}
        details = []
        total_import = 0.0
        uom = rows[0][3] if rows else "MW"  # Get UOM from first row
        
        for asset_id, asset_name, import_capacity, asset_uom in rows:
            import_capacity = float(import_capacity)
            per_asset[asset_name] = import_capacity
            total_import += import_capacity
            
            details.append({
                "asset_id": str(asset_id),
                "asset_name": asset_name,
                "import_capacity": round(import_capacity, 6),
                "uom": asset_uom
            })
        
        return {
            "success": True,
            "message": f"Fetched {len(details)} import asset(s) from AssetImportMapping for {month}/{year}",
            "fym_id": fym_id,
            "per_asset": per_asset,
            "total_import_capacity": round(total_import, 6),
            "uom": uom,
            "details": details
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching import power: {str(e)}",
            "fym_id": None,
            "per_asset": {},
            "total_import_capacity": 0.0,
            "uom": "N/A",
            "details": []
        }
    finally:
        conn.close()


def fetch_total_import_capacity(month: int, year: int) -> dict:
    """
    Fetch total import power capacity for a month (simpler version).
    
    Args:
        month: Financial month (1-12)
        year: Financial year (e.g., 2024)
    
    Returns:
        dict with:
        - success: bool
        - total_import_capacity: float
        - uom: str (unit of measure)
        - message: str (if error)
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        cur.execute(
            """
            SELECT 
                SUM(CAST(aim.Value AS DECIMAL(18,6))) AS TotalImportCapacity,
                aim.UOM
            FROM AssetImportMapping aim
            JOIN FinancialYearMonth fym ON aim.FinancialMonthId = fym.Id
            WHERE fym.[Month] = ? AND fym.[Year] = ?
              AND aim.UOM IN ('MW', 'MWh')
            GROUP BY aim.UOM
            """,
            (month, year)
        )
        row = cur.fetchone()
        
        if row and row[0]:
            total_import = float(row[0])
            uom = row[1] if row[1] else "MW"
        else:
            total_import = 0.0
            uom = "MW"
        
        return {
            "success": True,
            "total_import_capacity": round(total_import, 6),
            "uom": uom,
            "message": "Successfully fetched total import capacity from AssetImportMapping"
        }
        
    except Exception as e:
        return {
            "success": False,
            "total_import_capacity": 0.0,
            "uom": "MW",
            "message": f"Error fetching total import capacity: {str(e)}"
        }
    finally:
        conn.close()


def fetch_import_power_by_asset(asset_name: str, month: int, year: int) -> dict:
    """
    Fetch import power for a specific asset in a specific month.
    
    Args:
        asset_name: Name of the asset (e.g., "Import Power", "Grid Import")
        month: Financial month (1-12)
        year: Financial year (e.g., 2024)
    
    Returns:
        dict with:
        - success: bool
        - import_capacity: float
        - uom: str
        - message: str
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        cur.execute(
            """
            SELECT 
                aim.Value AS ImportCapacity,
                aim.UOM
            FROM AssetImportMapping aim
            JOIN FinancialYearMonth fym ON aim.FinancialMonthId = fym.Id
            JOIN PowerGenerationAssets a ON a.AssetId = aim.AssetId
            WHERE fym.[Month] = ? AND fym.[Year] = ?
              AND a.AssetName = ?
              AND aim.UOM IN ('MW', 'MWh', 'KW', 'KWh')
            """,
            (month, year, asset_name)
        )
        row = cur.fetchone()
        
        if row:
            import_capacity = float(row[0])
            uom = row[1]
            return {
                "success": True,
                "import_capacity": round(import_capacity, 6),
                "uom": uom,
                "message": f"Found import capacity for {asset_name}"
            }
        else:
            return {
                "success": False,
                "import_capacity": 0.0,
                "uom": "MW",
                "message": f"No import data found for {asset_name} in {month}/{year}"
            }
        
    except Exception as e:
        return {
            "success": False,
            "import_capacity": 0.0,
            "uom": "MW",
            "message": f"Error fetching import power for {asset_name}: {str(e)}"
        }
    finally:
        conn.close()


def fetch_stg_min_operating_capacity(month: int, year: int) -> dict:
    """
    Fetch STG minimum operating capacity for a month from AssetImportMapping.
    
    This looks for STG asset with appropriate UOM (MW, MWh, etc).
    
    Args:
        month: Financial month (1-12)
        year: Financial year (e.g., 2024)
    
    Returns:
        dict with:
        - success: bool
        - min_operating_capacity: float
        - uom: str
        - message: str
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        cur.execute(
            """
            SELECT 
                aim.Value AS MinOperatingCapacity,
                aim.UOM
            FROM AssetImportMapping aim
            JOIN FinancialYearMonth fym ON aim.FinancialMonthId = fym.Id
            JOIN PowerGenerationAssets a ON a.AssetId = aim.AssetId
            WHERE fym.[Month] = ? AND fym.[Year] = ?
              AND (a.AssetName LIKE '%STG%' OR a.AssetName LIKE '%Steam Turbine%')
              AND aim.UOM IN ('MW', 'MWh', 'KW', 'KWh')
            ORDER BY a.AssetName
            """,
            (month, year)
        )
        row = cur.fetchone()
        
        if row:
            min_operating = float(row[0])
            uom = row[1]
            return {
                "success": True,
                "min_operating_capacity": round(min_operating, 6),
                "uom": uom,
                "message": "Successfully fetched STG minimum operating capacity from AssetImportMapping"
            }
        else:
            # Fallback to reasonable default
            return {
                "success": False,
                "min_operating_capacity": 2400.0,  # Default: 2400 MWh (100 MW * 24 hours)
                "uom": "MWh",
                "message": f"STG asset not found in AssetImportMapping for {month}/{year}, using default (2400 MWh)"
            }
        
    except Exception as e:
        return {
            "success": False,
            "min_operating_capacity": 2400.0,  # Default fallback
            "uom": "MWh",
            "message": f"Error fetching STG capacity: {str(e)}, using default"
        }
    finally:
        conn.close()
