package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.caseengine.process.message.MessageSenderService;

@RestController
public class MessageController {

	@Autowired
	private MessageSenderService messageSenderService;

	@PostMapping(value = "/message")
	public void save(@RequestBody ProcessMessage processMessage) throws Exception {
		messageSenderService.sendMessage(processMessage);
	}

}
