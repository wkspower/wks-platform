package com.wks.caseengine.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.NormsMonthDetail;

@Repository
public interface NormsMonthDetailRepository
        extends JpaRepository<NormsMonthDetail, UUID> {

    /**
     * Find a specific month detail by composite key
     * 
     * @param normsHeaderFkId        The norms header foreign key
     * @param financialYearMonthFkId The financial year month foreign key
     * @return Optional containing the found record or empty
    */

    Optional<NormsMonthDetail> findByNormsHeaderFkIdAndFinancialYearMonthFkId(
            UUID normsHeaderFkId,
            UUID financialYearMonthFkId);

}
