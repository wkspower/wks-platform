package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.MCUNormsValue;

@Repository
public interface ShutdownNormsRepository extends JpaRepository<MCUNormsValue,UUID>{
	
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
  FROM [dbo].[vwScrnShutdownNorms]
		    WHERE Plant_FK_Id = :plantId AND (FinancialYear = :year OR FinancialYear IS NULL)
		    """, nativeQuery = true)
		List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantId") UUID plantId);


}
