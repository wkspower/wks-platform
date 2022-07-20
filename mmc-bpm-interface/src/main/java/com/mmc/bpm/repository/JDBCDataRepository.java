package com.mmc.bpm.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mmc.bpm.cases.instance.CaseAttribute;
import com.mmc.bpm.cases.instance.CaseInstance;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

@Component
public class JDBCDataRepository implements DataRepository {

	private final String DATABASE_URL = "jdbc:h2:file:./mmc_bpm_interface";
	private Connection connection;

	public JDBCDataRepository() throws Exception {
		try {
			this.connection = DriverManager.getConnection(DATABASE_URL);
		} catch (SQLException e) {
			throw new Exception(e);
		}
	}

	@PostConstruct
	private void postConstruct() throws Exception {

		try (var statement = connection.createStatement();) {

			statement.executeUpdate("DROP TABLE IF EXISTS generic_case;");

			statement.executeUpdate("CREATE TABLE generic_case ("

					+ "business_key varchar(255),"

					+ "attributes CLOB,"

					+ "processes CLOB);");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	@Override
	public List<CaseInstance> findCaseInstances() throws Exception {
		List<CaseInstance> casesInstances = new ArrayList<>();

		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement
					.executeQuery("SELECT business_key, attributes, processes FROM generic_case;");
			while (resultSet.next()) {
				String businessKey = resultSet.getString("business_key");

				Gson gson = new Gson();
				List<CaseAttribute> attributes = gson.fromJson(resultSet.getString("attributes"),
						new TypeToken<List<CaseAttribute>>() {
						}.getType());

				gson = new Gson();
				List<ProcessInstance> processes = gson.fromJson(resultSet.getString("processes"),
						new TypeToken<List<ProcessInstance>>() {
						}.getType());

				casesInstances.add(CaseInstance.builder().businessKey(businessKey).attributes(attributes)
						.processesInstances(processes).build());
			}

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
		return casesInstances;
	}

	@Override
	public void saveCaseInstance(CaseInstance caseInstance) throws Exception {

		Gson gson = new Gson();
		String attributesJSONString = gson.toJson(caseInstance.getAttributes());

		gson = new Gson();
		String processesJSONString = gson.toJson(caseInstance.getProcessesInstances());

		try (var statement = connection.createStatement();) {

			statement.executeUpdate("INSERT INTO generic_case (business_key, attributes, processes) VALUES ("

					+ "\'" + caseInstance.getBusinessKey() + "\'" + ", "

					+ "\'" + attributesJSONString + "\'" + ", "

					+ "\'" + processesJSONString + "\'"

					+ ");");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	@Override
	public void delete(CaseInstance caseInstance) throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("DELETE generic_case WHERE business_key = "

					+ "'" + caseInstance.getBusinessKey() + "'" + ";");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

}
