package com.wks.caseengine.dto.product;

public class ProductDTO {
    private Long id;
    private String name;
    private String displayName;

    // Constructors
    public ProductDTO() {}

    public ProductDTO(Long id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
