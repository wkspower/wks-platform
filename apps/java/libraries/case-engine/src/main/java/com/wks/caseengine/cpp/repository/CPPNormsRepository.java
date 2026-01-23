package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.cpp.entity.CPPNorms;

@Repository
public interface CPPNormsRepository extends JpaRepository<CPPNorms, UUID> {

    Optional<CPPNorms> findByNormsHeaderFkIdAndFinancialYear(UUID normsHeaderFkId, String financialYear);

    List<CPPNorms> findByFinancialYear(String financialYear);

    @Procedure(name = "CPP_UpdateCPPNorms")
    void updateCPPNorms(
        @Param("Id") UUID id,
        @Param("NormsHeaderFkId") UUID normsHeaderFkId,
        @Param("FinancialYear") String financialYear,
        @Param("AOPYear") String aopYear,
        @Param("NormTypeFkId") Integer normTypeFkId,
        @Param("Apr_Norms") java.math.BigDecimal aprNorms,
        @Param("May_Norms") java.math.BigDecimal mayNorms,
        @Param("Jun_Norms") java.math.BigDecimal junNorms,
        @Param("Jul_Norms") java.math.BigDecimal julNorms,
        @Param("Aug_Norms") java.math.BigDecimal augNorms,
        @Param("Sep_Norms") java.math.BigDecimal sepNorms,
        @Param("Oct_Norms") java.math.BigDecimal octNorms,
        @Param("Nov_Norms") java.math.BigDecimal novNorms,
        @Param("Dec_Norms") java.math.BigDecimal decNorms,
        @Param("Jan_Norms") java.math.BigDecimal janNorms,
        @Param("Feb_Norms") java.math.BigDecimal febNorms,
        @Param("Mar_Norms") java.math.BigDecimal marNorms,
        @Param("Remarks") String remarks,
        @Param("ModifiedBy") String modifiedBy
    );
}
