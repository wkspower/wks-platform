package com.wks.caseengine.cpp.entity;

import lombok.Data;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "CPP_SRMapping", schema = "dbo", catalog = "RIL.AOP")
@Data
public class CPPSRMapping {

    @Id
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Receiver Utility", nullable = false)
    private String receiverUtility;

    @Column(name = "Receiver Utility ID", nullable = false)
    private String receiverUtilityId;

    @Column(name = "Receiver Cost Center")
    private String receiverCostCenter;

    @Column(name = "Receiver Cost Center ID")
    private String receiverCostCenterId;

    @Column(name = "Receiver Plant")
    private String receiverPlant;

    @Column(name = "Receiver Plant ID")
    private String receiverPlantId;

    @Column(name = "Sender Cost Center")
    private String senderCostCenter;

    @Column(name = "Sender Cost Center ID")
    private String senderCostCenterId;

    @Column(name = "Sender Plant")
    private String senderPlant;

    @Column(name = "Sender Plant ID")
    private String senderPlantId;

    @Column(name = "Utility")
    private String utility;

    @Column(name = "Utility ID")
    private String utilityId;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "AOPYear", nullable = false)
    private String aopYear;

    @Column(name = "Vertical_FK_Id")
    private UUID verticalFkId;

    @Column(name = "Site_FK_Id")
    private UUID siteFkId;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;
}
