package com.wks.caseengine.entity;

import lombok.*;

import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Elastomer_SlowdownHistoryConfig")
@Data
public class SlowdownHistoryConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Description")
    private String description;

    @Column(name = "MaintStartDateTime")
    private Date maintStartDateTime;

    @Column(name = "MaintEndDateTime")
    private Date maintEndDateTime;

    @Column(name = "DurationInMins")
    private Integer durationInMins;

    @Column(name = "MaintForMonth")
    private Integer maintForMonth;

    @Column(name = "AuditYear")
    private String auditYear;

    @Column(name = "Rate")
    private Double rate;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "UpdatedOn")
    private Date updatedOn;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;
}
