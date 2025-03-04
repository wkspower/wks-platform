package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOP;

@Repository
public interface AOPRepository extends JpaRepository<AOP, UUID>{
	
	@Query(value = "SELECT b.*, a.NormParameters_FK_Id as BDNormParametersFKId " +
            "FROM BusinessDemand a " +
            "LEFT JOIN AOP b " +
            "ON a.Plant_FK_Id = b.Plant_FK_Id " +
            "AND a.NormParameters_FK_Id = b.NormParameters_FK_Id " +
            "WHERE b.Plant_FK_Id = :plantId and b.Year=:year", 
    nativeQuery = true)
	List<Object[]> findBusinessDemandWithAOP(@Param("plantId") UUID plantId, @Param("year") String year);


	@Query(value = """
	        SELECT AOP.Id, AOP.AOPCaseId, AOP.AOPStatus, AOP.AOPRemarks, AOP.NormItem, 
	               AOP.AOPType, AOP.Jan, AOP.Feb, AOP.March, AOP.April, AOP.May, AOP.June, 
	               AOP.July, AOP.Aug, AOP.Sep, AOP.Oct, AOP.Nov, AOP.Dec, AOP.AOPYear, 
	               AOP.Plant_FK_Id, AOP.AvgTPH, AOP.NormParameters_FK_Id, NP.DiplayOrder
	        FROM AOP AOP
	        JOIN NormParameters NP 
	        ON AOP.NormParameters_FK_Id = NP.Id 
	        WHERE AOP.AOPYear = :aopYear 
	        AND AOP.Plant_FK_Id = :plantFkId 
	        ORDER BY NP.DiplayOrder
	        """, nativeQuery = true)
	    List<Object[]> findByAOPYearAndPlantFkId(@Param("aopYear") String aopYear, @Param("plantFkId") UUID plantFkId);
	
    List<AOP> findAllByAopYearAndPlantFkId(String year, UUID fromString);


    @Query(value="select distinct NormParameters_FK_Id from BusinessDemand where Plant_FK_Id = :plantId and Year=:year "+
    " and NormParameters_FK_Id not in (select NormParameters_FK_Id from AOP where Plant_FK_Id= :plantId and NormParameters_FK_Id is not null and Year=:year) ", nativeQuery=true)
    List<Object[]> getDataBusinessAllData(@Param("plantId") String plantId, @Param("year") String year);
	
	

}
