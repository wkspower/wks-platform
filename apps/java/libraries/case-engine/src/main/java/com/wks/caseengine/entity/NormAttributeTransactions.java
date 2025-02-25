package com.wks.caseengine.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "NormAttributeTransactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormAttributeTransactions {

    @Id
    @Column(name = "Id", nullable = false)
    private UUID id;

    @Column(name = "AttributeName", length = 250)
    private String attributeName;

    @Column(name = "AttributeValue", length = 250)
    private String attributeValue;

    @Column(name = "AttributeLable", length = 250)
    private String attributeLable;

    @Column(name = "AttributeValueLable", length = 250)
    private String attributeValueLable;

    @Column(name = "AOPMonth")
    private LocalDate aopMonth;

    @Column(name = "AuditYear")
    private Integer auditYear;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "CreatedOn")
    private LocalDateTime createdOn;

    @Column(name = "ModifiedOn")
    private LocalDateTime modifiedOn;

    @Column(name = "AttributeValueVersion", length = 10)
    private String attributeValueVersion;

    // If you want to avoid using the reserved keyword 'User' directly in Java,
    // you can rename the field and map it to the column "User".
    @Column(name = "User", length = 255)
    private String userName;

    @Column(name = "NormParameter_FK_Id")
    private UUID normParameterFKId;

    @Column(name = "CatalystAttribute_FK_Id")
    private UUID catalystAttributeFKId;

}
