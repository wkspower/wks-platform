package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cpp.dto.IntersiteSteamTransferProjection;
import com.wks.caseengine.cpp.entity.CPPIntersiteSteamTransfer;

@Repository
@Transactional
public interface IntersiteSteamTransferRepository
        extends JpaRepository<CPPIntersiteSteamTransfer, UUID> {

    // ── GET (native query from the user's SQL) ──────────────────────────
    @Query(value =
        "SELECT " +
        "    cist.Id AS id, " +
        "    p.Name AS cppPlantName, " +
        "    p.PlantCode AS cppPlantCode, " +
        "    np.Name AS normParameterName, " +
        "    np.SAPMaterialCode AS sapMaterialCode, " +
        "    np.UOM AS uom, " +
        "    sp.Name AS senderPlantName, " +
        "    sp.PlantCode AS senderPlantCode, " +
        "    sccm.CostCenterName AS senderCostCenterName, " +
        "    sccm.CostCenterCode AS senderCostCenterCode, " +
        "    rp.Name AS receiverPlantName, " +
        "    rp.PlantCode AS receiverPlantCode, " +
        "    rccm.CostCenterName AS receiverCostCenterName, " +
        "    rccm.CostCenterCode AS receiverCostCenterCode, " +
        "    cist.AOP_Year AS aopYear, " +
        "    cist.Min_Apr AS minApr, " +
        "    cist.Max_Apr AS maxApr, " +
        "    cist.Min_May AS minMay, " +
        "    cist.Max_May AS maxMay, " +
        "    cist.Min_Jun AS minJun, " +
        "    cist.Max_Jun AS maxJun, " +
        "    cist.Min_Jul AS minJul, " +
        "    cist.Max_Jul AS maxJul, " +
        "    cist.Min_Aug AS minAug, " +
        "    cist.Max_Aug AS maxAug, " +
        "    cist.Min_Sep AS minSep, " +
        "    cist.Max_Sep AS maxSep, " +
        "    cist.Min_Oct AS minOct, " +
        "    cist.Max_Oct AS maxOct, " +
        "    cist.Min_Nov AS minNov, " +
        "    cist.Max_Nov AS maxNov, " +
        "    cist.Min_Dec AS minDec, " +
        "    cist.Max_Dec AS maxDec, " +
        "    cist.Min_Jan AS minJan, " +
        "    cist.Max_Jan AS maxJan, " +
        "    cist.Min_Feb AS minFeb, " +
        "    cist.Max_Feb AS maxFeb, " +
        "    cist.Min_Mar AS minMar, " +
        "    cist.Max_Mar AS maxMar, " +
        "    cist.Remarks AS remarks, " +
        "    cist.CPPPlant_FK_Id AS cppPlantFkId, " +
        "    cist.NormParameter_FK_Id AS normParameterFkId, " +
        "    cist.SenderPlant_FK_Id AS senderPlantFkId, " +
        "    cist.SenderCostCenter_FK_Id AS senderCostCenterFkId, " +
        "    cist.ReceiverPlant_FK_Id AS receiverPlantFkId, " +
        "    cist.ReceiverCostCenter_FK_Id AS receiverCostCenterFkId " +
        "FROM dbo.CPP_IntersiteSteamTransfer AS cist " +
        "LEFT JOIN dbo.Plants AS p ON p.Id = cist.CPPPlant_FK_Id " +
        "LEFT JOIN dbo.NormParameters AS np ON np.Id = cist.NormParameter_FK_Id " +
        "LEFT JOIN dbo.Plants AS sp ON sp.Id = cist.SenderPlant_FK_Id " +
        "LEFT JOIN dbo.CPPCostCentersMaster AS sccm ON sccm.CostCenterId = cist.SenderCostCenter_FK_Id " +
        "LEFT JOIN dbo.Plants AS rp ON rp.Id = cist.ReceiverPlant_FK_Id " +
        "LEFT JOIN dbo.CPPCostCentersMaster AS rccm ON rccm.CostCenterId = cist.ReceiverCostCenter_FK_Id " +
        "WHERE cist.AOP_Year = :financialYear " +
        "  AND cist.CPPPlant_FK_Id IN (" +
        "      SELECT TRY_CAST(value AS UNIQUEIDENTIFIER) FROM STRING_SPLIT(:plantIds, ',') " +
        "  )",
        nativeQuery = true)
    List<IntersiteSteamTransferProjection> getIntersiteSteamTransfer(
            @Param("plantIds") String plantIds,
            @Param("financialYear") String financialYear);

    // ── GET (by financial year only — common for all plants) ───────────
    @Query(value =
        "SELECT " +
        "    cist.Id AS id, " +
        "    p.Name AS cppPlantName, " +
        "    p.PlantCode AS cppPlantCode, " +
        "    np.Name AS normParameterName, " +
        "    np.SAPMaterialCode AS sapMaterialCode, " +
        "    np.UOM AS uom, " +
        "    sp.Name AS senderPlantName, " +
        "    sp.PlantCode AS senderPlantCode, " +
        "    sccm.CostCenterName AS senderCostCenterName, " +
        "    sccm.CostCenterCode AS senderCostCenterCode, " +
        "    rp.Name AS receiverPlantName, " +
        "    rp.PlantCode AS receiverPlantCode, " +
        "    rccm.CostCenterName AS receiverCostCenterName, " +
        "    rccm.CostCenterCode AS receiverCostCenterCode, " +
        "    cist.AOP_Year AS aopYear, " +
        "    cist.Min_Apr AS minApr, " +
        "    cist.Max_Apr AS maxApr, " +
        "    cist.Min_May AS minMay, " +
        "    cist.Max_May AS maxMay, " +
        "    cist.Min_Jun AS minJun, " +
        "    cist.Max_Jun AS maxJun, " +
        "    cist.Min_Jul AS minJul, " +
        "    cist.Max_Jul AS maxJul, " +
        "    cist.Min_Aug AS minAug, " +
        "    cist.Max_Aug AS maxAug, " +
        "    cist.Min_Sep AS minSep, " +
        "    cist.Max_Sep AS maxSep, " +
        "    cist.Min_Oct AS minOct, " +
        "    cist.Max_Oct AS maxOct, " +
        "    cist.Min_Nov AS minNov, " +
        "    cist.Max_Nov AS maxNov, " +
        "    cist.Min_Dec AS minDec, " +
        "    cist.Max_Dec AS maxDec, " +
        "    cist.Min_Jan AS minJan, " +
        "    cist.Max_Jan AS maxJan, " +
        "    cist.Min_Feb AS minFeb, " +
        "    cist.Max_Feb AS maxFeb, " +
        "    cist.Min_Mar AS minMar, " +
        "    cist.Max_Mar AS maxMar, " +
        "    cist.Remarks AS remarks, " +
        "    cist.CPPPlant_FK_Id AS cppPlantFkId, " +
        "    cist.NormParameter_FK_Id AS normParameterFkId, " +
        "    cist.SenderPlant_FK_Id AS senderPlantFkId, " +
        "    cist.SenderCostCenter_FK_Id AS senderCostCenterFkId, " +
        "    cist.ReceiverPlant_FK_Id AS receiverPlantFkId, " +
        "    cist.ReceiverCostCenter_FK_Id AS receiverCostCenterFkId " +
        "FROM dbo.CPP_IntersiteSteamTransfer AS cist " +
        "LEFT JOIN dbo.Plants AS p ON p.Id = cist.CPPPlant_FK_Id " +
        "LEFT JOIN dbo.NormParameters AS np ON np.Id = cist.NormParameter_FK_Id " +
        "LEFT JOIN dbo.Plants AS sp ON sp.Id = cist.SenderPlant_FK_Id " +
        "LEFT JOIN dbo.CPPCostCentersMaster AS sccm ON sccm.CostCenterId = cist.SenderCostCenter_FK_Id " +
        "LEFT JOIN dbo.Plants AS rp ON rp.Id = cist.ReceiverPlant_FK_Id " +
        "LEFT JOIN dbo.CPPCostCentersMaster AS rccm ON rccm.CostCenterId = cist.ReceiverCostCenter_FK_Id " +
        "WHERE cist.AOP_Year = :financialYear",
        nativeQuery = true)
    List<IntersiteSteamTransferProjection> getIntersiteSteamTransferByYear(
            @Param("financialYear") String financialYear);

    // ── Carry-forward: find raw entities for a given AOP year ───────────
    List<CPPIntersiteSteamTransfer> findByAopYear(String aopYear);

    // ── SAVE (update min/max month columns + remarks by Id) ─────────────
    @Modifying
    @Transactional
    @Query(value =
        "UPDATE dbo.CPP_IntersiteSteamTransfer SET " +
        "  Min_Apr = :minApr, Max_Apr = :maxApr, " +
        "  Min_May = :minMay, Max_May = :maxMay, " +
        "  Min_Jun = :minJun, Max_Jun = :maxJun, " +
        "  Min_Jul = :minJul, Max_Jul = :maxJul, " +
        "  Min_Aug = :minAug, Max_Aug = :maxAug, " +
        "  Min_Sep = :minSep, Max_Sep = :maxSep, " +
        "  Min_Oct = :minOct, Max_Oct = :maxOct, " +
        "  Min_Nov = :minNov, Max_Nov = :maxNov, " +
        "  Min_Dec = :minDec, Max_Dec = :maxDec, " +
        "  Min_Jan = :minJan, Max_Jan = :maxJan, " +
        "  Min_Feb = :minFeb, Max_Feb = :maxFeb, " +
        "  Min_Mar = :minMar, Max_Mar = :maxMar, " +
        "  Remarks = :remarks, UpdatedDate = GETDATE() " +
        "WHERE Id = :id",
        nativeQuery = true)
    int updateMonthValues(
            @Param("id")      UUID   id,
            @Param("minApr")  Double minApr,
            @Param("maxApr")  Double maxApr,
            @Param("minMay")  Double minMay,
            @Param("maxMay")  Double maxMay,
            @Param("minJun")  Double minJun,
            @Param("maxJun")  Double maxJun,
            @Param("minJul")  Double minJul,
            @Param("maxJul")  Double maxJul,
            @Param("minAug")  Double minAug,
            @Param("maxAug")  Double maxAug,
            @Param("minSep")  Double minSep,
            @Param("maxSep")  Double maxSep,
            @Param("minOct")  Double minOct,
            @Param("maxOct")  Double maxOct,
            @Param("minNov")  Double minNov,
            @Param("maxNov")  Double maxNov,
            @Param("minDec")  Double minDec,
            @Param("maxDec")  Double maxDec,
            @Param("minJan")  Double minJan,
            @Param("maxJan")  Double maxJan,
            @Param("minFeb")  Double minFeb,
            @Param("maxFeb")  Double maxFeb,
            @Param("minMar")  Double minMar,
            @Param("maxMar")  Double maxMar,
            @Param("remarks") String remarks);
}
