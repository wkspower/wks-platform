package com.wks.caseengine.jpa.entity;
import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "case_email")
@Getter
@Setter
public class CaseEmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uid;

    @Column(name = "case_instance_business_key", nullable = false)
    private String caseInstanceBusinessKey;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "sender")
    private String sender;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "received_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date receivedDateTime;

    @Column(name = "has_attachments")
    private Boolean hasAttachments;

    @Column(name = "to_email")
    private String to;

    @Column(name = "from_email")
    private String from;

    @Column(name = "body_preview", columnDefinition = "TEXT")
    private String bodyPreview;

    @Column(name = "importance")
    private String importance;

    @Column(name = "case_definition_id")
    private String caseDefinitionId;

    @Column(name = "outbound")
    private Boolean outbound;

    @Column(name = "status")
    private String status;
    
}
