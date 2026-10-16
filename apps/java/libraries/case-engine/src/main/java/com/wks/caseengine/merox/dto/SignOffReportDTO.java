package com.wks.caseengine.merox.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignOffReportDTO {
    
    private String id;
    private String remarks;
}
