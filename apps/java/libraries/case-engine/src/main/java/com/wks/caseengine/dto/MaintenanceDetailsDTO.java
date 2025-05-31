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
	    private Double April;
	    private Double May;
	    private Double June;
	    private Double July;
	    private Double Aug;
	    private Double Sep;
	    private Double Oct;
	    private Double Nov;
	    private Double Dec;
	    private Double Jan;
	    private Double Feb;
	    private Double Mar;
	
}
