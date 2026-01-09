package com.wks.caseengine.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;

@Entity
@Table(name = "ApprovedAOP")
@Data
public class ApprovedAOP {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "AOPYear", length = 100)
    private String aopYear;

    @Column(name = "Plant_FK_Id", nullable = false)
    private UUID plantFkId;

}
