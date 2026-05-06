package com.wkspower.platform.engine.listeners;

import com.wkspower.platform.domain.port.BackendSignal;
import com.wkspower.platform.domain.port.BackendSignalKind;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.engine.BpmnBackendAdapter;
import com.wkspower.platform.engine.properties.CamundaPropertyReader;
import java.util.Map;
import java.util.UUID;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.engine.delegate.ExecutionListener;
import org.cibseven.bpm.model.bpmn.instance.EndEvent;
import org.cibseven.bpm.model.bpmn.instance.FlowElement;
import org.cibseven.bpm.model.bpmn.instance.UserTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Story 4.4a — engine-callback bridge that converts BPMN execution events into {@link
 * BackendSignal}s and dispatches them through {@link BpmnBackendAdapter} (which forwards to the
 * registered {@link com.wkspower.platform.domain.service.BackendSignalRouter} per Story 4.3).
 *
 * <p><b>AC1 — direct-mutation REMOVED:</b> earlier revisions of this listener (Story 2.4) called
 * {@code CaseStatusUpdater.updateStatus} and published {@code CaseStatusChanged} directly. Story
 * 4.4a removes that path entirely; case state mutations now flow through the Mapping Layer router
 * (Architecture Decision 22). Manual transitions through {@code CaseService.transition} still use
 * the legacy direct path until Story 4.4b reroutes them — documented dual-state.
 *
 * <p><b>AC2 — userTask reframe:</b> the listener emits {@code USER_TASK_STATUS} when an explicit
 * {@code <camunda:property name="status">} is declared on the userTask, otherwise emits {@code
 * USER_TASK_COMPLETE} on task end. Both signals dispatch through the router which consults the
 * mapping layer for the configured rule.
 *
 * <p><b>AC5 — Phase-0 fallback REMOVED:</b> the legacy {@code resolveNewStatus} userTask fallback
 * picked {@code getActiveActivityIds → first non-self} which broke under parallel gateways. That
 * branch is gone; the deploy-time validator ({@code BpmnValidator}, AC5) now rejects userTasks
 * lacking an explicit status property at deploy time when stage-scoped statuses are in play. See
 * {@code WKS-CFG-024}.
 */
@Component
public class CaseStatusListener implements ExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(CaseStatusListener.class);

  private final BpmnBackendAdapter adapter;

  public CaseStatusListener(BpmnBackendAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public void notify(DelegateExecution execution) {
    Object caseIdRaw = execution.getVariable("caseId");
    if (caseIdRaw == null) {
      // Not a WKS-managed process — silently no-op so embedded test fixtures and admin BPMNs
      // without our convention don't trip.
      return;
    }
    UUID caseId;
    try {
      caseId = UUID.fromString(caseIdRaw.toString());
    } catch (IllegalArgumentException ex) {
      log.warn(
          "CaseStatusListener: 'caseId' variable is not a UUID (value={}); skipping signal emit",
          caseIdRaw);
      return;
    }
    Object caseTypeIdRaw = execution.getVariable("caseTypeId");
    Object caseTypeVersionRaw = execution.getVariable("caseTypeVersion");
    if (caseTypeIdRaw == null || caseTypeVersionRaw == null) {
      log.warn(
          "CaseStatusListener: missing caseTypeId / caseTypeVersion process variables for"
              + " caseId={} — skipping signal emit (Story 4.4a requires both)",
          caseId);
      return;
    }
    CaseTypeRef caseTypeRef =
        new CaseTypeRef(caseTypeIdRaw.toString(), caseTypeVersionRaw.toString());
    CaseInstanceRef caseInstanceRef = new CaseInstanceRef(caseId, caseTypeRef);

    String currentElementId = execution.getCurrentActivityId();
    FlowElement element = execution.getBpmnModelElementInstance();

    BackendSignal signal = buildSignal(element, currentElementId, caseInstanceRef);
    if (signal == null) {
      return;
    }
    adapter.emit(signal);
  }

  /**
   * Build the {@link BackendSignal} for the BPMN element being ended, or {@code null} when the
   * element is not a status boundary the listener cares about.
   *
   * <ul>
   *   <li>End event → {@link BackendSignalKind#END_EVENT}; {@code source} is the explicit {@code
   *       camunda:property name="status"} value when present, else the element id (legacy behaviour
   *       preserved so end-event-id-as-status mappings keep working).
   *   <li>User task with explicit {@code <camunda:property name="status">} → {@link
   *       BackendSignalKind#USER_TASK_STATUS}; payload carries {@code value=<declared status>}.
   *   <li>User task without an explicit status property → {@link
   *       BackendSignalKind#USER_TASK_COMPLETE}; the router consults the mapping layer for any rule
   *       keyed on the userTask id (e.g. stage advance on completion).
   * </ul>
   */
  private static BackendSignal buildSignal(
      FlowElement element, String currentElementId, CaseInstanceRef caseInstanceRef) {
    if (element instanceof EndEvent endEvent) {
      String declared = CamundaPropertyReader.read(endEvent.getExtensionElements(), "status");
      String source = declared != null ? declared : currentElementId;
      Map<String, Object> payload = declared != null ? Map.of("value", declared) : Map.of();
      return new BackendSignal(
          BackendSignalKind.END_EVENT,
          BpmnBackendAdapter.ADAPTER_NAME,
          caseInstanceRef,
          source,
          payload);
    }
    if (element instanceof UserTask userTask) {
      String declared = CamundaPropertyReader.read(userTask.getExtensionElements(), "status");
      if (declared != null) {
        return new BackendSignal(
            BackendSignalKind.USER_TASK_STATUS,
            BpmnBackendAdapter.ADAPTER_NAME,
            caseInstanceRef,
            currentElementId,
            Map.of("value", declared));
      }
      // No explicit status property on this userTask — emit USER_TASK_COMPLETE so the router
      // can pick up any task-completion rule from the mapping layer (Story 4.3.1 AC10 split).
      // The Phase-0 fallback (pick "next active activity id" from getActiveActivityIds) is
      // deliberately gone — see AC5 / WKS-CFG-024.
      return new BackendSignal(
          BackendSignalKind.USER_TASK_COMPLETE,
          BpmnBackendAdapter.ADAPTER_NAME,
          caseInstanceRef,
          currentElementId,
          Map.of());
    }
    return null;
  }
}
