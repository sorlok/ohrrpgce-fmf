package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.game.LiteException;

/**
 * The smallest unit of menu design. A Slice can have a border, a background, and 
 *  a directly-drawn-to area. It also incorporates several layout-related features.
 * @author Seth N. Hetu
 */
public class MenuSlice {
	//Internal Static Variables
	private static final int X = 0;
	private static final int Y = 1;
	private static final int WIDTH = 2;
	private static final int HEIGHT = 3;
	
	//External Static variables: FILL
	public static final int FILL_NONE = 0;
	public static final int FILL_SOLID = 1;
	public static final int FILL_TRANSLUCENT = 2;
	public static final int FILL_GUESS = 3;
	
	//External Static variables: CONNECTions
    public static final int CONNECT_TOP = 0;
    public static final int CONNECT_BOTTOM = 1;
    public static final int CONNECT_LEFT = 2;
    public static final int CONNECT_RIGHT = 3;
    public static final int CONNECT_HORIZONTAL = 0;
    public static final int CONNECT_VERTICAL = 1;
    
    
    //We store a lot of the Slice's information in a MenuFormatArgs
    private MenuFormatArgs mFormat;
    
    //Directly drawn-to area.
    private int[] pixelBuffer;
    private boolean hasExpanded;
    private int[] pixelBufferSize = new int[]{0,0,0,0};
    
    //Tooltip
    private String helperText;
    
    //What it's connected to, for painting purposes and for actual control
    private MenuSlice[] paintConnect = new MenuSlice[4];
    private MenuSlice[] commandConnect = new MenuSlice[4];
    private boolean[] paintBlock = new boolean[]{false, false, false, false};
    
    //Event Listeners help our menu interract with the outside world
    private Action focusGainedListener;
    private Action focusLostListener;    //If this returns "false", then don't move!
    private Action cancelListener;
    private Action acceptListener;
    
    //User-specific, a la SWT
    private Object data;
    
    
    /**
     * Construct a new Menu Slice
     * @param mFormat Deep copy performed; feel free to reuse this object.
     */
    public MenuSlice(MenuFormatArgs mFormat) {
    	this.mFormat = new MenuFormatArgs(mFormat);
        
		//A fill type of GUESS implies we are asking the system to try and 
    	//  guess the fill type for us, from the argument sent...
		//IMPORTANT: This assumes that there IS an alpha channel.
    	if (mFormat.fillType==FILL_GUESS) {
    		if ((this.mFormat.bgColor&0xFF000000)==0)
    			this.mFormat.fillType = FILL_NONE;
    		else if ((this.mFormat.bgColor&0xFF000000)==0xFF000000)
    			this.mFormat.fillType = FILL_SOLID;
    		else
    			this.mFormat.fillType = FILL_TRANSLUCENT;
    	}
    	
    	/*if (width!=-1 && height!=-1)
    		this.setSize(width, height);*/
    	this.hasExpanded = false;
    }
    
    
    /**
     * Draw directly onto the surface of this Slice
     * @param x,y The location of the pixel to color.
     * @param colorRGB The pixel's RGB color value.
     */
    public void setPixel(int x, int y, int colorRGB) {
        x -= this.mFormat.borderColors.length;
        y -= this.mFormat.borderColors.length;
        if (!hasExpanded)
            expand();
        pixelBuffer[y*pixelBufferSize[WIDTH] + x] = 0xFF000000|colorRGB;
    }
    
    
    /**
     * Paint the Slice
     */
    public void paint() {
    	paint(getPosX(), getPosY(), this.mFormat.layoutRule);
    }
    
    /**
     * Paint the Slice. "Draw flags" contains the Graphics flags. 
     */
    public void paint(int x, int y, int drawFlags) {
    	//Transform based on the draw flags
    	int[] TL = getTopLeftCorner(x, y, drawFlags);
    	x = TL[X];
    	y = TL[Y];
    	
        //Save memory
        if (hasExpanded)
        	contract();
        
        //Draw the background
        if (this.mFormat.fillType==FILL_SOLID) {
        	GraphicsAdapter.setColor(this.mFormat.bgColor);
            GraphicsAdapter.fillRect(x, y, getWidth(), getHeight());
        }

        
        //Draw the pixel buffer
        if (pixelBuffer!=null) {
        	GraphicsAdapter.drawRGB(pixelBuffer, 0, pixelBufferSize[WIDTH], x+pixelBufferSize[X]+this.mFormat.borderColors.length, y+pixelBufferSize[Y]+this.mFormat.borderColors.length, pixelBufferSize[WIDTH], pixelBufferSize[HEIGHT], true);
        }

        //Draw the borders as ever-decreasing rectangles.
        for (int i=0; i<this.mFormat.borderColors.length; i++) {
            GraphicsAdapter.setColor(this.mFormat.borderColors[i]);
            GraphicsAdapter.drawRect(x+i, y+i, getWidth()-2*i, getHeight()-2*i);
        }
    }
    
    
    
    ///NOTES:
    /*
     * 
     *  MenuFormatArgs should re-name x/y/width/height to "xHint, yHint, widthHint, heightHint"
     *  rectangle[4] should be added and maintained.
     *  Add to MenuFormatArgs "connectedTo"
     *  Make xHint/widthHint relative, and rectangle[] absolute.
     *  Try: setSize(), connect() triggers re-calculation of component positions, etc. 
     * 
     * 
     * 
     * 
     * 
     */
    
    
    
    
    
    
    
    
    
    
    
    ///
    /// Expand the pixel buffer to fit the maximum width and height.
    ///
    private void expand() {
    	if (this.mFormat.fillType == FILL_NONE || this.mFormat.fillType == FILL_SOLID) {
    		//Create a new integer array
                int newW = getWidth()-this.mFormat.borderColors.length*2;
                int newH = getHeight()-this.mFormat.borderColors.length*2;
    		int[] newBuffer = new int[newW * newH];
    		
    		//Copy over the old one
        		if (pixelBuffer != null) {
        			for (int srcY=0; srcY<pixelBufferSize[HEIGHT]; srcY++) {
        				for (int srcX=0; srcX<pixelBufferSize[WIDTH]; srcX++) {
        					int destX = srcX + pixelBufferSize[X];
        					int destY = srcY + pixelBufferSize[Y];
        					newBuffer[destY*newW+destX] = pixelBuffer[srcY*pixelBufferSize[WIDTH]+srcX];
        				}
        			}
        		}
  
        		//Assign the buffer
    		pixelBuffer = newBuffer;
    		pixelBufferSize[X]= 0;
    		pixelBufferSize[Y]= 0;
    		pixelBufferSize[WIDTH] = newW;
    		pixelBufferSize[HEIGHT] = newH;
    	}

    	hasExpanded = true;
    }
    
    
    
    ///
    /// Shrink the pixel buffer to fit the required width and height.
    ///
    private void contract() {
    	//Shrink our pixel buffer to avoid storing transparent pixels
    	if (pixelBuffer!=null && (this.mFormat.fillType==FILL_SOLID || this.mFormat.fillType==FILL_NONE)) {
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
    	hasExpanded = false;
    }
    
    ///
    /// Helper method used by contract()
    ///
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
    
    ///
    /// Helper method used by paint()
    ///
    private int[] getTopLeftCorner() {
        return getTopLeftCorner(getPosX(), getPosY(), this.mFormat.layoutRule);
    }
    
    ///
    /// Helper method used by paint()
    ///
    private int[] getTopLeftCorner(int posX, int posY, int drawRule) {
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

}













