package com.wks.bpm.engine.camunda;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceErrorHandler {

	private static final int DEFAULT_RETRY = 3;

	public void handle(final String errorMessage, final ExternalTaskService externalTaskService,
			final ExternalTask externalTask, final Exception e) {

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);

		externalTaskService.handleFailure(externalTask, errorMessage, stringWriter.toString(),
				externalTask.getRetries() == null ? DEFAULT_RETRY : externalTask.getRetries() - 1, 3000L);

	}

}
