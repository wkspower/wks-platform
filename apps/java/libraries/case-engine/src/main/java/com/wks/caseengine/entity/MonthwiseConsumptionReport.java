package com.wks.caseengine.entity;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MonthwiseConsumptionReport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthwiseConsumptionReport {
	
	@Id
    @UuidGenerator
    @Column(name = "Id", nullable = false, unique = true)
    private UUID id;

	@Column(name = "Plant_FK_Id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID plantFkId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "ReportType")
    private String reportType;

    @Column(name = "NormType")
    private String normType;

    @Column(name = "Material")
    private String material;

    @Column(name = "RowNo")
    private Integer rowNo;

    @Column(name = "UOM")
    private String uom;

    @Column(name = "Spec")
    private String spec;

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

    @Column(name = "Total")
    private Double total;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "CreatedOn")
    private Date createdOn;

    @Column(name = "ModifiedOn")
    private Date modifiedOn;


}
