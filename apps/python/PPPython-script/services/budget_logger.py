"""
Budget Calculation Logger
=========================
Centralized logging module for budget calculations.
Provides consistent, well-structured output with clear section headers and proper sequencing.

LOG STRUCTURE:
==============
1. HEADER - Budget calculation start
2. INPUT SUMMARY - Fixed demands and parameters
3. NORMS REFERENCE - All norms used in calculations
4. POWER SECTION
   4.1 Asset Availability
   4.2 Import Power Status
   4.3 Demand Summary
   4.4 Dispatch Result
   4.5 Power Balance Verification
5. STEAM SECTION
   5.1 HRSG Availability
   5.2 SHP Balance Analysis
   5.3 Steam Balance Summary
6. ITERATION SECTION
   6.1 Iteration Progress
   6.2 Convergence Check
   6.3 U4U Power Calculation
7. FINAL RESULTS
   7.1 Power Summary
   7.2 Steam Summary
   7.3 Utility Consumption
8. FOOTER - Completion status
"""

# ============================================================
# FORMATTING CONSTANTS
# ============================================================
LINE_WIDTH = 100
SECTION_CHAR = "="
SUBSECTION_CHAR = "-"
BOX_CHAR = "│"
BOX_TOP = "┌"
BOX_BOTTOM = "└"
BOX_MID = "├"
BOX_LINE = "─"


# ============================================================
# SECTION HEADERS
# ============================================================
def print_header(title: str, width: int = LINE_WIDTH, char: str = SECTION_CHAR):
    """Print a major section header."""
    print("\n" + char * width)
    print(f"  {title}")
    print(char * width)


def print_subheader(title: str, width: int = LINE_WIDTH, char: str = SUBSECTION_CHAR):
    """Print a subsection header."""
    print("\n" + char * width)
    print(f"  {title}")
    print(char * width)


def print_step_header(step_num: int, title: str, width: int = LINE_WIDTH):
    """Print a numbered step header."""
    print("\n" + "=" * width)
    print(f"  STEP {step_num}: {title}")
    print("=" * width)


def print_iteration_header(iteration: int, width: int = LINE_WIDTH):
    """Print an iteration header."""
    print("\n" + "-" * width)
    print(f"  ITERATION {iteration}")
    print("-" * width)


# ============================================================
# TABLE FORMATTING
# ============================================================
def print_table_header(columns: list, widths: list = None):
    """Print a table header row."""
    if widths is None:
        widths = [15] * len(columns)
    
    header = "  "
    separator = "  "
    for col, width in zip(columns, widths):
        header += f"{col:<{width}}"
        separator += "-" * width
    
    print(separator)
    print(header)
    print(separator)


def print_table_row(values: list, widths: list = None, formats: list = None):
    """Print a table row with optional formatting."""
    if widths is None:
        widths = [15] * len(values)
    if formats is None:
        formats = ["s"] * len(values)
    
    row = "  "
    for val, width, fmt in zip(values, widths, formats):
        if fmt == "s":
            row += f"{str(val):<{width}}"
        elif fmt == "d":
            row += f"{int(val):<{width}}"
        elif fmt == ".2f":
            row += f"{float(val):<{width}.2f}"
        elif fmt == ".4f":
            row += f"{float(val):<{width}.4f}"
        elif fmt == ",.2f":
            row += f"{float(val):>{width},.2f}"
        else:
            row += f"{val:<{width}}"
    print(row)


def print_table_separator(widths: list = None, total_width: int = LINE_WIDTH):
    """Print a table separator line."""
    if widths:
        print("  " + "-" * sum(widths))
    else:
        print("  " + "-" * (total_width - 4))


# ============================================================
# BOX FORMATTING (for summaries)
# ============================================================
def print_box_start(width: int = 70):
    """Print box top border."""
    print(f"  {BOX_TOP}" + BOX_LINE * width + "┐")


def print_box_end(width: int = 70):
    """Print box bottom border."""
    print(f"  {BOX_BOTTOM}" + BOX_LINE * width + "┘")


def print_box_separator(width: int = 70):
    """Print box middle separator."""
    print(f"  {BOX_MID}" + BOX_LINE * width + "┤")


def print_box_line(text: str, width: int = 70):
    """Print a line inside a box."""
    print(f"  {BOX_CHAR} {text:<{width-1}}{BOX_CHAR}")


def print_box_title(title: str, width: int = 70):
    """Print a title line inside a box."""
    print(f"  {BOX_CHAR} {title:<{width-1}}{BOX_CHAR}")


# ============================================================
# KEY-VALUE FORMATTING
# ============================================================
def print_kv(key: str, value, format_spec: str = ".2f", key_width: int = 35, value_width: int = 15, unit: str = ""):
    """Print a key-value pair with consistent formatting."""
    if isinstance(value, (int, float)):
        if format_spec == ".2f":
            val_str = f"{value:>{value_width},.2f}"
        elif format_spec == ".4f":
            val_str = f"{value:>{value_width}.4f}"
        elif format_spec == ".6f":
            val_str = f"{value:>{value_width}.6f}"
        elif format_spec == "d":
            val_str = f"{int(value):>{value_width},}"
        else:
            val_str = f"{value:>{value_width}}"
    else:
        val_str = f"{str(value):>{value_width}}"
    
    if unit:
        print(f"  {key:<{key_width}} {val_str} {unit}")
    else:
        print(f"  {key:<{key_width}} {val_str}")


def print_kv_line(key: str, value, format_spec: str = ".2f", key_width: int = 35, value_width: int = 15):
    """Print a key-value pair with a separator line."""
    print_kv(key, value, format_spec, key_width, value_width)
    print(f"  {'-' * (key_width + value_width + 2)}")


# ============================================================
# SPECIALIZED LOGGING FUNCTIONS
# ============================================================

def log_budget_start(month: int, year: int, export_available: bool = False):
    """Log the start of budget calculation."""
    print("\n" + "=" * LINE_WIDTH)
    print("  BUDGET CALCULATION WITH USD ITERATION")
    print("=" * LINE_WIDTH)
    print(f"  Period: {month}/{year}")
    print(f"  Export Power Available: {'YES' if export_available else 'NO'}")
    print("=" * LINE_WIDTH)


def log_fixed_demands(lp_process: float, lp_fixed: float, lp_ufu: float, lp_total: float,
                      mp_process: float, mp_fixed: float, mp_for_lp: float, mp_total: float,
                      hp_process: float, hp_fixed: float,
                      shp_process: float, shp_fixed: float):
    """Log fixed steam demands input."""
    print_step_header(1, "FIXED STEAM DEMANDS (Input)")
    
    print(f"\n  {'Steam Type':<25} {'Demand (MT)':>15}")
    print(f"  {'-' * 42}")
    print(f"  {'LP Process':<25} {lp_process:>15,.2f}")
    print(f"  {'LP Fixed':<25} {lp_fixed:>15,.2f}")
    print(f"  {'LP from BFW UFU':<25} {lp_ufu:>15,.2f}")
    print(f"  {'LP TOTAL':<25} {lp_total:>15,.2f}")
    print(f"  {'-' * 42}")
    print(f"  {'MP Process':<25} {mp_process:>15,.2f}")
    print(f"  {'MP Fixed':<25} {mp_fixed:>15,.2f}")
    print(f"  {'MP for LP PRDS':<25} {mp_for_lp:>15,.2f}")
    print(f"  {'MP TOTAL':<25} {mp_total:>15,.2f}")
    print(f"  {'-' * 42}")
    print(f"  {'HP Process':<25} {hp_process:>15,.2f}")
    print(f"  {'HP Fixed':<25} {hp_fixed:>15,.2f}")
    print(f"  {'-' * 42}")
    print(f"  {'SHP Process':<25} {shp_process:>15,.2f}")
    print(f"  {'SHP Fixed':<25} {shp_fixed:>15,.2f}")


def log_norms_reference(norms: dict):
    """Log all norms used in calculations."""
    print_subheader("NORMS REFERENCE")
    
    # Power Plant Norms
    print("\n  [POWER PLANT NORMS]")
    print(f"  {'Norm':<40} {'Value':>12} {'Unit':<15}")
    print(f"  {'-' * 70}")
    print(f"  {'GT Auxiliary Consumption':<40} {norms.get('gt_aux', 0.014):>12.4f} {'KWH/KWH':<15}")
    print(f"  {'STG Auxiliary Consumption':<40} {norms.get('stg_aux', 0.002):>12.4f} {'KWH/KWH':<15}")
    print(f"  {'STG SHP Steam Requirement':<40} {norms.get('stg_shp', 0.00356):>12.4f} {'MT/KWH':<15}")
    
    # HRSG Norms
    print("\n  [HRSG NORMS (per MT SHP)]")
    print(f"  {'Norm':<40} {'Value':>12} {'Unit':<15}")
    print(f"  {'-' * 70}")
    print(f"  {'BFW Consumption':<40} {norms.get('hrsg_bfw', 1.024):>12.4f} {'M3/MT SHP':<15}")
    print(f"  {'Natural Gas':<40} {norms.get('hrsg_ng', 2.8116):>12.4f} {'MMBTU/MT SHP':<15}")
    print(f"  {'LP Steam Credit':<40} {norms.get('hrsg_lp_credit', -0.0504):>12.4f} {'MT LP/MT SHP':<15}")
    
    # Utility Power Norms
    print("\n  [UTILITY POWER NORMS]")
    print(f"  {'Norm':<40} {'Value':>12} {'Unit':<15}")
    print(f"  {'-' * 70}")
    print(f"  {'BFW Power':<40} {norms.get('bfw_power', 9.5):>12.4f} {'KWH/M3':<15}")
    print(f"  {'DM Water Power':<40} {norms.get('dm_power', 1.21):>12.4f} {'KWH/M3':<15}")
    print(f"  {'Cooling Water 1 Power':<40} {norms.get('cw1_power', 245.0):>12.4f} {'KWH/KM3':<15}")
    print(f"  {'Cooling Water 2 Power':<40} {norms.get('cw2_power', 250.0):>12.4f} {'KWH/KM3':<15}")
    print(f"  {'Compressed Air Power':<40} {norms.get('air_power', 0.165):>12.4f} {'KWH/NM3':<15}")
    print(f"  {'Effluent Treatment Power':<40} {norms.get('effluent_power', 3.54):>12.4f} {'KWH/M3':<15}")
    print(f"  {'Oxygen Power':<40} {norms.get('oxygen_power', 936.04):>12.4f} {'KWH/MT':<15}")


def log_power_assets(assets: list, total_min: float, total_max: float):
    """Log power generation assets availability."""
    print_step_header(2, "POWER GENERATION ASSETS")
    
    print(f"\n  {'Asset':<25} {'Avail':<8} {'Priority':<10} {'Min MW':<10} {'Max MW':<10} {'Hours':<8} {'Min MWh':<12} {'Max MWh':<12}")
    print(f"  {'-' * 95}")
    
    for asset in assets:
        name = asset.get('name', 'Unknown')[:24]
        avail = 'YES' if asset.get('available', False) else 'NO'
        priority = asset.get('priority', '-')
        min_mw = asset.get('min_mw', 0)
        max_mw = asset.get('max_mw', 0)
        hours = asset.get('hours', 0)
        min_mwh = asset.get('min_mwh', 0)
        max_mwh = asset.get('max_mwh', 0)
        
        if avail == 'YES':
            print(f"  {name:<25} {avail:<8} {priority:<10} {min_mw:<10.2f} {max_mw:<10.2f} {hours:<8.0f} {min_mwh:<12.2f} {max_mwh:<12.2f}")
        else:
            print(f"  {name:<25} {avail:<8} {'-':<10} {0:<10.2f} {0:<10.2f} {0:<8.0f} {0:<12.2f} {0:<12.2f}")
    
    print(f"  {'-' * 95}")
    print(f"  {'TOTAL (Available)':<25} {'':<8} {'':<10} {'':<10} {'':<10} {'':<8} {total_min:<12.2f} {total_max:<12.2f}")


def log_import_power(import_available: bool, import_capacity_mw: float, import_used_mwh: float, max_import_mwh: float):
    """Log import power status."""
    print_subheader("IMPORT POWER STATUS")
    
    status = "AVAILABLE" if import_available else "NOT AVAILABLE"
    print(f"\n  Import Status:          {status}")
    if import_available:
        print(f"  Import Capacity:        {import_capacity_mw:>12.2f} MW")
        print(f"  Max Import (monthly):   {max_import_mwh:>12.2f} MWh")
        print(f"  Import Used:            {import_used_mwh:>12.2f} MWh")
    else:
        print(f"  Import Capacity:        {0:>12.2f} MW")
        print(f"  Import Used:            {0:>12.2f} MWh")


def log_power_demand_summary(plant_demand: float, fixed_demand: float, u4u_power: float, 
                             import_power: float, net_demand: float):
    """Log power demand summary in a clear box format."""
    print_subheader("POWER DEMAND SUMMARY")
    
    base_demand = plant_demand + fixed_demand
    total_demand = base_demand + u4u_power
    
    print(f"""
  ┌────────────────────────────────────────────────────────────────────┐
  │ STEP 1: BASE DEMAND (Consumer Requirements)                        │
  │   Process Plant Demand:           {plant_demand:>15,.2f} MWh        │
  │   Fixed Consumption:              {fixed_demand:>15,.2f} MWh        │
  │   ─────────────────────────────────────────────                    │
  │   Base Demand:                    {base_demand:>15,.2f} MWh        │
  ├────────────────────────────────────────────────────────────────────┤
  │ STEP 2: ADD U4U (Utility Power Consumption)                        │
  │   U4U Power (Aux + Utility):      {u4u_power:>15,.2f} MWh        │
  │   ─────────────────────────────────────────────                    │
  │   TOTAL DEMAND:                   {total_demand:>15,.2f} MWh        │
  ├────────────────────────────────────────────────────────────────────┤
  │ STEP 3: SUBTRACT IMPORT (External Supply)                          │
  │   Import Power:                   {import_power:>15,.2f} MWh        │
  │   ─────────────────────────────────────────────                    │
  │   NET DEMAND (for dispatch):      {net_demand:>15,.2f} MWh        │
  └────────────────────────────────────────────────────────────────────┘""")


def log_power_dispatch(dispatch: list, total_gross: float, total_aux: float, total_net: float):
    """Log power dispatch result."""
    print_step_header(3, "POWER DISPATCH RESULT")
    
    print(f"\n  {'Asset':<20} {'Priority':<10} {'Load MW':<10} {'Gross MWh':<12} {'Aux MWh':<10} {'Net MWh':<12} {'Status':<12}")
    print(f"  {'-' * 86}")
    
    for d in dispatch:
        name = d.get('AssetName', 'Unknown')[:19]
        priority = d.get('Priority', '-')
        load_mw = d.get('LoadMW', 0)
        gross = d.get('GrossMWh', 0)
        aux = d.get('AuxMWh', 0)
        net = d.get('NetMWh', 0)
        
        # Determine status
        min_mw = d.get('MinMW', 0)
        max_mw = d.get('CapacityMW', 0)
        if gross == 0:
            status = "OFF"
        elif abs(load_mw - min_mw) < 0.1:
            status = "MIN LOAD"
        elif abs(load_mw - max_mw) < 0.1:
            status = "MAX LOAD"
        else:
            status = "PARTIAL"
        
        print(f"  {name:<20} {priority:<10} {load_mw:<10.2f} {gross:<12.2f} {aux:<10.2f} {net:<12.2f} {status:<12}")
    
    print(f"  {'-' * 86}")
    print(f"  {'TOTAL':<20} {'':<10} {'':<10} {total_gross:<12.2f} {total_aux:<10.2f} {total_net:<12.2f}")


def log_power_balance(total_demand: float, import_power: float, gross_gen: float, 
                      aux_consumption: float, net_gen: float):
    """Log power balance verification."""
    total_supply = import_power + net_gen
    balance_diff = total_supply - total_demand
    
    if abs(balance_diff) < 1:
        status = "✓ BALANCED: Supply = Demand"
    elif balance_diff > 0:
        status = f"EXCESS: {balance_diff:,.2f} MWh (available for EXPORT)"
    else:
        status = f"SHORTFALL: {abs(balance_diff):,.2f} MWh"
    
    print(f"""
  ┌────────────────────────────────────────────────────────────────────┐
  │ POWER BALANCE VERIFICATION                                         │
  ├────────────────────────────────────────────────────────────────────┤
  │ DEMAND:                           {total_demand:>15,.2f} MWh        │
  ├────────────────────────────────────────────────────────────────────┤
  │ SUPPLY:                                                            │
  │   Import Power:                   {import_power:>15,.2f} MWh        │
  │   Gross Generation:               {gross_gen:>15,.2f} MWh        │
  │   Auxiliary Consumption:          {aux_consumption:>15,.2f} MWh        │
  │   Net Generation:                 {net_gen:>15,.2f} MWh        │
  │   ─────────────────────────────────────────────                    │
  │   TOTAL SUPPLY:                   {total_supply:>15,.2f} MWh        │
  ├────────────────────────────────────────────────────────────────────┤
  │ {status:<67}│
  └────────────────────────────────────────────────────────────────────┘""")


def log_hrsg_availability(hrsg_details: list, total_free_steam: float, total_supp_max: float, total_capacity: float):
    """Log HRSG availability and SHP capacity."""
    print_step_header(4, "HRSG AVAILABILITY & SHP CAPACITY")
    
    print("\n  (HRSG availability linked to GT dispatch)")
    print(f"\n  {'HRSG':<12} {'Available':<10} {'Hours':<10} {'Free Steam':<12} {'Supp Max':<12} {'Total Max':<12}")
    print(f"  {'-' * 68}")
    
    for hrsg in hrsg_details:
        name = hrsg.get('name', 'Unknown')[:11]
        avail = 'YES' if hrsg.get('is_available', False) else 'NO'
        hours = hrsg.get('hours', 0)
        free_steam = hrsg.get('free_steam_mt', 0)
        supp_max = hrsg.get('supp_max_mt_month', 0)
        total_max = free_steam + supp_max
        
        print(f"  {name:<12} {avail:<10} {hours:<10.0f} {free_steam:<12.2f} {supp_max:<12.2f} {total_max:<12.2f}")
    
    print(f"  {'-' * 68}")
    print(f"  {'TOTAL':<12} {'':<10} {'':<10} {total_free_steam:<12.2f} {total_supp_max:<12.2f} {total_capacity:<12.2f}")


def log_shp_balance(shp_process: float, shp_fixed: float, shp_stg_power: float,
                    shp_lp_extraction: float, shp_mp_extraction: float,
                    shp_hp_prds: float, shp_mp_prds: float,
                    total_demand: float, free_steam: float, supp_needed: float,
                    max_capacity: float, deficit: float, utilization: float):
    """Log SHP balance analysis."""
    print_step_header(5, "SHP BALANCE ANALYSIS")
    
    can_meet = deficit <= 0
    status = "CAN MEET DEMAND" if can_meet else "CANNOT MEET DEMAND"
    
    print(f"""
  ┌─────────────────────────────────────────────────────────────────┐
  │ SHP DEMAND BREAKDOWN                                            │
  ├─────────────────────────────────────────────────────────────────┤
  │   SHP Process Demand:             {shp_process:>12,.2f} MT        │
  │   SHP Fixed Demand:               {shp_fixed:>12,.2f} MT        │
  │   SHP for STG Power:              {shp_stg_power:>12,.2f} MT        │
  │   SHP for LP Extraction (STG):    {shp_lp_extraction:>12,.2f} MT        │
  │   SHP for MP Extraction (STG):    {shp_mp_extraction:>12,.2f} MT        │
  │   SHP for HP PRDS:                {shp_hp_prds:>12,.2f} MT        │
  │   SHP for MP PRDS:                {shp_mp_prds:>12,.2f} MT        │
  │   ─────────────────────────────────────────────                 │
  │   TOTAL SHP DEMAND:               {total_demand:>12,.2f} MT        │
  ├─────────────────────────────────────────────────────────────────┤
  │ SHP SUPPLY                                                      │
  ├─────────────────────────────────────────────────────────────────┤
  │   Free Steam (from GT):           {free_steam:>12,.2f} MT        │
  │   Supplementary Firing Needed:    {supp_needed:>12,.2f} MT        │
  │   Max SHP Capacity:               {max_capacity:>12,.2f} MT        │
  ├─────────────────────────────────────────────────────────────────┤
  │ BALANCE                                                         │
  ├─────────────────────────────────────────────────────────────────┤
  │   Deficit (Demand - Capacity):    {deficit:>12,.2f} MT        │
  │   Utilization:                    {utilization:>12.2f} %         │
  │   Status: {status:<54}│
  └─────────────────────────────────────────────────────────────────┘""")


def log_u4u_power(power_aux: float, gt1_aux: float, gt2_aux: float, gt3_aux: float, stg_aux: float,
                  utility_power: float, bfw_power: float, dm_power: float, cw1_power: float,
                  cw2_power: float, air_power: float, oxygen_power: float, effluent_power: float,
                  total_u4u: float):
    """Log U4U power calculation."""
    print_subheader("U4U POWER CALCULATION")
    
    print(f"""
  ┌─────────────────────────────────────────────────────────────────┐
  │ POWER PLANT AUXILIARY                     {power_aux:>12,.2f} MWh   │
  │   - GT1 Aux:                              {gt1_aux:>12,.2f} MWh   │
  │   - GT2 Aux:                              {gt2_aux:>12,.2f} MWh   │
  │   - GT3 Aux:                              {gt3_aux:>12,.2f} MWh   │
  │   - STG Aux:                              {stg_aux:>12,.2f} MWh   │
  ├─────────────────────────────────────────────────────────────────┤
  │ UTILITY POWER                             {utility_power:>12,.2f} MWh   │
  │   - BFW Power:                            {bfw_power:>12,.2f} MWh   │
  │   - DM Power:                             {dm_power:>12,.2f} MWh   │
  │   - CW1 Power:                            {cw1_power:>12,.2f} MWh   │
  │   - CW2 Power:                            {cw2_power:>12,.2f} MWh   │
  │   - Air Power:                            {air_power:>12,.2f} MWh   │
  │   - Oxygen Power:                         {oxygen_power:>12,.2f} MWh   │
  │   - Effluent Power:                       {effluent_power:>12,.2f} MWh   │
  ├─────────────────────────────────────────────────────────────────┤
  │ TOTAL U4U POWER                           {total_u4u:>12,.2f} MWh   │
  └─────────────────────────────────────────────────────────────────┘""")


def log_convergence_check(iteration: int, current_aux: float, previous_aux: float, 
                          aux_error: float, shp_deficit: float, tolerance: float,
                          converged: bool):
    """Log convergence check status."""
    status = "CONVERGED" if converged else "ITERATING"
    
    print(f"""
  ┌─────────────────────────────────────────────────────────────────┐
  │ CONVERGENCE CHECK - Iteration {iteration:<3}                            │
  ├─────────────────────────────────────────────────────────────────┤
  │   Current U4U Power:              {current_aux:>12,.4f} MWh       │
  │   Previous U4U Power:             {previous_aux:>12,.4f} MWh       │
  │   Power Error:                    {aux_error:>12,.6f} MWh       │
  │   Tolerance:                      {tolerance:>12,.7f} MWh       │
  │   SHP Deficit:                    {shp_deficit:>12,.2f} MT        │
  ├─────────────────────────────────────────────────────────────────┤
  │   Status: {status:<54}│
  └─────────────────────────────────────────────────────────────────┘""")


def log_iteration_history(history: list):
    """Log iteration history summary."""
    print_subheader("ITERATION HISTORY")
    
    print(f"\n  {'Iter':<6} {'Demand MWh':<14} {'Gross MWh':<14} {'Prev Aux':<14} {'New Aux':<14} {'Error':<14} {'Status':<12}")
    print(f"  {'-' * 88}")
    
    for it in history:
        print(f"  {it['iteration']:<6} {it['total_demand_mwh']:<14,.2f} {it['total_gross_mwh']:<14,.2f} "
              f"{it.get('previous_aux_mwh', 0):<14.4f} {it.get('current_aux_mwh', 0):<14.4f} "
              f"{it.get('aux_power_error_mwh', 0):<14.6f} {it['status']:<12}")


def log_final_results(converged: bool, iterations: int, final_aux: float,
                      stg_reduction: float, import_compensation: float):
    """Log final results summary."""
    print_header("FINAL RESULTS SUMMARY")
    
    status = "CONVERGED" if converged else "NOT CONVERGED"
    
    print(f"""
  ┌─────────────────────────────────────────────────────────────────┐
  │ USD ITERATION RESULT                                            │
  ├─────────────────────────────────────────────────────────────────┤
  │   Status:                         {status:<20}          │
  │   Iterations Used:                {iterations:>12}              │
  │   Final U4U Power:                {final_aux:>12,.4f} MWh       │
  │   STG Reduction:                  {stg_reduction:>12,.2f} MWh       │
  │   Import Compensation:            {import_compensation:>12,.2f} MWh       │
  └─────────────────────────────────────────────────────────────────┘""")


def log_budget_complete(converged: bool):
    """Log budget calculation completion."""
    print("\n" + "=" * LINE_WIDTH)
    if converged:
        print("  ✓ USD ITERATION CONVERGED - BUDGET CALCULATION COMPLETE")
    else:
        print("  ✗ USD ITERATION DID NOT CONVERGE")
    print("=" * LINE_WIDTH)


# ============================================================
# UTILITY FUNCTIONS
# ============================================================
def format_number(value: float, decimals: int = 2, thousands: bool = True) -> str:
    """Format a number with optional thousands separator."""
    if thousands:
        return f"{value:,.{decimals}f}"
    return f"{value:.{decimals}f}"


def format_percent(value: float, decimals: int = 2) -> str:
    """Format a percentage value."""
    return f"{value:.{decimals}f}%"
