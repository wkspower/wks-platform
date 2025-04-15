package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.entity.UserScreenMapping;

@Repository
public interface UserScreenMappingRepository extends JpaRepository<UserScreenMapping, UUID>{

	@Query(value="SELECT ScreenCode"
			+ "  FROM [dbo].[UserScreenMapping] where UserId=:userId and PlantFKId=:plantId and VerticalFKId=:verticalId GROUP BY ScreenCode", nativeQuery=true)
	List<String> findByVerticalFKIdAndPlantFKIdandUserId(@Param("verticalId") String verticalId, @Param("plantId") String plantId, @Param("userId") String userId);

	List<UserScreenMapping> findByUserIdAndPlantFKIdAndVerticalFKId(UUID userId, UUID plantId,
			UUID verticalId);
	


}
