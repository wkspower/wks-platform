package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "BusinessDemand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessDemand {
    
     @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "Remark", columnDefinition = "nvarchar(max)")
    private String remark;
    
    @Column(name = "NormParameters_FK_Id", nullable = false)
    private UUID normParameterId;
    
    @Column(name = "Jan")
    private Double jan;
    
    @Column(name = "Feb")
    private Double feb;
    
    @Column(name = "March")
    private Double march;
    
    @Column(name = "April")
    private Double april;
    
    @Column(name = "May")
    private Double may;
    
    @Column(name = "June")
    private Double june;
    
    @Column(name = "July")
    private Double july;
    
    @Column(name = "Aug")
    private Double aug;
    
    @Column(name = "Sep")
    private Double sep;
    
    @Column(name = "Oct")
    private Double oct;
    
    @Column(name = "Nov")
    private Double nov;
    
    @Column(name = "Dec")
    private Double dec;
    
    @Column(name = "Year", length = 9, nullable = false)
    private String year;
    
    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantId;
    
    @Column(name = "AvgTPH")
    private Double avgTph;
    
    @Column(name="Site_FK_Id")
    private UUID siteFKId;
    
    @Column(name="Vertical_FK_Id")
    private UUID verticalFKId;
    

}
