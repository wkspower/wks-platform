package com.wks.caseengine.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Report_CapexPIOPlan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCapexPIOPlan {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Proposal")
    private String proposal;

    @Column(name = "Category")
    private String category;

    @Column(name = "Justification")
    private String justification;

    @Column(name = "CostRsCr")
    private Double costRsCr;

    @Column(name = "BenefitRsCr")
    private Double benefitRsCr;

    @Column(name = "TargetPlan")
    private String targetPlan;

    @Column(name = "StatusPlan")
    private String statusPlan;

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
