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
package com.wks.bpm.engine.noop.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Brings the no-op BPM engine client into component scanning when
 * {@code wks.bpm.engine=none}. Mirrors {@code Camunda7ClientScan}: the contract
 * module {@code bpm-engine-client} scans this package by name (off the
 * classpath), so adding/removing the {@code noop-bpm-engine-client} jar — not a
 * compile-time dependency — is what makes the no-op engine available.
 *
 * @author wks
 */
@Configuration
@ConditionalOnProperty(name = "wks.bpm.engine", havingValue = "none")
@ComponentScan(basePackages = { "com.wks.bpm.engine.noop.client" })
public class NoopBpmEngineClientScan {

}
