package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

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

    @Column(name = "NormItem", length = 255)
    private String normItem;

    @Column(name = "AOPType", length = 255)
    private String aopType;

    @Column(name = "Jan")
    private Float jan;

    @Column(name = "Feb")
    private Float feb;

    @Column(name = "March")
    private Float march;

    @Column(name = "April")
    private Float april;

    @Column(name = "May")
    private Float may;

    @Column(name = "June")
    private Float june;

    @Column(name = "July")
    private Float july;

    @Column(name = "Aug")
    private Float aug;

    @Column(name = "Sep")
    private Float sep;

    @Column(name = "Oct")
    private Float oct;

    @Column(name = "Nov")
    private Float nov;

    @Column(name = "Dec")
    private Float dec;

    @Column(name = "AOPYear", length = 100)
    private String aopYear;

    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantFkId;

    @Column(name = "AvgTPH")
    private Float avgTPH;
    
    @Column(name="Material_FK_Id")
    private UUID MaterialFKId;
}
