package com.wks.caseengine.entity;


import lombok.Data;


import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Data
@Table(name = "AOPStatus")
public class AOPStatus {

    @Id
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Status", nullable = false, length = 255)
    private String status;

    @Column(name = "Description", nullable = true, length = 500)
    private String description;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
