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
	protected static final int X = 0;
	protected static final int Y = 1;
	protected static final int WIDTH = 2;
	protected static final int HEIGHT = 3;
	private static final int MAX_LISTENERS = 5;
	
	//External Static variables: FILL
	public static final int FILL_NONE = 0;
	public static final int FILL_SOLID = 1;
	public static final int FILL_TRANSLUCENT = 2;
	public static final int FILL_GUESS = 3;
	
	//External Static variables: ConnectionFlags
	public static final int CFLAG_PAINT = 1;
	public static final int CFLAG_CONTROL = 2;
	
	//External Static variables: CONNECTions
    public static final int CONNECT_TOP = 0;
    public static final int CONNECT_BOTTOM = 1;
    public static final int CONNECT_LEFT = 2;
    public static final int CONNECT_RIGHT = 3;
    public static final int CONNECT_HORIZONTAL = 0;
    public static final int CONNECT_VERTICAL = 1;
    
    
    //We store a lot of the Slice's information in a MenuFormatArgs
    protected MenuFormatArgs mFormat;
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
    private MenuSlice currActiveChildMI;
    private MenuSlice parent;
    
    //Event Listeners help our menu interract with the outside world
    private Action[] focusGainedListeners = new Action[MAX_LISTENERS];
    private int numberOfFocusGainedListeners = 0;
    private Action focusLostListener;    //If this returns "false", then don't move!
    private Action cancelListener;
    private Action acceptListener;
    
    //User-specific, a la SWT
    private Object data;
    
    private boolean hasClip;
    private int[] clipRectangle = new int[]{0,0,0,0};
    
    
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
     * Warning: Modify the return value of this function 
     *  ONLY if you know what you're doing.
     */
    public MenuFormatArgs getInitialFormatArgs() {
    	return mFormat;
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
        
        try {
        	pixelBuffer[y*pixelBufferSize[WIDTH] + x] = 0xFF000000|colorRGB;
        } catch (ArrayIndexOutOfBoundsException ex) {
        	throw new LiteException(this, ex, "MenuSlice.setPixel() is out of bounds.");
        }
    }
    
    
    //Parts of painting....
    protected void drawPixelBuffer(int atX, int atY) {
    	if (pixelBuffer!=null)
    		GraphicsAdapter.drawRGB(pixelBuffer, 0, pixelBufferSize[WIDTH], atX, atY, pixelBufferSize[WIDTH], pixelBufferSize[HEIGHT], true);
    }
    
    
    
    
    /**
     * Helper method for drawing a menu slice at specific co-ordinates. Does NOT cascade the paint.
     * @param x,y location
     */
    public void paintAt(int x, int y) {
    	this.paintAt(x, y, false);
    }
    
    
    private void paintAt(int x, int y, boolean paintInner) {
        //Save memory
        if (hasExpanded)
        	contract();
        
        //Clip
        if (hasClip)
        	GraphicsAdapter.setClip(clipRectangle[X], clipRectangle[Y], clipRectangle[WIDTH], clipRectangle[HEIGHT]);
        
        //Draw the background
        if (this.mFormat.fillType==FILL_SOLID) {
        	GraphicsAdapter.setColor(this.mFormat.bgColor);
            GraphicsAdapter.fillRect(x, y, getWidth(), getHeight());
        }
        
        //Draw the pixel buffer
        drawPixelBuffer(x+pixelBufferSize[X]+this.mFormat.borderColors.length, y+pixelBufferSize[Y]+this.mFormat.borderColors.length);
        
        //Draw all inner components
        if (paintInner && topLeftChildMI!=null)
        	topLeftChildMI.paintMenuSlice(-1);

        //Draw the borders as ever-decreasing rectangles.
        for (int i=0; i<this.mFormat.borderColors.length; i++) {
            GraphicsAdapter.setColor(this.mFormat.borderColors[i]);
            GraphicsAdapter.drawRect(x+i, y+i, getWidth()-2*i-1, getHeight()-2*i-1);
        }
        
        //Un-clip
        if (hasClip)
        	GraphicsAdapter.resetClip();

    }
    
    
    
    /**
     * Paint this as a menu component; cascade the paint.
     * @param paintFromDir Set to -1 initially.
     */
    public void paintMenuSlice(int paintFromDir) {
    	//Keep this for now...
    	int x = getPosX();
    	int y = getPosY();
    	
    	paintAt(x, y, true);
        
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
    
    
    
    /**
     * Helper method: calls doHorizontalLayou() and then doVerticalLayout().
     *   If we allow vertical displays in the future, this may call doVerticalLayout()
     *   first.
     */
    public void doLayout() {
    	this.doHorizontalLayout(new Vector(), null, new Int(0));
    	this.doVerticalLayout(new Vector(), null, new Int(0));
    }
    

    
    public void doHorizontalLayout(Vector alreadyLaidOut, MenuSlice parentContainer, Int rightMostPoint) {
    	//Essentially, we need to figure out X, Y, WIDTH, and HEIGHT, given
    	//  our x/y/w/h "hints". Depending on the hints, various additional data
    	//  are needed.
    	
    	//First:
    	this.parent = parentContainer;
    	
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
    	int parentX = 0;
    	int parentBorderPadding = 0;
    	int parentNumBorders = 0;
    	if (parentContainer != null) {
    		parentX = parentContainer.getPosX();
    		parentBorderPadding = parentContainer.mFormat.borderPadding;
    		parentNumBorders = parentContainer.mFormat.borderColors.length;
    	}
    	if (alreadyLaidOut.isEmpty()) //Special case: first element
    		this.rectangle[X] = parentX + this.mFormat.xHint  + parentBorderPadding + parentNumBorders;
    	else {
    		//Relate to our last-painted component
    		int lastPaintXAnchor = 0;
    		if (lastPaintedMI == null) {
    			lastPaintXAnchor = parentX + parentNumBorders;
    			if ((this.mFormat.fromAnchor&GraphicsAdapter.LEFT)!=0)
    				lastPaintXAnchor += parentBorderPadding;
    			else { 
    				if (parentContainer==null)
    					throw new LiteException(this, null, "A parent with WIDTH_MINIMUM can ONLY have a topLeftChildMI at the LEFT.");
    				
    				if ((this.mFormat.fromAnchor&GraphicsAdapter.HCENTER)!=0)
    					lastPaintXAnchor += parentContainer.getWidth()/2;
    				else if ((this.mFormat.fromAnchor&GraphicsAdapter.RIGHT)!=0)
    					lastPaintXAnchor += parentContainer.getWidth();
    			}
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
    	
    	//Layout's done for this object
    	alreadyLaidOut.add(this);
    	if (this.getPosX()+this.getWidth() > rightMostPoint.getValue())
    		rightMostPoint.setValue(this.getPosX()+this.getWidth());
    	
    	//Continue layout for child objects
    	if (this.topLeftChildMI!=null && !alreadyLaidOut.contains(this.topLeftChildMI))
    		this.topLeftChildMI.doHorizontalLayout(alreadyLaidOut, this, rightMostPoint);
    	
    	//Continue layout for remaining objects
    	for (int i=0; i<paintConnect.length; i++) {
    		//Don't paint in loops!
    		if (i==dirToLastPaintedMI || paintConnect[i]==null)
    			continue;
    		
    		//Continue calculating
    		paintConnect[i].doHorizontalLayout(alreadyLaidOut, parentContainer, rightMostPoint);
    	}
    }
    
    
    private int calcWidth(Vector alreadyLaidOut, MenuSlice lastPaintedMI, int dirToLastPaintedMI, MenuSlice parentContainer) {
    	//Easy: already set for us
    	if (this.mFormat.widthHint > 0 ) {
    		return this.mFormat.widthHint; 
    	} else if (this.mFormat.widthHint == MenuFormatArgs.WIDTH_MINIMUM) {
    		//Minimum width - mostly only makes sense for group controls
    		if (topLeftChildMI==null) {
    			//Last save chance: a component may define minimum width itself.
    			int minWidth = calcMinWidth();
    			if (minWidth<0)
    				throw new LiteException(this, new IllegalArgumentException(), "Width hint MINIMUM doesn't make sense if there are no children.");
    			return minWidth + this.mFormat.borderColors.length*2 + this.mFormat.borderPadding*2;
    		}
    		
    		//We need to calculate all internal widths first.
    		Int newWidth = new Int(0);
    		this.topLeftChildMI.doHorizontalLayout(alreadyLaidOut, this, newWidth);
    		
    		//Now, get the right-most point and return it.
    		return newWidth.getValue() - this.getPosX() + this.mFormat.borderPadding + this.mFormat.borderColors.length;
    	} else if (this.mFormat.widthHint == MenuFormatArgs.WIDTH_MAXIMUM) {
    		//First...
    		if (parentContainer==null)
    			throw new LiteException(this, new IllegalArgumentException(), "Null parent for child layout of MAXIMUM width.");
    		else if (!alreadyLaidOut.contains(parentContainer))
    			throw new LiteException(this, new IllegalArgumentException(), "Child layout of MAXIMUM width attempted with parent demanding MINIMUM width.");
    		
    		//Maximum width makes sense in only three cases:
    		//  1) You're the only MI
    		//  2) You're left-connected to the right edge
    		//  3) You're right-connected to the left edge
    		if (lastPaintedMI == null)
    			return parentContainer.getWidth() - 2*parentContainer.mFormat.borderPadding - 2*parentContainer.mFormat.borderColors.length;
    		else if (((this.mFormat.fromAnchor&GraphicsAdapter.RIGHT)!=0) && ((this.mFormat.toAnchor&GraphicsAdapter.LEFT)!=0))
    			return parentContainer.getWidth() - (lastPaintedMI.getPosX()-parentContainer.getPosX() + lastPaintedMI.getWidth()) - parentContainer.mFormat.borderPadding - parentContainer.mFormat.borderColors.length;
    		else if (((this.mFormat.fromAnchor&GraphicsAdapter.LEFT)!=0) && ((this.mFormat.toAnchor&GraphicsAdapter.RIGHT)!=0))
    			return lastPaintedMI.getPosX() - parentContainer.getPosX() - parentContainer.mFormat.borderPadding - parentContainer.mFormat.borderColors.length;
    		else
    			throw new LiteException(this, new IllegalArgumentException(), "Invalid connect fromAnchor("+this.mFormat.fromAnchor+") and toAnchor("+this.mFormat.toAnchor+") for MAX_WIDTH");
    	} else
    		throw new LiteException(this, new IllegalArgumentException(), "Invalid width hint: " + this.mFormat.widthHint);
    }
    
    
    
    public void doVerticalLayout(Vector alreadyLaidOut, MenuSlice parentContainer, Int lowerMostPoint) {
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
    	
    	//Set our Y co-ordinate
    	int calcdHeight = -1;
    	int parentY = 0;
    	int parentBorderPadding = 0;
    	int parentNumBorders = 0;
    	if (parentContainer != null) {
    		parentY = parentContainer.getPosY();
    		parentBorderPadding = parentContainer.mFormat.borderPadding;
    		parentNumBorders = parentContainer.mFormat.borderColors.length;
    	}
    	if (alreadyLaidOut.isEmpty()) //Special case: first element
    		this.rectangle[Y] = parentY + this.mFormat.yHint + parentBorderPadding + parentNumBorders;
    	else {
    		//Relate to our last-painted component
    		int lastPaintYAnchor = 0;
    		if (lastPaintedMI == null) {
    			lastPaintYAnchor = parentY + parentNumBorders;
    			if ((this.mFormat.fromAnchor&GraphicsAdapter.TOP)!=0)
    				lastPaintYAnchor += parentBorderPadding;
    			else {
    				if (parentContainer==null)
    					throw new LiteException(this, null, "A parent with HEIGHT_MINIMUM can ONLY have a topLeftChildMI at the TOP.");
    				
    				if ((this.mFormat.fromAnchor&GraphicsAdapter.VCENTER)!=0)
    					lastPaintYAnchor += parentContainer.getHeight()/2;
    				else if ((this.mFormat.fromAnchor&GraphicsAdapter.BOTTOM)!=0)
    					lastPaintYAnchor += parentContainer.getHeight();
    			}
    		} else {
    			lastPaintYAnchor = lastPaintedMI.getPosY();
    			if ((this.mFormat.fromAnchor&GraphicsAdapter.VCENTER)!=0)
    				lastPaintYAnchor +=  lastPaintedMI.getHeight()/2;
    			else if ((this.mFormat.fromAnchor&GraphicsAdapter.BOTTOM)!=0)
    				lastPaintYAnchor +=  lastPaintedMI.getHeight();
    		}
    		
    		//Now, set our Y
    		if ((this.mFormat.toAnchor&GraphicsAdapter.TOP)!=0)
    			this.rectangle[Y] = lastPaintYAnchor + this.mFormat.yHint;
    		else {
    			//We need to know the height of our component to set in this fashion....
    			// We need to be careful what situations we let ourselves get into here.
    			calcdHeight = calcHeight(alreadyLaidOut, lastPaintedMI, dirToLastPaintedMI, parentContainer);
    			
    			//Continue setting
    			if ((this.mFormat.toAnchor&GraphicsAdapter.VCENTER)!=0)
    				this.rectangle[Y] = lastPaintYAnchor - calcdHeight/2 + this.mFormat.yHint;
    			else if ((this.mFormat.toAnchor&GraphicsAdapter.BOTTOM)!=0)
    				this.rectangle[Y] = lastPaintYAnchor - calcdHeight + this.mFormat.yHint;
    		}
    	}
    	
    	//Set our height, if it hasn't already been set.
    	if (calcdHeight==-1)
    		calcdHeight = calcHeight(alreadyLaidOut, lastPaintedMI, dirToLastPaintedMI, parentContainer);
    	
    	//Set height
    	this.setHeight(calcdHeight);
    	
    	//Layout's done for this object
    	alreadyLaidOut.add(this);
    	if (this.getPosY()+this.getHeight() > lowerMostPoint.getValue())
    		lowerMostPoint.setValue(this.getPosY()+this.getHeight());
    	
    	//Continue layout for child objects
    	if (this.topLeftChildMI!=null && !alreadyLaidOut.contains(this.topLeftChildMI))
    		this.topLeftChildMI.doVerticalLayout(alreadyLaidOut, this, lowerMostPoint);
    	
    	//Continue layout for remaining objects
    	for (int i=0; i<paintConnect.length; i++) {
    		//Don't paint in loops!
    		if (i==dirToLastPaintedMI || paintConnect[i]==null)
    			continue;
    		
    		//Continue calculating
    		paintConnect[i].doVerticalLayout(alreadyLaidOut, parentContainer, lowerMostPoint);
    	}
    }
    
    
    private int calcHeight(Vector alreadyLaidOut, MenuSlice lastPaintedMI, int dirToLastPaintedMI, MenuSlice parentContainer) {
    	//Easy: already set for us
    	if (this.mFormat.heightHint > 0 ) {
    		return this.mFormat.heightHint; 
    	} else if (this.mFormat.heightHint == MenuFormatArgs.HEIGHT_MINIMUM) {
    		//Minimum height - mostly only makes sense for group controls
    		if (topLeftChildMI==null) {
    			//Last save chance: a component may define minimum height itself.
    			int minHeight = calcMinHeight();
    			if (minHeight<0)
    				throw new LiteException(this, new IllegalArgumentException(), "Height hint MINIMUM doesn't make sense if there are no children.");
    			
    			return minHeight + this.mFormat.borderColors.length*2 + this.mFormat.borderPadding*2;
    		}
    		
    		//We need to calculate all internal heights first.
    		Int newHeight = new Int(0);
    		this.topLeftChildMI.doVerticalLayout(alreadyLaidOut, this, newHeight);
    			
    		//Now, get the right-most point and return it.
    		return newHeight.getValue() - this.getPosY() + this.mFormat.borderPadding + this.mFormat.borderColors.length;
    	} else if (this.mFormat.heightHint == MenuFormatArgs.HEIGHT_MAXIMUM) {
    		//First...
    		if (parentContainer==null)
    			throw new LiteException(this, new IllegalArgumentException(), "Null parent for child layout of MAXIMUM height.");
    		else if (!alreadyLaidOut.contains(parentContainer))
    			throw new LiteException(this, new IllegalArgumentException(), "Child layout of MAXIMUM height attempted with parent demanding MINIMUM height.");
    		
    		//Maximum height makes sense in only three cases:
    		//  1) You're the only MI
    		//  2) You're top-connected to the bottom edge
    		//  3) You're bottom-connected to the top edge
    		if (lastPaintedMI == null)
    			return parentContainer.getHeight() - 2*parentContainer.mFormat.borderPadding - 2*parentContainer.mFormat.borderColors.length;
    		else if (((this.mFormat.fromAnchor&GraphicsAdapter.BOTTOM)!=0) && ((this.mFormat.toAnchor&GraphicsAdapter.TOP)!=0))
    			return parentContainer.getHeight() - (lastPaintedMI.getPosY()-parentContainer.getPosY() + lastPaintedMI.getHeight())- parentContainer.mFormat.borderPadding - parentContainer.mFormat.borderColors.length;
    		else if (((this.mFormat.fromAnchor&GraphicsAdapter.TOP)!=0) && ((this.mFormat.toAnchor&GraphicsAdapter.BOTTOM)!=0))
    			return lastPaintedMI.getPosY() - parentContainer.getPosY()- parentContainer.mFormat.borderPadding - parentContainer.mFormat.borderColors.length;
    		else
    			throw new LiteException(this, new IllegalArgumentException(), "Invalid connect fromAnchor("+this.mFormat.fromAnchor+") and toAnchor("+this.mFormat.toAnchor+") for MAX_HEIGHT");
    	} else
    		throw new LiteException(this, new IllegalArgumentException(), "Invalid height hint: " + this.mFormat.heightHint);
    }
    
    

    
    /**
     * Connect a second item to this menu item.
     * @param secondary The item to connect to this.
     * @param connectOn Either of CONNECT_TOP/BOTTOM/LEFT/RIGHT.
     * @param flags Any combination of CFLAG_PAINT, CFLAG_CONTROL 
     */
    public void connect(MenuSlice secondary, int connectOn, int flags) {
        int converse = MenuSlice.inverseDir(connectOn);
        
        //Command connect:
        if ((flags&MenuSlice.CFLAG_CONTROL)!=0) {
        	//Are these slots actually free?
        	if (commandConnect[connectOn]!=null || secondary.commandConnect[converse]!=null)
        		throw new LiteException(this, new IllegalArgumentException(), "MenuItem cannot be COMMAND connected: An item is already connected on: " + connectOn  + " (or: " + converse + ")");

        	//Connection applies to both objects.
            this.commandConnect[connectOn] = secondary;
            secondary.commandConnect[converse] = this;
            
            //Debug
            //System.out.println("connected from["+connectOn+"]: " + this.getClass().getName().replaceAll(".*\\.", "") + "  to["+converse+"]  " + secondary.getClass().getName().replaceAll(".*\\.", ""));
        }
        		
        //Paint connect:		
        if ((flags&MenuSlice.CFLAG_PAINT)!=0) {
        	//Are these slots actually free?
        	if (paintConnect[connectOn]!=null || secondary.paintConnect[converse]!=null)
        		throw new LiteException(this, new IllegalArgumentException(), "MenuItem cannot be PAINT connected: An item is already connected on: " + connectOn  + " (or: " + converse + ")");
        	
        	//Connection applies to both objects.
            this.paintConnect[connectOn] = secondary;
            secondary.paintConnect[converse] = this;
        }
    }
    
    
    /**
     * Intended only to be used for animation. Does not persist beyond the next doLayout()
     * @param x,y in screen co-ordinates
     */
    public void forceToLocation(int x, int y) {
    	this.rectangle[X] = x;
    	this.rectangle[Y] = y;
    }
    
    
    /**
     * Disconnect menu items.
     * @param disconOn
     * @param flags Any combination of CFLAG_PAINT, CFLAG_CONTROL
     */
    public void disconnect(int disconOn, int flags) {
        int converse = MenuSlice.inverseDir(disconOn);
        
        //Command connect:
        if ((flags&MenuSlice.CFLAG_CONTROL)!=0) {
            if (commandConnect[disconOn]==null || commandConnect[disconOn].commandConnect[converse]==null)
            	throw new LiteException(this, new IllegalArgumentException(), "MenuItem cannot be COMMAND disconnected: no item connected to: " + disconOn + " (or: " + converse + ")");
            commandConnect[disconOn].commandConnect[converse] = null;
            commandConnect[disconOn] = null;
        }
        
        //Paint connect:
        if ((flags&MenuSlice.CFLAG_PAINT)!=0) {
            if (paintConnect[disconOn]==null || paintConnect[disconOn].paintConnect[converse]==null)
            	throw new LiteException(this, new IllegalArgumentException(), "MenuItem cannot be PAINT disconnected: no item connected to: " + disconOn + " (or: " + converse + ")");
            paintConnect[disconOn].paintConnect[converse] = null;
            paintConnect[disconOn] = null;
        }
    }
    
    public MenuSlice getConnect(int dir, int flags) {    
        //Command connect:
        if ((flags&MenuSlice.CFLAG_CONTROL)!=0) {
        	return commandConnect[dir];
        }
        
        //Paint connect:
        if ((flags&MenuSlice.CFLAG_PAINT)!=0) {
            return paintConnect[dir];
        }
        
        return null;
    }

    
    public void setTopLeftChild(MenuSlice child) {
    	this.topLeftChildMI = child;
    }
    
    public MenuSlice getTopLeftChild() {
    	return this.topLeftChildMI;
    }
    
    /**
     * Called when width is minimum and there're no internal components.
     *   NOTE: Do _not_ figure in borders or padding, this will be added later.
     * @return -1 to throw an exception, or the minimum width if it's known
     */
    protected int calcMinWidth() {
    	return -1;
    }
    
    /**
     * Called when height is minimum and there're no internal components.
     *   NOTE: Do _not_ figure in borders or padding, this will be added later.
     * @return -1 to throw an exception, or the minimum height if it's known
     */
    protected int calcMinHeight() {
    	return -1;
    }
    
    
    /**
     * Removes all drawn pixels from the buffer
     */
    public void clearPixelBuffer() {
    	hasExpanded = false;
    	pixelBuffer = null;
    	
    	if (this.mFormat.fillType == FILL_TRANSLUCENT) 
    		resizeAlphaBkgrd();
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
    public void setData(Object data) {
    	this.data = data;
    }
    public Object getData() {
    	return this.data;
    }
    
    
    //Special properties
    protected void setWidth(int newWidth) {
    	rectangle[WIDTH] = newWidth;
    	
    	if (this.mFormat.fillType == FILL_TRANSLUCENT) 
    		resizeAlphaBkgrd();
    }
    
    protected void setHeight(int newHeight) {
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
    
    
    
    
    //////////////////////////
    // Control Properties
    //////////////////////////
    
    public boolean accept() {
    	//Always delegate to inner child elements
    	if (currActiveChildMI==null)
    		currActiveChildMI = topLeftChildMI;
    	if (currActiveChildMI!=null && currActiveChildMI.accept())
    		return true;
    	
    	if (this.acceptListener!=null)
    		return acceptListener.perform(this);
    	
    	return false;
    }
    
    public boolean cancel() {
    	//Always delegate to inner child elements
    	if (currActiveChildMI==null)
    		currActiveChildMI = topLeftChildMI;
    	if (currActiveChildMI!=null && currActiveChildMI.cancel())
    		return true;
    	
    	if (this.cancelListener!=null)
    		return cancelListener.perform(this);
    	
    	return false;
    }
    
    
    /**
     * A MenuItem responds when keys are pressed.
     * Calls the "processInput" functions for the focussed element first.
     * @param direction The key that was pressed. This must already be pre-processed.
     * @return true if input was processed, false otherwise.
     */
    public boolean processInput(int direction) {
    	//Always delegate to inner child elements
    	if (currActiveChildMI==null)
    		currActiveChildMI = topLeftChildMI;
    	if (currActiveChildMI!=null && currActiveChildMI.processInput(direction))
    		return true;
    	
    	//First, check if we should continue..
    	if (consumeInput(direction))
    		return true;
    	
    	//Is there anything to process?
        if (commandConnect[direction]==null)
            return false;
        
        //Ok, we need to fire off a focus-lost listener, and also a focus-gained one...
        if (this.focusLostListener != null) {
        	//But, don't move unless the focus lost listener returns true.
        	if (!focusLostListener.perform(this))
        		return true;
        }
        
        //Move, and fire the focus-gained listener
        //System.out.println("moving from["+direction+"]: " + this.getClass().getName().replaceAll(".*\\.", "") + "  to  " + commandConnect[direction].getClass().getName().replaceAll(".*\\.", ""));
        commandConnect[direction].moveTo();
        return true;
    }
    
    public void moveTo() {
    	//Set active, cascade this to all parent components.
    	MenuSlice currParent = this;
    	MenuSlice prevParent = this.parent;
    	while (prevParent != null) {
    		prevParent.currActiveChildMI = currParent;
    		currParent = prevParent;
    		prevParent = prevParent.parent;
    	}
    	
    	//Move to child..
    	if (currActiveChildMI==null)
    		currActiveChildMI = topLeftChildMI;
    	if (currActiveChildMI!=null)
    		currActiveChildMI.moveTo();
    	
    	//Listeners...
    	for (int i=0; i<numberOfFocusGainedListeners; i++)
    		this.focusGainedListeners[i].perform(this);
    }

    
    public MenuSlice getCurrActiveChild() {
    	return currActiveChildMI;
    }
    
    
    
    /**
     * Intended to be over-ridden by complex sub-components
     * @return "true" if standard menu control should NOT be processed.
     */
    public boolean consumeInput(int direction) {
    	return false;
    }
    
    
    //Listeners
    public void setFocusLostListener(Action listener) {
    	this.focusLostListener = listener;
    }
    public void addFocusGainedListener(Action listener) {
    	if (numberOfFocusGainedListeners==MAX_LISTENERS)
    		throw new LiteException(this, null, "Maximum number of listeners("+MAX_LISTENERS+") exceeded.");
    	focusGainedListeners[numberOfFocusGainedListeners++] = listener;
    }
    public void setCancelListener(Action listener) {
    	this.cancelListener = listener;
    }
    public void setAcceptListener(Action listener) {
    	this.acceptListener = listener;
    }
    
    
    public void setClip(int x, int y, int w, int h) {
    	hasClip = true;
    	clipRectangle[X] = x;
    	clipRectangle[Y] = y;
    	clipRectangle[WIDTH] = w;
    	clipRectangle[HEIGHT] = h;
    }
    
    
    public void setClip(int[] bounds) {
    	if (bounds == null) {
    		hasClip = false;
    	} else {
    		hasClip = true;
        	clipRectangle[X] = bounds[X];
        	clipRectangle[Y] = bounds[Y];
        	clipRectangle[WIDTH] = bounds[WIDTH];
        	clipRectangle[HEIGHT] = bounds[HEIGHT];
    	}
    }
    
    

}













