package com.wks.caseengine.repository;

import com.wks.caseengine.entity.FinancialYearMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialYearMonthRepository extends JpaRepository<FinancialYearMonth, UUID> {

    Optional<FinancialYearMonth> findByMonthAndYear(Integer month, Integer year);

    List<FinancialYearMonth> findByYear(Integer year);

    @Query(value = """
        SELECT Id
        FROM FinancialYearMonth
        WHERE Year = :year AND Month = :month
        """, nativeQuery = true)
    UUID findFinancialMonthId(
            @Param("year") int year,
            @Param("month") int month
    );
}