package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity
@Table(name = "TurnAroundPlan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnAroundPlan {
   @Id
    @UuidGenerator
    @Column(name = "Id", nullable = false, unique = true)
    private UUID id;

    

     @Column(name = "RowNo", nullable = true)
    private Integer rowNumber;

    @Column(name = "Plant_FK_Id")
    private UUID plantFkId;

    
    @Column(name = "AOPYear", nullable = false, length = 255)
    private String aopYear;

    @Column(name = "activity", nullable = false, length = 255)
    private String activity;

    @Column(name = "toDate", nullable = true, length = 255)
    private String toDate;

    @Column(name = "Remark", nullable = true, length = 255)
    private String remark;

     @Column(name = "fromDate", nullable = true, length = 255)
    private String fromDate;

    
   
    @Column(name = "durationInHrs", nullable = true)
    private Double durationInHrs;

    @Column(name = "periodInMonths", nullable = true)
    private Double periodInMonths;



    
    
}
