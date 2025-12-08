package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "PowerGenerationAssets")
@Data
public class PowerGenerationAssets {

    @Id
    @Column(name = "AssetId", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID assetId;

    @Column(name = "AssetName", length = 300)
    private String assetName;

    @Column(name = "AssetCapacity")
    private Double assetCapacity;
}
