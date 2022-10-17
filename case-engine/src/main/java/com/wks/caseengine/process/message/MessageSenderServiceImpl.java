package com.wks.caseengine.process.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.ProcessEngineClient;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.caseengine.repository.BpmEngineRepository;

@Component
public class MessageSenderServiceImpl implements MessageSenderService {

	@Autowired
	private ProcessEngineClient processEngineClient;
	
	@Autowired
	private BpmEngineRepository bpmEngineRepository;

	@Override
	public void sendMessage(final ProcessMessage processMessage, final String bpmEngineId) throws Exception {
		processEngineClient.sendMessage(processMessage, bpmEngineRepository.get(bpmEngineId));

	}

}
