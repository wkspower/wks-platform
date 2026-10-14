package com.wks.caseengine.pcg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GasifierDropdownAopBasisDTO {

    private String id;
    private String name;
    private String displayName;
    private String configuration;
}
