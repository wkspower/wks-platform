package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.GradeShutdownNormsValue;
import com.wks.caseengine.entity.ShutdownNormsValue;

@Repository
public interface GradeShutdownNormsValueRepository extends JpaRepository<GradeShutdownNormsValue,UUID>{
	
	@Query(value = "SELECT TOP 1 Id FROM GradeShutdownNormsValue " +
            "WHERE Plant_FK_Id = :plantId " +
            "AND Site_FK_Id = :siteId " +
            "AND Vertical_FK_Id = :verticalId " +
            "AND Material_FK_Id = :materialId " +
            "AND Grade_FK_Id = :gradeId " +
            "AND FinancialYear = :financialYear", 
    nativeQuery = true)
	UUID findIdByFilters(@Param("plantId") UUID plantId,
                  @Param("siteId") UUID siteId,
                  @Param("verticalId") UUID verticalId,
                  @Param("materialId") UUID materialId,
                  @Param("financialYear") String financialYear,@Param("gradeId") UUID gradeId);
	
	@Query(
		      value = "SELECT * FROM dbo.GradeShutdownNormsValue WHERE Plant_FK_Id = :plantId and FinancialYear = :FinancialYear", 
		      nativeQuery = true
		    )
		    List<GradeShutdownNormsValue> findByPlantFkIdAndFinancialYear(@Param("plantId") UUID plantId,@Param("FinancialYear") String FinancialYear);

	
}
