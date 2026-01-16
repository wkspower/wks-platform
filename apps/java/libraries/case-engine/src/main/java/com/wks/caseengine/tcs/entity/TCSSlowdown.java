package com.wks.caseengine.tcs.entity;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "TCSSlowdown")
@Data
public class TCSSlowdown {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "Tentative Duration (Days) is required")
    @Column(name = "TentativeDurationInDays")
    private Integer tentativeDurationInDays;

    @NotNull(message = "Throughput during the Slowdown is required")
    @Column(name = "ThroughputDuringSlowdown")
    private Double throughputDuringSlowdown;

    @NotNull(message = "Throughput UoM is required")
    @Column(name = "ThroughputUOM")
    private String throughputUOM;

    @NotNull(message = "Start Date is required")
    @Column(name = "StartDate")
    private Date startDate;

    @NotNull(message = "Purpose of Slowdown is required")
    @Size(max = 1000)
    @Column(name = "PurposeOfSlowdown", length = 1000)
    private String purpose;

    @Size(max = 20)
    @Column(name = "AOPYear", length = 20)
    private String aopYear;

    @Column(name = "Plant_FK_ID")
    private UUID plantFkId;

    @Column(name = "InsertedDateTime")
	private Date insertedDateTime;

	@Column(name = "UpdatedDateTime")
	private Date updatedDateTime;
}


