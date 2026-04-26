/**
 * Workflow domain — engine-agnostic records describing deployment requests and results. Pure Java;
 * no Spring, no engine SDK, no Jackson. The {@link
 * com.wkspower.platform.domain.port.WorkflowEngine} port consumes these types; the only adapter
 * lives in {@code engine/} and is the sole place engine-specific imports are allowed.
 * Process-instance concepts (start, complete, transition) belong to later stories and live in
 * {@code domain.model}.
 */
package com.wkspower.platform.domain.workflow;
