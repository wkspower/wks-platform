package com.wks.caseengine.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import com.wks.caseengine.entity.GroupMaster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VerticalScreenMappingDTO {
	
	private Long id;
    private String verticalId;
    private String screenDisplayName;
    private String screenCode;
    private Integer sequence;
    private String route;
    private String menuJson;
    private String title;
    private String type;
    private String icon;
    private Boolean breadCrumbs;
    private GroupMaster group;


}
