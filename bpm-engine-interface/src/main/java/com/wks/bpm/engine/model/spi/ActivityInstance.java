package com.wks.bpm.engine.model.spi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ActivityInstance {

	private String id;
	private String activityId;
	private String activityType;

	private ActivityInstance[] childActivityInstances;

}
