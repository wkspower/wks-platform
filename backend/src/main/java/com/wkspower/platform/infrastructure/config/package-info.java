/**
 * Spring {@code @Configuration} classes — DataSource, web MVC, CORS, etc. — plus the case-type YAML
 * loading and validation pipeline ({@link
 * com.wkspower.platform.infrastructure.config.CaseTypeYamlLoader}, {@link
 * com.wkspower.platform.infrastructure.config.ConfigValidator}) and Story 4.2's Mapping Layer
 * deploy-time validator ({@link com.wkspower.platform.infrastructure.config.MappingValidator}).
 *
 * <p>Story 4.2 / D22 — {@code MappingValidator} runs after stage validation as a single pipeline
 * step inside {@code ConfigValidator}; there is no second invocation site. The validator is
 * I/O-free; callers (startup loader, future admin deploy controller) supply BPMN bytes as a
 * caller-controlled {@code Map<String, byte[]>}.
 */
package com.wkspower.platform.infrastructure.config;
