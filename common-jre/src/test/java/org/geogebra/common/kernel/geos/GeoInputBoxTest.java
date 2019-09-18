package org.geogebra.common.kernel.geos;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.util.TextObject;
import org.junit.Assert;
import org.junit.Test;

public class GeoInputBoxTest extends BaseUnitTest {

	private TextObject textObject = new TextObject() {
		String content;

		@Override
		public String getText() {
			return content;
		}

		@Override
		public void setText(String s) {
			content = s;
		}

		@Override
		public void setColumns(int fieldWidth) {

		}

		@Override
		public void setVisible(boolean b) {

		}

		@Override
		public void setEditable(boolean b) {

		}
	};

	@Test
	public void symbolicInputBoxUseDefinitionForFunctions() {
		add("f = x+1");
		add("g = 2f(x+2)+1");
		GeoInputBox inputBox1 = (GeoInputBox) add("InputBox(f)");
		GeoInputBox inputBox2 = (GeoInputBox) add("InputBox(g)");
		inputBox2.setSymbolicMode(true, false);
		Assert.assertEquals("x + 1", inputBox1.getText());
		Assert.assertEquals("2f(x + 2) + 1", inputBox2.getTextForEditor());
	}

	@Test
	public void symbolicInputBoxTextShouldBeInLaTeX() {
		add("f = x + 12");
		add("g = 2f(x + 1) + 2");
		GeoInputBox inputBox2 = (GeoInputBox) add("InputBox(g)");
		inputBox2.setSymbolicMode(true, false);
		Assert.assertEquals("2 \\; f\\left(x + 1 \\right) + 2", inputBox2.getText());
	}

	@Test
	public void testForSimpleUndefinedGeo() {
		add("a=?");
		GeoInputBox inputBox = (GeoInputBox) add("InputBox(a)");
		inputBox.setSymbolicMode(true, false);
		Assert.assertEquals("", inputBox.getText());
		Assert.assertEquals("", inputBox.getTextForEditor());

		inputBox.setSymbolicMode(false, false);
		Assert.assertEquals("", inputBox.getText());

	}

	@Test
	public void testForComplexUndefinedGeo() {
		add("a=1");
		add("b=?a");
		GeoInputBox inputBox = (GeoInputBox) add("InputBox(b)");
		inputBox.setSymbolicMode(true, false);
		Assert.assertEquals("?a", inputBox.getText());
		Assert.assertEquals("?a", inputBox.getTextForEditor());

		inputBox.setSymbolicMode(false, false);
		Assert.assertEquals("?a", inputBox.getText());
	}

	@Test
	public void testForEmptyInput() {
		add("a=1");

		GeoInputBox inputBox = (GeoInputBox) add("InputBox(a)");
		inputBox.setSymbolicMode(true, false);

		textObject.setText("");
		inputBox.textObjectUpdated(textObject);

		Assert.assertEquals("", inputBox.getText());
		Assert.assertEquals("", inputBox.getTextForEditor());

		inputBox.setSymbolicMode(false, false);

		Assert.assertEquals("", inputBox.getText());
	}

	@Test
	public void testForUndefinedInputInput() {
		add("a=1");

		GeoInputBox inputBox = (GeoInputBox) add("InputBox(a)");
		inputBox.setSymbolicMode(true, false);

		textObject.setText("?");
		inputBox.textObjectUpdated(textObject);

		Assert.assertEquals("", inputBox.getText());
		Assert.assertEquals("", inputBox.getTextForEditor());

		inputBox.setSymbolicMode(false, false);

		Assert.assertEquals("", inputBox.getText());
	}

	@Test
	public void testInputForGeoText() {
		add("text = \"?\" ");
		GeoInputBox inputBox = (GeoInputBox) add("InputBox(text)");

		inputBox.setSymbolicMode(true, false);
		Assert.assertEquals("\"?\"", inputBox.getText());
		Assert.assertEquals("\"?\"", inputBox.getTextForEditor());

		inputBox.setSymbolicMode(false, false);
		Assert.assertEquals("\"?\"", inputBox.getText());
	}
}
