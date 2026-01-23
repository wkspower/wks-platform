package com.wks.caseengine.cpp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CPPNorms", schema = "dbo", catalog = "RIL.AOP")
@Getter
@Setter
public class CPPNorms {

    @Id
    @Column(name = "Id")
    private UUID id;

    @Column(name = "NormsHeader_FK_Id")
    private UUID normsHeaderFkId;

    @Column(name = "FinancialYear")
    private String financialYear;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "NormType_FK_Id")
    private Integer normTypeFkId;

    @Column(name = "Apr_Norms")
    private BigDecimal aprNorms;

    @Column(name = "May_Norms")
    private BigDecimal mayNorms;

    @Column(name = "Jun_Norms")
    private BigDecimal junNorms;

    @Column(name = "Jul_Norms")
    private BigDecimal julNorms;

    @Column(name = "Aug_Norms")
    private BigDecimal augNorms;

    @Column(name = "Sep_Norms")
    private BigDecimal sepNorms;

    @Column(name = "Oct_Norms")
    private BigDecimal octNorms;

    @Column(name = "Nov_Norms")
    private BigDecimal novNorms;

    @Column(name = "Dec_Norms")
    private BigDecimal decNorms;

    @Column(name = "Jan_Norms")
    private BigDecimal janNorms;

    @Column(name = "Feb_Norms")
    private BigDecimal febNorms;

    @Column(name = "Mar_Norms")
    private BigDecimal marNorms;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "CreatedBy")
    private String createdBy;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "ModifiedBy")
    private String modifiedBy;

    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;
}
