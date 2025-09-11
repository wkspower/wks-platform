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
    private double price;

    @Column(name = "Actual1")
    private double actual1;

    @Column(name = "Actual2")
    private double actual2;

    @Column(name = "Actual3")
    private double actual3;

    @Column(name = "Actual4")
    private double actual4;

    @Column(name = "BudgetCurrentYear")
    private double budgetCurrentYear;

    @Column(name = "Actual1Cost")
    private double actual1Cost;

    @Column(name = "Actual2Cost")
    private double actual2Cost;

    @Column(name = "Actual3Cost")
    private double actual3Cost;

    @Column(name = "Actual4Cost")
    private double actual4Cost;

    @Column(name = "BudgetCurrentYearCost")
    private double budgetCurrentYearCost;

    @Column(name = "Plant_FK_Id", columnDefinition = "uniqueidentifier")
    private UUID plantFkId;

    @Column(name = "AOPYear", length = 50)
    private String aopYear;
    
    @Column(name="ReportType")
    private String reportType;

}
