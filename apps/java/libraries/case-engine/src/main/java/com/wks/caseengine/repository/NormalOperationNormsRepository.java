package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.MCUNormsValue;

@Repository
public interface NormalOperationNormsRepository extends JpaRepository<MCUNormsValue,UUID>{
	
	@Query(value = """
		    SELECT MNV.Id, MNV.Site_FK_Id, MNV.Plant_FK_Id, MNV.Vertical_FK_Id, MNV.Material_FK_Id, MNV.April, MNV.May, MNV.June, MNV.July, MNV.August,  
		           MNV.September, MNV.October, MNV.November, MNV.December, MNV.January, MNV.February, MNV.March, MNV.FinancialYear, MNV.Remarks, 
		           MNV.CreatedOn, MNV.ModifiedOn, MNV.MCUVersion, MNV.UpdatedBy 
		    FROM MCUNormsValue MNV
		    WHERE MNV.FinancialYear = :year AND MNV.Plant_FK_Id = :plantId
		    """, nativeQuery = true)
		List<Object[]> findByYearAndPlantFkId(@Param("year") String year, @Param("plantId") UUID plantId);


}
