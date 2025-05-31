package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "AOP")
@Data
public class AOP {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "AOPCaseId", length = 255)
    private String aopCaseId;

    @Column(name = "AOPStatus", length = 255)
    private String aopStatus;

    @Column(name = "AOPRemarks", length = 500)
    private String aopRemarks;

    @Column(name = "AOPType", length = 255)
    private String aopType;

    @Column(name = "Jan")
    private Double jan;

    @Column(name = "Feb")
    private Double feb;

    @Column(name = "March")
    private Double march;

    @Column(name = "April")
    private Double april;

    @Column(name = "May")
    private Double may;

    @Column(name = "June")
    private Double june;

    @Column(name = "July")
    private Double july;

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

    @Column(name = "AOPYear", length = 100)
    private String aopYear;

    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantFkId;

    @Column(name = "AvgTPH")
    private Double avgTPH;
    
    @Column(name="Material_FK_Id")
    private UUID MaterialFKId;
    
    @Column(name = "Site_FK_Id", nullable = false)
    private UUID siteFkId;

    @Column(name = "Vertical_FK_Id", nullable = false)
    private UUID verticalFkId;
    
    @Column(name = "CreatedOn")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "ModifiedOn")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;

    @Column(name = "AOPVersion")
    private String aopVersion;

    @Column(name = "UpdatedBy")
    private String updatedBy;
}
