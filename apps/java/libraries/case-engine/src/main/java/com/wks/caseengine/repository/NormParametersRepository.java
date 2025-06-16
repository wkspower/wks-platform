package com.wks.caseengine.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.NormParameters;

@Repository
public interface NormParametersRepository extends JpaRepository<NormParameters, UUID> {

	List<NormParameters> findAllByType(String type);

	@Query(value = "SELECT * FROM vwScrnPEConfigurationGrades WHERE Plant_FK_Id = :plantId", nativeQuery = true)
	List<NormParameters> getAllGrades(@Param("plantId") String plantId);
	
	@Query(value = "SELECT Id FROM NormParameters WHERE Name = :name AND Plant_FK_Id = :plantId", nativeQuery = true)
	UUID findNormParameterIdByNameAndPlant(@Param("name") String name, @Param("plantId") UUID plantId);

    Optional<NormParameters> findByName(String string);

    Optional<NormParameters> findByNameAndPlantFkId(String string, UUID plantId);

    Optional<NormParameters> findByDisplayNameAndPlantFkId(String normParameterDisplayName, UUID plantFKId);

	

}
