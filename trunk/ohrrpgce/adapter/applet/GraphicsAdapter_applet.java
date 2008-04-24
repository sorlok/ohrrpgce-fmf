package ohrrpgce.adapter.applet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import ohrrpgce.adapter.FontAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.menu.Action;

/**
 * NOTE: This class should be copied into ohrrpgce.adapter, and re-named "GraphicsAdapter.java"
 *    (don't forget to change the package)
 *    This is an efficiency issue; iheritance is just too slow for Graphics  
 * @author Seth N. Hetu
 */
public class GraphicsAdapter_applet {
	//Inherited from J2ME's layout rules...
	public static final int HCENTER = 1;
	public static final int VCENTER = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int TOP = 16;
	public static final int BOTTOM = 32;
	public static final int BASELINE = 64;
	
	private static Graphics g;
	private static Action flushAction;
	private static Hashtable preComputedImages;
	private static final int MAX_IMAGES = 1024;
        
        private static int[] origClip;
	
	public static final void init(Graphics gContext, Action doFlush) {
		g = gContext;
		flushAction = doFlush;
		preComputedImages = new Hashtable/*<int[], Image>*/();
	}
	
	public static final void drawImage(ImageAdapter img, int tlX, int tlY) {
		drawImage(img, tlX, tlY, TOP|LEFT);
	}

	private static final int[] alignBlock(int anchorX, int anchorY, int width, int height, int anchorFlags) {
		int tlX = anchorX;
		int tlY = anchorY;
		if ((anchorFlags&BOTTOM)!=0 || (anchorFlags&BASELINE)!=0)
			tlY -= height;
		else if ((anchorFlags&VCENTER)!=0)
			tlY -= height/2;

		if ((anchorFlags&RIGHT)!=0)
			tlX -= width;
		else if ((anchorFlags&HCENTER)!=0)
			tlX -= width/2;
		
		return new int[]{tlX, tlY};
	}
	
	public static final void drawImage(ImageAdapter img, int anchorX, int anchorY, int anchor) {
		int[] tlCorner = alignBlock(anchorX, anchorY, img.getWidth(), img.getHeight(), anchor);
		
		g.drawImage((Image)img.getInternalImage(), tlCorner[0], tlCorner[1], null);
	}

	public static final void drawLine(int startX, int startY, int endX, int endY) {
		g.drawLine(startX, startY, endX, endY);
	}
	
	public static final void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha) {
		//Null?
		if (rgbData.length==0)
			return;
		
		//Make an image
		if (!preComputedImages.containsKey(rgbData)) {
			//Prevents infinite memory growth... probably not needed, though.
			if (preComputedImages.size()>=MAX_IMAGES)
				preComputedImages.clear();
			
			int type = BufferedImage.TYPE_INT_RGB;
			if (processAlpha)
				type = BufferedImage.TYPE_INT_ARGB;
			BufferedImage img = new BufferedImage(width, height, type);
			img.setRGB(0, 0, width, height, rgbData, offset, scanlength);
			
			preComputedImages.put(rgbData, img);
		}
		g.drawImage((Image)preComputedImages.get(rgbData), x, y, null);
	}
	
	public static final void drawRect(int x, int y, int width, int height) {
		g.drawRect(x, y, width, height);
	}
	
	public static final void fillRect(int x, int y, int width, int height) {
		g.fillRect(x, y, width, height);
	}
	
	
	public static final void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		g.drawArc(x, y, width, height, startAngle, arcAngle);
	}
	
	public static final void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		g.fillArc(x, y, width, height, startAngle, arcAngle);
	}
	
	
	public static final void setClip(int x, int y, int width, int height) {
            if (origClip==null)
                origClip = new int[]{x, y, width, height};
	    g.setClip(x, y, width, height);
	}
	
	public static final void resetClip() {
		g.setClip(origClip[0], origClip[1], origClip[2], origClip[3]);
	}
	
	public static final int getColor() {
		return g.getColor().getRGB();
	}
	public static final void setColor(int newRGB) {
		g.setColor(new Color(newRGB));
	}
	
	public static final void setFont(FontAdapter fa) {
		g.setFont((Font)fa.getFontData());
	}
	
	public static final FontAdapter getFont() {
		return new ohrrpgce.adapter.applet.FontAdapter(g.getFont());
	}
	
	public static final void drawString(String str, int anchorX, int anchorY, int anchor) {
		int width = g.getFontMetrics().stringWidth(str);
		int[] tlCorner = alignBlock(anchorX, anchorY, width, g.getFont().getSize(), anchor);
		tlCorner[1] += g.getFont().getSize()-1; //Fonts are always baseline-displayed (-1 to look nicer)
		
		g.drawString(str, tlCorner[0], tlCorner[1]);
	}
	
	public static final int stringWidth(Font awtFont, String str) {
		return g.getFontMetrics(awtFont).stringWidth(str);
	}

	public static final void flushGraphics() {
		//Flush the double buffer?
		flushAction.perform(null);
	}
}
