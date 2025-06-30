package com.wks.caseengine.entity;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;




@Entity
@Table(name = "MCUNormsValueGrade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MCUNormsValueGrade {
	
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Site_FK_Id")
    private UUID siteFkId;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;

    @Column(name = "Vertical_FK_Id")
    private UUID verticalFkId;

    @Column(name = "Material_FK_Id")
    private UUID materialFkId;

    @Column(name = "Grade_FK_Id")
    private UUID gradeFkId;

    @Column(name = "NormParameterType_FK_Id")
    private UUID normParameterTypeFkId;

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
    private Date createdOn;

    @Column(name = "ModifiedOn")
    private Date modifiedOn;

    @Column(name = "MCUVersion", length = 10)
    private String mcuVersion;

    @Column(name = "UpdatedBy", length = 255)
    private String updatedBy;

}
