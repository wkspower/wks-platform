package com.wks.caseengine.process.message;

import java.util.Optional;

import com.google.gson.JsonArray;
import com.wks.bpm.engine.model.spi.ProcessMessage;

public interface MessageSenderService {

	void sendMessage(final ProcessMessage processMessage, final Optional<JsonArray> variables) throws Exception;

}
