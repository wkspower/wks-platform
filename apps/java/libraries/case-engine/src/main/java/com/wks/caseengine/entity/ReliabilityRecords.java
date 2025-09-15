package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ReliabilityRecords")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReliabilityRecords {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "reportType")
    private String reportType;

    @Lob
    @Column(name = "IncidentDescription", columnDefinition = "text")
    private String incidentDescription;

    @Lob
    @Column(name = "RootCauseAnalysis", columnDefinition = "text")
    private String rootCauseAnalysis;

    @Lob
    @Column(name = "Initiative", columnDefinition = "text")
    private String initiative;

    @Lob
    @Column(name = "Outcome", columnDefinition = "text")
    private String outcome;

    @Lob
    @Column(name = "Recommendation", columnDefinition = "text")
    private String recommendation;

    @Column(name = "TargetDate")
    private LocalDate targetDate;

    @Column(name = "Responsible")
    private String responsible;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "aopYear")
    private String aopYear;

    @Column(name = "plantId")
    private UUID plantId;

    @Column(name = "row_no")
    private Integer rowNo;
}
