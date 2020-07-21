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
		update();
		measureCaption();
		positionDynamicCaption();
		drawTextCaption.draw(g2);
	}

	private void update() {
		if (captionText == null) {
			return;
		}

		GeoText caption = captionText.copy();
		caption.setFontSizeMultiplier(inputBox.getFontSizeMultiplier());
		caption.setEuclidianVisible(true);
		caption.setAbsoluteScreenLocActive(true);
		drawTextCaption = new DrawText(view, caption);
	}

	private void measureCaption() {
		if (drawTextCaption == null) {
			return;
		}

		drawInputBox.labelSize.x = (int) drawTextCaption.getBounds().getWidth();
		drawInputBox.labelSize.y = (int) drawTextCaption.getBounds().getHeight();
		drawInputBox.calculateBoxBounds();

	}
	private void positionDynamicCaption() {
		int x = drawInputBox.xLabel - drawInputBox.labelSize.x;
		int y =  (int) drawInputBox.getyLabel()
				+ (drawInputBox.boxHeight - drawInputBox.labelSize.y) / 2;
		drawTextCaption.xLabel = x;
		drawTextCaption.yLabel = y;
	}

}
