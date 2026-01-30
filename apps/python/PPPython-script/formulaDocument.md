# Reverse-Calculated Norms and Plant Utilities Documentation

## Overview
This document provides a comprehensive list of all **reverse-calculated norms** and **plant utilities** used in the Python budget model, along with their formulas and calculation methods.

---

## Table of Contents
1. [Reverse-Calculated Norms](#reverse-calculated-norms)
2. [Plant Utilities and Their Formulas](#plant-utilities-and-their-formulas)
3. [Steam Balance Calculations](#steam-balance-calculations)
4. [Power Generation Requirements](#power-generation-requirements)

---

# 1. Reverse-Calculated Norms

## 1.1 GT Natural Gas (MMBTU per KWH) - **REVERSE CALCULATED**

### Formula
```
NET GT MMBTU = GROSS MMBTU - FREE STEAM MMBTU
```

### Detailed Calculation

**Step 1: GROSS MMBTU**
```
Gross MMBTU = KWH × Heat Rate × 3.96567 / 1,000,000
```

**Step 2: FREE STEAM MMBTU**
```
Free Steam MMBTU = KWH × FreeSteamFactor × 760.87 × 3.96567 / 1,000,000
```

**Step 3: NET MMBTU**
```
Net MMBTU = Gross MMBTU - Free Steam MMBTU
```

**Step 4: NORM (MMBTU per KWH)**
```
GT NG Norm = Net MMBTU / KWH
```

### Parameters
- **KWH**: GT generation in KWH
- **Heat Rate**: From `HeatRateLookup` table (KCAL/KWH) - varies by GT load
- **FreeSteamFactor**: From `HeatRateLookup` table (e.g., 1.97) - varies by GT load
- **760.87**: Free steam energy = (810 - 110) / 0.92 KCAL/kg
  - 810 = SHP steam enthalpy (KCAL/kg)
  - 110 = HRSG inlet enthalpy (KCAL/kg)
  - 0.92 = HRSG efficiency
- **3.96567**: KCAL to BTU conversion factor
- **1,000,000**: BTU to MMBTU conversion

### Applies To
- **GT1 Natural Gas Norm** (MMBTU/KWH)
- **GT2 Natural Gas Norm** (MMBTU/KWH)
- **GT3 Natural Gas Norm** (MMBTU/KWH)

### Location in Code
`services/utility_service.py` - `calculate_gt_ng_mmbtu()` function

---

## 1.2 HRSG Natural Gas (MMBTU per MT SHP) - **REVERSE CALCULATED**

### Formula
```
HRSG NG = (GT Gross MMBTU - GT Net MMBTU) / Free Steam MT
```

Where:
```
Free Steam MT = KWH × FreeSteamFactor
GT Net MMBTU = KWH × GT NG Norm (from reverse calculation above)
GT Gross MMBTU = KWH × Heat Rate × 3.96567 / 1,000,000
```

### Simplified Formula
```
HRSG NG Norm = (Heat Rate × 3.96567 / 1,000,000 - GT NG Norm) / FreeSteamFactor
```

Or:
```
HRSG NG Norm = (FreeSteamFactor × 760.87 × 3.96567 / 1,000,000) / FreeSteamFactor
              = 760.87 × 3.96567 / 1,000,000
              = 0.003017 MMBTU/kg
              = 3.017 MMBTU/MT (approximately)
```

### Applies To
- **HRSG1 Natural Gas Norm** (MMBTU/MT SHP)
- **HRSG2 Natural Gas Norm** (MMBTU/MT SHP)
- **HRSG3 Natural Gas Norm** (MMBTU/MT SHP)

### Note
The HRSG NG norm is calculated from the "free steam" energy that would otherwise be wasted. This represents the supplementary firing needed to produce SHP steam.

### Location in Code
`services/iteration_service.py` - HRSG NG reverse calculation logic

---

## 1.3 LP/MP Steam Distribution Ratios - **REVERSE CALCULATED**

### Formula (STG Load-Based)
```
LP from STG = STG LP Extraction Rate (TPH) × STG Operating Hours
LP from PRDS = Total LP Demand - LP from STG

LP STG Ratio = LP from STG / Total LP Demand
LP PRDS Ratio = LP from PRDS / Total LP Demand
```

Similarly for MP:
```
MP from STG = STG MP Extraction Rate (TPH) × STG Operating Hours
MP from PRDS = Total MP Demand - MP from STG

MP STG Ratio = MP from STG / Total MP Demand
MP PRDS Ratio = MP from PRDS / Total MP Demand
```

### Parameters
- **STG LP Extraction Rate**: From `STGExtractionLookup` table (TPH) - varies by STG load
- **STG MP Extraction Rate**: From `STGExtractionLookup` table (TPH) - varies by STG load
- **STG Operating Hours**: Calculated from STG gross generation and load

### Default Ratios (Legacy - Fixed)
- **LP from PRDS**: 38.66% (0.3866)
- **LP from STG**: 61.34% (0.6134)
- **MP from PRDS**: 70.92% (0.7092)
- **MP from STG**: 29.08% (0.2908)

### Applies To
- **LP Steam Distribution Norms**
- **MP Steam Distribution Norms**

### Location in Code
`services/steam_service.py` - `calculate_lp_balance_stg_based()` and `calculate_mp_balance_stg_based()`

---

# 2. Plant Utilities and Their Formulas

## 2.1 Natural Gas (MMBTU)

### GT1 Natural Gas
```
GT1 NG = GT1 KWH × GT1 NG Norm (reverse calculated)
```

### GT2 Natural Gas
```
GT2 NG = GT2 KWH × GT2 NG Norm (reverse calculated)
```

### GT3 Natural Gas
```
GT3 NG = GT3 KWH × GT3 NG Norm (reverse calculated)
```

### HRSG1 Natural Gas (Supplementary Firing)
```
HRSG1 NG = HRSG1 SHP MT × HRSG1 NG Norm (reverse calculated)
```

### HRSG2 Natural Gas (Supplementary Firing)
```
HRSG2 NG = HRSG2 SHP MT × HRSG2 NG Norm (reverse calculated)
```

### HRSG3 Natural Gas (Supplementary Firing)
```
HRSG3 NG = HRSG3 SHP MT × HRSG3 NG Norm (reverse calculated)
```

### Total Natural Gas
```
Total NG = GT1 NG + GT2 NG + GT3 NG + HRSG1 NG + HRSG2 NG + HRSG3 NG
```

---

## 2.2 Boiler Feed Water (BFW) - M³

### Formula
```
Total BFW = BFW for HRSG + BFW for HP PRDS + BFW for MP PRDS + BFW for LP PRDS + BFW Process + BFW Fixed
```

### Component Formulas

**BFW for HRSG**
```
BFW for HRSG1 = HRSG1 SHP MT × 1.024 M³/MT
BFW for HRSG2 = HRSG2 SHP MT × 1.024 M³/MT
BFW for HRSG3 = HRSG3 SHP MT × 1.024 M³/MT
```

**BFW for HP PRDS**
```
BFW for HP PRDS = HP from PRDS MT × 0.0768 M³/MT
```

**BFW for MP PRDS**
```
BFW for MP PRDS = MP from PRDS MT × 0.09 M³/MT
```

**BFW for LP PRDS**
```
BFW for LP PRDS = LP from PRDS MT × 0.25 M³/MT
```

### Norms Used
- **HRSG BFW**: 1.024 M³ per MT SHP
- **HP PRDS BFW**: 0.0768 M³ per MT HP
- **MP PRDS BFW**: 0.09 M³ per MT MP
- **LP PRDS BFW**: 0.25 M³ per MT LP

---

## 2.3 DM Water (Demineralized Water) - M³

### Formula
```
Total DM Water = DM for BFW + DM Process + DM Fixed
```

### Component Formulas

**DM for BFW**
```
DM for BFW = Total BFW M³ × 0.86 M³/M³
```

### Norms Used
- **BFW DM Water**: 0.86 M³ DM per M³ BFW

---

## 2.4 Cooling Water - KM³

### Cooling Water 1 (CW1)
```
Total CW1 = CW1 Process
```

### Cooling Water 2 (CW2)
```
Total CW2 = CW2 for GT1 + CW2 for GT2 + CW2 for GT3 + CW2 for STG + 
            CW2 for BFW + CW2 for Air + CW2 for Oxygen + CW2 Process
```

### Component Formulas

**CW2 for Power Plants (Fixed per month when operating)**
```
CW2 for GT1 = 108 KM³ (if GT1 operating)
CW2 for GT2 = 108 KM³ (if GT2 operating)
CW2 for GT3 = 108 KM³ (if GT3 operating)
CW2 for STG = 2376 KM³ (if STG operating)
```

**CW2 for Utility Plants (Fixed per month)**
```
CW2 for BFW = 108 KM³
CW2 for Air = 175 KM³
```

**CW2 for Oxygen**
```
CW2 for Oxygen = Oxygen MT × 0.261 KM³/MT
```

### Norms Used
- **GT CW2**: 108 KM³ per month (fixed)
- **STG CW2**: 2376 KM³ per month (fixed)
- **BFW CW2**: 108 KM³ per month (fixed)
- **Air CW2**: 175 KM³ per month (fixed)
- **Oxygen CW2**: 0.261 KM³ per MT

---

## 2.5 Compressed Air - NM³

### Formula
```
Total Air = Air for GT1 + Air for GT2 + Air for GT3 + Air for STG + 
            Air for HRSG1 + Air for HRSG2 + Air for HRSG3 + 
            Air for CW1 + Air for CW2 + Air Process
```

### Component Formulas

**Air for Power Plants (Fixed per month when operating)**
```
Air for GT1 = 30,960 NM³ (if GT1 operating)
Air for GT2 = 30,960 NM³ (if GT2 operating)
Air for GT3 = 30,960 NM³ (if GT3 operating)
Air for STG = 41,040 NM³ (if STG operating)
```

**Air for HRSG (Fixed per month when operating)**
```
Air for HRSG1 = 453,600 NM³ (if HRSG1 operating)
Air for HRSG2 = 453,600 NM³ (if HRSG2 operating)
Air for HRSG3 = 453,600 NM³ (if HRSG3 operating)
```

**Air for Cooling Water Plants (Fixed per month)**
```
Air for CW1 = 1,650 NM³
Air for CW2 = 1,650 NM³
```

### Norms Used
- **GT Air**: 30,960 NM³ per month (fixed)
- **STG Air**: 41,040 NM³ per month (fixed)
- **HRSG Air**: 453,600 NM³ per month (fixed)
- **CW1 Air**: 1,650 NM³ per month (fixed)
- **CW2 Air**: 1,650 NM³ per month (fixed)

---

## 2.6 Raw Water - M³

### Formula
```
Total Raw Water = Water for CW1 + Water for CW2 + Water for DM + 
                  Water for HRSG2 + Water for HRSG3 + Water for Effluent
```

### Component Formulas

**Water for Cooling Water Plants**
```
Water for CW1 = Total CW KM³ × 0.5312 × 11.05 M³/KM³
Water for CW2 = Total CW KM³ × 0.4688 × 11.5 M³/KM³
```
Note: CW1 handles 53.12% of total cooling water, CW2 handles 46.88%

**Water for DM Water Plant**
```
Water for DM = Total DM M³ × 1.05 M³/M³
```

**Water for HRSG**
```
Water for HRSG2 = HRSG2 SHP MT × 0.0027 M³/MT
Water for HRSG3 = HRSG3 SHP MT × 0.0027 M³/MT
```

**Water for Effluent**
```
Water for Effluent = Effluent M³ × 0.0007 M³/M³
```

### Norms Used
- **CW1 Water**: 11.05 M³ per KM³
- **CW2 Water**: 11.5 M³ per KM³
- **DM Water**: 1.05 M³ per M³
- **HRSG Water**: 0.0027 M³ per MT SHP
- **Effluent Water**: 0.0007 M³ per M³

---

## 2.7 LP Steam - MT

### Formula
```
LP for BFW = Total BFW M³ × 0.145 MT/M³
```

### LP Steam Credit from HRSG
```
LP Credit from HRSG2 = HRSG2 SHP MT × 0.0504 MT/MT
LP Credit from HRSG3 = HRSG3 SHP MT × 0.0504 MT/MT
```

### Norms Used
- **BFW LP Steam**: 0.145 MT LP per M³ BFW
- **HRSG LP Credit**: -0.0504 MT LP per MT SHP (negative = credit)

---

## 2.8 Utility Power Consumption - KWH

### Formula
```
Total Utility Power = Power for BFW + Power for DM + Power for CW1 + 
                      Power for CW2 + Power for Air + Power for Oxygen + 
                      Power for Effluent
```

### Component Formulas

**Power for BFW**
```
Power for BFW = Total BFW M³ × 9.5 KWH/M³
```

**Power for DM Water**
```
Power for DM = Total DM M³ × 1.21 KWH/M³
```

**Power for Cooling Water**
```
Power for CW1 = Total CW KM³ × 0.5312 × 245 KWH/KM³
Power for CW2 = Total CW KM³ × 0.4688 × 250 KWH/KM³
```

**Power for Compressed Air**
```
Power for Air = Total Air NM³ × 0.165 KWH/NM³
```

**Power for Oxygen**
```
Power for Oxygen = Oxygen MT × 968.65 KWH/MT
```

**Power for Effluent**
```
Power for Effluent = Effluent M³ × 3.54 KWH/M³
```

### Norms Used
- **BFW Power**: 9.5 KWH per M³
- **DM Power**: 1.21 KWH per M³
- **CW1 Power**: 245 KWH per KM³
- **CW2 Power**: 250 KWH per KM³
- **Air Power**: 0.165 KWH per NM³
- **Oxygen Power**: 968.65 KWH per MT
- **Effluent Power**: 3.54 KWH per M³

---

## 2.9 Return Condensate - M³

### Formula
```
Total Condensate = Condensate from STG + Condensate from DM
```

### Component Formulas

**Condensate from STG**
```
Condensate from STG = STG KWH × 0.0029 M³/KWH
```

**Condensate from DM**
```
Condensate from DM = Total DM M³ × 0.203 M³/M³
```

### Norms Used
- **STG Condensate**: 0.0029 M³ per KWH
- **DM Condensate**: 0.203 M³ per M³

---

# 3. Steam Balance Calculations

## 3.1 LP Steam Balance

### Total LP Demand
```
Total LP = LP Process + LP Fixed + LP from UFU
```

Where:
```
LP from UFU = BFW UFU M³ × 0.145 MT/M³
```

### LP Supply Distribution (STG Load-Based)
```
LP from STG = STG LP Extraction Rate (TPH) × STG Operating Hours
LP from PRDS = Total LP - LP from STG
```

### Upstream Requirements

**For LP from STG**
```
SHP for STG LP = LP from STG × 0.48 MT SHP/MT LP
```

**For LP from PRDS**
```
MP for LP PRDS = LP from PRDS × 0.75 MT MP/MT LP
BFW for LP PRDS = LP from PRDS × 0.25 M³/MT LP
```

---

## 3.2 MP Steam Balance

### Total MP Demand
```
Total MP = MP Process + MP Fixed + MP for LP PRDS
```

### MP Supply Distribution (STG Load-Based)
```
MP from STG = STG MP Extraction Rate (TPH) × STG Operating Hours
MP from PRDS = Total MP - MP from STG
```

### Upstream Requirements

**For MP from STG**
```
SHP for STG MP = MP from STG × 0.69 MT SHP/MT MP
```

**For MP from PRDS**
```
SHP for MP PRDS = MP from PRDS × 0.91 MT SHP/MT MP
BFW for MP PRDS = MP from PRDS × 0.09 M³/MT MP
```

**Total SHP from MP Chain**
```
SHP from MP Chain = SHP for STG MP + SHP for MP PRDS
```

---

## 3.3 HP Steam Balance

### Total HP Demand
```
Total HP = HP Process + HP Fixed
```

### HP Supply (100% from PRDS)
```
HP from PRDS = Total HP × 1.0
```

### Upstream Requirements
```
SHP for HP PRDS = HP from PRDS × 0.9232 MT SHP/MT HP
BFW for HP PRDS = HP from PRDS × 0.0768 M³/MT HP
```

---

## 3.4 SHP Steam Balance

### Total SHP Demand
```
Total SHP = SHP Process + SHP Fixed + SHP from Headers + SHP for STG Power
```

Where:
```
SHP from Headers = SHP for STG LP + SHP from MP Chain + SHP for HP PRDS
```

### SHP for STG Power Generation
```
SHP for STG Power = STG Gross KWH × 0.0036 MT/KWH
```

---

# 4. Power Generation Requirements

## 4.1 GT Auxiliary Power

### Formula
```
GT Aux Power = GT Gross KWH × 0.014 KWH/KWH
```

### Norms Used
- **GT1 Aux Power**: 0.014 KWH per KWH (1.4%)
- **GT2 Aux Power**: 0.014 KWH per KWH (1.4%)
- **GT3 Aux Power**: 0.014 KWH per KWH (1.4%)

---

## 4.2 STG Power Requirements

### STG Auxiliary Power
```
STG Aux Power = STG Gross KWH × 0.002 KWH/KWH
```

### STG SHP Steam Requirement
```
STG SHP Required = STG Gross KWH × 0.0036 MT/KWH
```

### Norms Used
- **STG Aux Power**: 0.002 KWH per KWH (0.2%)
- **STG SHP Steam**: 0.0036 MT per KWH

---

# 5. Summary of Reverse-Calculated Norms

| Norm | Type | Formula | Source |
|------|------|---------|--------|
| **GT1 NG Norm** | Reverse Calculated | `(Heat Rate - FreeSteamFactor × 760.87) / 252,164` | HeatRateLookup table |
| **GT2 NG Norm** | Reverse Calculated | `(Heat Rate - FreeSteamFactor × 760.87) / 252,164` | HeatRateLookup table |
| **GT3 NG Norm** | Reverse Calculated | `(Heat Rate - FreeSteamFactor × 760.87) / 252,164` | HeatRateLookup table |
| **HRSG1 NG Norm** | Reverse Calculated | `FreeSteamFactor × 760.87 × 3.96567 / 1,000,000` | HeatRateLookup table |
| **HRSG2 NG Norm** | Reverse Calculated | `FreeSteamFactor × 760.87 × 3.96567 / 1,000,000` | HeatRateLookup table |
| **HRSG3 NG Norm** | Reverse Calculated | `FreeSteamFactor × 760.87 × 3.96567 / 1,000,000` | HeatRateLookup table |
| **LP STG Ratio** | Reverse Calculated | `LP from STG / Total LP` | STGExtractionLookup table |
| **LP PRDS Ratio** | Reverse Calculated | `LP from PRDS / Total LP` | STGExtractionLookup table |
| **MP STG Ratio** | Reverse Calculated | `MP from STG / Total MP` | STGExtractionLookup table |
| **MP PRDS Ratio** | Reverse Calculated | `MP from PRDS / Total MP` | STGExtractionLookup table |

---

# 6. Key Constants and Conversion Factors

| Constant | Value | Description |
|----------|-------|-------------|
| **KCAL_TO_BTU** | 3.96567 | 1 KCAL = 3.96567 BTU |
| **BTU_TO_MMBTU** | 1,000,000 | 1 MMBTU = 1,000,000 BTU |
| **SHP_ENTHALPY** | 810 | SHP steam enthalpy (KCAL/kg) |
| **HRSG_INLET_ENTHALPY** | 110 | HRSG inlet enthalpy (KCAL/kg) |
| **HRSG_EFFICIENCY** | 0.92 | HRSG efficiency |
| **FREE_STEAM_ENERGY** | 760.87 | (810-110)/0.92 KCAL/kg |
| **CW1_RATIO** | 0.5312 | 53.12% of total CW from CW1 |
| **CW2_RATIO** | 0.4688 | 46.88% of total CW from CW2 |

---

# 7. Code References

## Main Files
- **`run_full_year.py`**: Entry point for full year budget calculation
- **`services/budget_service.py`**: Main budget calculation orchestration
- **`services/iteration_service.py`**: USD iteration logic with reverse calculations
- **`services/utility_service.py`**: All utility consumption calculations
- **`services/steam_service.py`**: Steam balance calculations
- **`services/power_service.py`**: Power dispatch and generation
- **`services/norms_service.py`**: Norms management and database integration

## Key Functions
- **`calculate_gt_ng_mmbtu()`**: GT Natural Gas reverse calculation
- **`calculate_utilities_from_dispatch()`**: All utility calculations
- **`calculate_lp_balance_stg_based()`**: LP steam balance with STG extraction
- **`calculate_mp_balance_stg_based()`**: MP steam balance with STG extraction
- **`usd_iterate()`**: Main iteration loop with reverse norm calculations

---

**Document Version**: 1.0  
**Last Updated**: January 19, 2026  
**Model Location**: `apps/python/PPPython-script/`
