package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Data
@Table(name = "PlantMaintenance")
public class PlantMaintenance {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "MaintenanceText", nullable = false, length = 1000)
    private String maintenanceText;

    @Column(name = "IsDefault")
    private Boolean isDefault;

    @Column(name = "MaintenanceType_FK_Id")
    private UUID maintenanceTypeFkId;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;
}
