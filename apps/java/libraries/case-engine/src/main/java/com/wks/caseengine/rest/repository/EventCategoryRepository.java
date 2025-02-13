package com.wks.caseengine.rest.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.rest.entity.EventCategory;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Long> {

	@Query(value="SELECT * FROM EventCategories WHERE Event_Category_PK_ID = :eventCategoryId",nativeQuery = true)
	EventCategory getCategoryById(@Param(value = "eventCategoryId") UUID eventCategoryId);

}
