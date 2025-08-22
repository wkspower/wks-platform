package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "MCUDesignCapacity")
// Lombok annotations
@Getter
@Setter
@NoArgsConstructor
@ToString
public class MCUDesignCapacity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Material_FK_Id", nullable = false)
    private UUID materialFkId;

    @Column(name = "April")
    private Double april;

    @Column(name = "May")
    private Double may;

    @Column(name = "June")
    private Double june;

    @Column(name = "July")
    private Double july;

    @Column(name = "August")
    private Double august;

    @Column(name = "September")
    private Double september;

    @Column(name = "October")
    private Double october;

    @Column(name = "November")
    private Double november;

    @Column(name = "December")
    private Double december;

    @Column(name = "January")
    private Double january;

    @Column(name = "February")
    private Double february;

    @Column(name = "March")
    private Double march;

    @Column(name = "FinancialYear", length = 255)
    private String financialYear;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "CreatedOn")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "ModifiedOn")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;

    @Column(name = "UpdatedBy", length = 2555)
    private String updatedBy;

    @Column(name = "PlantId")
    private UUID plantId;
}
