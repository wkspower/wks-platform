package com.wks.caseengine.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseDefinitionNotFoundException;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.definition.event.CaseEvent;
import com.wks.caseengine.cases.definition.event.CaseEventDeserializer;
import com.wks.caseengine.cases.definition.hook.create.PostCaseCreateHook;
import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceNotFoundException;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormNotFoundException;

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
	public void postConstruct() throws Exception {
		createCaseDefinitionTable();
		createCaseInstanceTable();
		createFormTable();
	}

	private void createCaseInstanceTable() throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS case_instance ("

					+ "business_key varchar(255) UNIQUE,"

					+ "case_definition_id varchar(255),"

					+ "status varchar(20),"

					+ "attributes CLOB);");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	private void createCaseDefinitionTable() throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS case_definition ("

					+ "post_case_create_hook CLOB,"

					+ "id varchar(255) UNIQUE,"

					+ "name varchar(50));");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	private void createFormTable() throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("CREATE TABLE IF NOT EXISTS form ("

					+ "description varchar(255),"

					+ "components CLOB,"

					+ "form_key varchar(255) UNIQUE);");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	@Override
	public List<CaseInstance> findCaseInstances(final Optional<CaseStatus> caseStatus) throws Exception {
		List<CaseInstance> casesInstances = new ArrayList<>();

		try (var statement = connection.createStatement();) {

			StringBuilder where = new StringBuilder();
			if (caseStatus.isPresent()) {
				where.append("where status = '" + caseStatus.get().getCode() + "'");
			}

			ResultSet resultSet = statement.executeQuery(
					"SELECT business_key, status, attributes, case_definition_id FROM case_instance" + where + ";");
			while (resultSet.next()) {
				String businessKey = resultSet.getString("business_key");

				CaseStatus status = CaseStatus.valueOf(resultSet.getString("status"));
				String caseDefId = resultSet.getString("case_definition_id");

				Gson gson = new Gson();
				List<CaseAttribute> attributes = gson.fromJson(resultSet.getString("attributes"),
						new TypeToken<List<CaseAttribute>>() {
						}.getType());

				casesInstances.add(CaseInstance.builder().businessKey(businessKey).attributes(attributes).status(status)
						.caseDefinitionId(caseDefId).build());
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
					"SELECT business_key, status, attributes, case_definition_id FROM case_instance where business_key = '"
							+ businessKeyParam + "';");
			while (resultSet.next()) {
				String businessKey = resultSet.getString("business_key");
				CaseStatus status = CaseStatus.valueOf(resultSet.getString("status"));
				String caseDefinitionId = resultSet.getString("case_definition_id");

				Gson gson = new Gson();
				List<CaseAttribute> attributes = gson.fromJson(resultSet.getString("attributes"),
						new TypeToken<List<CaseAttribute>>() {
						}.getType());

				return CaseInstance.builder().businessKey(businessKey).attributes(attributes).status(status)
						.caseDefinitionId(caseDefinitionId).build();
			}
		}

		throw new CaseInstanceNotFoundException();
	}

	@Override
	public void saveCaseInstance(CaseInstance caseInstance) throws Exception {

		Gson gson = new Gson();
		String attributesJSONString = gson.toJson(caseInstance.getAttributes());

		try (var statement = connection.createStatement();) {

			statement.executeUpdate(
					"INSERT INTO case_instance (business_key, case_definition_id, status, attributes) VALUES ("

							+ "\'" + caseInstance.getBusinessKey() + "\'" + ", "

							+ "\'" + caseInstance.getCaseDefinitionId() + "\'" + ", "

							+ "\'" + caseInstance.getStatus().getCode() + "\'" + ", "

							+ "\'" + attributesJSONString + "\'"

							+ ");");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	@Override
	public void updateCaseStatus(final String businessKey, final CaseStatus newStatus) throws Exception {
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
	public void deleteCaseInstance(CaseInstance caseInstance) throws Exception {
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
					.executeQuery("SELECT id, name, post_case_create_hook FROM case_definition;");
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String name = resultSet.getString("name");

				final GsonBuilder builder = new GsonBuilder();
				builder.registerTypeAdapter(CaseEvent.class, new CaseEventDeserializer());
				Gson gson = builder.create();
				PostCaseCreateHook postCaseCreateHook = gson.fromJson(resultSet.getString("post_case_create_hook"),
						new TypeToken<PostCaseCreateHook>() {
						}.getType());

				casesDefinitions
						.add(CaseDefinition.builder().id(id).name(name).postCaseCreateHook(postCaseCreateHook).build());
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
			ResultSet resultSet = statement.executeQuery(
					"SELECT id, name, post_case_create_hook FROM case_definition where id = '" + caseDefId + "';");
			while (resultSet.next()) {
				String id = resultSet.getString("id");
				String name = resultSet.getString("name");

				// Adding custom deserializers
				final GsonBuilder builder = new GsonBuilder();
				builder.registerTypeAdapter(CaseEvent.class, new CaseEventDeserializer());
				Gson gson = builder.create();
				PostCaseCreateHook postCaseCreateHook = gson.fromJson(resultSet.getString("post_case_create_hook"),
						new TypeToken<PostCaseCreateHook>() {
						}.getType());

				return CaseDefinition.builder().id(id).name(name).postCaseCreateHook(postCaseCreateHook).build();
			}
		}

		throw new CaseDefinitionNotFoundException();
	}

	@Override
	public void saveCaseDefinition(CaseDefinition caseDefinition) throws Exception {

		Gson gson = new Gson();
		String postCaseCreateHook = gson.toJson(caseDefinition.getPostCaseCreateHook());

		try (var statement = connection.createStatement();) {

			statement.executeUpdate("INSERT INTO case_definition (post_case_create_hook, id, name ) VALUES ("

					+ "\'" + postCaseCreateHook + "\'" + ", "

					+ "\'" + caseDefinition.getId() + "\'" + ", "

					+ "\'" + caseDefinition.getName() + "\'"

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

	@Override
	public Form getForm(String formKey) throws Exception {

		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement.executeQuery(
					"SELECT form_key, components, description, FROM form where form_key = '" + formKey + "';");

			while (resultSet.next()) {
				String key = resultSet.getString("form_key");
				String description = resultSet.getString("description");

				Gson gson = new Gson();
				JsonObject components = gson.fromJson(resultSet.getString("components"), new TypeToken<JsonObject>() {
				}.getType());

				return Form.builder().key(key).description(description).structure(components).build();
			}

		}

		throw new FormNotFoundException();
	}

	@Override
	public void saveForm(Form form) throws Exception {
		Gson gson = new Gson();
		String componentsJSONString = gson.toJson(form.getStructure());

		try (var statement = connection.createStatement();) {

			statement.executeUpdate("INSERT INTO form (form_key, description, components) VALUES ("

					+ "\'" + form.getKey() + "\'" + ", "

					+ "\'" + form.getDescription() + "\'" + ", "

					+ "\'" + componentsJSONString + "\'"

					+ ");");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

	@Override
	public List<Form> findForms() throws Exception {

		List<Form> forms = new ArrayList<>();

		try (var statement = connection.createStatement();) {

			ResultSet resultSet = statement.executeQuery("SELECT form_key, components, description FROM form;");

			while (resultSet.next()) {
				String key = resultSet.getString("form_key");
				String description = resultSet.getString("description");

				Gson gson = new Gson();
				JsonObject components = gson.fromJson(resultSet.getString("components"), new TypeToken<JsonObject>() {
				}.getType());

				forms.add(Form.builder().key(key).description(description).structure(components).build());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new Exception(e);
		}

		return forms;
	}

	@Override
	public void deleteForm(String formKey) throws Exception {
		try (var statement = connection.createStatement();) {

			statement.executeUpdate("DELETE form WHERE form_key = "

					+ "'" + formKey + "'" + ";");

		} catch (SQLException ex) {
			// TODO error handling
			throw new Exception(ex);
		}
	}

}
