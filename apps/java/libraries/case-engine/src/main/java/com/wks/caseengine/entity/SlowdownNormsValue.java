package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "SlowdownNormsValue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlowdownNormsValue {

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

	@Column(name = "FinancialYear")
	private String financialYear;

	@Column(name = "Remarks")
	private String remarks;

	@Column(name = "CreatedOn")
	private Date createdOn;

	@Column(name = "ModifiedOn")
	private Date modifiedOn;

	@Column(name = "MCUVersion")
	private String mcuVersion;

	@Column(name = "UpdatedBy")
	private String updatedBy;
}
