package com.wks.caseengine.entity;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MCUValue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AOPMCCalculatedData {
    
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "Site_FK_ID")
    private UUID siteFKId;
    
    @Column(name = "Plant_FK_ID")
    private UUID plantFKId;
    @Column(name = "Vertical_FK_ID")
    private UUID verticalFKId;
    
    @Column(name = "Material_FK_ID")
    private UUID materialFKId;
    
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
    

    @Column(name="FinancialYear")
    private String financialYear;
    
    
    @Column(name="Remark")
    private String remark;
    @Column(name="CreatedOn")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @Column(name="ModifiedOn")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;
    @Column(name="MCUVersion")
    private String mcuVersion;
    @Column(name="UpdatedBy")
    private String updatedBy;
}
