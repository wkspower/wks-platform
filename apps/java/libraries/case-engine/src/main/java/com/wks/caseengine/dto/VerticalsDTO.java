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
public class VerticalsDTO {
	
    private String id;
    private String name;
    private String displayName;
    private Boolean isActive;
    private Integer displayOrder;

}
