package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "NormsTransactions", schema = "dbo")
public class NormsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantFkId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "NormParameter_FK_Id")
    private UUID normParameterFkId;

    @Column(name = "AOPMonth")
    private Integer aopMonth;

    @Column(name = "AttributeValue")
    private Double attributeValue;

    @Column(name = "Remark")
    private String remark;

    @Column(name = "Version")
    private Integer version;

    @Column(name = "CreatedBy")
    private String createdBy;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @Column(name = "CreatedDateTime")
    private Date createdDateTime;

    @Column(name = "UpdatedDateTime")
    private Date updatedDateTime;
    
    @Column(name = "MCUNormsValue_FK_Id")
    private UUID mcuNormsValueFKId;

 }

