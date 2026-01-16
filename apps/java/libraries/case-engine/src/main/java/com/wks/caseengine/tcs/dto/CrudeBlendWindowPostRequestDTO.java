package com.wks.caseengine.tcs.dto;

import java.util.List;

import lombok.Data;

@Data
public class CrudeBlendWindowPostRequestDTO<T> {
    
    private List<T> results;
}


