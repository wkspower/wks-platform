package com.wks.caseengine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ScreenDataConfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenDataConfig {

    @Id
    @Column(name = "Id", nullable = false)
    private UUID id;

    @Column(name = "ScreenName", nullable = false, length = 255)
    private String screenName;

    @Column(name = "Vertical_FK_Id", nullable = false)
    private UUID verticalFkId;

    @Column(name = "HeaderJson", columnDefinition = "NVARCHAR(MAX)")
    private String headerJson;

}
