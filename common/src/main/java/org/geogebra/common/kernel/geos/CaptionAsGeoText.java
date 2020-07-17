package org.geogebra.common.kernel.geos;

import org.geogebra.common.kernel.kernelND.GeoElementND;

/**
 * Geo caption can be GeoText if this interface is implemented.
 *
 * @author laszlo
 */
public interface CaptionAsGeoText extends GeoElementND {

	/**
	 * Enable/disable GeoTexts as caption.
	 * @param enabled to set.
	 */
	void setGeoTextAsCaptionEnabled(boolean enabled);

	/**
	 *
	 * @return if GeoTexts can be caption.
	 */
	boolean isGeoTextAsCaptionEnabled();

	/**
	 *
	 * @return the GeoText for caption
	 */
	GeoText getGeoTextAsCaption();
}
