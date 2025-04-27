package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "WorkflowInstances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workflow {
    
     @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
    
       
    @Column(name = "Year", length = 9, nullable = false)
    private String year;

    @Column(name = "case_Def_Id",  nullable = false)
    private String caseDefId;

    @Column(name = "case_Id",  nullable = false)
    private String caseId;

    @Column(name = "ProcessInstanceId",  nullable = true)
    private String processInstanceId;
    
    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantFKId;
    
   
    @Column(name="Site_FK_Id")
    private UUID siteFKId;
    
    @Column(name="Vertical_FK_Id")
    private UUID verticalFKId;
    
    @Column(name="isDeleted")
    private Boolean isDeleted;
}
