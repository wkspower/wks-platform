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
    
    @Column(name = "AvgSlowdownLoadPVT")
    private Float avgSlowdownLoadPVT;
    
    @Column(name = "SlowdownLoadReduction")
    private Float slowdownLoadReduction;
    
    @Column(name = "EffectiveOperatingHrs")
    private Integer effectiveOperatingHrs;
    
    @Column(name = "MonthNo")
    private Integer monthNo;
    
    @Column(name = "AOPYear")
    private String aopYear;
    
    @Column(name = "Plant_FK_Id")
    private UUID plantFKId;
}
