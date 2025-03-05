package com.wks.caseengine.dto;

import java.util.Date;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import com.wks.caseengine.entity.Plants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SlowDownPlanDTO {

    private String discription;
    private Date maintStartDateTime;
    private Date maintEndDateTime;
    private Long durationInMins; 
    private Double rate;  // Added field for Rate
    private String remarks; // Added field for Remarks
    private String product;
    private UUID maintenanceId;

    }

