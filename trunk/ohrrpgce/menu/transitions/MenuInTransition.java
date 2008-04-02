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
	private static final int boxesPerRow = 5;
    private static final int maxAngle = 360;
    private static final int halfTicks = 3; //Determines the speed of everything
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
		//  are slowly covering the screen with a downward-moving rectangle. These
		//  things are synchronized.
		//I'm just going to treat all this like an array access problem, to make
		//  the math simpler in my head. :)
        GraphicsAdapter.setColor(menuColor);
        
        //Draw the circles in THIS row -up to 180 degrees
        int currRow = currTick/halfTicks;
        int currAngle = angleHalfIncr*((currTick %halfTicks)+1);
        int yStart = currRow*boxSize;
        if (yStart < height) { 
        	for (int i=0; i<boxesPerRow; i++) {
        		int xStart = i*boxSize;
        		GraphicsAdapter.fillArc(xStart, yStart, boxSize, boxSize, -90, -currAngle);
        	}
        }
        
        //Now, draw the circles in the PREVIOUS row, from 180 to 360 degrees
        if (--currRow>=0) {
        	yStart-=boxSize;
            for (int i=0; i<boxesPerRow; i++) {
            	int xStart = i*boxSize;
            	GraphicsAdapter.fillArc(xStart, yStart, boxSize, boxSize, -90, -currAngle-180);
            }
        }
        
        
        //Now, draw the rectangle...
        int currTideline = -boxSize/2 + currTick*tideIncrement;
        if (currTideline>0)
        	GraphicsAdapter.fillRect(0, 0, width, currTideline);
        
        return true;
	}



	public boolean step() {
		//Are we done?
		if (currTick == lastTick) {
			return true;
		}
    
		//Lengthen our arc, tide
		currTick++;

		return false;
	}

}
