package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Report_ShutdownSlowdownPlan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportShutdownSlowdownPlan {

	 @Id
	    @GeneratedValue(generator = "UUID")
	    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	    @Column(name = "Id", updatable = false, nullable = false)
	    private UUID id;

    @Column(name = "Plant")
    private String plant;

    @Column(name = "NoOfShutdownDays")
    private Double noOfShutdownDays;

    @Column(name = "NoOfSlowdownDays")
    private Double noOfSlowdownDays;

    @Column(name = "MonthPlan")
    private String monthPlan;

    @Column(name = "ShutdownSlowdownPlan")
    private String shutdownSlowdownPlan;

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
