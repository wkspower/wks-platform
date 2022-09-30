package com.wks.bpm.engine.camunda;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstanceService;

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
			caseInstanceService.updateStatus(externalTask.getBusinessKey(),
					CaseStatus.WIP_CASE_STATUS);
			externalTaskService.complete(externalTask);
		} catch (Exception e) {
			// TODO error handling
			e.printStackTrace();
		}

	}

}