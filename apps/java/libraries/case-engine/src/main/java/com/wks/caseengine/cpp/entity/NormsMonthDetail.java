package com.wks.caseengine.cpp.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "NormsMonthDetail", schema = "dbo", catalog = "RIL.AOP")
@Getter
@Setter
public class NormsMonthDetail {

    @Id
    @Column(name = "Id")
    private UUID id;

    @Column(name = "NormsHeader_FK_Id")
    private UUID normsHeaderFkId;

    @Column(name = "FinancialYearMonth_FK_Id")
    private UUID financialYearMonthFkId;

    @Column(name = "ScenarioType")
    private String scenarioType;

    @Column(name = "Norms")
    private BigDecimal norms;

    @Column(name = "Quantity")
    private BigDecimal quantity;

    @Column(name = "Amount")
    private BigDecimal amount;

    @Column(name = "Price")
    private BigDecimal price;

    @Column(name = "DisplayOrder")
    private Integer displayOrder;

    @Column(name = "GenerationUOM")
    private String generationUom;

    @Column(name = "QTY")
    private BigDecimal qty;
}


