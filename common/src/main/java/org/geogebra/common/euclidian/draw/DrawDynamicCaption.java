package org.geogebra.common.euclidian.draw;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoText;

public class DrawDynamicCaption {
	private GeoInputBox inputBox;
	private GeoText linkedCaption;
	private GeoText captionCopy;
	private DrawText drawCaption =null;
	private EuclidianView view;
	private DrawInputBox drawInputBox;
	private int captionWidth;
	private int captionHeight;

	public DrawDynamicCaption(EuclidianView view,
			DrawInputBox drawInputBox) {
		this.view = view;
		this.drawInputBox = drawInputBox;
		this.inputBox = drawInputBox.getGeoInputBox();
	}

	public boolean isEnabled() {
		return inputBox.isDynamicCaptionEnabled();
	}

	void draw(GGraphics2D g2) {
		if (drawCaption == null) {
			return;
		}

		positionDynamicCaption();
		drawCaption.draw(g2);
	}

	public void update() {
		if (!isEnabled()) {
			return;
		}

		updateCaption();
		measureCaption();
	}

	private void updateCaption() {
		if (linkedCaption != inputBox.getDynamicCaption()) {
			createCopy();
		}

		updateCopy();
	}

	private void createCopy() {
		linkedCaption = inputBox.getDynamicCaption();
		captionCopy = linkedCaption.copy();
		drawCaption = new DrawText(view, captionCopy);
	}

	private void updateCopy() {
		captionCopy.set(inputBox.getDynamicCaption());
		captionCopy.setAllVisualPropertiesExceptEuclidianVisible(linkedCaption,
				false, false);
		captionCopy.setFontSizeMultiplier(inputBox.getFontSizeMultiplier());
		captionCopy.setEuclidianVisible(true);
		captionCopy.setAbsoluteScreenLocActive(true);
		drawCaption.update();
	}

	public boolean measureCaption() {
		if (drawCaption == null) {
			return false;
		}

		captionWidth = (int) drawCaption.getBounds().getWidth();
		captionHeight = (int) drawCaption.getBounds().getHeight();
		drawInputBox.labelSize.x = captionWidth;
		drawInputBox.labelSize.y = captionHeight;
		drawInputBox.calculateBoxBounds();
		return linkedCaption.isLaTeX();
	}

	private void positionDynamicCaption() {
		drawCaption.xLabel = drawInputBox.xLabel - captionWidth;
		int middle = drawInputBox.boxTop + drawInputBox.boxHeight / 2;
		drawCaption.yLabel = linkedCaption.isLaTeX()
				? middle - captionHeight / 2
				: drawInputBox.yLabel + drawInputBox.boxHeight;
	}

	public void highlightCapion() {
		if (captionCopy == null) {
			return;
		}

		captionCopy.setBackgroundColor(
				isHighlighted()
				? GColor.LIGHT_GRAY
				: linkedCaption.getBackgroundColor());
	}

	private boolean isHighlighted() {
		return drawInputBox.isHighlighted();
	}
}
