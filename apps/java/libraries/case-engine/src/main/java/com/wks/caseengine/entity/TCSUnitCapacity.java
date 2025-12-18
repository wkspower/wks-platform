package com.wks.caseengine.entity;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "TCSUnitCapacity")
@Data
public class TCSUnitCapacity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "Particulates is required")
    @Size(max = 300)
    @Column(name = "Particulates", length = 300)
    private String particulates;

    @NotNull(message = "UOM is required")
    @Size(max = 50)
    @Column(name = "UOM", length = 50, nullable = false)
    private String uom;

    @Column(name = "KBPSD", precision = 18, scale = 4)
    private Double kbpsd;

    @Size(max = 1000)
    @Column(name = "Remark", length = 1000)
    private String remark;

    @Size(max = 20)
    @Column(name = "AOPYear", length = 20)
    private String aopYear;

    @Column(name = "Plant_FK_ID")
    private UUID plantFkId;

    @Column(name = "InsertedDateTime")
	private Date insertedDateTime;

	@Column(name = "UpdatedDateTime")
	private Date updatedDateTime;
}
