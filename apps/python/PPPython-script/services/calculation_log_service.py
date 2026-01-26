"""
Calculation Log Service
Saves model execution logs to ModelCalculationLogs table
"""

import json
import uuid
from datetime import datetime
from database.connection import get_connection


def create_parent_execution_log(year: int) -> dict:
    """
    Create parent execution log for full year run.
    
    Args:
        year: Financial year (e.g., 2025)
    
    Returns:
        dict with parent_id and status
    """
    try:
        conn = get_connection()
        cursor = conn.cursor()
        
        parent_id = str(uuid.uuid4())
        
        insert_query = """
        INSERT INTO CPPModelCalculationLogs (
            Id,
            ParentExecution_FK_Id,
            FinancialYearMonth_FK_Id,
            FinancialYear,
            Month,
            ExecutionDateTime,
            Status,
            ErrorMessage,
            ErrorType,
            IterationCount,
            ConvergenceAchieved,
            ExecutionTimeSeconds,
            AssetStatusJSON,
            PowerBalanceJSON,
            SteamBalanceJSON,
            CreatedBy,
            CreatedDate
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        
        cursor.execute(insert_query, (
            parent_id,
            None,  # ParentExecution_FK_Id = NULL (this is parent)
            None,  # FinancialYearMonth_FK_Id = NULL (no specific month)
            year,
            None,  # Month = NULL (represents all months)
            datetime.now(),
            'InProgress',  # Will be updated after all months complete
            None,
            None,
            0,  # Will be updated with total iterations
            None,  # Will be updated with overall convergence
            0.0,  # Will be updated with total execution time
            None,  # Parent has no asset status
            None,  # Parent has no power balance
            None,  # Parent has no steam balance
            'PythonModel',
            datetime.now()
        ))
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return {
            "success": True,
            "parent_id": parent_id,
            "message": f"Parent execution log created for FY{year}"
        }
    
    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "message": f"Failed to create parent execution log: {str(e)}"
        }


def update_parent_execution_summary(
    parent_id: str,
    total_execution_time: float,
    success_count: int,
    failed_count: int,
    warning_count: int,
    total_iterations: int
) -> dict:
    """
    Update parent execution log with summary after all months complete.
    
    Args:
        parent_id: Parent execution log ID
        total_execution_time: Total time for all months (seconds)
        success_count: Number of successful months
        failed_count: Number of failed months
        warning_count: Number of months with warnings
        total_iterations: Total iterations across all months
    
    Returns:
        dict with update status
    """
    try:
        conn = get_connection()
        cursor = conn.cursor()
        
        # Determine overall status
        if failed_count > 0:
            status = 'Failed'
            error_message = f'{failed_count} month(s) failed'
        elif warning_count > 0:
            status = 'Warning'
            error_message = f'{warning_count} month(s) had warnings'
        else:
            status = 'Success'
            error_message = None
        
        update_query = """
        UPDATE CPPModelCalculationLogs
        SET Status = ?,
            ErrorMessage = ?,
            ExecutionTimeSeconds = ?,
            IterationCount = ?,
            ConvergenceAchieved = ?
        WHERE Id = ?
        """
        
        cursor.execute(update_query, (
            status,
            error_message,
            total_execution_time,
            total_iterations,
            1 if success_count == 12 else 0,  # All months converged
            parent_id
        ))
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return {
            "success": True,
            "status": status,
            "message": f"Parent execution updated: {success_count} success, {failed_count} failed, {warning_count} warnings"
        }
    
    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "message": f"Failed to update parent execution: {str(e)}"
        }


def save_calculation_log(
    month: int,
    year: int,
    financial_year_month_id: str,
    calculation_result: dict,
    execution_time_seconds: float = None,
    parent_execution_id: str = None
) -> dict:
    """
    Save calculation log to ModelCalculationLogs table.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025)
        financial_year_month_id: FK to FinancialYearMonth table (GUID)
        calculation_result: Result dict from calculate_budget_with_iteration
        execution_time_seconds: Time taken to execute (optional)
        parent_execution_id: FK to parent execution log (for full year runs)
    
    Returns:
        dict with save status
    """
    try:
        # Determine financial year (April-March)
        if month >= 4:
            financial_year = year
        else:
            financial_year = year - 1
        
        # Extract status and error info
        overall_success = calculation_result.get("overall_success", False)
        usd_result = calculation_result.get("usd_result", {})
        
        # Determine status
        if overall_success:
            status = "Success"
            error_type = None
            error_message = None
        else:
            # Check for errors
            errors = calculation_result.get("errors", [])
            if errors and errors[0].get("error_type"):
                status = "Failed"
                error_type = errors[0].get("error_type", "UnknownError")
                error_message = errors[0].get("message", "Unknown error occurred")
            else:
                status = "Warning"
                error_type = "ConvergenceNotAchieved"
                error_message = "Model ran but did not fully converge within iteration limit"
        
        # Extract iteration info
        iterations_used = calculation_result.get("iterations_used", 0) or usd_result.get("iterations_used", 0)
        
        # Build Asset Status JSON
        asset_status_json = _build_asset_status_json(calculation_result)
        
        # Build Power Balance JSON
        power_balance_json = _build_power_balance_json(calculation_result)
        
        # Build Steam Balance JSON
        steam_balance_json = _build_steam_balance_json(calculation_result)
        
        # Save to database
        conn = get_connection()
        cursor = conn.cursor()
        
        # Generate new ID
        log_id = str(uuid.uuid4())
        
        insert_query = """
        INSERT INTO CPPModelCalculationLogs (
            Id,
            ParentExecution_FK_Id,
            FinancialYearMonth_FK_Id,
            FinancialYear,
            Month,
            ExecutionDateTime,
            Status,
            ErrorMessage,
            ErrorType,
            IterationCount,
            ConvergenceAchieved,
            ExecutionTimeSeconds,
            AssetStatusJSON,
            PowerBalanceJSON,
            SteamBalanceJSON,
            CreatedBy,
            CreatedDate
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        
        cursor.execute(insert_query, (
            log_id,
            parent_execution_id,  # Link to parent execution
            financial_year_month_id,
            financial_year,
            month,
            datetime.now(),
            status,
            error_message,
            error_type,
            iterations_used,
            1 if overall_success else 0,  # ConvergenceAchieved: 1 if converged, 0 otherwise
            execution_time_seconds,
            json.dumps(asset_status_json) if asset_status_json else None,
            json.dumps(power_balance_json) if power_balance_json else None,
            json.dumps(steam_balance_json) if steam_balance_json else None,
            'PythonModel',
            datetime.now()
        ))
        
        conn.commit()
        cursor.close()
        conn.close()
        
        return {
            "success": True,
            "log_id": log_id,
            "status": status,
            "message": f"Calculation log saved successfully for {month}/{year}"
        }
    
    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "message": f"Failed to save calculation log: {str(e)}"
        }


def _build_asset_status_json(calculation_result: dict) -> list:
    """Build asset status JSON array from calculation result."""
    asset_list = []
    
    usd_result = calculation_result.get("usd_result", {})
    final_dispatch = usd_result.get("final_dispatch", [])
    
    # Track which assets we've seen
    assets_seen = set()
    
    # Process dispatched assets (GTs, STG, Import)
    for asset in final_dispatch:
        asset_name = asset.get("AssetName", "")
        asset_type = _determine_asset_type(asset_name)
        
        asset_data = {
            "asset": asset_name,
            "type": asset_type,
            "isAvailable": asset.get("GrossMWh", 0) > 0 or asset.get("LoadMW", 0) > 0,
            "operatingHours": asset.get("OperatingHours", 0),
            "minCapacityMW": asset.get("MinCapacityMW"),
            "maxCapacityMW": asset.get("MaxCapacityMW"),
            "dispatchedLoadMW": asset.get("LoadMW"),
            "grossMWh": asset.get("GrossMWh"),
            "netMWh": asset.get("NetMWh"),
            "auxiliaryMWh": asset.get("AuxiliaryMWh"),
            "status": _determine_asset_status(asset)
        }
        
        asset_list.append(asset_data)
        assets_seen.add(asset_name.upper())
    
    # Add HRSG status
    hrsg_dispatch = usd_result.get("hrsg_dispatch", {})
    hrsg_dispatch_list = hrsg_dispatch.get("hrsg_dispatch", [])
    
    if hrsg_dispatch_list:
        for hrsg in hrsg_dispatch_list:
            hrsg_name = hrsg.get("name", "")
            is_available = hrsg.get("is_available", False)
            operating_hours = hrsg.get("operating_hours", 0)
            dispatched_supp_mt = hrsg.get("dispatched_supp_mt", 0)  # SHP steam generation in MT
            
            asset_data = {
                "asset": hrsg_name,
                "type": "HRSG",
                "isAvailable": is_available,
                "operatingHours": operating_hours,
                "steamGenerationMT": dispatched_supp_mt,  # SHP steam generation
                "status": "Running" if is_available and operating_hours > 0 else "Off"
            }
            
            asset_list.append(asset_data)
            assets_seen.add(hrsg_name.upper())
    else:
        # Fallback: Add HRSG info from shp_capacity
        shp_capacity = usd_result.get("final_shp_capacity", {})
        hrsg_details = shp_capacity.get("hrsg_details", [])
        
        for hrsg in hrsg_details:
            hrsg_name = hrsg.get("name", "")
            is_available = hrsg.get("is_available", False)
            
            asset_data = {
                "asset": hrsg_name,
                "type": "HRSG",
                "isAvailable": is_available,
                "operatingHours": 0,
                "status": "Running" if is_available else "Off"
            }
            
            asset_list.append(asset_data)
            assets_seen.add(hrsg_name.upper())
    
    return asset_list


def _determine_asset_type(asset_name: str) -> str:
    """Determine asset type from name."""
    name_upper = asset_name.upper()
    
    if 'GT' in name_upper or 'PLANT-' in name_upper:
        return "GT"
    elif 'STG' in name_upper or 'STEAM TURBINE' in name_upper:
        return "STG"
    elif 'MEL' in name_upper or 'IMPORT' in name_upper:
        return "ImportPower"
    elif 'HRSG' in name_upper:
        return "HRSG"
    else:
        return "Other"


def _determine_asset_status(asset: dict) -> str:
    """Determine asset operating status."""
    gross_mwh = asset.get("GrossMWh", 0)
    load_mw = asset.get("LoadMW", 0)
    max_capacity = asset.get("MaxCapacityMW", 0)
    min_capacity = asset.get("MinCapacityMW", 0)
    
    if gross_mwh == 0 and load_mw == 0:
        return "Shutdown"
    
    if max_capacity and load_mw:
        # Check if at max capacity (within 1% tolerance)
        if abs(load_mw - max_capacity) / max_capacity < 0.01:
            return "AtMax"
        
        # Check if at min capacity
        if min_capacity and abs(load_mw - min_capacity) / min_capacity < 0.01:
            return "AtMin"
    
    return "Running"


def _build_power_balance_json(calculation_result: dict) -> dict:
    """Build power balance JSON from calculation result."""
    usd_result = calculation_result.get("usd_result", {})
    power_result = usd_result.get("power_result", {})
    final_dispatch = usd_result.get("final_dispatch", [])
    
    if not power_result:
        return None
    
    # Extract demand components (keys without "Units" suffix)
    demand_data = {
        "processDemand": power_result.get("processDemand", 0),
        "fixedDemand": power_result.get("fixedDemand", 0),
        "u4uDemand": power_result.get("u4uPower", 0),  # Key is u4uPower, not u4uDemand
        "utilityAuxPower": power_result.get("totalAuxConsumption", 0),  # Auxiliary consumption
        "total": power_result.get("totalDemandUnits", 0)
    }
    
    # Extract supply components from dispatch
    supply_data = {
        "importPower": 0,
        "gt1Net": 0,
        "gt2Net": 0,
        "gt3Net": 0,
        "stgNet": 0,
        "totalNetGeneration": 0,
        "total": 0
    }
    
    for asset in final_dispatch:
        asset_name = asset.get("AssetName", "").upper()
        net_mwh = asset.get("NetMWh", 0)
        
        if 'GT1' in asset_name or 'PLANT-1' in asset_name:
            supply_data["gt1Net"] = net_mwh
        elif 'GT2' in asset_name or 'PLANT-2' in asset_name:
            supply_data["gt2Net"] = net_mwh
        elif 'GT3' in asset_name or 'PLANT-3' in asset_name:
            supply_data["gt3Net"] = net_mwh
        elif 'STG' in asset_name:
            supply_data["stgNet"] = net_mwh
        elif 'MEL' in asset_name or 'IMPORT' in asset_name:
            supply_data["importPower"] = net_mwh
    
    # Import power comes from power_result, not dispatch (MEL is not in final_dispatch)
    supply_data["importPower"] = power_result.get("mandatoryImportUsed", 0)
    supply_data["totalNetGeneration"] = power_result.get("totalNetGeneration", 0)
    supply_data["total"] = supply_data["totalNetGeneration"] + supply_data["importPower"]
    
    # Calculate balance
    balance_data = {
        "difference": supply_data["total"] - demand_data["total"],
        "isBalanced": abs(supply_data["total"] - demand_data["total"]) < 0.01,
        "excessPower": max(0, supply_data["total"] - demand_data["total"]),
        "shortfall": max(0, demand_data["total"] - supply_data["total"])
    }
    
    return {
        "demand": demand_data,
        "supply": supply_data,
        "balance": balance_data
    }


def _build_steam_balance_json(calculation_result: dict) -> dict:
    """Build steam balance JSON from calculation result."""
    usd_result = calculation_result.get("usd_result", {})
    steam_balance = usd_result.get("final_steam_balance", {})
    
    if not steam_balance:
        return None
    
    steam_data = {}
    
    # Process each steam type (SHP, HP, MP, LP)
    for steam_type in ["shp", "hp", "mp", "lp"]:
        balance_key = f"{steam_type}_balance"
        type_balance = steam_balance.get(balance_key, {})
        
        if not type_balance:
            continue
        
        # Extract demand components (keys are like "shp_process", "shp_total_demand")
        demand_data = {
            "processDemand": type_balance.get(f"{steam_type}_process", 0),
            "fixedDemand": type_balance.get(f"{steam_type}_fixed", 0),
            "stgConsumption": type_balance.get(f"{steam_type}_to_stg", 0),
            "prdsConsumption": 0,
            "total": type_balance.get(f"{steam_type}_total_demand", 0) or type_balance.get(f"{steam_type}_total", 0)
        }
        
        # Add PRDS consumption if applicable
        if steam_type == "shp":
            demand_data["prdsToHP"] = type_balance.get("shp_to_hp_prds", 0)
            demand_data["prdsToMP"] = type_balance.get("shp_to_mp_prds", 0)
            demand_data["prdsToLP"] = type_balance.get("shp_to_lp_prds", 0)
            demand_data["prdsConsumption"] = (
                demand_data["prdsToHP"] + 
                demand_data["prdsToMP"] + 
                demand_data["prdsToLP"]
            )
        
        # Extract supply components (supply varies by steam type)
        supply_data = {"freeSteam": type_balance.get("free_steam", 0)}
        
        # Add HRSG supplementary for SHP
        if steam_type == "shp":
            supply_data["hrsg1Supplementary"] = type_balance.get("shp_from_hrsg1", 0)
            supply_data["hrsg2Supplementary"] = type_balance.get("shp_from_hrsg2", 0)
            supply_data["hrsg3Supplementary"] = type_balance.get("shp_from_hrsg3", 0)
            # Total SHP supply = demand (balanced by HRSGs)
            supply_data["total"] = demand_data["total"]
        
        # HP supply comes from PRDS only
        elif steam_type == "hp":
            supply_data["fromPRDS"] = type_balance.get("hp_total", 0)  # HP total is the supply from PRDS
            supply_data["total"] = demand_data["total"]
        
        # MP supply comes from STG extraction and PRDS
        elif steam_type == "mp":
            supply_data["stgExtraction"] = type_balance.get("mp_from_stg", 0)
            supply_data["fromPRDS"] = type_balance.get("mp_from_prds", 0)
            supply_data["total"] = supply_data["stgExtraction"] + supply_data["fromPRDS"]
        
        # LP supply comes from STG extraction and PRDS
        elif steam_type == "lp":
            supply_data["stgExtraction"] = type_balance.get("lp_from_stg", 0)
            supply_data["fromPRDS"] = type_balance.get("lp_from_prds", 0)
            supply_data["total"] = supply_data["stgExtraction"] + supply_data["fromPRDS"]
        
        # Calculate balance
        balance_data = {
            "difference": supply_data["total"] - demand_data["total"],
            "isBalanced": abs(supply_data["total"] - demand_data["total"]) < 0.01,
            "excess": max(0, supply_data["total"] - demand_data["total"]),
            "shortfall": max(0, demand_data["total"] - supply_data["total"])
        }
        
        steam_data[steam_type.upper()] = {
            "demand": demand_data,
            "supply": supply_data,
            "balance": balance_data
        }
    
    return steam_data


def get_financial_year_month_id(month: int, year: int) -> str:
    """
    Get FinancialYearMonth ID from database.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025)
    
    Returns:
        GUID string or None if not found
    """
    try:
        conn = get_connection()
        cursor = conn.cursor()
        
        query = """
        SELECT Id 
        FROM FinancialYearMonth
        WHERE Month = ? AND Year = ?
        """
        
        cursor.execute(query, (month, year))
        row = cursor.fetchone()
        
        cursor.close()
        conn.close()
        
        if row:
            return str(row[0])
        return None
    
    except Exception as e:
        print(f"Error fetching FinancialYearMonth ID: {str(e)}")
        return None
