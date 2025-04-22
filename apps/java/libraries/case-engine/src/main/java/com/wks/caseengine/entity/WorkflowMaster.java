package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "WorkflowMaster")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowMaster {
    
     @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "WorkflowId",  nullable = false)
    private String workflowId;

    @Column(name = "case_Def_Id",  nullable = false)
    private String caseDefId;

    @Column(name="Vertical_FK_Id")
    private UUID verticalFKId;
    
   
}
