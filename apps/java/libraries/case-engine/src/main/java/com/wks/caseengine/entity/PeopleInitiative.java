package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;



@Entity
@Table(name = "PeopleInitiative")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeopleInitiative {

	 @Id
	    @GeneratedValue(generator = "UUID")
	    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	    @Column(name = "Id", nullable = false, updatable = false)
	    private UUID id;

    @Column(name = "Initiative")
    private String initiative;

    @Column(name = "Outcome")
    private String outcome;

    @Column(name = "Recommendation")
    private String recommendation;

    @Column(name = "TargetDate")
    private Date targetDate;

    @Column(name = "Responsible")
    private String responsible;

    @Column(name = "Plant_Id")
    private UUID plantId;

    @Column(name = "AOPYear")
    private String aopYear;

    @Column(name = "Remark")
    private String remark;
}
