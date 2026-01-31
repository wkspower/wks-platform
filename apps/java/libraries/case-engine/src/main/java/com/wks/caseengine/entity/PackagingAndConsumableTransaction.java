package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.GenericGenerator;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "PackagingAndConsumableTransaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagingAndConsumableTransaction {

	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "MaterialId")
    private UUID materialId;

    @Column(name = "PackagingPrice")
    private Double packagingPrice;

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