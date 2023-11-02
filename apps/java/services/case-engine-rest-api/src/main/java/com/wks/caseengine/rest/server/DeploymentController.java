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
package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.client.BpmEngineClientFacade;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("deployment")
@Tag(name = "Deployment")
public class DeploymentController {

	// TODO replace this hard code
	private static final String FILE_NAME_BPMN = "fileName.bpmn";

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@PostMapping
	public ResponseEntity<Void> deploy(@RequestBody String file) {
		processEngineClient.deploy(FILE_NAME_BPMN, new String(file.getBytes()));
		return ResponseEntity.noContent().build();
	}

}
