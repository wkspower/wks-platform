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
package com.wks.caseengine.cmmn.form;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.caseengine.cmmn.Slug;
import com.wks.caseengine.config.schema.ConfigSchemaVersion;
import com.wks.caseengine.form.Form;

/**
 * Generates the minimal forms an imported case needs to be usable.
 *
 * <p>CMMN describes <em>what happens</em>, not <em>what is captured</em> — it has no
 * notion of a form. So an import that generated none would produce a case with
 * nothing to fill in and tasks that open with only a Complete button, which reads as
 * broken even though the process is running correctly.
 *
 * <p>What is generated is deliberately thin: a few general-purpose fields, clearly
 * labelled as a starting point. It is scaffolding to be replaced in the Case Builder,
 * not a guess at the customer's data model — inventing plausible-looking domain fields
 * would be worse than offering none, because it invites people to trust them.
 *
 * <p>Labels are German, matching the language such a model is authored in. They are
 * case configuration, so they are shown as authored and are not translated.
 *
 * @author victor.franca
 */
@Component
public class CmmnFormGenerator {

	static final String CASE_FORM_SUFFIX = "-fall";

	static final String TASK_FORM_SUFFIX = "-aufgabe";

	/** The form the case itself renders: the few facts every case needs. */
	public Form generateCaseForm(final String caseDefinitionId, final String caseName) {
		JsonArray components = new JsonArray();
		components.add(textField("aktenzeichen", "Aktenzeichen", true));
		components.add(textField("name", "Name", false));
		components.add(textArea("notiz", "Notiz"));

		return form(caseFormKey(caseDefinitionId), caseName,
				"Automatisch beim CMMN-Import erzeugt — im Case Builder anpassen.", components);
	}

	/**
	 * One shared form behind every generated task.
	 *
	 * <p>Shared rather than per-task on purpose: a distinct empty form for each task
	 * would be a dozen artifacts to clean up with nothing to distinguish them. One
	 * obviously-generic form makes the placeholder nature plain.
	 */
	public Form generateTaskForm(final String caseDefinitionId, final String caseName) {
		JsonArray components = new JsonArray();
		components.add(textArea("bearbeitungsnotiz", "Bearbeitungsnotiz"));

		return form(taskFormKey(caseDefinitionId), caseName + " — Aufgabe",
				"Platzhalter für importierte Aufgaben — im Case Builder anpassen.", components);
	}

	public String caseFormKey(final String caseDefinitionId) {
		return Slug.of(caseDefinitionId) + CASE_FORM_SUFFIX;
	}

	public String taskFormKey(final String caseDefinitionId) {
		return Slug.of(caseDefinitionId) + TASK_FORM_SUFFIX;
	}

	private Form form(final String key, final String title, final String toolTip, final JsonArray components) {
		JsonObject structure = new JsonObject();
		structure.addProperty("display", "form");
		structure.add("components", components);

		return Form.builder().key(key).title(title).toolTip(toolTip).structure(structure)
				.schemaVersion(ConfigSchemaVersion.CURRENT).build();
	}

	private JsonObject textField(final String key, final String label, final boolean required) {
		JsonObject component = component("textfield", key, label);
		if (required) {
			JsonObject validate = new JsonObject();
			validate.addProperty("required", true);
			component.add("validate", validate);
		}
		return component;
	}

	private JsonObject textArea(final String key, final String label) {
		JsonObject component = component("textarea", key, label);
		component.addProperty("rows", 3);
		return component;
	}

	private JsonObject component(final String type, final String key, final String label) {
		JsonObject component = new JsonObject();
		component.addProperty("type", type);
		component.addProperty("key", key);
		component.addProperty("label", label);
		component.addProperty("input", true);
		component.addProperty("tableView", true);
		return component;
	}

}
