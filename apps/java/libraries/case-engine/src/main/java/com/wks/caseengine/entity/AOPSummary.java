package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "AOPSummary")
@Data
public class AOPSummary {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "Id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "Plant_Fk_Id", nullable = false)
	private UUID plantFkId;

	@Column(name = "AOPYear", nullable = false)
	private String aopYear;

	@Column(name = "Summary", columnDefinition = "NVARCHAR(MAX)")
	private String summary;

	@Column(name = "UpdatedBy", length = 100)
	private String updatedBy;

	@Column(name = "UpdatedDateTime")
	private LocalDateTime updatedDateTime;
}
