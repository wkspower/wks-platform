package com.wks.caseengine.process.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ProcessMessage;

@Component
public class MessageSenderServiceImpl implements MessageSenderService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;
	
	@Override
	public void sendMessage(final ProcessMessage processMessage) throws Exception {
		processEngineClient.sendMessage(processMessage);

	}

}
