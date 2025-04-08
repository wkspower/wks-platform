 package com.wks.caseengine.repository;

 import java.util.List;
 import java.util.UUID;

 import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 import org.springframework.stereotype.Repository;

 import com.wks.caseengine.entity.NormParameters;

 @Repository
 public interface NormParametersRepository extends JpaRepository<NormParameters,UUID>{
	
 	List<NormParameters> findAllByType(String type);
	
	 @Query(value = "SELECT * FROM v_NormParameters_Filtered WHERE Plant_FK_Id = :plantId", nativeQuery = true)
	 List<NormParameters> getAllGrades(@Param("plantId") String plantId);

 }
