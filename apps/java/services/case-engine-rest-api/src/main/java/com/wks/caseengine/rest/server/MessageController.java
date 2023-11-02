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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.caseengine.process.message.MessageSenderService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("message")
@Tag(name = "Message", description = "Send messages to processes")
public class MessageController {

	@Autowired
	private MessageSenderService messageSenderService;

	@PostMapping
	public ResponseEntity<Void> save(@RequestBody final ProcessMessage processMessage) {
		messageSenderService.sendMessage(processMessage, Optional.empty());
		return ResponseEntity.noContent().build();
	}

}
