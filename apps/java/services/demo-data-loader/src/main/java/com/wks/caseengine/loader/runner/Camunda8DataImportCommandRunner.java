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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty("camunda8.data.import.enabled")
@Order(4)
@Slf4j
public class Camunda8DataImportCommandRunner implements CommandLineRunner {

	@Value("${camunda8.data.import.folder}")
	private String importDir;

	@Autowired
	private ZeebeClient zeebeClient;

	@Override
	public void run(String... args) throws IOException {
		log.info("Starting upload model to zeebe....");

		importData();

		log.info("Finish upload model to zeebe");
	}

	private void importData() throws IOException {
		if (importDir != null && !importDir.isEmpty()) {
			listFiles(importDir).forEach(fileName -> {
				try {
					zeebeClient.newDeployResourceCommand()
							.addResourceBytes(Files.readAllBytes(Paths.get(fileName)), fileName).send().join();
					log.info("Camunda process imported into Zeebe: {}", fileName);
				} catch (IOException e) {
					log.error("Error importing Camunda process into Zeebe: {}", fileName, e);
				}
			});
		}
	}

	private Set<String> listFiles(String dir) throws IOException {
		try (Stream<Path> stream = Files.list(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).filter(f -> f.toFile().getName().endsWith(".bpmn"))
					.map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.toSet());
		}
	}

}