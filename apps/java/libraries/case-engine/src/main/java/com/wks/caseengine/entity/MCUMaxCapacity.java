package com.wks.caseengine.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "MCUMaxCapacity")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class MCUMaxCapacity {

	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Site_FK_Id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID siteFkId;

    @Column(name = "Plant_FK_Id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID plantFkId;

    @Column(name = "Vertical_FK_Id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID verticalFkId;

    @Column(name = "Material_FK_Id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID materialFkId;

    @Column(name = "April")
    private double april;

    @Column(name = "May")
    private double may;

    @Column(name = "June")
    private double june;

    @Column(name = "July")
    private double july;

    @Column(name = "August")
    private double august;

    @Column(name = "September")
    private double september;

    @Column(name = "October")
    private double october;

    @Column(name = "November")
    private double november;

    @Column(name = "December")
    private double december;

    @Column(name = "January")
    private double january;

    @Column(name = "February")
    private double february;

    @Column(name = "March")
    private double march;

    @Column(name = "FinancialYear", length = 255)
    private String financialYear;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "CreatedOn")
    private LocalDateTime createdOn;

    @Column(name = "ModifiedOn")
    private LocalDateTime modifiedOn;

    @Column(name = "MCUVersion", length = 10)
    private String mcuVersion;

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;
}

