package org.geogebra.common.euclidian.draw;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GRectangle;
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
		positionDynamicCaption();
		drawTextCaption.draw(g2);
	}

	public void update() {
		if (!isEnabled() || captionText == null) {
			return;
		}

		updateDrawTextCaption();
		measureCaption();
	}

	private void updateDrawTextCaption() {
		GeoText caption = captionText.copy();
		caption.setAllVisualPropertiesExceptEuclidianVisible(captionText,
				false, false);
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
		drawInputBox.labelSize.x = captionWidth;
		drawInputBox.labelSize.y = captionHeight;
		drawInputBox.calculateBoxBounds();
		return captionText.isLaTeX();
	}

	private void positionDynamicCaption() {
		drawTextCaption.xLabel = drawInputBox.xLabel - captionWidth;
		int middle = drawInputBox.boxTop + drawInputBox.boxHeight / 2;
		drawTextCaption.yLabel = middle - captionHeight / 2;
	}

	public void highlightLabel(GGraphics2D g2) {
		if (!drawInputBox.isHighlighted()) {
			return;
		}
		g2.setPaint(GColor.LIGHT_GRAY);
		GRectangle bounds = drawTextCaption.getBounds();
		g2.fillRect((int)bounds.getX(), (int)bounds.getY(), (int)bounds.getWidth(),
				(int)bounds.getHeight());
	}
}
