package com.wks.caseengine.cpp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDemandUpdateResponse {
    
    private int totalReceived;
    private int successCount;
    private int failureCount;
    private List<String> errors;  // List of error messages for failed items
}
