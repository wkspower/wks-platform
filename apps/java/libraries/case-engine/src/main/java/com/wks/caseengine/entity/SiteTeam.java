package com.wks.caseengine.entity;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SiteTeam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteTeam {
	
	@Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
	
    @Column(name = "JobRole")
    private String jobRole;

    @Column(name = "Name")
    private String name;

    @Column(name = "Age")
    private Integer age;

    @Column(name = "TeamSize")
    private Integer teamSize;

    @Column(name = "Remark")
    private String remark;

    @Column(name = "SiteId")
    private UUID siteId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @Column(name = "UpdatedDate")
    private Date updatedDate;

}
