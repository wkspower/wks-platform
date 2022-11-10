package com.wks.caseengine.data.iimport;

import com.google.gson.JsonObject;

public interface DataImportService {

	void importData(final JsonObject data) throws Exception;

}
