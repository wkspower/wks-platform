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
package com.wks.caseengine.data.export;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.caseengine.command.CommandExecutor;

@Component
public class DataExportServiceImpl implements DataExportService {

	private CommandExecutor commandExecutor;

	@Override
	public JsonObject export() throws Exception {
		return commandExecutor.execute(new ExportDataCmd());
	}

}
