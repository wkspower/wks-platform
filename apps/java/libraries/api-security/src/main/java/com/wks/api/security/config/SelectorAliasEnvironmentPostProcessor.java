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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Bridges the public {@code wks.*} selector names to the legacy property keys that
 * the many {@code @ConditionalOnProperty} sites still bind to, so concerns can be
 * selected with the consistent {@code wks.<concern>.*} convention without renaming
 * (and breaking) every conditional. The legacy keys remain fully honored.
 *
 * <p>When a new key is set it is mapped onto the legacy key with highest precedence
 * (so it wins over a legacy default declared in application.yml). When only the
 * legacy key is set, nothing changes.
 *
 * <ul>
 *   <li>{@code wks.datastore.type}  -&gt; {@code database.type}</li>
 *   <li>{@code wks.storage.driver}  -&gt; {@code driver.storage.factoryclass}</li>
 * </ul>
 *
 * Runs last (LOWEST_PRECEDENCE) so config data (application.yml / profiles) is
 * already loaded and the new keys are visible.
 */
public class SelectorAliasEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	/** new public selector -> legacy key bound by existing @ConditionalOnProperty. */
	private static final Map<String, String> ALIASES = Map.of(
			"wks.datastore.type", "database.type",
			"wks.storage.driver", "driver.storage.factoryclass");

	static final String PROPERTY_SOURCE_NAME = "wksSelectorAliases";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Map<String, Object> mapped = new LinkedHashMap<>();
		ALIASES.forEach((newKey, legacyKey) -> {
			String value = environment.getProperty(newKey);
			if (value != null && !value.isBlank()) {
				mapped.put(legacyKey, value);
			}
		});
		if (!mapped.isEmpty()) {
			environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, mapped));
		}
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
