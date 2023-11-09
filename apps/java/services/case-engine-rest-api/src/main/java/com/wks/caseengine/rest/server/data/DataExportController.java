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
package com.wks.caseengine.rest.server.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.wks.caseengine.data.export.DataExportService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("export")
@Tag(name = "Export")
public class DataExportController {

	@Autowired
	private DataExportService dataExportService;

	@GetMapping
	public JsonObject export() {
		return dataExportService.export();
	}

}
