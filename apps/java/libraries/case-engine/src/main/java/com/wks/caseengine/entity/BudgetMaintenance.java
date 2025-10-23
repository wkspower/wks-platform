package com.wks.caseengine.entity;

import lombok.*;


import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "BudgetMaintenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetMaintenance {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "Id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "PlantId", nullable = false)
	private UUID plantId;

	@Column(name = "PlantName")
	private String plantName;

	@Column(name = "CostName")
	private String costName;

	@Column(name = "BudgetType")
	private String budgetType;

	@Column(name = "BudgetCategory")
	private String budgetCategory;

	@Column(name = "Apr")
	private double apr;

	@Column(name = "May")
	private double may;

	@Column(name = "Jun")
	private double jun;

	@Column(name = "Jul")
	private double jul;

	@Column(name = "Aug")
	private double aug;

	@Column(name = "Sep")
	private double sep;

	@Column(name = "Oct")
	private double oct;

	@Column(name = "Nov")
	private double nov;

	@Column(name = "Dec")
	private double dec;

	@Column(name = "Jan")
	private double jan;

	@Column(name = "Feb")
	private double feb;

	@Column(name = "Mar")
	private double mar;

	@Column(name = "Remark")
	private String remark;

	@Column(name = "AOPYear")
	private String aopYear;
	
	@Column(name = "IsEditable")
	private Boolean isEditable;
	
	@Column(name = "UpdatedBy")
	private String updatedBy;
	
	@Column(name = "ModifiedOn")
	private Date modifiedOn;
	
	@Column(name = "Symbol")
	private String symbol;
	
	@Column(name="PercentChange")
	private Double percentChange;
}
