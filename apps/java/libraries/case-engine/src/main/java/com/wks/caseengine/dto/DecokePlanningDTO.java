package com.wks.caseengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Configuration
public class DecokePlanningDTO {

    private UUID id;
    private String monthName;
    private Integer ibr;
    private Integer mnt;
    private Integer shutdown;
    private Integer sad;
    private Integer bud;
    private Integer demoHSS;
    private Integer demoBBU;
    private Integer demoSAD;
    private Double fourFD;
    private Double fourF;
    private Double fiveF;
    private Double total;
    private Double fourFHours;
    private String aopYear;
    private UUID plantId;
    private String remarks;
}
