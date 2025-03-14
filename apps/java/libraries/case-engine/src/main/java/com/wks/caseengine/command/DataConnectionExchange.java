package com.wks.caseengine.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface DataConnectionExchange {

	JsonObject exportFromDatabase(Gson gson);

	void importToDatabase(JsonObject data, Gson gson);

}
