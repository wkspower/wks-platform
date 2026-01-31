"""
Import Power Availability Database Queries
Fetches import power data from new multi-source tables (CPP*)

NEW TABLE STRUCTURE (Multi-Source Support):
  CPPImportPowerSourceMapping
  ├─ Id (PK) - ImportPowerSource_FK_Id in other tables
  ├─ SourceName (e.g., "MEL", "Power_Dis", "SIEL")
  ├─ CPPPlant_FK_Id (Filter by plant)
  └─ IsActive

  CPPImportPowerCapacity
  ├─ ImportPowerSource_FK_Id (FK)
  ├─ FinancialYear (e.g., "2024")
  └─ Monthly columns: Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar (MW)

  CPPImportPowerOperationalHours
  ├─ ImportPowerSource_FK_Id (FK)
  ├─ FinancialYear (e.g., "2024")
  └─ Monthly columns: Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar (Hours)

CALCULATION:
  For each source: MWh = Capacity_MW × OperationalHours
  Total Import MWh = SUM(all sources)
"""

from database.connection import get_connection


# ============================================================
# MONTH TO COLUMN MAPPING
# ============================================================
def _get_month_column_name(month: int) -> str:
    """
    Map month number to database column name.
    
    Args:
        month: Month number (1-12)
    
    Returns:
        str: Column name (Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec)
    """
    month_map = {
        1: "Jan", 2: "Feb", 3: "Mar", 4: "Apr", 5: "May", 6: "Jun",
        7: "Jul", 8: "Aug", 9: "Sep", 10: "Oct", 11: "Nov", 12: "Dec"
    }
    return month_map.get(month, "Jan")


# ============================================================
# NEW MULTI-SOURCE IMPORT POWER FUNCTIONS
# ============================================================
def fetch_import_power_sources(cpp_plant_id: str, financial_year: str) -> dict:
    """
    Fetch all import power sources for a specific CPP plant.
    
    Args:
        cpp_plant_id: UUID of the CPP plant (e.g., "23BCA1B3-56DD-4C15-A3D6-3C2C9A62E653")
        financial_year: Financial year string (e.g., "2024")
    
    Returns:
        dict with:
        - success: bool
        - sources: list of {id, source_name}
        - count: int
        - message: str
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        cur.execute("""
            SELECT Id, SourceName
            FROM CPPImportPowerSourceMapping
            WHERE CPPPlant_FK_Id = ?
              AND IsActive = 1
            ORDER BY SourceName
        """, (cpp_plant_id,))
        
        rows = cur.fetchall()
        
        if not rows:
            return {
                "success": True,
                "sources": [],
                "count": 0,
                "message": f"No import power sources found for plant {cpp_plant_id}"
            }
        
        sources = [{"id": str(row[0]), "source_name": row[1]} for row in rows]
        
        return {
            "success": True,
            "sources": sources,
            "count": len(sources),
            "message": f"Found {len(sources)} import power source(s)"
        }
        
    except Exception as e:
        return {
            "success": False,
            "sources": [],
            "count": 0,
            "message": f"Error fetching import power sources: {str(e)}"
        }
    finally:
        conn.close()


def fetch_import_power_capacity_multi_source(source_ids: list, financial_year: str, month: int) -> dict:
    """
    Fetch capacity (MW) for multiple import power sources for a specific month.
    
    Args:
        source_ids: List of ImportPowerSource_FK_Id (UUIDs as strings)
        financial_year: Financial year string (e.g., "2024")
        month: Month number (1-12)
    
    Returns:
        dict with:
        - success: bool
        - capacities: dict of {source_id: capacity_mw}
        - message: str
    """
    if not source_ids:
        return {
            "success": True,
            "capacities": {},
            "message": "No source IDs provided"
        }
    
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        month_col = _get_month_column_name(month)
        
        # Build query with proper parameter placeholders
        placeholders = ','.join(['?' for _ in source_ids])
        query = f"""
            SELECT ImportPowerSource_FK_Id, [{month_col}]
            FROM CPPImportPowerCapacity
            WHERE ImportPowerSource_FK_Id IN ({placeholders})
              AND FinancialYear = ?
        """
        
        params = source_ids + [financial_year]
        cur.execute(query, params)
        
        rows = cur.fetchall()
        
        capacities = {}
        for row in rows:
            source_id = str(row[0])
            capacity = float(row[1]) if row[1] is not None else 0.0
            capacities[source_id] = capacity
        
        # Fill missing sources with 0
        for source_id in source_ids:
            if source_id not in capacities:
                capacities[source_id] = 0.0
        
        return {
            "success": True,
            "capacities": capacities,
            "message": f"Fetched capacity for {len(capacities)} source(s) for {month_col}"
        }
        
    except Exception as e:
        return {
            "success": False,
            "capacities": {},
            "message": f"Error fetching import power capacity: {str(e)}"
        }
    finally:
        conn.close()


def fetch_import_power_hours_multi_source(source_ids: list, financial_year: str, month: int) -> dict:
    """
    Fetch operational hours for multiple import power sources for a specific month.
    
    Args:
        source_ids: List of ImportPowerSource_FK_Id (UUIDs as strings)
        financial_year: Financial year string (e.g., "2024")
        month: Month number (1-12)
    
    Returns:
        dict with:
        - success: bool
        - hours: dict of {source_id: operational_hours}
        - message: str
    """
    if not source_ids:
        return {
            "success": True,
            "hours": {},
            "message": "No source IDs provided"
        }
    
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        month_col = _get_month_column_name(month)
        
        # Build query with proper parameter placeholders
        placeholders = ','.join(['?' for _ in source_ids])
        query = f"""
            SELECT ImportPowerSource_FK_Id, [{month_col}]
            FROM CPPImportPowerOperationalHours
            WHERE ImportPowerSource_FK_Id IN ({placeholders})
              AND FinancialYear = ?
        """
        
        params = source_ids + [financial_year]
        cur.execute(query, params)
        
        rows = cur.fetchall()
        
        hours = {}
        for row in rows:
            source_id = str(row[0])
            op_hours = float(row[1]) if row[1] is not None else 0.0
            hours[source_id] = op_hours
        
        # Fill missing sources with 0
        for source_id in source_ids:
            if source_id not in hours:
                hours[source_id] = 0.0
        
        return {
            "success": True,
            "hours": hours,
            "message": f"Fetched operational hours for {len(hours)} source(s) for {month_col}"
        }
        
    except Exception as e:
        return {
            "success": False,
            "hours": {},
            "message": f"Error fetching import power operational hours: {str(e)}"
        }
    finally:
        conn.close()


def fetch_total_import_power_for_month(cpp_plant_id: str, month: int, year: int) -> dict:
    """
    Fetch total import power (MWh) for a specific month by aggregating all sources.
    
    This is the main function to use for getting import power data.
    
    Workflow:
    1. Get all import power sources for the plant
    2. Get capacity (MW) for each source for the month
    3. Get operational hours for each source for the month
    4. Calculate: MWh = Capacity × Hours for each source
    5. Sum all sources to get total import power
    
    Args:
        cpp_plant_id: UUID of the CPP plant
        month: Month number (1-12)
        year: Financial year (e.g., 2024)
    
    Returns:
        dict with:
        - success: bool
        - total_import_mwh: float (total MWh from all sources)
        - source_count: int (number of sources)
        - per_source: list of {source_name, capacity_mw, hours, mwh}
        - message: str
    
    Example:
        result = fetch_total_import_power_for_month(plant_id, 4, 2024)
        if result['success']:
            print(f"Total Import: {result['total_import_mwh']:,.2f} MWh")
            for source in result['per_source']:
                print(f"  {source['source_name']}: {source['mwh']:,.2f} MWh")
    """
    # Convert year to financial year format (YYYY-YY)
    # Month 1-3 belongs to previous FY, month 4-12 belongs to current FY
    if month >= 4:
        # April-December: FY is current year
        fy_start = year
    else:
        # January-March: FY is previous year
        fy_start = year - 1
    
    fy_end = str(fy_start + 1)[-2:]  # Get last 2 digits
    financial_year = f"{fy_start}-{fy_end}"
    
    # Step 1: Get all sources for the plant
    sources_result = fetch_import_power_sources(cpp_plant_id, financial_year)
    
    if not sources_result["success"]:
        return {
            "success": False,
            "total_import_mwh": 0.0,
            "source_count": 0,
            "per_source": [],
            "message": sources_result["message"]
        }
    
    if sources_result["count"] == 0:
        return {
            "success": True,
            "total_import_mwh": 0.0,
            "source_count": 0,
            "per_source": [],
            "message": "No import power sources found for this plant"
        }
    
    sources = sources_result["sources"]
    source_ids = [s["id"] for s in sources]
    
    # Step 2: Get capacity for all sources
    capacity_result = fetch_import_power_capacity_multi_source(source_ids, financial_year, month)
    
    if not capacity_result["success"]:
        return {
            "success": False,
            "total_import_mwh": 0.0,
            "source_count": 0,
            "per_source": [],
            "message": capacity_result["message"]
        }
    
    # Step 3: Get operational hours for all sources
    hours_result = fetch_import_power_hours_multi_source(source_ids, financial_year, month)
    
    if not hours_result["success"]:
        return {
            "success": False,
            "total_import_mwh": 0.0,
            "source_count": 0,
            "per_source": [],
            "message": hours_result["message"]
        }
    
    # Step 4: Calculate MWh for each source and aggregate
    per_source = []
    total_mwh = 0.0
    
    for source in sources:
        source_id = source["id"]
        source_name = source["source_name"]
        capacity = capacity_result["capacities"].get(source_id, 0.0)
        hours = hours_result["hours"].get(source_id, 0.0)
        mwh = capacity * hours
        
        per_source.append({
            "source_id": source_id,
            "source_name": source_name,
            "capacity_mw": round(capacity, 2),
            "hours": round(hours, 2),
            "mwh": round(mwh, 2)
        })
        
        total_mwh += mwh
    
    return {
        "success": True,
        "total_import_mwh": round(total_mwh, 2),
        "source_count": len(sources),
        "per_source": per_source,
        "message": f"Calculated import power from {len(sources)} source(s) for month {month}/{year}"
    }


# ============================================================
# OLD FUNCTIONS - DEPRECATED (Keep for backward compatibility)
# ============================================================


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
