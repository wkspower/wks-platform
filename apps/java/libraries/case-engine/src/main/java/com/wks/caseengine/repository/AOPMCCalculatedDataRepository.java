package com.wks.caseengine.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPMCCalculatedData;


@Repository
public interface AOPMCCalculatedDataRepository extends JpaRepository<AOPMCCalculatedData, UUID>{
	
	
	@Query(value = "SELECT b.*, a.NormParameters_FK_Id as BDNormParametersFKId " +
            "FROM BusinessDemand a " +
            "LEFT JOIN AOPMCCalculatedData b " +
            "ON a.Plant_FK_Id = b.Plant_FK_Id " +
            "AND a.NormParameters_FK_Id = b.NormParameters_FK_Id " +
            "WHERE b.Plant_FK_Id = :plantId and b.Year=:year", 
    nativeQuery = true)
	List<Object[]> findBusinessDemandWithAOPMC(@Param("plantId") String plantId, @Param("year") String year);

//    public List<AOPMCCalculatedData> findAllByYearAndPlantFKId(String year,UUID plantId);

    @Query(value = """
	    SELECT MCU.Site_FK_Id,MCU.Plant_FK_Id,MCU.Material_FK_Id,
	MCU.January,MCU.February,MCU.March,MCU.April,MCU.May,MCU.June,MCU.July,MCU.August,MCU.September,
	MCU.October,MCU.November,MCU.December,MCU.Id,MCU.FinancialYear,MCU.Remarks
	 FROM MCUValue MCU JOIN NormParameters NP ON MCU.Material_FK_Id = NP.Id 
	WHERE MCU.FinancialYear = :year AND MCU.Plant_FK_Id= :plantFkId
	 		    """, nativeQuery = true)
    List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantFkId") UUID plantFkId);
 		
	@Query(value = "SELECT TOP (1000) [Id], [Site_FK_Id], [Plant_FK_Id], [Material_FK_Id], [April], [May], [June], [July], [August], [September], [October], [November], [December], [January], [February], [March], [FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion], [UpdatedBy],[Vertical_FK_Id],[NormParameterDisplayOrder],[ProductName] FROM [dbo].[vwAOPMCValues] WHERE PLANT_FK_ID = :plantId AND FinancialYear = :year order by [NormParameterDisplayOrder]",
	        nativeQuery = true)
	List<Object[]> getDataMCUValuesAllData(@Param("year") String year, @Param("plantId") String plantId);

	@Query(value = "SELECT TOP (1000) [Id], [Material_FK_Id], [MaterialDisplayName], [April], [May], [June], [July], [August], [September], [October], [November], [December], [January], [February], [March], [FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [UpdatedBy],[PlantId] FROM [dbo].[vwAOPMCValuesMaxAchivedCapacity] WHERE PlantId = :plantId AND FinancialYear = :year",
	        nativeQuery = true)
	List<Object[]> getMaxAchievedCapacityData(@Param("year") String year, @Param("plantId") String plantId);
	
	
	@Query(value = "SELECT TOP (1000) [Id], [Material_FK_Id], [MaterialDisplayName], [April], [May], [June], [July], [August], [September], [October], [November], [December], [January], [February], [March], [FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [UpdatedBy],[PlantId] FROM [dbo].[vwAOPMCValuesDesignCapacity] WHERE PlantId = :plantId AND FinancialYear = :year",
    nativeQuery = true)
	List<Object[]> getDesignCapacityData(@Param("year") String year, @Param("plantId") String plantId);

	@Query(value = "SELECT TOP (1000) [Id], [Material_FK_Id], [MaterialDisplayName], [April], [May], [June], [July], [August], [September], [October], [November], [December], [January], [February], [March], [FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [UpdatedBy],[PlantId], [Line_FK_Id] FROM [dbo].[vwAOPMCValuesDesignCapacity] WHERE PlantId = :plantId AND FinancialYear = :year AND Line_FK_Id = :lineId",
		    nativeQuery = true)
			List<Object[]> getLineWiseDesignCapacityData(@Param("year") String year, @Param("plantId") String plantId,@Param("lineId") String lineId);

	@Query(value = "SELECT * FROM MCUValue WHERE Plant_FK_ID = :plantId " +
            "AND FinancialYear = :year " +
            "AND Material_FK_ID = :materialId", nativeQuery = true)
Optional<AOPMCCalculatedData> findByPlantYearAndMaterial(
     @Param("plantId") UUID plantId, 
     @Param("year") String year, 
     @Param("materialId") UUID materialId);

 }
