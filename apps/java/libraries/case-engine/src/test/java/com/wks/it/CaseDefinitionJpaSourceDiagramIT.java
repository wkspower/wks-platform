/*
 * WKS Platform - Open-Source Project
 *
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 *
 * WKS Platform is licensed under the MIT License.
 *
 * © 2021 WKS Power. All rights reserved.
 *
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.repository.CaseDefinitionRepository;

/**
 * The source diagram must survive an update on the JPA (PostgreSQL) backend.
 *
 * Attaching a diagram is a read-modify-write through {@code update}
 * ({@code CaseDefinitionServiceImpl.attachSourceDiagram}). The JPA update copied
 * the fields one by one and left {@code sourceDiagram} out, so the attach
 * answered 204 while nothing was stored — on every PostgreSQL deployment. This
 * runs the real JPA repository against a database, not a mock, so the field list
 * is what is under test.
 */
@SpringBootTest(classes = JpaDataExportIT.TestApp.class, properties = {
		"database.type=jpa",
		"spring.datasource.jdbcUrl=jdbc:h2:mem:wks_source_diagram_it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create",
		"spring.autoconfigure.exclude="
				+ "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration,"
				+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
				+ "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration" })
public class CaseDefinitionJpaSourceDiagramIT {

	private static final String SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"10\" height=\"10\"/></svg>";

	@Autowired
	private CaseDefinitionRepository caseDefinitionRepository;

	@Test
	public void shouldStoreADiagramAttachedThroughUpdate() throws Exception {
		caseDefinitionRepository.save(CaseDefinition.builder().id("cd-attach").name("Attach").formKey("f").build());

		// What attachSourceDiagram does: read, set the diagram, update.
		CaseDefinition loaded = caseDefinitionRepository.get("cd-attach");
		loaded.setSourceDiagram(SVG);
		caseDefinitionRepository.update("cd-attach", loaded);

		assertEquals(SVG, caseDefinitionRepository.get("cd-attach").getSourceDiagram());
	}

	@Test
	public void shouldKeepTheDiagramWhenTheCaseTypeIsEditedAndSavedBack() throws Exception {
		caseDefinitionRepository
				.save(CaseDefinition.builder().id("cd-edit").name("Before").formKey("f").sourceDiagram(SVG).build());

		// What the case type editor does: send back the object it loaded, with an edit.
		CaseDefinition loaded = caseDefinitionRepository.get("cd-edit");
		loaded.setName("After");
		caseDefinitionRepository.update("cd-edit", loaded);

		CaseDefinition reloaded = caseDefinitionRepository.get("cd-edit");
		assertEquals("After", reloaded.getName());
		assertEquals(SVG, reloaded.getSourceDiagram());
	}
}
