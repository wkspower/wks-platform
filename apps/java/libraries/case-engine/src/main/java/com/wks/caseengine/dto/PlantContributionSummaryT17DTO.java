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
public class PlantContributionSummaryT17DTO {
    private Object sno;
    private String id;
    private String material;
    private Double price;
    private String uom;
    private Double design;
    private Double designRsMt;
    private Double bestAchivedActual;
    private Double bestAchivedActualRsMT;
    private String globalBenchmark;
    private Double globalBenchmarkRsMT;
    private Double budgetPrevYear;
    private Double budgetPrevYearRsMT;
    private Double actualPrevYear;
    private Double actualPrevYearRsMT;
    private Double proposedBudget;
    private Double proposedBudgetRsMT;
    private Object plantFkId;
    private Object aopYear;
    private String remarks;
}