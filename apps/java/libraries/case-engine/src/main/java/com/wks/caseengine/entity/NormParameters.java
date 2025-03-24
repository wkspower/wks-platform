package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Data
@Table(name = "NormParameters")
public class NormParameters {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @Column(name = "DisplayName", nullable = false, length = 255)
    private String displayName;

    @Column(name = "UOM", length = 100)
    private String uom;

    @Column(name = "Expression", length = 1000)
    private String expression;
    
    @Column(name = "ExecuteQuery", length = 1000)
    private String executeQuery;

    @Column(name = "DependantAttributeId", length = 250)
    private String dependantAttributeId;

    @Column(name = "Type", nullable = false, length = 255)
    private String type;

    @Column(name = "NormParameterType_FK_Id")
    private UUID normParameterTypeFkId;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;
    
    @Column(name = "NormType_FK_Id")
    private Integer normTypeFKId;
    
    @Column(name = "DisplayOrder")
    private Integer displayOrder;

}

