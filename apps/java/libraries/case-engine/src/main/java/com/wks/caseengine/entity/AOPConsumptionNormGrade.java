package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "AOPConsumptionNormGrade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AOPConsumptionNormGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "Site_FK_Id")
    private UUID siteFkId;

    @Column(name = "Vertical_FK_Id")
    private UUID verticalFkId;

    @Column(name = "AOPCaseId")
    private String aopCaseId;

    @Column(name = "AOPStatus")
    private String aopStatus;

    @Column(name = "AOPRemarks", length = 500)
    private String aopRemarks;

    @Column(name = "Material_FK_Id")
    private UUID materialFkId;

    @Column(name = "Grade_FK_Id")
    private UUID gradeFkId;

    @Column(name = "NormParameterType_FK_Id")
    private UUID normParameterTypeFkId;

    @Column(name = "Jan")
    private Double jan;

    @Column(name = "Feb")
    private Double feb;

    @Column(name = "March")
    private Double march;

    @Column(name = "April")
    private Double april;

    @Column(name = "May")
    private Double may;

    @Column(name = "June")
    private Double june;

    @Column(name = "July")
    private Double july;

    @Column(name = "Aug")
    private Double aug;

    @Column(name = "Sep")
    private Double sep;

    @Column(name = "Oct")
    private Double oct;

    @Column(name = "Nov")
    private Double nov;

    @Column(name = "Dec")
    private Double dec;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;
}