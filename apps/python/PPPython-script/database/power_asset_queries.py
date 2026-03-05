"""
Power Generation Assets Database Queries
Fetches power generation assets from PowerGenerationAssets table

TABLE STRUCTURE (Discovered):
  PowerGenerationAssets
  ├─ AssetId (uniqueidentifier) - Primary Key (GUID)
  ├─ AssetName (varchar) - e.g., "NMD-Power Plant-1", "NMD-STG Power Plant"
  ├─ AssetCapacity (decimal) - Base capacity in MW
  ├─ CPPPLANT_FK_Id (uniqueidentifier) - Foreign key to CPP Plant (groups assets by plant)
  └─ PlantCode (nvarchar) - Plant code (40NB, 40NC, 40ND, 40NE)

CPPPLANT_FK_Id Purpose:
  - Groups all power generation assets belonging to a specific CPP plant
  - Useful for multi-plant scenarios to filter assets by plant
  - All assets with same CPPPLANT_FK_Id belong to the same plant

Asset Mapping (Current Plant: 23BCA1B3-56DD-4C15-A3D6-3C2C9A62E653):
  - NMD-Power Plant-1 (40NB) → GT1 (linked to HRSG1)
  - NMD-Power Plant-2 (40NC) → GT2 (linked to HRSG2)
  - NMD-Power Plant-3 (40ND) → GT3 (linked to HRSG3)
  - NMD-STG Power Plant (40NE) → STG

Related Tables (for monthly data):
  - OperationalHours: Hours each asset operates per month (availability = hours > 0)
  - AssetAvailability: Min capacity, priority per month (legacy?)
  - AssetImportMapping: Import power mapping per month

OperationalHours Table Structure:
  ├─ Id (uniqueidentifier) - Primary Key
  ├─ FinancialMonthId (uniqueidentifier) - FK to FinancialYearMonth
  ├─ Asset_FK_Id (uniqueidentifier) - FK to PowerGenerationAssets
  └─ OperationalHours (decimal) - Hours of operation (0 = not available)

AssetAvailability Table Structure:
  ├─ Id (uniqueidentifier) - Primary Key
  ├─ AssetId (uniqueidentifier) - FK to PowerGenerationAssets
  ├─ FinancialYearMonthId (uniqueidentifier) - FK to FinancialYearMonth
  ├─ Priority (int) - Dispatch priority for the month (lower = higher priority)
  ├─ MinOperatingCapacity (decimal) - Actual min capacity for that month (MW)
  ├─ MaxOperatingCapacity (decimal) - Actual max capacity for that month (MW)
  ├─ FixedMin (decimal) - Fixed minimum capacity for whole year (MW)
  └─ FixedMax (decimal) - Fixed maximum capacity for whole year (MW)
  
  Note: IsAssetAvailable and operationalHours in this table are NOT used.
        Availability is determined from OperationalHours table instead.

PlantImportMapping Table Structure:
  ├─ Id (uniqueidentifier) - Primary Key
  ├─ AssetId (uniqueidentifier) - Separate import asset ID (NOT in PowerGenerationAssets)
  ├─ FinancialMonthId (uniqueidentifier) - FK to FinancialYearMonth
  ├─ Value (decimal) - Import power capacity in MW
  └─ UOM (varchar) - Unit of measure (MW)
  
  Note: Import power in MWh = Value (MW) × Operational Hours
        Operational hours table is under development.
        TEMPORARY: Use month days × 24 (30 days = 720 hrs, 31 days = 744 hrs)
"""

from database.connection import get_connection
from typing import List, Dict, Optional
import pandas as pd


# ============================================================
# ASSET TYPE DETECTION HELPERS
# ============================================================
def get_asset_type(asset_name: str) -> str:
    """
    Determine asset type from asset name.
    
    Args:
        asset_name: Name from database (e.g., "NMD-Power Plant-1", "NMD-STG Power Plant")
    
    Returns:
        str: "GT" for gas turbines, "STG" for steam turbine, "IMPORT" for import power, "UNKNOWN" otherwise
    """
    name_upper = asset_name.upper()
    
    if "STG" in name_upper or "STEAM TURBINE" in name_upper:
        return "STG"
    elif "POWER PLANT" in name_upper or "GT" in name_upper:
        return "GT"
    elif "IMPORT" in name_upper:
        return "IMPORT"
    else:
        return "UNKNOWN"


def get_gt_number(asset_name: str) -> Optional[int]:
    """
    Extract GT number from asset name.
    
    Mapping (CORRECTED):
    - NMD-Power Plant-1 (40NB) → GT1
    - NMD-Power Plant-2 (40NC) → GT2
    - NMD-Power Plant-3 (40ND) → GT3
    
    Args:
        asset_name: Name from database
    
    Returns:
        int: GT number (1, 2, or 3) or None if not a GT
    """
    name_upper = asset_name.upper()
    
    if "STG" in name_upper:
        return None
    
    # Extract number from "NMD-Power Plant-X" format
    if "POWER PLANT" in name_upper:
        # Find the number at the end
        for char in reversed(asset_name):
            if char.isdigit():
                plant_num = int(char)
                # Direct mapping: Plant-1 → GT1, Plant-2 → GT2, Plant-3 → GT3
                return plant_num
    
    # Try to extract from "GTX" format
    if "GT" in name_upper:
        import re
        match = re.search(r'GT(\d+)', name_upper)
        if match:
            return int(match.group(1))
    
    return None


def get_linked_hrsg(asset_name: str) -> Optional[str]:
    """
    Get linked HRSG for a GT asset.
    
    Mapping:
    - GT1 → HRSG1
    - GT2 → HRSG2
    - GT3 → HRSG3
    
    Args:
        asset_name: Name from database
    
    Returns:
        str: HRSG name (e.g., "HRSG1") or None if not a GT
    """
    gt_num = get_gt_number(asset_name)
    if gt_num:
        return f"HRSG{gt_num}"
    return None


def fetch_power_generation_assets() -> Dict:
    """
    Fetch all power generation assets from PowerGenerationAssets table.
    
    This is the master list of all power generation assets (GT1, GT2, GT3, STG, etc.)
    without any monthly-specific data.
    
    Returns:
        dict with:
        - success: bool
        - message: str
        - assets: list of dicts with asset details
        - count: int (number of assets)
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # First, let's discover the table structure
        cur.execute("""
            SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_NAME = 'PowerGenerationAssets'
            ORDER BY ORDINAL_POSITION
        """)
        columns_info = cur.fetchall()
        
        if not columns_info:
            return {
                "success": False,
                "message": "PowerGenerationAssets table not found or has no columns",
                "assets": [],
                "count": 0,
                "table_structure": []
            }
        
        # Store table structure for reference
        table_structure = [
            {"column": col[0], "type": col[1], "nullable": col[2]}
            for col in columns_info
        ]
        
        # Get column names for the SELECT query
        column_names = [col[0] for col in columns_info]
        
        # Fetch all power generation assets (GT and STG only)
        cur.execute(f"""
            SELECT *
            FROM PowerGenerationAssets
            WHERE AssetType IN ('GT', 'STG')
            ORDER BY AssetName
        """)
        rows = cur.fetchall()
        
        if not rows:
            return {
                "success": True,
                "message": "No power generation assets found in table",
                "assets": [],
                "count": 0,
                "table_structure": table_structure
            }
        
        # Build asset list with derived fields
        assets = []
        for row in rows:
            asset = {}
            for i, col_name in enumerate(column_names):
                value = row[i]
                # Convert UUID to string for JSON compatibility
                if col_name.lower() in ['assetid', 'id', 'cppplant_fk_id']:
                    value = str(value) if value else None
                # Convert Decimal to float
                elif col_name.lower() == 'assetcapacity':
                    value = float(value) if value else 0.0
                asset[col_name] = value
            
            # Add derived fields
            asset_name = asset.get("AssetName", "")
            asset["asset_type"] = get_asset_type(asset_name)
            asset["gt_number"] = get_gt_number(asset_name)
            asset["linked_hrsg"] = get_linked_hrsg(asset_name)
            asset["is_gt"] = asset["asset_type"] == "GT"
            asset["is_stg"] = asset["asset_type"] == "STG"
            
            assets.append(asset)
        
        # Sort: GTs first (by GT number), then STG
        assets.sort(key=lambda x: (
            0 if x["is_gt"] else 1,  # GTs first
            x["gt_number"] if x["gt_number"] else 999,  # By GT number
            x.get("AssetName", "")  # Then by name
        ))
        
        return {
            "success": True,
            "message": f"Found {len(assets)} power generation asset(s)",
            "assets": assets,
            "count": len(assets),
            "gt_count": sum(1 for a in assets if a["is_gt"]),
            "stg_count": sum(1 for a in assets if a["is_stg"]),
            "table_structure": table_structure
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching power generation assets: {str(e)}",
            "assets": [],
            "count": 0,
            "table_structure": []
        }
    finally:
        conn.close()


def fetch_power_asset_by_name(asset_name: str) -> Dict:
    """
    Fetch a specific power generation asset by name.
    
    Args:
        asset_name: Name of the asset (e.g., "GT1", "GT2", "STG")
    
    Returns:
        dict with asset details or error message
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        cur.execute("""
            SELECT *
            FROM PowerGenerationAssets
            WHERE AssetName = ?
        """, (asset_name,))
        
        row = cur.fetchone()
        
        if not row:
            return {
                "success": False,
                "message": f"Asset '{asset_name}' not found",
                "asset": None
            }
        
        # Get column names
        columns = [col[0] for col in cur.description]
        
        asset = {}
        for i, col_name in enumerate(columns):
            value = row[i]
            if col_name.lower() in ['assetid', 'id']:
                value = str(value) if value else None
            asset[col_name] = value
        
        return {
            "success": True,
            "message": f"Found asset: {asset_name}",
            "asset": asset
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching asset '{asset_name}': {str(e)}",
            "asset": None
        }
    finally:
        conn.close()


def fetch_power_asset_by_id(asset_id: str) -> Dict:
    """
    Fetch a specific power generation asset by ID.
    
    Args:
        asset_id: UUID of the asset
    
    Returns:
        dict with asset details or error message
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        cur.execute("""
            SELECT *
            FROM PowerGenerationAssets
            WHERE AssetId = ?
        """, (asset_id,))
        
        row = cur.fetchone()
        
        if not row:
            return {
                "success": False,
                "message": f"Asset with ID '{asset_id}' not found",
                "asset": None
            }
        
        # Get column names
        columns = [col[0] for col in cur.description]
        
        asset = {}
        for i, col_name in enumerate(columns):
            value = row[i]
            if col_name.lower() in ['assetid', 'id']:
                value = str(value) if value else None
            asset[col_name] = value
        
        return {
            "success": True,
            "message": f"Found asset with ID: {asset_id}",
            "asset": asset
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching asset by ID: {str(e)}",
            "asset": None
        }
    finally:
        conn.close()


def print_power_assets_summary():
    """
    Print a summary of all power generation assets.
    Useful for debugging and verification.
    """
    result = fetch_power_generation_assets()
    
    print("\n" + "="*100)
    print("POWER GENERATION ASSETS (from PowerGenerationAssets table)")
    print("="*100)
    
    if not result["success"]:
        print(f"ERROR: {result['message']}")
        return result
    
    # Print summary counts
    print(f"\nTotal Assets: {result['count']} (GTs: {result.get('gt_count', 0)}, STG: {result.get('stg_count', 0)})")
    
    # Print assets in table format
    print("\n" + "-"*100)
    print(f"{'Asset Name':<25} {'Type':<8} {'GT#':<5} {'Capacity':<12} {'Plant Code':<12} {'Linked HRSG':<12} {'Asset ID'}")
    print("-"*100)
    
    if result["assets"]:
        for asset in result["assets"]:
            asset_name = asset.get('AssetName', 'N/A')
            asset_type = asset.get('asset_type', 'N/A')
            gt_num = asset.get('gt_number', '-')
            capacity = asset.get('AssetCapacity', 0)
            plant_code = asset.get('PlantCode', 'N/A')
            linked_hrsg = asset.get('linked_hrsg', '-')
            asset_id = asset.get('AssetId', 'N/A')[:8] + "..." if asset.get('AssetId') else 'N/A'
            
            print(f"{asset_name:<25} {asset_type:<8} {str(gt_num):<5} {capacity:<12.2f} {plant_code:<12} {str(linked_hrsg):<12} {asset_id}")
    else:
        print("  No assets found")
    
    print("-"*100)
    print("="*100 + "\n")
    return result


# ============================================================
# CONVENIENCE FUNCTIONS FOR SERVICE INTEGRATION
# ============================================================
def get_all_power_assets() -> List[Dict]:
    """
    Get all power generation assets as a simple list.
    
    Returns:
        List of asset dicts, or empty list if error
    """
    result = fetch_power_generation_assets()
    return result.get("assets", []) if result.get("success") else []


def get_gt_assets() -> List[Dict]:
    """
    Get only GT (Gas Turbine) assets.
    
    Returns:
        List of GT asset dicts
    """
    assets = get_all_power_assets()
    return [a for a in assets if a.get("is_gt")]


def get_stg_asset() -> Optional[Dict]:
    """
    Get the STG (Steam Turbine Generator) asset.
    
    Returns:
        STG asset dict or None
    """
    assets = get_all_power_assets()
    stg_assets = [a for a in assets if a.get("is_stg")]
    return stg_assets[0] if stg_assets else None


def get_asset_id_by_name(asset_name: str) -> Optional[str]:
    """
    Get asset ID by asset name (partial match).
    
    Args:
        asset_name: Full or partial asset name
    
    Returns:
        Asset ID (GUID string) or None
    """
    assets = get_all_power_assets()
    name_upper = asset_name.upper()
    
    for asset in assets:
        if name_upper in asset.get("AssetName", "").upper():
            return asset.get("AssetId")
    
    return None


def fetch_assets_by_plant_id(plant_id: str) -> Dict:
    """
    Fetch all power generation assets belonging to a specific CPP plant.
    
    Args:
        plant_id: CPPPLANT_FK_Id (GUID string)
    
    Returns:
        dict with:
        - success: bool
        - message: str
        - assets: list of asset dicts
        - count: int
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        cur.execute("""
            SELECT 
                AssetId,
                AssetName,
                AssetCapacity,
                CPPPLANT_FK_Id,
                PlantCode
            FROM PowerGenerationAssets
            WHERE CPPPLANT_FK_Id = ?
                AND AssetType IN ('GT', 'STG')
            ORDER BY AssetName
        """, (plant_id,))
        
        rows = cur.fetchall()
        
        if not rows:
            return {
                "success": True,
                "message": f"No assets found for plant ID: {plant_id}",
                "assets": [],
                "count": 0,
                "plant_id": plant_id
            }
        
        assets = []
        for row in rows:
            asset_name = row[1]
            asset = {
                "AssetId": str(row[0]) if row[0] else None,
                "AssetName": asset_name,
                "AssetCapacity": float(row[2]) if row[2] else 0.0,
                "CPPPLANT_FK_Id": str(row[3]) if row[3] else None,
                "PlantCode": row[4],
                "asset_type": get_asset_type(asset_name),
                "gt_number": get_gt_number(asset_name),
                "linked_hrsg": get_linked_hrsg(asset_name),
                "is_gt": get_asset_type(asset_name) == "GT",
                "is_stg": get_asset_type(asset_name) == "STG",
            }
            assets.append(asset)
        
        return {
            "success": True,
            "message": f"Found {len(assets)} asset(s) for plant ID: {plant_id}",
            "assets": assets,
            "count": len(assets),
            "plant_id": plant_id
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching assets by plant ID: {str(e)}",
            "assets": [],
            "count": 0,
            "plant_id": plant_id
        }
    finally:
        conn.close()


def get_all_plant_ids() -> List[str]:
    """
    Get all unique CPP plant IDs from the PowerGenerationAssets table.
    
    Returns:
        List of plant IDs (GUID strings)
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        cur.execute("""
            SELECT DISTINCT CPPPLANT_FK_Id
            FROM PowerGenerationAssets
            WHERE CPPPLANT_FK_Id IS NOT NULL
        """)
        
        rows = cur.fetchall()
        return [str(row[0]) for row in rows if row[0]]
        
    except Exception as e:
        print(f"Error fetching plant IDs: {str(e)}")
        return []
    finally:
        conn.close()


# ============================================================
# OPERATIONAL HOURS QUERIES
# ============================================================
def fetch_operational_hours(month: int, year: int) -> Dict:
    """
    Fetch operational hours for all power generation assets for a specific month.
    
    Availability Rule: If OperationalHours > 0, asset is available; otherwise not available.
    
    Args:
        month: Financial month (1-12, where 4=April is start of FY)
        year: Financial year (e.g., 2025)
    
    Returns:
        dict with:
        - success: bool
        - message: str
        - month: int
        - year: int
        - assets: list of dicts with asset details and operational hours
        - available_count: int (assets with hours > 0)
        - unavailable_count: int (assets with hours = 0 or not in table)
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # First get FinancialYearMonth ID
        cur.execute("""
            SELECT Id FROM FinancialYearMonth 
            WHERE Month = ? AND Year = ?
        """, (month, year))
        fym_row = cur.fetchone()
        
        if not fym_row:
            return {
                "success": False,
                "message": f"FinancialYearMonth not found for {month}/{year}",
                "month": month,
                "year": year,
                "assets": [],
                "available_count": 0,
                "unavailable_count": 0
            }
        
        fym_id = fym_row[0]
        
        # Get all power generation assets with their operational hours and capacity
        # AssetCapacity is in AssetAvailability table (MaxOperatingCapacity)
        cur.execute("""
            SELECT 
                p.AssetId,
                p.AssetName,
                COALESCE(aa.MaxOperatingCapacity, 0) as AssetCapacity,
                p.PlantCode,
                p.CPPPLANT_FK_Id,
                COALESCE(oh.OperationalHours, 0) as OperationalHours
            FROM PowerGenerationAssets p with(nolock)
            LEFT JOIN OperationalHours oh with(nolock) ON p.AssetId = oh.Asset_FK_Id 
                AND oh.FinancialMonthId = ?
            LEFT JOIN AssetAvailability aa ON p.AssetId = aa.AssetId
                AND aa.FinancialYearMonthId = ?
            WHERE p.AssetType IN ('GT', 'STG')
            ORDER BY p.AssetName
        """, (fym_id, fym_id))
        
        rows = cur.fetchall()
        
        assets = []
        available_count = 0
        unavailable_count = 0
        
        for row in rows:
            asset_name = row[1]
            op_hours = float(row[5]) if row[5] else 0.0
            is_available = op_hours > 0
            
            if is_available:
                available_count += 1
            else:
                unavailable_count += 1
            
            asset = {
                "AssetId": str(row[0]) if row[0] else None,
                "AssetName": asset_name,
                "AssetCapacity": float(row[2]) if row[2] else 0.0,
                "PlantCode": row[3],
                "CPPPLANT_FK_Id": str(row[4]) if row[4] else None,
                "OperationalHours": op_hours,
                "IsAvailable": is_available,
                "asset_type": get_asset_type(asset_name),
                "gt_number": get_gt_number(asset_name),
                "linked_hrsg": get_linked_hrsg(asset_name),
                "is_gt": get_asset_type(asset_name) == "GT",
                "is_stg": get_asset_type(asset_name) == "STG",
            }
            assets.append(asset)
        
        # Sort: GTs first (by GT number), then STG
        assets.sort(key=lambda x: (
            0 if x["is_gt"] else 1,
            x["gt_number"] if x["gt_number"] else 999,
            x.get("AssetName", "")
        ))
        
        return {
            "success": True,
            "message": f"Found {len(assets)} asset(s) for {month}/{year}",
            "month": month,
            "year": year,
            "fym_id": str(fym_id),
            "assets": assets,
            "available_count": available_count,
            "unavailable_count": unavailable_count
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching operational hours: {str(e)}",
            "month": month,
            "year": year,
            "assets": [],
            "available_count": 0,
            "unavailable_count": 0
        }
    finally:
        conn.close()


def get_available_assets(month: int, year: int) -> List[Dict]:
    """
    Get only available assets (OperationalHours > 0) for a specific month.
    
    Args:
        month: Financial month (1-12)
        year: Financial year
    
    Returns:
        List of available asset dicts
    """
    result = fetch_operational_hours(month, year)
    if not result.get("success"):
        return []
    return [a for a in result.get("assets", []) if a.get("IsAvailable")]


def get_asset_operational_hours(asset_name: str, month: int, year: int) -> Optional[float]:
    """
    Get operational hours for a specific asset in a specific month.
    
    Args:
        asset_name: Full or partial asset name
        month: Financial month
        year: Financial year
    
    Returns:
        Operational hours (float) or None if not found
    """
    result = fetch_operational_hours(month, year)
    if not result.get("success"):
        return None
    
    name_upper = asset_name.upper()
    for asset in result.get("assets", []):
        if name_upper in asset.get("AssetName", "").upper():
            return asset.get("OperationalHours")
    
    return None


def print_operational_hours_summary(month: int, year: int):
    """
    Print a summary of operational hours for a specific month.
    """
    result = fetch_operational_hours(month, year)
    
    print("\n" + "="*100)
    print(f"OPERATIONAL HOURS - {month}/{year}")
    print("="*100)
    
    if not result["success"]:
        print(f"ERROR: {result['message']}")
        return result
    
    print(f"\nAvailable: {result['available_count']} | Unavailable: {result['unavailable_count']}")
    
    print("\n" + "-"*100)
    print(f"{'Asset Name':<25} {'Type':<8} {'GT#':<5} {'Capacity':<12} {'Op Hours':<12} {'Status':<15}")
    print("-"*100)
    
    for asset in result["assets"]:
        asset_name = asset.get('AssetName', 'N/A')
        asset_type = asset.get('asset_type', 'N/A')
        gt_num = asset.get('gt_number', '-')
        capacity = asset.get('AssetCapacity', 0)
        op_hours = asset.get('OperationalHours', 0)
        status = "AVAILABLE" if asset.get('IsAvailable') else "NOT AVAILABLE"
        
        print(f"{asset_name:<25} {asset_type:<8} {str(gt_num):<5} {capacity:<12.2f} {op_hours:<12.1f} {status:<15}")
    
    print("-"*100)
    print("="*100 + "\n")
    return result


# ============================================================
# ASSET AVAILABILITY QUERIES (Priority & Capacity)
# ============================================================
def fetch_asset_availability(month: int, year: int) -> Dict:
    """
    Fetch asset availability data (priority, min/max capacity) for a specific month.
    
    This provides:
    - Priority: Dispatch priority for the month (lower number = higher priority)
    - MinOperatingCapacity: Actual min capacity for that specific month (MW)
    - MaxOperatingCapacity: Actual max capacity for that specific month (MW)
    - FixedMin: Fixed minimum capacity for whole year (MW)
    - FixedMax: Fixed maximum capacity for whole year (MW)
    
    Note: Does NOT use IsAssetAvailable or operationalHours from this table.
          Availability is determined from OperationalHours table.
    
    Args:
        month: Financial month (1-12)
        year: Financial year (e.g., 2025)
    
    Returns:
        dict with:
        - success: bool
        - message: str
        - month: int
        - year: int
        - assets: list of dicts with asset details
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # First get FinancialYearMonth ID
        cur.execute("""
            SELECT Id FROM FinancialYearMonth 
            WHERE Month = ? AND Year = ?
        """, (month, year))
        fym_row = cur.fetchone()
        
        if not fym_row:
            return {
                "success": False,
                "message": f"FinancialYearMonth not found for {month}/{year}",
                "month": month,
                "year": year,
                "assets": []
            }
        
        fym_id = fym_row[0]
        
        # Get all power generation assets with their availability data (LEFT JOIN)
        # Note: AssetCapacity is in AssetAvailability.MaxOperatingCapacity, not in PowerGenerationAssets
        cur.execute("""
            SELECT 
                p.AssetId,
                p.AssetName,
                COALESCE(aa.MaxOperatingCapacity, 0) as AssetCapacity,
                p.PlantCode,
                aa.Priority,
                aa.MinOperatingCapacity,
                aa.MaxOperatingCapacity,
                aa.FixedMin,
                aa.FixedMax
            FROM PowerGenerationAssets p
            LEFT JOIN AssetAvailability aa ON p.AssetId = aa.AssetId 
                AND aa.FinancialYearMonthId = ?
            WHERE p.AssetType IN ('GT', 'STG')
            ORDER BY aa.Priority, p.AssetName
        """, (fym_id,))
        
        rows = cur.fetchall()
        
        assets = []
        for row in rows:
            asset_name = row[1]
            
            # Handle None values
            priority = row[4]  # Can be None
            min_op = float(row[5]) if row[5] is not None else None
            max_op = float(row[6]) if row[6] is not None else None
            fixed_min = float(row[7]) if row[7] is not None else None
            fixed_max = float(row[8]) if row[8] is not None else None
            
            asset = {
                "AssetId": str(row[0]) if row[0] else None,
                "AssetName": asset_name,
                "AssetCapacity": float(row[2]) if row[2] else 0.0,
                "PlantCode": row[3],
                "Priority": priority,
                "MinOperatingCapacity": min_op,
                "MaxOperatingCapacity": max_op,
                "FixedMin": fixed_min,
                "FixedMax": fixed_max,
                "asset_type": get_asset_type(asset_name),
                "gt_number": get_gt_number(asset_name),
                "linked_hrsg": get_linked_hrsg(asset_name),
                "is_gt": get_asset_type(asset_name) == "GT",
                "is_stg": get_asset_type(asset_name) == "STG",
            }
            assets.append(asset)
        
        return {
            "success": True,
            "message": f"Found {len(assets)} asset(s) for {month}/{year}",
            "month": month,
            "year": year,
            "fym_id": str(fym_id),
            "assets": assets
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching asset availability: {str(e)}",
            "month": month,
            "year": year,
            "assets": []
        }
    finally:
        conn.close()


def get_asset_priority(asset_name: str, month: int, year: int) -> Optional[int]:
    """
    Get priority for a specific asset in a specific month.
    
    Args:
        asset_name: Full or partial asset name
        month: Financial month
        year: Financial year
    
    Returns:
        Priority (int) or None if not found
    """
    result = fetch_asset_availability(month, year)
    if not result.get("success"):
        return None
    
    name_upper = asset_name.upper()
    for asset in result.get("assets", []):
        if name_upper in asset.get("AssetName", "").upper():
            return asset.get("Priority")
    
    return None


def get_asset_capacity_limits(asset_name: str, month: int, year: int) -> Dict:
    """
    Get capacity limits for a specific asset in a specific month.
    
    Args:
        asset_name: Full or partial asset name
        month: Financial month
        year: Financial year
    
    Returns:
        dict with MinOperatingCapacity, MaxOperatingCapacity, FixedMin, FixedMax
    """
    result = fetch_asset_availability(month, year)
    if not result.get("success"):
        return {
            "MinOperatingCapacity": None,
            "MaxOperatingCapacity": None,
            "FixedMin": None,
            "FixedMax": None
        }
    
    name_upper = asset_name.upper()
    for asset in result.get("assets", []):
        if name_upper in asset.get("AssetName", "").upper():
            return {
                "MinOperatingCapacity": asset.get("MinOperatingCapacity"),
                "MaxOperatingCapacity": asset.get("MaxOperatingCapacity"),
                "FixedMin": asset.get("FixedMin"),
                "FixedMax": asset.get("FixedMax")
            }
    
    return {
        "MinOperatingCapacity": None,
        "MaxOperatingCapacity": None,
        "FixedMin": None,
        "FixedMax": None
    }


def print_asset_availability_summary(month: int, year: int):
    """
    Print a summary of asset availability (priority & capacity) for a specific month.
    """
    result = fetch_asset_availability(month, year)
    
    print("\n" + "="*120)
    print(f"ASSET AVAILABILITY (Priority & Capacity) - {month}/{year}")
    print("="*120)
    
    if not result["success"]:
        print(f"ERROR: {result['message']}")
        return result
    
    print("\n" + "-"*120)
    header = f"{'Asset Name':<25} {'Type':<6} {'Priority':<10} {'MinOp (MW)':<12} {'MaxOp (MW)':<12} {'FixedMin':<12} {'FixedMax':<12}"
    print(header)
    print("-"*120)
    
    for asset in result["assets"]:
        asset_name = asset.get('AssetName', 'N/A')
        asset_type = asset.get('asset_type', 'N/A')
        priority = asset.get('Priority')
        pri_str = str(priority) if priority is not None else "-"
        min_op = asset.get('MinOperatingCapacity')
        max_op = asset.get('MaxOperatingCapacity')
        fixed_min = asset.get('FixedMin')
        fixed_max = asset.get('FixedMax')
        
        min_op_str = f"{min_op:.2f}" if min_op is not None else "-"
        max_op_str = f"{max_op:.2f}" if max_op is not None else "-"
        fixed_min_str = f"{fixed_min:.2f}" if fixed_min is not None else "-"
        fixed_max_str = f"{fixed_max:.2f}" if fixed_max is not None else "-"
        
        print(f"{asset_name:<25} {asset_type:<6} {pri_str:<10} {min_op_str:<12} {max_op_str:<12} {fixed_min_str:<12} {fixed_max_str:<12}")
    
    print("-"*120)
    print("="*120 + "\n")
    return result


# ============================================================
# COMBINED ASSET DATA (All tables joined)
# ============================================================
def fetch_complete_asset_data(month: int, year: int) -> Dict:
    """
    Fetch complete asset data combining:
    - PowerGenerationAssets (base info)
    - OperationalHours (hours & availability)
    - AssetAvailability (priority & capacity limits)
    
    This is the main function to get all asset data for a month.
    
    Args:
        month: Financial month (1-12)
        year: Financial year
    
    Returns:
        dict with complete asset data
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # First get FinancialYearMonth ID
        cur.execute("""
            SELECT Id FROM FinancialYearMonth 
            WHERE Month = ? AND Year = ?
        """, (month, year))
        fym_row = cur.fetchone()
        
        if not fym_row:
            return {
                "success": False,
                "message": f"FinancialYearMonth not found for {month}/{year}",
                "month": month,
                "year": year,
                "assets": [],
                "available_assets": [],
                "available_count": 0,
                "unavailable_count": 0
            }
        
        fym_id = fym_row[0]
        
        # Get all data joined
        cur.execute("""
            SELECT 
                p.AssetId,
                p.AssetName,
                p.AssetCapacity,
                p.PlantCode,
                p.CPPPLANT_FK_Id,
                COALESCE(oh.OperationalHours, 0) as OperationalHours,
                aa.Priority,
                aa.MinOperatingCapacity,
                aa.MaxOperatingCapacity,
                aa.FixedMin,
                aa.FixedMax
            FROM PowerGenerationAssets p with(nolock)
            LEFT JOIN OperationalHours oh with(nolock) ON p.AssetId = oh.Asset_FK_Id 
                AND oh.FinancialMonthId = ?
            LEFT JOIN AssetAvailability aa ON p.AssetId = aa.AssetId 
                AND aa.FinancialYearMonthId = ?
            WHERE p.AssetType IN ('GT', 'STG')
            ORDER BY aa.Priority, p.AssetName
        """, (fym_id, fym_id))
        
        rows = cur.fetchall()
        
        assets = []
        available_assets = []
        available_count = 0
        unavailable_count = 0
        
        for row in rows:
            asset_name = row[1]
            op_hours = float(row[5]) if row[5] else 0.0
            is_available = op_hours > 0
            
            if is_available:
                available_count += 1
            else:
                unavailable_count += 1
            
            # Handle None values for capacity
            priority = row[6]
            min_op = float(row[7]) if row[7] is not None else None
            max_op = float(row[8]) if row[8] is not None else None
            fixed_min = float(row[9]) if row[9] is not None else None
            fixed_max = float(row[10]) if row[10] is not None else None
            
            asset = {
                "AssetId": str(row[0]) if row[0] else None,
                "AssetName": asset_name,
                "AssetCapacity": float(row[2]) if row[2] else 0.0,
                "PlantCode": row[3],
                "CPPPLANT_FK_Id": str(row[4]) if row[4] else None,
                "OperationalHours": op_hours,
                "IsAvailable": is_available,
                "Priority": priority,
                "MinOperatingCapacity": min_op,
                "MaxOperatingCapacity": max_op,
                "FixedMin": fixed_min,
                "FixedMax": fixed_max,
                "asset_type": get_asset_type(asset_name),
                "gt_number": get_gt_number(asset_name),
                "linked_hrsg": get_linked_hrsg(asset_name),
                "is_gt": get_asset_type(asset_name) == "GT",
                "is_stg": get_asset_type(asset_name) == "STG",
            }
            assets.append(asset)
            
            if is_available:
                available_assets.append(asset)
        
        # Sort available assets by priority (None priorities go last)
        available_assets.sort(key=lambda x: (
            x["Priority"] if x["Priority"] is not None else 999,
            0 if x["is_gt"] else 1,
            x["gt_number"] if x["gt_number"] else 999
        ))
        
        return {
            "success": True,
            "message": f"Found {len(assets)} asset(s) for {month}/{year}",
            "month": month,
            "year": year,
            "fym_id": str(fym_id),
            "assets": assets,
            "available_assets": available_assets,
            "available_count": available_count,
            "unavailable_count": unavailable_count
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching complete asset data: {str(e)}",
            "month": month,
            "year": year,
            "assets": [],
            "available_assets": [],
            "available_count": 0,
            "unavailable_count": 0
        }
    finally:
        conn.close()


def print_complete_asset_summary(month: int, year: int):
    """
    Print a complete summary of all asset data for a specific month.
    """
    result = fetch_complete_asset_data(month, year)
    
    print("\n" + "="*140)
    print(f"COMPLETE ASSET DATA - {month}/{year}")
    print("="*140)
    
    if not result["success"]:
        print(f"ERROR: {result['message']}")
        return result
    
    print(f"\nAvailable: {result['available_count']} | Unavailable: {result['unavailable_count']}")
    
    print("\n" + "-"*140)
    header = f"{'Asset Name':<25} {'Type':<6} {'Hours':<8} {'Status':<12} {'Priority':<10} {'MinOp':<10} {'MaxOp':<10} {'FixMin':<10} {'FixMax':<10}"
    print(header)
    print("-"*140)
    
    for asset in result["assets"]:
        asset_name = asset.get('AssetName', 'N/A')
        asset_type = asset.get('asset_type', 'N/A')
        op_hours = asset.get('OperationalHours', 0)
        status = "AVAIL" if asset.get('IsAvailable') else "NOT AVAIL"
        priority = asset.get('Priority')
        pri_str = str(priority) if priority is not None else "-"
        
        min_op = asset.get('MinOperatingCapacity')
        max_op = asset.get('MaxOperatingCapacity')
        fixed_min = asset.get('FixedMin')
        fixed_max = asset.get('FixedMax')
        
        min_op_str = f"{min_op:.1f}" if min_op is not None else "-"
        max_op_str = f"{max_op:.1f}" if max_op is not None else "-"
        fixed_min_str = f"{fixed_min:.1f}" if fixed_min is not None else "-"
        fixed_max_str = f"{fixed_max:.1f}" if fixed_max is not None else "-"
        
        print(f"{asset_name:<25} {asset_type:<6} {op_hours:<8.1f} {status:<12} {pri_str:<10} {min_op_str:<10} {max_op_str:<10} {fixed_min_str:<10} {fixed_max_str:<10}")
    
    print("-"*140)
    print("="*140 + "\n")
    return result


# ============================================================
# IMPORT POWER QUERIES (PlantImportMapping)
# ============================================================
import calendar


def get_month_hours(month: int, year: int) -> int:
    """
    Get operational hours for a month based on number of days.
    
    TEMPORARY: Until operational hours table for import power is ready,
    calculate based on month days × 24 hours.
    
    Args:
        month: Month (1-12)
        year: Year
    
    Returns:
        Hours in the month (e.g., 720 for 30-day month, 744 for 31-day month)
    """
    days_in_month = calendar.monthrange(year, month)[1]
    return days_in_month * 24


def fetch_import_power(month: int, year: int) -> Dict:
    """
    Fetch import power data for a specific month from PlantImportMapping.
    
    Import Power (MWh) = Value (MW) × Operational Hours
    
    TEMPORARY: Operational hours calculated from month days until
    the operational hours table for import power is ready.
    
    Args:
        month: Financial month (1-12)
        year: Financial year
    
    Returns:
        dict with:
        - success: bool
        - message: str
        - month: int
        - year: int
        - import_power_mw: float (capacity in MW)
        - operational_hours: int (hours in month)
        - import_power_mwh: float (total energy = MW × hours)
        - uom: str
        - asset_id: str (import asset ID)
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # Get import power for the month
        cur.execute("""
            SELECT 
                pim.AssetId,
                pim.Value,
                pim.UOM,
                fym.Month,
                fym.Year
            FROM PlantImportMapping pim
            JOIN FinancialYearMonth fym ON pim.FinancialMonthId = fym.Id
            WHERE fym.Month = ? AND fym.Year = ?
        """, (month, year))
        
        row = cur.fetchone()
        
        if not row:
            return {
                "success": False,
                "message": f"No import power data found for {month}/{year}",
                "month": month,
                "year": year,
                "import_power_mw": 0.0,
                "operational_hours": 0,
                "import_power_mwh": 0.0,
                "uom": "MW",
                "asset_id": None
            }
        
        asset_id = str(row[0]) if row[0] else None
        import_power_mw = float(row[1]) if row[1] else 0.0
        uom = row[2] or "MW"
        
        # Calculate operational hours (TEMPORARY: based on month days)
        operational_hours = get_month_hours(month, year)
        
        # Calculate total import power in MWh
        import_power_mwh = import_power_mw * operational_hours
        
        return {
            "success": True,
            "message": f"Import power for {month}/{year}: {import_power_mw} MW × {operational_hours} hrs = {import_power_mwh} MWh",
            "month": month,
            "year": year,
            "import_power_mw": import_power_mw,
            "operational_hours": operational_hours,
            "import_power_mwh": import_power_mwh,
            "uom": uom,
            "asset_id": asset_id,
            "hours_source": "TEMPORARY (month days × 24)"  # Will change when op hours table is ready
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching import power: {str(e)}",
            "month": month,
            "year": year,
            "import_power_mw": 0.0,
            "operational_hours": 0,
            "import_power_mwh": 0.0,
            "uom": "MW",
            "asset_id": None
        }
    finally:
        conn.close()


def get_import_power_mwh(month: int, year: int) -> float:
    """
    Get total import power in MWh for a specific month.
    
    Simple convenience function that returns just the MWh value.
    
    Args:
        month: Financial month
        year: Financial year
    
    Returns:
        Import power in MWh, or 0.0 if not found
    """
    result = fetch_import_power(month, year)
    return result.get("import_power_mwh", 0.0) if result.get("success") else 0.0


def fetch_import_power_for_year(year: int) -> Dict:
    """
    Fetch import power data for all months in a financial year (April to March).
    
    Args:
        year: Starting year of financial year (e.g., 2025 for FY 2025-26)
    
    Returns:
        dict with monthly import power data
    """
    conn = get_connection()
    cur = conn.cursor()
    
    try:
        # Financial year: April (month 4) of year to March (month 3) of year+1
        monthly_data = []
        total_mwh = 0.0
        
        # April to December of starting year
        for m in range(4, 13):
            result = fetch_import_power(m, year)
            if result["success"]:
                monthly_data.append({
                    "month": m,
                    "year": year,
                    "import_mw": result["import_power_mw"],
                    "hours": result["operational_hours"],
                    "import_mwh": result["import_power_mwh"]
                })
                total_mwh += result["import_power_mwh"]
        
        # January to March of next year
        for m in range(1, 4):
            result = fetch_import_power(m, year + 1)
            if result["success"]:
                monthly_data.append({
                    "month": m,
                    "year": year + 1,
                    "import_mw": result["import_power_mw"],
                    "hours": result["operational_hours"],
                    "import_mwh": result["import_power_mwh"]
                })
                total_mwh += result["import_power_mwh"]
        
        return {
            "success": True,
            "message": f"Import power for FY {year}-{(year+1) % 100:02d}",
            "financial_year": f"{year}-{(year+1) % 100:02d}",
            "monthly_data": monthly_data,
            "total_mwh": total_mwh,
            "months_found": len(monthly_data)
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"Error fetching yearly import power: {str(e)}",
            "financial_year": f"{year}-{(year+1) % 100:02d}",
            "monthly_data": [],
            "total_mwh": 0.0,
            "months_found": 0
        }
    finally:
        conn.close()


def print_import_power_summary(month: int, year: int):
    """
    Print import power summary for a specific month.
    """
    result = fetch_import_power(month, year)
    
    print("\n" + "="*80)
    print(f"IMPORT POWER - {month}/{year}")
    print("="*80)
    
    if not result["success"]:
        print(f"ERROR: {result['message']}")
        return result
    
    print(f"\n  Import Power Capacity:  {result['import_power_mw']:>10.2f} MW")
    print(f"  Operational Hours:      {result['operational_hours']:>10} hrs  ({result.get('hours_source', 'N/A')})")
    print(f"  ─────────────────────────────────────────")
    print(f"  Total Import Power:     {result['import_power_mwh']:>10.2f} MWh")
    print(f"\n  Asset ID: {result['asset_id']}")
    
    print("="*80 + "\n")
    return result


def print_yearly_import_power_summary(year: int):
    """
    Print import power summary for a full financial year.
    """
    result = fetch_import_power_for_year(year)
    
    print("\n" + "="*100)
    print(f"IMPORT POWER - Financial Year {result['financial_year']}")
    print("="*100)
    
    if not result["success"]:
        print(f"ERROR: {result['message']}")
        return result
    
    print("\n" + "-"*100)
    print(f"{'Month':<10} {'Year':<8} {'Import (MW)':<15} {'Hours':<10} {'Import (MWh)':<15}")
    print("-"*100)
    
    for data in result["monthly_data"]:
        month_name = calendar.month_abbr[data['month']]
        print(f"{month_name:<10} {data['year']:<8} {data['import_mw']:<15.2f} {data['hours']:<10} {data['import_mwh']:<15.2f}")
    
    print("-"*100)
    print(f"{'TOTAL':<10} {'':<8} {'':<15} {'':<10} {result['total_mwh']:<15.2f}")
    print("="*100 + "\n")
    return result


# ============================================================
# STG EXTRACTION LOOKUP
# ============================================================
# Fetches STG extraction data (LP/MP extraction rates based on STG load)
# from STGExtractionLookup table
#
# Table Structure:
#   STGExtractionLookup
#   ├─ Id (uniqueidentifier) - Primary Key
#   ├─ LoadMW (decimal) - STG Load in MW (11.0 to 25.0)
#   ├─ SVHInletTPH (decimal) - SHP Inlet Flow (TPH) - Column B
#   ├─ SMBleedFlowTPH (decimal) - MP Extraction (TPH) - Column D
#   ├─ SLExtFlowTPH (decimal) - LP Extraction (TPH) - Column C
#   ├─ CondensingLoadM3Hr (decimal) - Condensing Load (m³/hr)
#   ├─ HeatRateKcalKWH (decimal) - Heat Rate (Kcal/KWH)
#   ├─ EqSvhMp (decimal) - Equivalent SHP for MP extraction (TPH) - Column H
#   ├─ EqSvhLp (decimal) - Equivalent SHP for LP extraction (TPH) - Column I
#   ├─ SteamForPower (decimal) - Steam used for power generation (TPH) - Column J
#   └─ SpSteamPower (decimal) - Specific steam consumption (MT/MWh) - Column K
#
# Usage:
#   - LP from STG (MT/month) = SLExtFlowTPH × STG Operating Hours
#   - MP from STG (MT/month) = SMBleedFlowTPH × STG Operating Hours
#   - STG SHP Steam_Dis (MT/month) = SteamForPower × STG Operating Hours
#   - Remaining LP/MP demand comes from PRDS
# ============================================================

def fetch_stg_extraction_lookup() -> pd.DataFrame:
    """
    Fetch all STG extraction lookup data from database.
    
    Returns:
        DataFrame with columns: LoadMW, SVHInletTPH, SMBleedFlowTPH, SLExtFlowTPH, 
                               CondensingLoadM3Hr, HeatRateKcalKWH, EqSvhMp, EqSvhLp,
                               SteamForPower, SpSteamPower
        Sorted by LoadMW ascending.
    """
    conn = get_connection()
    cur = conn.cursor()
    
    cur.execute("""
        SELECT 
            LoadMW,
            SVHInletTPH,
            SMBleedFlowTPH,
            SLExtFlowTPH,
            CondensingLoadM3Hr,
            HeatRateKcalKWH,
            ISNULL(EqSvhMp, 0) AS EqSvhMp,
            ISNULL(EqSvhLp, 0) AS EqSvhLp,
            ISNULL(SteamForPower, 0) AS SteamForPower,
            ISNULL(SpSteamPower, 0) AS SpSteamPower
        FROM STGExtractionLookup
        ORDER BY LoadMW ASC
    """)
    
    rows = cur.fetchall()
    cols = ["LoadMW", "SVHInletTPH", "SMBleedFlowTPH", "SLExtFlowTPH", 
            "CondensingLoadM3Hr", "HeatRateKcalKWH", "EqSvhMp", "EqSvhLp",
            "SteamForPower", "SpSteamPower"]
    
    conn.close()
    
    if not rows:
        return pd.DataFrame(columns=cols)
    
    df = pd.DataFrame.from_records(rows, columns=cols)
    
    # Convert to float
    for col in cols:
        df[col] = df[col].astype(float)
    
    return df


def get_stg_extraction_for_load(stg_load_mw: float, lookup_df: pd.DataFrame = None) -> dict:
    """
    Get LP and MP extraction rates for a given STG load using interpolation.
    
    Args:
        stg_load_mw: STG load in MW
        lookup_df: Optional pre-fetched lookup DataFrame (for efficiency in iterations)
    
    Returns:
        dict with:
            - lp_extraction_tph: LP extraction rate (TPH) - SLExtFlowTPH
            - mp_extraction_tph: MP extraction rate (TPH) - SMBleedFlowTPH
            - shp_inlet_tph: SHP inlet flow (TPH) - SVHInletTPH
            - heat_rate: Heat rate (Kcal/KWH)
            - eq_svh_mp_tph: Equivalent SHP for MP extraction (TPH) - EqSvhMp
            - eq_svh_lp_tph: Equivalent SHP for LP extraction (TPH) - EqSvhLp
            - steam_for_power_tph: Steam for power generation (TPH) - SteamForPower
            - sp_steam_power: Specific steam consumption (MT/MWh) - SpSteamPower
            - load_mw: Actual load used (may be clamped to min/max)
    """
    # Fetch lookup table if not provided
    if lookup_df is None or lookup_df.empty:
        lookup_df = fetch_stg_extraction_lookup()
    
    if lookup_df.empty:
        return {
            "lp_extraction_tph": 0.0,
            "mp_extraction_tph": 0.0,
            "shp_inlet_tph": 0.0,
            "condensing_load_m3hr": 0.0,
            "heat_rate": 0.0,
            "eq_svh_mp_tph": 0.0,
            "eq_svh_lp_tph": 0.0,
            "steam_for_power_tph": 0.0,
            "sp_steam_power": 0.0,
            "load_mw": 0.0,
            "interpolated": False
        }
    
    min_load = lookup_df["LoadMW"].min()
    max_load = lookup_df["LoadMW"].max()
    
    # Handle edge cases - clamp to min/max
    if stg_load_mw <= 0:
        return {
            "lp_extraction_tph": 0.0,
            "mp_extraction_tph": 0.0,
            "shp_inlet_tph": 0.0,
            "condensing_load_m3hr": 0.0,
            "heat_rate": 0.0,
            "eq_svh_mp_tph": 0.0,
            "eq_svh_lp_tph": 0.0,
            "steam_for_power_tph": 0.0,
            "sp_steam_power": 0.0,
            "load_mw": 0.0,
            "interpolated": False
        }
    
    if stg_load_mw < min_load:
        # Use minimum load values
        row = lookup_df[lookup_df["LoadMW"] == min_load].iloc[0]
        return {
            "lp_extraction_tph": row["SLExtFlowTPH"],
            "mp_extraction_tph": row["SMBleedFlowTPH"],
            "shp_inlet_tph": row["SVHInletTPH"],
            "condensing_load_m3hr": row["CondensingLoadM3Hr"],
            "heat_rate": row["HeatRateKcalKWH"],
            "eq_svh_mp_tph": row["EqSvhMp"],
            "eq_svh_lp_tph": row["EqSvhLp"],
            "steam_for_power_tph": row["SteamForPower"],
            "sp_steam_power": row["SpSteamPower"],
            "load_mw": min_load,
            "interpolated": False,
            "clamped": "min"
        }
    
    if stg_load_mw > max_load:
        # Use maximum load values
        row = lookup_df[lookup_df["LoadMW"] == max_load].iloc[0]
        return {
            "lp_extraction_tph": row["SLExtFlowTPH"],
            "mp_extraction_tph": row["SMBleedFlowTPH"],
            "shp_inlet_tph": row["SVHInletTPH"],
            "condensing_load_m3hr": row["CondensingLoadM3Hr"],
            "heat_rate": row["HeatRateKcalKWH"],
            "eq_svh_mp_tph": row["EqSvhMp"],
            "eq_svh_lp_tph": row["EqSvhLp"],
            "steam_for_power_tph": row["SteamForPower"],
            "sp_steam_power": row["SpSteamPower"],
            "load_mw": max_load,
            "interpolated": False,
            "clamped": "max"
        }
    
    # Check for exact match
    exact_match = lookup_df[lookup_df["LoadMW"] == stg_load_mw]
    if not exact_match.empty:
        row = exact_match.iloc[0]
        return {
            "lp_extraction_tph": row["SLExtFlowTPH"],
            "mp_extraction_tph": row["SMBleedFlowTPH"],
            "shp_inlet_tph": row["SVHInletTPH"],
            "condensing_load_m3hr": row["CondensingLoadM3Hr"],
            "heat_rate": row["HeatRateKcalKWH"],
            "eq_svh_mp_tph": row["EqSvhMp"],
            "eq_svh_lp_tph": row["EqSvhLp"],
            "steam_for_power_tph": row["SteamForPower"],
            "sp_steam_power": row["SpSteamPower"],
            "load_mw": stg_load_mw,
            "interpolated": False
        }
    
    # Linear interpolation between two closest points
    lower_df = lookup_df[lookup_df["LoadMW"] < stg_load_mw]
    upper_df = lookup_df[lookup_df["LoadMW"] > stg_load_mw]
    
    if lower_df.empty or upper_df.empty:
        # Fallback to nearest
        nearest_idx = (lookup_df["LoadMW"] - stg_load_mw).abs().idxmin()
        row = lookup_df.loc[nearest_idx]
        return {
            "lp_extraction_tph": row["SLExtFlowTPH"],
            "mp_extraction_tph": row["SMBleedFlowTPH"],
            "shp_inlet_tph": row["SVHInletTPH"],
            "condensing_load_m3hr": row["CondensingLoadM3Hr"],
            "heat_rate": row["HeatRateKcalKWH"],
            "eq_svh_mp_tph": row["EqSvhMp"],
            "eq_svh_lp_tph": row["EqSvhLp"],
            "steam_for_power_tph": row["SteamForPower"],
            "sp_steam_power": row["SpSteamPower"],
            "load_mw": row["LoadMW"],
            "interpolated": False
        }
    
    lower_row = lower_df.iloc[-1]  # Highest load below target
    upper_row = upper_df.iloc[0]   # Lowest load above target
    
    # Interpolation factor
    load_range = upper_row["LoadMW"] - lower_row["LoadMW"]
    factor = (stg_load_mw - lower_row["LoadMW"]) / load_range if load_range > 0 else 0
    
    # Interpolate each value
    def interpolate(lower_val, upper_val):
        return lower_val + factor * (upper_val - lower_val)
    
    return {
        "lp_extraction_tph": interpolate(lower_row["SLExtFlowTPH"], upper_row["SLExtFlowTPH"]),
        "mp_extraction_tph": interpolate(lower_row["SMBleedFlowTPH"], upper_row["SMBleedFlowTPH"]),
        "shp_inlet_tph": interpolate(lower_row["SVHInletTPH"], upper_row["SVHInletTPH"]),
        "condensing_load_m3hr": interpolate(lower_row["CondensingLoadM3Hr"], upper_row["CondensingLoadM3Hr"]),
        "heat_rate": interpolate(lower_row["HeatRateKcalKWH"], upper_row["HeatRateKcalKWH"]),
        "eq_svh_mp_tph": interpolate(lower_row["EqSvhMp"], upper_row["EqSvhMp"]),
        "eq_svh_lp_tph": interpolate(lower_row["EqSvhLp"], upper_row["EqSvhLp"]),
        "steam_for_power_tph": interpolate(lower_row["SteamForPower"], upper_row["SteamForPower"]),
        "sp_steam_power": interpolate(lower_row["SpSteamPower"], upper_row["SpSteamPower"]),
        "load_mw": stg_load_mw,
        "interpolated": True,
        "lower_load": lower_row["LoadMW"],
        "upper_load": upper_row["LoadMW"]
    }


def get_stg_operating_hours(month: int, year: int) -> float:
    """
    Get STG operating hours for a specific month.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025)
    
    Returns:
        Operating hours for STG (0 if not available)
    """
    conn = get_connection()
    cur = conn.cursor()
    
    # Get FinancialYearMonth ID
    cur.execute("""
        SELECT Id FROM FinancialYearMonth 
        WHERE Month = ? AND Year = ?
    """, (month, year))
    fym_row = cur.fetchone()
    
    if not fym_row:
        conn.close()
        return 0.0
    
    fym_id = fym_row[0]
    
    # Get STG operating hours
    cur.execute("""
        SELECT oh.OperationalHours
        FROM OperationalHours oh with(nolock)
        INNER JOIN PowerGenerationAssets p with(nolock) ON p.AssetId = oh.Asset_FK_Id
        WHERE oh.FinancialMonthId = ?
          AND (p.AssetName LIKE '%STG%' OR p.PlantCode = '40NE')
    """, (fym_id,))
    
    row = cur.fetchone()
    conn.close()
    
    if row and row[0]:
        return float(row[0])
    
    return 0.0


def print_stg_extraction_lookup_summary():
    """Print a summary of STG extraction lookup table."""
    df = fetch_stg_extraction_lookup()
    
    print("\n" + "="*100)
    print("STG EXTRACTION LOOKUP TABLE")
    print("="*100)
    
    if df.empty:
        print("  No data found in STGExtractionLookup table")
        return
    
    print(f"{'Load (MW)':<12} {'SHP Inlet':<12} {'MP Ext':<12} {'LP Ext':<12} {'Condensing':<12} {'Heat Rate':<12}")
    print(f"{'':12} {'(TPH)':12} {'(TPH)':12} {'(TPH)':12} {'(m³/hr)':12} {'(Kcal/KWH)':12}")
    print("-"*100)
    
    for _, row in df.iterrows():
        print(f"{row['LoadMW']:<12.1f} {row['SVHInletTPH']:<12.1f} {row['SMBleedFlowTPH']:<12.1f} "
              f"{row['SLExtFlowTPH']:<12.1f} {row['CondensingLoadM3Hr']:<12.1f} {row['HeatRateKcalKWH']:<12.0f}")
    
    print("="*100 + "\n")


# ============================================================
# TEST FUNCTION
# ============================================================
if __name__ == "__main__":
    print("Testing Power Generation Assets Queries...")
    print_power_assets_summary()
    
    # Test convenience functions
    print("\n--- Testing Convenience Functions ---")
    
    gt_assets = get_gt_assets()
    print(f"\nGT Assets ({len(gt_assets)}):")
    for gt in gt_assets:
        print(f"  - {gt['AssetName']} (GT{gt['gt_number']}) -> {gt['linked_hrsg']}")
    
    stg = get_stg_asset()
    if stg:
        print(f"\nSTG Asset: {stg['AssetName']} ({stg['AssetCapacity']} MW)")
    
    # Test asset ID lookup
    asset_id = get_asset_id_by_name("STG")
    print(f"\nSTG Asset ID: {asset_id}")
    
    # Test plant ID functions
    print("\n--- Testing Plant ID Functions ---")
    plant_ids = get_all_plant_ids()
    print(f"\nPlant IDs found: {len(plant_ids)}")
    
    for plant_id in plant_ids:
        result = fetch_assets_by_plant_id(plant_id)
        print(f"\nPlant: {plant_id[:8]}...")
        print(f"  Assets: {result['count']}")
        for asset in result['assets']:
            print(f"    - {asset['AssetName']} ({asset['asset_type']}, {asset['AssetCapacity']} MW)")
    
    # Test operational hours
    print("\n--- Testing Operational Hours ---")
    print_operational_hours_summary(4, 2025)
    
    # Test asset availability (priority & capacity)
    print("\n--- Testing Asset Availability (Priority & Capacity) ---")
    print_asset_availability_summary(4, 2025)
    
    # Test complete asset data (combined)
    print("\n--- Testing Complete Asset Data ---")
    print_complete_asset_summary(4, 2025)
    
    # Test specific asset functions
    print("\n--- Testing Specific Asset Functions ---")
    priority = get_asset_priority("STG", 4, 2025)
    print(f"STG Priority for April 2025: {priority}")
    
    limits = get_asset_capacity_limits("STG", 4, 2025)
    print(f"STG Capacity Limits: {limits}")
    
    # Test import power
    print("\n--- Testing Import Power ---")
    print_import_power_summary(4, 2025)
    print_import_power_summary(6, 2025)
    
    # Test yearly import power
    print("\n--- Testing Yearly Import Power ---")
    print_yearly_import_power_summary(2025)


# ============================================================
# HRSG HEAT RATE LOOKUP TABLE
# ============================================================
# Used for reverse calculation of Natural Gas norms for HRSGs
# 
# Formula:
#   NG Norm (MMBTU/MT) = Heat Rate (BTU/lb) × 0.00396567
#   Natural Gas (MMBTU) = SHP Production (MT) × NG Norm (MMBTU/MT)
#
# Conversion factor: 1 kcal/kg = 3.96567 BTU/lb
# ============================================================

# Conversion constant: BTU/lb to MMBTU/MT
BTU_LB_TO_MMBTU_MT = 0.00396567


def fetch_hrsg_heat_rate_lookup() -> pd.DataFrame:
    """
    Fetch all HRSG heat rate lookup data from database.
    
    Returns:
        DataFrame with columns: EquipmentName, CPPUtility, HRSGLoad, HeatRate
        Sorted by EquipmentName and HRSGLoad ascending.
    """
    conn = get_connection()
    cur = conn.cursor()
    
    cur.execute("""
        SELECT 
            EquipmentName,
            CPPUtility,
            HRSGLoad,
            HeatRate
        FROM HRSGHeatRateLookup
        ORDER BY EquipmentName ASC, HRSGLoad ASC
    """)
    
    rows = cur.fetchall()
    cols = ["EquipmentName", "CPPUtility", "HRSGLoad", "HeatRate"]
    
    conn.close()
    
    if not rows:
        return pd.DataFrame(columns=cols)
    
    df = pd.DataFrame.from_records(rows, columns=cols)
    
    # Convert numeric columns to float
    df["HRSGLoad"] = df["HRSGLoad"].astype(float)
    df["HeatRate"] = df["HeatRate"].astype(float)
    
    return df


def get_hrsg_heat_rate_for_load(
    equipment_name: str, 
    hrsg_load_tph: float, 
    lookup_df: pd.DataFrame = None
) -> dict:
    """
    Get heat rate for a given HRSG at a specific load using interpolation.
    
    Args:
        equipment_name: HRSG name ('HRSG1', 'HRSG2', 'HRSG3')
        hrsg_load_tph: HRSG steam load in TPH (tonnes per hour)
        lookup_df: Optional pre-fetched lookup DataFrame (for efficiency)
    
    Returns:
        dict with:
            - equipment_name: HRSG name
            - heat_rate_btu_lb: Heat rate in BTU/lb
            - ng_norm_mmbtu_mt: Calculated NG norm in MMBTU/MT
            - hrsg_load_tph: Load used for lookup
            - interpolated: Whether interpolation was used
    """
    # Fetch lookup table if not provided
    if lookup_df is None or lookup_df.empty:
        lookup_df = fetch_hrsg_heat_rate_lookup()
    
    if lookup_df.empty:
        return {
            "equipment_name": equipment_name,
            "heat_rate_btu_lb": 0.0,
            "ng_norm_mmbtu_mt": 0.0,
            "hrsg_load_tph": hrsg_load_tph,
            "interpolated": False,
            "error": "No lookup data available"
        }
    
    # Normalize HRSG name: remove hyphens to match database format
    # e.g., "HRSG-1" -> "HRSG1", "HRSG-2" -> "HRSG2"
    normalized_name = equipment_name.replace("-", "")
    
    # Filter for specific HRSG
    hrsg_df = lookup_df[lookup_df["EquipmentName"] == normalized_name]
    
    if hrsg_df.empty:
        # Debug: print available equipment names
        available_names = lookup_df["EquipmentName"].unique().tolist() if not lookup_df.empty else []
        print(f"  [DEBUG] HRSG lookup failed for '{equipment_name}' (normalized: '{normalized_name}')")
        print(f"  [DEBUG] Available equipment names in lookup table: {available_names}")
        return {
            "equipment_name": equipment_name,
            "heat_rate_btu_lb": 0.0,
            "ng_norm_mmbtu_mt": 0.0,
            "hrsg_load_tph": hrsg_load_tph,
            "interpolated": False,
            "error": f"No data for {equipment_name} (normalized: {normalized_name})"
        }
    
    min_load = hrsg_df["HRSGLoad"].min()
    max_load = hrsg_df["HRSGLoad"].max()
    
    # Handle edge cases
    if hrsg_load_tph <= 0:
        return {
            "equipment_name": equipment_name,
            "heat_rate_btu_lb": 0.0,
            "ng_norm_mmbtu_mt": 0.0,
            "hrsg_load_tph": 0.0,
            "interpolated": False
        }
    
    # Clamp to min/max if outside range
    if hrsg_load_tph <= min_load:
        row = hrsg_df[hrsg_df["HRSGLoad"] == min_load].iloc[0]
        heat_rate = row["HeatRate"]
        return {
            "equipment_name": equipment_name,
            "heat_rate_btu_lb": heat_rate,
            "ng_norm_mmbtu_mt": heat_rate * BTU_LB_TO_MMBTU_MT,
            "hrsg_load_tph": min_load,
            "interpolated": False
        }
    
    if hrsg_load_tph >= max_load:
        row = hrsg_df[hrsg_df["HRSGLoad"] == max_load].iloc[0]
        heat_rate = row["HeatRate"]
        return {
            "equipment_name": equipment_name,
            "heat_rate_btu_lb": heat_rate,
            "ng_norm_mmbtu_mt": heat_rate * BTU_LB_TO_MMBTU_MT,
            "hrsg_load_tph": max_load,
            "interpolated": False
        }
    
    # Find bracketing values for interpolation
    lower_df = hrsg_df[hrsg_df["HRSGLoad"] <= hrsg_load_tph]
    upper_df = hrsg_df[hrsg_df["HRSGLoad"] >= hrsg_load_tph]
    
    if lower_df.empty or upper_df.empty:
        # Fallback to closest value
        closest_idx = (hrsg_df["HRSGLoad"] - hrsg_load_tph).abs().idxmin()
        row = hrsg_df.loc[closest_idx]
        heat_rate = row["HeatRate"]
        return {
            "equipment_name": equipment_name,
            "heat_rate_btu_lb": heat_rate,
            "ng_norm_mmbtu_mt": heat_rate * BTU_LB_TO_MMBTU_MT,
            "hrsg_load_tph": row["HRSGLoad"],
            "interpolated": False
        }
    
    lower_row = lower_df.iloc[-1]  # Highest value <= target
    upper_row = upper_df.iloc[0]   # Lowest value >= target
    
    # Check for exact match
    if lower_row["HRSGLoad"] == upper_row["HRSGLoad"]:
        heat_rate = lower_row["HeatRate"]
        return {
            "equipment_name": equipment_name,
            "heat_rate_btu_lb": heat_rate,
            "ng_norm_mmbtu_mt": heat_rate * BTU_LB_TO_MMBTU_MT,
            "hrsg_load_tph": hrsg_load_tph,
            "interpolated": False
        }
    
    # Linear interpolation
    load_range = upper_row["HRSGLoad"] - lower_row["HRSGLoad"]
    load_fraction = (hrsg_load_tph - lower_row["HRSGLoad"]) / load_range
    
    heat_rate = lower_row["HeatRate"] + load_fraction * (upper_row["HeatRate"] - lower_row["HeatRate"])
    
    return {
        "equipment_name": equipment_name,
        "heat_rate_btu_lb": round(heat_rate, 4),
        "ng_norm_mmbtu_mt": round(heat_rate * BTU_LB_TO_MMBTU_MT, 7),
        "hrsg_load_tph": hrsg_load_tph,
        "interpolated": True,
        "lower_load": lower_row["HRSGLoad"],
        "upper_load": upper_row["HRSGLoad"]
    }


def calculate_hrsg_ng_from_heat_rate(
    hrsg_name: str,
    shp_production_mt: float,
    operational_hours: float,
    lookup_df: pd.DataFrame = None
) -> dict:
    """
    Calculate HRSG Natural Gas consumption using heat rate from lookup table.
    
    This is the reverse calculation:
    1. Get heat rate (BTU/lb) from lookup based on steam flow (TPH)
    2. Convert to NG norm (MMBTU/MT): Heat Rate × 0.00396567
    3. Calculate NG quantity: SHP Production × NG Norm
    
    Args:
        hrsg_name: HRSG name ('HRSG1', 'HRSG2', 'HRSG3')
        shp_production_mt: Total SHP production in MT for the month
        operational_hours: HRSG operational hours for the month
        lookup_df: Optional pre-fetched lookup DataFrame
    
    Returns:
        dict with:
            - hrsg_name: HRSG name
            - shp_production_mt: SHP production used
            - steam_flow_tph: Calculated steam flow (MT/hours)
            - heat_rate_btu_lb: Heat rate from lookup
            - ng_norm_mmbtu_mt: Calculated NG norm
            - ng_quantity_mmbtu: Total NG consumption
    """
    # Calculate average steam flow in TPH
    steam_flow_tph = shp_production_mt / operational_hours if operational_hours > 0 else 0.0
    
    # Get heat rate for this load
    heat_rate_result = get_hrsg_heat_rate_for_load(hrsg_name, steam_flow_tph, lookup_df)
    
    heat_rate = heat_rate_result.get("heat_rate_btu_lb", 0.0)
    ng_norm = heat_rate_result.get("ng_norm_mmbtu_mt", 0.0)
    
    # Calculate NG quantity
    ng_quantity = shp_production_mt * ng_norm
    
    return {
        "hrsg_name": hrsg_name,
        "shp_production_mt": round(shp_production_mt, 2),
        "operational_hours": round(operational_hours, 2),
        "steam_flow_tph": round(steam_flow_tph, 4),
        "heat_rate_btu_lb": heat_rate,
        "ng_norm_mmbtu_mt": ng_norm,
        "ng_quantity_mmbtu": round(ng_quantity, 2),
        "interpolated": heat_rate_result.get("interpolated", False)
    }


def print_hrsg_heat_rate_lookup_summary():
    """Print a summary of HRSG heat rate lookup table."""
    df = fetch_hrsg_heat_rate_lookup()
    
    print("\n" + "="*80)
    print("HRSG HEAT RATE LOOKUP TABLE")
    print("="*80)
    
    if df.empty:
        print("  No data available in HRSGHeatRateLookup table.")
        print("="*80)
        return
    
    print(f"{'Equipment':<12} {'CPPUtility':<15} {'HRSGLoad':<15} {'HeatRate':<12} {'NG Norm':<15}")
    print(f"{'Name':<12} {'(AssetId)':<15} {'(TPH)':<15} {'(BTU/lb)':<12} {'(MMBTU/MT)':<15}")
    print("-"*80)
    
    for _, row in df.iterrows():
        ng_norm = row["HeatRate"] * BTU_LB_TO_MMBTU_MT
        print(f"{row['EquipmentName']:<12} {row['CPPUtility']:<15} {row['HRSGLoad']:>12.2f}   {row['HeatRate']:>10.2f}   {ng_norm:>12.7f}")
    
    print("="*80)
    print(f"  Conversion: NG Norm (MMBTU/MT) = Heat Rate (BTU/lb) × {BTU_LB_TO_MMBTU_MT}")
    print("="*80 + "\n")
