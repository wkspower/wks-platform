package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Switch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
//@JsonInclude(Include.ALWAYS)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class WorkflowYearDTO {

	private String particulates;
	private String uom;
	private String fyAop;
	private String fyActual;
	private String syAop;
	private String remark;
	private String aopType;
	private String aopYear;

}
