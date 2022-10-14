package com.wks.caseengine.bpm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BpmEngine {

	private String id;
	private String name;
	private BpmEngineType type;
	private String url;
	private String port;

}
