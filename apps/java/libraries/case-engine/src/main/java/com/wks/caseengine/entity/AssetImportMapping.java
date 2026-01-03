package com.wks.caseengine.entity;

import lombok.Data;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "AssetImportMapping")
@Data
public class AssetImportMapping {

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
