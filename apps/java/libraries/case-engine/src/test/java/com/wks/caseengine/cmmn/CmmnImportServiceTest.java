/*
 * WKS Platform - Open-Source Project
 *
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 *
 * WKS Platform is licensed under the MIT License.
 *
 * © 2021 WKS Power. All rights reserved.
 *
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.cmmn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.CaseStageProcessDefinition;
import com.wks.caseengine.cases.definition.service.CaseDefinitionService;
import com.wks.caseengine.cmmn.bpmn.StageProcessGenerator;
import com.wks.caseengine.cmmn.form.CmmnFormGenerator;
import com.wks.caseengine.cmmn.map.CmmnToCaseDefinitionMapper;
import com.wks.caseengine.cmmn.parse.CmmnParseException;
import com.wks.caseengine.cmmn.parse.CmmnXmlReader;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormNotFoundException;
import com.wks.caseengine.form.FormService;

/**
 * Covers the orchestration: what gets written, in what order, and — most
 * importantly — that a dry run writes nothing at all.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CmmnImportServiceTest {

	private static String asylVerfahrenXml;

	// The pipeline stages are pure and already covered by their own tests; running
	// the real ones here is what makes this a test of the wiring rather than of mocks.
	@Spy
	private CmmnXmlReader reader = new CmmnXmlReader();

	@Spy
	private CmmnToCaseDefinitionMapper mapper = new CmmnToCaseDefinitionMapper();

	@Spy
	private StageProcessGenerator processGenerator = new StageProcessGenerator();

	@Spy
	private CmmnFormGenerator formGenerator = new CmmnFormGenerator();

	@Mock
	private CaseDefinitionService caseDefinitionService;

	@Mock
	private FormService formService;

	@Mock
	private BpmEngineClientFacade bpmEngineClient;

	@InjectMocks
	private CmmnImportService service;

	@org.junit.jupiter.api.BeforeEach
	void formsDoNotExistByDefault() {
		// doThrow, not when(...): the latter would invoke the stubbed method.
		doThrow(new FormNotFoundException()).when(formService).get(anyString());
	}

	@BeforeAll
	static void loadFixture() throws IOException {
		try (InputStream in = CmmnImportServiceTest.class.getResourceAsStream("/cmmn/asyl-verfahren.cmmn")) {
			assertNotNull(in, "fixture missing");
			asylVerfahrenXml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * The whole value of the preview: a user reviews the warnings and only then
	 * commits. If a dry run wrote anything, reviewing would be too late.
	 */
	@Test
	void shouldWriteNothingAtAllOnADryRun() {
		CmmnImportResult result = service.importModel(asylVerfahrenXml, true);

		assertTrue(result.isDryRun());
		verifyNoInteractions(caseDefinitionService);
		verifyNoInteractions(formService);
		verifyNoInteractions(bpmEngineClient);
	}

	/** A dry run must return exactly what a real import would produce. */
	@Test
	void shouldReturnTheSameResultDryOrNot() {
		CmmnImportResult preview = service.importModel(asylVerfahrenXml, true);
		CmmnImportResult committed = service.importModel(asylVerfahrenXml, false);

		assertEquals(preview.getCaseDefinition().getId(), committed.getCaseDefinition().getId());
		assertEquals(preview.getFormKeys(), committed.getFormKeys());
		assertEquals(preview.getWarnings().size(), committed.getWarnings().size());
		assertEquals(preview.getProcesses().stream().map(CmmnImportResult.ProcessSummary::getKey).toList(),
				committed.getProcesses().stream().map(CmmnImportResult.ProcessSummary::getKey).toList());
	}

	@Test
	void shouldDeployEveryGeneratedProcessAndSaveTheDefinition() {
		CmmnImportResult result = service.importModel(asylVerfahrenXml, false);

		assertFalse(result.isDryRun());
		verify(bpmEngineClient, times(5)).deploy(anyString(), anyString());
		verify(formService, times(2)).save(any(Form.class));
		verify(caseDefinitionService).create(any(CaseDefinition.class));
	}

	/**
	 * Forms and processes must exist before the definition that references them, or
	 * a failed deployment would leave a definition pointing at nothing.
	 */
	@Test
	void shouldCreateTheDefinitionOnlyAfterItsFormsAndProcessesExist() {
		org.mockito.InOrder order = org.mockito.Mockito.inOrder(formService, bpmEngineClient,
				caseDefinitionService);

		service.importModel(asylVerfahrenXml, false);

		order.verify(formService, times(2)).save(any(Form.class));
		order.verify(bpmEngineClient, times(5)).deploy(anyString(), anyString());
		order.verify(caseDefinitionService).create(any(CaseDefinition.class));
	}

	@Test
	void shouldPointTheCaseDefinitionAtTheGeneratedCaseForm() {
		CmmnImportResult result = service.importModel(asylVerfahrenXml, true);

		assertEquals("asyl-verfahren-fall", result.getCaseDefinition().getFormKey());
		assertTrue(result.getFormKeys().contains("asyl-verfahren-fall"));
		assertTrue(result.getFormKeys().contains("asyl-verfahren-aufgabe"));
	}

	/**
	 * The binding that makes the case self-driving: entering a stage starts its own
	 * process, while a discretionary one waits to be started by a person.
	 */
	@Test
	void shouldBindEachStagesProcessAsAutoStartAndDiscretionaryOnesNot() {
		CaseDefinition definition = service.importModel(asylVerfahrenXml, true).getCaseDefinition();

		for (CaseStage stage : definition.getStages()) {
			List<CaseStageProcessDefinition> autoStart = stage.getProcessesDefinitions().stream()
					.filter(CaseStageProcessDefinition::isAutoStart).toList();
			assertEquals(1, autoStart.size(), "stage " + stage.getName() + " needs exactly one auto-start process");
		}

		CaseStage einreichung = definition.getStages().stream().filter(s -> "Einreichung".equals(s.getName()))
				.findFirst().orElseThrow();

		assertEquals(2, einreichung.getProcessesDefinitions().size(), "its own process plus the discretionary one");
		assertEquals(1, einreichung.getProcessesDefinitions().stream()
				.filter(process -> !process.isAutoStart()).count());
	}

	/**
	 * Every process key bound to a stage must be one that was actually deployed,
	 * otherwise the case would try to start something that does not exist.
	 */
	@Test
	void shouldOnlyBindProcessKeysThatWereDeployed() {
		CmmnImportResult result = service.importModel(asylVerfahrenXml, false);

		ArgumentCaptor<String> fileNames = ArgumentCaptor.forClass(String.class);
		verify(bpmEngineClient, times(5)).deploy(fileNames.capture(), anyString());

		List<String> deployedKeys = fileNames.getAllValues().stream().map(name -> name.replace(".bpmn", "")).toList();

		result.getCaseDefinition().getStages().stream().flatMap(stage -> stage.getProcessesDefinitions().stream())
				.map(CaseStageProcessDefinition::getDefinitionKey)
				.forEach(key -> assertTrue(deployedKeys.contains(key), key + " is bound but was never deployed"));
	}

	/**
	 * Form keys derive from the case id, so a re-import meets the forms it created
	 * before. It must not fail there, and must not overwrite work done on them.
	 */
	@Test
	void shouldKeepAnExistingFormRatherThanFailingOrOverwritingIt() {
		doReturn(Form.builder().key("asyl-verfahren-fall").title("hand-tuned").build()).when(formService)
				.get("asyl-verfahren-fall");
		doThrow(new FormNotFoundException()).when(formService).get("asyl-verfahren-aufgabe");

		service.importModel(asylVerfahrenXml, false);

		// Only the missing one is written; the existing one is left exactly as it is.
		verify(formService, times(1)).save(any(Form.class));
		verify(caseDefinitionService).create(any(CaseDefinition.class));
	}

	@Test
	void shouldCarryTheMappingWarningsOntoTheResult() {
		CmmnImportResult result = service.importModel(asylVerfahrenXml, true);

		assertFalse(result.getWarnings().isEmpty());
		assertTrue(result.getWarnings().stream()
				.anyMatch(w -> w.message().contains("Antragstellung entgegennehmen")));
	}

	@Test
	void shouldRejectSomethingThatIsNotACmmnModelWithoutWritingAnything() {
		org.junit.jupiter.api.Assertions.assertThrows(CmmnParseException.class,
				() -> service.importModel("<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>", false));

		verify(caseDefinitionService, never()).create(any());
		verify(bpmEngineClient, never()).deploy(anyString(), anyString());
	}

}
