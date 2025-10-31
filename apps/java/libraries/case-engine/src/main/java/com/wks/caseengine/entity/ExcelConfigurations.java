package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "ExcelConfigurations")
@Data
public class ExcelConfigurations {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ScreenName", length = 200)
    private String screenName;

    @Column(name = "Excel_Id", length = 255)
    private String excelId;

    @Lob
    @Column(name = "JsonValue", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String jsonValue;

    @Column(name = "Vertical_FK_Id", nullable = false)
    private UUID verticalFkId;
    
    @Column(name = "Site_FK_Id")
    private UUID siteFkId;
    
    @Column(name = "CreatedOn")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "ModifiedOn")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;
   
}
