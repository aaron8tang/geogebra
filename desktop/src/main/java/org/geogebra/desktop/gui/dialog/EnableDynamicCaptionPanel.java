package org.geogebra.desktop.gui.dialog;

import org.geogebra.common.gui.dialog.options.model.EnableDynamicCaptionModel;
import org.geogebra.desktop.gui.inputfield.AutoCompleteTextFieldD;
import org.geogebra.desktop.main.AppD;

public class EnableDynamicCaptionPanel extends CheckboxPanel {
	private final AutoCompleteTextFieldD textField;

	public EnableDynamicCaptionPanel(AppD app, AutoCompleteTextFieldD textField,
			UpdateTabs tabs) {
		super(app, "UseTextAsCaption", tabs,
				new EnableDynamicCaptionModel(null, app));
		this.textField = textField;
	}

	@Override
	public void updateCheckbox(boolean value) {
		super.updateCheckbox(value);
		setCaptionTextFieldEnabled(!value);
	}

	@Override
	public void apply(boolean value) {
		super.apply(value);
		setCaptionTextFieldEnabled(!value);
	}

	private void setCaptionTextFieldEnabled(boolean enable) {
		textField.setEnabled(enable);
	}

}
