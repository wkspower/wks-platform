package com.wks.bpm.engine.camunda;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.wks.caseengine.record.RecordService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ExternalTaskSubscription(topicName = "recordSave", includeExtensionProperties = true)
@Slf4j
public class RecordSaveUpdateHandler implements ExternalTaskHandler {

	@Autowired
	private RecordService recordService;

	@Autowired
	private ExternalServiceErrorHandler errorHandler;

	@Override
	public void execute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {
		log.debug("Starting External Task Handler processing..." + externalTask.getActivityId());

		try {

			String record = externalTask.getVariable("record");
			recordService.save(externalTask.getVariable("recordTypeId"),
					new Gson().fromJson(record, com.google.gson.JsonObject.class));
			externalTaskService.complete(externalTask);
		} catch (Exception e) {
			log.error("Error saving new record: " + externalTask.getVariable("record"));
			
			errorHandler.handle("Error saving new record", externalTaskService, externalTask, e);

			// TODO error handling
			e.printStackTrace();

		}

	}

}