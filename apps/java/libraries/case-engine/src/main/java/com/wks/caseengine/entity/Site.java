package com.wks.caseengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="Site")
public class Site {
	
	@Id
    @Column(name = "site_id", nullable = false, unique = true)
    private String siteId;
	
    @Column(name = "site_name")
    private String siteName;

    @Column(name = "vertical_id")
    private String verticalId;

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getVerticalId() {
		return verticalId;
	}

	public void setVerticalId(String verticalId) {
		this.verticalId = verticalId;
	}
    
    


}
