package com.mmc.bpm.engine.camunda;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.mmc.bpm.client.cases.instance.CaseInstanceService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ExternalTaskSubscription("caseStatusUpdate")
@Slf4j
public class CaseUpdateHandler implements ExternalTaskHandler {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Override
	public void execute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {
		log.debug("Starting External Task Handler processing...");

		try {
			caseInstanceService.updateStatus(externalTask.getBusinessKey(), externalTask.getActivityId());
			externalTaskService.complete(externalTask);
		} catch (Exception e) {
			//TODO error handling
			e.printStackTrace();
		}

	}

}