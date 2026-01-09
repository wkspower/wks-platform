package com.wks.caseengine.dto.tcs;

import java.util.List;

import lombok.Data;

@Data
public class CrudeBlendWindowPostRequestDTO<T> {
    
    private List<T> results;
}
