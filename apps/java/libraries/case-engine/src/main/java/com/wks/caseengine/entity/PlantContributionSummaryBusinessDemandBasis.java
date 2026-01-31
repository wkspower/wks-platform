package com.wks.caseengine.entity;

import lombok.Data;

import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "PlantContributionSummaryBusinessDemandBasis")
@Data

public class PlantContributionSummaryBusinessDemandBasis {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ReportType")
    private String reportType;

    @Column(name = "RowNo")
    private Integer rowNo;

    @Column(name = "MatCode")
    private String matCode;

    @Column(name = "Material")
    private String material;

    @Column(name = "UOM")
    private String uom;

    @Column(name = "Price")
    private Integer price;

    @Column(name = "BestAchivedActual")
    private Double bestAchivedActual;

    @Column(name = "Design")
    private Double design;

    @Column(name = "Actual1")
    private Double actual1;

    @Column(name = "Actual2")
    private Double actual2;

    @Column(name = "Actual3")
    private Double actual3;

    @Column(name = "Actual4")
    private Double actual4;

    @Column(name = "Budget4")
    private Double budget4;

    @Column(name = "BudgetCurrentYear")
    private Double budgetCurrentYear;

    @Column(name = "BudgetCurrentYearCross")
    private Double budgetCurrentYearCross;

    @Column(name = "Actual1Cost")
    private Double actual1Cost;

    @Column(name = "Actual2Cost")
    private Double actual2Cost;

    @Column(name = "Actual3Cost")
    private Double actual3Cost;

    @Column(name = "Actual4Cost")
    private Double actual4Cost;

    @Column(name = "BudgetCurrentYearCost")
    private Double budgetCurrentYearCost;

    @Column(name = "ProposedNormCost")
    private Double proposedNormCost;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "GlobalBenchMark")
    private String globalBenchMark;

    @Column(name = "Remarks", length = 4000)
    private String remarks;
}