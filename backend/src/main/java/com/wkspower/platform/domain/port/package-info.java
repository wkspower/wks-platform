/**
 * Outbound ports — interfaces implemented by {@code infrastructure.*} adapters.
 *
 * <ul>
 *   <li>{@link com.wkspower.platform.domain.port.WorkflowEngine} — embedded BPMN engine port
 *       (Stories 2.2 / 2.3 / 2.4).
 *   <li>{@link com.wkspower.platform.domain.port.WorkflowAdapter} — generic execution-backend port
 *       per architecture Decision 22 (BPMN Attachment &amp; Mapping Layer); BPMN today via Story
 *       4.4, state-machine / Temporal / AI tomorrow. Story 4.1 introduces this port as
 *       interface-only scaffolding.
 * </ul>
 */
package com.wkspower.platform.domain.port;
