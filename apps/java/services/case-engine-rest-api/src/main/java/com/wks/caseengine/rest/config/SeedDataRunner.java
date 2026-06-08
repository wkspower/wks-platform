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
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormRepository;
import com.wks.caseengine.queue.Queue;
import com.wks.caseengine.queue.QueueRepository;
import com.wks.caseengine.repository.Repository;

import lombok.extern.slf4j.Slf4j;

/**
 * Datastore-agnostic startup seeder for the minimal dev mode. Reads the bundled
 * demo collections (forms, case definitions, queues) and inserts them through the
 * repository interfaces, so it works for both the JPA/H2 and Mongo backends.
 *
 * <p>Gated by {@code wks.seed.enabled} (default off) — only the minimal profile
 * turns it on. It is idempotent: if any case definition already exists it does
 * nothing, so restarts and the demo-data-loader (Mongo full stack) never clash.
 *
 * <p>Runs at startup with no request context; in single-tenant mode the JPA path
 * resolves to the "public" schema and the Mongo path resolves to the configured
 * default tenant, so no tenant header is required.
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "wks.seed.enabled", havingValue = "true")
public class SeedDataRunner implements CommandLineRunner {

	// Single source of truth: demo-data-loader/data/mongodb/mongo-base-collections.json,
	// copied onto this module's classpath at build time (see maven-resources-plugin in pom.xml).
	private static final String SEED_RESOURCE = "seed/mongo-base-collections.json";

	@Autowired
	private CaseDefinitionRepository caseDefinitionRepository;

	@Autowired
	private FormRepository formRepository;

	@Autowired
	private QueueRepository queueRepository;

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

		// Forms first (case definitions reference a formKey), then definitions, then queues.
		int forms = seed(gson, data, "form", Form.class, formRepository);
		int defs = seed(gson, data, "caseDefinition", CaseDefinition.class, caseDefinitionRepository);
		int queues = seed(gson, data, "queue", Queue.class, queueRepository);

		log.info("Seed complete: {} forms, {} case definitions, {} queues.", forms, defs, queues);
	}

	private <T> int seed(Gson gson, JsonObject data, String key, Class<T> type, Repository<T> repository) {
		JsonElement element = data.get(key);
		if (element == null || !element.isJsonArray()) {
			return 0;
		}

		int count = 0;
		JsonArray array = element.getAsJsonArray();
		Function<JsonElement, T> toDomain = e -> gson.fromJson(e, type);
		for (JsonElement item : array) {
			try {
				repository.save(toDomain.apply(item));
				count++;
			} catch (Exception e) {
				log.warn("Seed: failed to insert one {} record: {}", key, e.getMessage());
			}
		}
		return count;
	}
}
