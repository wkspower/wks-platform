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
	    private String id;
	    private String name;
	    private String displayName;
	    private String siteFkId;
	    private String verticalFKId;
	    private Boolean isActive;
	    private Integer displayOrder;
	    @Override
	    public String toString() {
	        return "PlantsDTO(id=" + id + ", name=" + name + ", displayName=" + displayName + 
	               ", siteFkId=" + siteFkId + ", verticalFKId=" + verticalFKId + 
	               ", isActive=" + isActive + ", displayOrder=" + displayOrder + ")";
	    }
}
