package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "NormAttributeTransactionLine")
public class NormAttributeTransactionLine {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "Grade_FK_ID")
    private UUID gradeFkId;

    @Column(name = "Line_FK_ID")
    private UUID lineFkId;

    @Column(name = "Plant_FK_ID")
    private UUID plantFkId;

    private String attributeValue;

    @Column(name = "USER_NAME")
    private String user;

    private String aopYear;
    private Date createdOn;
    private Date modifiedOn;
}
