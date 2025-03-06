package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.NormParameters;

@Repository
public interface NormParametersRepository extends JpaRepository<NormParameters,UUID>{
	
	List<NormParameters> findAllByType(@Param("type") String type);
	

}
