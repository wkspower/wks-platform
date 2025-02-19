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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.wks.caseengine.product.repository.ProductMonthWiseDataRepository;
import com.wks.caseengine.rest.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class ProductServiceImpl implements ProductService {

	@Autowired
	@Qualifier("db1JdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@PersistenceContext(unitName = "db1")
	private EntityManager entityManager;
	
	@Autowired
	private ProductMonthWiseDataRepository productMonthWiseDataRepository;

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
	public List<Object[]> getMonthWiseDataByTypeAndYear(String type, int currentYear) {
		List<Object[]> productMonthWiseData= productMonthWiseDataRepository.getMonthWiseDataByTypeAndYear(type,currentYear,currentYear+1);
		
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


	// Service method to fetch products from NormParameters table
	public List<Object[]> getAllProductsFromNormParameters() {
		String query = "SELECT Id, Name, DisplayName FROM [RIL.AOP].[dbo].[NormParameters]";
		return entityManager.createNativeQuery(query).getResultList();
	}



    public List<Object[]> getMonthlyDataForYear(int year) {
        String query = "SELECT NormParameters_FK_Id, month, monthValue, Remarks FROM [RIL.AOP].[dbo].[NormParameterMonthlyTransaction] WHERE year = :year";
        return entityManager.createNativeQuery(query).setParameter("year", year).getResultList();
    }

}
