package com.wks.caseengine.tasks.event.complete;

import org.springframework.context.ApplicationEvent;

public class TaskCompleteEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public TaskCompleteEvent(final TaskCompleteEventObject source) {
		super(source);
	}

}