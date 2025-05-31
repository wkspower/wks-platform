package com.wks.caseengine.entity;

import java.util.UUID;

import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "ScreenMapping")
@Data
public class ScreenMapping {
	
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "calculationScreen")
    private String calculationScreen;

    @Column(name = "dependentScreen")
    private String dependentScreen;


}
