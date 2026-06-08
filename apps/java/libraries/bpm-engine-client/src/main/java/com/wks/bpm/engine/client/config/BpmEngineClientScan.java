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
package com.wks.bpm.engine.client.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Component-scans the BPM facade plus every engine module's {@code *Scan} by
 * package <em>name</em>, not by class. This is what decouples the contract
 * module from the concrete engines: {@code bpm-engine-client} no longer depends
 * on {@code c7-client} at compile time. Each engine's {@code *Scan} is
 * {@code @ConditionalOnProperty(wks.bpm.engine=...)}, so whichever engine jar a
 * service puts on its classpath (and the selected property) activates exactly
 * one {@code BpmEngineClient}. Scanning a package whose jar is absent is a no-op.
 *
 * <p><strong>One-way door:</strong> re-adding a compile dependency on a concrete
 * engine module (e.g. {@code c7-client}) regresses this decoupling.
 *
 * @author victor.franca
 */
@Configuration
@ComponentScan(basePackages = {
		"com.wks.bpm.engine.client.facade",
		"com.wks.bpm.engine.camunda.client.config",
		"com.wks.bpm.engine.noop.client.config" })
public class BpmEngineClientScan {

}
