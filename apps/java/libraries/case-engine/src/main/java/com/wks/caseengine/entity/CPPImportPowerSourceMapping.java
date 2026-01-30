package com.wks.caseengine.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CPPImportPowerSourceMapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CPPImportPowerSourceMapping {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false, columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "SourceName", nullable = false, length = 100)
    private String sourceName;

    @Column(name = "MaterialCode", length = 50)
    private String materialCode;

    @Column(name = "NormParameter_FK_Id", columnDefinition = "uniqueidentifier")
    private UUID normParameterFkId;

    @Column(name = "Plant_FK_Id", columnDefinition = "uniqueidentifier")
    private UUID plantFkId;

    @Column(name = "CPPPlant_FK_Id", columnDefinition = "uniqueidentifier")
    private UUID cppPlantFkId;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;

    @Column(name = "Remarks", length = 8000)
    private String remarks;
}
