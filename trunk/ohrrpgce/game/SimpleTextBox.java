/*
 * TextBox.java
 * Created on April 9, 2007, 1:25 PM
 */

package ohrrpgce.game;

import java.util.Vector;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.Message;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;
import ohrrpgce.menu.TextSlice;

/**
 * A simple wrapper class for TextSlice to allow easy creation of stand-alone Slices.
 *   Exists mostly for backwards compatability, and until we get a better system for
 *   standalone menu slices. (Creating a MenuFormatArgs just to show a button at x,y,w,h 
 *   seems a bit excessive.)
 * @author Seth N. Hetu
 */
public class SimpleTextBox  {
	private TextSlice wrappedSlice;
	private int layoutRule;
	private int pos[] = new int[]{0,0};

    /**
     * Create a text box.
     * @param lines is how we want it to appear, with embedded \n's. The actual lines may vary.
     * @param borderColor,bkgrdColor are RRGGBB values
     * @param shade if true will draw a shadow for each character. This increases the box size.
     * @param transparency allows further control over transparency/translucency/opacity
     * @param skipNLSymbol will not display the newline symbol when a string is cut.
     * @param forcedSize overrides the MAX_WIDTH, MAX_HEIGHT default.
     */
    public SimpleTextBox(String lines, ImageAdapter font, int borderColor, int bkgrdColor, boolean shade, int transparency, boolean skipNLSymbol, int[] forcedSize) {
    	//Needs init...
    	if (Message.MAX_WIDTH==-1 || Message.MAX_HEIGHT==-1)
    		throw new LiteException(this, null, "Message.init() was not called.");
    	
    	//Prepare our slice
    	MenuFormatArgs mf = new MenuFormatArgs();
    	mf.widthHint = forcedSize[0];
    	mf.heightHint = forcedSize[1];
        mf.borderColors = new int[]{borderColor, 0};
        mf.bgColor = bkgrdColor;
    	if (transparency==MenuSlice.FILL_NONE) {
    		mf.fillType = MenuSlice.FILL_NONE;
    		mf.borderColors = new int[]{};
    	} else if (transparency==MenuSlice.FILL_SOLID)
    		mf.fillType = MenuSlice.FILL_SOLID;
    	else if (transparency==MenuSlice.FILL_TRANSLUCENT)
    		mf.fillType = MenuSlice.FILL_TRANSLUCENT;
    	else if (transparency==MenuSlice.FILL_GUESS)
    		throw new LiteException(this, null, "Cannot \"GUESS\" a SimpleTextBox's background");

    	//Make our box.
        wrappedSlice = new TextSlice(mf, lines, font, skipNLSymbol, shade, true);
        wrappedSlice.doLayout();
    }
    
    /**
     * Create a text box.
     * @param lines is how we want it to appear, with embedded \n's. The actual lines may vary.
     * @param borderColor,bkgrdColor are RRGGBB values
     * @shade if true will draw a shadow for each character. This increases the box size.
     */
    public SimpleTextBox(String lines, ImageAdapter font, int borderColor, int bkgrdColor, boolean shade) {
        this(lines, font, borderColor, bkgrdColor, shade, MenuSlice.FILL_TRANSLUCENT, false, new int[]{Message.MAX_WIDTH, MenuFormatArgs.HEIGHT_MINIMUM});
    }
    
    public SimpleTextBox(String lines, ImageAdapter font, int borderColor, int bkgrdColor, boolean shade, int transparency) {
        this(lines, font, borderColor, bkgrdColor, shade, transparency, false, new int[]{Message.MAX_WIDTH, MenuFormatArgs.HEIGHT_MINIMUM});
    }


    /**
     * Draw this as a message box (top-aligned).
     * @param gConext is the Graphics object.
     */
    public void paint(int offset, int screenWidth) {
    	wrappedSlice.paintAt(screenWidth/2-this.getWidth()/2, offset);
    }


    public void paint(int x, int y, int drawFlags) {
    	if ((drawFlags&GraphicsAdapter.HCENTER)!=0)
    		x -= getWidth()/2;
    	if ((drawFlags&GraphicsAdapter.RIGHT)!=0)
    		x -= getWidth();
    	if ((drawFlags&GraphicsAdapter.VCENTER)!=0)
    		y -= getHeight()/2;
    	if ((drawFlags&GraphicsAdapter.BOTTOM)!=0)
    		y -= getHeight();

    	wrappedSlice.paintAt(x, y);
    }
    
    public void paint() {
    	paint(getPosX(), getPosY(), getLayoutRule());
    }
   
    
    public int getWidth() {
        return wrappedSlice.getWidth();
    }

    public int getHeight() {
    	return wrappedSlice.getHeight();
    }
    
    public int getPosX() {
    	return pos[0];
    }
    
    public int getPosY() {
    	return pos[1];
    }
    
    public void setPosition(int x, int y) {
    	pos[0] = x;
    	pos[1] = y;
    }
    
    public int getLayoutRule() {
    	return layoutRule;
    }
    
    public void setLayoutRule(int rule) {
    	layoutRule = rule;
    }
}
