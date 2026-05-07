package com.wkspower.platform.api.dto.response;

/**
 * Preview endpoint response (Story 14.2 AC3).
 *
 * <ul>
 *   <li>{@code previewable=true}: the frontend should render inline ({@code <iframe>} for PDF,
 *       {@code <img>} for images). The {@code url} is a presigned MinIO URL (5-minute TTL) or the
 *       platform's download endpoint ({@code /api/documents/{id}/download}) when using the local
 *       store.
 *   <li>{@code previewable=false}: the document type cannot be rendered inline. The frontend should
 *       show a Download button pointing at {@code url}.
 * </ul>
 */
public record PreviewResponse(boolean previewable, String url) {}
