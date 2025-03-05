package com.wks.caseengine.dto;

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
public class PlantsDTO {
	    private UUID id;
	    private String name;
	    private String displayName;
	    private UUID siteFkId;
	    private UUID verticalFKId;
	    private Boolean isActive;
	    private Integer displayOrder;
}
