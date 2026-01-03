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
@Table(name = "HRSGHeatRateLookup", schema = "dbo")
public class HRSGHeatRateLookup {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "EquipmentName", length = 50, nullable = false)
    private String equipmentName;

    @Column(name = "CPPUtility", length = 20, nullable = false)
    private String cppUtility;

    @Column(name = "HRSGLoad", precision = 18, scale = 2, nullable = false)
    private BigDecimal hrsgLoad;

    @Column(name = "HeatRate", precision = 18, scale = 2, nullable = false)
    private BigDecimal heatRate;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "Remarks")
    private String remarks;
}
