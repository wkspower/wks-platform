package com.wks.caseengine.rest.server.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.wks.caseengine.data.iimport.DataImportService;

@RestController
@RequestMapping("import")
public class DataImportController {

	@Autowired
	private DataImportService dataImportService;

	@PostMapping(value = "/")
	public void importData(@RequestBody final JsonObject data) throws Exception {
		dataImportService.importData(data);
	}

}
