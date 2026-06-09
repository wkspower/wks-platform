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
package com.wks.caseengine.rest.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.data.iimport.DataImportService;

import lombok.extern.slf4j.Slf4j;

/**
 * Datastore-agnostic startup seeder for the minimal dev mode. Reads the bundled
 * canonical collections (forms, case definitions, queues) and routes them through
 * the same {@link DataImportService} seam that backs the REST data-import
 * endpoint, so the active {@code database.type} strategy (JPA or Mongo) decides
 * how they are persisted — no per-repository writes here.
 *
 * <p>Gated by {@code wks.seed.enabled} (default off) — only the minimal profile
 * turns it on. It is idempotent: if any case definition already exists it does
 * nothing, so restarts and the demo-data-loader (Mongo full stack) never clash.
 *
 * <p>Runs at startup with no request context; in single-tenant mode the JPA path
 * resolves to the default schema and the Mongo path resolves to the configured
 * default tenant, so no tenant header is required.
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "wks.seed.enabled", havingValue = "true")
public class SeedDataRunner implements CommandLineRunner {

	// Canonical platform seed in the wks-platform import format, owned by this module.
	private static final String SEED_RESOURCE = "seed/wks-base-collections.json";

	@Autowired
	private CaseDefinitionRepository caseDefinitionRepository;

	@Autowired
	private DataImportService dataImportService;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public void run(String... args) throws Exception {
		if (!caseDefinitionRepository.find().isEmpty()) {
			log.info("Seed skipped: case definitions already present.");
			return;
		}

		ClassPathResource resource = new ClassPathResource(SEED_RESOURCE);
		if (!resource.exists()) {
			log.warn("Seed skipped: classpath resource {} not found.", SEED_RESOURCE);
			return;
		}

		Gson gson = gsonBuilder.create();
		JsonObject data;
		try (InputStream in = resource.getInputStream();
				InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			data = gson.fromJson(reader, JsonObject.class);
		}

		dataImportService.importData(data);

		log.info("Seed complete: imported {} via the data-import seam.", SEED_RESOURCE);
	}

}
