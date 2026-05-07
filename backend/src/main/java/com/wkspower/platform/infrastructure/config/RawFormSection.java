package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Transport-shaped record for one section entry in a CaseType YAML {@code forms[].sections[]}
 * block. Used by {@code dataModel: sectioned} forms.
 *
 * <p>Story 5.3 — schema extension; no runtime or persistence concern.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawFormSection(
    @JsonProperty("id") String id,
    @JsonProperty("label") String label,
    @JsonProperty("fields") List<Map<String, Object>> fields) {}
