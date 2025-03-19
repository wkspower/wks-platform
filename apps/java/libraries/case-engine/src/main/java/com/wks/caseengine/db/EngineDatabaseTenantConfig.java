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
package com.wks.caseengine.db;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
@EnableJpaRepositories(basePackages = { "com.wks.caseengine.cases.definition.repository",
		"com.wks.caseengine.cases.instance.email.repository", "com.wks.caseengine.cases.instance.repository",
		"com.wks.caseengine.form", "com.wks.caseengine.queue", "com.wks.caseengine.record",
		"com.wks.caseengine.record.type" })
@EntityScan(basePackages = "com.wks.caseengine.entity")
@EnableTransactionManagement
public class EngineDatabaseTenantConfig {

	@Bean
	@ConfigurationProperties("spring.datasource")
	public HikariConfig hikariConfig() {
		return new HikariConfig();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan("com.wks.caseengine.entity");
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		return em;
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}

}
