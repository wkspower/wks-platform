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

    private double actualLastYear;

    private double actualTwoYearsAgo;

    private double actualThreeYearsAgo;

    private double actualFourYearsAgo;

    private double budgetCurrent;

}
