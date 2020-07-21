package org.geogebra.common.euclidian.draw;

import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoText;

public class DrawDynamicCaption {
	private GeoText captionText;
	private GeoInputBox inputBox;
	private DrawText drawTextCaption;
	private EuclidianView view;
	private DrawInputBox drawInputBox;
	private int captionWidth;
	private int captionHeight;

	public DrawDynamicCaption(EuclidianView view,
			DrawInputBox drawInputBox) {
		this.view = view;
		this.drawInputBox = drawInputBox;
		this.inputBox = drawInputBox.getGeoInputBox();
		this.captionText = inputBox.getDynamicCaption();
	}

	public boolean isEnabled() {
		return inputBox.isDynamicCaptionEnabled();
	}

	void draw(GGraphics2D g2) {
		drawTextCaption.draw(g2);
	}

	public void update() {
		if (!isEnabled() || captionText == null) {
			return;
		}

		updateDrawTextCaption();
		positionDynamicCaption();
		positionDynamicCaption();
	}

	private void updateDrawTextCaption() {
		GeoText caption = captionText.copy();
		caption.setFontSizeMultiplier(inputBox.getFontSizeMultiplier());
		caption.setEuclidianVisible(true);
		caption.setAbsoluteScreenLocActive(true);
		drawTextCaption = new DrawText(view, caption);
	}

	public boolean measureCaption() {
		if (drawTextCaption == null) {
			return false;
		}

		captionWidth = (int) drawTextCaption.getBounds().getWidth();
		captionHeight = (int) drawTextCaption.getBounds().getHeight();
		drawInputBox.calculateBoxBounds();
		return captionText.isLaTeX();
	}

	private void positionDynamicCaption() {
		int x = drawInputBox.xLabel ;
		int y = drawInputBox.yLabel;
		drawTextCaption.xLabel = x - captionWidth;
		drawTextCaption.yLabel = y - captionHeight / 2;
	}
}
