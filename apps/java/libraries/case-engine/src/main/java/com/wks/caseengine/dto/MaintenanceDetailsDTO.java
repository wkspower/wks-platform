package com.wks.caseengine.dto;

import java.util.Date;

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
public class MaintenanceDetailsDTO {
	
	
	    private String Name;
	    private Float April;
	    private Float May;
	    private Float June;
	    private Float July;
	    private Float Aug;
	    private Float Sep;
	    private Float Oct;
	    private Float Nov;
	    private Float Dec;
	    private Float Jan;
	    private Float Feb;
	    private Float Mar;
	
}
