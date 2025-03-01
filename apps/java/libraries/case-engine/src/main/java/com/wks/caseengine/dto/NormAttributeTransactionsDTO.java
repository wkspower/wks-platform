package com.wks.caseengine.dto;

import java.util.Date;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NormAttributeTransactionsDTO {
	
	private UUID normAttributeTransactionsId;
	private Integer month;
	private String attributeValue;
	private String auditYear;
	private UUID normParameterFKId;
}
