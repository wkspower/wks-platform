package com.wks.caseengine.dto;

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
public class NormAttributeTransactionReceipeDTO {
	private String gradeName;
	private String receipeName;
	private String gradeFkId;
	private String reciepeFkId;
	private String attributeValue;

}
