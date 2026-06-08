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

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Brings the embedded dev-token issuer package into component scanning for any
 * service that already imports {@code ApiSecurityScan}.
 *
 * <p>
 * This configuration itself is unconditional and harmless: every bean in the
 * scanned {@code com.wks.api.security.devtoken} package is guarded by
 * {@code @ConditionalOnProperty(name = "wks.auth.mode", havingValue = "dev-token")},
 * so with the default {@code keycloak} mode nothing is registered.
 *
 * @author wks
 */
@Configuration
@ComponentScan(basePackages = { "com.wks.api.security.devtoken" })
public class DevTokenScan {

}
