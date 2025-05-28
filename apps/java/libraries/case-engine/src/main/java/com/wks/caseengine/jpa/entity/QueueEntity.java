package com.wks.caseengine.jpa.entity;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "queue")
@Getter
@Setter
public class QueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    private UUID uid;

    @Column(name = "id", nullable = false)
    private String id;
    
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;
    
}
