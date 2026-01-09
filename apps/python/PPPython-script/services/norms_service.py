"""
Norms Service
=============
Service to fetch, store, and compare norms from database with hardcoded values.
Provides dynamic norms loading based on month/year input.
"""

from database.norms_queries import fetch_all_norms_for_month


# ============================================================
# GLOBAL NORMS HOLDER - Stores dynamically loaded norms
# ============================================================
class DynamicNorms:
    """
    Holds dynamically loaded norms from database.
    Access norms like: NORMS.GT2_NATURAL_GAS or NORMS.get('GT2_NATURAL_GAS')
    """
    
    def __init__(self):
        self._norms = {}
        self._loaded = False
        self._month = None
        self._year = None
    
    def set(self, key: str, value: float):
        """Set a norm value."""
        self._norms[key] = value
    
    def get(self, key: str, default: float = None):
        """Get a norm value with optional default."""
        return self._norms.get(key, default)
    
    def __getattr__(self, name: str):
        """Allow attribute-style access: NORMS.GT2_NATURAL_GAS"""
        if name.startswith('_'):
            return object.__getattribute__(self, name)
        return self._norms.get(name)
    
    def is_loaded(self) -> bool:
        return self._loaded
    
    def get_period(self) -> tuple:
        return (self._month, self._year)
    
    def clear(self):
        self._norms = {}
        self._loaded = False
        self._month = None
        self._year = None


# Global instance
NORMS = DynamicNorms()


# ============================================================
# NORM ACCESSOR FUNCTIONS WITH FALLBACK DEFAULTS
# ============================================================
# These functions return the DB norm if loaded, else fallback to hardcoded default

# Default hardcoded values (used as fallback if DB norm is not available)
_DEFAULTS = {
    # Power Plants - Natural Gas
    'GT2_NATURAL_GAS': 0.0101,
    'GT3_NATURAL_GAS': 0.0095,
    
    # Power Plants - Auxiliary Power
    'GT1_AUX_POWER': 0.014,
    'GT2_AUX_POWER': 0.014,
    'GT3_AUX_POWER': 0.014,
    'STG_AUX_POWER': 0.002,
    
    # STG Norms
    'STG_SHP_PER_KWH': 0.0036,
    'STG_CONDENSATE': 0.0029,
    
    # BFW Norms
    'BFW_DM_WATER': 0.86,
    'BFW_LP_STEAM': 0.145,
    'BFW_POWER': 9.5,
    
    # Compressed Air
    'AIR_POWER': 0.165,
    
    # Cooling Water
    'CW1_POWER': 245.0,
    'CW2_POWER': 250.0,
    'CW2_WATER': 11.5,
    
    # DM Water
    'DM_POWER': 1.21,
    'DM_AIR': 0.077,
    'DM_CONDENSATE': 0.203,
    'DM_WATER': 1.05,
    
    # Effluent
    'EFFLUENT_POWER': 3.54,
    'EFFLUENT_WATER': 0.0007,
    
    # Oxygen
    'OXYGEN_POWER': 968.65,
    'OXYGEN_CW2': 0.261,
    'OXYGEN_NITROGEN': 2448.4,
    
    # HRSG
    'HRSG2_BFW': 1.024,
    'HRSG3_BFW': 1.024,
    'HRSG2_LP_CREDIT': -0.0504,
    'HRSG3_LP_CREDIT': -0.0504,
    'HRSG2_NATURAL_GAS': 2.8064,
    'HRSG3_NATURAL_GAS': 2.8168,
    
    # PRDS
    'HP_PRDS_BFW': 0.0768,
    'HP_PRDS_SHP': 0.9232,
    'LP_PRDS_BFW': 0.25,
    'LP_PRDS_MP': 0.75,
    'MP_PRDS_BFW': 0.09,
    'MP_PRDS_SHP': 0.91,
    
    # STG Steam Extraction
    'STG_LP_SHP': 0.48,
    'STG_MP_SHP': 0.69,
}


def get_norm(key: str, default: float = None) -> float:
    """
    Get a norm value from loaded DB norms, with fallback to default.
    
    Args:
        key: Norm key (e.g., 'GT2_NATURAL_GAS')
        default: Override default value (optional)
    
    Returns:
        Norm value from DB if loaded and not None, else fallback default
    """
    # First try to get from loaded norms
    if NORMS.is_loaded():
        db_value = NORMS.get(key)
        if db_value is not None:
            return db_value
    
    # Fallback to provided default or hardcoded default
    if default is not None:
        return default
    return _DEFAULTS.get(key, 0.0)


class NormsService:
    """
    Service class to manage norms data from database.
    Fetches norms for a specific month/year and provides comparison with hardcoded values.
    """
    
    def __init__(self):
        self.db_norms = {}  # Stores norms fetched from database
        self.month = None
        self.year = None
        self._raw_data = []  # Raw data from database
    
    def load_norms(self, month: int, year: int) -> dict:
        """
        Load all norms from database for a specific month and year.
        
        Args:
            month: Month number (1-12)
            year: Year (e.g., 2025, 2026)
        
        Returns:
            Dictionary of norms organized by plant and material
        """
        self.month = month
        self.year = year
        self._raw_data = fetch_all_norms_for_month(month, year)
        
        # Organize norms by plant -> utility -> material
        self.db_norms = {}
        
        for row in self._raw_data:
            plant = row.get('PlantName', '')
            utility = row.get('UtilityName', '')
            material = row.get('MaterialName', '')
            norm_value = row.get('NormValue')
            quantity = row.get('Quantity')
            generation = row.get('Generation')
            
            if plant not in self.db_norms:
                self.db_norms[plant] = {}
            
            if utility not in self.db_norms[plant]:
                self.db_norms[plant][utility] = {}
            
            self.db_norms[plant][utility][material] = {
                'norm_value': float(norm_value) if norm_value is not None else None,
                'quantity': float(quantity) if quantity is not None else None,
                'generation': float(generation) if generation is not None else None,
                'uom': row.get('UtilityUOM'),
                'issuing_uom': row.get('IssuingUOM'),
                'account': row.get('AccountName'),
            }
        
        return self.db_norms
    
    def get_norm(self, plant: str, utility: str, material: str) -> dict:
        """
        Get a specific norm value.
        
        Args:
            plant: Plant name (e.g., 'NMD - Power Plant 2')
            utility: Utility name (e.g., 'POWERGEN')
            material: Material name (e.g., 'NATURAL GAS')
        
        Returns:
            Dictionary with norm_value, quantity, generation, etc.
        """
        return self.db_norms.get(plant, {}).get(utility, {}).get(material, {})
    
    def get_norm_value(self, plant: str, utility: str, material: str) -> float:
        """
        Get just the norm value (rate) for a specific norm.
        
        Returns:
            Norm value or None if not found
        """
        norm = self.get_norm(plant, utility, material)
        return norm.get('norm_value') if norm else None
    
    def get_quantity(self, plant: str, utility: str, material: str) -> float:
        """
        Get the quantity for a specific norm.
        
        Returns:
            Quantity or None if not found
        """
        norm = self.get_norm(plant, utility, material)
        return norm.get('quantity') if norm else None
    
    def print_all_norms(self):
        """Print all loaded norms in a readable format."""
        print(f"\n{'='*120}")
        print(f"DATABASE NORMS FOR {self.month}/{self.year}")
        print(f"{'='*120}")
        
        for plant, utilities in sorted(self.db_norms.items()):
            print(f"\n{plant}")
            print("-" * 100)
            for utility, materials in utilities.items():
                for material, data in materials.items():
                    norm_val = data.get('norm_value')
                    qty = data.get('quantity')
                    norm_str = f"{norm_val:.6f}" if norm_val is not None else "NULL"
                    qty_str = f"{qty:,.2f}" if qty is not None else "NULL"
                    print(f"  {utility:<20} | {material:<35} | Norm: {norm_str:>12} | Qty: {qty_str:>15}")
    
    def compare_with_hardcoded(self) -> dict:
        """
        Compare database norms with hardcoded values in the code.
        
        Returns:
            Dictionary with comparison results
        """
        # Hardcoded norms from utility_service.py and iteration_service.py
        HARDCODED_NORMS = {
            # Power Plants - Natural Gas
            ('NMD - Power Plant 2', 'POWERGEN', 'NATURAL GAS'): {'code_norm': 0.0101, 'description': 'GT2 NG Norm'},
            ('NMD - Power Plant 3', 'POWERGEN', 'NATURAL GAS'): {'code_norm': 0.0095, 'description': 'GT3 NG Norm'},
            
            # Power Plants - Auxiliary Power
            ('NMD - Power Plant 1', 'POWERGEN', 'Power_Dis'): {'code_norm': 0.0140, 'description': 'GT1 Aux Power'},
            ('NMD - Power Plant 2', 'POWERGEN', 'Power_Dis'): {'code_norm': 0.0140, 'description': 'GT2 Aux Power'},
            ('NMD - Power Plant 3', 'POWERGEN', 'Power_Dis'): {'code_norm': 0.0140, 'description': 'GT3 Aux Power'},
            
            # STG Power Plant
            ('NMD - STG Power Plant', 'POWERGEN', 'Power_Dis'): {'code_norm': 0.0020, 'description': 'STG Aux Power'},
            ('NMD - STG Power Plant', 'POWERGEN', 'SHP Steam_Dis'): {'code_norm': 0.0036, 'description': 'STG SHP Norm'},
            ('NMD - STG Power Plant', 'POWERGEN', 'Ret steam condensate'): {'code_norm': 0.0029, 'description': 'STG Condensate'},
            
            # BFW
            ('NMD - Utility Plant', 'Boiler Feed Water', 'D M Water'): {'code_norm': 0.8600, 'description': 'BFW DM Water'},
            ('NMD - Utility Plant', 'Boiler Feed Water', 'LP Steam_Dis'): {'code_norm': 0.1450, 'description': 'BFW LP Steam'},
            ('NMD - Utility Plant', 'Boiler Feed Water', 'Power_Dis'): {'code_norm': 9.5000, 'description': 'BFW Power'},
            ('NMD - Utility Plant', 'Boiler Feed Water', 'CHEM CYCLO HEXY'): {'code_norm': 0.0001, 'description': 'BFW Cyclohexy'},
            ('NMD - Utility Plant', 'Boiler Feed Water', 'CHEM MORPHOLENE'): {'code_norm': 0.000002, 'description': 'BFW Morpholene'},
            ('NMD - Utility Plant', 'Boiler Feed Water', 'KEM WATREAT B 70M'): {'code_norm': 0.0005700, 'description': 'BFW Watreat'},
            
            # Compressed Air
            ('NMD - Utility Plant', 'COMPRESSED AIR', 'Power_Dis'): {'code_norm': 0.1650, 'description': 'Air Power'},
            
            # Cooling Water 1
            ('NMD - Utility Plant', 'Cooling Water 1', 'Power_Dis'): {'code_norm': 245.0000, 'description': 'CW1 Power'},
            ('NMD - Utility Plant', 'Cooling Water 1', 'SULPHURIC ACID'): {'code_norm': 0.0001580, 'description': 'CW1 Sulphuric Acid'},
            
            # Cooling Water 2
            ('NMD - Utility Plant', 'Cooling Water 2', 'Power_Dis'): {'code_norm': 250.0000, 'description': 'CW2 Power'},
            ('NMD - Utility Plant', 'Cooling Water 2', 'Water'): {'code_norm': 11.5000, 'description': 'CW2 Water'},
            ('NMD - Utility Plant', 'Cooling Water 2', 'SULPHURIC ACID'): {'code_norm': 0.0001580, 'description': 'CW2 Sulphuric Acid'},
            
            # DM Water
            ('NMD - Utility Plant', 'D M Water', 'Power_Dis'): {'code_norm': 1.2100, 'description': 'DM Power'},
            ('NMD - Utility Plant', 'D M Water', 'COMPRESSED AIR'): {'code_norm': 0.0770, 'description': 'DM Air'},
            ('NMD - Utility Plant', 'D M Water', 'Ret steam condensate'): {'code_norm': 0.2030, 'description': 'DM Condensate'},
            ('NMD - Utility Plant', 'D M Water', 'Water'): {'code_norm': 1.0500, 'description': 'DM Water'},
            ('NMD - Utility Plant', 'D M Water', 'CAUSTIC SODA LYE – GRADE 1'): {'code_norm': 0.0002260, 'description': 'DM Caustic'},
            ('NMD - Utility Plant', 'D M Water', 'CHEM ALUM.SULFATE, AL2(SO4)3,18H2O'): {'code_norm': 0.0007570, 'description': 'DM Alum Sulfate'},
            ('NMD - Utility Plant', 'D M Water', 'CHEM  SODIUM SULPHITE;PN:MIS 19OX'): {'code_norm': 0.0007010, 'description': 'DM Sodium Sulphite'},
            ('NMD - Utility Plant', 'D M Water', 'POLYELECTROLYTE'): {'code_norm': 0.0005780, 'description': 'DM Polyelectrolyte'},
            ('NMD - Utility Plant', 'D M Water', 'SODIUM CHLORIDE IS 797 GRADE1'): {'code_norm': 0.0000100, 'description': 'DM Sodium Chloride'},
            ('NMD - Utility Plant', 'D M Water', 'HYDRO CHLORIC ACID (30%) -VIRGIN'): {'code_norm': 0.0003800, 'description': 'DM HCl'},
            
            # Effluent
            ('NMD - Utility Plant', 'Effluent Treated', 'Power_Dis'): {'code_norm': 3.5400, 'description': 'Effluent Power'},
            ('NMD - Utility Plant', 'Effluent Treated', 'Water'): {'code_norm': 0.0007, 'description': 'Effluent Water'},
            
            # HP Steam PRDS
            ('NMD - Utility Plant', 'HP Steam PRDS', 'Boiler Feed Water'): {'code_norm': 0.0768, 'description': 'HP PRDS BFW'},
            ('NMD - Utility Plant', 'HP Steam PRDS', 'SHP Steam_Dis'): {'code_norm': 0.9232, 'description': 'HP PRDS SHP'},
            
            # HRSG2
            ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'Boiler Feed Water'): {'code_norm': 1.0240, 'description': 'HRSG2 BFW'},
            ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'LP Steam_Dis'): {'code_norm': -0.0504, 'description': 'HRSG2 LP Credit'},
            ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'NATURAL GAS'): {'code_norm': 2.8064, 'description': 'HRSG2 NG'},
            ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'CHEM TRISODIUM PHOSPHATE'): {'code_norm': 0.0009, 'description': 'HRSG2 Trisodium'},
            ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'FURNACE OIL ( MEDIUM VISCOSITY GRADE )'): {'code_norm': 0.0001, 'description': 'HRSG2 Furnace Oil'},
            ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'Water'): {'code_norm': 0.0027, 'description': 'HRSG2 Water'},
            
            # HRSG3
            ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'Boiler Feed Water'): {'code_norm': 1.0240, 'description': 'HRSG3 BFW'},
            ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'LP Steam_Dis'): {'code_norm': -0.0504, 'description': 'HRSG3 LP Credit'},
            ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'NATURAL GAS'): {'code_norm': 2.8168, 'description': 'HRSG3 NG'},
            ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'CHEM TRISODIUM PHOSPHATE'): {'code_norm': 0.0009, 'description': 'HRSG3 Trisodium'},
            ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'FURNACE OIL ( MEDIUM VISCOSITY GRADE )'): {'code_norm': 0.0001, 'description': 'HRSG3 Furnace Oil'},
            ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'Water'): {'code_norm': 0.0027, 'description': 'HRSG3 Water'},
            
            # LP Steam PRDS
            ('NMD - Utility Plant', 'LP Steam PRDS', 'Boiler Feed Water'): {'code_norm': 0.2500, 'description': 'LP PRDS BFW'},
            ('NMD - Utility Plant', 'LP Steam PRDS', 'MP Steam_Dis'): {'code_norm': 0.7500, 'description': 'LP PRDS MP'},
            
            # MP Steam PRDS
            ('NMD - Utility Plant', 'MP Steam PRDS SHP', 'Boiler Feed Water'): {'code_norm': 0.0900, 'description': 'MP PRDS BFW'},
            ('NMD - Utility Plant', 'MP Steam PRDS SHP', 'SHP Steam_Dis'): {'code_norm': 0.9100, 'description': 'MP PRDS SHP'},
            
            # Oxygen
            ('NMD - Utility Plant', 'Oxygen', 'Power_Dis'): {'code_norm': 968.6500, 'description': 'Oxygen Power'},
            ('NMD - Utility Plant', 'Oxygen', 'Cooling Water 2'): {'code_norm': 0.2610, 'description': 'Oxygen CW2'},
            ('NMD - Utility Plant', 'Oxygen', 'Nitrogen Gas'): {'code_norm': 2448.4000, 'description': 'Oxygen Nitrogen'},
            
            # STG LP/MP Steam
            ('NMD - Utility Plant', 'STG1_LP STEAM', 'SHP Steam_Dis'): {'code_norm': 0.4800, 'description': 'STG LP SHP'},
            ('NMD - Utility Plant', 'STG1_MP STEAM', 'SHP Steam_Dis'): {'code_norm': 0.6900, 'description': 'STG MP SHP'},
        }
        
        comparison_results = {
            'matched': [],
            'mismatched': [],
            'missing_in_db': [],
            'extra_in_db': [],
        }
        
        # Compare hardcoded with database
        for (plant, utility, material), code_data in HARDCODED_NORMS.items():
            code_norm = code_data['code_norm']
            description = code_data['description']
            
            db_norm_data = self.get_norm(plant, utility, material)
            db_norm = db_norm_data.get('norm_value') if db_norm_data else None
            db_qty = db_norm_data.get('quantity') if db_norm_data else None
            
            result = {
                'plant': plant,
                'utility': utility,
                'material': material,
                'description': description,
                'code_norm': code_norm,
                'db_norm': db_norm,
                'db_quantity': db_qty,
            }
            
            if db_norm is None:
                result['status'] = 'MISSING_IN_DB'
                comparison_results['missing_in_db'].append(result)
            elif abs(code_norm - db_norm) < 0.0000001:
                result['status'] = 'MATCH'
                result['difference'] = 0
                result['pct_diff'] = 0
                comparison_results['matched'].append(result)
            else:
                result['status'] = 'MISMATCH'
                result['difference'] = db_norm - code_norm
                result['pct_diff'] = ((db_norm - code_norm) / code_norm * 100) if code_norm != 0 else float('inf')
                comparison_results['mismatched'].append(result)
        
        return comparison_results
    
    def print_comparison_report(self):
        """Print a detailed comparison report."""
        results = self.compare_with_hardcoded()
        
        print(f"\n{'='*140}")
        print(f"NORMS COMPARISON REPORT - Database ({self.month}/{self.year}) vs Hardcoded Values")
        print(f"{'='*140}")
        
        # Summary
        total = len(results['matched']) + len(results['mismatched']) + len(results['missing_in_db'])
        print(f"\nSUMMARY:")
        print(f"  Total Norms Compared: {total}")
        print(f"  ✅ Matched:           {len(results['matched'])}")
        print(f"  ⚠️  Mismatched:        {len(results['mismatched'])}")
        print(f"  ❌ Missing in DB:     {len(results['missing_in_db'])}")
        
        # Matched
        if results['matched']:
            print(f"\n{'='*140}")
            print("✅ MATCHED NORMS")
            print(f"{'='*140}")
            print(f"{'Description':<25} {'Plant':<25} {'Utility':<20} {'Material':<30} {'Code':>12} {'DB':>12}")
            print("-" * 140)
            for r in results['matched']:
                print(f"{r['description']:<25} {r['plant']:<25} {r['utility']:<20} {r['material']:<30} {r['code_norm']:>12.6f} {r['db_norm']:>12.6f}")
        
        # Mismatched
        if results['mismatched']:
            print(f"\n{'='*140}")
            print("⚠️  MISMATCHED NORMS")
            print(f"{'='*140}")
            print(f"{'Description':<25} {'Plant':<25} {'Material':<30} {'Code':>12} {'DB':>12} {'Diff':>12} {'%Diff':>10}")
            print("-" * 140)
            for r in results['mismatched']:
                pct_str = f"{r['pct_diff']:>+.2f}%" if r['pct_diff'] != float('inf') else "INF"
                print(f"{r['description']:<25} {r['plant']:<25} {r['material']:<30} {r['code_norm']:>12.6f} {r['db_norm']:>12.6f} {r['difference']:>+12.6f} {pct_str:>10}")
        
        # Missing in DB
        if results['missing_in_db']:
            print(f"\n{'='*140}")
            print("❌ MISSING IN DATABASE (Norm value is NULL)")
            print(f"{'='*140}")
            print(f"{'Description':<25} {'Plant':<25} {'Utility':<20} {'Material':<30} {'Code':>12} {'DB Qty':>15}")
            print("-" * 140)
            for r in results['missing_in_db']:
                qty_str = f"{r['db_quantity']:,.2f}" if r['db_quantity'] is not None else "NULL"
                print(f"{r['description']:<25} {r['plant']:<25} {r['utility']:<20} {r['material']:<30} {r['code_norm']:>12.6f} {qty_str:>15}")
        
        print(f"\n{'='*140}")
        print("END OF COMPARISON REPORT")
        print(f"{'='*140}\n")
        
        return results


# Module-level instance for easy access
_norms_service = None


def get_norms_service() -> NormsService:
    """Get the singleton norms service instance."""
    global _norms_service
    if _norms_service is None:
        _norms_service = NormsService()
    return _norms_service


def load_and_compare_norms(month: int, year: int) -> dict:
    """
    Convenience function to load norms and print comparison report.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025, 2026)
    
    Returns:
        Comparison results dictionary
    """
    service = get_norms_service()
    service.load_norms(month, year)
    return service.print_comparison_report()


def load_norms_for_calculation(month: int, year: int, use_hardcoded: bool = True) -> DynamicNorms:
    """
    Load norms from database into the global NORMS object for use in calculations.
    This is the main function to call at the start of budget calculation.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025, 2026)
        use_hardcoded: If True, use hardcoded defaults instead of DB values (default: True)
                       Set to False once DB data is corrected.
    
    Returns:
        The global NORMS object with loaded values
    """
    global NORMS
    
    # Clear previous norms
    NORMS.clear()
    
    # TEMPORARY: Use hardcoded defaults until DB data is fixed
    if use_hardcoded:
        print(f"\n{'='*70}")
        print(f"USING HARDCODED NORMS (DB data needs correction)")
        print(f"{'='*70}")
        print(f"Period: {month}/{year}")
        
        # Load all defaults into NORMS
        for key, value in _DEFAULTS.items():
            NORMS.set(key, value)
        
        NORMS._loaded = True
        NORMS._month = month
        NORMS._year = year
        
        print(f"Norms loaded: {len(_DEFAULTS)} (hardcoded defaults)")
        print(f"{'='*70}\n")
        return NORMS
    
    # Load from database
    service = get_norms_service()
    service.load_norms(month, year)
    
    # Map database norms to standardized keys
    # Format: NORMS.PLANT_UTILITY_MATERIAL or simplified keys
    
    NORM_MAPPING = {
        # Power Plants - Natural Gas (MMBTU per KWH)
        ('NMD - Power Plant 2', 'POWERGEN', 'NATURAL GAS'): 'GT2_NATURAL_GAS',
        ('NMD - Power Plant 3', 'POWERGEN', 'NATURAL GAS'): 'GT3_NATURAL_GAS',
        
        # Power Plants - Auxiliary Power (KWH per KWH)
        ('NMD - Power Plant 1', 'POWERGEN', 'Power_Dis'): 'GT1_AUX_POWER',
        ('NMD - Power Plant 2', 'POWERGEN', 'Power_Dis'): 'GT2_AUX_POWER',
        ('NMD - Power Plant 3', 'POWERGEN', 'Power_Dis'): 'GT3_AUX_POWER',
        ('NMD - STG Power Plant', 'POWERGEN', 'Power_Dis'): 'STG_AUX_POWER',
        
        # STG Norms
        ('NMD - STG Power Plant', 'POWERGEN', 'SHP Steam_Dis'): 'STG_SHP_PER_KWH',
        ('NMD - STG Power Plant', 'POWERGEN', 'Ret steam condensate'): 'STG_CONDENSATE',
        
        # BFW Norms (per M3)
        ('NMD - Utility Plant', 'Boiler Feed Water', 'D M Water'): 'BFW_DM_WATER',
        ('NMD - Utility Plant', 'Boiler Feed Water', 'LP Steam_Dis'): 'BFW_LP_STEAM',
        ('NMD - Utility Plant', 'Boiler Feed Water', 'Power_Dis'): 'BFW_POWER',
        ('NMD - Utility Plant', 'Boiler Feed Water', 'CHEM CYCLO HEXY'): 'BFW_CYCLOHEXY',
        ('NMD - Utility Plant', 'Boiler Feed Water', 'CHEM MORPHOLENE'): 'BFW_MORPHOLENE',
        ('NMD - Utility Plant', 'Boiler Feed Water', 'KEM WATREAT B 70M'): 'BFW_WATREAT',
        
        # Compressed Air Norms (per NM3)
        ('NMD - Utility Plant', 'COMPRESSED AIR', 'Power_Dis'): 'AIR_POWER',
        
        # Cooling Water 1 Norms (per KM3)
        ('NMD - Utility Plant', 'Cooling Water 1', 'Power_Dis'): 'CW1_POWER',
        ('NMD - Utility Plant', 'Cooling Water 1', 'SULPHURIC ACID'): 'CW1_SULPHURIC_ACID',
        
        # Cooling Water 2 Norms (per KM3)
        ('NMD - Utility Plant', 'Cooling Water 2', 'Power_Dis'): 'CW2_POWER',
        ('NMD - Utility Plant', 'Cooling Water 2', 'Water'): 'CW2_WATER',
        ('NMD - Utility Plant', 'Cooling Water 2', 'SULPHURIC ACID'): 'CW2_SULPHURIC_ACID',
        
        # DM Water Norms (per M3)
        ('NMD - Utility Plant', 'D M Water', 'Power_Dis'): 'DM_POWER',
        ('NMD - Utility Plant', 'D M Water', 'COMPRESSED AIR'): 'DM_AIR',
        ('NMD - Utility Plant', 'D M Water', 'Ret steam condensate'): 'DM_CONDENSATE',
        ('NMD - Utility Plant', 'D M Water', 'Water'): 'DM_WATER',
        ('NMD - Utility Plant', 'D M Water', 'CAUSTIC SODA LYE – GRADE 1'): 'DM_CAUSTIC',
        ('NMD - Utility Plant', 'D M Water', 'CHEM ALUM.SULFATE, AL2(SO4)3,18H2O'): 'DM_ALUM_SULFATE',
        ('NMD - Utility Plant', 'D M Water', 'CHEM  SODIUM SULPHITE;PN:MIS 19OX'): 'DM_SODIUM_SULPHITE',
        ('NMD - Utility Plant', 'D M Water', 'POLYELECTROLYTE'): 'DM_POLYELECTROLYTE',
        ('NMD - Utility Plant', 'D M Water', 'SODIUM CHLORIDE IS 797 GRADE1'): 'DM_SODIUM_CHLORIDE',
        ('NMD - Utility Plant', 'D M Water', 'HYDRO CHLORIC ACID (30%) -VIRGIN'): 'DM_HCL',
        
        # Effluent Norms (per M3)
        ('NMD - Utility Plant', 'Effluent Treated', 'Power_Dis'): 'EFFLUENT_POWER',
        ('NMD - Utility Plant', 'Effluent Treated', 'Water'): 'EFFLUENT_WATER',
        
        # HP Steam PRDS Norms (per MT)
        ('NMD - Utility Plant', 'HP Steam PRDS', 'Boiler Feed Water'): 'HP_PRDS_BFW',
        ('NMD - Utility Plant', 'HP Steam PRDS', 'SHP Steam_Dis'): 'HP_PRDS_SHP',
        
        # HRSG2 Norms (per MT SHP)
        ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'Boiler Feed Water'): 'HRSG2_BFW',
        ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'LP Steam_Dis'): 'HRSG2_LP_CREDIT',
        ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'NATURAL GAS'): 'HRSG2_NATURAL_GAS',
        ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'CHEM TRISODIUM PHOSPHATE'): 'HRSG2_TRISODIUM',
        ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'FURNACE OIL ( MEDIUM VISCOSITY GRADE )'): 'HRSG2_FURNACE_OIL',
        ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'Water'): 'HRSG2_WATER',
        
        # HRSG3 Norms (per MT SHP)
        ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'Boiler Feed Water'): 'HRSG3_BFW',
        ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'LP Steam_Dis'): 'HRSG3_LP_CREDIT',
        ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'NATURAL GAS'): 'HRSG3_NATURAL_GAS',
        ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'CHEM TRISODIUM PHOSPHATE'): 'HRSG3_TRISODIUM',
        ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'FURNACE OIL ( MEDIUM VISCOSITY GRADE )'): 'HRSG3_FURNACE_OIL',
        ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'Water'): 'HRSG3_WATER',
        
        # LP Steam PRDS Norms (per MT)
        ('NMD - Utility Plant', 'LP Steam PRDS', 'Boiler Feed Water'): 'LP_PRDS_BFW',
        ('NMD - Utility Plant', 'LP Steam PRDS', 'MP Steam_Dis'): 'LP_PRDS_MP',
        
        # MP Steam PRDS Norms (per MT)
        ('NMD - Utility Plant', 'MP Steam PRDS SHP', 'Boiler Feed Water'): 'MP_PRDS_BFW',
        ('NMD - Utility Plant', 'MP Steam PRDS SHP', 'SHP Steam_Dis'): 'MP_PRDS_SHP',
        
        # Oxygen Norms (per MT)
        ('NMD - Utility Plant', 'Oxygen', 'Power_Dis'): 'OXYGEN_POWER',
        ('NMD - Utility Plant', 'Oxygen', 'Cooling Water 2'): 'OXYGEN_CW2',
        ('NMD - Utility Plant', 'Oxygen', 'Nitrogen Gas'): 'OXYGEN_NITROGEN',
        
        # STG Steam Extraction Norms
        ('NMD - Utility Plant', 'STG1_LP STEAM', 'SHP Steam_Dis'): 'STG_LP_SHP',
        ('NMD - Utility Plant', 'STG1_MP STEAM', 'SHP Steam_Dis'): 'STG_MP_SHP',
    }
    
    # Load norms from database into NORMS object
    loaded_count = 0
    for (plant, utility, material), key in NORM_MAPPING.items():
        norm_data = service.get_norm(plant, utility, material)
        if norm_data and norm_data.get('norm_value') is not None:
            NORMS.set(key, norm_data['norm_value'])
            loaded_count += 1
        else:
            # Set to None to indicate missing
            NORMS.set(key, None)
    
    # Also load fixed quantities (for items with NULL norm but fixed quantity)
    QUANTITY_MAPPING = {
        # Power Plants - Fixed quantities per month
        ('NMD - Power Plant 2', 'POWERGEN', 'Cooling Water 2'): 'GT2_CW2_QTY',
        ('NMD - Power Plant 2', 'POWERGEN', 'COMPRESSED AIR'): 'GT2_AIR_QTY',
        ('NMD - Power Plant 3', 'POWERGEN', 'Cooling Water 2'): 'GT3_CW2_QTY',
        ('NMD - Power Plant 3', 'POWERGEN', 'COMPRESSED AIR'): 'GT3_AIR_QTY',
        ('NMD - STG Power Plant', 'POWERGEN', 'Cooling Water 2'): 'STG_CW2_QTY',
        ('NMD - STG Power Plant', 'POWERGEN', 'COMPRESSED AIR'): 'STG_AIR_QTY',
        
        # HRSG Fixed quantities
        ('NMD - Utility Plant', 'HRSG2_SHP STEAM', 'COMPRESSED AIR'): 'HRSG2_AIR_QTY',
        ('NMD - Utility Plant', 'HRSG3_SHP STEAM', 'COMPRESSED AIR'): 'HRSG3_AIR_QTY',
        
        # Utility Fixed quantities
        ('NMD - Utility Plant', 'Boiler Feed Water', 'Cooling Water 2'): 'BFW_CW2_QTY',
        ('NMD - Utility Plant', 'COMPRESSED AIR', 'Cooling Water 2'): 'AIR_CW2_QTY',
        ('NMD - Utility Plant', 'Cooling Water 1', 'COMPRESSED AIR'): 'CW1_AIR_QTY',
        ('NMD - Utility Plant', 'Cooling Water 2', 'COMPRESSED AIR'): 'CW2_AIR_QTY',
    }
    
    for (plant, utility, material), key in QUANTITY_MAPPING.items():
        norm_data = service.get_norm(plant, utility, material)
        if norm_data and norm_data.get('quantity') is not None:
            NORMS.set(key, norm_data['quantity'])
    
    # Mark as loaded
    NORMS._loaded = True
    NORMS._month = month
    NORMS._year = year
    
    print(f"\n{'='*70}")
    print(f"NORMS LOADED FROM DATABASE")
    print(f"{'='*70}")
    print(f"Period: {month}/{year}")
    print(f"Norms loaded: {loaded_count}")
    print(f"{'='*70}\n")
    
    return NORMS


def get_loaded_norms() -> DynamicNorms:
    """
    Get the currently loaded norms.
    Raises error if norms haven't been loaded yet.
    """
    if not NORMS.is_loaded():
        raise RuntimeError("Norms not loaded! Call load_norms_for_calculation(month, year) first.")
    return NORMS


def print_loaded_norms():
    """Print all currently loaded norms."""
    if not NORMS.is_loaded():
        print("No norms loaded yet!")
        return
    
    month, year = NORMS.get_period()
    print(f"\n{'='*70}")
    print(f"LOADED NORMS FOR {month}/{year}")
    print(f"{'='*70}")
    
    for key, value in sorted(NORMS._norms.items()):
        if value is not None:
            print(f"  {key:<30}: {value:>15.6f}")
        else:
            print(f"  {key:<30}: {'NULL':>15}")
    
    print(f"{'='*70}\n")


# For testing
if __name__ == "__main__":
    # Test with April 2026 (the data you provided)
    load_and_compare_norms(4, 2026)
    
    print("\n" + "="*70)
    print("TESTING DYNAMIC NORMS LOADING")
    print("="*70)
    
    # Load norms for calculation
    load_norms_for_calculation(4, 2026)
    
    # Test accessing norms
    print(f"\nAccessing norms:")
    print(f"  NORMS.GT2_NATURAL_GAS = {NORMS.GT2_NATURAL_GAS}")
    print(f"  NORMS.GT3_NATURAL_GAS = {NORMS.GT3_NATURAL_GAS}")
    print(f"  NORMS.STG_SHP_PER_KWH = {NORMS.STG_SHP_PER_KWH}")
    print(f"  NORMS.BFW_POWER = {NORMS.BFW_POWER}")
    print(f"  NORMS.get('GT2_AUX_POWER') = {NORMS.get('GT2_AUX_POWER')}")
