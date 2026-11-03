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
package com.wks.caseengine.loader.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.sun.net.httpserver.HttpServer;

/**
 * The loader runs on every start of the stack, so each deployment it sends must
 * let Camunda recognise an unchanged model and skip it. Without a stable
 * deployment name and duplicate filtering, every restart minted a new version of
 * every bundled process.
 *
 * The runner is driven through {@code run()} against a local HTTP server standing
 * in for the Camunda REST API, so the request it really sends is what is checked.
 */
class Camunda7DataImportCommandRunnerTest {

	private HttpServer camunda;
	private final List<String> deploymentBodies = new CopyOnWriteArrayList<>();

	@TempDir
	Path importDir;

	@BeforeEach
	void startFakeCamunda() throws IOException {
		camunda = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		// Tenant already exists: GET answers 200, so the runner skips creating it.
		camunda.createContext("/engine-rest/tenant/", exchange -> {
			byte[] body = "{\"id\":\"acme\"}".getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		camunda.createContext("/engine-rest/deployment/create", exchange -> {
			deploymentBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		camunda.start();
	}

	@AfterEach
	void stopFakeCamunda() {
		camunda.stop(0);
	}

	@Test
	void shouldSendEachModelWithAStableNameAndDuplicateFiltering() throws Exception {
		Files.writeString(importDir.resolve("case-instance-create.bpmn"), "<bpmn/>");
		Files.writeString(importDir.resolve("email-to-case-owner-decision.dmn"), "<dmn/>");
		Files.writeString(importDir.resolve("README.txt"), "not a model");

		runner().run();

		assertEquals(2, deploymentBodies.size(), "one deployment per BPMN/DMN file, none for other files");
		List<String> names = deploymentBodies.stream().map(body -> String.valueOf(part(body, "deployment-name")))
				.sorted().toList();
		assertEquals(List.of("case-instance-create.bpmn", "email-to-case-owner-decision.dmn"), names);
		for (String body : deploymentBodies) {
			assertEquals("true", part(body, "enable-duplicate-filtering"));
			assertEquals("true", part(body, "deploy-changed-only"));
			assertEquals("acme", part(body, "tenant-id"));
		}
	}

	@Test
	void shouldSendTheSameNameOnEveryRun() throws Exception {
		Files.writeString(importDir.resolve("customer-support.bpmn"), "<bpmn/>");

		runner().run();
		runner().run();

		assertEquals(2, deploymentBodies.size());
		assertEquals("customer-support.bpmn", part(deploymentBodies.get(0), "deployment-name"));
		assertEquals("customer-support.bpmn", part(deploymentBodies.get(1), "deployment-name"));
		assertTrue(deploymentBodies.get(1).contains("filename=\"customer-support.bpmn\""),
				"the resource name must stay the file name, or Camunda compares different resources");
	}

	private Camunda7DataImportCommandRunner runner() {
		Camunda7DataImportCommandRunner runner = new Camunda7DataImportCommandRunner();
		ReflectionTestUtils.setField(runner, "importDir", importDir.toString());
		ReflectionTestUtils.setField(runner, "baseUrl",
				"http://127.0.0.1:" + camunda.getAddress().getPort() + "/engine-rest");
		ReflectionTestUtils.setField(runner, "tenantId", "acme");
		return runner;
	}

	/** Value of a simple (non-file) multipart form field. */
	private static String part(String body, String name) {
		Matcher m = Pattern.compile("name=\"" + Pattern.quote(name) + "\"\\r\\n(?:[^\\r\\n]+\\r\\n)*\\r\\n([^\\r\\n]*)")
				.matcher(body);
		return m.find() ? m.group(1) : null;
	}
}
