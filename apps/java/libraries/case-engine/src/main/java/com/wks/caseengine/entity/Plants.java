package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity
@Table(name = "Plants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plants {
    @Id
    @UuidGenerator
    @Column(name = "Id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "Name", nullable = false, length = 255)
    private String name;

    @Column(name = "DisplayName", nullable = false, length = 255)
    private String displayName;

    @Column(name = "Site_FK_Id")
    private UUID siteFkId;
    
    @Column(name = "Vertical_FK_Id")
    private UUID verticalFKId;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "DisplayOrder")
    private Integer displayOrder;
}
