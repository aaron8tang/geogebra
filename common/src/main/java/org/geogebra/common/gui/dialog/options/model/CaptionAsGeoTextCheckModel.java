package org.geogebra.common.gui.dialog.options.model;

import org.geogebra.common.kernel.geos.HasDynamicCaption;
import org.geogebra.common.main.App;

public class CaptionAsGeoTextCheckModel extends BooleanOptionModel {

	public CaptionAsGeoTextCheckModel(IBooleanOptionListener listener, App app) {
		super(listener, app);
	}

	private HasDynamicCaption at(int index) {
		return (HasDynamicCaption) getObjectAt(index);
	}

	@Override
	public boolean getValueAt(int index) {
		return at(index).isDynamicCaptionEnabled();
	}

	@Override
	public boolean isValidAt(int index) {
		return getObjectAt(index) instanceof HasDynamicCaption;
	}

	@Override
	public void apply(int index, boolean value) {
		HasDynamicCaption asGeoText = at(index);
		asGeoText.setDynamicCaptionEnabled(value);
		asGeoText.setDynamicCaption(null);
		asGeoText.updateRepaint();
	}
}
