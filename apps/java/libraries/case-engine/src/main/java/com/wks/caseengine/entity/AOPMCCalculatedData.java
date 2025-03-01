package com.wks.caseengine.entity;

import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AOPMCCalculatedData")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AOPMCCalculatedData {
    
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "Site")
    private String site;
    
    @Column(name = "Plant")
    private String plant;
    
    @Column(name = "Material")
    private String material;
    
    @Column(name = "April")
    private Double april;
    
    @Column(name = "May")
    private Double may;
    
    @Column(name = "June")
    private Double june;
    
    @Column(name = "July")
    private Double july;
    
    @Column(name = "August")
    private Double august;
    
    @Column(name = "September")
    private Double september;
    
    @Column(name = "October")
    private Double october;
    
    @Column(name = "November")
    private Double november;
    
    @Column(name = "December")
    private Double december;
    
    @Column(name = "January")
    private Double january;
    
    @Column(name = "February")
    private Double february;
    
    @Column(name = "March")
    private Double march;
    
    @Column(name="Plant_FK_Id")
    private UUID plantFKId;
}
