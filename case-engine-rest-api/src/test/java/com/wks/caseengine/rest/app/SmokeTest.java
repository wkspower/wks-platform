package com.wks.caseengine.rest.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.wks.caseengine.rest.server.CaseController;
import com.wks.caseengine.rest.server.CaseDefinitionController;
import com.wks.caseengine.rest.server.CaseEventTypeController;
import com.wks.caseengine.rest.server.FormController;
import com.wks.caseengine.rest.server.HealthCheckController;
import com.wks.caseengine.rest.server.MessageController;
import com.wks.caseengine.rest.server.TaskController;
import com.wks.caseengine.rest.server.TaskFormController;
import com.wks.caseengine.rest.server.VariableController;

@SpringBootTest
public class SmokeTest {

	@Autowired
	private CaseController caseController;

	@Autowired
	private CaseDefinitionController caseDefinitionController;

	@Autowired
	private TaskFormController taskFormController;

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

	@Autowired
	private FormController formController;

	@Test
	public void shouldLoadContexts() throws Exception {
		assertThat(caseController).isNotNull();
		assertThat(caseDefinitionController).isNotNull();
		assertThat(taskFormController).isNotNull();
		assertThat(healthCheckController).isNotNull();
		assertThat(messageController).isNotNull();
		assertThat(taskController).isNotNull();
		assertThat(variableController).isNotNull();
		assertThat(caseEventTypeController).isNotNull();
		assertThat(formController).isNotNull();
	}
}
