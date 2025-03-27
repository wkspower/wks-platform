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
    private Float jan;

    @Column(name = "Feb")
    private Float feb;

    @Column(name = "March")
    private Float march;

    @Column(name = "April")
    private Float april;

    @Column(name = "May")
    private Float may;

    @Column(name = "June")
    private Float june;

    @Column(name = "July")
    private Float july;

    @Column(name = "Aug")
    private Float aug;

    @Column(name = "Sep")
    private Float sep;

    @Column(name = "Oct")
    private Float oct;

    @Column(name = "Nov")
    private Float nov;

    @Column(name = "Dec")
    private Float dec;

    @Column(name = "AOPYear", length = 100, nullable = false)
    private String aopYear;

    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantFkId;
}
