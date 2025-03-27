package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "MaintenanceCalculatedData")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceCalculatedData {
    
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "RunningHoursInMonth")
    private Integer runningHoursInMonth;
    
    @Column(name = "ShoutdownHrs")
    private Float shoutdownHrs;
    
    @Column(name = "NonShoutdownHrs")
    private Integer nonShoutdownHrs;
    @Column(name = "EOE_AvgSlowdownLoadPVT")
    private Float eoeAvgSlowdownLoadPVT;
    
    @Column(name = "EO_AvgSlowdownLoadPVT")
    private Float eoAvgSlowdownLoadPVT;
    
    @Column(name = "EOE_SlowdownLoadReduction")
    private Float eoeSlowdownLoadReduction;
    @Column(name = "EO_SlowdownLoadReduction")
    private Float eoSlowdownLoadReduction;
    
    @Column(name = "EOE_EffectiveOperatingHrs")
    private Integer eoeEffectiveOperatingHrs;
    @Column(name = "EO_EffectiveOperatingHrs")
    private Integer eoEffectiveOperatingHrs;
    
    @Column(name = "MonthNo")
    private Integer monthNo;
    
    @Column(name = "AOPYear")
    private String aopYear;
    
    @Column(name = "Plant_FK_Id")
    private UUID plantFKId;
}
