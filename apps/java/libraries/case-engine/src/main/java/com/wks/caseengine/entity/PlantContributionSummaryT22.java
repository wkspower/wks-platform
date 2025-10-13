package com.wks.caseengine.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.GenericGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Entity
@Table(name = "PlantContributionSummaryT22")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantContributionSummaryT22 {

    @Column(name = "RowNo", nullable = false)
    private int rowNo;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "MatCode", length = 50)
    private String matCode;

    @Column(name = "Material", length = 500)
    private String material;

    @Column(name = "UOM", length = 50)
    private String uom;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Actual1")
    private Double actual1;

    @Column(name = "Actual2")
    private Double actual2;

    @Column(name = "Actual3")
    private Double actual3;

    @Column(name = "Actual4")
    private Double actual4;

    @Column(name = "BudgetCurrentYear")
    private Double budgetCurrentYear;

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

    @Column(name = "Plant_FK_Id", columnDefinition = "uniqueidentifier")
    private UUID plantFkId;

    @Column(name = "AOPYear", length = 50)
    private String aopYear;
    
    @Column(name="ReportType")
    private String reportType;

}
