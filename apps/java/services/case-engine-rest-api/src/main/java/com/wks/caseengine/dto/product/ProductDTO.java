package com.wks.caseengine.dto.product;

public class ProductDTO {
    private String id;
    private String name;
    private String displayName;

    // Constructors
    public ProductDTO() {}

    public ProductDTO(String id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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
