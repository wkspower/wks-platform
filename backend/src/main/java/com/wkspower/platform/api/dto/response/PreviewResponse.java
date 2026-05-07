package com.wkspower.platform.api.dto.response;

/**
 * Preview endpoint response (Story 14.2 AC3).
 *
 * <ul>
 *   <li>{@code previewable=true}: the frontend should render inline ({@code <iframe>} for PDF,
 *       {@code <img>} for images). The {@code url} is a presigned MinIO URL (60-second TTL; P7) or
 *       the platform's download endpoint ({@code /api/documents/{id}/download?inline=true}) when
 *       using the local store.
 *   <li>{@code previewable=false}: the document type cannot be rendered inline. The frontend should
 *       show a Download button pointing at {@code url}.
 * </ul>
 *
 * <p>P7 — When {@code url} is a presigned MinIO URL it grants unauthenticated object access for 60
 * seconds. Callers must NOT cache or forward the URL beyond that window; treat it as ephemeral.
 */
public record PreviewResponse(boolean previewable, String url) {}
