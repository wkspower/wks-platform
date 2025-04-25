package com.wks.caseengine.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "AnnualAOPCost")
@Data
public class AnnualAOPCost {

    @Id
    @Column(name = "Id", nullable = false)
    private UUID id;

    @Column(name = "Particulates", length = 300)
    private String particulates;

    @Column(name = "UOM", length = 20)
    private String uom;

    @Column(name = "AOPYear", length = 20)
    private String aopYear;

    @Column(name = "AOPType", length = 20)
    private String aopType;

    @Column(name = "Cost", precision = 18, scale = 8)
    private BigDecimal cost;

    @Column(name = "Remark", length = 500)
    private String remark;

    @Column(name = "Plant_FK_ID")
    private UUID plantFkId;

    @Column(name = "InsertedDateTime")
    private LocalDateTime insertedDateTime;

    @Column(name = "UpdatedDateTime")
    private LocalDateTime updatedDateTime;

    // Getters and Setters
}
