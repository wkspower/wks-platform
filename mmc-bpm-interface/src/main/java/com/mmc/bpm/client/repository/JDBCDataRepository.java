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
import com.mmc.bpm.client.cases.definition.CaseDefinition;
import com.mmc.bpm.client.cases.definition.CaseDefinitionNotFoundException;
import com.mmc.bpm.client.cases.instance.CaseAttribute;
import com.mmc.bpm.client.cases.instance.CaseInstance;
import com.mmc.bpm.client.cases.instance.CaseInstanceNotFoundException;
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
		createCaseDefinitionTable();
		createCaseInstanceTable();
	}

	private void createCaseInstanceTable() throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS case_instance ("

					+ "business_key varchar(255),"

					+ "case_definition_id varchar(255),"

					+ "status varchar(20),"

					+ "attributes CLOB,"

					+ "processes CLOB);");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	private void createCaseDefinitionTable() throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS case_definition ("

					+ "id varchar(255),"

					+ "case_definition_id varchar(255),"

					+ "name varchar(50),"

					+ "on_create_process_definition_keys CLOB);");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	@Override
	public List<CaseInstance> findCaseInstances() throws Exception {
		List<CaseInstance> casesInstances = new ArrayList<>();

		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement.executeQuery(
					"SELECT business_key, status, attributes, processes, case_definition_id FROM case_instance;");
			while (resultSet.next()) {
				String businessKey = resultSet.getString("business_key");
				String status = resultSet.getString("status");
				String caseDefId = resultSet.getString("caseDefinitionId");

				Gson gson = new Gson();
				List<CaseAttribute> attributes = gson.fromJson(resultSet.getString("attributes"),
						new TypeToken<List<CaseAttribute>>() {
						}.getType());

				gson = new Gson();
				List<ProcessInstance> processes = gson.fromJson(resultSet.getString("processes"),
						new TypeToken<List<ProcessInstance>>() {
						}.getType());

				casesInstances.add(CaseInstance.builder().businessKey(businessKey).attributes(attributes).status(status)
						.caseDefinitionId(caseDefId).processesInstances(processes).build());
			}

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
		return casesInstances;
	}

	@Override
	public CaseInstance getCaseInstance(final String businessKeyParam) throws Exception {

		// TODO ensure single return (throw specific exception otherwise)
		try (var statement = connection.createStatement();) {
			ResultSet resultSet = statement.executeQuery(
					"SELECT business_key, status, attributes, processes, case_definition_id FROM case_instance where business_key = '"
							+ businessKeyParam + "';");
			while (resultSet.next()) {
				String businessKey = resultSet.getString("business_key");
				String status = resultSet.getString("status");
				String caseDefinitionId = resultSet.getString("caseDefinitionId");

				Gson gson = new Gson();
				List<CaseAttribute> attributes = gson.fromJson(resultSet.getString("attributes"),
						new TypeToken<List<CaseAttribute>>() {
						}.getType());

				gson = new Gson();
				List<ProcessInstance> processes = gson.fromJson(resultSet.getString("processes"),
						new TypeToken<List<ProcessInstance>>() {
						}.getType());
				return CaseInstance.builder().businessKey(businessKey).attributes(attributes).status(status)
						.caseDefinitionId(caseDefinitionId).processesInstances(processes).build();
			}
		}

		throw new CaseInstanceNotFoundException();
	}

	@Override
	public void saveCaseInstance(CaseInstance caseInstance) throws Exception {

		Gson gson = new Gson();
		String attributesJSONString = gson.toJson(caseInstance.getAttributes());

		gson = new Gson();
		String processesJSONString = gson.toJson(caseInstance.getProcessesInstances());

		try (var statement = connection.createStatement();) {

			statement.executeUpdate(
					"INSERT INTO case_instance (business_key, status, attributes, processes, case_definition_id) VALUES ("

							+ "\'" + caseInstance.getBusinessKey() + "\'" + ", "

							+ "\'" + caseInstance.getCaseDefinitionId() + "\'" + ", "

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

			statement.executeUpdate("UPDATE case_instance "

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
	public void deleteCase(CaseInstance caseInstance) throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("DELETE case_instance WHERE business_key = "

					+ "'" + caseInstance.getBusinessKey() + "'" + ";");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	@Override
	public List<CaseDefinition> findCaseDefintions() throws Exception {
		List<CaseDefinition> casesDefinitions = new ArrayList<>();

		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement
					.executeQuery("SELECT id, name, on_create_process_definition_keys FROM case_definition;");
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String name = resultSet.getString("name");

				Gson gson = new Gson();
				List<String> onCreateProcessDefinitionKeys = gson.fromJson(
						resultSet.getString("on_create_process_definition_keys"), new TypeToken<List<String>>() {
						}.getType());

				casesDefinitions.add(CaseDefinition.builder().id(id).name(name)
						.onCreateProcessDefinitions(onCreateProcessDefinitionKeys).build());
			}

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
		return casesDefinitions;
	}

	@Override
	public CaseDefinition getCaseDefinition(String caseDefId) throws Exception {

		// TODO ensure single return (throw specific exception otherwise)
		try (var statement = connection.createStatement();) {
			ResultSet resultSet = statement
					.executeQuery("SELECT id, name, on_create_process_definition_keys FROM case_definition where id = '"
							+ caseDefId + "';");
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String name = resultSet.getString("name");

				Gson gson = new Gson();
				List<String> onCreateProcessDefinitionKeys = gson.fromJson(
						resultSet.getString("on_create_process_definition_keys"), new TypeToken<List<String>>() {
						}.getType());

				return CaseDefinition.builder().id(id).name(name)
						.onCreateProcessDefinitions(onCreateProcessDefinitionKeys).build();
			}
		}

		throw new CaseDefinitionNotFoundException();
	}

	@Override
	public void saveCaseDefinition(CaseDefinition caseDefinition) throws Exception {
		Gson gson = new Gson();
		String onCreateProcessDefKeysJSONString = gson.toJson(caseDefinition.getOnCreateProcessDefinitions());

		try (var statement = connection.createStatement();) {

			statement.executeUpdate("INSERT INTO case_definition (id, name, on_create_process_definition_keys) VALUES ("

					+ "\'" + caseDefinition.getId() + "\'" + ", "

					+ "\'" + caseDefinition.getName() + "\'" + ", "

					+ "\'" + onCreateProcessDefKeysJSONString + "\'"

					+ ");");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}

	}

	@Override
	public void deleteCaseDefinition(String caseDefId) throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("DELETE case_definition WHERE id = "

					+ "'" + caseDefId + "'" + ";");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}

	}

}
