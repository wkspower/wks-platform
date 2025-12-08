package com.wks.caseengine.repository;

import com.wks.caseengine.entity.FinancialYearMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialYearMonthRepository extends JpaRepository<FinancialYearMonth, UUID> {

    Optional<FinancialYearMonth> findByMonthAndYear(Integer month, Integer year);

    List<FinancialYearMonth> findByYear(Integer year);
}
