package com.wks.caseengine.entity;



import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "PIOImpact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PIOImpact {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "Id", nullable = false, updatable = false)
	private UUID id;

    @Column(name = "Description")
    private String description;

    @Column(name = "StartMonth")
    private Integer startMonth;

    @Column(name = "EndMonth")
    private Integer endMonth;

    @Column(name = "Value")
    private Double value;

    @Column(name = "Remarks")
    private String remarks;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "Plant_FK_Id")
    private UUID plantId;

    @Column(name = "Site_FK_Id")
    private UUID siteId;

    @Column(name = "Vertical_FK_Id")
    private UUID verticalId;

}

