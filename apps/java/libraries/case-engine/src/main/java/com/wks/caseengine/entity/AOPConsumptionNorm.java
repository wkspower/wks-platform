package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "AOPConsumptionNorm", schema = "dbo")
public class AOPConsumptionNorm {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "Site_FK_Id", nullable = false)
    private UUID siteFkId;

    @Column(name = "Vertical_FK_Id", nullable = false)
    private UUID verticalFkId;

    @Column(name = "AOPCaseId", length = 255, nullable = false)
    private String aopCaseId;

    @Column(name = "AOPStatus", length = 255, nullable = false)
    private String aopStatus;

    @Column(name = "AOPRemarks", length = 500)
    private String aopRemarks;

    @Column(name = "Material_FK_Id", nullable = false)
    private UUID materialFkId;

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

    @Column(name = "AOPYear", length = 100, nullable = false)
    private String aopYear;

    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantFkId;
}
