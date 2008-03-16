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
    	
       // System.out.println("  New canvas: " + fillType);
        
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
        x -= borderColors.length;
        y -= borderColors.length;
        if (!hasExpanded)
            expand();
        pixelBuffer[y*pixelBufferSize[WIDTH] + x] = 0xFF000000|colorRGB;
    }
    
    /*public void setPixel(int offset, int colorRGB) {

    	
    }*/
   
    
    private void expand() {
    //	System.out.println("  Expand" );
    	if (fillType == FILL_NONE || fillType == FILL_SOLID) {
    		//Create a new integer array
                int newW = getWidth()-borderColors.length*2;
                int newH = getHeight()-borderColors.length*2;
    		int[] newBuffer = new int[newW * newH];
    		
    		//Copy over the old one
    		if (pixelBuffer != null) {
           //         System.out.println("  Copy buffer");
    			for (int srcY=0; srcY<pixelBufferSize[HEIGHT]; srcY++) {
    				for (int srcX=0; srcX<pixelBufferSize[WIDTH]; srcX++) {
    					int destX = srcX + pixelBufferSize[X];
    					int destY = srcY + pixelBufferSize[Y];
    					newBuffer[destY*newW+destX] = pixelBuffer[srcY*pixelBufferSize[WIDTH]+srcX];
    				}
    			}
    		}
    		
           //     System.out.println("  Buffer size: " + newW + "," + newH);
                
    		//Assign the buffer
    		pixelBuffer = newBuffer;
    		pixelBufferSize[X]= 0;
    		pixelBufferSize[Y]= 0;
    		pixelBufferSize[WIDTH] = newW;
    		pixelBufferSize[HEIGHT] = newH;
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
  //  	System.out.println("Paint");
    	//Transform based on the draw flags
    	int[] TL = getTopLeftCorner(x, y, drawFlags);
    	x = TL[X];
    	y = TL[Y];
    	
        //Save memory
        if (hasExpanded)
        	contract();
        
        //Draw the background
  //      System.out.println("background");
        if (fillType==FILL_SOLID) {
        	GraphicsAdapter.setColor(bgColor);
            GraphicsAdapter.fillRect(x, y, getWidth(), getHeight());
        }

        
        //Draw the pixel buffer
       // System.out.println("pixbuf: " + (fillType==FILL_TRANSLUCENT));
        if (pixelBuffer!=null) {
            //GraphicsAdapter.setColor(0xFF0000);
           // GraphicsAdapter.drawRect(x+pixelBufferSize[X]+borderColors.length, y+pixelBufferSize[Y]+borderColors.length, pixelBufferSize[WIDTH], pixelBufferSize[HEIGHT]);
        	GraphicsAdapter.drawRGB(pixelBuffer, 0, pixelBufferSize[WIDTH], x+pixelBufferSize[X]+borderColors.length, y+pixelBufferSize[Y]+borderColors.length, pixelBufferSize[WIDTH], pixelBufferSize[HEIGHT], true);
        }

        //Draw the borders as ever-decreasing rectangles.
       // System.out.println("border");
        for (int i=0; i<borderColors.length; i++) {
            GraphicsAdapter.setColor(borderColors[i]);
            GraphicsAdapter.drawRect(x+i, y+i, getWidth()-2*i, getHeight()-2*i);
        }
       // System.out.println("Paint Done");
    }
    
    
    
    private int crop(int start, int incr, boolean isX) throws ArrayIndexOutOfBoundsException {
    	//Just count up/down until we overflow the array. :) 
    	for (;;) {
    		//Check this line for non-clear pixels.
    		if (isX) {
    			//Check this line -vert
    			for (int currY=0; currY<pixelBufferSize[HEIGHT]; currY++) {
    				if ((pixelBuffer[currY*pixelBufferSize[WIDTH]+start]&0xFF000000)!=0)
    					return start;
    			}
    		} else {
    			//Check this line -horiz
    			for (int currX=0; currX<pixelBufferSize[WIDTH]; currX++) {
    				if ((pixelBuffer[start*pixelBufferSize[WIDTH]+currX]&0xFF000000)!=0)
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
                boolean doUpdate = false;
                int xMin=0; int xMax=0; int yMin=0; int yMax=0;
    		try {
    			//Re-compute boundaries
    			xMin = crop(0, 1, true); 
    			xMax = crop(pixelBufferSize[WIDTH]-1, -1, true);
    			yMin = crop(0, 1, false); 
    			yMax = crop(pixelBufferSize[HEIGHT]-1, -1, false);
                        doUpdate = true;
    		} catch (ArrayIndexOutOfBoundsException ex) {
    			//Special case: Nothing left!
        		pixelBuffer = null;
    		}
                
                if (doUpdate) {
    			//It's possible nothing's changed...
    			if (xMin!=0 || xMax!=getWidth()-1 || yMin!=0 || yMax!=getHeight()-1) {
                            try {
    				//Else, copy over the entire array
    				int newWidth = xMax-xMin+1; 
                                int newHeight = yMax-yMin+1;
    				int[] newBuffer = new int[newWidth*newHeight];
    				for (int destY=0; destY<newHeight; destY++) {
    					for (int destX=0; destX<newWidth; destX++) {
    						int srcX = destX+xMin;
    						int srcY = destY+yMin;
    						newBuffer[destY*newWidth+destX] = pixelBuffer[srcY*pixelBufferSize[WIDTH]+srcX];
    					}
    				}
    				pixelBuffer = newBuffer;
                                pixelBufferSize[X] = xMin;
                                pixelBufferSize[Y] = yMin;
                                pixelBufferSize[WIDTH] = newWidth;
                                pixelBufferSize[HEIGHT] = newHeight;
                            } catch (ArrayIndexOutOfBoundsException ex) {
                                throw new LiteException(this, ex, "Error copying old buffer on contract()");
                            }
    			}
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
    		pixelBufferSize[X] = 0;
    		pixelBufferSize[Y] = 0;
    		pixelBufferSize[WIDTH] = getWidth()-borderColors.length*2;
    		pixelBufferSize[HEIGHT] = getHeight()-borderColors.length*2;
                
    		pixelBuffer = new int[pixelBufferSize[WIDTH]*pixelBufferSize[HEIGHT]];
    		for (int i=0; i<pixelBuffer.length; i++)
    			pixelBuffer[i] = bgColor;
    	}
    }
    
	

}
