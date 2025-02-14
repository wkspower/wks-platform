package com.wks.caseengine.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.rest.db1.entity.ProductMonthWiseData;

import java.util.List;

@Repository
public interface ProductMonthWiseDataRepository extends JpaRepository<ProductMonthWiseData, Long>{
	
	@Query(value = "SELECT month, plant_id, SUM(month_value) as total_value " +
            "FROM dbo.product_month_plant_wise_data " +
            "WHERE type = :type " +
            "AND ((year = :currentYear AND CAST(month AS INT) >= 4) OR (year = :nextYear AND CAST(month AS INT) <= 3)) " +
            "GROUP BY month, plant_id", nativeQuery = true)
			List<Object[]> getMonthWiseDataByTypeAndYear(@Param("type") String type, 
                                          @Param("currentYear") int currentYear,
                                          @Param("nextYear") int nextYear);


}
