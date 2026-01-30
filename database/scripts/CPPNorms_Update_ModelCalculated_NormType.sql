USE [RIL.AOP]
GO

/****** 
Script: Update CPPNorms to set NormType_FK_Id = 6 (Model Calculated) 
        for all materials that have reverse calculated norms from Python model
******/

-- Update Natural Gas norms for GT Power Plants (3 plants)
UPDATE cn
SET cn.NormType_FK_Id = 7
FROM CPPNorms cn
INNER JOIN NormsHeader nh ON nh.Id = cn.NormsHeader_FK_Id
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name IN ('NMD - Power Plant 1', 'NMD - Power Plant 2', 'NMD - Power Plant 3')
  AND nh.UtilityName = 'POWERGEN'
  AND nh.MaterialName = 'NATURAL GAS';

PRINT 'Updated Natural Gas norms for GT Power Plants (PP1, PP2, PP3)';

-- Update Natural Gas norms for HRSG (3 units under Utility Plant)
UPDATE cn
SET cn.NormType_FK_Id = 7
FROM CPPNorms cn
INNER JOIN NormsHeader nh ON nh.Id = cn.NormsHeader_FK_Id
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name = 'NMD - Utility Plant'
  AND nh.UtilityName IN ('HRSG1_SHP STEAM', 'HRSG2_SHP STEAM', 'HRSG3_SHP STEAM')
  AND nh.MaterialName = 'NATURAL GAS';

PRINT 'Updated Natural Gas norms for HRSG units (HRSG1, HRSG2, HRSG3)';

-- Update SHP Steam_Dis and Ret steam condensate for STG Power Plant
UPDATE cn
SET cn.NormType_FK_Id = 6
FROM CPPNorms cn
INNER JOIN NormsHeader nh ON nh.Id = cn.NormsHeader_FK_Id
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name = 'NMD - STG Power Plant'
  AND nh.UtilityName = 'POWERGEN'
  AND nh.MaterialName IN ('SHP Steam_Dis', 'Ret steam condensate');

PRINT 'Updated SHP Steam_Dis and Ret steam condensate norms for STG Power Plant';

-- Update Power_Dis distribution ratios (Import Power from MEL + all POWERGEN sources)
-- First, let's check what material names actually exist for Power_Dis
-- SELECT DISTINCT nh.MaterialName 
-- FROM NormsHeader nh 
-- INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id 
-- WHERE p.Name = 'NMD - Utility/Power Dist' AND nh.UtilityName = 'Power_Dis';

UPDATE cn
SET cn.NormType_FK_Id = 6
FROM CPPNorms cn
INNER JOIN NormsHeader nh ON nh.Id = cn.NormsHeader_FK_Id
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name = 'NMD - Utility/Power Dist'
  AND nh.UtilityName = 'Power_Dis'
  AND (nh.MaterialName LIKE 'Power from MEL%' 
       OR nh.MaterialName LIKE 'POWERGEN%'
       OR nh.MaterialName LIKE '%PP1%'
       OR nh.MaterialName LIKE '%PP2%'
       OR nh.MaterialName LIKE '%PP3%'
       OR nh.MaterialName LIKE '%STG%'
       OR nh.MaterialName LIKE '%Power Plant%');

PRINT 'Updated Power_Dis distribution ratios (MEL Import + all POWERGEN sources)';

-- Update LP Steam_Dis distribution ratios
UPDATE cn
SET cn.NormType_FK_Id = 7
FROM CPPNorms cn
INNER JOIN NormsHeader nh ON nh.Id = cn.NormsHeader_FK_Id
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name = 'NMD - Utility/Power Dist'
  AND nh.UtilityName = 'LP Steam_Dis'
  AND nh.MaterialName IN ('LP Steam PRDS', 'STG1_LP STEAM');

PRINT 'Updated LP Steam_Dis distribution ratios (PRDS, STG)';

-- Update MP Steam_Dis distribution ratios
UPDATE cn
SET cn.NormType_FK_Id = 7
FROM CPPNorms cn
INNER JOIN NormsHeader nh ON nh.Id = cn.NormsHeader_FK_Id
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name = 'NMD - Utility/Power Dist'
  AND nh.UtilityName = 'MP Steam_Dis'
  AND nh.MaterialName IN ('MP Steam PRDS SHP', 'STG1_MP STEAM');

PRINT 'Updated MP Steam_Dis distribution ratios (PRDS, STG)';

-- Update SHP Steam_Dis distribution ratios
UPDATE cn
SET cn.NormType_FK_Id = 7
FROM CPPNorms cn
INNER JOIN NormsHeader nh ON nh.Id = cn.NormsHeader_FK_Id
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
WHERE p.Name = 'NMD - Utility/Power Dist'
  AND nh.UtilityName = 'SHP Steam_Dis'
  AND nh.MaterialName IN ('HRSG1_SHP STEAM', 'HRSG2_SHP STEAM', 'HRSG3_SHP STEAM');

PRINT 'Updated SHP Steam_Dis distribution ratios (HRSG1, HRSG2, HRSG3)';

-- Summary of updates
SELECT 
    p.Name AS PlantName,
    nh.UtilityName,
    nh.MaterialName,
    nt.NormName AS NormType,
    COUNT(*) AS RecordCount
FROM CPPNorms cn
INNER JOIN NormsHeader nh ON nh.Id = cn.NormsHeader_FK_Id
INNER JOIN Plants p ON p.Id = nh.Plant_FK_Id
INNER JOIN NormTypes nt ON nt.Id = cn.NormType_FK_Id
WHERE cn.NormType_FK_Id = 7
GROUP BY p.Name, nh.UtilityName, nh.MaterialName, nt.NormName
ORDER BY p.Name, nh.UtilityName, nh.MaterialName;

PRINT 'Update completed. Summary of Model Calculated norms displayed above.';

GO
