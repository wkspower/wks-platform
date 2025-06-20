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
import java.util.Optional;
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
 	       SELECT Id, NormParameterName, NormParameterDisplayName, NormParameterType_FK_Id, 
		       Material_FK_Id, DisplayName, April, May, June, 
		       July, Aug, Sep, Oct, Nov, Dec,Jan,Feb,March,AvgTPH,AOPRemarks, DisplayOrder, 
		       IsEditable, IsVisible
		FROM vwScrnAOP
		WHERE AOPYear = :AOPYear 
		AND Plant_FK_Id = :Plant_FK_Id 
		AND NormParameterName = :NormParameterName
		ORDER BY DisplayOrder;
 	        """, nativeQuery = true)
 	    List<Object[]> findByAOPYearAndPlantFkId(@Param("AOPYear") String AOPYear, @Param("Plant_FK_Id") UUID Plant_FK_Id,@Param("NormParameterName") String NormParameterName);

    
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
            "    SELECT AOP.Material_FK_Id " +
            "    FROM AOP AOP " +
            "    WHERE AOP.Plant_FK_Id = :plantId AND AOP.AOPYear=:year " +
            "      AND AOP.Material_FK_Id IS NOT NULL" +
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


    

    // @Transactional
    // @Query(value = "EXEC HMD_MaintenanceCalculation @plantName = :plantName,@siteName=:siteName,@verticalName=:verticalName,@aopYear=:aopYear ", nativeQuery = true)
    // List<Object[]>  HMD_MaintenanceCalculation(@Param("plantName") String plantName, @Param("siteName") String siteName,
    // @Param("verticalName") String verticalName,@Param("aopYear") String aopYear );


    @Transactional
    @Query(value = "EXEC MEG_HMD_MaintenanceCalculation @plantId = :plantId,@siteId=:siteId,@verticalId=:verticalId,@aopYear=:aopYear ", nativeQuery = true)
    List<Object[]>  HMD_MaintenanceCalculation(@Param("plantId") String plantName, @Param("siteId") String siteName,
    @Param("verticalId") String verticalName,@Param("aopYear") String aopYear);

    @Query(value = "SELECT TOP 1 Id FROM AOP " +
            "WHERE Site_FK_Id = :siteId " +
            "AND Vertical_FK_Id = :verticalId " +
            "AND Material_FK_Id = :materialId " +
            "AND Plant_FK_Id = :plantId " +
            "AND AOPYear = :aopYear", nativeQuery = true)
    Optional<UUID> findAopIdByFilters(@Param("siteId") UUID siteId,
                               @Param("verticalId") UUID verticalId,
                               @Param("materialId") UUID materialId,
                               @Param("plantId") UUID plantId,
                               @Param("aopYear") String aopYear);
    
    @Query(value = "SELECT DISTINCT [AOPYear], [AOPYear],[currentYear] FROM vwGetAOPYears", nativeQuery = true)
    List<Object[]> getAOPYears();

}
