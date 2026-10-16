package com.wks.caseengine.pcg.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ShutdownTaTransactionDTO {

    private String id;
    private String name;
    private String description;
    private Double durationInHrs;
    private Date maintStartDateTime;
    private Date maintEndDateTime;
    private Integer durationInMins;
    private String maintForMonth;
    private String auditYear;
    private String remarks;
    private String createdOn;
    private String user;
    private String version;
    private String plantMaintenanceFKId;
    private String normParameterFKId;
    private String plantFKId;
    private String saveStatus;
    private String errDescription;
}
