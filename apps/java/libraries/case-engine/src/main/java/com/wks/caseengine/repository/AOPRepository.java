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


    @Query(value="select distinct [NormParameters_FK_Id] from BusinessDemand where Plant_FK_Id = :plantId and Year=:year "+
    " and [NormParameters_FK_Id] not in (select [NormParameters_FK_Id] from [dbo].[AOP] where Plant_FK_Id= :plantId and [NormParameters_FK_Id] is not null and Year=:year) ", nativeQuery=true)
    List<Object[]> getDataBusinessAllData(@Param("plantId") String plantId, @Param("year") String year);


    


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
