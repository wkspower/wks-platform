package com.wks.caseengine.repository.tcs;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.dto.tcs.PCGOutlookProjection;
import com.wks.caseengine.entity.DummyEntity;

import jakarta.transaction.Transactional;

@org.springframework.stereotype.Repository
@Transactional
public interface PCGOutlookRepository extends JpaRepository<DummyEntity, Long> {

    @Query(
        value = """
            EXEC Get_TCS_PCGOutlook
                @Site_FK_Id = :siteId,
                @FinancialYear = :financialYear
        """,
        nativeQuery = true
    )
    List<PCGOutlookProjection> getPcgOutlookBySiteAndFY(
            @Param("siteId") UUID siteId,
            @Param("financialYear") String financialYear
    );
 
    @Query(
        value = """
            select FinancialYearMonthId from TCS_PCGOutlook where Site_FK_Id = :siteId and FinancialYearMonthId in ( :financialYearMonthIds )
        """,
        nativeQuery = true
    )
    List<UUID> getPcgOutlookFinancialYearMonthIdsBySiteAndFY(
        @Param("siteId") UUID siteId,
        @Param("financialYearMonthIds") List<UUID> financialYearMonthIds
    );
    
 
}
