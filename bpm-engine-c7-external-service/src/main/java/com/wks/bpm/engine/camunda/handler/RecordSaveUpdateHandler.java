package com.wks.bpm.engine.camunda.handler;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;
import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.record.RecordService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ExternalTaskSubscription(topicName = "recordSave", includeExtensionProperties = true)
@Slf4j
public class RecordSaveUpdateHandler implements ExternalTaskHandler {

	@Autowired
	private SecurityContextTenantHolder holder;

	@Autowired
	private RecordService recordService;

	@Autowired
	private ExternalServiceErrorHandler errorHandler;
	
	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public void execute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {
		try {
			log.info("Starting External Task Handler processing '{}' for tenant '{}'", externalTask.getActivityId());
			
			if (externalTask.getTenantId() == null) {
				log.warn("Could not start External Task Handler processing '{}' without tenant id", externalTask.getActivityId());
				return;
			}
			
			holder.setTenantId(externalTask.getTenantId());
			
			String record = externalTask.getVariable("record");
			
			recordService.save(externalTask.getVariable("recordTypeId"), gsonBuilder.create().fromJson(record, com.google.gson.JsonObject.class));
			
			externalTaskService.complete(externalTask);
		} catch (Exception e) {
			log.info("Error saving new record: {}", externalTask.getActivityId(), externalTask.getVariable("record"));
			errorHandler.handle("Error saving new record", externalTaskService, externalTask, e);
		} finally {
			holder.clear();
		}
	}

}