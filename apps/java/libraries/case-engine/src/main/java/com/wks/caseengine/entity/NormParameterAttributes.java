// package com.wks.caseengine.entity;

// import jakarta.persistence.*;
// import lombok.*;
// import org.hibernate.annotations.UuidGenerator;
// import java.util.UUID;

// @Entity
// @Table(name = "NormParameterAttributes")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class NormParameterAttributes {

//     @Id
//     @UuidGenerator
//     @Column(name = "Id", nullable = false)
//     private UUID id;

//     @Column(name = "Name", nullable = false, length = 255)
//     private String name;

//     @Column(name = "DisplayName", nullable = false, length = 255)
//     private String displayName;

//     @Column(name = "TagName", length = 255)
//     private String tagName;

//     @Column(name = "DataSource", length = 255)
//     private String dataSource;

//     @Column(name = "AggrigationType", length = 100)
//     private String aggrigationType;

//     @Column(name = "DefaultValue")
//     private Double defaultValue;

//     @Column(name = "NormParameter_FK_Id")
//     private UUID normParameterFkId;

    
// }
