package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.dto.FinancialYearMonthProjection;
import com.wks.caseengine.dto.FixedConsumptionProjection;
import com.wks.caseengine.entity.DummyEntity;

import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface FixedConsumptionRepository extends JpaRepository<DummyEntity, Long> {
   // @Transactional
    @Query(value = "EXEC GetFixedConsumptionByPlant :plantId, :financialYear", nativeQuery = true)
    List<FixedConsumptionProjection> getFixedConsumption(@Param("plantId") UUID plantId, @Param("financialYear") String financialYear);

    @Query(value = "SELECT Id, Year, Month FROM FinancialYearMonth", nativeQuery = true)
    List<FinancialYearMonthProjection> getFinancialYearMonth();

    @Query(value = "SELECT CostCenterId FROM CPPCostCenters WHERE CostCenterCode IN :costCenterCodes", nativeQuery = true)
    List<String> getCostCenterIds(@Param("costCenterCodes") List<String> costCenterCodes);

    @Query(value = "SELECT Id FROM NormParameters np WHERE np.Id IN ( Select NormParameterFK_Id from CostCenterNormParameterMapping map where map.CostCenterFK_Id IN :costCenterIds)", nativeQuery = true)
    List<String> getNormParameterIds(@Param("costCenterIds") List<String> costCenterIds);

    @Query(value = "SELECT Id from UtilityFixedConsumption  WHERE CostCenter_FK_Id IN :costCenterIds AND NormParameter_FK_Id = :normParameterId", nativeQuery = true)
    List<String> getUtilityFixedConsumptionIds(@Param("costCenterIds") List<String> costCenterIds, @Param("normParameterId") String normParameterId);

   @Modifying
   @Transactional
   @Query(value = "UPDATE UtilityFixedConsumption SET ConsumptionValue = :consumptionValue WHERE Id IN :utilityFixedConsumptionIds AND FinancialYearMonth_FK_Id = :financialYearMonthId", nativeQuery = true)
   void updateUtilityFixedConsumption(@Param("consumptionValue") Double consumptionValue, @Param("utilityFixedConsumptionIds") List<String> utilityFixedConsumptionIds, @Param("financialYearMonthId") String financialYearMonthId);

   //get FinancialYearMonthIds for UtilityFixedConsumptionIds
   @Query(value = "SELECT FinancialYearMonth_FK_Id FROM UtilityFixedConsumption WHERE Id IN :utilityFixedConsumptionIds", nativeQuery = true)
   List<String> getFinancialYearMonthIdsForUtilityFixedConsumptionIds(@Param("utilityFixedConsumptionIds") List<String> utilityFixedConsumptionIds);

   // data entry for missing month
   // @Modifying
   // @Transactional                                                                                                                    
   // @Query(value = "INSERT INTO UtilityFixedConsumption (FinancialYearMonth_FK_Id, NormParameter_FK_Id, CostCenter_FK_Id, ConsumptionValue) VALUES (:financialYearMonthId, :normParameterId, :costCenterId, :consumptionValue)", nativeQuery = true)
   // void insertUtilityFixedConsumption(@Param("financialYearMonthId") String financialYearMonthId, @Param("normParameterId") String normParameterId, @Param("costCenterId") String costCenterId, @Param("consumptionValue") Double consumptionValue);

   
   // @Modifying
   // @Transactional
   // @Query(value = "INSERT INTO UtilityFixedConsumption " +
   //         "(FinancialYearMonth_FK_Id, NormParameter_FK_Id, CostCenter_FK_Id, ConsumptionValue) " +
   //         "OUTPUT INSERTED.Id " +
   //         "VALUES (:financialYearMonthId, :normParameterId, :costCenterId, :consumptionValue)",
   //         nativeQuery = true)
   // String insertUtilityFixedConsumption(
   //         @Param("financialYearMonthId") String financialYearMonthId,
   //         @Param("normParameterId") String normParameterId,
   //         @Param("costCenterId") String costCenterId,
   //         @Param("consumptionValue") Double consumptionValue);
   


 @Transactional
@Query(value = 
    "INSERT INTO UtilityFixedConsumption " +
    "(FinancialYearMonth_FK_Id, NormParameter_FK_Id, CostCenter_FK_Id, ConsumptionValue) " +
    "OUTPUT INSERTED.Id " +
    "SELECT :financialYearMonthId, :normParameterId, :costCenterId, :consumptionValue",
    nativeQuery = true)
String insertUtilityFixedConsumption(
        @Param("financialYearMonthId") String financialYearMonthId,
        @Param("normParameterId") String normParameterId,
        @Param("costCenterId") String costCenterId,
        @Param("consumptionValue") Double consumptionValue);


     
    
}

