package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for {@code case_type_deployments} (Story 2.4 folded debt #1). Stores the caseTypeId →
 * processDefinitionKey mapping durably so {@code POST /api/cases} resolves a key for admin-deployed
 * case types after a JVM restart, even when no YAML is on disk.
 *
 * <p>Does NOT extend {@link BaseJpaEntity} — the primary key is a string ({@code case_type_id})
 * rather than a UUID, and there is no edit lifecycle (rows are upserted by the deploy event), so
 * the shared audit columns and version field would be dead weight.
 */
@Entity
@Table(name = "case_type_deployments")
public class CaseTypeDeploymentEntity {

  @Id
  @Column(name = "case_type_id", length = 64, nullable = false)
  private String caseTypeId;

  @Column(name = "case_type_version", nullable = false)
  private int caseTypeVersion;

  @Column(name = "process_definition_key", length = 255, nullable = false)
  private String processDefinitionKey;

  @Column(name = "deployment_id", length = 64, nullable = false)
  private String deploymentId;

  @Column(name = "deployed_at", nullable = false)
  private Instant deployedAt;

  protected CaseTypeDeploymentEntity() {
    // JPA
  }

  public CaseTypeDeploymentEntity(
      String caseTypeId,
      int caseTypeVersion,
      String processDefinitionKey,
      String deploymentId,
      Instant deployedAt) {
    this.caseTypeId = caseTypeId;
    this.caseTypeVersion = caseTypeVersion;
    this.processDefinitionKey = processDefinitionKey;
    this.deploymentId = deploymentId;
    this.deployedAt = deployedAt;
  }

  public String getCaseTypeId() {
    return caseTypeId;
  }

  public int getCaseTypeVersion() {
    return caseTypeVersion;
  }

  public void setCaseTypeVersion(int caseTypeVersion) {
    this.caseTypeVersion = caseTypeVersion;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Instant getDeployedAt() {
    return deployedAt;
  }

  public void setDeployedAt(Instant deployedAt) {
    this.deployedAt = deployedAt;
  }
}
