package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "MCUNormsValue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MCUNormsValue {

	 @Id
	    @GeneratedValue(generator = "UUID")
	    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	    @Column(name = "Id", nullable = false, updatable = false)
	    private UUID id;

    @Column(name = "Site_FK_Id")
    private UUID siteFkId;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;

    @Column(name = "Vertical_FK_Id")
    private UUID verticalFkId;

    @Column(name = "Material_FK_Id")
    private UUID materialFkId;

    @Column(name = "NormParameterType_FK_Id")
    private UUID normParameterTypeFkId;

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
    
    @Column(name = "FinancialYear")
    private String financialYear;
    
    @Column(name = "Remarks")
    private String remarks;
    
    @Column(name = "CreatedOn")
    private Date createdOn;
    
    @Column(name = "ModifiedOn")
    private Date modifiedOn;
    
    @Column(name = "MCUVersion")
    private String mcuVersion;
    
    @Column(name = "UpdatedBy")
    private String updatedBy;
}
