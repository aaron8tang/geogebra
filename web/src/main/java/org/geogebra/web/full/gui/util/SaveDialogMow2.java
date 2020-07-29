package org.geogebra.web.full.gui.util;

import org.geogebra.common.move.ggtapi.models.Material;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.shared.components.ComponentCheckbox;
import org.geogebra.web.shared.components.DialogData;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class SaveDialogMow2 extends DoYouWantToSaveChangesDialog {
	private ComponentCheckbox templateCheckbox;

	/**
	 * base dialog constructor
	 * @param app - see {@link AppW}
	 * @param dialogData - contains trans keys for title and buttons
	 * @param autoHide - if the dialog should be closed on click outside
	 * @param hasScrim - background should be greyed out
	 */
	public SaveDialogMow2(AppW app,
			DialogData dialogData, boolean autoHide,
			boolean hasScrim) {
		super(app, dialogData, autoHide, hasScrim);
		addStyleName("templateSave");
		setOnPositiveAction(() -> {
			if (templateCheckbox.isSelected()) {
				setSaveType(Material.MaterialType.ggsTemplate);
				app.getSaveController().ensureTypeOtherThan(Material.MaterialType.ggs);
			} else {
				setSaveType(Material.MaterialType.ggs);
				app.getSaveController().ensureTypeOtherThan(Material.MaterialType.ggsTemplate);
			}
			app.getSaveController().saveAs(getInputField().getText(),
					getSaveVisibility(), this);
		});
	}

	@Override
	public void buildContent() {
		super.buildContent();
		Label templateTxt = new Label(app.getLocalization().getMenu("saveTemplate"));
		templateCheckbox = new ComponentCheckbox(false, templateTxt);
		getContentPanel().add(templateCheckbox);
	}

	@Override
	public void show() {
		super.show();
		Material activeMaterial = ((AppW) app).getActiveMaterial();
		templateCheckbox.setSelected(activeMaterial != null && Material.MaterialType.ggsTemplate
				.equals(activeMaterial.getType()));
	}

	@Override
	public void showAndPosition(Widget anchor) {
		// only in super
	}
}