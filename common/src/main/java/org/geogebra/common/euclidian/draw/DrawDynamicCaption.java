package org.geogebra.common.euclidian.draw;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoText;

public class DrawDynamicCaption {
	private GeoText caption;
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
		if (caption == null) {
			caption = captionText.copy();
			drawTextCaption = new DrawText(view, caption);
		} else {
			caption.set(captionText);
			drawTextCaption.update();
		}

		caption.setAllVisualPropertiesExceptEuclidianVisible(captionText,
				false, false);
		caption.setFontSizeMultiplier(inputBox.getFontSizeMultiplier());
		caption.setEuclidianVisible(true);
		caption.setAbsoluteScreenLocActive(true);

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
		drawTextCaption.yLabel = captionText.isLaTeX()
				? middle - captionHeight / 2
				: drawInputBox.yLabel + drawInputBox.boxHeight;
	}

	public void highlightCapion() {
		caption.setBackgroundColor(
				isHighlighted()
				? GColor.LIGHT_GRAY
				: captionText.getBackgroundColor());
	}

	private boolean isHighlighted() {
		return drawInputBox.isHighlighted();
	}
}
