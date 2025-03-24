package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOP;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.transaction.annotation.Transactional;

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



    List<AOP> findAllByAopYearAndPlantFkId(String year, UUID fromString);
    
    @Query(value = """
 	        SELECT AOP.Id, AOP.AOPCaseId, AOP.AOPStatus, AOP.AOPRemarks, AOP.NormItem, 
 	               AOP.AOPType, AOP.Jan, AOP.Feb, AOP.March, AOP.April, AOP.May, AOP.June, 
 	               AOP.July, AOP.Aug, AOP.Sep, AOP.Oct, AOP.Nov, AOP.Dec, AOP.AOPYear, 
 	               AOP.Plant_FK_Id, AOP.AvgTPH, AOP.NormParameters_FK_Id, NP.DisplayOrder
 	        FROM AOP AOP
 	        JOIN NormParameters NP 
 	        ON AOP.NormParameters_FK_Id = NP.Id 
 	        WHERE AOP.AOPYear = :aopYear 
 	        AND AOP.Plant_FK_Id = :plantFkId 
 	        ORDER BY NP.DisplayOrder
 	        """, nativeQuery = true)
 	    List<Object[]> findByAOPYearAndPlantFkId(@Param("aopYear") String aopYear, @Param("plantFkId") UUID plantFkId);

    
 @Query(value="SELECT *" + 
          "  FROM PlantMaintenanceTransaction pmt " + 
          " join  PlantMaintenance pt on pmt.PlantMaintenance_FK_Id = pt.Id" + 
          " where pt.Plant_FK_Id = :plantId and pmt.AuditYear = :year ", nativeQuery=true)
    List<Object[]> CheckIfMaintainanceDataExists(@Param("plantId") String plantId, @Param("year") String year);



    @Query(value = "SELECT DISTINCT NP.Id " +
            "FROM NormParameters NP " +
            "JOIN NormTypes NT ON NT.Id = NP.NormType_FK_Id " +
            "WHERE NP.Plant_FK_Id = :plantId " +
            "  AND NT.NormName = 'Production' " +
            "  AND NP.NormParameterType_FK_Id IS NOT NULL " +
            "  AND NP.Id NOT IN (" +
            "    SELECT AOP.NormParameters_FK_Id " +
            "    FROM AOP AOP " +
            "    WHERE AOP.Plant_FK_Id = :plantId AND AOP.AOPYear=:year " +
            "      AND AOP.NormParameters_FK_Id IS NOT NULL" +
            ")",
    nativeQuery = true)
List<Object[]> getDataBusinessAllData(@Param("plantId") String plantId,@Param("year") String year);

 
    // Assuming the stored procedure is named 'getEmployeeDetails'
    @Procedure(name = "HMD_MaintenanceCalculation")
    String getEmployeeDetails(Integer employeeId);


    @Procedure(name = "getData")
    String getData();

    @Transactional
    @Query(value = "EXEC getData @plantName = :plantName", nativeQuery = true)
    List<Object[]>  getData(@Param("plantName") String plantName);


    

    @Transactional
    @Query(value = "EXEC HMD_MaintenanceCalculation @plantName = :plantName,@siteName=:siteName,@verticalName=:verticalName,@aopYear=:aopYear ", nativeQuery = true)
    List<Object[]>  HMD_MaintenanceCalculation(@Param("plantName") String plantName, @Param("siteName") String siteName,
    @Param("verticalName") String verticalName,@Param("aopYear") String aopYear );

	
	

}
