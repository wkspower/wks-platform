package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "AOPProposedNormsGradeWise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AOPProposedNormsGradeWise {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "NormparameterTypeDisplayName", length = 100)
    private String normparameterTypeDisplayName;

    @Column(name = "NormparameterDisplayName", length = 100)
    private String normparameterDisplayName;

    @Column(name = "UOM", length = 100)
    private String uom;

    // --- Previous Year Budget Fields ---
    private Double prevYearBudgetApril;
    private Double prevYearBudgetMay;
    private Double prevYearBudgetJune;
    private Double prevYearBudgetJuly;
    private Double prevYearBudgetAugust;
    private Double prevYearBudgetSeptember;
    private Double prevYearBudgetOctober;
    private Double prevYearBudgetNovember;
    private Double prevYearBudgetDecember;
    private Double prevYearBudgetJanuary;
    private Double prevYearBudgetFebruary;
    private Double prevYearBudgetMarch;

    // --- Current Year Budget Fields ---
    private Double currYearBudgetApril;
    private Double currYearBudgetMay;
    private Double currYearBudgetJune;
    private Double currYearBudgetJuly;
    private Double currYearBudgetAugust;
    private Double currYearBudgetSeptember;
    private Double currYearBudgetOctober;
    private Double currYearBudgetNovember;
    private Double currYearBudgetDecember;
    private Double currYearBudgetJanuary;
    private Double currYearBudgetFebruary;
    private Double currYearBudgetMarch;

    // --- Current Year Proposed Fields ---
    private Double currYearProposedApril;
    private Double currYearProposedMay;
    private Double currYearProposedJune;
    private Double currYearProposedJuly;
    private Double currYearProposedAugust;
    private Double currYearProposedSeptember;
    private Double currYearProposedOctober;
    private Double currYearProposedNovember;
    private Double currYearProposedDecember;
    private Double currYearProposedJanuary;
    private Double currYearProposedFebruary;
    private Double currYearProposedMarch;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "GradeId")
    private UUID gradeId;

    @Column(name = "PlantId")
    private UUID plantId;

    @Column(name = "AOPYear", length = 50)
    private String aopYear;

    @Column(name = "ModifiedBy", length = 50)
    private String modifiedBy;

    @Column(name = "ModifiedOn")
    private Date modifiedOn;

    @Column(name = "IsEditable")
    private Boolean isEditable;
}