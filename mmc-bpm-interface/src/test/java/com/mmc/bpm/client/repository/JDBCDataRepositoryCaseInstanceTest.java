package com.mmc.bpm.client.repository;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mmc.bpm.client.cases.instance.CaseAttribute;
import com.mmc.bpm.client.cases.instance.CaseInstance;

//TODO create abstract test class for JDBC Repos
public class JDBCDataRepositoryCaseInstanceTest {

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
	public void shouldCreateCaseInstance_withAttributes() throws Exception {

		// Given
		List<CaseAttribute> caseAttributes = new ArrayList<>();
		caseAttributes.add(CaseAttribute.builder().name("name").value("aName").build());
		caseAttributes.add(CaseAttribute.builder().name("value").value("aValue").build());

		CaseInstance caseInstance = CaseInstance.builder().id("1").businessKey("1").attributes(caseAttributes).build();

		// When
		jdbcDataRepository.saveCaseInstance(caseInstance);

		// Then
		String id;
		String businessKey;
		List<CaseAttribute> attributes;
		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement.executeQuery("SELECT business_key, attributes FROM case_instance;");
			resultSet.next();
			Gson gson = new Gson();

			businessKey = resultSet.getString("business_key");
			attributes = gson.fromJson(resultSet.getString("attributes"), new TypeToken<List<CaseAttribute>>() {
			}.getType());

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}

		assertEquals("1", businessKey);
		assertEquals("name", attributes.get(0).getName());
		assertEquals("aName", attributes.get(0).getValue());
		assertEquals("value", attributes.get(1).getName());
		assertEquals("aValue", attributes.get(1).getValue());
	}

	@Test
	public void updateStatusTest() throws Exception {

		CaseInstance caseInstance = CaseInstance.builder().businessKey("BK-01").build();

		jdbcDataRepository.saveCaseInstance(caseInstance);

		jdbcDataRepository.updateCaseStatus("BK-01", "REVIEWED");

		String status;
		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement.executeQuery("SELECT status FROM case_instance;");
			resultSet.next();
			status = resultSet.getString("status");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}

		assertEquals("REVIEWED", status);

	}

	private void deleteTables() throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("DROP TABLE IF EXISTS case_instance");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

}
