USE [RIL.AOP]
GO

-- Diagnostic Query: Check what materials exist for Power_Dis utility
PRINT '=== Materials under Power_Dis utility ==='
SELECT DISTINCT 
    p.Name AS PlantName,
    nh.UtilityName,
    nh.MaterialName,
    nh.AccountName
FROM NormsHeader nh
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name = 'NMD - Utility/Power Dist'
  AND nh.UtilityName = 'Power_Dis'
  AND nh.IsActive = 1
ORDER BY nh.MaterialName;

PRINT '';
PRINT '=== All utilities under NMD - Utility/Power Dist plant ==='
SELECT DISTINCT 
    p.Name AS PlantName,
    nh.UtilityName,
    COUNT(DISTINCT nh.MaterialName) AS MaterialCount
FROM NormsHeader nh
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name = 'NMD - Utility/Power Dist'
  AND nh.IsActive = 1
GROUP BY p.Name, nh.UtilityName
ORDER BY nh.UtilityName;

PRINT '';
PRINT '=== Check if POWERGEN materials exist anywhere ==='
SELECT DISTINCT 
    p.Name AS PlantName,
    nh.UtilityName,
    nh.MaterialName
FROM NormsHeader nh
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE nh.MaterialName LIKE '%POWERGEN%'
   OR nh.MaterialName LIKE '%PP1%'
   OR nh.MaterialName LIKE '%PP2%'
   OR nh.MaterialName LIKE '%PP3%'
ORDER BY p.Name, nh.UtilityName, nh.MaterialName;

GO
