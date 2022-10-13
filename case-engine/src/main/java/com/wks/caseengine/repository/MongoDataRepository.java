package com.wks.caseengine.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.form.Form;

/**
 * @author victor.franca
 *
 */
@Component
public class MongoDataRepository implements DataRepository {

	private MongoClient mongoClient;
	private MongoDatabase database;

	private MongoCollection<JsonObject> caseDefCollection;
	private MongoCollection<JsonObject> caseInstCollection;
	private MongoCollection<JsonObject> formCollection;

	public MongoDataRepository(final DataBaseConfig databaseConfig) throws Exception {
		CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder()
						// https://jira.mongodb.org/browse/JAVA-2750
						.conventions(Arrays.asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build()));

		MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build();
		mongoClient = MongoClients.create(settings);
		database = mongoClient.getDatabase(databaseConfig.getMongoDatabase());

		caseDefCollection = database.getCollection("caseDefinitions", JsonObject.class);
		caseInstCollection = database.getCollection("caseInstances", JsonObject.class);
		formCollection = database.getCollection("forms", JsonObject.class);

	}

	@Override
	public List<CaseDefinition> findCaseDefintions() throws Exception {
		Gson gson = new Gson();
		return caseDefCollection.find().map(o -> gson.fromJson(o.getJson(), CaseDefinition.class))
				.into(new ArrayList<>());
	}

	@Override
	public CaseDefinition getCaseDefinition(final String caseDefId) throws Exception {
		Bson filter = Filters.eq("id", caseDefId);
		Gson gson = new Gson();
		return gson.fromJson(caseDefCollection.find(filter).first().getJson(), CaseDefinition.class);
	}

	@Override
	public void saveCaseDefinition(final CaseDefinition caseDefinition) throws Exception {
		caseDefCollection.insertOne((new JsonObject(new Gson().toJson(caseDefinition))));
	}

	@Override
	public void updateCaseDefinition(final String caseDefId, final CaseDefinition caseDefinition) throws Exception {
		Bson filter = Filters.eq("id", caseDefId);

		Bson update = Updates.combine(Updates.set("stages", caseDefinition.getStages()),
				Updates.set("formKey", caseDefinition.getFormKey()), Updates.set("name", caseDefinition.getName()),
				Updates.set("stagesLifecycleProcessKey", caseDefinition.getStagesLifecycleProcessKey()));

		caseDefCollection.updateOne(filter, update);
	}

	@Override
	public void deleteCaseDefinition(final String caseDefinitionId) throws Exception {
		Bson filter = Filters.eq("id", caseDefinitionId);
		caseDefCollection.deleteMany(filter);
	}

	@Override
	public List<CaseInstance> findCaseInstances(final Optional<CaseStatus> status) throws Exception {
		Gson gson = new Gson();
		Bson filter = status.isPresent() ? Filters.eq("status", status.get()) : Filters.empty();
		return caseInstCollection.find().filter(filter).map(o -> gson.fromJson(o.getJson(), CaseInstance.class))
				.into(new ArrayList<>());
	}

	@Override
	public CaseInstance getCaseInstance(final String businessKey) throws Exception {
		Bson filter = Filters.eq("businessKey", businessKey);
		Gson gson = new Gson();
		return gson.fromJson(caseInstCollection.find(filter).first().getJson(), CaseInstance.class);
	}

	@Override
	public void saveCaseInstance(final CaseInstance caseInstance) throws Exception {
		caseInstCollection.insertOne((new JsonObject(new Gson().toJson(caseInstance))));
	}

	@Override
	public void updateCaseStatus(final String businessKey, final CaseStatus newStatus) throws Exception {
		Bson filter = Filters.eq("businessKey", businessKey);
		Bson update = Updates.set("status", newStatus);
		caseInstCollection.updateMany(filter, update);
	}

	@Override
	public void updateCaseStage(final String businessKey, final String caseStage) {
		Bson filter = Filters.eq("businessKey", businessKey);
		Bson update = Updates.set("stage", caseStage);
		caseInstCollection.updateMany(filter, update);
	}

	@Override
	public void deleteCaseInstance(final CaseInstance caseInstance) throws Exception {
		Bson filter = Filters.eq("businessKey", caseInstance.getBusinessKey());
		caseInstCollection.deleteMany(filter);
	}

	@Override
	public Form getForm(final String formKey) throws Exception {
		Bson filter = Filters.eq("key", formKey);
		Gson gson = new Gson();
		return gson.fromJson(formCollection.find(filter).first().getJson(), Form.class);
	}

	@Override
	public void saveForm(final Form form) throws Exception {
		formCollection.insertOne((new JsonObject(new Gson().toJson(form))));

	}

	@Override
	public List<Form> findForms() throws Exception {
		Gson gson = new Gson();
		return formCollection.find().map(o -> gson.fromJson(o.getJson(), Form.class)).into(new ArrayList<>());
	}

	@Override
	public void deleteForm(final String formKey) throws Exception {
		Bson filter = Filters.eq("key", formKey);
		formCollection.deleteMany(filter);
	}

	@Override
	public void updateForm(final String formKey, final Form form) throws Exception {
		Bson filter = Filters.eq("key", formKey);

		Bson update = Updates.combine(Updates.set("title", form.getTitle()), Updates.set("toolTip", form.getToolTip()),
				Updates.set("structure", (new JsonObject(new Gson().toJson(form.getStructure())))));

		formCollection.updateOne(filter, update);
	}

}
