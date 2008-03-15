package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.game.LiteException;

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
	public static final int FILL_TRANSLUCENT = 2;
	public static final int FILL_GUESS = 3;
	
	//Properties common to all canvases
	private int[] rectangle;
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
    
    public Canvas(int bgColor, int[] borderColors, int fillType) {
    	this(-1, -1, bgColor, borderColors, fillType);
    }
    
    /**
     * Create a surface with a certain background color and some border lines.
     * @width, height MUST assume that "borderColors.length"*2 pixels for the borders
     * @param bgColor AARRGGBB format; the background for the box. Fills the entire box.
     * @param borderColors An array of RRGGBB values to be applies, from outer to inner, to the box.
     * @param fillType NONE or SOLID are the best; TRANSLUCENT uses a lot of memory.
     */
    public Canvas(int width, int height, int bgColor, int[] borderColors, int fillType) {
    	this.bgColor = bgColor;
    	this.borderColors = borderColors;
    	this.rectangle = new int[]{0,0,0,0};
    	
    	if (fillType==FILL_GUESS) {
    		//We are asking the system to try and guess the fill type for us, from the argument sent...
    		//IMPORTANT: This assumes that there IS an alpha channel.
    		if ((bgColor&0xFF000000)==0)
    			this.fillType = FILL_NONE;
    		else if ((bgColor&0xFF000000)==0xFF000000)
    			this.fillType = FILL_SOLID;
    		else
    			this.fillType = FILL_TRANSLUCENT;
    	} else
    		this.fillType = fillType;
    	
    	if (width!=-1 && height!=-1)
    		this.setSize(width, height);
    	this.hasExpanded = false;
    }
    
    
    public void setPixel(int x, int y, int colorRGB) {
    	setPixel(y*getWidth()+x, colorRGB);
    }
    
    public void setPixel(int offset, int colorRGB) {
    	if (!hasExpanded)
    		expand();
    	pixelBuffer[offset] = 0xFF000000|colorRGB;
    }
   
    
    private void expand() {
  //  	System.out.println("Expand");
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
    	
  //  	System.out.println("end expand");

    	hasExpanded = true;
    }
    
    
    public void paint() {
    	paint(rectangle[X], rectangle[Y], layoutRule);
    }
    
    
    /**
     * "Draw flags" contains the Graphics flags. 
     */
    public void paint(int x, int y, int drawFlags) {
    	System.out.println("Paint");
    	//Transform based on the draw flags
    	int[] TL = getTopLeftCorner(x, y, drawFlags);
    	x = TL[X];
    	y = TL[Y];
    	
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
        System.out.println("Paint Done");
    }
    
    
    
    private int crop(int start, int incr, boolean isX) throws ArrayIndexOutOfBoundsException {
    	//Just count up/down until we overflow the array. :) 
    	for (;;) {
    		//Check this line for non-transparent pixels.
    		if (isX) {
    			//Check this line -vert
    			for (int currY=0; currY<getHeight(); currY++) {
    				if ((pixelBuffer[currY*getWidth()+start]&0xFF000000)!=0xFF000000)
    					return start;
    			}
    		} else {
    			//Check this line -horiz
    			for (int currX=0; currX<getWidth(); currX++) {
    				if ((pixelBuffer[start*getWidth()+currX]&0xFF000000)!=0xFF000000)
    					return start;
    			}
    		}
    		
    		//Increment/Decrement
    		start += incr;
    	}
    }
    
    
    
    private void contract() {
    //	System.out.println("contract");
    	//Shrink our pixel buffer to avoid storing transparent pixels
    	if (pixelBuffer!=null && (fillType==FILL_SOLID || fillType==FILL_NONE)) {
    		try {
    			//Re-compute boundaries
    			int xMin = crop(0, 1, true); 
    			int xMax = crop(getWidth()-1, -1, true);
    			int yMin = crop(0, 1, false); 
    			int yMax = crop(getHeight()-1, -1, false);
    			
    			//It's possible nothing's changed...
    			if (xMin!=0 || xMax!=getWidth()-1 || yMin!=0 || yMax!=getHeight()-1) {
    				//Else, copy over the entire array
    				pixelBufferSize = new int[]{xMin, yMin, xMax-xMin+1, yMax-yMin+1};
    				int[] newBuffer = new int[pixelBufferSize[HEIGHT]*pixelBufferSize[WIDTH]];
    				for (int destY=0; destY<pixelBufferSize[HEIGHT]; destY++) {
    					for (int destX=0; destX<pixelBufferSize[WIDTH]; destX++) {
    						int srcX = destX+pixelBufferSize[X];
    						int srcY = destY+pixelBufferSize[Y];
    						newBuffer[destY*pixelBufferSize[WIDTH]+destX] = pixelBuffer[srcY*getWidth()+srcX];
    					}
    				}
    			}
    		} catch (ArrayIndexOutOfBoundsException ex) {
    			//Special case: Nothing left!
        		pixelBuffer = null;
    		}
    	}
    //	System.out.println("end contract");
    	
    	hasExpanded = false;
    }
    
    
    public int[] getTopLeftCorner() {
        return getTopLeftCorner(rectangle[X], rectangle[Y], layoutRule);
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
    public void setPosition(int x, int y) { rectangle[X] = x; rectangle[Y] = y; }
    public void setLayoutRule(int rule) { this.layoutRule = rule; }
    public int getLayoutRule() { return layoutRule; }
    public int getPosX() { return rectangle[X]; }
    public int getPosY() { return rectangle[Y]; }
    public int getWidth() { return rectangle[WIDTH]; }
    public int getHeight() { return rectangle[HEIGHT]; }
    
    /**
     * Handle with care
     */
    public void setSize(int w, int h) { 
    	//Set size
    	rectangle[WIDTH] = w;
    	rectangle[HEIGHT] = h;
    	
    	//Copy buffer over...
    	if (fillType==FILL_TRANSLUCENT) {   		
    		pixelBuffer = new int[getWidth()*getHeight()];
    		for (int i=0; i<pixelBuffer.length; i++)
    			pixelBuffer[i] = bgColor;
    	}
    }
    
	

}
