package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "ReliabilityPerformance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReliabilityPerformance {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "row_no")
    private Integer rowNo;

    @Column(name = "parameter")
    private String parameter;

    @Column(name = "uom")
    private String uom;

    @Column(name = "best_achieved")
    private Double bestAchieved;

    @Column(name = "aop")
    private Double aop;

    @Column(name = "actual")
    private Double actual;

    @Column(name = "plann")
    private Double plann;

    @Column(name = "limit")
    private String limit;

    @Column(name = "rationale")
    private String rationale;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "aopYear")
    private String aopYear;

    @Column(name = "plantId")
    private UUID plantId;

    @Column(name = "reportType")
    private String reportType;
}
