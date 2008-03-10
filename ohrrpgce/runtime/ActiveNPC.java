/*
 * ActiveNPC.java
 * Created on February 12, 2007, 4:50 PM
 */

package ohrrpgce.runtime;

import ohrrpgce.data.NPC;


/**
 *
 * @author Seth N. Hetu
 */
public class ActiveNPC extends ActiveWalker {
    
  //  private static final int STEP_NILL = -1;
    
    private NPC currNPC;
    private boolean schedulePause;
    
    //The step buffer is used to schedule scripted steps, and for pushability.
    //It is set up as [dx, dy], where dx/y = number of steps to take in the x/y direction.
    private int[] stepBuffer;
    
    public ActiveNPC(OHRRPG parent, NPC currNPC, int posX, int posY, int direction) {
        super(parent);
        this.currNPC = currNPC;
        this.position = new int[] {posX, posY};
        this.speed = OHRRPG.getWalkSpeed(currNPC.speed);
        this.nativeSpeed = speed;
        this.setDirection(direction);
       
        stepBuffer = new int[]{0, 0};
      
        //  scheduleStep = STEP_NILL;
        setGraphics(currNPC.getWalkabout(), currNPC.walkaboutPaletteID);
        scheduleStepUpdate(false); //Schedule the next move for the first game tick.
    }
    
    
    public boolean hasStepsInBuffer() {
    	return stepBuffer[0] != 0 || stepBuffer[1] != 0;
    }

    
    private static final int Step2Dir(int arrIndex, int arrVal) {
    	if (arrIndex==0) {
    		if (arrVal>0)
    			return NPC.DIR_RIGHT;
    		else if (arrVal<0)
    			return NPC.DIR_LEFT;
    		else
    			throw new RuntimeException("Step2Dir, bad arrVal: " + arrVal);
    	} else if (arrIndex==1) {
    		if (arrVal>0)
    			return NPC.DIR_DOWN;
    		else if (arrVal<0)
    			return NPC.DIR_UP;
    		else
    			throw new RuntimeException("Step2Dir, bad arrVal: " + arrVal);    		
    	} else 
    		throw new RuntimeException("Step2Dir, bad arrIndex: " + arrIndex);
    }
    
    
    protected void stepNearlyDone() {
    }
    
    
    /**
     * "Schedule" the next step.
     */
    protected void stepDone(boolean moved) {
        if (schedulePause) {
            this.pause();
            schedulePause = false;
        }
        
        //Schedule the next move.
       // if (scheduleStep != STEP_NILL) {
            //step(scheduleStep);
           // scheduleStep = STEP_NILL;
        //} else {
        boolean doStep = true;
            switch(currNPC.movePattern) {
                case NPC.MOVE_STAND_STILL:
                    doStep = false;
                    break;
                case NPC.MOVE_WANDER:
                    setDirection(getParent().rand.nextInt(4));
                    break;
                case NPC.MOVE_PACE:
                    if (!moved)
                        turn(2);
                    break;
                case NPC.MOVE_TURN_CLOCKWISE:
                    if (!moved)
                        turn(1);
                    break;
                case NPC.MVOE_TURN_COUNTER_CLOCK:
                    if (!moved)
                        turn(-1);
                    break;
                case NPC.MOVE_RANDOM_TURNS:
                    if (!moved)
                        turn(2*getParent().rand.nextInt(2)-1);
                    break;
                    //Not implemented yet....
                case NPC.MOVE_CHASE_PLAYER:
                case NPC.MOVE_AVOID_PLAYER:
                case NPC.MOVE_WALK_IN_PLACE:
                default:
                    doStep = false;
            }
            
            //Step in the scheduled direction
	        if (doStep) {
	        	if (getParent().suspendedBlockability)
	        		step(getDirection());
	        	else
	        		forceStep(getDirection());
            }
      //  }
    }
    
    
    public void tick() {    	
        super.tick();
        //walkingDir = -1;
        
    	//Do pushability and scripted steps.
        if (!inMidStep()) {
	        for (int i=0; i<stepBuffer.length; i++) {
	            if (stepBuffer[i]!=0) {
	                int facingDir = getDirection();
	                if (getParent().suspendedBlockability)
	                	forceStep(Step2Dir(i, stepBuffer[i]));
	                else
	                	step(Step2Dir(i, stepBuffer[i]));
	                setDirection(facingDir); //Hack!
	                
	                //Increment or decrement as necessary
	                stepBuffer[i] -= (stepBuffer[i]/Math.abs(stepBuffer[i]));
	            }
	        }
        }
    }
    
    
    /**
     * Does this NPC block the current hero or NPC?
     */
    public boolean blocks(boolean npcIsAsking) {
  /*      System.out.println("   Blocks: " + npcIsAsking);
        System.out.println("   Blocks: " + currNPC.activation);
        System.out.println("   Blocks: " + (currNPC.activation==currNPC.ACTIVATE_STEP_ON));*/
        
        if (currNPC.activation==NPC.ACTIVATE_USE || currNPC.activation==NPC.ACTIVATE_TOUCH)
            return true;
        else if (currNPC.activation==NPC.ACTIVATE_STEP_ON)
            return npcIsAsking;
        
        return false;
    }
    
    
    public void push(int direction) {
    	push(direction, 1, false);
    }
    
    
    public void push(int direction, int numSteps, boolean force) {
        if (force || currNPC.canPush(direction)) {
            System.out.println("Push NPC for: " + numSteps);
            switch (direction) {
            	case NPC.DIR_DOWN:
            		stepBuffer[1] = numSteps;
            		break;
            	case NPC.DIR_UP:
            		stepBuffer[1] = -numSteps;
            		break;
            	case NPC.DIR_RIGHT:
            		stepBuffer[0] = numSteps;
            		break;
            	case NPC.DIR_LEFT:
            		stepBuffer[0] = -numSteps;
            		break;
            	default:
            		throw new RuntimeException("push(), bad direction: " + direction);
            }
            //walkingDir = direction;
           // if (!inMidStep())
             //   System.out.println("OK!");
        }
    }
    
    public boolean isVisible() {
        return getParent().tagCheck(currNPC.appearTag1) && getParent().tagCheck(currNPC.appearTag2);
    }
    
    
    public boolean activateByTalk() {
        return (currNPC.activation==NPC.ACTIVATE_USE);
    }
    
    public void activateScript() {
        if (currNPC.plotscript!=0) {
        	if (currNPC.plotscript > 16383)
        		System.out.println("Lookup script: " + currNPC.plotscript);
        	else {
        		System.out.println("Run script: " + currNPC.plotscript);
        		getParent().startPlotscriptExtern(currNPC.plotscript);
        	}
        }
    }

    //What happens when the hero activates
    public void heroActivated() {
        if (getParent().isRidingVehicle() && !getParent().getCurrVehicle().enableNPCActivation) {
            System.out.println("Riding vehicle: no activation allowed");
            return;
        }
        
        if (currNPC.textBox!=0) {
            getParent().showTextBox(this, currNPC.textBox);
        }
        if (currNPC.vehicleID!=-1) {
            System.out.println("Ride vehicle: " + currNPC.vehicleID);
            getParent().rideVehicle(this, currNPC.vehicleID);
        }

    }
    
    public boolean inBounds(int[] tileBounds) {
        return (this.getTileX()>=tileBounds[0] && this.getTileX()<=tileBounds[1] && this.getTileY()>=tileBounds[2] && this.getTileY()<=tileBounds[3]) ;
    }
    
    
    protected boolean performStep(ActiveNPC blockingNPC, ActiveHero currHero, int[] toTile) {
        boolean heroCheck = currHero.getTileX()!=toTile[0] || currHero.getTileY()!=toTile[1];
        boolean npcCheck = blockingNPC==null || !blockingNPC.blocks(true);
        return (heroCheck && npcCheck);
    }
    
    
    /**
     * Cause this NPC to pause on his next step. Scheduling a pause fo an NPC that has
     *  already paused may cause that NPC to freeze, or to move one tile at a time.
     */ 
    public void schedulePause() {
        this.schedulePause = true;
    }
    
}


