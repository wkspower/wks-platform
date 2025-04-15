package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wks.caseengine.entity.ScreenDataConfig;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

@Repository
public interface ScreenDataConfigRepository extends JpaRepository<ScreenDataConfig, UUID>{
	
	@Query(value = "SELECT * FROM ScreenDataConfig " +
	            "WHERE ScreenName = :screenName AND Vertical_FK_Id = :verticalFkId", nativeQuery = true)
	Optional<ScreenDataConfig> findByScreenNameAndVerticalFkId(@Param("screenName") String screenName,
	                                                         @Param("verticalFkId") UUID verticalFkId);

}
