package com.wks.caseengine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "DecokePlanning")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecokePlanning {

	@Id
	@GeneratedValue
	@Column(name = "Id", nullable = false, updatable = false)
	private UUID id;
	
    @Column(name = "MonthName")
    private String monthName;

    @Column(name = "IBR")
    private Integer ibr;

    @Column(name = "MNT")
    private Integer mnt;

    @Column(name = "ShutdownName")
    private Integer shutdown;

    @Column(name = "SAD")
    private Integer sad;

    @Column(name = "BUD")
    private Integer bud;

    @Column(name = "DemoHSS")
    private Integer demoHSS;

    @Column(name = "DemoBBU")
    private Integer demoBBU;

    @Column(name = "DemoSAD")
    private Integer demoSAD;

    @Column(name = "4FD")
    private Double fourFD;

    @Column(name = "4F")
    private Double fourF;

    @Column(name = "5F")
    private Double fiveF;

    @Column(name = "Total")
    private Double total;

    @Column(name = "4FHours")
    private Double fourFHours;

    @Column(name = "AOPYear", length = 10)
    private String aopYear;

    @Column(name = "PlantId")
    private UUID plantId;

    @Column(name = "Remarks", length = 800)
    private String remarks;
}
