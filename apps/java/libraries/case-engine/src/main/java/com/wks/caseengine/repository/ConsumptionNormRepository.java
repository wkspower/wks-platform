package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.entity.AOPConsumptionNorm;

@Repository
public interface ConsumptionNormRepository extends JpaRepository<AOPConsumptionNorm,UUID>{
	
	@Query(value = """
		    SELECT 
		        acn.Id,
		        acn.Site_FK_Id,
		        acn.Vertical_FK_Id,
		        acn.AOPCaseId,
		        acn.AOPStatus,
		        acn.AOPRemarks,
		        acn.Material_FK_Id,
		        acn.Jan,
		        acn.Feb,
		        acn.March,
		        acn.April,
		        acn.May,
		        acn.June,
		        acn.July,
		        acn.Aug,
		        acn.Sep,
		        acn.Oct,
		        acn.Nov,
		        acn.Dec,
		        acn.AOPYear,
		        acn.Plant_FK_Id,
		        npt.DisplayName AS NormParameterType_DisplayName,np.UOM
		    FROM AOPConsumptionNorm acn
		    JOIN NormParameters np ON acn.Material_FK_Id = np.Id
		    JOIN NormParameterType npt ON np.NormParameterType_FK_Id = npt.Id
		    WHERE acn.Plant_FK_Id = :plantFkId 
		    AND acn.AOPYear = :aopYear ORDER BY npt.DisplayOrder
		    """, nativeQuery = true)
		List<Object[]> findByPlantFkIdAndAopYear(@Param("plantFkId") UUID plantFkId, @Param("aopYear") String aopYear);

	@Modifying
		@Transactional
    @Query(value = "EXEC MEG_HMD_CalculateConsumptionAOPValues :finYear", nativeQuery = true)
		int calculateExpressionConsumptionNorms(@Param("finYear") String finYear);


		@Query(value = "SELECT TOP 1 Id FROM AOPConsumptionNorm " +
                "WHERE Plant_FK_Id = :plantId " +
                "AND Site_FK_Id = :siteId " +
                "AND Vertical_FK_Id = :verticalId " +
                "AND Material_FK_Id = :materialId " +
                "AND AOPYear = :financialYear", 
        nativeQuery = true)
		UUID findIdByFilters(@Param("plantId") UUID plantId,
                      @Param("siteId") UUID siteId,
                      @Param("verticalId") UUID verticalId,
                      @Param("materialId") UUID materialId,
                      @Param("financialYear") String financialYear);

}



