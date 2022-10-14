package com.wks.caseengine.process.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.ProcessEngineClient;
import com.wks.bpm.engine.model.spi.ProcessMessage;

@Component
public class MessageSenderServiceImpl implements MessageSenderService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Override
	public void sendMessage(ProcessMessage processMessage) {
		processEngineClient.sendMessage(processMessage);

	}

}
