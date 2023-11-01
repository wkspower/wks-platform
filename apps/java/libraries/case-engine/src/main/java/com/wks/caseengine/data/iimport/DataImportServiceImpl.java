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
package com.wks.caseengine.data.iimport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.caseengine.command.CommandExecutor;

@Component
public class DataImportServiceImpl implements DataImportService {

	@Autowired
	private CommandExecutor commandExecutor;

	@Override
	public void importData(JsonObject data) throws Exception {
		commandExecutor.execute(new ImportaDataCmd(data));
	}

}
