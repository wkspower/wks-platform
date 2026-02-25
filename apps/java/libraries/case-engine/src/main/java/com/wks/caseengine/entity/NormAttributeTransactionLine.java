package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@Entity
@Table(name = "NormAttributeTransactionLine")
public class NormAttributeTransactionLine {

	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Grade_FK_ID")
    private UUID gradeFkId;

    @Column(name = "Line_FK_ID")
    private UUID lineFkId;

    @Column(name = "Plant_FK_ID")
    private UUID plantFkId;

    @Column(name = "AttributeValue")
    private Double attributeValue;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "AOPYear")
    private String aopYear;

    
    @Column(name = "CreatedOn")
    private Date createdOn;

    
    @Column(name = "ModifiedOn")
    private Date modifiedOn;}
