package com.wks.caseengine.shell.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.caseengine.process.message.MessageSenderService;

@ShellComponent
public class MessageCommand {

	@Autowired
	private MessageSenderService messageSenderService;

	@ShellMethod(value = "Sends a process message.")
	public String sendMessage(final String messageName, final String businessKey) {

		messageSenderService
				.sendMessage(ProcessMessage.builder().businessKey(businessKey).messageName(messageName).build());

		return "Message sent";

	}

}
