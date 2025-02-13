package com.wks.caseengine.rest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.rest.entity.Case;

public interface CaseRepository extends JpaRepository<Case, Long> {
	
	@Query(value="SELECT HierarchyNode_PK_ID FROM [case_management].[dbo].[HierarchyNodes] WHERE DisplayNamePath LIKE :assetName AND isDeleted = 0",nativeQuery = true)
	String gethierarchyNodePKID(@Param(value = "assetName") String assetName);

	@Query(value =" select * from cases where hierarchy_node_pk_id in (:assetsPKIds)", nativeQuery = true)
	List<Case> findAllByAssetsPKID(@Param(value="assetsPKIds") List<String> assetsPKIds);

	@Query(value =" select * from cases where case_no =:case_no", nativeQuery = true)
	Case getByCaseNo(@Param(value="case_no") String case_no);
}