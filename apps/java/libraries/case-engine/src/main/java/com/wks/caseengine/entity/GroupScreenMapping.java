package com.wks.caseengine.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;


@Entity
@Table(name = "GroupScreenMapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupScreenMapping {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "GroupId")
    private Long groupId;

    @Column(name = "PlantFKId")
    private UUID plantFKId;

    @Column(name = "VerticalFKId")
    private UUID verticalFKId;

    @Column(name = "ScreenCode")
    private String screenCode;


}
