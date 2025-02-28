package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "BusinessDemand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessDemand {
    
    @Id
    @GeneratedValue
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "Remark", columnDefinition = "nvarchar(max)")
    private String remark;
    
    @Column(name = "NormParameter_FK_Id", nullable = false)
    private UUID normParameterId;
    
    @Column(name = "Jan", precision = 18, scale = 2)
    private Float jan;
    
    @Column(name = "Feb", precision = 18, scale = 2)
    private Float feb;
    
    @Column(name = "March", precision = 18, scale = 2)
    private Float march;
    
    @Column(name = "April", precision = 18, scale = 2)
    private Float april;
    
    @Column(name = "May", precision = 18, scale = 2)
    private Float may;
    
    @Column(name = "June", precision = 18, scale = 2)
    private Float june;
    
    @Column(name = "July", precision = 18, scale = 2)
    private Float july;
    
    @Column(name = "Aug", precision = 18, scale = 2)
    private Float aug;
    
    @Column(name = "Sep", precision = 18, scale = 2)
    private Float sep;
    
    @Column(name = "Oct", precision = 18, scale = 2)
    private Float oct;
    
    @Column(name = "Nov", precision = 18, scale = 2)
    private Float nov;
    
    @Column(name = "Dec", precision = 18, scale = 2)
    private Float dec;
    
    @Column(name = "Year", length = 9, nullable = false)
    private String year;
    
    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantId;
    
    @Column(name = "AvgTPH", precision = 18, scale = 2)
    private Float avgTph;
}
