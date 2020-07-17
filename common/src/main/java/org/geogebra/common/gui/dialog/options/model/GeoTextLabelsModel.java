package org.geogebra.common.gui.dialog.options.model;

import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;

public class GeoTextLabelsModel extends CommonOptionsModel<String> {

	private final Construction construction;
	private final List<String> choices;

	public GeoTextLabelsModel(App app) {
		super(app);
		construction = app.getKernel().getConstruction();
		choices = new ArrayList<>();
	}

	@Override
	public List<String> getChoices(Localization loc) {
		choices.clear();
		for (GeoElement geo: construction.getGeoSetConstructionOrder()) {
			if (geo.isGeoText()) {
				choices.add(geo.getLabelSimple());
			}
		}
		return choices;
	}

	@Override
	protected void apply(int index, String value) {

	}

	@Override
	protected String getValueAt(int index) {
		return choices.get(index);
	}

	@Override
	protected boolean isValidAt(int index) {
		return false;
	}
}
