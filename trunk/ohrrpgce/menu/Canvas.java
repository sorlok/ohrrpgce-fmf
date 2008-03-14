package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import sun.swing.BakedArrayList;

/**
 * The canvas is the default drawable object for all menu items.
 * A canvas can have a border/bkgrd color, and has an internal drawable surface.
 * @author Seth N. Hetu
 */
public class Canvas {
	//Statics
	private static final int X = 0;
	private static final int Y = 1;
	private static final int WIDTH = 2;
	private static final int HEIGHT = 3;
	public static final int FILL_NONE = 0;
	public static final int FILL_SOLID = 1;
	public static final int FILL_TRANSPARENT = 2;
	
	//Properties common to all canvases
	private int[] rectangle = new int[]{0,0,0,0};
	private int layoutRule = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
	private int[] pixelBuffer;
	private int bgColor;
    private int[] borderColors;
    
    //Used to help caching, etc.
    private int fillType;
    private boolean hasExpanded;
    private int[] pixelBufferSize = new int[]{0,0,0,0};
    
    
    ///////////////////////////////////
    // Implementation
    ///////////////////////////////////
    
    /**
     * Create a surface with a certain background color and some border lines.
     * @width, height MUST assume that "borderColors.length"*2 pixels for the borders
     * @param bgColor AARRGGBB format; the background for the box. Fills the entire box.
     * @param borderColors An array of RRGGBB values to be applies, from outer to inner, to the box.
     * @param fillType NONE or SOLID are the best; transparent uses a lot of memory.
     */
    public Canvas(int width, int height, int bgColor, int[] borderColors, int fillType) {
    	this.fillType = fillType;
    	this.bgColor = bgColor;
    	this.borderColors = borderColors;
    	this.setSize(width, height);
    	this.hasExpanded = false;
    }
    
    
    public void setPixel(int x, int y, int colorRGB) {
    	if (!hasExpanded)
    		expand();
    	pixelBuffer[y*getWidth()+x] = 0xFF000000|colorRGB;
    }
    
    private void expand() {
    	if (fillType == FILL_NONE || fillType == FILL_SOLID) {
    		//Create a new integer array
    		int[] newBuffer = new int[getWidth()*getHeight()];
    		
    		//Copy over the old one
    		if (pixelBuffer != null) {
    			for (int srcY=0; srcY<pixelBufferSize[HEIGHT]; srcY++) {
    				for (int srcX=0; srcX<pixelBufferSize[WIDTH]; srcX++) {
    					int destX = srcX + pixelBufferSize[X];
    					int destY = srcY + pixelBufferSize[Y];
    					newBuffer[destY*getWidth()+destX] = pixelBuffer[srcY*pixelBufferSize[WIDTH]+srcX];
    				}
    			}
    		}
    		
    		//Assign the buffer
    		pixelBuffer = newBuffer;
    		pixelBufferSize[X]= 0;
    		pixelBufferSize[Y]= 0;
    		pixelBufferSize[WIDTH]= getWidth();
    		pixelBufferSize[HEIGHT]= getHeight();
    	}

    	hasExpanded = true;
    }
    
    /**
     * "Draw flags" contains the Graphics flags. 
     */
    public void paint(int x, int y, int drawFlags) {
    	//Transform based on the draw flags
        if ((drawFlags&GraphicsAdapter.RIGHT)!=0)
            x -= getWidth();
        if ((drawFlags&GraphicsAdapter.HCENTER)!=0)
            x -= getWidth()/2;
        if ((drawFlags&GraphicsAdapter.BOTTOM)!=0)
            y -= getHeight();
        if ((drawFlags&GraphicsAdapter.VCENTER)!=0)
            y -= getHeight()/2;
        
        //Save memory
        if (hasExpanded)
        	contract();
        
        //Draw the background
        if (fillType==FILL_SOLID) {
        	GraphicsAdapter.setColor(bgColor);
            GraphicsAdapter.fillRect(x, y, getWidth(), getHeight());
        }
        
        //Draw the pixel buffer
        if (pixelBuffer!=null) {
        	GraphicsAdapter.drawRGB(pixelBuffer, 0, pixelBufferSize[WIDTH], x+pixelBufferSize[X], y+pixelBufferSize[Y], pixelBufferSize[WIDTH], pixelBufferSize[WIDTH], true);
        }
        
        //Draw the borders as ever-decreasing rectangles.
        for (int i=0; i<borderColors.length; i++) {
            GraphicsAdapter.setColor(borderColors[i]);
            GraphicsAdapter.drawRect(x+i, y+i, getWidth()-2*i, getHeight()-2*i);
        }
    }
    
    
    private void contract() {
    	//Shrink our pixel buffer to avoid storing transparent pixels
    	
    	
    	hasExpanded = false;
    }
    
    
    
    
    ///////////////////////////////////
    // Properties
    ///////////////////////////////////
    public void setPosition(int x, int y) { rectangle[X] = x; rectangle[Y] = y; }
    public void setLayoutRule(int rule) { this.layoutRule = rule; }
    public int getLayoutRule() { return layoutRule; }
    public int getPosX() { return rectangle[X]; }
    public int getPosY() { return rectangle[Y]; }
    public int getWidth() { return rectangle[WIDTH]; }
    public int getHeight() { return rectangle[HEIGHT]; }
    
    //Special property
    private void setSize(int w, int h) { 
    	//Set size
    	rectangle[WIDTH] = w;
    	rectangle[HEIGHT] = h;
    	
    	//Copy buffer over...
    	if (fillType==FILL_TRANSPARENT) {
    		pixelBuffer = new int[getWidth()*getHeight()];
    		for (int i=0; i<pixelBuffer.length; i++)
    			pixelBuffer[i] = bgColor;
    	}
    }
    
	

}
