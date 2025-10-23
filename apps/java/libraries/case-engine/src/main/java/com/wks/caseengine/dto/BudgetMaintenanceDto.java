package com.wks.caseengine.dto;



import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetMaintenanceDto {

    private UUID id;

    private UUID plantId;

    private String plantName;

    private String costName;

    private String budgetType;

    private String budgetCategory;

    private Double apr;

    private Double may;

    private Double jun;

    private Double jul;

    private Double aug;

    private Double sep;

    private Double oct;

    private Double nov;

    private Double dec;

    private Double jan;

    private Double feb;

    private Double mar;

    private String remark;

    private String aopYear;
    
	private Boolean isEditable;
	
	private String updatedBy;
	
	private Date modifiedOn;
	
	private String saveStatus;
	private String errDescription;
	 private String tableId;
	 private String symbol;
	 private Double percentChange;
	 private Integer sequence;
}

