package com.wks.caseengine.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;


@Entity
@Table(name = "ExclusionDate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExclusionDate {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "startDate")
    private Date startDate;

    @Column(name = "endDate")
    private Date endDate;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "revison") 
    private Integer revision;

    @Column(name = "plantId")
    private UUID plantId;

    @Column(name = "aopYear")
    private String aopYear;

    @Column(name = "modifiedBy")
    private String modifiedBy;

    @Column(name = "modifiedOn")
    private Date modifiedOn;


}