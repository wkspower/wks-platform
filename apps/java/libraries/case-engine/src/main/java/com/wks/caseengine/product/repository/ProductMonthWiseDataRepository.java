package com.wks.caseengine.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.ProductMonthWiseData;

import java.util.List;

@Repository
public interface ProductMonthWiseDataRepository extends JpaRepository<ProductMonthWiseData, Long>{
	
	@Query(value = "SELECT month_number, plant_id, SUM(month_value) as total_value " +
	        "FROM ( " +
	        "  SELECT " +
	        "    CASE " +
	        "      WHEN month = 'January' THEN 1 " +
	        "      WHEN month = 'February' THEN 2 " +
	        "      WHEN month = 'March' THEN 3 " +
	        "      WHEN month = 'April' THEN 4 " +
	        "      WHEN month = 'May' THEN 5 " +
	        "      WHEN month = 'June' THEN 6 " +
	        "      WHEN month = 'July' THEN 7 " +
	        "      WHEN month = 'August' THEN 8 " +
	        "      WHEN month = 'September' THEN 9 " +
	        "      WHEN month = 'October' THEN 10 " +
	        "      WHEN month = 'November' THEN 11 " +
	        "      WHEN month = 'December' THEN 12 " +
	        "    END AS month_number, " +
	        "    plant_id, " +
	        "    month_value, " +
	        "    year " +
	        "  FROM dbo.product_month_plant_wise_data " +
	        "  WHERE type = :type " +
	        ") AS month_data " +
	        "WHERE ((year = :currentYear " +
	        "GROUP BY month_number, plant_id", 
	        nativeQuery = true)
	List<Object[]> getMonthWiseDataByTypeAndYear(@Param("type") String type, 
	                                             @Param("currentYear") String currentYear
	                                            );



}
