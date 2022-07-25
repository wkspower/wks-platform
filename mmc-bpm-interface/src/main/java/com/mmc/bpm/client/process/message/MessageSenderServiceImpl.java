package com.mmc.bpm.client.process.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.camunda.client.ProcessEngineClient;
import com.mmc.bpm.engine.model.spi.ProcessMessage;

@Component
public class MessageSenderServiceImpl implements MessageSenderService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Override
	public void sendMessage(ProcessMessage processMessage) {
		processEngineClient.sendMessage(processMessage);

	}

}
