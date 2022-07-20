package com.mmc.bpm.engine.camunda;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.context.annotation.Configuration;

@Configuration
@ExternalTaskSubscription("caseStatusUpdate")
public class CaseUpdateHandler implements ExternalTaskHandler {

//	@Autowired
//	private CaseInstanceService caseInstanceService;

	@Override
	public void execute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {
//		log.debug("Starting External Task Handler processing...");
//
//		try {
//			caseInstanceService.updateStatus(externalTask.getBusinessKey(), "REVIEWED");
//			externalTaskService.complete(externalTask);
//		} catch (Exception e) {
//			// error handling
//			e.printStackTrace();
//		}

	}

}