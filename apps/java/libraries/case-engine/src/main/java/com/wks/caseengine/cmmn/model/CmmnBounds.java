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
package com.wks.caseengine.cmmn.model;

/**
 * A shape's position and size, read from the model's diagram interchange.
 *
 * <p>Geometry is load-bearing here, not decoration: CMMN plan items carry no
 * intrinsic order, so the importer derives stage ordering from where the modeller
 * put things left to right. See the mapper for how bands are formed.
 *
 * @param x      left edge
 * @param y      top edge
 * @param width  shape width
 * @param height shape height
 *
 * @author victor.franca
 */
public record CmmnBounds(double x, double y, double width, double height) {

	/** Right edge. */
	public double right() {
		return x + width;
	}

	/**
	 * Whether this shape's horizontal span overlaps {@code other}'s.
	 *
	 * <p>Touching edges do not count as overlapping, so two shapes laid out
	 * strictly side by side stay in separate bands.
	 */
	public boolean overlapsHorizontally(final CmmnBounds other) {
		return x < other.right() && other.x < right();
	}

	/** Whether this shape fully contains {@code other} — used to infer nesting. */
	public boolean contains(final CmmnBounds other) {
		return x <= other.x && y <= other.y && right() >= other.right()
				&& (y + height) >= (other.y + other.height);
	}

}
