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
public class UserScreenMappingDTO {

	private Long id;
	private String userId;
	private String plantFKId;
	private String verticalFKId;
	private String screenCode;

}
