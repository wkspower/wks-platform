package com.wks.caseengine.jpa.entity;
import java.util.List;
import java.util.UUID;

import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseDocument;
import com.wks.caseengine.cases.instance.CaseOwner;
import com.wks.caseengine.jpa.entity.converter.CaseCommentListConverter;
import com.wks.caseengine.jpa.entity.converter.CaseDefAttributeConverter;
import com.wks.caseengine.jpa.entity.converter.CaseDocumentListConverter;
import com.wks.caseengine.jpa.entity.converter.CaseOwnerConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "case_instance")
@Getter
@Setter
public class CaseInstanceEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uid;

    @Column(name = "business_key", unique = true, nullable = false)
    private String businessKey;

    @Column(name = "status")
    private String status;

    @Column(name = "stage")
    private String stage;
    
    @Column(name = "queue_id")
    private String queueId;

    @Column(name = "attributes", columnDefinition = "TEXT")
    @Convert(converter = CaseDefAttributeConverter.class)
    private List<CaseAttribute> attributes;
    
    @Column(name = "documents", columnDefinition = "TEXT")
    @Convert(converter = CaseDocumentListConverter.class)
    private List<CaseDocument> documents;

	@Column(name="comments", columnDefinition = "TEXT")
	@Convert(converter = CaseCommentListConverter.class)
    private List<CaseComment> comments;
	
	@Column(name = "case_definition_id")
	private String caseDefinitionId;

	@Column(name  = "owner", columnDefinition = "TEXT")
    @Convert(converter = CaseOwnerConverter.class)
	private CaseOwner owner;
    
}
