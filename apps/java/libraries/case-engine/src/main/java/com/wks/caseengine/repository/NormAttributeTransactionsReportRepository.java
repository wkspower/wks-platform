package com.wks.caseengine.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.NormAttributeTransactionsReport;

@Repository 
public interface NormAttributeTransactionsReportRepository extends JpaRepository<NormAttributeTransactionsReport, UUID> {
    
    @Query(value = """
        SELECT * FROM NormAttributeTransactionsReport d WHERE d.NormParameter_FK_Id = :normParameterFKId  AND d.AOPMonth = :month 
        AND d.AuditYear = :auditYear AND PlantMaintenanceTransaction_FK_Id IS NULL
    """, nativeQuery = true)
Optional<NormAttributeTransactionsReport> findByNormParameterFKIdAndAOPMonthAndAuditYear(
    @Param ("normParameterFKId") UUID normParameterFKId,
    @Param("month") Integer month, @Param("auditYear") String auditYear);
}
