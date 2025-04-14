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
    private Float jan;
    
    @Column(name = "Feb")
    private Float feb;
    
    @Column(name = "March")
    private Float march;
    
    @Column(name = "April")
    private Float april;
    
    @Column(name = "May")
    private Float may;
    
    @Column(name = "June")
    private Float june;
    
    @Column(name = "July")
    private Float july;
    
    @Column(name = "Aug")
    private Float aug;
    
    @Column(name = "Sep")
    private Float sep;
    
    @Column(name = "Oct")
    private Float oct;
    
    @Column(name = "Nov")
    private Float nov;
    
    @Column(name = "Dec")
    private Float dec;
    
    @Column(name = "Year", length = 9, nullable = false)
    private String year;
    
    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantId;
    
    @Column(name = "AvgTPH")
    private Float avgTph;
    
    @Column(name="Site_FK_Id")
    private UUID siteFKId;
    
    @Column(name="Vertical_FK_Id")
    private UUID verticalFKId;
    
    @Column(name="isEditable")
    private Boolean isEditable;
    
    @Column(name="isVisible")
    private Boolean isVisible;
}
