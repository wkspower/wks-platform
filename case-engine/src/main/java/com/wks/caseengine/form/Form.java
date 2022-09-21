package com.wks.caseengine.form;

import com.google.gson.JsonObject;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Form {

	private String key;
	private String title;
	private String toolTip;
	private JsonObject structure;

}
