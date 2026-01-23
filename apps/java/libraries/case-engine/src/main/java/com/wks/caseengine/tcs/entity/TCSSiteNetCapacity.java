package com.wks.caseengine.tcs.entity;

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
public class TCSSiteNetCapacity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "Capacity Type is required")
    @Size(max = 20)
    @Column(name = "CapacityType", length = 50, nullable = false)
    private String capacityType;

    @Column(name = "Apr", precision = 18, scale = 4)
    private Double apr;

    @Column(name = "May", precision = 18, scale = 4)
    private Double may;

    @Column(name = "June", precision = 18, scale = 4)
    private Double jun;

    @Column(name = "July", precision = 18, scale = 4)
    private Double jul;

    @Column(name = "Aug", precision = 18, scale = 4)
    private Double aug;

    @Column(name = "Sep", precision = 18, scale = 4)
    private Double sep;

    @Column(name = "Oct", precision = 18, scale = 4)
    private Double oct;

    @Column(name = "Nov", precision = 18, scale = 4)
    private Double nov;

    @Column(name = "Dec", precision = 18, scale = 4)
    private Double dec;

    @Column(name = "Jan", precision = 18, scale = 4)
    private Double jan;

    @Column(name = "Feb", precision = 18, scale = 4)
    private Double feb;

    @Column(name = "March", precision = 18, scale = 4)
    private Double mar;

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


