package com.wks.caseengine.rest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="Equipments")
public class Equipments {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "equipment_PK_ID")
    private String equipmentPKId;

    @Column(name = "Name", length = 300, nullable = false)
    private String name;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "Is_Class", length = 1, nullable = false)
    private String isClass; // Using String for bit field

    @Column(name = "Criticality", nullable = false)
    private String criticality;

    @Column(name = "Mode", nullable = false)
    private String mode;

    @Column(name = "Performance", nullable = false)
    private String performance;

    @Column(name = "Status", nullable = false)
    private String status;

    @Column(name = "Priority", nullable = false)
    private String priority;

    @Column(name = "Owner", length = 100)
    private String owner;

    @Column(name = "SecurityCode", columnDefinition = "nvarchar(MAX)")
    private String securityCode;

    @Column(name = "ConfirmStatus", length = 1)
    private String confirmStatus; // Using String for bit field

    @Column(name = "InheritStatus")
    private String inheritStatus;

    @Column(name = "Transponder", length = 200)
    private String transponder;

    @Column(name = "Order")
    private String order;

    @Column(name = "InheritModeCategory", length = 1)
    private String inheritModeCategory; // Using String for bit field

    @Column(name = "CreatedDate")
    private String createdDate; // Assuming datetime as a formatted String

    @Column(name = "ModifiedDate")
    private String modifiedDate; // Assuming datetime as a formatted String

    @Column(name = "IsDeleted", length = 1)
    private String isDeleted; // Using String for bit field

    @Column(name = "DeletedTime")
    private String deletedTime; // Assuming datetime as a formatted String

    @Column(name = "EquipmentType_PK_ID", nullable = false)
    private String equipmentTypePKId;

    @Column(name = "Location_PK_ID")
    private String locationPKId;

    @Column(name = "ModeCategory_PK_ID")
    private String modeCategoryPKId;

    @Column(name = "ModeType_PK_ID")
    private String modeTypePKId;

    @Column(name = "Display_Name", length = 500)
    private String displayName;

    @Column(name = "EquipmentClusteredId", nullable = false)
    private String equipmentClusteredId;

    @Column(name = "FilterTag", columnDefinition = "nvarchar(MAX)")
    private String filterTag;

    @Column(name = "Timezone", length = 500)
    private String timezone;

    @Column(name = "Latitude", length = 50)
    private String latitude;

    @Column(name = "Longitude", length = 50)
    private String longitude;

    @Column(name = "Location", length = 500)
    private String location;

    @Column(name = "AssetId", length = 100)
    private String assetId;

	public String getEquipmentPKId() {
		return equipmentPKId;
	}

	public void setEquipmentPKId(String equipmentPKId) {
		this.equipmentPKId = equipmentPKId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIsClass() {
		return isClass;
	}

	public void setIsClass(String isClass) {
		this.isClass = isClass;
	}

	public String getCriticality() {
		return criticality;
	}

	public void setCriticality(String criticality) {
		this.criticality = criticality;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getPerformance() {
		return performance;
	}

	public void setPerformance(String performance) {
		this.performance = performance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getSecurityCode() {
		return securityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}

	public String getConfirmStatus() {
		return confirmStatus;
	}

	public void setConfirmStatus(String confirmStatus) {
		this.confirmStatus = confirmStatus;
	}

	public String getInheritStatus() {
		return inheritStatus;
	}

	public void setInheritStatus(String inheritStatus) {
		this.inheritStatus = inheritStatus;
	}

	public String getTransponder() {
		return transponder;
	}

	public void setTransponder(String transponder) {
		this.transponder = transponder;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getInheritModeCategory() {
		return inheritModeCategory;
	}

	public void setInheritModeCategory(String inheritModeCategory) {
		this.inheritModeCategory = inheritModeCategory;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getDeletedTime() {
		return deletedTime;
	}

	public void setDeletedTime(String deletedTime) {
		this.deletedTime = deletedTime;
	}

	public String getEquipmentTypePKId() {
		return equipmentTypePKId;
	}

	public void setEquipmentTypePKId(String equipmentTypePKId) {
		this.equipmentTypePKId = equipmentTypePKId;
	}

	public String getLocationPKId() {
		return locationPKId;
	}

	public void setLocationPKId(String locationPKId) {
		this.locationPKId = locationPKId;
	}

	public String getModeCategoryPKId() {
		return modeCategoryPKId;
	}

	public void setModeCategoryPKId(String modeCategoryPKId) {
		this.modeCategoryPKId = modeCategoryPKId;
	}

	public String getModeTypePKId() {
		return modeTypePKId;
	}

	public void setModeTypePKId(String modeTypePKId) {
		this.modeTypePKId = modeTypePKId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEquipmentClusteredId() {
		return equipmentClusteredId;
	}

	public void setEquipmentClusteredId(String equipmentClusteredId) {
		this.equipmentClusteredId = equipmentClusteredId;
	}

	public String getFilterTag() {
		return filterTag;
	}

	public void setFilterTag(String filterTag) {
		this.filterTag = filterTag;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
    
}
