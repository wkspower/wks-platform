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
package com.wks.caseengine.tenancy;

import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "jpa")
public class JpaTenantListProvider implements TenantListProvider {

	@Autowired
	private DataSource dataSource;

	@Override
	public List<String> getTenantIds() {
		try {
			JdbcTemplate jdbc = new JdbcTemplate(dataSource);
			return jdbc.query("SELECT name FROM tenant_database", (rs, rowNum) -> rs.getString("name"));
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
