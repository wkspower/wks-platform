package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.BusinessDemand;

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

    public List<AOPMCCalculatedData> findAllByYearAndPlantFKId(String year,UUID plantId);

    // @Query(value = """
 	// 	    SELECT AMC.Site, AMC.Plant, AMC.Material, AMC.January, AMC.February, AMC.March, AMC.April, AMC.May, AMC.June, AMC.July, 
 	// 	           AMC.August, AMC.September, AMC.October, AMC.November, AMC.December, 
 	// 	            AMC.Plant_FK_Id, AMC.Id, AMC.Year, AMC.NormParameters_FK_Id, 
 	// 	           NP.DisplayOrder, AMC.Remark
 	// 	    FROM AOPMCCalculatedData AMC 
 	// 	    JOIN NormParameters NP 
 	// 	    ON AMC.NormParameters_FK_Id = NP.Id 
 	// 	    WHERE AMC.Year = :year AND AMC.Plant_FK_Id = :plantFkId 
 	// 	    ORDER BY NP.DisplayOrder
 	// 	    """, nativeQuery = true)
 	// 	List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantFkId") UUID plantFkId);


     @Query(value = """
	    SELECT MCU.Site_FK_Id,MCU.Plant_FK_Id,MCU.Material_FK_Id,
MCU.January,MCU.February,MCU.March,MCU.April,MCU.May,MCU.June,MCU.July,MCU.August,MCU.September,
MCU.October,MCU.November,MCU.December,MCU.Id,MCU.FinancialYear,MCU.Remarks
 FROM MCUValue MCU JOIN NormParameters NP ON MCU.Material_FK_Id = NP.Id 
WHERE MCU.FinancialYear = :year AND MCU.Plant_FK_Id= :plantFkId
	    """, nativeQuery = true)
	List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantFkId") UUID plantFkId);
 		
 		@Query(value = "SELECT DISTINCT NP.Id " +
                "FROM NormParameters NP " +
                "JOIN NormTypes NT ON NT.Id = NP.NormType_FK_Id " +
                "WHERE NP.Plant_FK_Id = :plantId " +
                "  AND NT.NormName = 'Production' " +
                "  AND NP.NormParameterType_FK_Id IS NOT NULL " +
                "  AND NP.Id NOT IN (" +
                "    SELECT AOP.NormParameters_FK_Id " +
                "    FROM AOPMCCalculatedData AOP " +
                "    WHERE AOP.Plant_FK_Id = :plantId AND AOP.Year=:year " +
                "      AND AOP.NormParameters_FK_Id IS NOT NULL" +
                ")",
        nativeQuery = true)
 List<Object[]> getDataBusinessAllData(@Param("plantId") String plantId,@Param("year") String year);

 			   
 			  

 			 

}
