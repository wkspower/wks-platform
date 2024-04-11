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
package com.wks.bpm.engine.model.spi;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author victor.franca
 *
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProcessMessage {

	private String messageCode;
	private Optional<List<ProcessVariable>> processVariables;

}
