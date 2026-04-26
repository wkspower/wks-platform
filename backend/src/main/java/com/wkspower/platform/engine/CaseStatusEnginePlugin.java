package com.wkspower.platform.engine;

import com.wkspower.platform.engine.listeners.CaseStatusBpmnParseListener;
import com.wkspower.platform.engine.listeners.CaseStatusListener;
import java.util.ArrayList;
import org.cibseven.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.cibseven.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.cibseven.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.stereotype.Component;

/**
 * CIB seven {@link AbstractProcessEnginePlugin} that registers our case-status BPMN parse listener
 * with the engine at boot. Story 2.4 Dev Notes §Listener registration: programmatic registration
 * keeps BPMN XML free of {@code camunda:executionListener class="..."} boilerplate and guarantees
 * every deployed process is wired identically.
 */
@Component
public class CaseStatusEnginePlugin extends AbstractProcessEnginePlugin {

  private final CaseStatusListener caseStatusListener;

  public CaseStatusEnginePlugin(CaseStatusListener caseStatusListener) {
    this.caseStatusListener = caseStatusListener;
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl configuration) {
    if (configuration.getCustomPostBPMNParseListeners() == null) {
      configuration.setCustomPostBPMNParseListeners(new ArrayList<BpmnParseListener>());
    }
    configuration
        .getCustomPostBPMNParseListeners()
        .add(new CaseStatusBpmnParseListener(caseStatusListener));
  }
}
