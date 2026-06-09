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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Brings the embedded dev-token issuer package ({@code com.wks.api.security.devtoken})
 * into component scanning. It is discovered because services component-scan
 * {@code com.wks.api.security.config} (the package of {@code ApiSecurityScan}).
 *
 * <p>Gated on {@code wks.auth.mode=dev-token}, so in the default {@code keycloak}
 * mode the devtoken package is not scanned at all.
 *
 * @author wks
 */
@Configuration
@ConditionalOnProperty(name = "wks.auth.mode", havingValue = "dev-token")
@ComponentScan(basePackages = { "com.wks.api.security.devtoken" })
public class DevTokenScan {

}
