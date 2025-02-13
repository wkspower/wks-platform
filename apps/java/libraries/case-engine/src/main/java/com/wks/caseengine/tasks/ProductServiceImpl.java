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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.model.spi.Product;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.tasks.command.FindTaskCmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Component
public class ProductServiceImpl implements ProductService {

	@Autowired
	@Qualifier("db1JdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<Product> getAllProducts() {
		try {
			String sql = "SELECT * FROM [MST].[mesProduct]";
			List<Product> products = jdbcTemplate.query(sql,
					(rs, rowNum) -> new Product(rs.getInt("productId"), rs.getString("productName")));

			// If no products are found, return a default product
			if (products.isEmpty()) {
				return List.of(new Product(0, "No Products Available"));
			}

			return products;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Return a default product when an error occurs
		return List.of(new Product(-1, "Error Fetching Products"));
	}

}
