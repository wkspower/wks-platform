package com.wks.bpm.engine.camunda.plugin.parselistener;

import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

import com.wks.bpm.engine.camunda.plugin.notify.NotifyAssigneeTaskListener;

/**
 * BPMN Parse Listener to add task listener on user task
 *
 */
public class UserTaskParseListener extends AbstractBpmnParseListener {

	@Override
	public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
		ActivityBehavior activityBehavior = activity.getActivityBehavior();
		if (activityBehavior instanceof UserTaskActivityBehavior) {
			UserTaskActivityBehavior userTaskActivityBehavior = (UserTaskActivityBehavior) activityBehavior;
			userTaskActivityBehavior.getTaskDefinition().addTaskListener("create",
					NotifyAssigneeTaskListener.getInstance());
		}
	}
}
