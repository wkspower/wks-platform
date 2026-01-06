package com.wks.caseengine.entity;

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
@Table(name = "CalculatedProcessDemand", schema = "dbo")
public class CalculatedProcessDemand {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "financial_year", length = 10, nullable = false)
    private String financialYear;

    @Column(name = "process_plant", length = 100, nullable = false)
    private String processPlant;

    @Column(name = "process_plant_id", length = 20, nullable = false)
    private String processPlantId;

    @Column(name = "cpp_utility", length = 100, nullable = false)
    private String cppUtility;

    @Column(name = "cpp_utility_id", length = 50, nullable = false)
    private String cppUtilityId;

    @Column(name = "cpp_plant", length = 100)
    private String cppPlant;

    @Column(name = "cpp_plant_id", length = 20)
    private String cppPlantId;

    @Column(name = "uom", length = 20, nullable = false)
    private String uom;

    @Column(name = "apr")
    private Double apr;

    @Column(name = "may")
    private Double may;

    @Column(name = "jun")
    private Double jun;

    @Column(name = "jul")
    private Double jul;

    @Column(name = "aug")
    private Double aug;

    @Column(name = "sep")
    private Double sep;

    @Column(name = "oct")
    private Double oct;

    @Column(name = "nov")
    private Double nov;

    @Column(name = "dec")
    private Double dec;

    @Column(name = "jan")
    private Double jan;

    @Column(name = "feb")
    private Double feb;

    @Column(name = "mar")
    private Double mar;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
