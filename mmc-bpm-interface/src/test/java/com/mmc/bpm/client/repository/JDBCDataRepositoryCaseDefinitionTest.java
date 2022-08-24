package com.mmc.bpm.client.repository;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mmc.bpm.client.cases.definition.CaseDefinition;
import com.mmc.bpm.client.cases.definition.event.CaseEvent;
import com.mmc.bpm.client.cases.definition.event.ProcessStartEvent;
import com.mmc.bpm.client.cases.definition.hook.create.PostCaseCreateHook;

//TODO create abstract test class for JDBC Repos
public class JDBCDataRepositoryCaseDefinitionTest {

	private final String DATABASE_URL = "jdbc:h2:file:~/mmc_bpm_interface_test";
	private Connection connection;

	private JDBCDataRepository jdbcDataRepository;

	@Before
	public void init() throws Exception {
		this.connection = DriverManager.getConnection(DATABASE_URL);

		deleteTables();

		DataBaseConfig dataBaseConfig = new DataBaseConfig();
		dataBaseConfig.setDatabaseURL(DATABASE_URL);
		jdbcDataRepository = new JDBCDataRepository(dataBaseConfig);
		jdbcDataRepository.postConstruct();

	}

	@After
	public void cleanUpTestData() throws Exception {
		deleteTables();
	}

	@Test
	public void getProcessDefinitionTest() throws Exception {

		// Given
		CaseEvent caseEvent = new ProcessStartEvent("1", "proc-def-key");

		PostCaseCreateHook postCaseCreateHook = new PostCaseCreateHook();
		postCaseCreateHook.attach(caseEvent);

		CaseDefinition caseDefinition = CaseDefinition.builder().id("1").name("generic-case")
				.postCaseCreateHook(postCaseCreateHook).build();
		jdbcDataRepository.saveCaseDefinition(caseDefinition);

		// When
		caseDefinition = jdbcDataRepository.getCaseDefinition("1");

		// Then
		assertEquals("1", caseDefinition.getId());
		assertEquals("generic-case", caseDefinition.getName());
		assertEquals("proc-def-key", ((ProcessStartEvent) caseDefinition.getPostCaseCreateHook().getCaseEvents().get(0))
				.getProcessDefinitionKey());
	}

	@Test
	public void createProcessDefinitionTest() throws Exception {

		CaseDefinition caseDefinition = CaseDefinition.builder().id("1").name("generic-case").build();

		jdbcDataRepository.saveCaseDefinition(caseDefinition);

		String id;
		String name;
		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement.executeQuery("SELECT id, name FROM case_definition;");
			resultSet.next();
			id = resultSet.getString("id");
			name = resultSet.getString("name");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}

		assertEquals("1", id);
		assertEquals("generic-case", name);
	}

	private void deleteTables() throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("DROP TABLE IF EXISTS case_definition");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

}
