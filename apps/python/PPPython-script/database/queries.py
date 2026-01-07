FETCH_POWER_AND_ASSETS = """
WITH req AS (
    SELECT 
        fym.id AS fym_id,
        SUM(pr.powerRequirement) AS PlantRequirementMW,
        SUM(fc.powerRequirement) AS FixedRequirementMW
    FROM FinancialYearMonth fym
    LEFT JOIN PlantRequirement pr ON pr.financialYearMonthId = fym.id
    LEFT JOIN FixedConsumption fc ON fc.financialYearMonthId = fym.id
    WHERE fym.month = ? AND fym.year = ?
    GROUP BY fym.id
)
SELECT 
    (r.PlantRequirementMW + r.FixedRequirementMW) AS TotalDemandMW,
    a.AssetName,
    a.AssetCapacity,
    aa.minOperatingCapacity,
    aa.operationalHours,
    aa.Priority,
    aa.isAssetAvailable
FROM req r
JOIN AssetAvailability aa ON aa.financialYearMonthId = r.fym_id
JOIN PowerGenerationAssets a ON a.assetId = aa.assetId
ORDER BY aa.Priority ASC;
"""

# ============================================================
# DEPRECATED: IMPORT POWER AVAILABILITY QUERIES
# ============================================================
# NOTE: These queries reference the ImportPowerAvailability table
# which is no longer used. Import power functionality has been
# moved to database/import_queries.py which queries the existing
# AssetImportMapping table instead.
#
# DO NOT USE - For historical reference only
# Use database/import_queries.py functions instead:
#   - fetch_import_power_availability(month, year)
#   - fetch_total_import_capacity(month, year)
#   - fetch_stg_min_operating_capacity(month, year)
# ============================================================
