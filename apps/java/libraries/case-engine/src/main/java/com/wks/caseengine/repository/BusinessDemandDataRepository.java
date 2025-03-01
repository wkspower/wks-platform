package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.BusinessDemand;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BusinessDemandDataRepository extends JpaRepository<BusinessDemand, UUID>{
	
	List<BusinessDemand> findAllByYearAndPlantIdAndIsDeletedFalse(String year, UUID plantId);
	
	@Transactional
    @Modifying
    @Query("UPDATE BusinessDemand b SET b.isDeleted = true WHERE b.id = :id")
    void softDelete(@Param("id") UUID id);
	
	
	

}
