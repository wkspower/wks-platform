package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.CalculatedProcessDemand;

@Repository
public interface CalculatedProcessDemandRepository extends JpaRepository<CalculatedProcessDemand, UUID> {

    List<CalculatedProcessDemand> findByFinancialYearOrderByProcessPlant(String financialYear);

    @Query(value = "EXEC dbo.GetProcessDemandByYear @FinancialYear = :financialYear", nativeQuery = true)
    List<Object[]> getProcessDemandByYear(@Param("financialYear") String financialYear);

}
