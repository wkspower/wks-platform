package com.wks.caseengine.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "NormAttributeTransactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormAttributeTransactions {

   @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

//    @Column(name = "AttributeName", length = 250)
//    private String attributeName;

    @Column(name = "AttributeValue", length = 250)
    private String attributeValue;

//    @Column(name = "AttributeLable", length = 250)
//    private String attributeLable;
//
//    @Column(name = "AttributeValueLable", length = 250)
//    private String attributeValueLable;

    @Column(name = "AOPMonth")
    private Integer aopMonth;

    @Column(name = "AuditYear")
    private String auditYear;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "CreatedOn")
    private Date createdOn;

    @Column(name = "ModifiedOn")
    private Date modifiedOn;

    @Column(name = "AttributeValueVersion", length = 10)
    private String attributeValueVersion;

    // If you want to avoid using the reserved keyword 'User' directly in Java,
    // you can rename the field and map it to the column "User".

    @Column(name = "User", length = 255)  // Use backticks to escape reserved keyword
    private String userName;

    @Column(name = "NormParameter_FK_Id")
    private UUID normParameterFKId;

//    @Column(name = "CatalystAttribute_FK_Id")
//    private UUID catalystAttributeFKId;
    
//    @Column(name="Month")
//    private Integer month;
    
    @Column(name = "Plant_FK_Id")
    private UUID plantFKId;

}
