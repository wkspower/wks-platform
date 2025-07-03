package com.wks.caseengine.entity;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;


@Entity
@Table(name = "DecokeRunLength")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecokeRunLength {

	@Id
	@GeneratedValue
	@Column(name = "Id", nullable = false, updatable = false)
	private UUID id;

    @Column(name = "Date")
    private LocalDate date;

    @Column(name = "H10 Actual")
    private Double h10Actual;

    @Column(name = "H10 Proposed")
    private Double h10Proposed;

    @Column(name = "H11 Actual")
    private Double h11Actual;

    @Column(name = "H11 Proposed")
    private Double h11Proposed;

    @Column(name = "H12 Actual")
    private Double h12Actual;

    @Column(name = "H12 Proposed")
    private Double h12Proposed;

    @Column(name = "H13 Actual")
    private Double h13Actual;

    @Column(name = "H13 Proposed")
    private Double h13Proposed;

    @Column(name = "H14 Actual")
    private Double h14Actual;

    @Column(name = "H14 Proposed")
    private Double h14Proposed;

    @Column(name = "Demo", length = 100)
    private String demo;

    @Column(name = "AOPYear", length = 20)
    private String aopYear;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;

    @Column(name = "Remarks", length = 800)
    private String remarks;
}
