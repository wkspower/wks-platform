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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionJpaRepositoryImpl;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.command.JpaDataConnectionExchange;
import com.wks.caseengine.db.EngineDatabaseTenantConfig;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormJpaRepositoryImpl;
import com.wks.caseengine.form.FormRepository;
import com.wks.caseengine.queue.Queue;
import com.wks.caseengine.queue.QueueJpaRepositoryImpl;
import com.wks.caseengine.queue.QueueRepository;

/**
 * Proves {@link JpaDataConnectionExchange#exportFromDatabase} (which previously threw
 * "not implemented") returns the canonical form/caseDefinition/queue collections, symmetric
 * to its import side, against embedded H2 (database.type=jpa) with no external datastore.
 *
 * <p>Lives in {@code com.wks.it} (outside every production @ComponentScan) so its nested
 * @SpringBootConfiguration is not picked up by other tests' contexts.
 */
@SpringBootTest(classes = JpaDataExportIT.TestApp.class, properties = {
		"database.type=jpa",
		"spring.datasource.jdbcUrl=jdbc:h2:mem:wks_export_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create",
		"spring.autoconfigure.exclude="
				+ "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration,"
				+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
				+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration" })
public class JpaDataExportIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import({ EngineDatabaseTenantConfig.class, CaseDefinitionJpaRepositoryImpl.class, FormJpaRepositoryImpl.class,
			QueueJpaRepositoryImpl.class, JpaDataConnectionExchange.class })
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
	private FormRepository formRepository;

	@Autowired
	private CaseDefinitionRepository caseDefinitionRepository;

	@Autowired
	private QueueRepository queueRepository;

	@Autowired
	private JpaDataConnectionExchange dataConnectionExchange;

	@Test
	public void shouldExportCanonicalCollectionsFromJpaBackend() throws Exception {
		formRepository.save(Form.builder().key("form-1").title("Form 1").build());
		caseDefinitionRepository.save(CaseDefinition.builder().id("cd-1").name("Case Def 1").deployed(true).build());
		queueRepository.save(Queue.builder().id("q-1").name("Queue 1").build());

		JsonObject exported = dataConnectionExchange.exportFromDatabase(new Gson());

		assertNotNull(exported, "export must return data, not null");
		assertTrue(exported.has("form") && exported.get("form").isJsonArray());
		assertTrue(exported.has("caseDefinition") && exported.get("caseDefinition").isJsonArray());
		assertTrue(exported.has("queue") && exported.get("queue").isJsonArray());

		assertEquals(1, exported.getAsJsonArray("form").size());
		assertEquals(1, exported.getAsJsonArray("caseDefinition").size());
		assertEquals(1, exported.getAsJsonArray("queue").size());
	}
}
