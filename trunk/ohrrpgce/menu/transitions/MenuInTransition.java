package ohrrpgce.menu.transitions;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.RPG;

public class MenuInTransition extends Transition {
	//State tracking
    private static final int PHASE_ONE = 1;
   // private static final int PHASE_TWO = 2;
    private static final int PHASE_DONE = 3;
    private int phase;
    
    //Track our angles...
    private int currAngle;
    private static final int maxAngle = 360;
    private static final int angleIncr = 30;
    
    //Used internally
    private RPG currRPG;
    private int width;
    private int height;
    
    public MenuInTransition(RPG currRPG, int canvasWidth, int canvasHeight) {
    	this.currRPG = currRPG;
    	this.width = canvasWidth;
    	this.height = canvasHeight;
    	
    	reset();
    }

	public void reset() {
        currAngle = 0;
        phase = PHASE_ONE;
	}
    
	
	public boolean doPaintOver() {
        int[] clr0 = currRPG.getTextBoxColors(0);
        int boxBounds = Math.max(width, height);
        int xStart = (width-boxBounds)/2;
        int yStart = (height-boxBounds)/2;
        
        //Draw the arc
        GraphicsAdapter.setColor(clr0[0]);
        GraphicsAdapter.fillArc(xStart, yStart, boxBounds, boxBounds, 0, -currAngle);
                
        return true;
	}



	public boolean step() {
        switch (phase) {
        	case PHASE_ONE:
        		//Are we done?
        		if (currAngle == maxAngle) {
        			phase = PHASE_DONE;
        			break;
        		}
            
        		//Lengthen our arc
        		if (currAngle<maxAngle) {
        			currAngle+=angleIncr;
        			if (currAngle+angleIncr>maxAngle)
        				currAngle = maxAngle;
        		}

        		break;

        	default:
        		throw new RuntimeException("Illegal step: this Transition has finished!");
        }
 
        return (phase==PHASE_DONE);
	}

}
