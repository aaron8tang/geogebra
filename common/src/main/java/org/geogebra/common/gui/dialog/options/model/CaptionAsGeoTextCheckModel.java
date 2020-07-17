package org.geogebra.common.gui.dialog.options.model;

import org.geogebra.common.kernel.geos.CaptionAsGeoText;
import org.geogebra.common.main.App;

public class CaptionAsGeoTextCheckModel extends BooleanOptionModel {

	public CaptionAsGeoTextCheckModel(IBooleanOptionListener listener, App app) {
		super(listener, app);
	}

	private CaptionAsGeoText at(int index) {
		return (CaptionAsGeoText) getObjectAt(index);
	}

	@Override
	public boolean getValueAt(int index) {
		return at(index).isGeoTextAsCaptionEnabled();
	}

	@Override
	public boolean isValidAt(int index) {
		return getObjectAt(index) instanceof CaptionAsGeoText;
	}

	@Override
	public void apply(int index, boolean value) {
		CaptionAsGeoText asGeoText = at(index);
		asGeoText.setGeoTextAsCaptionEnabled(value);
		asGeoText.updateRepaint();
	}
}
