package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity
@Table(name = "MonthWiseProductionPlan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthWiseProductionPlan {
    @Id
    @UuidGenerator
    @Column(name = "Id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "RowNo", nullable = true)
    private Integer rowNumber;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;

    @Column(name = "AOPYear", nullable = false, length = 255)
    private String aopYear;

    @Column(name = "Remark", nullable = true, length = 255)
    private String remark;

    @Column(name = "Month", nullable = true, length = 255)
    private String month;

    // @Column(name = "durationInHrs", nullable = true)
    // private Double durationInHrs;

    @Column(name = "EOEProdActual", nullable = true)
    private Double eoeProdActual;

    @Column(name = "EOEProdBudget", nullable = true)
    private Double eoeProdBudget;

    @Column(name = "EOEThroughput", nullable = true)
    private Double eoeThroughput;

    @Column(name = "EOThroughput", nullable = true)
    private Double eoThroughput;

    @Column(name = "MEGThroughput", nullable = true)
    private Double megThroughput;

    @Column(name = "OpHrsActual", nullable = true)
    private Double opHrsActual;

    @Column(name = "OpHrsBudget", nullable = true)
    private Double opHrsBudget;

    @Column(name = "ThroughputActual", nullable = true)
    private Double throughputActual;

    @Column(name = "ThroughputBudget", nullable = true)
    private Double throughputBudget;

    @Column(name = "TotalEOE", nullable = true)
    private Double totalEOE;

    @Column(name = "OperatingHours", nullable = true)
    private Double operatingHours;

}
