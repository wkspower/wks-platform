package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.VerticalScreenMapping;


@Repository
public interface VerticalScreenMappingRepository extends JpaRepository<VerticalScreenMapping,Long>{
	
	List<VerticalScreenMapping> findAllByVerticalFKIdOrderBySequence(@Param("verticalFKId") UUID verticalFKId);


}
