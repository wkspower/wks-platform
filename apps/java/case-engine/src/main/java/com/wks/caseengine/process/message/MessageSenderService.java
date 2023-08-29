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
package com.wks.caseengine.process.message;

import java.util.Optional;

import com.google.gson.JsonArray;
import com.wks.bpm.engine.model.spi.ProcessMessage;

public interface MessageSenderService {

	void sendMessage(final ProcessMessage processMessage, final Optional<JsonArray> variables) throws Exception;

}
