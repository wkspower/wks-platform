package com.wks.caseengine.entity;

import lombok.*;

import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Elastomer_ShutdownHistoryConfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShutdownHistoryConfig {

	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Month")
    private Integer month;
    
    @Column(name = "PlantFKId")
    private UUID plantFKId;

    @Column(name = "Year", length = 4)
    private String year;

    @Column(name = "AOPYear", length = 8)
    private String aopYear;

    @Column(name = "Remark", length = 500)
    private String remark;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedOn")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ModifiedOn")
    private Date modifiedOn;

    @Column(name = "ModifiedBy", length = 100)
    private String modifiedBy;
}

