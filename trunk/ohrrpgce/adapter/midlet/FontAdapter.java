package ohrrpgce.adapter.midlet;

import javax.microedition.lcdui.Font;
import ohrrpgce.adapter.GraphicsAdapter;

public class FontAdapter implements ohrrpgce.adapter.FontAdapter {

	private Font j2meFont;
	private GraphicsAdapter parentGraphics;
	
	public FontAdapter(Font j2meFont) {
            this.j2meFont = j2meFont;
	}
	
	public Object getFontData() {
            return j2meFont;
	}
	
	public int getFontHeight() {
            return j2meFont.getHeight();
	}

	public int stringWidth(String str) {
            return j2meFont.stringWidth(str);
	}

}
