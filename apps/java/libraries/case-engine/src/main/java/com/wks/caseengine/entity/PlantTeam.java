package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "PlantTeam")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantTeam {

	 	@Id
	    @GeneratedValue(generator = "UUID")
	    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	    @Column(name = "Id", nullable = false, updatable = false)
	    private UUID id;

    @Column(name = "Functions")
    private String functions;

    @Column(name = "JobRole")
    private String jobRole;

    @Column(name = "Name")
    private String name;

    @Column(name = "Age")
    private Integer age;

    @Column(name = "TeamSize")
    private Integer teamSize;

    @Column(name = "Plant_Id")
    private UUID plantId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "Remark")
    private String remark;
}