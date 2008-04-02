package ohrrpgce.menu.transitions;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.RPG;

/**
 * A simple transition that draws a bunch of circles over the screen, and slowly
 *   "drags down" the background menu color. 
 * @author Seth N. Hetu
 */
public class MenuInTransition extends Transition {
	//Useful constants
	private static final int boxesPerRow = 6;
    private static final int maxAngle = 360;
    private static final int halfTicks = 6; //Determines the speed of everything
    private static final int angleIncr = maxAngle/(halfTicks*2);
    private static final int angleHalfIncr = (maxAngle/2)/halfTicks;
	
    //Track our progress
    private int currTick;
    
    //Used internally
    private RPG currRPG;
    private int menuColor;
    private int width;
    private int height;
    private int boxSize;
    private int numBoxRows;
    private int lastTick;
    private int tideIncrement;
    
    public MenuInTransition(RPG currRPG, int canvasWidth, int canvasHeight) {
    	//Save for later
    	this.currRPG = currRPG;
    	this.width = canvasWidth;
    	this.height = canvasHeight;
    	
    	//Calculate
    	boxSize = canvasWidth/boxesPerRow; //Later, scroll left-to-right for horiz. displaysMath.min(canvasHeight, canvasWidth)/5;
    	numBoxRows = canvasHeight/boxSize;
    	menuColor = currRPG.getTextBoxColors(0)[0];
    	tideIncrement = boxSize/halfTicks;
    	lastTick = (height+boxSize/2)/tideIncrement;
    	
    	reset();
    }

	public void reset() {
		currTick = 0;
	}
    
	
	public boolean doPaintOver() {
		//We have two things going on here, all painting with the menu's background
		//  color. First, we are filling up circles from bottom-to-top; second, we 
		//  are slowly covering the screen with a large-moving rectangle. These
		//  things are synchronized.
        GraphicsAdapter.setColor(menuColor);
        
        //Draw the circles in THIS row -up to 180 degrees
        if (currTick < halfTicks*2) {
        	int currAngle = angleHalfIncr*(currTick+1);
        	for (int j=0; j<=numBoxRows; j++) {
        		int yStart = j*boxSize;
        		for (int i=0; i<boxesPerRow; i++) {
        			int xStart = i*boxSize;
        			GraphicsAdapter.fillArc(xStart, yStart, boxSize, boxSize, -90, -currAngle);
        		}
        	}
        }
        
        
        //Now, draw the rectangle...
        if (currTick >= halfTicks*2) {
        	int currWidth = Math.min(width, (((currTick-halfTicks*2)+1)*width)/((halfTicks*3)/2));
        	int currHeight = Math.min(height, (((currTick-halfTicks*2)+1)*height)/((halfTicks*3)/2));
        	GraphicsAdapter.fillRect(width/2-currWidth/2, height/2-currHeight/2, currWidth, currHeight);
        	
        }

        return true;
	}



	public boolean step() {
		//Are we done?
		if (currTick == halfTicks*4) {
			return true;
		}
    
		//Lengthen our arc, tide
		currTick++;

		return false;
	}

}