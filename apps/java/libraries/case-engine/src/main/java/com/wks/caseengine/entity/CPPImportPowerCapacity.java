package com.wks.caseengine.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CPPImportPowerCapacity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CPPImportPowerCapacity {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false, columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "ImportPowerSource_FK_Id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID importPowerSourceFkId;

    @Column(name = "FinancialYear", nullable = false, length = 10)
    private String financialYear;

    @Column(name = "Apr", precision = 18, scale = 2)
    private Double apr;

    @Column(name = "May", precision = 18, scale = 2)
    private Double may;

    @Column(name = "Jun", precision = 18, scale = 2)
    private Double jun;

    @Column(name = "Jul", precision = 18, scale = 2)
    private Double jul;

    @Column(name = "Aug", precision = 18, scale = 2)
    private Double aug;

    @Column(name = "Sep", precision = 18, scale = 2)
    private Double sep;

    @Column(name = "Oct", precision = 18, scale = 2)
    private Double oct;

    @Column(name = "Nov", precision = 18, scale = 2)
    private Double nov;

    @Column(name = "Dec", precision = 18, scale = 2)
    private Double dec;

    @Column(name = "Jan", precision = 18, scale = 2)
    private Double jan;

    @Column(name = "Feb", precision = 18, scale = 2)
    private Double feb;

    @Column(name = "Mar", precision = 18, scale = 2)
    private Double mar;

    @Column(name = "UOM", length = 10)
    private String uom;

    @Column(name = "Remarks", length = 8000)
    private String remarks;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;
}
