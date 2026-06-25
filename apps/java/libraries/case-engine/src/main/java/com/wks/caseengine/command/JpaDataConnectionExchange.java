package com.wks.caseengine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormRepository;
import com.wks.caseengine.queue.Queue;
import com.wks.caseengine.queue.QueueRepository;
import com.wks.caseengine.repository.Repository;

import lombok.extern.slf4j.Slf4j;

/**
 * JPA strategy for the data import seam. Persists the canonical
 * {@code form}/{@code caseDefinition}/{@code queue} collections through the JPA
 * repositories, so the same {@link DataImportService} path that backs the REST
 * data-import endpoint also backs the JPA/H2 backend (e.g. the startup seeder in
 * minimal mode). Selected when {@code database.type=jpa}; the Mongo counterpart
 * is {@link MongoDataConnectionExchange}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "database.type", havingValue = "jpa", matchIfMissing = false)
public class JpaDataConnectionExchange implements DataConnectionExchange {

	@Autowired
	private CaseDefinitionRepository caseDefinitionRepository;

	@Autowired
	private FormRepository formRepository;

	@Autowired
	private QueueRepository queueRepository;

	@Override
	@Transactional(readOnly = true)
	public JsonObject exportFromDatabase(Gson gson) {
		// Symmetric to importToDatabase: export the canonical form/caseDefinition/queue
		// collections under the same keys the import side reads.
		JsonObject exportedData = new JsonObject();
		exportedData.add("form", gson.toJsonTree(formRepository.find()).getAsJsonArray());
		exportedData.add("caseDefinition", gson.toJsonTree(caseDefinitionRepository.find()).getAsJsonArray());
		exportedData.add("queue", gson.toJsonTree(queueRepository.find()).getAsJsonArray());
		return exportedData;
	}

	@Override
	@Transactional
	public void importToDatabase(JsonObject data, Gson gson) {
		// Forms first (case definitions reference a formKey), then definitions, then queues.
		importEach(data, gson, "form", Form.class, formRepository);
		importEach(data, gson, "caseDefinition", CaseDefinition.class, caseDefinitionRepository);
		importEach(data, gson, "queue", Queue.class, queueRepository);
	}

	private <T> void importEach(JsonObject data, Gson gson, String key, Class<T> type, Repository<T> repository) {
		JsonElement element = data.get(key);
		if (element == null || !element.isJsonArray()) {
			return;
		}

		JsonArray array = element.getAsJsonArray();
		for (JsonElement item : array) {
			try {
				repository.save(gson.fromJson(item, type));
			} catch (Exception e) {
				log.warn("Import: failed to insert one {} record: {}", key, e.getMessage());
			}
		}
	}

}
