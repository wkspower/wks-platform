package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity
@Table(name = "PlantProductionSummary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantProductionSummary {
    @Id
    @UuidGenerator
    @Column(name = "Id", nullable = false, unique = true)
    private UUID id;

    

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;

    
    @Column(name = "AOPYear", nullable = false, length = 255)
    private String aopYear;

    @Column(name = "Material", nullable = false, length = 255)
    private String material;

    @Column(name = "UOM", nullable = true, length = 255)
    private String uom;
    @Column(name = "Remark", nullable = true, length = 255)
    private String remark;

    
    @Column(name = "RowNo", nullable = true)
    private Integer rowNumber;

    @Column(name = "BudgetPrevYear", nullable = true)
    private Double budgetPrevYear;

    @Column(name = "ActualPrevYear", nullable = true)
    private Double actualPrevYear;

    @Column(name = "BudgetCurrentYear", nullable = true)
    private Double budgetCurrentYear;

     @Column(name = "VarBudgetMT", nullable = true)
     private Double varBudgetMT;

     @Column(name = "VarBudget", nullable = true)
     private Double varBudget;

    @Column(name = "VarActualMT", nullable = true)
     private Double varActualMT;

     @Column(name = "VarActual", nullable = true)
     private Double varActual;




    
    
}
