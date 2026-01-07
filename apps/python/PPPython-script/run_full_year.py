"""
Run Budget Model for Full Financial Year
Executes the model for all 12 months (April to March)
Fetches fixed consumption demands dynamically from DB for each month.
Process demands remain constant (same for all months).

Usage:
    python run_full_year.py --fy 2025 --cpp <CPP_PLANT_ID>
    
    --fy: Financial Year (e.g., 2025 means FY 2025-26: April 2025 to March 2026)
    --cpp: CPP Plant ID (GUID) - optional, fetches from DB if not provided
    --json: Return structured JSON response (suppresses console output)
    --auto: Run in auto mode (for Java integration)
"""

import sys
import os
import argparse
import json
from datetime import datetime
from io import StringIO

from services.budget_service import calculate_budget_with_iteration
from services.fixed_consumption_service import get_fixed_consumption_for_month, print_fixed_consumption
from database.connection import get_connection

# ============================================================
# CONFIGURATION
# ============================================================
# Use environment variable for log folder, with fallback for local development
LOG_FOLDER = os.environ.get("LOG_FOLDER", r"C:\Users\shrik\Desktop\Project\fork repo\development\PP python-script repo\PPPython-script\logs\full_year_run")


def get_fy_months(financial_year: int) -> list:
    """
    Get list of (month, year) tuples for a financial year.
    Financial Year runs from April to March.
    
    Args:
        financial_year: Starting year of FY (e.g., 2025 for FY 2025-26)
    
    Returns:
        List of (month, year) tuples
    """
    return [
        (4, financial_year),      # April
        (5, financial_year),      # May
        (6, financial_year),      # June
        (7, financial_year),      # July
        (8, financial_year),      # August
        (9, financial_year),      # September
        (10, financial_year),     # October
        (11, financial_year),     # November
        (12, financial_year),     # December
        (1, financial_year + 1),  # January (next year)
        (2, financial_year + 1),  # February (next year)
        (3, financial_year + 1),  # March (next year)
    ]


def get_available_cpp_plants() -> list:
    """Fetch all available CPP Plants from database."""
    conn = get_connection()
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT DISTINCT CPPPLANT_FK_Id
        FROM PowerGenerationAssets
        WHERE CPPPLANT_FK_Id IS NOT NULL
    """)
    
    plants = [str(row[0]) for row in cursor.fetchall()]
    conn.close()
    return plants


def get_default_cpp_plant() -> str:
    """Get the default (first) CPP Plant ID."""
    plants = get_available_cpp_plants()
    if plants:
        return plants[0]
    return None

# Process Demands (same for all months - these are constant)
# Fixed demands are fetched dynamically from database per month
DEFAULT_PROCESS_DEMANDS = {
    "lp_process": 30043.15,
    "mp_process": 14030.65,
    "hp_process": 4971.91,
    "shp_process": 20975.34,
    "bfw_ufu": 0.00,
    "air_process": 6095102.0,
    "cw1_process": 15194.0,
    "cw2_process": 9016.0,
    "dm_process": 54779.0,
    "export_available": False,
}

# Fallback fixed demands (used if DB fetch fails)
DEFAULT_FIXED_DEMANDS = {
    "lp_fixed": 5169.51,
    "mp_fixed": 518.00,
    "hp_fixed": 0.00,
    "shp_fixed": 0.00,
}


def get_demands_for_month(month: int, year: int, process_demands: dict = None) -> dict:
    """
    Get combined demands (process + fixed) for a specific month.
    Fixed demands are fetched from database, process demands are constant.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025)
        process_demands: Process demands dict (uses DEFAULT_PROCESS_DEMANDS if None)
    
    Returns:
        Combined demands dict for the month
    """
    if process_demands is None:
        process_demands = DEFAULT_PROCESS_DEMANDS.copy()
    
    # Fetch fixed consumption from database
    fixed_data = get_fixed_consumption_for_month(month, year)
    
    # Combine process and fixed demands
    demands = process_demands.copy()
    demands["lp_fixed"] = fixed_data.get("lp_fixed", DEFAULT_FIXED_DEMANDS["lp_fixed"])
    demands["mp_fixed"] = fixed_data.get("mp_fixed", DEFAULT_FIXED_DEMANDS["mp_fixed"])
    demands["hp_fixed"] = fixed_data.get("hp_fixed", DEFAULT_FIXED_DEMANDS["hp_fixed"])
    demands["shp_fixed"] = fixed_data.get("shp_fixed", DEFAULT_FIXED_DEMANDS["shp_fixed"])
    
    return demands

MONTH_NAMES = {
    1: "January", 2: "February", 3: "March", 4: "April",
    5: "May", 6: "June", 7: "July", 8: "August",
    9: "September", 10: "October", 11: "November", 12: "December"
}


class LogCapture:
    """Captures output for logging while still printing to console."""
    def __init__(self):
        self.buffer = StringIO()
        self.original_stdout = sys.stdout
    
    def write(self, text):
        self.original_stdout.write(text)
        self.buffer.write(text)
    
    def flush(self):
        self.original_stdout.flush()
    
    def get_output(self):
        return self.buffer.getvalue()
    
    def clear(self):
        self.buffer = StringIO()


def save_month_log(output_text, month, year, log_folder):
    """Save log for a single month."""
    os.makedirs(log_folder, exist_ok=True)
    filename = f"month_{year}_{month:02d}_{MONTH_NAMES[month]}.txt"
    filepath = os.path.join(log_folder, filename)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(output_text)
    
    return filepath


def run_single_month(month, year, demands, save_to_db=True):
    """
    Run the model for a single month.
    
    Note: If USD iteration doesn't converge, the model still returns partial results
    which can be saved to the database. The 'converged' flag indicates whether
    the iteration fully converged.
    """
    try:
        result = calculate_budget_with_iteration(
            month=month,
            year=year,
            lp_process=demands["lp_process"],
            lp_fixed=demands["lp_fixed"],
            mp_process=demands["mp_process"],
            mp_fixed=demands["mp_fixed"],
            hp_process=demands["hp_process"],
            hp_fixed=demands["hp_fixed"],
            shp_process=demands["shp_process"],
            shp_fixed=demands["shp_fixed"],
            bfw_ufu=demands["bfw_ufu"],
            export_available=demands["export_available"],
            air_process=demands["air_process"],
            cw1_process=demands["cw1_process"],
            cw2_process=demands["cw2_process"],
            dm_process=demands["dm_process"],
            save_to_db=save_to_db
        )
        return result
    except Exception as e:
        return {
            "overall_success": False,
            "error_type": "EXCEPTION",
            "message": str(e),
            "converged": False,
            "iterations": 0,
        }


def run_full_financial_year(financial_year: int, cpp_plant_id: str = None, process_demands=None, save_to_db=True, save_logs=True):
    """
    Run the budget model for all 12 months of a financial year.
    Fixed demands are fetched dynamically from DB for each month.
    
    Args:
        financial_year: Starting year of FY (e.g., 2025 for FY 2025-26)
        cpp_plant_id: CPP Plant ID (GUID) - uses default if None
        process_demands: Dict of process demands (uses DEFAULT_PROCESS_DEMANDS if None)
        save_to_db: Whether to save results to database
        save_logs: Whether to save individual month logs
    
    Returns:
        Dict with summary of all month results
    """
    if process_demands is None:
        process_demands = DEFAULT_PROCESS_DEMANDS.copy()
    
    # Get CPP Plant ID if not provided
    if cpp_plant_id is None:
        cpp_plant_id = get_default_cpp_plant()
        if cpp_plant_id is None:
            raise ValueError("No CPP Plant found in database")
    
    # Get months for this financial year
    fy_months = get_fy_months(financial_year)
    fy_label = f"FY {financial_year}-{str(financial_year + 1)[-2:]}"
    
    # Create log folder with timestamp
    run_timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    run_log_folder = os.path.join(LOG_FOLDER, f"run_{run_timestamp}")
    os.makedirs(run_log_folder, exist_ok=True)
    
    print("=" * 80)
    print(f"FULL FINANCIAL YEAR MODEL RUN - {fy_label}")
    print("=" * 80)
    print(f"CPP Plant ID: {cpp_plant_id}")
    print(f"Run started: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"Save to DB: {save_to_db}")
    print(f"Log folder: {run_log_folder}")
    print()
    
    # Print process demands (constant for all months)
    print("PROCESS DEMANDS (constant for all months):")
    print("-" * 40)
    print(f"  LP Steam:  {process_demands['lp_process']:.2f} MT")
    print(f"  MP Steam:  {process_demands['mp_process']:.2f} MT")
    print(f"  HP Steam:  {process_demands['hp_process']:.2f} MT")
    print(f"  SHP Steam: {process_demands['shp_process']:.2f} MT")
    print(f"  BFW UFU:   {process_demands['bfw_ufu']:.2f} M3")
    print(f"  Compressed Air: {process_demands['air_process']:,.0f} NM3")
    print(f"  Cooling Water 1: {process_demands['cw1_process']:,.0f} KM3")
    print(f"  Cooling Water 2: {process_demands['cw2_process']:,.0f} KM3")
    print(f"  DM Water: {process_demands['dm_process']:,.0f} M3")
    print()
    print("FIXED DEMANDS: Will be fetched dynamically from database for each month")
    print()
    
    # Results storage
    results = {
        "run_timestamp": run_timestamp,
        "process_demands": process_demands,
        "months": {},
        "summary": {
            "total_months": 12,
            "successful": 0,
            "failed": 0,
            "total_power_kwh": 0,
            "total_shp_mt": 0,
        }
    }
    
    # Run for each month
    for month, year in fy_months:
        month_name = MONTH_NAMES[month]
        print("=" * 80)
        print(f"PROCESSING: {month_name} {year} (Month {month}/{year})")
        print("=" * 80)
        
        # Capture output for this month
        log_capture = LogCapture()
        sys.stdout = log_capture
        
        try:
            # Get demands for this month (process + dynamic fixed from DB)
            month_demands = get_demands_for_month(month, year, process_demands)
            
            # Print fixed demands for this month
            print(f"\nFixed Demands for {month_name} {year}:")
            print(f"  LP Fixed: {month_demands['lp_fixed']:.2f} MT")
            print(f"  MP Fixed: {month_demands['mp_fixed']:.2f} MT")
            print(f"  HP Fixed: {month_demands['hp_fixed']:.2f} MT")
            print(f"  SHP Fixed: {month_demands['shp_fixed']:.2f} MT")
            
            # Run the model
            result = run_single_month(month, year, month_demands, save_to_db)
            
            # Restore stdout
            sys.stdout = log_capture.original_stdout
            
            # Extract key metrics
            success = result.get("overall_success", False)
            
            month_result = {
                "month": month,
                "year": year,
                "month_name": month_name,
                "success": success,
                "iterations": result.get("iterations", 0),
                "converged": result.get("converged", False),
            }
            
            # Extract power generation (even if not converged)
            usd_result = result.get("usd_result", {})
            final_dispatch = usd_result.get("final_dispatch", [])
            
            total_power = sum(asset.get("GrossMWh", 0) * 1000 for asset in final_dispatch)
            month_result["total_power_kwh"] = total_power
            results["summary"]["total_power_kwh"] += total_power
            
            # Extract SHP steam
            final_steam = usd_result.get("final_steam_balance", {})
            shp_balance = final_steam.get("shp_balance", {})
            total_shp = shp_balance.get("total_shp_supply", 0)
            month_result["total_shp_mt"] = total_shp
            results["summary"]["total_shp_mt"] += total_shp
            
            # Check if data was saved to DB
            save_result = result.get("save_result", {})
            records_saved = save_result.get("updated", 0) + save_result.get("unchanged", 0)
            month_result["records_saved"] = records_saved
            
            if success:
                results["summary"]["successful"] += 1
                print(f"\n✓ {month_name} {year}: SUCCESS (Converged)")
                print(f"  Iterations: {month_result['iterations']}")
                print(f"  Total Power: {total_power:,.0f} KWH")
                print(f"  Total SHP: {total_shp:,.2f} MT")
                print(f"  Records Saved: {records_saved}")
            else:
                results["summary"]["failed"] += 1
                month_result["errors"] = result.get("errors", [])
                converged = result.get("converged", False)
                
                # Still show results even if not converged
                print(f"\n⚠ {month_name} {year}: {'PARTIAL' if final_dispatch else 'FAILED'}")
                print(f"  Converged: {converged}")
                print(f"  Total Power: {total_power:,.0f} KWH")
                print(f"  Records Saved: {records_saved}")
                for error in month_result.get("errors", []):
                    print(f"  Note: {error.get('message', 'Unknown')}")
            
            results["months"][f"{year}_{month:02d}"] = month_result
            
            # Save month log
            if save_logs:
                log_path = save_month_log(log_capture.get_output(), month, year, run_log_folder)
                month_result["log_file"] = log_path
        
        except Exception as e:
            sys.stdout = log_capture.original_stdout
            results["summary"]["failed"] += 1
            results["months"][f"{year}_{month:02d}"] = {
                "month": month,
                "year": year,
                "month_name": month_name,
                "success": False,
                "error": str(e)
            }
            print(f"\n✗ {month_name} {year}: EXCEPTION - {str(e)}")
        
        print()
    
    # Print final summary
    print("\n" + "=" * 80)
    print("FULL YEAR RUN COMPLETE - SUMMARY")
    print("=" * 80)
    print(f"Run completed: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"Total months processed: {results['summary']['total_months']}")
    print(f"Successful: {results['summary']['successful']}")
    print(f"Failed: {results['summary']['failed']}")
    print(f"Total Power Generated: {results['summary']['total_power_kwh']:,.0f} KWH")
    print(f"Total SHP Steam: {results['summary']['total_shp_mt']:,.2f} MT")
    print(f"Log folder: {run_log_folder}")
    
    # Save summary
    summary_path = os.path.join(run_log_folder, "summary.txt")
    with open(summary_path, 'w', encoding='utf-8') as f:
        f.write(f"FULL FINANCIAL YEAR RUN SUMMARY - {fy_label}\n")
        f.write("=" * 60 + "\n\n")
        f.write(f"CPP Plant ID: {cpp_plant_id}\n")
        f.write(f"Run timestamp: {run_timestamp}\n")
        f.write(f"Successful months: {results['summary']['successful']}/12\n")
        f.write(f"Failed months: {results['summary']['failed']}/12\n")
        f.write(f"Total Power: {results['summary']['total_power_kwh']:,.0f} KWH\n")
        f.write(f"Total SHP: {results['summary']['total_shp_mt']:,.2f} MT\n\n")
        
        f.write("MONTH-BY-MONTH RESULTS:\n")
        f.write("-" * 60 + "\n")
        for key, month_data in results["months"].items():
            status = "✓" if month_data.get("success") else "✗"
            f.write(f"{status} {month_data['month_name']} {month_data['year']}: ")
            if month_data.get("success"):
                f.write(f"Power={month_data.get('total_power_kwh', 0):,.0f} KWH, ")
                f.write(f"SHP={month_data.get('total_shp_mt', 0):,.2f} MT\n")
            else:
                f.write(f"FAILED - {month_data.get('error', 'Unknown')}\n")
    
    print(f"\nSummary saved to: {summary_path}")
    
    return results


if __name__ == "__main__":
    # Parse command line arguments
    parser = argparse.ArgumentParser(
        description="Run Budget Model for Full Financial Year",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
    python run_full_year.py --fy 2025
    python run_full_year.py --fy 2025 --cpp 23BCA1B3-56DD-4C15-A3D6-3C2C9A62E653
    python run_full_year.py --fy 2025 --no-save
        """
    )
    
    parser.add_argument(
        "--fy", 
        type=int, 
        required=True,
        help="Financial Year (e.g., 2025 for FY 2025-26: April 2025 to March 2026)"
    )
    
    parser.add_argument(
        "--cpp", 
        type=str, 
        default=None,
        help="CPP Plant ID (GUID). If not provided, uses the first available plant from DB."
    )
    
    parser.add_argument(
        "--no-save", 
        action="store_true",
        help="Don't save results to database (dry run)"
    )
    
    parser.add_argument(
        "--no-logs", 
        action="store_true",
        help="Don't save log files"
    )
    
    parser.add_argument(
        "--json", 
        action="store_true",
        help="Output results as JSON (for Java/API integration)"
    )
    
    parser.add_argument(
        "--auto", 
        action="store_true",
        help="Skip confirmation prompt (for automated runs from Java)"
    )
    
    args = parser.parse_args()
    
    # Display configuration
    fy_label = f"FY {args.fy}-{str(args.fy + 1)[-2:]}"
    
    print("\n" + "=" * 80)
    print("POWER PLANT BUDGETING - FULL FINANCIAL YEAR RUN")
    print(f"{fy_label} (April {args.fy} - March {args.fy + 1})")
    print("=" * 80)
    
    # Get CPP Plant ID
    cpp_plant_id = args.cpp
    if cpp_plant_id is None:
        cpp_plant_id = get_default_cpp_plant()
        if cpp_plant_id:
            print(f"\nUsing default CPP Plant: {cpp_plant_id}")
        else:
            print("\nERROR: No CPP Plant found in database!")
            sys.exit(1)
    else:
        print(f"\nUsing CPP Plant: {cpp_plant_id}")
    
    # Show available plants
    available_plants = get_available_cpp_plants()
    print(f"Available CPP Plants: {len(available_plants)}")
    for plant in available_plants:
        marker = " (selected)" if plant == cpp_plant_id else ""
        print(f"  - {plant}{marker}")
    
    print(f"\nSave to DB: {not args.no_save}")
    print(f"Save logs: {not args.no_logs}")
    
    # Ask for confirmation (skip if --auto or --json mode)
    if args.auto or args.json:
        proceed = True
    else:
        print("\nThis will run the budget model for all 12 months.")
        confirm = input("Proceed? (yes/no): ").strip().lower()
        proceed = (confirm == 'yes')
    
    if proceed:
        # Suppress console output in JSON mode
        if args.json:
            # Redirect stdout to suppress prints during execution
            original_stdout = sys.stdout
            sys.stdout = StringIO()
        
        try:
            # Run the full year with dynamic fixed demands
            results = run_full_financial_year(
                financial_year=args.fy,
                cpp_plant_id=cpp_plant_id,
                process_demands=None,  # Uses DEFAULT_PROCESS_DEMANDS, fixed demands fetched from DB
                save_to_db=not args.no_save,
                save_logs=not args.no_logs
            )
            
            if args.json:
                # Restore stdout and output JSON
                sys.stdout = original_stdout
                
                # Prepare JSON response
                json_response = {
                    "success": True,
                    "financial_year": f"{args.fy}-{str(args.fy + 1)[-2:]}",
                    "cpp_plant_id": cpp_plant_id,
                    "run_timestamp": results.get("run_timestamp"),
                    "summary": {
                        "total_months": results["summary"]["total_months"],
                        "successful": results["summary"]["successful"],
                        "failed": results["summary"]["failed"],
                        "total_power_kwh": results["summary"]["total_power_kwh"],
                        "total_shp_mt": results["summary"]["total_shp_mt"],
                    },
                    "months": {}
                }
                
                # Add month details
                for key, month_data in results.get("months", {}).items():
                    json_response["months"][key] = {
                        "month": month_data.get("month"),
                        "year": month_data.get("year"),
                        "month_name": month_data.get("month_name"),
                        "success": month_data.get("success", False),
                        "converged": month_data.get("converged", False),
                        "iterations": month_data.get("iterations", 0),
                        "total_power_kwh": month_data.get("total_power_kwh", 0),
                        "total_shp_mt": month_data.get("total_shp_mt", 0),
                        "records_saved": month_data.get("records_saved", 0),
                    }
                
                # Print JSON to stdout (for Java to capture)
                print(json.dumps(json_response, indent=2))
            else:
                print("\n" + "=" * 80)
                print("FULL YEAR RUN COMPLETED!")
                print("=" * 80)
        
        except Exception as e:
            if args.json:
                sys.stdout = original_stdout
                error_response = {
                    "success": False,
                    "error": str(e),
                    "financial_year": f"{args.fy}-{str(args.fy + 1)[-2:]}",
                    "cpp_plant_id": cpp_plant_id
                }
                print(json.dumps(error_response, indent=2))
                sys.exit(1)
            else:
                raise
    else:
        print("Run cancelled.")
