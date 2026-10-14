package com.wks.caseengine.cpp.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CPP_FuelRatio", schema = "dbo")
@Data
public class CPPFuelRatio {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "CPPPlant_FK_Id", nullable = false)
    private UUID cppPlantFkId;

    @Column(name = "Fuel_FK_Id")
    private UUID fuelFkId;

    @Column(name = "FuelName", nullable = false, length = 100)
    private String fuelName;

    @Column(name = "GCV_Apr")
    private Double gcvApr;
    @Column(name = "GCV_May")
    private Double gcvMay;
    @Column(name = "GCV_Jun")
    private Double gcvJun;
    @Column(name = "GCV_Jul")
    private Double gcvJul;
    @Column(name = "GCV_Aug")
    private Double gcvAug;
    @Column(name = "GCV_Sep")
    private Double gcvSep;
    @Column(name = "GCV_Oct")
    private Double gcvOct;
    @Column(name = "GCV_Nov")
    private Double gcvNov;
    @Column(name = "GCV_Dec")
    private Double gcvDec;
    @Column(name = "GCV_Jan")
    private Double gcvJan;
    @Column(name = "GCV_Feb")
    private Double gcvFeb;
    @Column(name = "GCV_Mar")
    private Double gcvMar;

    @Column(name = "PercentageByWt_Apr")
    private Double percentageByWtApr;
    @Column(name = "PercentageByWt_May")
    private Double percentageByWtMay;
    @Column(name = "PercentageByWt_Jun")
    private Double percentageByWtJun;
    @Column(name = "PercentageByWt_Jul")
    private Double percentageByWtJul;
    @Column(name = "PercentageByWt_Aug")
    private Double percentageByWtAug;
    @Column(name = "PercentageByWt_Sep")
    private Double percentageByWtSep;
    @Column(name = "PercentageByWt_Oct")
    private Double percentageByWtOct;
    @Column(name = "PercentageByWt_Nov")
    private Double percentageByWtNov;
    @Column(name = "PercentageByWt_Dec")
    private Double percentageByWtDec;
    @Column(name = "PercentageByWt_Jan")
    private Double percentageByWtJan;
    @Column(name = "PercentageByWt_Feb")
    private Double percentageByWtFeb;
    @Column(name = "PercentageByWt_Mar")
    private Double percentageByWtMar;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "AOPYear", nullable = false, length = 20)
    private String aopYear;

    @Column(name = "CreatedDate", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
}
