# GT Natural Gas (MMBTU) Calculation Formula

## Final Formula

```
NET GT MMBTU = GROSS MMBTU - FREE STEAM MMBTU
```

---

## Step 1: GROSS MMBTU

```
Gross MMBTU = KWH × Heat Rate × 3.96567 / 1,000,000
```

Where:
- **KWH** = GT generation in KWH
- **Heat Rate** = From `HeatRateLookup` table (unit: **KCAL/KWH**)
- **3.96567** = KCAL to BTU conversion
- **1,000,000** = BTU to MMBTU (1 MMBTU = 1 million BTU)

---

## Step 2: FREE STEAM MMBTU

```
Free Steam MMBTU = KWH × FreeSteamFactor × 760.87 × 3.96567 / 1,000,000
```

Where:
- **FreeSteamFactor** = From `HeatRateLookup` table (e.g., 1.97)
- **760.87** = Free steam energy in KCAL/kg = (810 - 110) / 0.92
  - 810 = SHP steam enthalpy (KCAL/kg)
  - 110 = HRSG inlet enthalpy (KCAL/kg)
  - 0.92 = HRSG efficiency

---

## Simplified Combined Formula

```
NET GT MMBTU = KWH × 3.96567 / 1,000,000 × (Heat Rate - FreeSteamFactor × 760.87)
```

Or even simpler:
```
NET GT MMBTU = KWH × (Heat Rate - FreeSteamFactor × 760.87) / 252,164
```

---

## Example: GT2 Calculation

| Input | Value |
|-------|-------|
| KWH | 8,097,740 |
| Heat Rate | 4,084.94 KCAL/KWH |
| Free Steam Factor | 1.97 |

**Calculation:**
```
Gross MMBTU    = 8,097,740 × 4,084.94 × 3.96567 / 1,000,000 = 131,180
Free Steam     = 8,097,740 × 1.97 × 760.87 × 3.96567 / 1,000,000 = 48,135
NET GT MMBTU   = 131,180 - 48,135 = 83,045 MMBTU
```

| Result | Calculated | Reference | Error |
|--------|------------|-----------|-------|
| NET MMBTU | 83,045 | 82,162 | +1.07% |
| Norm | 0.01026 | 0.01015 | +1.07% |

---

## Key Constants

| Constant | Value | Description |
|----------|-------|-------------|
| KCAL_TO_BTU | 3.96567 | 1 KCAL = 3.96567 BTU |
| BTU_TO_MMBTU | 1,000,000 | 1 MMBTU = 1,000,000 BTU |
| SHP_ENTHALPY | 810 | SHP steam enthalpy (KCAL/kg) |
| HRSG_INLET_ENTHALPY | 110 | HRSG inlet enthalpy (KCAL/kg) |
| HRSG_EFFICIENCY | 0.92 | HRSG efficiency |
| FREE_STEAM_ENERGY | 760.87 | (810-110)/0.92 KCAL/kg |

---

## Python Implementation

```python
def calculate_gt_ng_mmbtu(kwh: float, heat_rate: float, free_steam_factor: float) -> dict:
    """
    Calculate GT Natural Gas MMBTU.
    
    Args:
        kwh: GT generation in KWH
        heat_rate: Heat rate from lookup (KCAL/KWH)
        free_steam_factor: Free steam factor from lookup
    
    Returns:
        dict with gross_mmbtu, free_steam_mmbtu, net_mmbtu, norm
    """
    KCAL_TO_BTU = 3.96567
    BTU_TO_MMBTU = 1_000_000
    FREE_STEAM_ENERGY = 760.87  # KCAL/kg
    
    # Gross MMBTU
    gross_mmbtu = kwh * heat_rate * KCAL_TO_BTU / BTU_TO_MMBTU
    
    # Free Steam MMBTU
    free_steam_kg = kwh * free_steam_factor
    free_steam_mmbtu = free_steam_kg * FREE_STEAM_ENERGY * KCAL_TO_BTU / BTU_TO_MMBTU
    
    # Net MMBTU
    net_mmbtu = gross_mmbtu - free_steam_mmbtu
    
    # Norm
    norm = net_mmbtu / kwh if kwh > 0 else 0
    
    return {
        'gross_mmbtu': gross_mmbtu,
        'free_steam_mmbtu': free_steam_mmbtu,
        'net_mmbtu': net_mmbtu,
        'norm': norm
    }
```
