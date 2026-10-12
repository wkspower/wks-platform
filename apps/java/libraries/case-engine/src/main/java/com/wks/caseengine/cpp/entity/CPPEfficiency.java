package com.wks.caseengine.cpp.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CPP_Efficiency", schema = "dbo")
@Data
public class CPPEfficiency {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "CPPPlant_FK_Id", nullable = false)
    private UUID cppPlantFkId;

    @Column(name = "Asset_FK_Id")
    private UUID assetFkId;

    @Column(name = "AssetName", nullable = false, length = 100)
    private String assetName;

    @Column(name = "Type", length = 50)
    private String type;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "UOM", length = 50)
    private String uom;

    @Column(name = "Apr")
    private Double apr;
    @Column(name = "May")
    private Double may;
    @Column(name = "Jun")
    private Double jun;
    @Column(name = "Jul")
    private Double jul;
    @Column(name = "Aug")
    private Double aug;
    @Column(name = "Sep")
    private Double sep;
    @Column(name = "Oct")
    private Double oct;
    @Column(name = "Nov")
    private Double nov;
    @Column(name = "Dec")
    private Double dec;
    @Column(name = "Jan")
    private Double jan;
    @Column(name = "Feb")
    private Double feb;
    @Column(name = "Mar")
    private Double mar;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "AOPYear", nullable = false, length = 20)
    private String aopYear;

    @Column(name = "CreatedDate", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
}
