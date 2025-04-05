package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "NormAttributeTransactionReceipe")
public class NormAttributeTransactionReceipe {

    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Grade_FK_ID", nullable = false)
    private UUID gradeFkId;

    @Column(name = "Reciepe_FK_ID", nullable = false)
    private UUID reciepeFkId;

    @Column(name = "Plant_FK_ID", nullable = false)
    private UUID plantFkId;

    @Column(name = "AttributeValue")
    private Integer attributeValue;

    @Column(name = "[User]", nullable = false, length = 50)
    private String user;

    @Column(name = "AOPYear", length = 20)
    private String aopYear;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedOn")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ModifiedOn")
    private Date modifiedOn;
}
