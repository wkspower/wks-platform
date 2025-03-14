package com.wks.caseengine.entity;

import java.util.UUID;

import com.google.gson.JsonObject;
import com.wks.caseengine.entity.converter.JsonConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "record_type")
@Getter
@Setter
public class RecordTypeEntity {

	@Id
	@Column(name="uid")
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID uid;
	
	@Column(name="id")
	private String id;
	
	@Column(name = "fields", columnDefinition = "TEXT")
	@Convert(converter = JsonConverter.class)
	private JsonObject fields;
	
}
