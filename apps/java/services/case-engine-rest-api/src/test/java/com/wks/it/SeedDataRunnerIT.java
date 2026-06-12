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
package com.wks.it;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionJpaRepositoryImpl;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.db.EngineDatabaseTenantConfig;
import com.wks.caseengine.form.FormJpaRepositoryImpl;
import com.wks.caseengine.queue.QueueJpaRepositoryImpl;
import com.wks.caseengine.rest.config.GsonConfiguration;
import com.wks.caseengine.rest.config.SeedDataRunner;

/**
 * WP-1.1 acceptance: proves the datastore-agnostic seeder deserializes the bundled
 * demo collections and persists them through the repository interfaces onto embedded
 * H2 (database.type=jpa) — i.e. the minimal core boots with demo case definitions.
 *
 * <p>Lives in {@code com.wks.it} (outside every production @ComponentScan) so its
 * nested @SpringBootConfiguration is not picked up by other tests' contexts.
 */
@SpringBootTest(classes = SeedDataRunnerIT.TestApp.class, properties = {
		"database.type=jpa",
		"wks.seed.enabled=true",
		"wks.tenancy.multi-tenant=false",
		"spring.datasource.jdbcUrl=jdbc:h2:mem:wks_seed_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
				+ "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
				+ "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration" })
public class SeedDataRunnerIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import({ EngineDatabaseTenantConfig.class, CaseDefinitionJpaRepositoryImpl.class, FormJpaRepositoryImpl.class,
			QueueJpaRepositoryImpl.class, GsonConfiguration.class, SeedDataRunner.class })
	static class TestApp {

		@Bean
		static PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
			return new PersistenceAnnotationBeanPostProcessor();
		}

		/** In production the DataSource is TenantRoutingDatasource; the test builds the global one directly. */
		@Bean
		javax.sql.DataSource dataSource(com.zaxxer.hikari.HikariConfig hikariConfig) {
			return new com.zaxxer.hikari.HikariDataSource(hikariConfig);
		}

		@Bean
		SecurityContextTenantHolder tenantHolder() {
			return new SecurityContextTenantHolder() {
				@Override
				public Optional<String> getTenantId() {
					return Optional.empty();
				}

				@Override
				public void setTenantId(String tenantId) {
				}

				@Override
				public Optional<String> getUserId() {
					return Optional.empty();
				}

				@Override
				public void setUserId(String userId) {
				}

				@Override
				public void clear() {
				}
			};
		}
	}

	@Autowired
	private CaseDefinitionRepository caseDefinitionRepository;

	@Test
	public void shouldSeedDemoCaseDefinitionsIntoH2() {
		List<CaseDefinition> defs = caseDefinitionRepository.find();
		assertFalse(defs.isEmpty(), "seeder must have inserted demo case definitions");
		assertTrue(defs.stream().anyMatch(d -> "customer-support".equals(d.getId())),
				"expected the 'customer-support' demo case definition to be seeded");
	}
}
