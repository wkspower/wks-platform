package com.wks.caseengine.entity;

import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "ConfigurationAccessMatrix")
@Data
public class ConfigurationAccessMatrix {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "Id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "VerticalId")
	private UUID verticalId;

	@Column(name = "SiteId")
	private UUID siteId;

	@Column(name = "PlantId")
	private UUID plantId;

	@Column(name = "ConfigurationTabs")
	private String configurationTabs;

}
