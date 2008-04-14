package ohrrpgce.game;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;

/**
 * A wrapper for MenuSlice (see: SimpleTextBox)
 * @author Seth N. Hetu
 */
public class SimpleCanvas {
	//Layout
	private int[] pos = new int[]{0,0};
	private int layoutRule = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
    
    //Wrapped Object
	private MenuSlice wrapped;
	private MenuFormatArgs cachedMF;
    
    
    ///////////////////////////////////
    // Implementation
    ///////////////////////////////////
    
    public SimpleCanvas(int bgColor, int[] borderColors, int fillType) {
    	this(-1, -1, bgColor, borderColors, fillType);
    }
    
    /**
     * Create a surface with a certain background color and some border lines.
     * @width, height MUST assume that "borderColors.length"*2 pixels for the borders
     * @param bgColor AARRGGBB format; the background for the box. Fills the entire box.
     * @param borderColors An array of RRGGBB values to be applies, from outer to inner, to the box.
     * @param fillType NONE or SOLID are the best; TRANSLUCENT uses a lot of memory.
     */
    public SimpleCanvas(int width, int height, int bgColor, int[] borderColors, int fillType) {
    	//Prepare our slice
    	cachedMF = new MenuFormatArgs();
    	cachedMF.bgColor = bgColor;
    	cachedMF.borderColors = borderColors;
    	cachedMF.fillType = fillType;
    	cachedMF.widthHint = width;
    	cachedMF.heightHint = height;
    	if (width==-1 || height==-1) {
        	cachedMF.widthHint = 1;
        	cachedMF.heightHint = 1;
    	}
    	
    	//Start your engines!
    	wrapped = new MenuSlice(cachedMF);
    	wrapped.doLayout();
    }
    
    
    public void setPixel(int x, int y, int colorRGB) {
    	wrapped.setPixel(x, y, colorRGB);
    }
    
    public void paint() {
    	paint(pos[0], pos[1], layoutRule);
    }
    
    
    /**
     * "Draw flags" contains the Graphics flags. 
     */
    public void paint(int x, int y, int drawFlags) {
    	int[] TL = getTopLeftCorner(x, y, drawFlags);
    	x = TL[0];
    	y = TL[1];
        
    	//Draw our slice
    	wrapped.paintAt(x, y);
    }
    
    
    public int[] getTopLeftCorner() {
        return getTopLeftCorner(pos[0], pos[1], layoutRule);
    }
    
    public int[] getTopLeftCorner(int posX, int posY, int drawRule) {
        if ((drawRule&GraphicsAdapter.RIGHT)!=0)
            posX -= getWidth();
        if ((drawRule&GraphicsAdapter.HCENTER)!=0)
            posX -= getWidth()/2;
        if ((drawRule&GraphicsAdapter.BOTTOM)!=0)
            posY -= getHeight();
        if ((drawRule&GraphicsAdapter.VCENTER)!=0)
            posY -= getHeight()/2;
        
        return new int[]{posX, posY};
    }
    
    
    
    
    
    ///////////////////////////////////
    // Properties
    ///////////////////////////////////
    public void setPosition(int x, int y) { pos[0] = x; pos[1] = y; }
    public void setLayoutRule(int rule) { this.layoutRule = rule; }
    public int getLayoutRule() { return layoutRule; }
    public int getPosX() { return pos[0]; }
    public int getPosY() { return pos[1]; }
    public int getWidth() { return wrapped.getWidth(); }
    public int getHeight() { return wrapped.getHeight(); }
    
    /**
     * Handle with care
     */
    public void setSize(int w, int h) { 
    	//Set size
    	cachedMF.widthHint = w;
    	cachedMF.heightHint = h;
    	
    	//Might work...
    	wrapped = new MenuSlice(cachedMF);
    	wrapped.doLayout();
    }
    
	

}
