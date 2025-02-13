package com.wks.caseengine.rest.db2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="fault_category")
public class FaultCategory {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private Boolean recommendationFlag;

    // Constructors
    public FaultCategory() {
    }

    public FaultCategory(String name) {
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getRecommendationFlag() {
		return recommendationFlag;
	}

	public void setRecommendationFlag(Boolean recommendationFlag) {
		this.recommendationFlag = recommendationFlag;
	}

	// toString method for easy debugging
    @Override
    public String toString() {
        return "FaultCategory [id=" + id + ", name=" + name + "]";
    }
}
