package com.mmc.bpm.engine.camunda;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.context.annotation.Configuration;

@Configuration
@ExternalTaskSubscription("creditScoreChecker")
public class CreditScoreCheckerHandler implements ExternalTaskHandler {

	@Override
	public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		System.out.println("External task executed!");
		externalTaskService.complete(externalTask);
	}

}