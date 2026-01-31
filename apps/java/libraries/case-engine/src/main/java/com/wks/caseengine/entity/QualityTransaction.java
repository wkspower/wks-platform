package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "QualityTransaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityTransaction {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "MaterialId")
    private UUID materialId;

    @Column(name = "PrevBudget")
    private Double prevBudget;

    @Column(name = "PrevActual")
    private Double prevActual;

    @Column(name = "ProposedNorm")
    private Double proposedNorm;

    @Column(name = "PlantId")
    private UUID plantId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @Column(name = "ModifiedOn")
    private Date modifiedOn;

    @Column(name = "Remark")
    private String remark;
}