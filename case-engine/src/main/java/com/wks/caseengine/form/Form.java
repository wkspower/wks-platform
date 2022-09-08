package com.wks.caseengine.form;

import com.google.gson.JsonObject;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Form {

	public String key;
	public String description;
	public JsonObject structure;

}
