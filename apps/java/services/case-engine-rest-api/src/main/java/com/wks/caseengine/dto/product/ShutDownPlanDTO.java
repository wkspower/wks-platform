package com.wks.caseengine.dto.product;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Data
public class ShutDownPlanDTO {

    private String discription;
    private Date maintStartDateTime;
    private Date maintEndDateTime;
	// Change from Integer to Long
    private Integer durationInMins; 
    
	//FOR ID : pmt.Id
	private UUID maintenanceId; 
	

	private Double rate;
	private String remark;
	private UUID productId;
	private String maintenanceTypeName;
	private Double durationInHrs;
	private Double durationInDays;
	private String product;
}
