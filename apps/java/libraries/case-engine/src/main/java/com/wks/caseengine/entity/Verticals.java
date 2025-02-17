package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "Verticals")
public class Verticals {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @Column(name = "DisplayName", nullable = false, length = 255)
    private String displayName;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "DisplayOrder")
    private Integer displayOrder;

}
