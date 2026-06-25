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
package com.wks.caseengine.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepositoryImpl;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.PageResult;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class CaseInstanceRepositoryImplIT {

	@Autowired
	private MongoOperations operations;

	private CaseInstanceRepositoryImpl repository;

	@BeforeEach
	public void setup() {
		operations.insert(fixtures(), CaseInstance.class);
		repository = new CaseInstanceRepositoryImpl() {
			@Override
			protected MongoOperations getOperations() {
				return operations;
			}

			// The write path (update/get/save) goes through the driver collection rather than
			// the template; build it with the same POJO codec registry the production
			// EngineMongoTenantConfig configures so CaseInstance round-trips identically.
			@Override
			protected MongoCollection<CaseInstance> getCollection() {
				CodecRegistry pojo = CodecRegistries.fromRegistries(
						MongoClientSettings.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(PojoCodecProvider.builder()
								.conventions(Arrays.asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build()));
				return ((MongoTemplate) operations).getDb().withCodecRegistry(pojo)
						.getCollection("caseInstance", CaseInstance.class);
			}
		};
	}

	@AfterEach
	public void teadown() {
		operations.remove(new Query(), CaseInstance.class);
	}

	@Test
	public void shouldGetResultsByFindUsingPaginationFilter() throws Exception {
		CaseInstanceFilter filter = new CaseInstanceFilter("ARCHIVED_CASE_STATUS", "1", Cursor.empty(), "asc", "1");

		PageResult<CaseInstance> results = repository.find(filter);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("634d1eac797f75ecc4a10052", results.first().get_id());
		assertEquals("92935", results.first().getBusinessKey());
		assertEquals("1", results.first().getCaseDefinitionId());
		assertEquals("Data Collection", results.first().getStage());
		assertEquals(CaseStatus.ARCHIVED_CASE_STATUS, results.first().getStatus());
	}

	@Test
	public void shouldPreservePersistedStatusWhenPatchCarriesNullStatus() throws Exception {
		// Regression: a patch touching only stage must not null out the persisted status.
		// Mirrors the JPA backend's guard so both behave identically.
		CaseInstance patch = CaseInstance.builder().businessKey("92935").stage("New Stage").build();
		assertEquals(null, patch.getStatus(), "precondition: patch has no status");

		repository.update("92935", patch);

		CaseInstance reloaded = repository.get("92935");
		assertEquals("New Stage", reloaded.getStage());
		assertEquals(CaseStatus.ARCHIVED_CASE_STATUS, reloaded.getStatus(), "status must be preserved");
	}

	@Test
	public void shouldWriteStatusWhenPatchCarriesStatus() throws Exception {
		CaseInstance patch = CaseInstance.builder().businessKey("92935").stage("New Stage")
				.status(CaseStatus.CLOSED_CASE_STATUS.getCode()).build();

		repository.update("92935", patch);

		CaseInstance reloaded = repository.get("92935");
		assertEquals(CaseStatus.CLOSED_CASE_STATUS, reloaded.getStatus());
	}

	@Test
	public void shouldGetEmptyResultsByFindUsingPaginationFilter() throws Exception {
		CaseInstanceFilter filter = new CaseInstanceFilter("ARCHIVED_CASE_STATUS", "-1", Cursor.empty(), "asc", "0");

		PageResult<CaseInstance> results = repository.find(filter);

		assertNotNull(results);
		assertEquals(0, results.size());
	}

	@Test
	public void shouldGetResultsByFindUsingPaginationFilterWithNextAndBeforeCursor() throws Exception {
		PageResult<CaseInstance> results1 = repository
				.find(new CaseInstanceFilter("CLOSED_CASE_STATUS", "1", Cursor.empty(), "asc", "2"));
		PageResult<CaseInstance> results2 = repository
				.find(new CaseInstanceFilter("CLOSED_CASE_STATUS", "1", Cursor.of(null, results1.next()), "asc", "2"));
		PageResult<CaseInstance> results3 = repository
				.find(new CaseInstanceFilter("CLOSED_CASE_STATUS", "1", Cursor.of(null, results2.next()), "asc", "2"));

		assertNotNull(results1);
		assertTrue(results1.hasNext());
		assertFalse(results1.hasPrevious());
		assertNotNull(results2);
		assertTrue(results2.hasNext());
		assertTrue(results2.hasPrevious());
		assertNotNull(results3);
		assertTrue(results3.hasNext());
		assertTrue(results3.hasPrevious());
	}

	public static List<CaseInstance> fixtures() {
		List<CaseInstance> items = new ArrayList<>();
		items.add(
				new CaseInstance("634d1eac797f75ecc4a10052", "92935", "1", "Data Collection", "ARCHIVED_CASE_STATUS"));
		items.add(new CaseInstance("634d1eb2797f75ecc4a10059", "1711", "1", "Data Collection", "WIP_CASE_STATUS"));
		items.add(new CaseInstance("634d1eb2797f75ecc4a1005e", "98228", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634d1eb3797f75ecc4a10063", "65422", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634d1ee0797f75ecc4a10076", "992", "1", "Data Collection", "CLOSED_CASE_STATUS"));

		items.add(new CaseInstance("634d227d797f75ecc4a10124", "40187", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634e8b48ee448937ec2a31ac", "95622", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634e8b4aee448937ec2a31b1", "88595", "1", "Data Collection", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634e8b5aee448937ec2a31cc", "63618", "1", "Stage 1", "CLOSED_CASE_STATUS"));
		items.add(new CaseInstance("634e8cb8ee448937ec2a32ce", "25003", "1", "Review", "CLOSED_CASE_STATUS"));
		return items;
	}

}
