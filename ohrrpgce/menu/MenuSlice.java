package ohrrpgce.menu;

import java.util.Vector;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.game.LiteException;
import ohrrpgce.henceforth.Int;

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
    private int[] rectangle = new int[]{0,0,0,0};
    
    //Directly drawn-to area.
    private int[] pixelBuffer;
    private boolean hasExpanded;
    private int[] pixelBufferSize = new int[]{0,0,0,0};
    
    //Tooltip
    private String helperText;
    
    //What it's connected to, for painting purposes and for actual control
    private MenuSlice[] paintConnect = new MenuSlice[4];
    private MenuSlice[] commandConnect = new MenuSlice[4];
    private MenuSlice topLeftChildMI;
    
    //Event Listeners help our menu interract with the outside world
    private Action focusGainedListener;
    private Action focusLostListener;    //If this returns "false", then don't move!
    private Action cancelListener;
    private Action acceptListener;
    
    //User-specific, a la SWT
    private Object data;
    
    
    //Switch directions
    private static final int inverseDir(int dir) {
    	switch (dir) {
    		case CONNECT_TOP:
    			return CONNECT_BOTTOM;
    		case CONNECT_BOTTOM:
    			return CONNECT_TOP;
    		case CONNECT_LEFT:
    			return CONNECT_RIGHT;
    		case CONNECT_RIGHT:
    			return CONNECT_LEFT;
    		default:
    			throw new LiteException(MenuSlice.class, new IllegalArgumentException(), "Bad direction: " + dir);
    	}
    }
    
    
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
     * Paint this as a menu component; cascade the paint.
     * @param paintFromDir Set to -1 initially.
     */
    public void paintMenuSlice(int paintFromDir) {
    	//Keep this for now...
    	int x = getPosX();
    	int y = getPosY();
    	
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
        
        //Draw all inner components
        if (topLeftChildMI!=null)
        	topLeftChildMI.paintMenuSlice(-1);

        //Draw the borders as ever-decreasing rectangles.
        for (int i=0; i<this.mFormat.borderColors.length; i++) {
            GraphicsAdapter.setColor(this.mFormat.borderColors[i]);
            GraphicsAdapter.drawRect(x+i, y+i, getWidth()-2*i, getHeight()-2*i);
        }
        
        //Draw all connected components
        for (int i=0; i<paintConnect.length; i++) {
        	if (paintConnect[i]!=null && i!=paintFromDir)
        		paintConnect[i].paintMenuSlice(MenuSlice.inverseDir(i));
        }
    }
    
    
    
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
    

    
    public void doHorizontalLayout(Vector alreadyLaidOut, MenuSlice parentContainer, Int rightMostPoint) {
    	//Essentially, we need to figure out X, Y, WIDTH, and HEIGHT, given
    	//  our x/y/w/h "hints". Depending on the hints, various additional data
    	//  are needed.
    	
    	//Get the component we're connecting FROM
    	MenuSlice lastPaintedMI = null;
    	int dirToLastPaintedMI = -1;
    	for (int dir=0; dir<paintConnect.length; dir++) {
    		if (alreadyLaidOut.contains(paintConnect[dir])) {
    			lastPaintedMI = paintConnect[dir];
    			dirToLastPaintedMI = dir;
    			break;
    		}
    	}
    	
    	//Set our X co-ordinate
    	int calcdWidth = -1;
    	if (alreadyLaidOut.isEmpty()) //Special case: first element
    		this.rectangle[X] = parentContainer.getPosX() + this.mFormat.xHint;
    	else {
    		//Relate to our last-painted component
    		int lastPaintXAnchor = 0;
    		if (lastPaintedMI == null) {
    			lastPaintXAnchor = parentContainer.getPosX(); 
    		} else {
    			lastPaintXAnchor = lastPaintedMI.getPosX();
    			if ((this.mFormat.fromAnchor&GraphicsAdapter.HCENTER)!=0)
    				lastPaintXAnchor +=  lastPaintedMI.getWidth()/2;
    			else if ((this.mFormat.fromAnchor&GraphicsAdapter.RIGHT)!=0)
    				lastPaintXAnchor +=  lastPaintedMI.getWidth();
    		}
    		
    		//Now, set our X
    		if ((this.mFormat.toAnchor&GraphicsAdapter.LEFT)!=0)
    			this.rectangle[X] = lastPaintXAnchor + this.mFormat.xHint;
    		else {
    			//We need to know the width of our component to set in this fashion....
    			// We need to be careful what situations we let ourselves get into here.
    			calcdWidth = calcWidth(alreadyLaidOut, lastPaintedMI, dirToLastPaintedMI, parentContainer);
    			
    			//Continue setting
    			if ((this.mFormat.toAnchor&GraphicsAdapter.HCENTER)!=0)
    				this.rectangle[X] = lastPaintXAnchor - calcdWidth/2 + this.mFormat.xHint;
    			else if ((this.mFormat.toAnchor&GraphicsAdapter.RIGHT)!=0)
    				this.rectangle[X] = lastPaintXAnchor - calcdWidth + this.mFormat.xHint;
    		}
    	}
    	
    	//Set our width, if it hasn't already been set.
    	if (calcdWidth==-1)
    		calcdWidth = calcWidth(alreadyLaidOut, lastPaintedMI, dirToLastPaintedMI, parentContainer);
    	
    	//Set width
    	this.setWidth(calcdWidth);
    	
    	//Layout's done for this objects
    	alreadyLaidOut.add(this);
    	if (this.getPosX()+this.getWidth() > rightMostPoint.getValue())
    		rightMostPoint.setValue(this.getPosX()+this.getWidth());
    	
    	//Continue layout for remaining objects
    	for (int i=0; i<paintConnect.length; i++) {
    		//Don't paint in loops!
    		if (i==dirToLastPaintedMI)
    			continue;
    		
    		//Continue calculating
    		doHorizontalLayout(alreadyLaidOut, parentContainer, rightMostPoint);
    	}
    }
    
    
    private int calcWidth(Vector alreadyLaidOut, MenuSlice lastPaintedMI, int dirToLastPaintedMI, MenuSlice parentContainer) {
    	//Easy: already set for us
    	if (this.mFormat.widthHint > 0 ) {
    		return this.mFormat.widthHint; 
    	} else if (this.mFormat.widthHint == MenuFormatArgs.WIDTH_MINIMUM) {
    		//Minimum width - mostly only makes sense for group controls
    		if (topLeftChildMI==null) {
    			throw new LiteException(this, new IllegalArgumentException(), "Width hint MINIMUM doesn't make sense if there are no children.");
    		}
    		
    		//We need to calculate all internal widths first.
    		Int newWidth = new Int(0);
    		this.topLeftChildMI.doHorizontalLayout(alreadyLaidOut, this, newWidth);
    		
    		//Now, get the right-most point and return it.
    		return newWidth.getValue();
    	} else if (this.mFormat.widthHint == MenuFormatArgs.WIDTH_MAXIMUM) {
    		//First...
    		if (!alreadyLaidOut.contains(parentContainer))
    			throw new LiteException(this, new IllegalArgumentException(), "Child layout of MAXIMUM width attempted with parent demanding MINIMUM width.");
    		
    		//Maximum width makes sense in only three cases:
    		//  1) You're the only MI
    		//  2) You're left-connected to the right edge
    		//  3) You're right-connected to the left edge
    		if (lastPaintedMI == null)
    			return parentContainer.getWidth();
    		else if (((this.mFormat.fromAnchor&GraphicsAdapter.RIGHT)!=0) && ((this.mFormat.toAnchor&GraphicsAdapter.LEFT)!=0))
    			return parentContainer.getWidth() - (lastPaintedMI.getPosX()-parentContainer.getPosX() + lastPaintedMI.getWidth());
    		else if (((this.mFormat.fromAnchor&GraphicsAdapter.LEFT)!=0) && ((this.mFormat.toAnchor&GraphicsAdapter.RIGHT)!=0))
    			return lastPaintedMI.getPosX() - parentContainer.getPosX();
    		else
    			throw new LiteException(this, new IllegalArgumentException(), "Invalid connect fromAnchor("+this.mFormat.fromAnchor+") and toAnchor("+this.mFormat.toAnchor+") for MAX_WIDTH");
    	} else
    		throw new LiteException(this, new IllegalArgumentException(), "Invalid width hint: " + this.mFormat.widthHint);
    }
    

    
    
    //Properties: these require judicious calls to "doLayout()"
    
    public int getPosX() {
    	return rectangle[X];
    }
    public int getPosY() {
    	return rectangle[Y];
    }
    public int getWidth() {
    	return rectangle[WIDTH];
    }
    public int getHeight() {
    	return rectangle[HEIGHT];
    }
    
    
    //Special properties
    private void setWidth(int newWidth) {
    	rectangle[WIDTH] = newWidth;
    	
    	if (this.mFormat.fillType == FILL_TRANSLUCENT) 
    		resizeAlphaBkgrd();
    }
    
    private void setHeight(int newHeight) {
    	rectangle[HEIGHT] = newHeight;
    	
    	if (this.mFormat.fillType == FILL_TRANSLUCENT) 
    		resizeAlphaBkgrd();
    }
    
    
    //Helper
    private void resizeAlphaBkgrd() {
    	//Don't size invisible areas
    	if (getWidth()==0 || getHeight()==0)
    		return;
    	
    	//Ok, re-size and fill with the alpha background color.
		pixelBufferSize[X] = 0;
		pixelBufferSize[Y] = 0;
		pixelBufferSize[WIDTH] = getWidth()-this.mFormat.borderColors.length*2;
		pixelBufferSize[HEIGHT] = getHeight()-this.mFormat.borderColors.length*2;
		pixelBuffer = new int[pixelBufferSize[WIDTH]*pixelBufferSize[HEIGHT]];
		for (int i=0; i<pixelBuffer.length; i++)
			pixelBuffer[i] = this.mFormat.bgColor;
    }
    

}













