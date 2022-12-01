package com.wks.caseengine.data.iimport;

import java.io.File;
import java.util.List;

import com.google.gson.JsonObject;

public interface DataImportService {

	void importData(final JsonObject data) throws Exception;

}
