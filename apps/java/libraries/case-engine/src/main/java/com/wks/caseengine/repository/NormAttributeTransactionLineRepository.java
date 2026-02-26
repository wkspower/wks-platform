package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.NormAttributeTransactionLine;

@Repository
public interface NormAttributeTransactionLineRepository
        extends JpaRepository<NormAttributeTransactionLine, UUID> {

    @Query("""
                SELECT n FROM NormAttributeTransactionLine n
                WHERE n.aopYear = :year
                AND n.plantFkId = :plantId
                AND n.gradeFkId = :gradeId
                AND n.lineFkId = :lineId
            """)
    NormAttributeTransactionLine findExisting(
            @Param("year") String year,
            @Param("plantId") UUID plantId,
            @Param("gradeId") UUID gradeId,
            @Param("lineId") UUID lineId);
}
