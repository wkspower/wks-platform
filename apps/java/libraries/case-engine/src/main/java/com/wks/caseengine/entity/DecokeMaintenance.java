package com.wks.caseengine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "DecokeMaintenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecokeMaintenance {

	@Id
	@GeneratedValue
	@Column(name = "Id", nullable = false, updatable = false)
	private UUID id;
	
	@Column(name = "MonthName", length = 15)
    private String monthName;

    @Column(name = "IBR")
    private Double ibr;

    @Column(name = "MNT")
    private Double mnt;

    @Column(name = "Shoutdown")
    private Double shoutdown;

    @Column(name = "SAD")
    private Double sad;

    @Column(name = "BBU")
    private Double bbu;

    @Column(name = "DemoHSS")
    private Double demoHss;

    @Column(name = "DemoBBU")
    private Double demoBbu;

    @Column(name = "DemoSAD")
    private Double demoSad;

    @Column(name = "4FD")
    private Double fourFd;

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

    @Column(name = "PlantId", nullable = false)
    private UUID plantId;

    @Column(name = "Remarks", length = 600)
    private String remarks;

    @Column(name = "Slowdown")
    private Double slowdown;

    @Column(name = "BBD")
    private Double bbd;

    @Column(name = "DemoSD")
    private Double demoSd;
}
