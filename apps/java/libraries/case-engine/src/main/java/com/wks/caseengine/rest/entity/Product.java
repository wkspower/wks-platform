package com.wks.caseengine.rest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Product", schema = "dbo")
public class Product {
	
	@Id
	@Column(name = "product_id", nullable = false, unique = true)
    private String productId;
	
	@Column(name="product_name")
	private String productName;
	
	@Column(name="vertical_id")
	private String verticalId;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getVerticalId() {
		return verticalId;
	}

	public void setVerticalId(String verticalId) {
		this.verticalId = verticalId;
	}
	
	
}
