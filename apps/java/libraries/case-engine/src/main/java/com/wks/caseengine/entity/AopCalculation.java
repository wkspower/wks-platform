package com.wks.caseengine.entity;

import java.util.UUID;

import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "AopCalculation")
@Data
public class AopCalculation {
	
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;


    @Column(name = "plantId")
    private UUID plantId;

    @Column(name = "aopYear")
    private String aopYear;

    @Column(name = "isChanged")
    private Boolean isChanged;

    @Column(name = "calculationScreen")
    private String calculationScreen;

    @Column(name = "updatedScreen")
    private String updatedScreen;


}
