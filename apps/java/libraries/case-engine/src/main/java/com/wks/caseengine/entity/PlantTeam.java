package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "PlantTeam")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantTeam {

    @Id
    @Column(name = "Id")
    private UUID id;

    @Column(name = "SrNo")
    private Integer srNo;

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