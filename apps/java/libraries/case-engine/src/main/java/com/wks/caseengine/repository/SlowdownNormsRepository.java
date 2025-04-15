package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.ShutdownNormsValue;

@Repository
public interface SlowdownNormsRepository extends JpaRepository<ShutdownNormsValue,UUID>{
	
	@Query(value = """
		    SELECT TOP (1000) [Id]
      ,[Site_FK_Id]
      ,[Plant_FK_Id]
      ,[Vertical_FK_Id]
      ,[Material_FK_Id]
      ,[April]
      ,[May]
      ,[June]
      ,[July]
      ,[August]
      ,[September]
      ,[October]
      ,[November]
      ,[December]
      ,[January]
      ,[February]
      ,[March]
      ,[FinancialYear]
      ,[Remarks]
      ,[CreatedOn]
      ,[ModifiedOn]
      ,[MCUVersion]
      ,[UpdatedBy]
      ,[NormParameterTypeId]
      ,[NormParameterTypeName]
      ,[NormParameterTypeDisplayName]
      ,[NormTypeDisplayOrder]
      ,[MaterialDisplayOrder]
      ,[UOM]
  FROM [dbo].[vwScrnShutdownNorms]
		    WHERE Plant_FK_Id = :plantId AND (FinancialYear = :year OR FinancialYear IS NULL) ORDER BY NormTypeDisplayOrder
		    """, nativeQuery = true)
		List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantId") UUID plantId);
		
		@Query(value = "SELECT TOP 1 Id FROM ShutdownNormsValue " +
                "WHERE Plant_FK_Id = :plantId " +
                "AND Site_FK_Id = :siteId " +
                "AND Vertical_FK_Id = :verticalId " +
                "AND Material_FK_Id = :materialId " +
                "AND FinancialYear = :financialYear", 
        nativeQuery = true)
		UUID findIdByFilters(@Param("plantId") UUID plantId,
                      @Param("siteId") UUID siteId,
                      @Param("verticalId") UUID verticalId,
                      @Param("materialId") UUID materialId,
                      @Param("financialYear") String financialYear);

}
