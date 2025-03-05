package com.wks.caseengine.dto;

import java.util.ArrayList;

import org.springframework.context.annotation.Configuration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

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
    private List<SitesDTO> sites = new ArrayList<>(); // Add sites list

    @Override
    public String toString() {
        return "VerticalsDTO(id=" + id + ", name=" + name + ", displayName=" + displayName + 
               ", isActive=" + isActive + ", displayOrder=" + displayOrder + 
               ", sites=" + sites + ")";
    }

}
