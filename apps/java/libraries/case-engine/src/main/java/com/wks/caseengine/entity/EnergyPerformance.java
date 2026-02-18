package com.wks.caseengine.entity;


import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.*;




@Entity
@Table(name = "EnergyPerformance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyPerformance {

	 @Id
	 @GeneratedValue(generator = "UUID")
	 @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	 @Column(name = "Id", nullable = false, updatable = false)
	 private UUID id;
	 
    @Column(name = "Plant")
    private String plant;

    @Column(name = "UOM")
    private String uom;

    @Column(name = "AOPValue")
    private Double aopValue;

    @Column(name = "ActualValue")
    private Double actualValue;

    @Column(name = "PlanValue")
    private Double planValue;

    @Column(name = "Remark")
    private String remark;

    @Column(name = "SiteId")
    private UUID siteId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @Column(name = "UpdatedDateTime")
    private Date updatedDateTime;
}
