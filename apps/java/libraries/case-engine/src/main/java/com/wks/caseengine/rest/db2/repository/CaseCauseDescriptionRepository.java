package com.wks.caseengine.rest.db2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.rest.db2.entity.CaseCauseDescription;

@Repository
public interface CaseCauseDescriptionRepository extends JpaRepository<CaseCauseDescription, Long> {

	@Query(value="SELECT * FROM case_cause_description where categoryId = :categoryId",nativeQuery = true)
	List<CaseCauseDescription> findAllDescriptionByCategoryId(@Param(value = "categoryId") Long categoryId);

}
