package com.wks.caseengine.rest.db2.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Users")
public class Users {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "User_PK_ID")
    private String userPkId;

	
    @Column(name = "UserId", length = 50)
	private String userId;

    @Column(name = "FirstName", length = 50)
	private String firstName;

    @Column(name = "LastName", length = 50)
    private String lastName;

    @Column(name = "EmailId", length = 100)
    private String emailId;
	
    @Column(name = "RoleName", length = 100)
    private String roleName;

    @Column(name = "UserClusteredId", nullable = false)
    private Integer userClusteredId;

	public String getUserPkId() {
		return userPkId;
	}

	public void setUserPkId(String userPkId) {
		this.userPkId = userPkId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Integer getUserClusteredId() {
		return userClusteredId;
	}

	public void setUserClusteredId(Integer userClusteredId) {
		this.userClusteredId = userClusteredId;
	}
    
    
    
	
}
