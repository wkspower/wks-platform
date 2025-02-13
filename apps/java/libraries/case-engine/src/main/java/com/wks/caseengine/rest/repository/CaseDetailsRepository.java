package com.wks.caseengine.rest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.rest.entity.Case;
import com.wks.caseengine.rest.entity.FaultCategory;

@Repository
public interface CaseDetailsRepository extends JpaRepository<FaultCategory, Long> {

	@Query(value = "SELECT hn.HierarchyNode_PK_ID" +
            " FROM case_management.dbo.HierarchyNodes hn " +
            "JOIN case_management.dbo.HierarchyTrees ht ON hn.HierarchyTree_PK_ID = ht.HierarchyTree_PK_ID " +
            "WHERE hn.IsDeleted = 0 " +
            "AND hn.DisplayNamePath LIKE CONCAT('%', :displayName, '%') " +
            "AND ht.HierarchyType = :hierarchyName", nativeQuery = true)
	List<String> findNodesByHierarchyNameAndDisplayName(@Param(value= "displayName") String displayName, @Param(value= "hierarchyName") String hierarchyName);
}
