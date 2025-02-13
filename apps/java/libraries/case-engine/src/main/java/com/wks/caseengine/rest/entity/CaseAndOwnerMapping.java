//package com.wks.caseengine.rest.entity;
//
//import java.util.UUID;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//
//@Entity
//@Table(name = "case_and_owner_mapping")
//public class CaseAndOwnerMapping {
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "case_owner_mapping_pk_id", nullable = false)
//    private Long caseOwnerMappingPkId;
//	
//	@Column(name = "owner_pk_id")
//	private Long ownerPkId;
//	
//	@Column(name = "case_pk_id")
//	private Long casePkId;
//	
//
//	public Long getCaseOwnerMappingPkId() {
//		return caseOwnerMappingPkId;
//	}
//
//	public void setCaseOwnerMappingPkId(Long caseOwnerMappingPkId) {
//		this.caseOwnerMappingPkId = caseOwnerMappingPkId;
//	}
//
//	public Long getOwnerPkId() {
//		return ownerPkId;
//	}
//
//	public void setOwnerPkId(Long ownerPkId) {
//		this.ownerPkId = ownerPkId;
//	}
//
//	public Long getCasePkId() {
//		return casePkId;
//	}
//
//	public void setCasePkId(Long casePkId) {
//		this.casePkId = casePkId;
//	}
//	
//}
