package com.wkspower.platform.engine;

import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.service.WorkflowAdapterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Story 4.4a — listens for {@link ConfigDeployed} events and registers the singleton {@link
 * BpmnWorkflowAdapter} with the {@link WorkflowAdapterBinder} for the deployed CaseType version.
 * Architecture Decision 22: a CaseType with a BPMN attachment routes its signals through the BPMN
 * adapter; CaseTypes with no attachment fall through to {@code NullAdapter} via the binder's
 * resolution rule.
 *
 * <p>Lives in {@code engine/} alongside the adapter so the wiring stays inside the package that
 * already imports the engine SDK; ArchUnit's {@code hexagonalLayering} rule keeps {@code engine}
 * inaccessible from other layers.
 *
 * <p>Phase-0 — every {@link ConfigDeployed} carries a non-null {@code processDefinitionKey} (the
 * publish call is fired only on the BPMN-present path). When stage-scoped attachments land (Story
 * 4.5), the registration scope upgrades from {@link AttachmentScope#ofCase()}.
 */
@Component
public class BpmnWorkflowAdapterRegistrar {

  private static final Logger log = LoggerFactory.getLogger(BpmnWorkflowAdapterRegistrar.class);

  private final BpmnWorkflowAdapter adapter;
  private final WorkflowAdapterBinder binder;
  private final ExecutionSignalHandler handler;

  public BpmnWorkflowAdapterRegistrar(
      BpmnWorkflowAdapter adapter, WorkflowAdapterBinder binder, ExecutionSignalHandler handler) {
    this.adapter = adapter;
    this.binder = binder;
    this.handler = handler;
  }

  /**
   * Eagerly subscribe the adapter to the signal handler at boot so that BPMN execution-listener
   * callbacks from pre-deploy process instances (i.e. surviving a JVM restart before any {@link
   * ConfigDeployed} fires) are not silently dropped.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    adapter.onExecutionSignal(handler);
    log.debug("BpmnWorkflowAdapter handler eagerly registered at application ready");
  }

  @EventListener
  public void onConfigDeployed(ConfigDeployed event) {
    if (event.processDefinitionKey() == null) {
      // Zero-attachment CaseType — leave NullAdapter as the binder default (AC3).
      return;
    }
    CaseTypeRef ref = new CaseTypeRef(event.caseTypeId(), String.valueOf(event.version()));
    adapter.attach(ref, AttachmentScope.ofCase());
    binder.register(ref, adapter);
    log.debug(
        "BpmnWorkflowAdapter registered for caseType={} version={} processDefinitionKey={}",
        event.caseTypeId(),
        event.version(),
        event.processDefinitionKey());
  }
}
