"""
Power Plant Budgeting - Main Entry Point
Follows Utility Automation Flowchart V0

FLOW:
1. INPUT: Month, Year, Steam Demands
2. POWER: Demand Check -> Capacity Check -> Dispatch
3. STEAM: Demand Calculation -> HRSG Availability (from GT) -> Capacity Check
4. USD ITERATION: Balance Power & Steam until converged (0.1% tolerance)
5. OUTPUT: Combined Result with adjustments
"""

import sys
import os
from datetime import datetime
from io import StringIO

from services.budget_service import (
    calculate_budget, 
    calculate_budget_with_iteration,
    print_detailed_results
)
from services.save_service import save_model_quantities
from services.process_demand_service import (
    get_process_demand_for_month,
    get_default_process_demands,
    print_process_demands,
)
from services.fixed_consumption_service import (
    get_fixed_consumption_for_month,
    get_default_fixed_consumption,
    print_fixed_consumption,
)

# ============================================================
# LOG OUTPUT CONFIGURATION
# ============================================================
# Change this path to your desired log folder
LOG_FOLDER = r"C:\Users\shrik\Desktop\Project\fork repo\development\PP python-script repo\PPPython-script\logs"

class TeeOutput:
    """Captures output to both console and a string buffer for logging."""
    def __init__(self, original_stdout):
        self.original_stdout = original_stdout
        self.buffer = StringIO()
    
    def write(self, text):
        self.original_stdout.write(text)
        self.buffer.write(text)
    
    def flush(self):
        self.original_stdout.flush()
    
    def get_output(self):
        return self.buffer.getvalue()

def save_log(output_text, month, year, log_folder):
    """Save the output to a timestamped log file."""
    # Create log folder if it doesn't exist
    os.makedirs(log_folder, exist_ok=True)
    
    # Generate filename with timestamp: log-yearmonth-date-hours-minute.txt
    now = datetime.now()
    filename = f"log-{year}{month:02d}-{now.day:02d}-{now.hour:02d}-{now.minute:02d}.txt"
    filepath = os.path.join(log_folder, filename)
    
    # Write to file
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(f"Budget Calculation Log\n")
        f.write(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"Period: {month}/{year}\n")
        f.write("="*80 + "\n\n")
        f.write(output_text)
    
    return filepath


if __name__ == "__main__":
    # Start capturing output
    tee = TeeOutput(sys.stdout)
    sys.stdout = tee
    print("="*70)
    print("POWER PLANT BUDGETING SYSTEM")
    print("With USD Iteration (Power-Steam Balancing)")
    print("="*70)
    
    # -----------------------------------------------------------
    # STEP 1: GET INPUTS
    # -----------------------------------------------------------
    print("\n--- INPUT: Period ---")
    month = int(input("Enter Month (1-12): "))
    year = int(input("Enter Year: "))
    
    # -----------------------------------------------------------
    # STEP 1.2: GET CPP PLANT ID
    # -----------------------------------------------------------
    print("\n--- CPP PLANT ID ---")
    print("Enter CPP Plant UUID (required for import power multi-source)")
    cpp_plant_id_input = input("CPP Plant ID [23BCA1B3-56DD-4C15-A3D6-3C2C9A62E653]: ").strip()
    cpp_plant_id = cpp_plant_id_input if cpp_plant_id_input else "23BCA1B3-56DD-4C15-A3D6-3C2C9A62E653"
    print(f"Using Plant ID: {cpp_plant_id}")
    
    # -----------------------------------------------------------
    # STEP 1.5: CHOOSE DATA SOURCE
    # -----------------------------------------------------------
    print("\n--- DATA SOURCE ---")
    print("1. Fetch demands from DATABASE (recommended)")
    print("2. Enter demands MANUALLY (use default/custom values)")
    data_source_input = input("Select data source [1]: ").strip()
    use_db_demands = data_source_input != '2'
    
    if use_db_demands:
        # Fetch from database
        print("\n--- FETCHING DEMANDS FROM DATABASE ---")
        process_demands = get_process_demand_for_month(month, year)
        fixed_demands = get_fixed_consumption_for_month(month, year)
        
        # Extract values
        lp_process = process_demands.get('lp_process', 30043.15)
        mp_process = process_demands.get('mp_process', 14030.65)
        hp_process = process_demands.get('hp_process', 4971.91)
        shp_process = process_demands.get('shp_process', 20975.34)
        air_process = process_demands.get('air_process', 6095102.0)
        cw1_process = process_demands.get('cw1_process', 15194.0)
        cw2_process = process_demands.get('cw2_process', 9016.0)
        dm_process = process_demands.get('dm_process', 54779.0)
        
        lp_fixed = fixed_demands.get('lp_fixed', 5169.51)
        mp_fixed = fixed_demands.get('mp_fixed', 518.00)
        hp_fixed = fixed_demands.get('hp_fixed', 0.00)
        shp_fixed = fixed_demands.get('shp_fixed', 0.00)
        
        # Display fetched values
        print("\n--- PROCESS DEMANDS (from DB) ---")
        print(f"  LP Process:  {lp_process:>12,.2f} MT")
        print(f"  MP Process:  {mp_process:>12,.2f} MT")
        print(f"  HP Process:  {hp_process:>12,.2f} MT")
        print(f"  SHP Process: {shp_process:>12,.2f} MT")
        print(f"  Compressed Air: {air_process:>12,.0f} NM3")
        print(f"  Cooling Water 1: {cw1_process:>12,.0f} KM3")
        print(f"  Cooling Water 2: {cw2_process:>12,.0f} KM3")
        print(f"  DM Water:    {dm_process:>12,.0f} M3")
        
        print("\n--- FIXED DEMANDS (from DB) ---")
        print(f"  LP Fixed:  {lp_fixed:>12,.2f} MT")
        print(f"  MP Fixed:  {mp_fixed:>12,.2f} MT")
        print(f"  HP Fixed:  {hp_fixed:>12,.2f} MT")
        print(f"  SHP Fixed: {shp_fixed:>12,.2f} MT")
        
        # Allow override if needed
        override_input = input("\nOverride any values? (y/n) [n]: ").strip().lower()
        if override_input == 'y':
            print("\n--- OVERRIDE VALUES (press Enter to keep DB value) ---")
            
            lp_process_input = input(f"LP Process [{lp_process:.2f}]: ").strip()
            if lp_process_input: lp_process = float(lp_process_input)
            
            mp_process_input = input(f"MP Process [{mp_process:.2f}]: ").strip()
            if mp_process_input: mp_process = float(mp_process_input)
            
            hp_process_input = input(f"HP Process [{hp_process:.2f}]: ").strip()
            if hp_process_input: hp_process = float(hp_process_input)
            
            shp_process_input = input(f"SHP Process [{shp_process:.2f}]: ").strip()
            if shp_process_input: shp_process = float(shp_process_input)
            
            lp_fixed_input = input(f"LP Fixed [{lp_fixed:.2f}]: ").strip()
            if lp_fixed_input: lp_fixed = float(lp_fixed_input)
            
            mp_fixed_input = input(f"MP Fixed [{mp_fixed:.2f}]: ").strip()
            if mp_fixed_input: mp_fixed = float(mp_fixed_input)
            
            hp_fixed_input = input(f"HP Fixed [{hp_fixed:.2f}]: ").strip()
            if hp_fixed_input: hp_fixed = float(hp_fixed_input)
            
            shp_fixed_input = input(f"SHP Fixed [{shp_fixed:.2f}]: ").strip()
            if shp_fixed_input: shp_fixed = float(shp_fixed_input)
    else:
        # Manual entry mode (original behavior)
        print("\n--- INPUT: Steam Demands (MT) ---")
        print("(Press Enter for default test values)")
        
        # LP Steam (Excel-matched: 30043.15 MT)
        lp_process_input = input("LP Process Demand [30043.15]: ").strip()
        lp_process = float(lp_process_input) if lp_process_input else 30043.15
        
        lp_fixed_input = input("LP Fixed Demand [5169.51]: ").strip()
        lp_fixed = float(lp_fixed_input) if lp_fixed_input else 5169.51
        
        # MP Steam (Excel-matched: 14030.65 MT)
        mp_process_input = input("MP Process Demand [14030.65]: ").strip()
        mp_process = float(mp_process_input) if mp_process_input else 14030.65
        
        mp_fixed_input = input("MP Fixed Demand [518.00]: ").strip()
        mp_fixed = float(mp_fixed_input) if mp_fixed_input else 518.00
        
        # HP Steam (Excel-matched: 4971.91 MT)
        hp_process_input = input("HP Process Demand [4971.91]: ").strip()
        hp_process = float(hp_process_input) if hp_process_input else 4971.91
        
        hp_fixed_input = input("HP Fixed Demand [0.00]: ").strip()
        hp_fixed = float(hp_fixed_input) if hp_fixed_input else 0.00
        
        # SHP Steam (Excel-matched: 20975.34 MT)
        shp_process_input = input("SHP Process Demand [20975.34]: ").strip()
        shp_process = float(shp_process_input) if shp_process_input else 20975.34
        
        shp_fixed_input = input("SHP Fixed Demand [0.00]: ").strip()
        shp_fixed = float(shp_fixed_input) if shp_fixed_input else 0.00
        
        # Process Utility Consumption (Excel-matched values)
        print("\n--- INPUT: Process Utility Consumption ---")
        print("(These are utilities consumed by PROCESS PLANTS, not utility plants)")
        
        # Compressed Air Process (Excel-matched: 6,095,102 NM3)
        air_process_input = input("Compressed Air Process (NM3) [6095102]: ").strip()
        air_process = float(air_process_input) if air_process_input else 6095102.0
        
        # Cooling Water 1 Process (Fixed + Process: 15,194 KM3)
        cw1_process_input = input("Cooling Water 1 (KM3) [15194]: ").strip()
        cw1_process = float(cw1_process_input) if cw1_process_input else 15194.0
        
        # Cooling Water 2 Process (Fixed + Process: 9,016 KM3)
        cw2_process_input = input("Cooling Water 2 (KM3) [9016]: ").strip()
        cw2_process = float(cw2_process_input) if cw2_process_input else 9016.0
        
        # DM Water Process (Excel-matched: 54,779 M3)
        dm_process_input = input("DM Water Process (M3) [54779]: ").strip()
        dm_process = float(dm_process_input) if dm_process_input else 54779.0
    
    # BFW for UFU (common for both modes)
    bfw_ufu = 0
    
    # Export Power Flag
    print("\n--- INPUT: Export Power ---")
    export_input = input("Is Export Power Available? (Y/N) [N]: ").strip().upper()
    export_available = export_input == 'Y'
    
    # -----------------------------------------------------------
    # STEP 2: SELECT CALCULATION MODE
    # -----------------------------------------------------------
    print("\n--- CALCULATION MODE ---")
    print("1. Basic (Power + Steam check only)")
    print("2. With USD Iteration (Power-Steam balancing)")
    mode_input = input("Select mode [2]: ").strip()
    use_iteration = mode_input != '1'
    
    # -----------------------------------------------------------
    # STEP 3: EXECUTE BUDGET CALCULATION
    # -----------------------------------------------------------
    if use_iteration:
        result = calculate_budget_with_iteration(
            month=month,
            year=year,
            cpp_plant_id=cpp_plant_id,
            lp_process=lp_process,
            lp_fixed=lp_fixed,
            mp_process=mp_process,
            mp_fixed=mp_fixed,
            hp_process=hp_process,
            hp_fixed=hp_fixed,
            shp_process=shp_process,
            shp_fixed=shp_fixed,
            bfw_ufu=bfw_ufu,
            export_available=export_available,
            air_process=air_process,
            cw1_process=cw1_process,
            cw2_process=cw2_process,
            dm_process=dm_process,
            save_to_db=True  # Auto-save QTY and Quantity to NormsMonthDetail
        )
    else:
        result = calculate_budget(
            month=month,
            year=year,
            cpp_plant_id=cpp_plant_id,
            lp_process=lp_process,
            lp_fixed=lp_fixed,
            mp_process=mp_process,
            mp_fixed=mp_fixed,
            hp_process=hp_process,
            hp_fixed=hp_fixed,
            shp_process=shp_process,
            shp_fixed=shp_fixed,
            bfw_ufu=bfw_ufu,
            air_process=air_process,
            cw1_process=cw1_process,
            cw2_process=cw2_process,
            dm_process=dm_process
        )
        
        # -----------------------------------------------------------
        # STEP 4: PRINT DETAILED RESULTS (Basic mode only)
        # -----------------------------------------------------------
        if result["overall_success"]:
            show_details = input("\nShow detailed results? (y/n) [y]: ").strip().lower()
            if show_details != 'n':
                print_detailed_results(result)
        else:
            print("\n" + "="*60)
            print("BUDGET CALCULATION FAILED")
            print("="*60)
            for error in result["errors"]:
                print(f"\nStage: {error['stage']}")
                print(f"Error Type: {error['error_type']}")
                print(f"Message: {error['message']}")
    
    # -----------------------------------------------------------
    # STEP 5: SAVE TO DATABASE (Optional - only for basic mode)
    # -----------------------------------------------------------
    # Note: When using iteration mode with save_to_db=True, the new norms_save_service
    # already saves all 118 records automatically. This old save is only for basic mode.
    if result.get("overall_success", False) and not use_iteration:
        save_input = input("\nSave calculated quantities to database? (y/n) [n]: ").strip().lower()
        if save_input == 'y':
            # Extract utilities and power dispatch from result
            utilities = result.get("utility_consumption", {})
            
            # Build power dispatch dict from result
            usd_result = result.get("usd_result", {})
            final_dispatch = usd_result.get("final_dispatch", [])
            final_steam = usd_result.get("final_steam_balance", {})
            
            power_dispatch = {
                "gt1_gross_kwh": 0,
                "gt2_gross_kwh": 0,
                "gt3_gross_kwh": 0,
                "stg_gross_kwh": 0,
                "hrsg1_shp_mt": 0,
                "hrsg2_shp_mt": 0,
                "hrsg3_shp_mt": 0,
                "oxygen_mt": 5786.0,
                "effluent_m3": 243000.0,
            }
            
            # Extract from dispatch
            for asset in final_dispatch:
                name = str(asset.get("AssetName", "")).upper()
                gross_mwh = asset.get("GrossMWh", 0)
                gross_kwh = gross_mwh * 1000
                
                if "GT1" in name or "POWER PLANT 3" in name:
                    power_dispatch["gt1_gross_kwh"] = gross_kwh
                elif "GT2" in name or "POWER PLANT 2" in name:
                    power_dispatch["gt2_gross_kwh"] = gross_kwh
                elif "GT3" in name or "POWER PLANT 1" in name:
                    power_dispatch["gt3_gross_kwh"] = gross_kwh
                elif "STG" in name:
                    power_dispatch["stg_gross_kwh"] = gross_kwh
            
            # Extract HRSG SHP from steam balance
            shp_balance = final_steam.get("shp_balance", {})
            power_dispatch["hrsg1_shp_mt"] = shp_balance.get("shp_from_hrsg1", 0)
            power_dispatch["hrsg2_shp_mt"] = shp_balance.get("shp_from_hrsg2", 0)
            power_dispatch["hrsg3_shp_mt"] = shp_balance.get("shp_from_hrsg3", 0)
            
            # Save to database
            save_result = save_model_quantities(month, year, utilities, power_dispatch)
            
            if save_result["success_count"] > 0:
                print(f"\n✓ Successfully saved {save_result['success_count']} records to database")
            if save_result["failed_count"] > 0:
                print(f"✗ Failed to save {save_result['failed_count']} records")
    
    # -----------------------------------------------------------
    # STEP 6: SAVE LOG FILE
    # -----------------------------------------------------------
    # Restore original stdout
    sys.stdout = tee.original_stdout
    
    # Save log to file
    log_path = save_log(tee.get_output(), month, year, LOG_FOLDER)
    print(f"\n{'='*70}")
    print(f"LOG SAVED TO: {log_path}")
    print(f"{'='*70}")
