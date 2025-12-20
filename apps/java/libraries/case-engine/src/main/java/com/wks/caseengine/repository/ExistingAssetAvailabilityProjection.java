package com.wks.caseengine.repository;

import java.util.UUID;

/**
 * Projection for existing AssetAvailability rows returned by native queries.
 */
public interface ExistingAssetAvailabilityProjection {

    UUID getAssetId();

    UUID getFymId();

    Integer getPriority();
}
