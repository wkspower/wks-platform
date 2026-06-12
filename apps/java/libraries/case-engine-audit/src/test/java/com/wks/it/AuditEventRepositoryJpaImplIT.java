package com.wks.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
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
import com.wks.caseengine.audit.AuditEvent;
import com.wks.caseengine.audit.AuditEventType;
import com.wks.caseengine.audit.repository.AuditEventJpaRepositoryImpl;
import com.wks.caseengine.audit.repository.AuditEventRepository;
import com.wks.caseengine.db.EngineDatabaseTenantConfig;

@SpringBootTest(classes = AuditEventRepositoryJpaImplIT.TestApp.class, properties = {
		"database.type=jpa",
		"spring.datasource.jdbcUrl=jdbc:h2:mem:wks_audit_jpa_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create",
		"spring.autoconfigure.exclude="
				+ "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
				+ "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration" })
public class AuditEventRepositoryJpaImplIT {

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import({ EngineDatabaseTenantConfig.class, AuditEventJpaRepositoryImpl.class })
	static class TestApp {

		@Bean
		public SecurityContextTenantHolder tenantHolder() {
			return new SecurityContextTenantHolder() {
				@Override
				public Optional<String> getTenantId() {
					return Optional.of("public");
				}
				@Override
				public void setTenantId(String tenantId) {}
				@Override
				public Optional<String> getUserId() {
					return Optional.of("test-user");
				}
				@Override
				public void setUserId(String userId) {}
				@Override
				public void clear() {}
			};
		}

		@Bean
		public PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
			return new PersistenceAnnotationBeanPostProcessor();
		}
	}

	@Autowired
	private AuditEventRepository auditEventRepository;

	@Test
	public void shouldSaveAndRetrieveAuditEvents() {
		AuditEvent event1 = AuditEvent.builder()
				.tenantId("public")
				.userId("test-user")
				.timestamp(Instant.now().toString())
				.eventType(AuditEventType.CASE_CREATED)
				.entityId("CASE-123")
				.entityType("CaseInstance")
				.payload("{\"status\":\"ACTIVE\"}")
				.processed(false)
				.build();

		AuditEvent event2 = AuditEvent.builder()
				.tenantId("public")
				.userId("test-user")
				.timestamp(Instant.now().toString())
				.eventType(AuditEventType.CASE_UPDATED)
				.entityId("CASE-123")
				.entityType("CaseInstance")
				.payload("{\"status\":\"COMPLETED\"}")
				.processed(false)
				.build();

		// Save
		auditEventRepository.save(event1);
		auditEventRepository.save(event2);

		assertNotNull(event1.getId());
		assertNotNull(event2.getId());

		// Find All
		List<AuditEvent> all = auditEventRepository.find();
		assertTrue(all.size() >= 2);

		// Find by Case
		List<AuditEvent> byCase = auditEventRepository.findByCaseInstanceId("CASE-123");
		assertEquals(2, byCase.size());
		assertEquals(AuditEventType.CASE_UPDATED, byCase.get(0).getEventType()); // Sorted descending

		// Find Unprocessed
		List<AuditEvent> unprocessed = auditEventRepository.findUnprocessed();
		assertEquals(2, unprocessed.size());

		// Mark Processed
		auditEventRepository.markProcessed(Arrays.asList(event1.getId()));
		List<AuditEvent> unprocessedAfter = auditEventRepository.findUnprocessed();
		assertEquals(1, unprocessedAfter.size());
		assertEquals(event2.getId(), unprocessedAfter.get(0).getId());
	}
}
