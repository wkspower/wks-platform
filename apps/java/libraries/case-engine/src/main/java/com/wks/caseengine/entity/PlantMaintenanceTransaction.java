package com.wks.caseengine.entity;


import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "PlantMaintenanceTransaction")
public class PlantMaintenanceTransaction {

    @Id
    @Column(name = "Id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @Column(name = "Discription", nullable = false, length = 500)
    private String description;

    @Column(name = "MaintStartDateTime")
    private Date maintStartDateTime;

    @Column(name = "MaintEndDateTime")
    private Date maintEndDateTime;

    @Column(name = "DurationInMins")
    private Integer durationInMins;

    @Column(name = "MaintForMonth")
    private Date maintForMonth;

    @Column(name = "Rate")
    private Double rate;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "CreatedOn", nullable = false)
    private Date createdOn;

    @Column(name = "User", nullable = false, length = 255)
    private String user;

    @Column(name = "Version", nullable = false, length = 10)
    private String version;

    @Column(name = "PlantMaintenance_FK_Id")
    private UUID plantMaintenanceFkId;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;
}
