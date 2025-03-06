package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SitesDTO {
	
	private String id;
    private String name;
    private String displayName;
    private Boolean isActive;
    private Integer displayOrder;
    private List<PlantsDTO> plants = new ArrayList<>(); // Add plants list

    @Override
    public String toString() {
        return "SitesDTO(id=" + id + ", name=" + name + ", displayName=" + displayName + 
               ", isActive=" + isActive + ", displayOrder=" + displayOrder + 
               ", plants=" + plants + ")";
    }

}
