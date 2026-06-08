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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.repository.CaseInstanceJpaRepositoryImpl;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.repository.JpaPaginator;

/**
 * WP-1.0 acceptance: proves the real {@link EngineDatabaseTenantConfig} JPA wiring
 * boots against an embedded H2 database (database.type=jpa) and serves CRUD on the
 * single ("public") schema with NO external datastore container.
 */
@SpringBootTest(classes = CaseInstanceJpaRepositoryImplIT.TestApp.class, properties = {
		"database.type=jpa",
		"spring.datasource.jdbcUrl=jdbc:h2:mem:wks_jpa_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.sql.init.schema-locations=classpath:schema-h2.sql",
		"spring.sql.init.mode=always",
		"spring.autoconfigure.exclude="
				+ "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration,"
				+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
				+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration" })
public class CaseInstanceJpaRepositoryImplIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import({ EngineDatabaseTenantConfig.class, CaseInstanceJpaRepositoryImpl.class, JpaPaginator.class })
	static class TestApp {

		@Bean
		static PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
			return new PersistenceAnnotationBeanPostProcessor();
		}

		/** Single-tenant stub: no JWT in tests, so the resolver falls back to "public". */
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
	private CaseInstanceRepository repository;

	@Test
	public void shouldPersistAndReadCaseInstanceOnEmbeddedH2() throws Exception {
		CaseInstance toSave = new CaseInstance("634d1eac797f75ecc4a10052", "WP10-0001", "loan-approval",
				"Data Collection", "WIP_CASE_STATUS");

		String uid = repository.save(toSave);
		assertNotNull(uid, "save() must return the generated uid");

		CaseInstance fetched = repository.get("WP10-0001");
		assertNotNull(fetched);
		assertEquals("WP10-0001", fetched.getBusinessKey());
		assertEquals("loan-approval", fetched.getCaseDefinitionId());
		assertEquals("Data Collection", fetched.getStage());
		assertEquals(CaseStatus.WIP_CASE_STATUS, fetched.getStatus());

		List<CaseInstance> all = repository.find();
		assertFalse(all.isEmpty(), "find() must return the persisted case");
	}
}
