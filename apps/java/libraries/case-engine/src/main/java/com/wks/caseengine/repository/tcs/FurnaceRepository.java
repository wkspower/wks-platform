package com.wks.caseengine.repository.tcs;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.DummyEntity;

@Repository
public interface FurnaceRepository extends JpaRepository<DummyEntity, UUID> {

    @Query(
        value = """
            EXEC dbo.GetFurnaceData
                @FinancialYear = :financialYear,
                @Site_FK_Id = :siteId,
                @Plant_FK_Id = :plantId
        """,
        nativeQuery = true
    )
    List<FurnaceProjection> getFurnaceData(
        @Param("financialYear") String financialYear,
        @Param("siteId") UUID siteId,
        @Param("plantId") UUID plantId
    );

    // tcs output 
    @Query(
        value = """
            EXEC dbo.GetFurnaceData_Output
                @FinancialYear = :financialYear,
                @Site_FK_Id = :siteId, 
        """,
        nativeQuery = true
    )
    List<FurnaceProjection> getFurnaceOutputData(
        @Param("financialYear") String financialYear,
        @Param("siteId") UUID siteId
      
    );



@Query(
       value = """
            select Id, FinancialYearMonthId, GCalPerHr from Furnace_GCalPerHr_Mapping where FinancialYearMonthId in (:financialYearMonthIds)
        """,
        nativeQuery = true
    )
    List<Object[]> getFurnaceGCalPerHrMapping(
        @Param("financialYearMonthIds") List<UUID> financialYearMonthIds
    ); 
}
