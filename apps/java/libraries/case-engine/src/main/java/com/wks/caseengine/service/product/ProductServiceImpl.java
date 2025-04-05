/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.service.product;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.wks.caseengine.product.repository.ProductMonthWiseDataRepository;
import com.wks.caseengine.rest.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Verticals;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Component
public class ProductServiceImpl implements ProductService {

	@Autowired
	@Qualifier("db1JdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@PersistenceContext(unitName = "db1")
	private EntityManager entityManager;
	
	@Autowired
	private ProductMonthWiseDataRepository productMonthWiseDataRepository;
	
	@Autowired
	PlantsRepository plantsRepository;
	
	@Autowired
	SiteRepository siteRepository;
	
	@Autowired
	VerticalsRepository verticalRepository;

	@Override
	public List<Product> getAllProducts() {
		String queryStr = "SELECT * FROM [MST].[mesProduct]";

		Query query = entityManager.createNativeQuery(queryStr, Product.class);
		List<Product> searchResults = query.getResultList();
		return searchResults;

	}
	public String getMonthName(int monthNumber) {
	    return switch (monthNumber) {
	        case 1 -> "January";
	        case 2 -> "February";
	        case 3 -> "March";
	        case 4 -> "April";
	        case 5 -> "May";
	        case 6 -> "June";
	        case 7 -> "July";
	        case 8 -> "August";
	        case 9 -> "September";
	        case 10 -> "October";
	        case 11 -> "November";
	        case 12 -> "December";
	        default -> "Invalid Month";
	    };
	}

	@Override
	public List<Object[]> getMonthWiseDataByTypeAndYear(String type, String currentYear) {
		List<Object[]> productMonthWiseData= productMonthWiseDataRepository.getMonthWiseDataByTypeAndYear(type,currentYear);
		
		//List<ProductMonthWiseDataDTO> productMonthWiseDataDTOList = new ArrayList<>();
		/*for(Object obj : productMonthWiseData) {
		    Object[] data = (Object[]) obj;
		    ProductMonthWiseDataDTO productMonthWiseDataDTO = new ProductMonthWiseDataDTO();
		    
		    int monthNumber = ((Number) data[0]).intValue();
		    //productMonthWiseDataDTO.setMonth(getMonthName(monthNumber));
		    
		    productMonthWiseDataDTO.setPlantId(((Number) data[1]).longValue());
		    productMonthWiseDataDTO.setMonthValue(((Number) data[2]).longValue());
		    
		    productMonthWiseDataDTOList.add(productMonthWiseDataDTO);
		}*/

		return productMonthWiseData;

	}
	
	/*public ProductMonthWiseDataDTO saveMonthWiseData(ProductMonthWiseDataDTO productMonthWiseDataDTO) {
		ProductMonthWiseData productMonthWiseData=new ProductMonthWiseData();
		productMonthWiseData.setMonth(productMonthWiseDataDTO.getMonth());
		productMonthWiseData.setMonthValue(productMonthWiseDataDTO.getMonthValue());
		productMonthWiseData.setPlantId(productMonthWiseDataDTO.getPlantId());
		productMonthWiseData.setProductId(productMonthWiseDataDTO.getProductId());
		productMonthWiseData.setType(productMonthWiseDataDTO.getType());
		productMonthWiseData.setYear(productMonthWiseDataDTO.getYear());
		productMonthWiseDataRepository.save(productMonthWiseData);
		return productMonthWiseDataDTO;
	}*/


	public List<Object[]> getAllProductsFromNormParameters(String normParameterTypeName, UUID plantId) {
	    System.out.println("normParameterTypeName: " + normParameterTypeName);

		if(normParameterTypeName.equalsIgnoreCase("BusinessDemandMEG")){
			return getProductsFromDynamicView("vwScrnMEGBusinessDemandGetAllProducts" , plantId);
		}

	    // Convert "null" string to actual null (if needed)
	    if ("null".equals(normParameterTypeName)) {
	        normParameterTypeName = null;
	        System.out.println("normParameterTypeName is the string 'null'");
	    }

	    // Start query construction
	    StringBuilder queryBuilder = new StringBuilder(
	        "SELECT CAST(np.Id AS VARCHAR(36)) as NormParameterId, np.Name, np.DisplayName " +
	        "FROM NormParameters np "
	    );

	    // If filtering by norm type, join with NormTypes
	    if (!"All".equals(normParameterTypeName)) {
	        queryBuilder.append("JOIN NormTypes nt ON np.NormType_FK_Id = nt.Id ")
	                    .append("WHERE np.Plant_FK_Id = :plantId AND np.NormParameterType_FK_Id IS NOT NULL ");
	        if (normParameterTypeName != null) {
	            queryBuilder.append("AND nt.NormName = :normParameterTypeName ");
	        }
	    } else {
	        queryBuilder.append("WHERE np.Plant_FK_Id = :plantId ");
	    }

	    // Append ordering clause
	    queryBuilder.append("ORDER BY np.DisplayOrder");

	    // Create and set parameters in the query
	    Query query = entityManager.createNativeQuery(queryBuilder.toString());
	    query.setParameter("plantId", plantId);
	    if (!"All".equals(normParameterTypeName) && normParameterTypeName != null) {
	        query.setParameter("normParameterTypeName", normParameterTypeName);
	    }

	    return query.getResultList();
	}

	public List<Object[]> getMonthlyDataForYear(int year) {
        String query = "SELECT NormParameter_FK_Id, month, monthValue, Remarks FROM NormParameterMonthlyTransaction WHERE year = :year";
        return entityManager.createNativeQuery(query).setParameter("year", year).getResultList();
    }

	
	public List<Object[]> getProductsFromDynamicView(String viewName, UUID plantId) {
        String sql = "SELECT Id, Name, DisplayName, Plant_FK_Id FROM " + viewName + " WHERE Plant_FK_Id = :plantId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("plantId", plantId);
        return query.getResultList();
	}
	
	public List<Object[]> getProductsFromDynamicViewForPE(String viewName, UUID plantFkId, String normParameterTypeName) {
		String sql = "SELECT NP.Id, NP.Name, NP.DisplayName FROM " + viewName + " NP, NormParameterType npt "
				+ "WHERE npt.Id = NP.NormParameterType_FK_Id " + "AND NP.NormParameterType_FK_Id IS NOT NULL "
				+ "AND NP.Plant_FK_Id = :plantFkId " + "AND npt.Name = :normParameterTypeName";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("plantFkId", plantFkId);
		query.setParameter("normParameterTypeName", normParameterTypeName);

		return query.getResultList();
	}


}
