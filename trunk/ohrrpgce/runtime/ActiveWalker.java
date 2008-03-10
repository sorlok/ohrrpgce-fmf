/*
 * ActiveWalker.java
 * Created on January 14, 2007, 7:38 PM
 */

package ohrrpgce.runtime;

import ohrrpgce.data.NPC;
import ohrrpgce.data.Sprite;
import ohrrpgce.data.loader.TilesetParser;

/**
 *
 * @author Seth N. Hetu
 */
public abstract class ActiveWalker {
    private OHRRPG parent;

    private int facingDir;
    protected int[] position; //in tiles
    private int[] velocity; //in pixels
    private int[] offset; //If, say, you're halfway between moving from one tile to the next, this equals [10,0]
    protected int[][] pixelData;
    protected int[][] savedData;
    private boolean savedOnce;
    private int frame;
    private boolean ignoreFrame; //Just guessing here...
    protected int speed;
    protected int nativeSpeed;
    
    //This array is odd... element[0] is always true (otherwise, the array is just null).
    // and it implies that the "stepDone()" function should be called on the next game tick.
    // element[1] means that a step was actually taken (if false, it could mean that the NPC
    // tried to move, but was blocked.) Use scheduleStepUpdate() to deal with it.
    private boolean[] stepDone;

    private boolean isPaused;

    protected ActiveWalker(OHRRPG parent) {
        this.parent = parent;
        this.offset = new int[] {0, 0};
        this.velocity = new int[] {0, 0};
        this.facingDir = NPC.DIR_DOWN;
    }

    
    protected boolean inMidStep() {
        return offset[0]!=0 || offset[1]!=0;
    }
            
    
    /**
     * Called whenever a step is finished. 
     * Intended to make it easy to implement NPC routes, etc.
     * @param moved If true, the walker actually took a step. This might be false
     *          if, say, an NPC tried to step right, but a wall was to the right.
     */
    protected abstract void stepDone(boolean moved);
    
    /**
     * Called one tick before the user will arrive at his square. 
     * Intended mostly for heroes to activate plotscripts correctly.
     */
    protected abstract void stepNearlyDone();
    
    protected boolean isHero() {
        return false;
    }

    protected void setGraphics(Sprite s, int palette) {
        if (s==null)
            return;
        pixelData = new int[s.spData.length][s.spData[0].length];
        for (int i=0; i<pixelData.length; i++) {
            pixelData[i] = new int[s.spData[i].length];
            for (int pix=0; pix<pixelData[i].length; pix++)
                pixelData[i][pix] = parent.getBaseRPG().getIndexedColor(palette, s.spData[i][pix]);
        }
    }

    public void forceStep(int direction) {
        step(direction, true);
    }
    
    /**
     * Returns true if the step was okay.
     */
    public boolean step(int direction) {
        return step(direction, false);
    }
    
    public void tempHide(boolean hide) {
        if (hide) {
            if (!savedOnce) {//Assuming hero's graphic won't change mid-runtime
                savedData = pixelData;
                pixelData = null;
                savedOnce = true;
            }
        } else {
            if (savedOnce) { //Do we have data to restore?
                pixelData = savedData;
                savedData = null;
                savedOnce = false;
            }
        }
    }
    
    
    /**
     * Provides a slight optimization when checking the ability of
     *  one walker (say, a hero) to step over/on and push other walkers
     *  (say, NPCs).
     * @param blockingNPC The NPC currently on the tile we wish to move to.
     * @param currHero The current hero (used by NPCs)
     * @param toTile The tile we're moving to (used by NPCs)
     * @returns <code>true</code> if we should perform the step.
     */
    protected abstract boolean performStep(ActiveNPC blockingNPC, ActiveHero currHero, int[] toTile);
    
    
    
    /**
     * Contains the general-purpose algorithm for 
     *  stepping a Walker in any given direction.
     * Note that performStep() is called to determine the walker's
     *  specific movement capabilities in relation to other walkers.
     * @param direction In which direction to step.
     * @param force If true, automatically move in this direction.
     * @returns true if the step occurred.
     */
    private boolean step(int direction, boolean force) {
        //Don't allow them to change direction mid-step.
        if (inMidStep())
            return false;
 
        //Turn in that direction
        this.facingDir = direction;
        
        //Can we move?
        int[] to = new int[] { position[0], position[1] };
        int[] newVel = new int[] { 0, 0 };
        int[] newOff = new int[] { 0, 0 };
        switch (direction) {
            case NPC.DIR_UP:
                to[1]--;
                newVel[1] = -speed;
                newOff[1] = TilesetParser.TILE_SIZE;
                break;
            case NPC.DIR_DOWN:
                to[1]++;
                newVel[1] = speed;
                newOff[1] = -TilesetParser.TILE_SIZE;
                break;
            case NPC.DIR_LEFT:
                to[0]--;
                newVel[0] = -speed;
                newOff[0] = TilesetParser.TILE_SIZE;
                break;
            case NPC.DIR_RIGHT:
                to[0]++;
                newVel[0] = speed;
                newOff[0] = -TilesetParser.TILE_SIZE;
                break;
            default:
                System.out.println("Bad direction code: " + direction);
        }
        
        ActiveNPC firstBlockingNPC = parent.getActiveMap().getFirstNPCAt(to[0], to[1]);;
        boolean tileFree = parent.getCurrMap().canMove(position[0], position[1], to[0], to[1]);
                
        if(force || (tileFree && performStep(firstBlockingNPC, parent.getActiveHero(), to))) {
            //Don't plan the move if we can't ever make it there.
            if (speed>0) {
                this.position = to;
                this.offset = newOff;
                this.velocity = newVel;
                return true;
            }
            return false;
        } else {
            //Plan a new move.
            scheduleStepUpdate(false);
            
            return false;
        }
    }


    public void tick() {
        if (isPaused)
            return;
        
        //Seperate each orthogonal direction; this might glitch if the user
        // tries to move diagonally.
        boolean moved = false;
        boolean oneStepAway = false;
        for (int i=0; i<2; i++) {
            //Can we move?
            if (offset[i]!=0) {
                //Move based on one's velocity.
                moved = true;
                offset[i] += velocity[i];
                
                //Have we moved a full tile?
                if (offset[i]==0) {                    
                    //Reset our movement
                    offset[i] = 0;
                    scheduleStepUpdate(true);
                    velocity[i] = 0;
                    
                    //Handle doors, NPC events, etc., if we're the hero.
                    if (isHero())
                        parent.getActiveMap().tileTouched(position[0], position[1]);
                } else if (offset[i]+velocity[i]==0)
                	oneStepAway = true;
            }
        }
        
        //Advance the animation
        if (!hasNoPicture()) {
            if (moved) {
                if (ignoreFrame)
                    ignoreFrame = false;
                else {
                    frame++;
                    if (frame==2)
                        frame = 0;
                    ignoreFrame = true;
                }
            } else {
                ignoreFrame = false;
                frame = 0;
            }
        }
        
        //Prepare the next step?
        if (stepDone != null) {
            boolean val = stepDone[1];
            stepDone = null;
            this.stepDone(val);
        } else if (oneStepAway)
        	stepNearlyDone();
    }
    
    /**
     * Temporarily set one's speed to a new value. Use "resetSpeed()" to recover.
     */
    public void setSpeed(int speedVal) {
        this.speed = speedVal;
    }
    
    public void resetSpeed() {
        this.speed = nativeSpeed;
    }
    
    protected void scheduleStepUpdate(boolean realStepTaken) {
        stepDone = new boolean[] {true, realStepTaken};
    }
    
    public boolean hasNoPicture() {
        return pixelData==null;
    }

    /**
     * Turn right (clockwise) this many times. 
     */
    protected void turn(int rightPlus) {
        while (rightPlus != 0) {
            facingDir += rightPlus/Math.abs(rightPlus);
            if (facingDir<0)
                facingDir = 3;
            if (facingDir>3)
                facingDir = 0;

            rightPlus -= rightPlus/Math.abs(rightPlus);
        }
    }
    
    protected void turnTo(int direction) {
        this.facingDir = direction;
    }

    public int getTileX() {
        return position[0];
    }

    public int getTileY() {
        return position[1];
    }

    public int getPixelX() {
        return position[0]*TilesetParser.TILE_SIZE + offset[0];
    }
    public int getPixelY() {
        return position[1]*TilesetParser.TILE_SIZE + offset[1];
    }
    public int[] getCurrFrame() {
        //URDL
        return pixelData[facingDir*2 + frame];
    }
    
    public void setDirection(int dir) {
        this.facingDir = dir;
    }
    
    public int getDirection() {
        return facingDir;
    }
    
    public OHRRPG getParent() {
        return parent;
    }
    
    public void pause() {
        this.isPaused = true;
    }
    
    public void unpause() {
        this.isPaused = false;
    }
}
