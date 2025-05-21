package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "NormsTransactions")
@Data
public class NormsTransactions {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "Id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "Plant_FK_Id", nullable = false)
	private UUID plantFkId;

	@Column(name = "AOPYear", nullable = false)
	private String aopYear;

	@Column(name = "NormParameter_FK_Id", nullable = false)
	private UUID normParameterFkId;

	@Column(name = "AOPMonth", nullable = false)
	private Integer aopMonth;

	@Column(name = "AttributeValue", nullable = false)
	private Double attributeValue;

	@Column(name = "Remark", length = 500)
	private String remark;

	@Column(name = "Version")
	private Integer version;

	@Column(name = "CreatedBy", length = 100)
	private String createdBy;

	@Column(name = "UpdatedBy", length = 100)
	private String updatedBy;

	@Column(name = "CreatedDateTime")
	private LocalDateTime createdDateTime;

	@Column(name = "UpdatedDateTime")
	private LocalDateTime updatedDateTime;

	@Column(name = "MCUNormsValue_FK_Id")
	private UUID mcuNormsValueFkId;
}
