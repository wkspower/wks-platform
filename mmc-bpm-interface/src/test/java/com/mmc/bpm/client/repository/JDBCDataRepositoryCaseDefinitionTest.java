package com.mmc.bpm.client.repository;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mmc.bpm.client.cases.definition.CaseDefinition;

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
	public void createProcessDefinitionTest() throws Exception {

		CaseDefinition caseDefinition = CaseDefinition.builder().id("1").name("generic-case")
				.onCreateProcessDefinitions(Arrays.asList("1", "2")).build();

		jdbcDataRepository.saveCaseDefinition(caseDefinition);

		String id;
		String name;
		List<String> onCreateProcessDefKey;
		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement
					.executeQuery("SELECT id, name, on_create_process_definition_keys FROM case_definition;");
			resultSet.next();
			id = resultSet.getString("id");
			name = resultSet.getString("name");
			onCreateProcessDefKey = new Gson().fromJson(resultSet.getString("on_create_process_definition_keys"),
					new TypeToken<List<String>>() {
					}.getType());

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}

		assertEquals("1", id);
		assertEquals("generic-case", name);
		assertEquals(Arrays.asList("1", "2"), onCreateProcessDefKey);

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
