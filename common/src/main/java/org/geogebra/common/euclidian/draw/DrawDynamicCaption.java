package org.geogebra.common.euclidian.draw;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoInputBox;
import org.geogebra.common.kernel.geos.GeoText;

public class DrawDynamicCaption {
	private final GeoInputBox inputBox;
	private final GeoText captionCopy;
	private final DrawText drawCaption;
	private final DrawInputBox drawInputBox;
	private int captionWidth;
	private int captionHeight;

	public DrawDynamicCaption(EuclidianView view,
			DrawInputBox drawInputBox) {
		this.drawInputBox = drawInputBox;
		this.inputBox = drawInputBox.getGeoInputBox();
		captionCopy = new GeoText(inputBox.cons);
		drawCaption = new DrawText(view, captionCopy);
	}

	public boolean isEnabled() {
		return inputBox.isDynamicCaptionEnabled();
	}

	void draw(GGraphics2D g2) {
		if (noCaption()) {
			return;
		}

		update();
		highlightCaption();
		positionDynamicCaption();
		drawCaption.draw(g2);
	}

	private boolean noCaption() {
		return getDynamicCaption() == null;
	}

	public void update() {
		if (noCaption() || !isEnabled()) {
			return;
		}

		updateCaptionCopy();
		setLabelSize();
	}

	private void updateCaptionCopy() {
		captionCopy.set(getDynamicCaption());
		captionCopy.setAllVisualPropertiesExceptEuclidianVisible(getDynamicCaption(),
				false, false);
		captionCopy.setFontSizeMultiplier(inputBox.getFontSizeMultiplier());
		captionCopy.setEuclidianVisible(true);
		captionCopy.setAbsoluteScreenLocActive(true);
		drawCaption.update();
	}

	private GeoText getDynamicCaption() {
		return inputBox.getDynamicCaption();
	}

	public boolean setLabelSize() {
		if (drawCaption == null || drawCaption.getBounds() == null) {
			return false;
		}

		captionWidth = (int) drawCaption.getBounds().getWidth();
		captionHeight = (int) drawCaption.getBounds().getHeight();
		drawInputBox.labelSize.x = captionWidth;
		drawInputBox.labelSize.y = captionHeight;
		drawInputBox.calculateBoxBounds();
		return getDynamicCaption().isLaTeX();
	}

	private void positionDynamicCaption() {
		drawCaption.xLabel = drawInputBox.xLabel - captionWidth;
		int middle = drawInputBox.boxTop + drawInputBox.boxHeight / 2;
		drawCaption.yLabel = getDynamicCaption().isLaTeX()
				? middle - captionHeight / 2
				: drawInputBox.yLabel + drawInputBox.boxHeight;
	}

	public void highlightCaption() {
		captionCopy.setBackgroundColor(
				isHighlighted()
				? GColor.LIGHT_GRAY
				: getDynamicCaption().getBackgroundColor());
	}

	private boolean isHighlighted() {
		return drawInputBox.isHighlighted();
	}

	public int getHeight() {
		return captionHeight;
	}

	public boolean hit(int x, int y, int hitThreshold) {
		if (!isEnabled()) {
			return false;
		}
		return drawCaption.hit(x, y, hitThreshold);
	}
}
