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
    private Float april;
    
    @Column(name = "May")
    private Float may;
    
    @Column(name = "June")
    private Float june;
    
    @Column(name = "July")
    private Float july;
    
    @Column(name = "August")
    private Float august;
    
    @Column(name = "September")
    private Float september;
    
    @Column(name = "October")
    private Float october;
    
    @Column(name = "November")
    private Float november;
    
    @Column(name = "December")
    private Float december;
    
    @Column(name = "January")
    private Float january;
    
    @Column(name = "February")
    private Float february;
    
    @Column(name = "March")
    private Float march;
    
    @Column(name="Plant_FK_Id")
    private UUID plantFKId;

    @Column(name="Year")
    private String year;
    
    @Column(name="NormParameters_FK_Id")
    private UUID normParametersFKId;
    
    @Column(name="Remark")
    private String remark;
}
