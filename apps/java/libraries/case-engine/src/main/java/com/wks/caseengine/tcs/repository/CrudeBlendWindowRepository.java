package com.wks.caseengine.tcs.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.dto.VGOVRDropProjection;
import com.wks.caseengine.tcs.dto.CrudeBlendProjection;
import com.wks.caseengine.tcs.dto.CrudeSpecificConstraintsProjection;
import com.wks.caseengine.entity.DummyEntity;

@org.springframework.stereotype.Repository
public interface CrudeBlendWindowRepository extends JpaRepository<DummyEntity, Long> {
 // fetch crude blend data 
   @Query(
    value = """
        SELECT Id, Property, Stream, Unit,
               MinValue, MaxValue, Criticality,
               Remarks, Type
        FROM CrudeBlendWindow
        WHERE Plant_FK_Id = :plantId
          AND Site_FK_Id = :siteId
          AND FinancialYear = :financialYear
        """,
    nativeQuery = true
)
     List<CrudeBlendProjection> findCrudeBlendByPlantIdAndSiteId(@Param("plantId") UUID plantId, @Param("siteId") UUID siteId, @Param("financialYear") String financialYear);


     // tcs output 

     @Query(
      value = """
          SELECT Id, Property, Stream, Unit,
                 MinValue, MaxValue, Criticality,
                 Remarks, Type
          FROM CrudeBlendWindow
          WHERE 
             Site_FK_Id = :siteId
            AND FinancialYear = :financialYear
          """,
      nativeQuery = true
  )
       List<CrudeBlendProjection> findCrudeBlendBySiteId(@Param("siteId") UUID siteId, @Param("financialYear") String financialYear);

     // fetch crude specific constraints data
     @Query(
        value = """
            SELECT Id, Crude, MaxBlendLimit, Reasons
            FROM CrudeSpecificConstraints
            WHERE Plant_FK_Id = :plantId
              AND Site_FK_Id = :siteId
              AND FinancialYear = :financialYear
            """,
        nativeQuery = true
    )
     List<CrudeSpecificConstraintsProjection> findCrudeSpecificConstraintsByPlant_FK_IdAndSite_FK_Id(@Param("plantId") UUID plantId, @Param("siteId") UUID siteId, @Param("financialYear") String financialYear);


     // tcs output  for crude specific constraints

     @Query(
      value = """
          SELECT Id, Crude, MaxBlendLimit, Reasons
          FROM CrudeSpecificConstraints
          WHERE 
            Site_FK_Id = :siteId
            AND FinancialYear = :financialYear
          """,
      nativeQuery = true
  )
   List<CrudeSpecificConstraintsProjection> findCrudeSpecificConstraintsBySite_FK_Id(@Param("siteId") UUID siteId, @Param("financialYear") String financialYear);



      @Query(
         value = """
              SELECT Id, kbpsd, value_345, Remarks
              FROM VGOVRDrop
              WHERE Plant_FK_Id = :plantId
                AND Site_FK_Id = :siteId
                AND FinancialYear = :financialYear
              """,
         nativeQuery = true )

     List<VGOVRDropProjection> findVGOVRDropByPlant_FK_IdAndSite_FK_Id(@Param("plantId") UUID plantId, @Param("siteId") UUID siteId, @Param("financialYear") String financialYear);

     // tcs output  for vgovr drop
     @Query(
      value = """
          SELECT Id, kbpsd, value_345, Remarks
          FROM VGOVRDrop
          WHERE 
            Site_FK_Id = :siteId
            AND FinancialYear = :financialYear
          """,
      nativeQuery = true
  )
   List<VGOVRDropProjection> findVGOVRDropBySite_FK_Id(@Param("siteId") UUID siteId, @Param("financialYear") String financialYear);


}


