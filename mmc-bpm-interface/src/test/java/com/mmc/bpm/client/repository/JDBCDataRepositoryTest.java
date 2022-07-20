package com.mmc.bpm.client.repository;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.mmc.bpm.client.cases.instance.CaseInstance;

public class JDBCDataRepositoryTest {

	private final String DATABASE_URL = "jdbc:h2:file:~/mmc_bpm_interface";
	private Connection connection;

	@Before
	public void init() throws SQLException {
		this.connection = DriverManager.getConnection(DATABASE_URL);
	}

	@Test
	public void updateStatusTest() throws Exception {

		JDBCDataRepository jdbcDataRepository = new JDBCDataRepository();
		jdbcDataRepository.postConstruct();

		CaseInstance caseInstance = CaseInstance.builder().businessKey("BK-01").build();

		jdbcDataRepository.saveCaseInstance(caseInstance);

		jdbcDataRepository.updateCaseStatus("BK-01", "REVIEWED");

		String status;
		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement.executeQuery("SELECT status FROM generic_case;");
			resultSet.next();
			status = resultSet.getString("status");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}

		assertEquals("REVIEWED", status);

	}

}
