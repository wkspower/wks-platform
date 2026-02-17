package com.wks.caseengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NormLineRequestDTO {

    private String lineId;
    private Map<String, String> grades;
}
