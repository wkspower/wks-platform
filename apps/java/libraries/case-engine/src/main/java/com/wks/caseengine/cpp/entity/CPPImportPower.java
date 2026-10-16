package com.wks.caseengine.cpp.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CPPImportPower", schema = "dbo")
@Data
public class CPPImportPower {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ImportPlantFK_ID")
    private UUID importPlantFkId;

    @Column(name = "NormParameter_FK_Id")
    private UUID normParameterFkId;

    @Column(name = "Utility_NormParameter_FK_Id")
    private UUID utilityNormParameterFkId;

    @Column(name = "Apr")
    private BigDecimal apr;

    @Column(name = "May")
    private BigDecimal may;

    @Column(name = "Jun")
    private BigDecimal jun;

    @Column(name = "Jul")
    private BigDecimal jul;

    @Column(name = "Aug")
    private BigDecimal aug;

    @Column(name = "Sep")
    private BigDecimal sep;

    @Column(name = "Oct")
    private BigDecimal oct;

    @Column(name = "Nov")
    private BigDecimal nov;

    @Column(name = "Dec")
    private BigDecimal dec;

    @Column(name = "Jan")
    private BigDecimal jan;

    @Column(name = "Feb")
    private BigDecimal feb;

    @Column(name = "Mar")
    private BigDecimal mar;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "Site_FK_Id")
    private UUID siteFkId;

    @Column(name = "Vertical_FK_ID")
    private UUID verticalFkId;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;

    @Column(name = "CPPPlant_FK_ID")
    private UUID cppPlantFkId;
}
