package org.geogebra.desktop.gui.dialog;

import java.awt.event.ItemEvent;

import javax.swing.JPanel;

import org.geogebra.common.gui.SetLabels;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.gui.inputfield.AutoCompleteTextFieldD;
import org.geogebra.desktop.main.AppD;

public class DynamicCaptionPanelD extends OptionPanel implements SetLabels {
	private final EnableDynamicCaptionPanel enableDynamicCaption;

	public DynamicCaptionPanelD(AppD app, AutoCompleteTextFieldD textField, UpdateTabs tabs) {
		this.enableDynamicCaption = new EnableDynamicCaptionPanel(app, textField, tabs);
		add(enableDynamicCaption);
	}


	@Override
	public void setLabels() {
		enableDynamicCaption.setLabels();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

	}

	@Override
	public void updateFonts() {

	}

	@Override
	public JPanel updatePanel(Object[] geos) {
		enableDynamicCaption.updatePanel(geos);
		return this;
	}

	@Override
	public void updateVisualStyle(GeoElement geo) {

	}
}
