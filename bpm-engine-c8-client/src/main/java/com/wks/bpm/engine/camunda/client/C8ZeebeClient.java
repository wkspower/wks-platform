package com.wks.bpm.engine.camunda.client;

import org.springframework.stereotype.Component;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.model.spi.ProcessInstance;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

@Component
public class C8ZeebeClient {

	public ProcessInstance startProcess(String processDefinitionKey, final BpmEngine bpmEngine) {
		final String zeebeEndpoint = bpmEngine.getParameters().get("zeebeEndpoint").getAsString();
		final String zeebeEndpointPort = bpmEngine.getParameters().get("zeebeEndpointPort").getAsString();
		final String clientId = bpmEngine.getParameters().get("clientId").getAsString();
		final String clientSecret = bpmEngine.getParameters().get("clientSecret").getAsString();

		OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder().audience(zeebeEndpoint)
				.clientId(clientId).clientSecret(clientSecret).build();

		ProcessInstance processInstance = null;

		try (ZeebeClient client = ZeebeClient.newClientBuilder().gatewayAddress(zeebeEndpoint + ":" + zeebeEndpointPort)
				.credentialsProvider(credentialsProvider).build()) {

			final ProcessInstanceEvent processInstanceEvent = client.newCreateInstanceCommand()
					.bpmnProcessId(processDefinitionKey).latestVersion().send().join();
			processInstance = ProcessInstance.builder()
					.businessKey(String.valueOf(processInstanceEvent.getProcessInstanceKey())).build();
		}
		return processInstance;
	}

	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, final BpmEngine bpmEngine) {
		ProcessInstance processInstance = startProcess(processDefinitionKey, bpmEngine);
		processInstance.setBusinessKey(businessKey);
		updateBusinessKey(processInstance, businessKey);
		return processInstance;
	}

	private void updateBusinessKey(ProcessInstance processInstance, String businessKey) {
		throw new UnsupportedOperationException();

	}

}
