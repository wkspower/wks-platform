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
package com.wks.caseengine.cases.definition;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * An uploaded diagram ends up inside a page, so what gets stored has to be inert.
 * These pin what is refused at the door.
 */
class SourceDiagramValidatorTest {

	@Test
	void shouldAcceptARealExportedDiagram() throws Exception {
		try (InputStream in = getClass().getResourceAsStream("/cmmn/asyl-verfahren.source-diagram.svg")) {
			String svg = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			assertDoesNotThrow(() -> SourceDiagramValidator.validate(svg));
		}
	}

	@Test
	void shouldAcceptAMinimalSvg() {
		assertDoesNotThrow(() -> SourceDiagramValidator
				.validate("<svg xmlns=\"http://www.w3.org/2000/svg\"><rect width=\"10\" height=\"10\"/></svg>"));
	}

	@Test
	void shouldRejectEmbeddedScript() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> SourceDiagramValidator.validate("<svg><script>alert(1)</script></svg>"));
		assertTrue(thrown.getMessage().contains("script"), thrown.getMessage());
	}

	@Test
	void shouldRejectInlineEventHandlers() {
		assertThrows(IllegalArgumentException.class,
				() -> SourceDiagramValidator.validate("<svg><rect onload=\"alert(1)\"/></svg>"));
		assertThrows(IllegalArgumentException.class,
				() -> SourceDiagramValidator.validate("<svg><a onclick='steal()'>x</a></svg>"));
	}

	@Test
	void shouldRejectJavascriptUrls() {
		assertThrows(IllegalArgumentException.class,
				() -> SourceDiagramValidator.validate("<svg><a href=\"javascript:alert(1)\">x</a></svg>"));
	}

	/** foreignObject is how arbitrary HTML gets smuggled into an SVG. */
	@Test
	void shouldRejectForeignObjectAndEmbeddedDocuments() {
		for (String hostile : new String[] { "<svg><foreignObject><b>x</b></foreignObject></svg>",
				"<svg><iframe src=\"http://elsewhere\"/></svg>", "<svg><object data=\"x\"/></svg>",
				"<svg><embed src=\"x\"/></svg>" }) {
			assertThrows(IllegalArgumentException.class, () -> SourceDiagramValidator.validate(hostile),
					"should have refused: " + hostile);
		}
	}

	/** Entity declarations are the danger in a DOCTYPE, not the DOCTYPE itself. */
	@Test
	void shouldRejectInternalEntityDeclarations() {
		assertThrows(IllegalArgumentException.class, () -> SourceDiagramValidator
				.validate("<!DOCTYPE svg [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><svg>&x;</svg>"));
	}

	/**
	 * Every real export carries a plain DOCTYPE — cmmn-js and the other bpmn.io tools
	 * all emit one. Refusing it would turn away every diagram this feature is for.
	 */
	@Test
	void shouldAcceptThePlainDoctypeThatRealExportersEmit() {
		assertDoesNotThrow(() -> SourceDiagramValidator.validate(
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" "
						+ "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg><rect/></svg>"));
	}

	/**
	 * A <use> with an internal reference is ordinary SVG and must not be refused —
	 * being over-strict here silently makes legitimate diagrams unusable.
	 */
	@Test
	void shouldAcceptInternalReferences() {
		assertDoesNotThrow(() -> SourceDiagramValidator
				.validate("<svg><defs><rect id=\"a\"/></defs><use xlink:href=\"#a\"/></svg>"));
	}

	@Test
	void shouldRejectSomethingThatIsNotAnSvgAtAll() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> SourceDiagramValidator.validate("<cmmn:definitions/>"));
		assertTrue(thrown.getMessage().contains("not an SVG"), thrown.getMessage());
	}

	@Test
	void shouldRejectEmptyContent() {
		assertThrows(IllegalArgumentException.class, () -> SourceDiagramValidator.validate(null));
		assertThrows(IllegalArgumentException.class, () -> SourceDiagramValidator.validate("   "));
	}

	@Test
	void shouldRejectSomethingTooLargeToBeADiagram() {
		String huge = "<svg>" + "x".repeat(SourceDiagramValidator.MAX_LENGTH) + "</svg>";
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> SourceDiagramValidator.validate(huge));
		assertTrue(thrown.getMessage().contains("larger than"), thrown.getMessage());
	}

}
