package com.wks.caseengine.rest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.rest.entity.FunctionalLocation;

@Repository
public interface FunctionalLocationRepository extends JpaRepository<FunctionalLocation, Long> {

	@Query(value = "SELECT * FROM dbo.functional_location WHERE uas_display_name = :uasDisplayName", nativeQuery = true)
    List<FunctionalLocation> findByUasDisplayName(@Param("uasDisplayName") String uasDisplayName);
    
    @Query(value = "SELECT DISTINCT asset_sort_feild, parent_fl_name FROM dbo.functional_location", nativeQuery = true)
    List<FunctionalLocation> findAllFunctionalLocations();
	    
}
