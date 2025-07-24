package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;


@Entity
@Table(name = "SlowdownConsumption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlowdownConsumption {
	
	@Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
	
    @Column(name = "Plant_FK_Id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID plantFkId;

    @Column(name = "AOPYear", length = 20, nullable = false)
    private String aopYear;

    @Column(name = "PlantMaintenance_FK_Id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID plantMaintenanceFkId;

    @Column(name = "NormParameter_FK_Id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID normParameterFkId;

    @Column(name = "AOPMonth", nullable = false)
    private Integer aopMonth;

    @Column(name = "ParameterValue")
    private Double parameterValue;

    @Column(name = "Remarks", length = 300)
    private String remarks;

    @Column(name = "CreatedOn", nullable = false)
    private Date createdOn;

    @Column(name = "CreatedBy", length = 100, nullable = false)
    private String createdBy;

    @Column(name = "UpdatedOn")
    private Date updatedOn;

    @Column(name = "UpdatedBy", length = 100)
    private String updatedBy;


}
