package com.wks.caseengine.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPConsumptionNormGrade;

@Repository
public interface AOPConsumptionNormGradeRepository extends JpaRepository<AOPConsumptionNormGrade,UUID>{

	@Query(value = "SELECT * FROM AOPConsumptionNormGrade " +
            "WHERE Material_FK_Id = :materialFkId " +
            "AND Grade_FK_Id = :gradeFkId " +
            "AND AOPYear = :aopYear " +
            "AND Plant_FK_Id = :plantFkId",
    nativeQuery = true)
	Optional<AOPConsumptionNormGrade> findByMaterialGradeAndAOPYear(
	  @Param("materialFkId") UUID materialFkId,
	  @Param("gradeFkId") UUID gradeFkId,
	  @Param("aopYear") String aopYear,
	  @Param("plantFkId") UUID plantFkId
	);
}
