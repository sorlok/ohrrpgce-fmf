package ohrrpgce.adapter.applet;

import java.awt.Font;

import ohrrpgce.adapter.GraphicsAdapter;

public class FontAdapter implements ohrrpgce.adapter.FontAdapter {

	private Font awtFont;
	private GraphicsAdapter_applet parentGraphics;
	
	public FontAdapter(Font awtFont/*, GraphicsAdapter_applet parentGraphics*/) {
		this.awtFont = awtFont;
		//this.parentGraphics = parentGraphics;
	}
	
	public Object getFontData() {
		return awtFont;
	}
	
	public int getFontHeight() {
		return awtFont.getSize();
	}

	public int stringWidth(String str) {
		return GraphicsAdapter.stringWidth(awtFont, str);
	}

}
