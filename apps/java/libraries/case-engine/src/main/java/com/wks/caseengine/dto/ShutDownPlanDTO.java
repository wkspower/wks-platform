package com.wks.caseengine.dto;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wks.caseengine.entity.Plants;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ShutDownPlanDTO {

    private String discription;
    private Date maintStartDateTime;
    private Date maintEndDateTime;
    private Integer durationInMins; 
	private String id; 
	private Double rate;
	private String remark;
	private UUID productId;
	private String maintenanceTypeName;
	private Double durationInHrs;
	private Double durationInDays;
	private String product;
	private UUID plantId;
	private String audityear;
	private Integer displayOrder;
	private String verticalName;
	private Date createdOn;
	private String plantMaintenanceTransactionName;
	private String productName;
	private Double rateEO;
	private Double rateEOE;
}
