package com.wks.caseengine.entity;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Report_FixedExpenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFixedExpenses {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Particulars")
    private String particulars;

    @Column(name = "FYPrevAOP")
    private Double fyPrevAOP;

    @Column(name = "FYPrevActual")
    private Double fyPrevActual;

    @Column(name = "FYCurrAOP")
    private Double fyCurrAOP;

    @Column(name = "PercentageChange")
    private Double percentageChange;

    @Column(name = "Variance")
    private Double variance;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "SiteId")
    private UUID siteId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @Column(name = "UpdatedDate")
    private Date updatedDate;
}
