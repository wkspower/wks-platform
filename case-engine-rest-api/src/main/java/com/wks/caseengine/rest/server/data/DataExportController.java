package com.wks.caseengine.rest.server.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.wks.caseengine.data.export.DataExportService;

@RestController
@RequestMapping("export")
public class DataExportController {

	@Autowired
	private DataExportService dataExportService;

	@GetMapping(value = "/")
	public JsonObject export() throws Exception {
		return dataExportService.export();
	}

}
