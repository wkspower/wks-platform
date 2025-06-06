package com.wks.caseengine.entity;


import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GroupMaster")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "GroupName", length = 255, nullable = false)
    private String groupName;

    @Column(name = "GroupCode", length = 255, nullable = false)
    private String groupCode;

    @Column(name = "Sequence")
    private Integer sequence;
    
    @Column(name = "Type")
    private String type;

    @Column(name = "Icon")
    private String icon;
    
    @Column(name = "Parent_Id")
    private UUID parentId;
}
