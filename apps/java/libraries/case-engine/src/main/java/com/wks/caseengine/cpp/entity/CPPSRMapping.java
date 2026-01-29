package com.wks.caseengine.cpp.entity;

import lombok.Data;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "CPP_SRMapping", schema = "dbo", catalog = "RIL.AOP")
public class CppSrMappingEntity {

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

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReceiverUtility() {
        return receiverUtility;
    }

    public void setReceiverUtility(String receiverUtility) {
        this.receiverUtility = receiverUtility;
    }

    public String getReceiverUtilityId() {
        return receiverUtilityId;
    }

    public void setReceiverUtilityId(String receiverUtilityId) {
        this.receiverUtilityId = receiverUtilityId;
    }

    public String getReceiverCostCenter() {
        return receiverCostCenter;
    }

    public void setReceiverCostCenter(String receiverCostCenter) {
        this.receiverCostCenter = receiverCostCenter;
    }

    public String getReceiverCostCenterId() {
        return receiverCostCenterId;
    }

    public void setReceiverCostCenterId(String receiverCostCenterId) {
        this.receiverCostCenterId = receiverCostCenterId;
    }

    public String getReceiverPlant() {
        return receiverPlant;
    }

    public void setReceiverPlant(String receiverPlant) {
        this.receiverPlant = receiverPlant;
    }

    public String getReceiverPlantId() {
        return receiverPlantId;
    }

    public void setReceiverPlantId(String receiverPlantId) {
        this.receiverPlantId = receiverPlantId;
    }

    public String getSenderCostCenter() {
        return senderCostCenter;
    }

    public void setSenderCostCenter(String senderCostCenter) {
        this.senderCostCenter = senderCostCenter;
    }

    public String getSenderCostCenterId() {
        return senderCostCenterId;
    }

    public void setSenderCostCenterId(String senderCostCenterId) {
        this.senderCostCenterId = senderCostCenterId;
    }

    public String getSenderPlant() {
        return senderPlant;
    }

    public void setSenderPlant(String senderPlant) {
        this.senderPlant = senderPlant;
    }

    public String getSenderPlantId() {
        return senderPlantId;
    }

    public void setSenderPlantId(String senderPlantId) {
        this.senderPlantId = senderPlantId;
    }

    public String getUtility() {
        return utility;
    }

    public void setUtility(String utility) {
        this.utility = utility;
    }

    public String getUtilityId() {
        return utilityId;
    }

    public void setUtilityId(String utilityId) {
        this.utilityId = utilityId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAopYear() {
        return aopYear;
    }

    public void setAopYear(String aopYear) {
        this.aopYear = aopYear;
    }

    public UUID getVerticalFkId() {
        return verticalFkId;
    }

    public void setVerticalFkId(UUID verticalFkId) {
        this.verticalFkId = verticalFkId;
    }

    public UUID getSiteFkId() {
        return siteFkId;
    }

    public void setSiteFkId(UUID siteFkId) {
        this.siteFkId = siteFkId;
    }

    public UUID getPlantFkId() {
        return plantFkId;
    }

    public void setPlantFkId(UUID plantFkId) {
        this.plantFkId = plantFkId;
    }
}
