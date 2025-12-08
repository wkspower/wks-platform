package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "FinancialYearMonth")
@Data
public class FinancialYearMonth {

    @Id
    @Column(name = "Id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "Month", nullable = false)
    private Integer month;

    @Column(name = "Year", nullable = false)
    private Integer year;
}
