package com.wks.caseengine.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "AOPMaintenanceDesignBasis", schema = "dbo")
@Getter
@Setter
public class AOPMaintenanceDesignBasis {

	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
	
    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "Summary")
    private String summary;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @Column(name = "UpdatedDateTime")
    private Date updatedDateTime;

}
