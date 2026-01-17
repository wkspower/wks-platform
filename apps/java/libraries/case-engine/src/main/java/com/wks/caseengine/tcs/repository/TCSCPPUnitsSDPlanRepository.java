package com.wks.caseengine.tcs.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.tcs.dto.TCSCPPUnitsSDPlanProjection;
import com.wks.caseengine.entity.DummyEntity;

@org.springframework.stereotype.Repository
public interface TCSCPPUnitsSDPlanRepository extends JpaRepository<DummyEntity, Long> {

    @Query(value = "SELECT Id, Machine, IBRDueDate, GTMaintenance, NoOfDays, ShutDownDate, StartUpDate, MajorJobs FROM TCS_CPPUnitsSD_Plan WHERE FinancialYear = :financialYear and Site_FK_Id = :siteId", nativeQuery = true)
    List<TCSCPPUnitsSDPlanProjection> findByFinancialYearAndSiteId(@Param("financialYear") String financialYear, @Param("siteId") UUID siteId);

//     @Query("""
//     SELECT t
//     FROM TCS_CPPUnitsSD_Plan t
//     WHERE t.financialYear = :financialYear
//       AND t.Site_FK_Id = :siteId
// """)
//     List<TCSCPPUnitsSDPlanProjection> findByFinancialYearAndSiteId(@Param("financialYear") String financialYear, @Param("siteId") UUID siteId);
}


