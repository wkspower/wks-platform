package com.wks.caseengine.entity;



import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "NormParameterMonthlyTransaction")
public class NormParameterMonthlyTransaction {
	
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
	
	@Column(name = "monthValue")
	private String monthValue;
	
	@Column(name="month")
	private String month;
	
	@Column(name="year")
	private String year;
	
	@Column(name="Unit")
	private String Unit;
	
	@Column(name="Remarks")
	private String Remarks;
	
	@Column(name="CreatedOn")
	private Date CreatedOn;
	
	@Column(name="User")
	private String User;
	
	@Column(name="Version")
	private String Version;
	
	@Column(name="NormParameters_FK_Id")
	private UUID NormParameters_FK_Id;
	
}
