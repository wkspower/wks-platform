package com.wks.caseengine.dto;

import java.util.List;

import lombok.Data;

@Data
public class MasterCrudeBlendDTO<T> {
    
    private List<String> headers;
    private List<String> keys;
    private List<T> results;
}
