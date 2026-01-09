package com.wks.caseengine.entity;

import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "PlantImportMapping")
@Data
public class PlantImportMapping {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false, columnDefinition = "uniqueidentifier")
    private UUID id;

    @NotNull(message = "AssetId is required")
    @Column(name = "AssetId", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID assetId;

    @NotNull(message = "FinancialMonthId is required")
    @Column(name = "FinancialMonthId", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID financialMonthId;

    @NotNull(message = "Value is required")
    @Column(name = "Value", precision = 18, scale = 2, nullable = false)
    private Double value;

    @NotNull
    @Column(name = "UOM", length = 50, nullable = false)
    private String uom;

    @Column(name = "Remarks", length = 8000, nullable = true)
    private String remarks;
}
