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
@Table(name = "form")
@Getter
@Setter
public class FormEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="uid")
    private UUID uid;

    @Column(name = "form_key", unique = true, nullable = false)
    private String key;

    @Column(name = "title")
    private String title;

    @Column(name = "tool_tip")
    private String toolTip;

    @Column(name = "structure", columnDefinition = "TEXT")
    @Convert(converter = JsonConverter.class)
    private JsonObject structure;
    
}
