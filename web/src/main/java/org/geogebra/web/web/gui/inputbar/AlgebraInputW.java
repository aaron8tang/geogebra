package org.geogebra.web.web.gui.inputbar;

import org.geogebra.common.euclidian.EuclidianViewInterfaceCommon;
import org.geogebra.common.kernel.CircularDefinitionException;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Feature;
import org.geogebra.common.main.GWTKeycodes;
import org.geogebra.common.main.MyError;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.common.util.Unicode;
import org.geogebra.web.html5.gui.AlgebraInput;
import org.geogebra.web.html5.gui.GPopupPanel;
import org.geogebra.web.html5.gui.NoDragImage;
import org.geogebra.web.html5.gui.inputfield.AutoCompleteTextFieldW;
import org.geogebra.web.html5.javax.swing.GOptionPaneW;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.web.css.GuiResources;
import org.geogebra.web.web.gui.layout.panels.AlgebraDockPanelW;
import org.geogebra.web.web.gui.view.algebra.AlgebraViewW;
import org.geogebra.web.web.gui.view.algebra.InputPanelW;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * @author gabor
 * 
 * InputBar for GeoGebraWeb
 *
 */
public class AlgebraInputW extends FlowPanel 
implements KeyUpHandler, FocusHandler, ClickHandler, BlurHandler, RequiresResize, AlgebraInput {

	protected AppW app;
	protected InputPanelW inputPanel;
	protected AutoCompleteTextFieldW inputField;
	protected FlowPanel eastPanel;
	//protected FlowPanel innerPanel;
	protected FlowPanel labelPanel;
	protected ToggleButton btnHelpToggle;
	protected InputBarHelpPopup helpPopup;
	//	protected PopupPanel helpPopup;
	private boolean focused = false;

	/**
	 * Creates AlgebraInput for Web
	 */
	public AlgebraInputW() {
		super();
	}

	/**
	 * @param app Application
	 * 
	 * Attaches Application and creates the GUI of AlgebraInput
	 */
	public void init(AppW app1) {
		this.app = app1;
		//AG I dont think we need this app.removeTraversableKeys(this);
		addStyleName("AlgebraInput");
		initGUI();
		app1.getGuiManager().addAlgebraInput(this);
	}

	private void initGUI() {
		clear();
		inputPanel = new InputPanelW(null,app,0,true);

		inputField = inputPanel.getTextComponent();
		inputField.requestToShowSymbolButton();

		inputField.getTextBox().addKeyUpHandler(this);
		inputField.getTextBox().addFocusHandler(this);
		inputField.getTextBox().addBlurHandler(this);

		inputField.addHistoryPopup(app.showInputTop());

		//AG updateFonts()

		btnHelpToggle = new ToggleButton(
				new NoDragImage(GuiResources.INSTANCE.input_help_left().getSafeUri().asString(),20),
				//new Image(AppResources.INSTANCE.inputhelp_left_20x20().getSafeUri().asString()), 
				new NoDragImage(GuiResources.INSTANCE.input_help_up().getSafeUri().asString(),20));
		//new Image(AppResources.INSTANCE.inputhelp_right_20x20().getSafeUri().asString()));
		btnHelpToggle.addStyleName("inputHelp-toggleButton");

		btnHelpToggle.addClickHandler(this);

		//in CSS btnHelpToggle.setIcon(app.getImageIcon("inputhelp_left_18x18.png"));
		//in CSS	btnHelpToggle.setSelectedIcon(app.getImageIcon("inputhelp_right_18x18.png"));

		labelPanel = new FlowPanel();
		//labelPanel.setHorizontalAlignment(ALIGN_RIGHT);
		//labelPanel.setVerticalAlignment(ALIGN_MIDDLE);
		labelPanel.setStyleName("AlgebraInputLabel");



		// TODO: eastPanel should hold the command help button
		//eastPanel = new FlowPanel();


		// place all components in an inner panel
		//innerPanel = new FlowPanel();	    
		add(labelPanel);
		//innerPanel.setCellHorizontalAlignment(labelPanel, ALIGN_RIGHT);
		//innerPanel.setCellVerticalAlignment(labelPanel, ALIGN_MIDDLE);
		add(inputPanel);
		//innerPanel.setCellHorizontalAlignment(inputPanel, ALIGN_LEFT);
		//innerPanel.setCellVerticalAlignment(inputPanel, ALIGN_MIDDLE);
		//setCellVerticalAlignment(innerPanel, ALIGN_MIDDLE);

		// add innerPanel to wrapper (this panel)
		//setVerticalAlignment(ALIGN_MIDDLE);
		//add(innerPanel);
		//add(eastPanel);
		//setCellVerticalAlignment(this, ALIGN_MIDDLE);
		if (app.showInputHelpToggle()) {
			add(btnHelpToggle);
		}

		setLabels();

		//setInputFieldWidth();

	}


	/**
	 * Sets the width of the text field so that the entire width of the parent
	 * container is used. (Really just a workaround because the nested gwt
	 * panels are not allowing 100% width to work as we would like).
	 */
	public void setInputFieldWidth(int width) {
		// if the size is too small, use default size
		if (width > 100) {
			inputPanel.setWidth((width - 100) + "px");
		}
	}

	public void onResize() {
		if(app == null){
			return;
		}
		if (!app.isApplet()) {
			setInputFieldWidth((int)app.getWidth());
		}

		// hide the help popup
		//		btnHelpToggle.setValue(false);
		setShowInputHelpPanel(false);
	}

	/**
	 * updates labels according to current locale
	 */
	public void setLabels() {
		if(app == null){
			return;
		}


		if (btnHelpToggle != null) {
			btnHelpToggle.setTitle(app.getMenu("InputHelp"));
		}

		if (helpPopup != null) {
			app.getGuiManager().getInputHelpPanel().setLabels();
		}

		inputField.setDictionary(false);
		inputField.getTextField().getElement().setAttribute("placeholder",
				app.getPlain("InputLabel") + Unicode.ellipsis);
	}

	/**
	 * Sets the content of the input textfield and gives focus
	 * to the input textfield.
	 * @param str 
	 */
	public void replaceString(String str) {
		inputField.setText(str);
	}

	// see actionPerformed
	public void insertCommand(String cmd) {
		if (cmd == null) return;

		int pos = inputField.getCaretPosition();
		String oldText = inputField.getText();
		String newText = 
				oldText.substring(0, pos) + 
				cmd + "[]" +
				oldText.substring(pos);			 			

		inputField.setText(newText);
		inputField.setCaretPosition(pos + cmd.length() + 1);		
		inputField.requestFocus();
	}

	public void insertString(String str) {
		if (str == null) return;

		int pos = inputField.getCaretPosition();
		String oldText = inputField.getText();
		String newText = 
				oldText.substring(0, pos) + str +
				oldText.substring(pos);			 			

		inputField.setText(newText);
		inputField.setCaretPosition(pos + str.length());		
		inputField.requestFocus();
	}

	private AlgebraDockPanelW getAlgebraDockPanel() {
		return (AlgebraDockPanelW) app.getGuiManager().getLayout()
				.getDockManager().getPanel(App.VIEW_ALGEBRA);

	}
	public void onFocus(FocusEvent event) {
		if (((AlgebraViewW) app.getGuiManager().getAlgebraView())
				.isNodeTableEmpty() && !getAlgebraDockPanel().hasLongStyleBar()) {
			getAlgebraDockPanel().showStyleBarPanel(false);
		}
		Object source = event.getSource();
		AutoCompleteTextFieldW.showSymbolButtonIfExists(source, true);
		app.getSelectionManager().clearSelectedGeos();
		this.focused = true;
	}

	public void onBlur(BlurEvent event) {
		getAlgebraDockPanel().showStyleBarPanel(true);
		Object source = event.getSource();
		AutoCompleteTextFieldW.showSymbolButtonIfExists(source, false);
		this.focused = false;
	}

	public void onKeyUp(KeyUpEvent event) {
		// the input field may have consumed this event
		// for auto completion
		//then it don't come here if (e.isConsumed()) return;

		int keyCode = event.getNativeKeyCode();
		if (app.has(Feature.INPUT_BAR_PREVIEW)) {
			app.getKernel().getInputPreviewHelper()
					.updatePreviewFromInputBar(inputField.getText(),
							new AsyncOperation<Boolean>() {

								@Override
								public void callback(Boolean obj) {
									((Element) inputField.getElement()
											.getChild(0))
													.getStyle()
													.setBorderColor(obj ? ""
													: "#FFA500");

								}
							});
		}
		if (keyCode == GWTKeycodes.KEY_ENTER && !inputField.isSuggestionJustHappened()) {
			app.getKernel().clearJustCreatedGeosInViews();
			final String input = app.getKernel().getInputPreviewHelper()
					.getInput(getTextField().getText());

			if (input == null || input.length() == 0)
			{
				app.getActiveEuclidianView().requestFocusInWindow(); // Michael Borcherds 2008-05-12
				return;
			}

			app.setScrollToShow(true);

			try {
				AsyncOperation<GeoElement[]> callback = new AsyncOperation<GeoElement[]>() {

					@Override
					public void callback(GeoElement[] geos) {

						if (geos == null) {
							inputField.getTextBox().setFocus(true);
							return;
						}

						// need label if we type just eg
						// lnx
						if (geos.length == 1 && !geos[0].labelSet) {
							geos[0].setLabel(geos[0].getDefaultLabel());
						}

						// create texts in the middle of the visible view
						// we must check that size of geos is not 0 (ZoomIn, ZoomOut, ...)
						if (geos.length > 0 && geos[0] != null && geos[0].isGeoText()) {
							GeoText text = (GeoText)geos[0];
							if (!text.isTextCommand() && text.getStartPoint() == null) {

								Construction cons = text.getConstruction();
								EuclidianViewInterfaceCommon ev = app.getActiveEuclidianView();

								boolean oldSuppressLabelsStatus = cons.isSuppressLabelsActive();
								cons.setSuppressLabelCreation(true);
								GeoPoint p = new GeoPoint(text.getConstruction(), null, ( ev.getXmin() + ev.getXmax() ) / 2, ( ev.getYmin() + ev.getYmax() ) / 2, 1.0);
								cons.setSuppressLabelCreation(oldSuppressLabelsStatus);

								try {
									text.setStartPoint(p);
									text.update();
								} catch (CircularDefinitionException e1) {
									e1.printStackTrace();
								}
							}
						}

						app.setScrollToShow(false);


						inputField.addToHistory(input);
						if (!getTextField().getText().equals(input)) {
							inputField.addToHistory(getTextField().getText());
						}
						inputField.setText(null);

						inputField.setIsSuggestionJustHappened(false);
					}

				};

				app.getKernel().getAlgebraProcessor()
						.processAlgebraCommandNoExceptionHandling(input, true,
								getErrorHandler(), true, callback);


			} catch (Exception ee) {
				inputField.addToHistory(getTextField().getText());
				GOptionPaneW.setCaller(inputField.getTextBox());
				app.showError(ee, inputField);
				return;
			} catch (MyError ee) {
				inputField.addToHistory(getTextField().getText());
				GOptionPaneW.setCaller(inputField.getTextBox());
				inputField.showError(ee);
				return;
			}				  			   

		} else if (keyCode != GWTKeycodes.KEY_C && keyCode != GWTKeycodes.KEY_V && keyCode != GWTKeycodes.KEY_X) { 
			app.getGlobalKeyDispatcher().handleGeneralKeys(event); // handle eg ctrl-tab 
			if (keyCode == GWTKeycodes.KEY_ESCAPE) inputField.setText(null);
		}
		inputField.setIsSuggestionJustHappened(false);
	}

	private ErrorHandler getErrorHandler() {
		return new ErrorHandler() {

			public void showError(String msg) {
				app.getDefaultErrorHandler().showError(msg);

			}

			public void setActive(boolean b) {
				// TODO Auto-generated method stub

			}

			public void showCommandError(String command, String message) {
				app.getDefaultErrorHandler().showCommandError(command, message);

			}

			public String getCurrentCommand() {
				return inputField.getCommand();
			}
		};
	}

	public void requestFocus(){
		inputField.requestFocus();
	}

	public void onClick(ClickEvent event) {
		Object source = event.getSource();

		if (source == btnHelpToggle) {
			if (btnHelpToggle.isDown()) {
				setShowInputHelpPanel(true);
			} else {
				setShowInputHelpPanel(false);
			}
		}
	}

	private void setHelpPopup(){
		if (helpPopup == null && app != null) {
			helpPopup = new InputBarHelpPopup(this.app, this.inputField);
			helpPopup.addAutoHidePartner(this.getElement());
			if (btnHelpToggle != null) {
				helpPopup.setBtnHelpToggle(this.btnHelpToggle);
			}
		}
	}

	public void setShowInputHelpPanel(boolean show) {
		
		if (show) {
			InputBarHelpPanelW helpPanel = (InputBarHelpPanelW) app.getGuiManager()
			        .getInputHelpPanel();
			helpPanel.updateGUI();
			setHelpPopup();
		
			helpPopup
					.setPopupPositionAndShow(new GPopupPanel.PositionCallback() {
				public void setPosition(int offsetWidth, int offsetHeight) {
					helpPopup.getElement().getStyle().setProperty("left", "auto");
					helpPopup.getElement().getStyle().setProperty("top", "auto");
					helpPopup.getElement().getStyle().setRight(0, Unit.PX);
					helpPopup.getElement().getStyle().setBottom(getOffsetHeight()*app.getArticleElement().getScaleX(), Unit.PX);
					helpPopup.show();
				}
			});

		} else if (helpPopup != null) {
			helpPopup.hide();
		}
	}

	public void setText(String s) {
		this.inputField.setText(s);
	}

	public boolean hasFocus(){
		return this.focused || AutoCompleteTextFieldW.showSymbolButtonFocused;
	}

	public AutoCompleteTextFieldW getTextField(){
		return this.inputField;
	}

}
