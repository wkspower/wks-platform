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
	
	 private String name;
	    private Float april;
	    private Float may;
	    private Float june;
	    private Float july;
	    private Float august;
	    private Float september;
	    private Float october;
	    private Float november;
	    private Float december;
	    private Float january;
	    private Float february;
	    private Float march;

}
