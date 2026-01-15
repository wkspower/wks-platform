package com.wks.caseengine.tcs.entity;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "TCSShutdown")
@Data
public class TCSShutdown {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "SD Total Duration (Days) is required")
    @Column(name = "SDTotalDurationInDays")
    private Integer sdTotalDurationInDays;

    @NotNull(message = "Start Date is required")
    @Column(name = "StartDate")
    private Date startDate;

    @NotNull(message = "Purpose of Shutdown is required")
    @Size(max = 1000)
    @Column(name = "Purpose", length = 1000)
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


