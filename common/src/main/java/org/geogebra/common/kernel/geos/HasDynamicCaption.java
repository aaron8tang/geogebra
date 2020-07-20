package org.geogebra.common.kernel.geos;

import org.geogebra.common.kernel.kernelND.GeoElementND;

/**
 * Geo caption can be dynamic,
 * ie GeoText if this interface is implemented.
 *
 * @author laszlo
 */
public interface HasDynamicCaption extends GeoElementND {

	/**
	 * Enable/disable dynamic caption.
	 * @param enabled to set.
	 */
	void setDynamicCaptionEnabled(boolean enabled);

	/**
	 *
	 * @return if caption can be dynamic (GeoText).
	 */
	boolean isDynamicCaptionEnabled();

	/**
	 *
	 * @return the GeoText as the dynamic caption
	 */
	GeoText getDynamicCaption();

	/**
	 * Sets GeoText as dynamix caption.
	 * @param caption to set.
	 */
	void setDynamicCaption(GeoText caption);
}
