package com.mmc.bpm.shell.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.mmc.bpm.client.process.message.MessageSenderService;
import com.mmc.bpm.engine.model.spi.ProcessMessage;

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
