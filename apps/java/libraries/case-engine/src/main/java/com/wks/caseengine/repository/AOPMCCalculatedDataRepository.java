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

    @Query(value = """
 		    SELECT AMC.Site, AMC.Plant, AMC.Material, AMC.January, AMC.February, AMC.March, AMC.April, AMC.May, AMC.June, AMC.July, 
 		           AMC.August, AMC.September, AMC.October, AMC.November, AMC.December, 
 		            AMC.Plant_FK_Id, AMC.Id, AMC.Year, AMC.NormParameters_FK_Id, 
 		           NP.DiplayOrder 
 		    FROM AOPMCCalculatedData AMC 
 		    JOIN NormParameters NP 
 		    ON AMC.NormParameters_FK_Id = NP.Id 
 		    WHERE AMC.Year = :year AND AMC.Plant_FK_Id = :plantFkId 
 		    ORDER BY NP.DiplayOrder
 		    """, nativeQuery = true)
 		List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantFkId") UUID plantFkId);
 		
 		@Query(value="select distinct NormParameters_FK_Id from BusinessDemand where Plant_FK_Id = :plantId and Year=:year "+
 			   " and NormParameters_FK_Id not in (select NormParameters_FK_Id from AOPMCCalculatedData where Plant_FK_Id= :plantId and NormParameters_FK_Id is not null and Year=:year) ", nativeQuery=true)
 			   List<Object[]> getDataBusinessAllData(@Param("plantId") String plantId, @Param("year") String year);
 			 

}
