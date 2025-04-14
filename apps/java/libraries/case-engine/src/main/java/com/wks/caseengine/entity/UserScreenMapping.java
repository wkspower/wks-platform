package com.wks.caseengine.entity;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;


@Entity
@Table(name = "UserScreenMapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScreenMapping {	
	@Id
    @Column(name = "Id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "UserId")
    private UUID userId;

    @Column(name = "PlantFKId")
    private UUID plantFKId;

    @Column(name = "VerticalFKId")
    private UUID verticalFKId;

    @Column(name = "ScreenCode")
    private String screenCode;
    
    @Column(name = "Permissions")
    private String permissions;
}
