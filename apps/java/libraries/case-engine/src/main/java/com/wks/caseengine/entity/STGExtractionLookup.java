package com.wks.caseengine.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "STGExtractionLookup", schema = "dbo")
public class STGExtractionLookup {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "LoadMW", precision = 10, scale = 2, nullable = false)
    private BigDecimal loadMW;

    @Column(name = "SVHInletTPH", precision = 10, scale = 2, nullable = false)
    private BigDecimal svhInletTPH;

    @Column(name = "SMBleedFlowTPH", precision = 10, scale = 2, nullable = false)
    private BigDecimal smBleedFlowTPH;

    @Column(name = "SLExtFlowTPH", precision = 10, scale = 2, nullable = false)
    private BigDecimal slExtFlowTPH;

    @Column(name = "CondensingLoadM3Hr", precision = 10, scale = 2, nullable = false)
    private BigDecimal condensingLoadM3Hr;

    @Column(name = "HeatRateKcalKWH", precision = 10, scale = 2, nullable = false)
    private BigDecimal heatRateKcalKWH;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "Remarks")
    private String remarks;
}
