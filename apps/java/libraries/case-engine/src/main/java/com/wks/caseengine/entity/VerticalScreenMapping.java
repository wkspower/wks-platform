package com.wks.caseengine.entity;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;


@Entity
@Table(name = "VerticalScreenMapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerticalScreenMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "VerticalFKId", nullable = false)
    private UUID verticalFKId;

    @Column(name = "ScreenDisplayName")
    private String screenDisplayName;

    @Column(name = "ScreenCode")
    private String screenCode;

    @Column(name = "Sequence")
    private Integer sequence;

    @Column(name = "GroupFKId")
    private UUID groupId;

    @Column(name = "Route")
    private String route;

    @Column(name = "MenuJSON")
    private String menuJson;

    @Column(name = "Title")
    private String title;

    @Column(name = "Type")
    private String type;

    @Column(name = "Icon")
    private String icon;

    @Column(name = "BreadCrumbs")
    private Boolean breadCrumbs;


}
