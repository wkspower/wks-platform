package com.wkspower.platform.infrastructure.storage;

import com.wkspower.platform.domain.port.DocumentRepository;
import com.wkspower.platform.domain.port.DocumentStore;
import com.wkspower.platform.domain.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free {@link DocumentService} domain service with its infrastructure ports
 * (Story 14.2). Mirrors the {@code ConfigServiceConfig} pattern: the domain service is pure Java;
 * Spring wiring lives here in the infrastructure layer.
 */
@Configuration
public class DocumentServiceConfig {

  @Value("${wks.documents.max-size-mb:25}")
  private long maxSizeMb;

  @Bean
  public DocumentService documentService(
      DocumentStore documentStore, DocumentRepository documentRepository) {
    return new DocumentService(documentStore, documentRepository, maxSizeMb);
  }
}
