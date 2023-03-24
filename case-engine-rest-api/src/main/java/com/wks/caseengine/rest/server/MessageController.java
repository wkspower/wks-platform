package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.caseengine.process.message.MessageSenderService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("message")
@Tag(name = "Message", description = "Send messages to processes")
public class MessageController {

	@Autowired
	private MessageSenderService messageSenderService;

	@PostMapping(value = "/")
	public void save(@RequestBody final ProcessMessage processMessage) throws Exception {
		messageSenderService.sendMessage(processMessage);
	}

}
