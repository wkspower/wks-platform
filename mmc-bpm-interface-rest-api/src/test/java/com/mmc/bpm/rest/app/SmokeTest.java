package com.mmc.bpm.rest.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mmc.bpm.rest.server.CaseController;
import com.mmc.bpm.rest.server.CaseDefinitionController;
import com.mmc.bpm.rest.server.CaseEventTypeController;
import com.mmc.bpm.rest.server.FormController;
import com.mmc.bpm.rest.server.HealthCheckController;
import com.mmc.bpm.rest.server.MessageController;
import com.mmc.bpm.rest.server.TaskController;
import com.mmc.bpm.rest.server.VariableController;

@SpringBootTest
public class SmokeTest {

	@Autowired
	private CaseController caseController;

	@Autowired
	private CaseDefinitionController caseDefinitionController;

	@Autowired
	private FormController formController;

	@Autowired
	private HealthCheckController healthCheckController;

	@Autowired
	private MessageController messageController;

	@Autowired
	private TaskController taskController;

	@Autowired
	private VariableController variableController;

	@Autowired
	private CaseEventTypeController caseEventTypeController;

	@Test
	public void shouldLoadContexts() throws Exception {
		assertThat(caseController).isNotNull();
		assertThat(caseDefinitionController).isNotNull();
		assertThat(formController).isNotNull();
		assertThat(healthCheckController).isNotNull();
		assertThat(messageController).isNotNull();
		assertThat(taskController).isNotNull();
		assertThat(variableController).isNotNull();
		assertThat(caseEventTypeController).isNotNull();
	}
}
