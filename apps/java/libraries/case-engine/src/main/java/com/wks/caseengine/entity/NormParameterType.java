package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;

@Entity
@Data
@Table(name = "NormParameterType")
public class NormParameterType {

    @Id
    @Column(name = "Id", nullable = false)
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
