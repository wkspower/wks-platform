package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.BusinessDemand;

@Repository
public interface BusinessDemandDataRepository extends JpaRepository<BusinessDemand, UUID>{
	
	public List<BusinessDemand> findAllByYearAndPlantId(String year,UUID plantId);
	
	@Query(value = """
		 	    SELECT BD.Id,BD.Remark,BD.Jan, BD.Feb,  BD.March,BD.April, BD.May, BD.June, BD.July, BD.Aug,  
    BD.Sep,  BD.Oct, BD.Nov,  BD.Dec, BD.Year, BD.Plant_FK_Id,BD.NormParameters_FK_Id, BD.AvgTPH,NP.DiplayOrder,NPT.Id AS NormParameterTypeId,
			NPT.Name AS NormParameterTypeName,
    NPT.DisplayName AS NormParameterTypeDisplayName FROM 
    BusinessDemand BD JOIN NormParameters NP ON BD.NormParameters_FK_Id = NP.Id
    JOIN NormParameterType NPT ON NP.NormParameterType_FK_Id = NPT.Id
	WHERE BD.Year = :year AND BD.Plant_FK_Id = :plantFkId 
    ORDER BY NPT.Id, NP.DiplayOrder
 	        """, nativeQuery = true)
 	    List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantFkId") UUID plantFkId);
	

}
