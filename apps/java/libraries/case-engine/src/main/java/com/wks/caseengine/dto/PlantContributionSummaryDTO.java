package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PlantContributionSummaryDTO {

    private UUID id;

    private Double actualLastYear;

    private Double actualTwoYearsAgo;

    private Double actualThreeYearsAgo;

    private Double actualFourYearsAgo;

    private Double budgetCurrent;

}
