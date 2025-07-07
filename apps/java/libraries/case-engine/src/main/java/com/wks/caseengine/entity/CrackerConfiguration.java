package com.wks.caseengine.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "CrackerConfiguration")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrackerConfiguration {

	@Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "Name", length = 100)
    private String name;

    @Column(name = "DisplayName", length = 200)
    private String displayName;

    @Column(name = "IBR_SD")
    @Temporal(TemporalType.DATE)
    private Date ibrStartDate;

    @Column(name = "IBR_ED")
    @Temporal(TemporalType.DATE)
    private Date ibrEndDate;

    @Column(name = "TA_SD")
    @Temporal(TemporalType.DATE)
    private Date taStartDate;

    @Column(name = "TA_ED")
    @Temporal(TemporalType.DATE)
    private Date taEndDate;

    @Column(name = "ShutDown_SD")
    @Temporal(TemporalType.DATE)
    private Date shutDownStartDate;

    @Column(name = "ShutDown_ED")
    @Temporal(TemporalType.DATE)
    private Date shutDownEndDate;

    @Column(name = "Post_CR_Days")
    private Integer postCrDays;

    @Column(name = "Pre_CR_Days")
    private Integer preCrDays;

    @Column(name = "IsCR")
    private Boolean isCr;

    @Column(name = "Plant_FK_Id", columnDefinition = "uniqueidentifier")
    private UUID plantFkId;

    @Column(name = "AOPYear", length = 10)
    private String aopYear;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "DisplaySeq")
    private Integer displaySeq;
}
