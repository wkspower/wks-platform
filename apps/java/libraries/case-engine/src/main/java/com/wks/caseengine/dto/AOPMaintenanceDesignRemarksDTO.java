package com.wks.caseengine.dto;


import lombok.*;

import java.util.Date;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Configuration
public class AOPMaintenanceDesignRemarksDTO {

    private UUID id;
    private UUID plantFkId;
    private String aopYear;
    private String summary;
    private String updatedBy;
    private Date updatedDateTime;

}
