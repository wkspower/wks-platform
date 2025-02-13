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
package com.wks.caseengine.tasks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.caseengine.rest.db1.entity.Product;

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

	@Override
	public List<Product> getAllProducts() {
		String queryStr = "SELECT * FROM [MST].[mesProduct]";

		Query query = entityManager.createNativeQuery(queryStr, Product.class);
		List<Product> searchResults = query.getResultList();
		return searchResults;

	}

}
