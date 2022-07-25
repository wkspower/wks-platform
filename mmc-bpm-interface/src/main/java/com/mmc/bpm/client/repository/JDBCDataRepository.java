package com.mmc.bpm.client.repository;

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
import com.mmc.bpm.client.cases.instance.CaseAttribute;
import com.mmc.bpm.client.cases.instance.CaseInstance;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

@Component
public class JDBCDataRepository implements DataRepository {

	private Connection connection;

	public JDBCDataRepository(final DataBaseConfig databaseConfig) throws Exception {
		try {
			this.connection = DriverManager.getConnection(databaseConfig.getDatabaseURL());
		} catch (SQLException e) {
			throw new Exception(e);
		}
	}

	@PostConstruct
	protected void postConstruct() throws Exception {

		try (var statement = connection.createStatement();) {

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS generic_case ("

					+ "business_key varchar(255),"

					+ "status varchar(20),"

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
					.executeQuery("SELECT business_key, status, attributes, processes FROM generic_case;");
			while (resultSet.next()) {
				String businessKey = resultSet.getString("business_key");
				String status = resultSet.getString("status");

				Gson gson = new Gson();
				List<CaseAttribute> attributes = gson.fromJson(resultSet.getString("attributes"),
						new TypeToken<List<CaseAttribute>>() {
						}.getType());

				gson = new Gson();
				List<ProcessInstance> processes = gson.fromJson(resultSet.getString("processes"),
						new TypeToken<List<ProcessInstance>>() {
						}.getType());

				casesInstances.add(CaseInstance.builder().businessKey(businessKey).attributes(attributes).status(status)
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

			statement.executeUpdate("INSERT INTO generic_case (business_key, status, attributes, processes) VALUES ("

					+ "\'" + caseInstance.getBusinessKey() + "\'" + ", "

					+ "\'" + caseInstance.getStatus() + "\'" + ", "

					+ "\'" + attributesJSONString + "\'" + ", "

					+ "\'" + processesJSONString + "\'"

					+ ");");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	@Override
	public void updateCaseStatus(String businessKey, String newStatus) throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("UPDATE generic_case "

					+ "SET status =" + "'" + newStatus + "'"

					+ " WHERE business_key = "

					+ "'" + businessKey + "'"

					+ ";");

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
