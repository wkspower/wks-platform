package com.wks.caseengine.entity;

import java.util.Date;
import java.util.UUID;
import jakarta.persistence.*;
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
    private String discription;

    @Column(name = "MaintStartDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date maintStartDateTime;

    @Column(name = "MaintEndDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date maintEndDateTime;

    @Column(name = "DurationInMins")
    private Integer durationInMins;

    @Column(name = "MaintForMonth")
    @Temporal(TemporalType.TIMESTAMP)
    private Date maintForMonth;
    
    @Column(name = "AuditYear")
    private String auditYear;

    @Column(name = "Rate")
    private Double rate;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "CreatedOn", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "[User]", nullable = false, length = 255)
    private String user;

    @Column(name = "Version", nullable = false, length = 10)
    private String version;

    @Column(name = "PlantMaintenance_FK_Id")
    private UUID plantMaintenanceFkId;
    
    @Column(name = "NormParameter_FK_Id")
    private UUID normParametersFKId;
    
    @Transient
    private Double durationInHrs;

    @PrePersist
    protected void onCreate() {
        if (createdOn == null) {
            createdOn = new Date();
        }
    }
}
