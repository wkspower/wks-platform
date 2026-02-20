package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "Elastomer_FinishingShutdownConfig", schema = "dbo")
@Data

public class FinishingShutdownConfig {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "Year")
    private Integer year;

    @Column(name = "Month")
    private Integer month;

    @Column(name = "ShutdownHours")
    private Double shutdownHours;

    @Column(name = "ShutdownDate")
    private Date shutdownDate;

    @Column(name = "Category")
    private Integer category;

    @Column(name = "AuditYear")
    private String auditYear;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "UpdatedOn")
    private Date updatedOn;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;
}