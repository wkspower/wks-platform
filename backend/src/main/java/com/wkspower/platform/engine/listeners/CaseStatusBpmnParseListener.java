package com.wkspower.platform.engine.listeners;

import org.cibseven.bpm.engine.delegate.ExecutionListener;
import org.cibseven.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.cibseven.bpm.engine.impl.pvm.process.ActivityImpl;
import org.cibseven.bpm.engine.impl.pvm.process.ScopeImpl;
import org.cibseven.bpm.engine.impl.util.xml.Element;

/**
 * Programmatic registration of {@link CaseStatusListener} on every parsed user task and end event.
 * Wires once at engine boot via {@code CaseStatusEnginePlugin} (Story 2.4 Dev Notes §Listener
 * registration) — keeps BPMN XML clean and prevents per-template drift.
 */
public class CaseStatusBpmnParseListener extends AbstractBpmnParseListener {

  private final ExecutionListener listener;

  public CaseStatusBpmnParseListener(ExecutionListener listener) {
    this.listener = listener;
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    activity.addExecutionListener(ExecutionListener.EVENTNAME_END, listener);
  }

  @Override
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    activity.addExecutionListener(ExecutionListener.EVENTNAME_END, listener);
  }
}
