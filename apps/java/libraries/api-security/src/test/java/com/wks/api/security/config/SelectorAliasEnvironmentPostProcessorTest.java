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
package com.wks.api.security.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class SelectorAliasEnvironmentPostProcessorTest {

	private final SelectorAliasEnvironmentPostProcessor processor = new SelectorAliasEnvironmentPostProcessor();

	@Test
	void newDatastoreSelectorMapsToLegacyKeyAndWinsOverLegacyDefault() {
		StandardEnvironment env = new StandardEnvironment();
		// Legacy default (as application.yml would declare) + the new public selector.
		env.getPropertySources().addLast(new MapPropertySource("yml", Map.of("database.type", "mongo")));
		env.getPropertySources().addLast(new MapPropertySource("user", Map.of("wks.datastore.type", "jpa")));

		processor.postProcessEnvironment(env, null);

		assertEquals("jpa", env.getProperty("database.type"),
				"new wks.datastore.type must win over the legacy database.type default");
	}

	@Test
	void newStorageSelectorMapsToLegacyDriverKey() {
		StandardEnvironment env = new StandardEnvironment();
		env.getPropertySources().addLast(new MapPropertySource("user", Map.of("wks.storage.driver", "filesystem")));

		processor.postProcessEnvironment(env, null);

		assertEquals("filesystem", env.getProperty("driver.storage.factoryclass"));
	}

	@Test
	void legacyKeyAloneIsUntouched() {
		StandardEnvironment env = new StandardEnvironment();
		env.getPropertySources().addLast(new MapPropertySource("yml", Map.of("database.type", "mongo")));

		processor.postProcessEnvironment(env, null);

		assertEquals("mongo", env.getProperty("database.type"));
		assertFalse(env.getPropertySources().contains(SelectorAliasEnvironmentPostProcessor.PROPERTY_SOURCE_NAME),
				"no alias source should be added when no new selector is set");
	}
}
