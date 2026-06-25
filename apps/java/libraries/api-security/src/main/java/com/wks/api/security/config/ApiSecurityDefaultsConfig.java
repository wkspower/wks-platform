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

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Contributes the {@code api-security} library defaults from
 * {@code api-security-defaults.properties}, so the security properties this
 * library binds via {@code @Value} (issuer URL, OPA URL, tenant claim name,
 * dev-token issuer settings) have a single home and are not hardcoded in the
 * Java sources.
 *
 * <p>
 * {@code @PropertySource} is loaded at the lowest config precedence (below any
 * {@code application.yml}, profile fragment or environment variable in the
 * consuming service), so services keep full control while every consumer —
 * including ones that only scan the library inertly, such as
 * {@code c7-external-tasks} — resolves the keys without re-declaring them.
 *
 * <p>
 * This is the working alternative to the
 * {@link SelectorAliasEnvironmentPostProcessor}, which does not run at boot under
 * Spring Boot 4 (see issue #480).
 *
 * @author wks
 */
@Configuration
@PropertySource("classpath:api-security-defaults.properties")
public class ApiSecurityDefaultsConfig {

}
