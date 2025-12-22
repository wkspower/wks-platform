package com.wks.caseengine.entity;

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
    @Size(max = 300)
    @Column(name = "ThroughputDuringSlowdown", length = 300)
    private String throughputDuringSlowdown;

    @NotNull(message = "Tentative Month is required")
    @Size(max = 50)
    @Column(name = "TentativeMonth", length = 50)
    private String tentativeMonth;

    @NotNull(message = "Purpose of Slowdown is required")
    @Size(max = 1000)
    @Column(name = "PurposeOfSlowdown", length = 1000)
    private String purposeOfSlowdown;

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
