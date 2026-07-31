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
package com.wks.caseengine.cmmn.bpmn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Assembles the flow elements of a BPMN process.
 *
 * <p>Nodes are declared with their connections and the XML is rendered at the end,
 * so a node's {@code <bpmn:incoming>} / {@code <bpmn:outgoing>} children are derived
 * from the sequence flows rather than written by hand alongside them. Emitting those
 * children matters: tooling built on Camunda's BPMN model API — which includes the
 * engine's own tests and any diagram viewer — reads a node's connections from them,
 * not from the flows' {@code sourceRef}/{@code targetRef}. A process missing them
 * parses, but reads as a graph of disconnected nodes.
 *
 * @author victor.franca
 */
class BpmnProcessBuilder {

	/** A flow element: its opening tag's attributes and any nested content. */
	private record Node(String id, String tag, String attributes, String innerXml) {
	}

	private record Flow(String id, String source, String target) {
	}

	private final List<Node> nodes = new ArrayList<>();

	private final List<Flow> flows = new ArrayList<>();

	void node(final String id, final String tag, final String attributes) {
		nodes.add(new Node(id, tag, attributes, null));
	}

	void node(final String id, final String tag, final String attributes, final String innerXml) {
		nodes.add(new Node(id, tag, attributes, innerXml));
	}

	void connect(final String source, final String target) {
		flows.add(new Flow("Flow_" + source + "_" + target, source, target));
	}

	/**
	 * Renders the diagram interchange for the process.
	 *
	 * <p>Execution does not need this — Camunda runs a model with no diagram at all —
	 * but every tool that <em>shows</em> a process reads it, so without it the modeler
	 * and the case's process viewer open on an empty canvas. A generated process
	 * nobody can look at is not much use for reviewing what an import produced.
	 *
	 * <p>The layout is a simple layered one: nodes are placed in columns by their
	 * distance from the start event and stacked vertically within a column, which is
	 * exactly the shape these generated processes have (a fan-out, a join, then a
	 * chain). Edges are straight lines between the facing edges of the two shapes.
	 */
	String renderDiagram(final String processId, final int indentDepth) {
		Map<String, Integer> depth = depths();
		Map<Integer, List<Node>> columns = new LinkedHashMap<>();
		for (Node node : nodes) {
			columns.computeIfAbsent(depth.getOrDefault(node.id(), 0), key -> new ArrayList<>()).add(node);
		}

		Map<String, Bounds> layout = new LinkedHashMap<>();
		int x = COLUMN_START_X;

		for (Integer column : columns.keySet().stream().sorted().toList()) {
			List<Node> members = columns.get(column);
			int widest = members.stream().mapToInt(node -> width(node.tag())).max().orElse(TASK_WIDTH);

			for (int row = 0; row < members.size(); row++) {
				Node node = members.get(row);
				int w = width(node.tag());
				int h = height(node.tag());

				// Centre each shape on its lane, and centre the column's rows on the axis.
				int centreY = LANE_CENTRE_Y + (int) ((row - (members.size() - 1) / 2.0) * ROW_SPACING);
				layout.put(node.id(), new Bounds(x + (widest - w) / 2, centreY - h / 2, w, h));
			}
			x += widest + COLUMN_GAP;
		}

		String indent = "  ".repeat(indentDepth);
		String inner = "  ".repeat(indentDepth + 1);
		String deeper = "  ".repeat(indentDepth + 2);

		StringBuilder xml = new StringBuilder();
		xml.append(indent).append("<bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n");
		xml.append(inner).append("<bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"").append(processId)
				.append("\">\n");

		for (Node node : nodes) {
			Bounds b = layout.get(node.id());
			xml.append(deeper).append("<bpmndi:BPMNShape id=\"Shape_").append(node.id())
					.append("\" bpmnElement=\"").append(node.id()).append("\">\n");
			xml.append(deeper).append("  <dc:Bounds x=\"").append(b.x()).append("\" y=\"").append(b.y())
					.append("\" width=\"").append(b.w()).append("\" height=\"").append(b.h()).append("\" />\n");
			xml.append(deeper).append("</bpmndi:BPMNShape>\n");
		}

		for (Flow flow : flows) {
			Bounds from = layout.get(flow.source());
			Bounds to = layout.get(flow.target());
			if (from == null || to == null) {
				continue;
			}

			xml.append(deeper).append("<bpmndi:BPMNEdge id=\"Edge_").append(flow.id())
					.append("\" bpmnElement=\"").append(flow.id()).append("\">\n");
			xml.append(deeper).append("  <di:waypoint x=\"").append(from.x() + from.w()).append("\" y=\"")
					.append(from.y() + from.h() / 2).append("\" />\n");
			xml.append(deeper).append("  <di:waypoint x=\"").append(to.x()).append("\" y=\"")
					.append(to.y() + to.h() / 2).append("\" />\n");
			xml.append(deeper).append("</bpmndi:BPMNEdge>\n");
		}

		xml.append(inner).append("</bpmndi:BPMNPlane>\n");
		xml.append(indent).append("</bpmndi:BPMNDiagram>\n");
		return xml.toString();
	}

	/**
	 * Distance of each node from the start event, following the flows. The longest
	 * path wins, so a join sits to the right of every branch that feeds it rather
	 * than overlapping the first one.
	 */
	private Map<String, Integer> depths() {
		Map<String, List<String>> outgoing = new LinkedHashMap<>();
		for (Flow flow : flows) {
			outgoing.computeIfAbsent(flow.source(), key -> new ArrayList<>()).add(flow.target());
		}

		Map<String, Integer> depth = new LinkedHashMap<>();
		nodes.stream().findFirst().ifPresent(first -> depth.put(first.id(), 0));

		// Relax repeatedly rather than recurse: the graphs are tiny, and this cannot
		// run away on an unexpected cycle the way a naive walk would.
		for (int pass = 0; pass < nodes.size(); pass++) {
			boolean changed = false;
			for (Flow flow : flows) {
				Integer source = depth.get(flow.source());
				if (source == null) {
					continue;
				}
				if (depth.getOrDefault(flow.target(), -1) < source + 1) {
					depth.put(flow.target(), source + 1);
					changed = true;
				}
			}
			if (!changed) {
				break;
			}
		}
		return depth;
	}

	private int width(final String tag) {
		return switch (tag) {
		case "startEvent", "endEvent" -> EVENT_SIZE;
		case "parallelGateway", "exclusiveGateway" -> GATEWAY_SIZE;
		default -> TASK_WIDTH;
		};
	}

	private int height(final String tag) {
		return switch (tag) {
		case "startEvent", "endEvent" -> EVENT_SIZE;
		case "parallelGateway", "exclusiveGateway" -> GATEWAY_SIZE;
		default -> TASK_HEIGHT;
		};
	}

	private record Bounds(int x, int y, int w, int h) {
	}

	private static final int EVENT_SIZE = 36;

	private static final int GATEWAY_SIZE = 50;

	private static final int TASK_WIDTH = 100;

	private static final int TASK_HEIGHT = 80;

	private static final int COLUMN_START_X = 160;

	private static final int COLUMN_GAP = 60;

	private static final int ROW_SPACING = 110;

	private static final int LANE_CENTRE_Y = 200;

	String render(final int indentDepth) {
		Map<String, List<String>> incoming = new LinkedHashMap<>();
		Map<String, List<String>> outgoing = new LinkedHashMap<>();

		for (Flow flow : flows) {
			outgoing.computeIfAbsent(flow.source(), key -> new ArrayList<>()).add(flow.id());
			incoming.computeIfAbsent(flow.target(), key -> new ArrayList<>()).add(flow.id());
		}

		String indent = "  ".repeat(indentDepth);
		String childIndent = "  ".repeat(indentDepth + 1);
		StringBuilder xml = new StringBuilder();

		for (Node node : nodes) {
			List<String> in = incoming.getOrDefault(node.id(), List.of());
			List<String> out = outgoing.getOrDefault(node.id(), List.of());

			xml.append(indent).append("<bpmn:").append(node.tag()).append(" id=\"").append(node.id()).append("\"")
					.append(node.attributes()).append(">\n");

			if (node.innerXml() != null) {
				xml.append(node.innerXml());
			}
			in.forEach(flow -> xml.append(childIndent).append("<bpmn:incoming>").append(flow)
					.append("</bpmn:incoming>\n"));
			out.forEach(flow -> xml.append(childIndent).append("<bpmn:outgoing>").append(flow)
					.append("</bpmn:outgoing>\n"));

			xml.append(indent).append("</bpmn:").append(node.tag()).append(">\n");
		}

		for (Flow flow : flows) {
			xml.append(indent).append("<bpmn:sequenceFlow id=\"").append(flow.id()).append("\" sourceRef=\"")
					.append(flow.source()).append("\" targetRef=\"").append(flow.target()).append("\" />\n");
		}

		return xml.toString();
	}

}
