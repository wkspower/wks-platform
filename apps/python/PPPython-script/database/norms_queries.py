"""
Norms Database Queries
======================
Queries to fetch norms data from NormsMonthDetail and NormsHeader tables.
"""

from database.connection import get_connection


def fetch_all_norms_for_month(month: int, year: int) -> list:
    """
    Fetch all norms for a specific month and year.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025, 2026)
    
    Returns:
        List of dictionaries containing norm data
    """
    conn = get_connection()
    cur = conn.cursor()
    
    query = """
    SELECT 
        nh.Id AS NormHeaderId,
        p.Name AS PlantName,
        p.DisplayName AS PlantDisplayName,
        nh.UtilityName,
        nh.UtilityId,
        nh.UtilityUOM,
        nh.AccountName,
        nh.MaterialName,
        nh.IssuingPlantName,
        nh.IssuingUOM,
        np.DisplayName AS NormParameterName,
        np.UOM AS NormParameterUOM,
        nmd.Norms AS NormValue,
        nmd.Quantity,
        nmd.QTY AS Generation,
        nmd.GenerationUOM,
        nmd.Amount,
        nmd.Price,
        nh.DisplayOrder AS HeaderDisplayOrder,
        nmd.DisplayOrder AS DetailDisplayOrder,
        fym.Id AS FinancialYearMonthId,
        fym.Month,
        fym.Year
    FROM NormsMonthDetail nmd
    INNER JOIN NormsHeader nh ON nh.Id = nmd.NormsHeader_FK_Id
    INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
    INNER JOIN FinancialYearMonth fym ON fym.Id = nmd.FinancialYearMonth_FK_Id
    LEFT JOIN NormParameters np ON np.Id = nh.NormParameter_FK_Id
    WHERE fym.Month = ? 
      AND fym.Year = ?
      AND nh.IsActive = 1
    ORDER BY p.Name, nh.DisplayOrder, nmd.DisplayOrder
    """
    
    cur.execute(query, (month, year))
    columns = [column[0] for column in cur.description]
    results = []
    
    for row in cur.fetchall():
        results.append(dict(zip(columns, row)))
    
    conn.close()
    return results


def fetch_norms_by_plant(month: int, year: int, plant_name: str) -> list:
    """
    Fetch norms for a specific plant, month, and year.
    
    Args:
        month: Month number (1-12)
        year: Year (e.g., 2025, 2026)
        plant_name: Plant name (e.g., 'NMD - Power Plant 2')
    
    Returns:
        List of dictionaries containing norm data for the plant
    """
    conn = get_connection()
    cur = conn.cursor()
    
    query = """
    SELECT 
        nh.Id AS NormHeaderId,
        p.Name AS PlantName,
        nh.UtilityName,
        nh.MaterialName,
        nh.UtilityUOM,
        nh.IssuingUOM,
        nmd.Norms AS NormValue,
        nmd.Quantity,
        nmd.QTY AS Generation,
        nmd.GenerationUOM
    FROM NormsMonthDetail nmd
    INNER JOIN NormsHeader nh ON nh.Id = nmd.NormsHeader_FK_Id
    INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
    INNER JOIN FinancialYearMonth fym ON fym.Id = nmd.FinancialYearMonth_FK_Id
    WHERE fym.Month = ? 
      AND fym.Year = ?
      AND nh.IsActive = 1
      AND p.Name = ?
    ORDER BY nh.DisplayOrder
    """
    
    cur.execute(query, (month, year, plant_name))
    columns = [column[0] for column in cur.description]
    results = []
    
    for row in cur.fetchall():
        results.append(dict(zip(columns, row)))
    
    conn.close()
    return results
