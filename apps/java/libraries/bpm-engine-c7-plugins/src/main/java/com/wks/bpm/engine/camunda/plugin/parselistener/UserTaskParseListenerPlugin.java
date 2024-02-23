package com.wks.bpm.engine.camunda.plugin.parselistener;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

/**
 * <p>
 * {@link ProcessEnginePlugin} enabling the assignee informing parse listener.
 * </p>
 *
 */
public class UserTaskParseListenerPlugin extends AbstractProcessEnginePlugin {

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
		if (preParseListeners == null) {
			preParseListeners = new ArrayList<BpmnParseListener>();
			processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
		}
		preParseListeners.add(new UserTaskParseListener());
	}

}
