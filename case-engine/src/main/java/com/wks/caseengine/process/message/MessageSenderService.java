package com.wks.caseengine.process.message;

import com.wks.bpm.engine.model.spi.ProcessMessage;

public interface MessageSenderService {

	public void sendMessage(final ProcessMessage processMessage);

}
