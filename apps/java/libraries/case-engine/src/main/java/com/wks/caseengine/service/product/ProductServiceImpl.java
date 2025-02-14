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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.dto.product.ProductMonthWiseDataDTO;
import com.wks.caseengine.product.repository.ProductMonthWiseDataRepository;
import com.wks.caseengine.rest.db1.entity.Product;
import com.wks.caseengine.rest.db1.entity.ProductMonthWiseData;

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
	
	@Override
	public List<ProductMonthWiseDataDTO> getMonthWiseDataByTypeAndYear(String type, int currentYear) {
		List<Object[]> productMonthWiseData= productMonthWiseDataRepository.getMonthWiseDataByTypeAndYear(type,currentYear,currentYear+1);
		List<ProductMonthWiseDataDTO> productMonthWiseDataDTOList = new ArrayList<ProductMonthWiseDataDTO>();
		for(Object obj:productMonthWiseData) {
			Object[] data = (Object[]) obj;
			ProductMonthWiseDataDTO productMonthWiseDataDTO= new ProductMonthWiseDataDTO();
			productMonthWiseDataDTO.setMonth(data[0].toString());
			productMonthWiseDataDTO.setPlantId((Long) data[1]);
			productMonthWiseDataDTO.setMonthValue((Long) data[2]);
			productMonthWiseDataDTOList.add(productMonthWiseDataDTO);
		}
		return productMonthWiseDataDTOList;
		
	}

}
