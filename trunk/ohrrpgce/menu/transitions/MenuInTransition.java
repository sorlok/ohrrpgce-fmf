package ohrrpgce.menu.transitions;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.RPG;
import ohrrpgce.menu.MenuSlice;

/**
 * A simple transition that draws a bunch of circles over the screen, and slowly
 *   "drags down" the background menu color. 
 * @author Seth N. Hetu
 */
public class MenuInTransition extends Transition {
	//Useful constants
	private static final int boxesPerRow = 8;
    private static final int halfTicks = 4; //Determines the speed of everything
	
    //Track our progress
    private int currTick;
    
    //Used internally
    //private RPG currRPG;
    private int menuColor;
    //private int width;
    private int height;
    private int boxSize;
    private int numTopRows;
    private int lastTick;
    private int originY; 


    private boolean done;
    
    
    public MenuInTransition(RPG currRPG, int canvasWidth, int canvasHeight) {
    	//Save for later
    	//this.currRPG = currRPG;
    	//this.width = canvasWidth;
    	this.height = canvasHeight;
    	
    	//Calculate
    	boxSize = canvasWidth/boxesPerRow; //Later, scroll left-to-right for horiz. displaysMath.min(canvasHeight, canvasWidth)/5;
    	numTopRows = (int)Math.ceil(((float)canvasHeight-boxSize)/2/boxSize); //Always center the one row
    	menuColor = currRPG.getTextBoxColors(0)[0];
    	originY = height/2 - boxSize/2;
    	lastTick = halfTicks*2 + numTopRows*halfTicks;//(height+boxSize/2)/tideIncrement;
    	
    	reset();
    }

	public void reset() {
		currTick = 0;
		done = false;
	}
    
	
	public boolean doPaintOver() {
		//We have two things going on here, all painting with the menu's background
		//  color. First, we are filling up circles from bottom-to-top; second, we 
		//  are slowly covering the screen with a downward-moving rectangle. These
		//  things are synchronized.
		//I'm just going to treat all this like an array access problem, to make
		//  the math simpler in my head. :)
        GraphicsAdapter.setColor(menuColor);
        
        //Draw the boxes in THIS/THESE row(s) up to 50%
        int currRow = currTick/halfTicks;
        int currPercent = (currTick %halfTicks)+1;
        int currBoxSize = (currPercent*boxSize/2)/halfTicks;
        for (int flip=0; flip<2; flip++) {
        	//Don't do this twice...
        	if (flip==1 && currRow==0)
        		break;
        	
        	//Calculate
        	int yStart = originY+boxSize/2;
        	if (yStart + currRow*boxSize > height)
        		break;
        	if (flip==0)
        		yStart += currRow*boxSize;
        	else
        		yStart -= currRow*boxSize;
        	
        	//Now... print!
        	for (int i=0; i<boxesPerRow; i++) {
        		int xStart = i*boxSize+boxSize/2;
        		GraphicsAdapter.fillRect(xStart-currBoxSize/2, yStart-currBoxSize/2, currBoxSize, currBoxSize);
        		//GraphicsAdapter.fillArc(xStart, yStart, boxSize, boxSize, -90, -currAngle);
        	}
        }
        
        
        //Now, draw the circles in the PREVIOUS row, from 50 to 100%
       if (--currRow>=0) {
    	   currBoxSize += boxSize/2;
    	   
           for (int flip=0; flip<2; flip++) {
           	//Don't do this twice...
           	if (flip==1 && currRow==0)
           		break;
           	
           	//Calculate
           	int yStart = originY+boxSize/2;
           	if (yStart + currRow*boxSize > height)
           		break;
           	if (flip==0)
           		yStart += currRow*boxSize;
           	else
           		yStart -= currRow*boxSize;
           	
           	//Now... print!
           	for (int i=0; i<boxesPerRow; i++) {
           		int xStart = i*boxSize+boxSize/2;
           		GraphicsAdapter.fillRect(xStart-currBoxSize/2, yStart-currBoxSize/2, currBoxSize, currBoxSize);
           	}
           }
        }
       
        return true;
	}

	
	public MenuSlice getNewFocus() {
		return null;
	}


	public void step() {
		//Are we done?
		if (currTick == lastTick) {
			done = true;
		}
    
		//Lengthen our arc, tide
		currTick++;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public boolean requiresReLayout() {
		return false;
	}

}
