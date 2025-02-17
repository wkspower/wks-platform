package com.wks.caseengine.entity;


import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "AttributeTransactions")
public class AttributeTransaction {

    @Id
    @Column(name = "Id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "AttributeName", nullable = false, length = 250)
    private String attributeName;

    @Column(name = "AttributeValue", nullable = false, length = 250)
    private String attributeValue;

    @Column(name = "AttributeLable", length = 250)
    private String attributeLable;

    @Column(name = "AttributeValueLable", length = 250)
    private String attributeValueLable;

    @Column(name = "CreatedOn", nullable = false)
    private Date createdOn;

    @Column(name = "AttributeValueDate", nullable = false, length = 100)
    private String attributeValueDate;

    @Column(name = "AttributeValueVersion", nullable = false, length = 10)
    private String attributeValueVersion;

    @Column(name = "User", nullable = false, length = 255)
    private String user;

   }
