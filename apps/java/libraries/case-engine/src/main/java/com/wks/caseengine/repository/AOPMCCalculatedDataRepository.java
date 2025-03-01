package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPMCCalculatedData;

@Repository
public interface AOPMCCalculatedDataRepository extends JpaRepository<AOPMCCalculatedData, UUID>{
	
	
	@Query(value = "SELECT b.*, a.NormParameters_FK_Id as BDNormParametersFKId " +
            "FROM BusinessDemand a " +
            "LEFT JOIN AOPMCCalculatedData b " +
            "ON a.Plant_FK_Id = b.Plant_FK_Id " +
            "AND a.NormParameters_FK_Id = b.NormParameters_FK_Id " +
            "WHERE b.Plant_FK_Id = :plantId and b.Year=:year", 
    nativeQuery = true)
	List<Object[]> findBusinessDemandWithAOPMC(@Param("plantId") UUID plantId, @Param("year") String year);
}
