package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "WorkflowStepsMaster")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStepsMaster {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
    
       
    @Column(name = "Name", length = 9, nullable = false)
    private String name;

    @Column(name = "DisplayName",  nullable = false)
    private String displayName;

    @Column(name = "sequence",  nullable = false)
    private Integer sequence;
    
    @Column(name = "WorkflowMaster_FK_Id", nullable = false)
    private UUID workflowMasterFKId;
    
    @Column(name="isRemarksDisabled")
    private Boolean isRemarksDisabled;
}
