-- ============================================================
-- UTILITY NORMS DATA INSERT SCRIPT
-- Power Plant Budgeting System
-- ============================================================

-- ============================================================
-- STEP 1: INSERT PLANT MASTER DATA
-- ============================================================
DECLARE @Plant_PowerPlant1 UNIQUEIDENTIFIER = NEWID();
DECLARE @Plant_PowerPlant2 UNIQUEIDENTIFIER = NEWID();
DECLARE @Plant_PowerPlant3 UNIQUEIDENTIFIER = NEWID();
DECLARE @Plant_STGPowerPlant UNIQUEIDENTIFIER = NEWID();
DECLARE @Plant_UtilityPlant UNIQUEIDENTIFIER = NEWID();
DECLARE @Plant_UtilityPowerDist UNIQUEIDENTIFIER = NEWID();
DECLARE @Plant_RevProc UNIQUEIDENTIFIER = NEWID();

INSERT INTO PlantMaster (PlantId, PlantCode, PlantName, Description) VALUES
(@Plant_PowerPlant1, 'PP1', 'NMD - Power Plant 1', 'Power Generation Plant 1 (GT1)'),
(@Plant_PowerPlant2, 'PP2', 'NMD - Power Plant 2', 'Power Generation Plant 2 (GT2)'),
(@Plant_PowerPlant3, 'PP3', 'NMD - Power Plant 3', 'Power Generation Plant 3 (GT3)'),
(@Plant_STGPowerPlant, 'STG', 'NMD - STG Power Plant', 'Steam Turbine Generator Power Plant'),
(@Plant_UtilityPlant, 'UTIL', 'NMD - Utility Plant', 'Utility Generation Plant'),
(@Plant_UtilityPowerDist, 'DIST', 'NMD - Utility/Power Dist', 'Utility and Power Distribution'),
(@Plant_RevProc, 'PROC', 'NMD-Rev Proc', 'Revenue Process / Raw Material Source');

-- ============================================================
-- STEP 2: INSERT ACCOUNT TYPE MASTER DATA
-- ============================================================
DECLARE @AccType_Utilities UNIQUEIDENTIFIER = NEWID();
DECLARE @AccType_RawMaterial UNIQUEIDENTIFIER = NEWID();
DECLARE @AccType_CatalystChemical UNIQUEIDENTIFIER = NEWID();
DECLARE @AccType_StoresSpares UNIQUEIDENTIFIER = NEWID();
DECLARE @AccType_ByProduct UNIQUEIDENTIFIER = NEWID();

INSERT INTO AccountTypeMaster (AccountTypeId, AccountTypeName, Description) VALUES
(@AccType_Utilities, 'Utilities', 'Utility inputs like steam, power, water'),
(@AccType_RawMaterial, 'Raw Material', 'Raw materials like natural gas, water, acids'),
(@AccType_CatalystChemical, 'Catalyst & Chemical', 'Chemicals and catalysts'),
(@AccType_StoresSpares, 'Stores & Spares', 'Spare parts and stores'),
(@AccType_ByProduct, 'By Product', 'By-products generated');

-- ============================================================
-- STEP 3: INSERT UTILITY MASTER DATA
-- ============================================================

-- Power Generation Utilities
DECLARE @Util_PowerGen_PP1 UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_PowerGen_PP2 UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_PowerGen_PP3 UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_PowerGen_STG UNIQUEIDENTIFIER = NEWID();

-- Steam Distribution Utilities
DECLARE @Util_LP_Steam_Dis UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_MP_Steam_Dis UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_HP_Steam_Dis UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_SHP_Steam_Dis UNIQUEIDENTIFIER = NEWID();

-- Steam Generation/Supply Utilities
DECLARE @Util_LP_Steam_PRDS UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_MP_Steam_PRDS_SHP UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_HP_Steam_PRDS UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_STG1_LP_Steam UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_STG1_MP_Steam UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_HRSG1_SHP_Steam UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_HRSG2_SHP_Steam UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_HRSG3_SHP_Steam UNIQUEIDENTIFIER = NEWID();

-- Water Utilities
DECLARE @Util_BFW UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_DMWater UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_CoolingWater1 UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_CoolingWater2 UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_RetSteamCondensate UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_EffluentTreated UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_TreatedSpentCaustic UNIQUEIDENTIFIER = NEWID();

-- Power Distribution
DECLARE @Util_Power_Dis UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_PowerFromMEL UNIQUEIDENTIFIER = NEWID();

-- Other Utilities
DECLARE @Util_CompressedAir UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_Oxygen UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_NitrogenGas UNIQUEIDENTIFIER = NEWID();

-- Raw Materials (for reference in norms)
DECLARE @Util_NaturalGas UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_FurnaceOil UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_Water UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_SulphuricAcid UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_HydrochloricAcid UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_CausticSodaLye UNIQUEIDENTIFIER = NEWID();

-- Chemicals
DECLARE @Util_ChemCycloHexy UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_ChemMorpholene UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_KemWatreat UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_ChemTrisodiumPhosphate UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_ChemAlumSulfate UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_ChemSodiumSulphite UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_Polyelectrolyte UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_SodiumChloride UNIQUEIDENTIFIER = NEWID();
DECLARE @Util_Urea UNIQUEIDENTIFIER = NEWID();

-- INSERT UTILITY MASTER RECORDS

-- Power Generation
INSERT INTO UtilityMaster (UtilityId, UtilityCode, UtilityName, UOM, PlantId, UtilityType, IsDistribution) VALUES
(@Util_PowerGen_PP1, '310027907', 'POWERGEN_PP1', 'KWH', @Plant_PowerPlant1, 'POWER', 0),
(@Util_PowerGen_PP2, '310027907', 'POWERGEN_PP2', 'KWH', @Plant_PowerPlant2, 'POWER', 0),
(@Util_PowerGen_PP3, '310027907', 'POWERGEN_PP3', 'KWH', @Plant_PowerPlant3, 'POWER', 0),
(@Util_PowerGen_STG, '310027907', 'POWERGEN_STG', 'KWH', @Plant_STGPowerPlant, 'POWER', 0);

-- Steam Distribution (these are the demand points)
INSERT INTO UtilityMaster (UtilityId, UtilityCode, UtilityName, UOM, PlantId, UtilityType, IsDistribution) VALUES
(@Util_LP_Steam_Dis, '310027965', 'LP Steam_Dis', 'MT', @Plant_UtilityPowerDist, 'STEAM', 1),
(@Util_MP_Steam_Dis, '310027940', 'MP Steam_Dis', 'MT', @Plant_UtilityPowerDist, 'STEAM', 1),
(@Util_HP_Steam_Dis, '310027939', 'HP Steam_Dis', 'MT', @Plant_UtilityPowerDist, 'STEAM', 1),
(@Util_SHP_Steam_Dis, '310027924', 'SHP Steam_Dis', 'MT', @Plant_UtilityPowerDist, 'STEAM', 1);

-- Steam Generation/Supply (these are the suppliers)
INSERT INTO UtilityMaster (UtilityId, UtilityCode, UtilityName, UOM, PlantId, UtilityType, IsDistribution) VALUES
(@Util_LP_Steam_PRDS, '310028013', 'LP Steam PRDS', 'MT', @Plant_UtilityPlant, 'STEAM', 0),
(@Util_MP_Steam_PRDS_SHP, '310028012', 'MP Steam PRDS SHP', 'MT', @Plant_UtilityPlant, 'STEAM', 0),
(@Util_HP_Steam_PRDS, '310028161', 'HP Steam PRDS', 'MT', @Plant_UtilityPlant, 'STEAM', 0),
(@Util_STG1_LP_Steam, '310028010', 'STG1_LP STEAM', 'MT', @Plant_UtilityPlant, 'STEAM', 0),
(@Util_STG1_MP_Steam, '310027952', 'STG1_MP STEAM', 'MT', @Plant_UtilityPlant, 'STEAM', 0),
(@Util_HRSG1_SHP_Steam, '310027926', 'HRSG1_SHP STEAM', 'MT', @Plant_UtilityPlant, 'STEAM', 0),
(@Util_HRSG2_SHP_Steam, '310027929', 'HRSG2_SHP STEAM', 'MT', @Plant_UtilityPlant, 'STEAM', 0),
(@Util_HRSG3_SHP_Steam, '310027930', 'HRSG3_SHP STEAM', 'MT', @Plant_UtilityPlant, 'STEAM', 0);

-- Water Utilities
INSERT INTO UtilityMaster (UtilityId, UtilityCode, UtilityName, UOM, PlantId, UtilityType, IsDistribution) VALUES
(@Util_BFW, '310027927', 'Boiler Feed Water', 'M3', @Plant_UtilityPlant, 'WATER', 0),
(@Util_DMWater, '310027966', 'D M Water', 'M3', @Plant_UtilityPlant, 'WATER', 0),
(@Util_CoolingWater1, '310028005', 'Cooling Water 1', 'KM3', @Plant_UtilityPlant, 'WATER', 0),
(@Util_CoolingWater2, '310028004', 'Cooling Water 2', 'KM3', @Plant_UtilityPlant, 'WATER', 0),
(@Util_RetSteamCondensate, NULL, 'Ret steam condensate', 'M3', @Plant_STGPowerPlant, 'WATER', 0),
(@Util_EffluentTreated, '310027994', 'Effluent Treated', 'M3', @Plant_UtilityPlant, 'WATER', 0),
(@Util_TreatedSpentCaustic, '310028011', 'Treated Spent Caustic', 'M3', @Plant_UtilityPlant, 'WATER', 0);

-- Power Distribution
INSERT INTO UtilityMaster (UtilityId, UtilityCode, UtilityName, UOM, PlantId, UtilityType, IsDistribution) VALUES
(@Util_Power_Dis, '310027910', 'Power_Dis', 'KWH', @Plant_UtilityPowerDist, 'POWER', 1),
(@Util_PowerFromMEL, NULL, 'Power from MEL', 'KWH', @Plant_RevProc, 'POWER', 0);

-- Other Utilities
INSERT INTO UtilityMaster (UtilityId, UtilityCode, UtilityName, UOM, PlantId, UtilityType, IsDistribution) VALUES
(@Util_CompressedAir, '310027904', 'COMPRESSED AIR', 'NM3', @Plant_UtilityPlant, 'GAS', 0),
(@Util_Oxygen, NULL, 'OXYGEN', 'MT', @Plant_UtilityPlant, 'GAS', 0),
(@Util_NitrogenGas, NULL, 'Nitrogen Gas', 'NM3', @Plant_UtilityPlant, 'GAS', 0);

-- Raw Materials
INSERT INTO UtilityMaster (UtilityId, UtilityCode, UtilityName, UOM, PlantId, UtilityType, IsDistribution) VALUES
(@Util_NaturalGas, NULL, 'NATURAL GAS', 'MMBTU', @Plant_RevProc, 'RAW_MATERIAL', 0),
(@Util_FurnaceOil, NULL, 'FURNACE OIL ( MEDIUM VISCOSITY GRADE )', 'MMBTU', @Plant_RevProc, 'RAW_MATERIAL', 0),
(@Util_Water, NULL, 'Water', 'M3', @Plant_RevProc, 'RAW_MATERIAL', 0),
(@Util_SulphuricAcid, NULL, 'SULPHURIC ACID', 'MT', @Plant_RevProc, 'RAW_MATERIAL', 0),
(@Util_HydrochloricAcid, NULL, 'HYDRO CHLORIC ACID (30%) -VIRGIN', 'MT', @Plant_RevProc, 'RAW_MATERIAL', 0),
(@Util_CausticSodaLye, NULL, 'CAUSTIC SODA LYE – GRADE 1', 'MT', @Plant_RevProc, 'RAW_MATERIAL', 0);

-- Chemicals
INSERT INTO UtilityMaster (UtilityId, UtilityCode, UtilityName, UOM, PlantId, UtilityType, IsDistribution) VALUES
(@Util_ChemCycloHexy, NULL, 'CHEM CYCLO HEXY', 'KG', @Plant_RevProc, 'CHEMICAL', 0),
(@Util_ChemMorpholene, NULL, 'CHEM MORPHOLENE', 'MT', @Plant_RevProc, 'CHEMICAL', 0),
(@Util_KemWatreat, NULL, 'KEM WATREAT B 70M', 'KG', @Plant_RevProc, 'CHEMICAL', 0),
(@Util_ChemTrisodiumPhosphate, NULL, 'CHEM TRISODIUM PHOSPHATE', 'KG', @Plant_RevProc, 'CHEMICAL', 0),
(@Util_ChemAlumSulfate, NULL, 'CHEM ALUM.SULFATE, AL2(SO4)3,18H2O', 'KG', @Plant_RevProc, 'CHEMICAL', 0),
(@Util_ChemSodiumSulphite, NULL, 'CHEM SODIUM SULPHITE;PN:MIS 19OX', 'KG', @Plant_RevProc, 'CHEMICAL', 0),
(@Util_Polyelectrolyte, NULL, 'POLYELECTROLYTE', 'KG', @Plant_RevProc, 'CHEMICAL', 0),
(@Util_SodiumChloride, NULL, 'SODIUM CHLORIDE IS 797 GRADE1', 'MT', @Plant_RevProc, 'CHEMICAL', 0),
(@Util_Urea, NULL, 'UREA,NITROGEN CONTENT 46%', 'KG', @Plant_RevProc, 'CHEMICAL', 0);

-- ============================================================
-- STEP 4: INSERT UTILITY NORMS DATA
-- ============================================================

-- ============================================================
-- LP Steam_Dis - Distribution Norms (Who supplies LP Steam)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_LP_Steam_Dis, @Util_LP_Steam_PRDS, @AccType_Utilities, 0.3866, 'DISTRIBUTION', 'LP Steam from PRDS - 38.66% of total LP demand'),
(@Util_LP_Steam_Dis, @Util_STG1_LP_Steam, @AccType_Utilities, 0.6134, 'DISTRIBUTION', 'LP Steam from STG1 - 61.34% of total LP demand');

-- ============================================================
-- MP Steam_Dis - Distribution Norms (Who supplies MP Steam)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_MP_Steam_Dis, @Util_MP_Steam_PRDS_SHP, @AccType_Utilities, 0.7092, 'DISTRIBUTION', 'MP Steam from PRDS SHP - 70.92% of total MP demand'),
(@Util_MP_Steam_Dis, @Util_STG1_MP_Steam, @AccType_Utilities, 0.2908, 'DISTRIBUTION', 'MP Steam from STG1 - 29.08% of total MP demand');

-- ============================================================
-- HP Steam_Dis - Distribution Norms (Who supplies HP Steam)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_HP_Steam_Dis, @Util_HP_Steam_PRDS, @AccType_Utilities, 1.0000, 'DISTRIBUTION', 'HP Steam 100% from PRDS');

-- ============================================================
-- SHP Steam_Dis - Distribution Norms (Who supplies SHP Steam)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_SHP_Steam_Dis, @Util_HRSG1_SHP_Steam, @AccType_Utilities, NULL, 'DISTRIBUTION', 'SHP from HRSG1 - factor TBD based on availability'),
(@Util_SHP_Steam_Dis, @Util_HRSG2_SHP_Steam, @AccType_Utilities, 0.4934, 'DISTRIBUTION', 'SHP from HRSG2 - 49.34%'),
(@Util_SHP_Steam_Dis, @Util_HRSG3_SHP_Steam, @AccType_Utilities, 0.5066, 'DISTRIBUTION', 'SHP from HRSG3 - 50.66%');

-- ============================================================
-- LP Steam PRDS - Conversion Norms (What LP PRDS needs)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_LP_Steam_PRDS, @Util_BFW, @AccType_Utilities, 0.2500, 'CONVERSION', '0.25 M3 BFW per MT LP from PRDS'),
(@Util_LP_Steam_PRDS, @Util_MP_Steam_Dis, @AccType_Utilities, 0.7500, 'CONVERSION', '0.75 MT MP required per MT LP from PRDS');

-- ============================================================
-- MP Steam PRDS SHP - Conversion Norms (What MP PRDS needs)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_MP_Steam_PRDS_SHP, @Util_BFW, @AccType_Utilities, 0.0900, 'CONVERSION', '0.09 M3 BFW per MT MP from PRDS'),
(@Util_MP_Steam_PRDS_SHP, @Util_SHP_Steam_Dis, @AccType_Utilities, 0.9100, 'CONVERSION', '0.91 MT SHP required per MT MP from PRDS');

-- ============================================================
-- HP Steam PRDS - Conversion Norms (What HP PRDS needs)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_HP_Steam_PRDS, @Util_BFW, @AccType_Utilities, 0.0768, 'CONVERSION', '0.0768 M3 BFW per MT HP from PRDS'),
(@Util_HP_Steam_PRDS, @Util_SHP_Steam_Dis, @AccType_Utilities, 0.9232, 'CONVERSION', '0.9232 MT SHP required per MT HP from PRDS');

-- ============================================================
-- STG1_LP STEAM - Conversion Norms (What STG LP needs)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_STG1_LP_Steam, @Util_SHP_Steam_Dis, @AccType_Utilities, 0.4800, 'CONVERSION', '0.48 MT SHP required per MT LP from STG');

-- ============================================================
-- STG1_MP STEAM - Conversion Norms (What STG MP needs)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_STG1_MP_Steam, @Util_SHP_Steam_Dis, @AccType_Utilities, 0.6900, 'CONVERSION', '0.69 MT SHP required per MT MP from STG');

-- ============================================================
-- Boiler Feed Water - Conversion Norms (What BFW needs)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_BFW, @Util_LP_Steam_Dis, @AccType_Utilities, 0.1450, 'CONVERSION', '0.145 MT LP Steam per M3 BFW (LP for UFU)'),
(@Util_BFW, @Util_DMWater, @AccType_Utilities, 0.8600, 'CONVERSION', '0.86 M3 DM Water per M3 BFW'),
(@Util_BFW, @Util_Power_Dis, @AccType_Utilities, 9.5000, 'CONVERSION', '9.5 KWH Power per M3 BFW'),
(@Util_BFW, @Util_ChemCycloHexy, @AccType_CatalystChemical, 0.0001, 'CONVERSION', 'Chemical per M3 BFW'),
(@Util_BFW, @Util_ChemMorpholene, @AccType_CatalystChemical, 0.0000, 'CONVERSION', 'Chemical per M3 BFW'),
(@Util_BFW, @Util_KemWatreat, @AccType_CatalystChemical, 0.0006, 'CONVERSION', 'Chemical per M3 BFW');

-- ============================================================
-- HRSG1_SHP STEAM - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_HRSG1_SHP_Steam, @Util_BFW, @AccType_Utilities, 1.0240, 'CONVERSION', '1.024 M3 BFW per MT SHP from HRSG1'),
(@Util_HRSG1_SHP_Steam, @Util_LP_Steam_Dis, @AccType_Utilities, -0.0504, 'CONVERSION', 'Free LP Steam generated (negative = output)'),
(@Util_HRSG1_SHP_Steam, @Util_FurnaceOil, @AccType_RawMaterial, 0.0001, 'CONVERSION', 'Furnace Oil per MT SHP'),
(@Util_HRSG1_SHP_Steam, @Util_Water, @AccType_RawMaterial, 0.0027, 'CONVERSION', 'Water per MT SHP'),
(@Util_HRSG1_SHP_Steam, @Util_ChemTrisodiumPhosphate, @AccType_CatalystChemical, 0.0009, 'CONVERSION', 'Chemical per MT SHP');

-- ============================================================
-- HRSG2_SHP STEAM - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_HRSG2_SHP_Steam, @Util_BFW, @AccType_Utilities, 1.0240, 'CONVERSION', '1.024 M3 BFW per MT SHP from HRSG2'),
(@Util_HRSG2_SHP_Steam, @Util_LP_Steam_Dis, @AccType_Utilities, -0.0504, 'CONVERSION', 'Free LP Steam generated (negative = output)'),
(@Util_HRSG2_SHP_Steam, @Util_NaturalGas, @AccType_RawMaterial, 2.8064, 'CONVERSION', 'Natural Gas MMBTU per MT SHP'),
(@Util_HRSG2_SHP_Steam, @Util_FurnaceOil, @AccType_RawMaterial, 0.0001, 'CONVERSION', 'Furnace Oil per MT SHP'),
(@Util_HRSG2_SHP_Steam, @Util_Water, @AccType_RawMaterial, 0.0027, 'CONVERSION', 'Water per MT SHP'),
(@Util_HRSG2_SHP_Steam, @Util_ChemTrisodiumPhosphate, @AccType_CatalystChemical, 0.0009, 'CONVERSION', 'Chemical per MT SHP');

-- ============================================================
-- HRSG3_SHP STEAM - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_HRSG3_SHP_Steam, @Util_BFW, @AccType_Utilities, 1.0240, 'CONVERSION', '1.024 M3 BFW per MT SHP from HRSG3'),
(@Util_HRSG3_SHP_Steam, @Util_LP_Steam_Dis, @AccType_Utilities, -0.0504, 'CONVERSION', 'Free LP Steam generated (negative = output)'),
(@Util_HRSG3_SHP_Steam, @Util_NaturalGas, @AccType_RawMaterial, 2.8168, 'CONVERSION', 'Natural Gas MMBTU per MT SHP'),
(@Util_HRSG3_SHP_Steam, @Util_FurnaceOil, @AccType_RawMaterial, 0.0001, 'CONVERSION', 'Furnace Oil per MT SHP'),
(@Util_HRSG3_SHP_Steam, @Util_Water, @AccType_RawMaterial, 0.0027, 'CONVERSION', 'Water per MT SHP'),
(@Util_HRSG3_SHP_Steam, @Util_ChemTrisodiumPhosphate, @AccType_CatalystChemical, 0.0009, 'CONVERSION', 'Chemical per MT SHP');

-- ============================================================
-- Power_Dis - Distribution Norms (Who supplies Power)
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_Power_Dis, @Util_PowerFromMEL, @AccType_Utilities, 0.3969, 'DISTRIBUTION', 'Power from MEL import - 39.69%'),
(@Util_Power_Dis, @Util_PowerGen_PP2, @AccType_Utilities, 0.1778, 'DISTRIBUTION', 'Power from PP2 - 17.78%'),
(@Util_Power_Dis, @Util_PowerGen_PP3, @AccType_Utilities, 0.1996, 'DISTRIBUTION', 'Power from PP3 - 19.96%'),
(@Util_Power_Dis, @Util_PowerGen_STG, @AccType_Utilities, 0.2256, 'DISTRIBUTION', 'Power from STG - 22.56%');

-- ============================================================
-- Power Plant Norms (Aux consumption, fuel, etc.)
-- ============================================================

-- NMD - Power Plant 1
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_PowerGen_PP1, @Util_Power_Dis, @AccType_Utilities, 0.0140, 'CONVERSION', 'Aux power consumption per KWH generated');

-- NMD - Power Plant 2
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_PowerGen_PP2, @Util_NaturalGas, @AccType_RawMaterial, 0.0101, 'CONVERSION', 'Natural Gas MMBTU per KWH'),
(@Util_PowerGen_PP2, @Util_Power_Dis, @AccType_Utilities, 0.0140, 'CONVERSION', 'Aux power consumption per KWH generated');

-- NMD - Power Plant 3
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_PowerGen_PP3, @Util_NaturalGas, @AccType_RawMaterial, 0.0095, 'CONVERSION', 'Natural Gas MMBTU per KWH'),
(@Util_PowerGen_PP3, @Util_Power_Dis, @AccType_Utilities, 0.0140, 'CONVERSION', 'Aux power consumption per KWH generated');

-- NMD - STG Power Plant
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_PowerGen_STG, @Util_RetSteamCondensate, @AccType_ByProduct, 0.0029, 'CONVERSION', 'Return steam condensate per KWH'),
(@Util_PowerGen_STG, @Util_Power_Dis, @AccType_Utilities, 0.0020, 'CONVERSION', 'Aux power consumption per KWH generated'),
(@Util_PowerGen_STG, @Util_SHP_Steam_Dis, @AccType_Utilities, 0.0036, 'CONVERSION', 'SHP Steam required per KWH from STG');

-- ============================================================
-- DM Water - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_DMWater, @Util_Water, @AccType_RawMaterial, 1.0500, 'CONVERSION', '1.05 M3 raw water per M3 DM Water'),
(@Util_DMWater, @Util_Power_Dis, @AccType_Utilities, 1.2100, 'CONVERSION', '1.21 KWH per M3 DM Water'),
(@Util_DMWater, @Util_CompressedAir, @AccType_Utilities, 0.0770, 'CONVERSION', 'Compressed air per M3 DM Water'),
(@Util_DMWater, @Util_RetSteamCondensate, @AccType_Utilities, 0.2030, 'CONVERSION', 'Return condensate per M3 DM Water'),
(@Util_DMWater, @Util_CausticSodaLye, @AccType_CatalystChemical, 0.0002, 'CONVERSION', 'Caustic per M3 DM Water'),
(@Util_DMWater, @Util_ChemAlumSulfate, @AccType_CatalystChemical, 0.0008, 'CONVERSION', 'Alum sulfate per M3 DM Water'),
(@Util_DMWater, @Util_ChemSodiumSulphite, @AccType_CatalystChemical, 0.0007, 'CONVERSION', 'Sodium sulphite per M3 DM Water'),
(@Util_DMWater, @Util_Polyelectrolyte, @AccType_CatalystChemical, 0.0006, 'CONVERSION', 'Polyelectrolyte per M3 DM Water'),
(@Util_DMWater, @Util_SodiumChloride, @AccType_CatalystChemical, 0.0000, 'CONVERSION', 'Sodium chloride per M3 DM Water'),
(@Util_DMWater, @Util_HydrochloricAcid, @AccType_RawMaterial, 0.0004, 'CONVERSION', 'HCl per M3 DM Water');

-- ============================================================
-- Cooling Water 1 - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_CoolingWater1, @Util_Water, @AccType_RawMaterial, 11.0500, 'CONVERSION', '11.05 M3 raw water per KM3 CW1'),
(@Util_CoolingWater1, @Util_Power_Dis, @AccType_Utilities, 245.0000, 'CONVERSION', '245 KWH per KM3 CW1'),
(@Util_CoolingWater1, @Util_SulphuricAcid, @AccType_RawMaterial, 0.0002, 'CONVERSION', 'Sulphuric acid per KM3 CW1');

-- ============================================================
-- Cooling Water 2 - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_CoolingWater2, @Util_Water, @AccType_RawMaterial, 11.5000, 'CONVERSION', '11.5 M3 raw water per KM3 CW2'),
(@Util_CoolingWater2, @Util_Power_Dis, @AccType_Utilities, 250.0000, 'CONVERSION', '250 KWH per KM3 CW2'),
(@Util_CoolingWater2, @Util_SulphuricAcid, @AccType_RawMaterial, 0.0002, 'CONVERSION', 'Sulphuric acid per KM3 CW2');

-- ============================================================
-- Compressed Air - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_CompressedAir, @Util_Power_Dis, @AccType_Utilities, 0.1650, 'CONVERSION', '0.165 KWH per NM3 Compressed Air');

-- ============================================================
-- Effluent Treated - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_EffluentTreated, @Util_Water, @AccType_RawMaterial, 0.0007, 'CONVERSION', 'Raw water per M3 effluent'),
(@Util_EffluentTreated, @Util_Power_Dis, @AccType_Utilities, 3.5400, 'CONVERSION', '3.54 KWH per M3 effluent');

-- ============================================================
-- Treated Spent Caustic - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_TreatedSpentCaustic, @Util_SulphuricAcid, @AccType_RawMaterial, 0.0200, 'CONVERSION', 'Sulphuric acid per M3'),
(@Util_TreatedSpentCaustic, @Util_Water, @AccType_RawMaterial, 0.0934, 'CONVERSION', 'Water per M3'),
(@Util_TreatedSpentCaustic, @Util_CompressedAir, @AccType_Utilities, 99.4124, 'CONVERSION', 'Compressed air per M3'),
(@Util_TreatedSpentCaustic, @Util_MP_Steam_Dis, @AccType_Utilities, 0.0390, 'CONVERSION', 'MP Steam per M3'),
(@Util_TreatedSpentCaustic, @Util_Power_Dis, @AccType_Utilities, 17.0400, 'CONVERSION', 'Power per M3');

-- ============================================================
-- Oxygen - Conversion Norms
-- ============================================================
INSERT INTO UtilityNorms (ConsumerUtilityId, SupplierUtilityId, AccountTypeId, NormFactor, NormType, Description) VALUES
(@Util_Oxygen, @Util_NitrogenGas, @AccType_ByProduct, 2448.4000, 'CONVERSION', 'Nitrogen by-product per MT Oxygen'),
(@Util_Oxygen, @Util_CoolingWater2, @AccType_Utilities, 0.2610, 'CONVERSION', 'Cooling water per MT Oxygen'),
(@Util_Oxygen, @Util_Power_Dis, @AccType_Utilities, 968.6500, 'CONVERSION', 'Power per MT Oxygen');

PRINT 'All data inserted successfully!';
PRINT '';
PRINT '=== SUMMARY ===';
PRINT 'Plants inserted: 7';
PRINT 'Account Types inserted: 5';
PRINT 'Utilities inserted: ~40';
PRINT 'Norms inserted: ~80';
