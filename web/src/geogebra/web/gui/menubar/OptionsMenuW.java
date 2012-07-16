package geogebra.web.gui.menubar;

import geogebra.common.gui.menubar.MenuInterface;
import geogebra.common.gui.menubar.MyActionListener;
import geogebra.common.gui.menubar.OptionsMenu;
import geogebra.common.kernel.Kernel;
import geogebra.common.main.App;
import geogebra.web.gui.images.AppResources;

import com.google.gwt.user.client.ui.MenuBar;

/**
 * The "Options" menu.
 */
public class OptionsMenuW extends MenuBar implements MenuInterface, MyActionListener{
	
	private static App app;
	static Kernel kernel;
	
	private LanguageMenuW languageMenu;
	/**
	 * Constructs the "Option" menu
	 * @param app Application instance
	 */
	public OptionsMenuW(App app) {
		super(true);
	    this.app = app;
	    kernel = app.getKernel();
	    OptionsMenu.init(app);
	    addStyleName("GeoGebraMenuBar");
	    initItems();
	}
	
	private void initItems(){
		//"Algebra Descriptions" menu
		OptionsMenu.addAlgebraDescriptionMenu(this);
		OptionsMenu.addPointCapturingMenu(this);
		OptionsMenu.addDecimalPlacesMenu(this);	
		addSeparator();
		OptionsMenu.addLabelingMenu(this);
		addSeparator();
		OptionsMenu.addFontSizeMenu(this);
		//language menu
		addLanguageMenu();
	}
	
	private void addLanguageMenu() {
		languageMenu = new LanguageMenuW(app);
		addItem(GeoGebraMenubarW.getMenuBarHtml(AppResources.INSTANCE
		        .empty().getSafeUri().asString(), app.getMenu("Language")), true, languageMenu);
	}
	
	public void actionPerformed(String cmd){
		OptionsMenu.processActionPerformed(cmd);
	}


}
