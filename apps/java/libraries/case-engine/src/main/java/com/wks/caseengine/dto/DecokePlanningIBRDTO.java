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
public class DecokePlanningIBRDTO {

    private UUID id;
    private String furnace;
    private String ibrSDId;
    private String ibrEDId;
    private String taSDId;
    private String taEDId;
    private String sdSDId;
    private String sdEDId;
    private String ibrSD;
    private String ibrED;
    private String taSD;
    private String taED;
    private String sdSD;
    private String sdED;
    private String remarks;
}
