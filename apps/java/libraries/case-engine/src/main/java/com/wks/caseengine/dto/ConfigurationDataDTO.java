package com.wks.caseengine.dto;

import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigurationDataDTO {
    private UUID id;
    private String catalyst;
    private UUID catalystId;
    private String normParameterFKId;
    private String remark;
    private Map<String, Object> monthValues = new LinkedHashMap<>();

}
