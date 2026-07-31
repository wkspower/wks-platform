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
package com.wks.caseengine.cmmn.parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.wks.caseengine.cmmn.model.CmmnBounds;
import com.wks.caseengine.cmmn.model.CmmnCriterion;
import com.wks.caseengine.cmmn.model.CmmnDiagram;
import com.wks.caseengine.cmmn.model.CmmnElement;
import com.wks.caseengine.cmmn.model.CmmnElementType;
import com.wks.caseengine.cmmn.model.CmmnSentry;
import com.wks.caseengine.cmmn.model.CmmnSentry.CmmnOnPart;

import lombok.extern.slf4j.Slf4j;

/**
 * Reads a CMMN 1.1 XML model into the importer's {@link CmmnDiagram}.
 *
 * <p>Two structural facts about CMMN shape this reader. First, an item's <em>kind
 * and name</em> live on a definition element while its <em>identity in the
 * diagram</em> lives on the plan item that references it, so both have to be read
 * and joined. Second, geometry lives in a separate diagram-interchange section
 * keyed by element id — and geometry is load-bearing for us, because CMMN plan
 * items carry no ordering of their own.
 *
 * <p>The XML is untrusted (a user uploads it), so the parser is configured exactly
 * as the BPMN deployment path already does: no DTDs and no external entities.
 *
 * @author victor.franca
 */
@Slf4j
@Component
public class CmmnXmlReader {

	private static final String CMMN_NS = "http://www.omg.org/spec/CMMN/20151109/MODEL";

	private static final String CMMNDI_NS = "http://www.omg.org/spec/CMMN/20151109/CMMNDI";

	private static final String DC_NS = "http://www.omg.org/spec/CMMN/20151109/DC";

	/**
	 * CMMN definition element names that become platform concepts, mapped to the
	 * type they become. Everything else recognised as a definition is reported as
	 * unsupported rather than skipped.
	 */
	private static final Map<String, CmmnElementType> DEFINITION_TYPES = Map.of(
			"humanTask", CmmnElementType.HUMAN_TASK,
			"stage", CmmnElementType.STAGE,
			"milestone", CmmnElementType.MILESTONE);

	/**
	 * Definition elements we can name but not represent. Listed explicitly so the
	 * mapper can warn about them by kind — a case file item and a decision task are
	 * dropped for quite different reasons, and the user deserves to know which.
	 */
	private static final List<String> KNOWN_UNSUPPORTED = List.of("caseTask", "processTask", "decisionTask", "task",
			"caseFileItem", "userEventListener", "timerEventListener");

	public CmmnDiagram read(final String cmmnXml) {
		if (cmmnXml == null || cmmnXml.isBlank()) {
			throw new CmmnParseException("No CMMN content was supplied.");
		}

		Document document = parse(cmmnXml);

		Element caseElement = firstChild(document.getDocumentElement(), "case");
		if (caseElement == null) {
			throw new CmmnParseException(
					"The document contains no CMMN <case> element. Export the model as CMMN 1.1 XML "
							+ "(a rendered diagram such as an SVG or PNG cannot be imported).");
		}

		Element casePlanModel = firstChild(caseElement, "casePlanModel");
		if (casePlanModel == null) {
			throw new CmmnParseException("The CMMN case contains no <casePlanModel>, so there is nothing to import.");
		}

		Map<String, Element> definitions = definitionsIn(casePlanModel);
		Map<String, CmmnBounds> bounds = boundsFrom(document);

		List<CmmnElement> elements = new ArrayList<>();
		// The case plan model's own children, then each stage's — parent is implied
		// by which container the plan item was found in, which is more reliable than
		// inferring nesting from geometry.
		collectPlanItems(casePlanModel, null, definitions, bounds, elements);

		for (Map.Entry<String, Element> definition : definitions.entrySet()) {
			if ("stage".equals(localName(definition.getValue()))) {
				String stagePlanItemId = planItemIdFor(definition.getKey(), elements);
				if (stagePlanItemId != null) {
					collectPlanItems(definition.getValue(), stagePlanItemId, definitions, bounds, elements);
				}
			}
		}

		CmmnDiagram diagram = CmmnDiagram.builder().caseId(attribute(caseElement, "id"))
				.caseName(caseName(caseElement, casePlanModel)).casePlanModelId(attribute(casePlanModel, "id"))
				.elements(elements).sentries(sentriesIn(casePlanModel, definitions))
				.annotations(annotationsIn(caseElement)).annotationByCriterion(associationsIn(caseElement)).build();

		log.debug("Read CMMN model {}: {} elements, {} sentries", diagram.getCaseId(), elements.size(),
				diagram.getSentries().size());

		return diagram;
	}

	private Document parse(final String cmmnXml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			// Untrusted XML: no external entities, no DTDs.
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);

			return factory.newDocumentBuilder()
					.parse(new ByteArrayInputStream(cmmnXml.getBytes(StandardCharsets.UTF_8)));

		} catch (ParserConfigurationException | SAXException | IOException e) {
			// The likeliest mistake by far is uploading the picture rather than the
			// model — and a rendered SVG carries a DOCTYPE, so the hardened parser
			// rejects it with a message about entity features that tells the user
			// nothing. Diagnose that case before surfacing the parser's own words.
			if (!looksLikeCmmn(cmmnXml)) {
				throw new CmmnParseException(notACmmnModelMessage(cmmnXml), e);
			}
			throw new CmmnParseException("The supplied file is not readable as CMMN XML: " + e.getMessage(), e);
		}
	}

	private boolean looksLikeCmmn(final String content) {
		return content.contains(CMMN_NS);
	}

	private String notACmmnModelMessage(final String content) {
		String what = content.contains("<svg") || content.contains("http://www.w3.org/2000/svg")
				? "That file is a rendered diagram (SVG), not a model — the shapes are pictures, "
						+ "so the case structure cannot be recovered from it. "
				: "";

		return what + "Export the model as CMMN 1.1 XML (a .cmmn file) and import that.";
	}

	/** Definition elements (tasks, stages, milestones, …) by their id. */
	private Map<String, Element> definitionsIn(final Element container) {
		Map<String, Element> definitions = new LinkedHashMap<>();
		collectDefinitions(container, definitions);
		return definitions;
	}

	private void collectDefinitions(final Element container, final Map<String, Element> definitions) {
		for (Element child : childElements(container)) {
			String name = localName(child);
			String id = attribute(child, "id");

			if (id != null && (DEFINITION_TYPES.containsKey(name) || KNOWN_UNSUPPORTED.contains(name))) {
				definitions.put(id, child);
			}

			// Stages nest their own definitions.
			if ("stage".equals(name)) {
				collectDefinitions(child, definitions);
			}
		}
	}

	/**
	 * Reads the plan items directly inside {@code container}, including the
	 * discretionary items held in its planning table.
	 */
	private void collectPlanItems(final Element container, final String parentId,
			final Map<String, Element> definitions, final Map<String, CmmnBounds> bounds,
			final List<CmmnElement> collected) {

		for (Element child : childElements(container)) {
			String name = localName(child);

			if ("planItem".equals(name)) {
				collected.add(toElement(child, parentId, definitions, bounds, false));
			} else if ("planningTable".equals(name)) {
				for (Element discretionary : childElements(child)) {
					if ("discretionaryItem".equals(localName(discretionary))) {
						collected.add(toElement(discretionary, parentId, definitions, bounds, true));
					}
				}
			}
		}
	}

	private CmmnElement toElement(final Element planItem, final String parentId,
			final Map<String, Element> definitions, final Map<String, CmmnBounds> bounds, final boolean discretionary) {

		String id = attribute(planItem, "id");
		String definitionRef = attribute(planItem, "definitionRef");
		Element definition = definitions.get(definitionRef);

		CmmnElementType type = CmmnElementType.UNSUPPORTED;
		String unsupportedKind = null;
		String name = null;

		if (definition != null) {
			name = attribute(definition, "name");
			String definitionName = localName(definition);
			type = DEFINITION_TYPES.getOrDefault(definitionName, CmmnElementType.UNSUPPORTED);
			if (CmmnElementType.UNSUPPORTED.equals(type)) {
				unsupportedKind = definitionName;
			}
		} else {
			// A plan item pointing at nothing: keep it, so the mapper reports it
			// rather than the element vanishing between the diagram and the result.
			unsupportedKind = "planItem without a resolvable definitionRef";
		}

		Element itemControl = firstChild(planItem, "itemControl");

		return CmmnElement.builder().id(id).type(type).definitionRef(definitionRef).unsupportedKind(unsupportedKind)
				.name(name).parentId(parentId)
				.bounds(bounds.get(id)).discretionary(discretionary)
				.manualActivation(itemControl != null && firstChild(itemControl, "manualActivationRule") != null)
				.repetition(itemControl != null && firstChild(itemControl, "repetitionRule") != null)
				.entryCriteria(criteriaIn(planItem, "entryCriterion"))
				.exitCriteria(criteriaIn(planItem, "exitCriterion")).build();
	}

	private List<CmmnCriterion> criteriaIn(final Element planItem, final String criterionName) {
		List<CmmnCriterion> criteria = new ArrayList<>();
		for (Element child : childElements(planItem)) {
			if (criterionName.equals(localName(child))) {
				criteria.add(new CmmnCriterion(attribute(child, "id"), attribute(child, "sentryRef")));
			}
		}
		return criteria;
	}

	/** Sentries declared on the case plan model and inside any of its stages. */
	private Map<String, CmmnSentry> sentriesIn(final Element casePlanModel, final Map<String, Element> definitions) {
		Map<String, CmmnSentry> sentries = new LinkedHashMap<>();
		collectSentries(casePlanModel, sentries);
		for (Element definition : definitions.values()) {
			if ("stage".equals(localName(definition))) {
				collectSentries(definition, sentries);
			}
		}
		return sentries;
	}

	private void collectSentries(final Element container, final Map<String, CmmnSentry> sentries) {
		for (Element child : childElements(container)) {
			if (!"sentry".equals(localName(child))) {
				continue;
			}

			List<CmmnOnPart> onParts = new ArrayList<>();
			String ifPart = null;

			for (Element part : childElements(child)) {
				String partName = localName(part);
				if ("planItemOnPart".equals(partName)) {
					Element event = firstChild(part, "standardEvent");
					onParts.add(CmmnOnPart.builder().sourceRef(attribute(part, "sourceRef"))
							.standardEvent(event != null ? textOf(event) : null).build());
				} else if ("ifPart".equals(partName)) {
					Element condition = firstChild(part, "condition");
					ifPart = condition != null ? textOf(condition) : "";
				}
			}

			String id = attribute(child, "id");
			sentries.put(id, CmmnSentry.builder().id(id).onParts(onParts).ifPartCondition(ifPart).build());
		}
	}

	private Map<String, String> annotationsIn(final Element caseElement) {
		Map<String, String> annotations = new LinkedHashMap<>();
		for (Element child : childElements(caseElement)) {
			if ("textAnnotation".equals(localName(child))) {
				Element text = firstChild(child, "text");
				annotations.put(attribute(child, "id"), text != null ? textOf(text) : "");
			}
		}
		return annotations;
	}

	/**
	 * Maps a criterion id to the annotation attached to it.
	 *
	 * <p>Associations are undirected in practice — modellers draw them either way
	 * round — so both ends are checked rather than assuming the annotation is the
	 * target.
	 */
	private Map<String, String> associationsIn(final Element caseElement) {
		Map<String, String> byCriterion = new LinkedHashMap<>();
		Map<String, String> annotations = annotationsIn(caseElement);

		for (Element child : childElements(caseElement)) {
			if (!"association".equals(localName(child))) {
				continue;
			}

			String source = attribute(child, "sourceRef");
			String target = attribute(child, "targetRef");

			if (annotations.containsKey(target)) {
				byCriterion.put(source, target);
			} else if (annotations.containsKey(source)) {
				byCriterion.put(target, source);
			}
		}
		return byCriterion;
	}

	/** Shape bounds by the id of the element the shape represents. */
	private Map<String, CmmnBounds> boundsFrom(final Document document) {
		Map<String, CmmnBounds> bounds = new LinkedHashMap<>();

		NodeList shapes = document.getElementsByTagNameNS(CMMNDI_NS, "CMMNShape");
		for (int i = 0; i < shapes.getLength(); i++) {
			Element shape = (Element) shapes.item(i);
			String ref = attribute(shape, "cmmnElementRef");
			if (ref == null) {
				continue;
			}

			NodeList boundsNodes = shape.getElementsByTagNameNS(DC_NS, "Bounds");
			if (boundsNodes.getLength() == 0) {
				continue;
			}

			Element box = (Element) boundsNodes.item(0);
			try {
				bounds.put(ref, new CmmnBounds(Double.parseDouble(box.getAttribute("x")),
						Double.parseDouble(box.getAttribute("y")), Double.parseDouble(box.getAttribute("width")),
						Double.parseDouble(box.getAttribute("height"))));
			} catch (NumberFormatException e) {
				// Unreadable geometry costs ordering accuracy, not the import.
				log.warn("Ignoring unreadable bounds for CMMN element {}", ref);
			}
		}
		return bounds;
	}

	private String caseName(final Element caseElement, final Element casePlanModel) {
		String name = attribute(caseElement, "name");
		if (name != null && !name.isBlank()) {
			return name;
		}

		name = attribute(casePlanModel, "name");
		if (name != null && !name.isBlank()) {
			return name;
		}

		return attribute(caseElement, "id");
	}

	/**
	 * The id of the plan item that instantiates {@code definitionId}, which is the
	 * id the enclosed items must record as their parent — a stage's children belong
	 * to the stage as it appears in the diagram, not to its reusable definition.
	 */
	private String planItemIdFor(final String definitionId, final List<CmmnElement> elements) {
		return elements.stream().filter(element -> definitionId.equals(element.getDefinitionRef()))
				.map(CmmnElement::getId).findFirst().orElse(null);
	}

	// ---- small DOM helpers, all namespace-aware on the CMMN namespace ----

	private List<Element> childElements(final Element parent) {
		List<Element> children = new ArrayList<>();
		NodeList nodes = parent.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				children.add((Element) node);
			}
		}
		return children;
	}

	private Element firstChild(final Element parent, final String localName) {
		for (Element child : childElements(parent)) {
			if (localName.equals(localName(child))) {
				return child;
			}
		}
		return null;
	}

	private String localName(final Element element) {
		String local = element.getLocalName();
		return local != null ? local : element.getNodeName();
	}

	private String attribute(final Element element, final String name) {
		String value = element.getAttribute(name);
		return value == null || value.isEmpty() ? null : value;
	}

	private String textOf(final Element element) {
		String text = element.getTextContent();
		return text != null ? text.trim() : null;
	}

}
