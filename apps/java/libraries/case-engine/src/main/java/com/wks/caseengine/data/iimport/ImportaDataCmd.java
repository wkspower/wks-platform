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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.command.DataConnectionExchange;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class ImportaDataCmd implements Command<Void> {

	private JsonObject data;

	@Override
	public Void execute(CommandContext commandContext) {
		GsonBuilder gsonBuilder = commandContext.getGsonBuilder();
		Gson json = gsonBuilder.create();
		DataConnectionExchange dataConnectionExchange = commandContext.getDataConnectionExchange();
		dataConnectionExchange.importToDatabase(data, json);
		return null;
	}

}
