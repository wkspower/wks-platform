package com.mmc.bpm.client.process.message;

import com.mmc.bpm.engine.model.spi.ProcessMessage;

public interface MessageSenderService {

	public void sendMessage(final ProcessMessage processMessage);

}
