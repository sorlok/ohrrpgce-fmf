/*
 * NPC.java
 * Created on February 8, 2007, 10:10 PM
 */

package ohrrpgce.data;


/**
 * Er... the definition of a "Hero" is much nicer. NPCs are just characters you don't
 *   play as... and sometimes they're doorsteps, or passive observers, or 
 *   day/night triggers, or....
 * @author Seth N. Hetu
 */
public class NPC {
    public static final int DIR_UP = 0;
    public static final int DIR_RIGHT = 1;
    public static final int DIR_DOWN = 2;
    public static final int DIR_LEFT = 3;
	
    private RPG parent;
    
    private int walkaboutID;
    public int walkaboutPaletteID;
    public int[][] instances; //[ID][x, y, DIR, walk frame]
    private int pushBits;
    public int speed;
    public int movePattern;
    public int activation;
    public int textBox; //-1 =  no box. Otherwise, the box's ID
    public int appearTag1; //0=N/A, >2 = tag x on, <2 = tag -x off
    public int appearTag2; //0=N/A, >2 = tag x on, <2 = tag -x off
    public int vehicleID; //0=N/A, else, that vehicle.
    public int plotscript; //0=none, 1-16383=script in .HSZ, 16384-32767=script in LOOKUP.BIN
    
    public static final int ACTIVATE_USE = 0;
    public static final int ACTIVATE_TOUCH = 1;
    public static final int ACTIVATE_STEP_ON = 2;
    
    public static final int MOVE_STAND_STILL = 0;
    public static final int MOVE_WANDER = 1;
    public static final int MOVE_PACE = 2;
    public static final int MOVE_TURN_CLOCKWISE = 3;
    public static final int MVOE_TURN_COUNTER_CLOCK = 4;
    public static final int MOVE_RANDOM_TURNS = 5;
    public static final int MOVE_CHASE_PLAYER = 6;
    public static final int MOVE_AVOID_PLAYER = 7;
    public static final int MOVE_WALK_IN_PLACE = 8;

    
    public NPC(RPG parent) {
        this.parent = parent;
    }
    
    public RPG getParent() {
        return parent;
    }
    
    public void setWalkabout(int id) {
        this.walkaboutID = id;
    }
    
    public Sprite getWalkabout() {
        if (walkaboutID==-1)
            return null;
        return parent.getWalkabout(walkaboutID);
    }
    
    public void setPushability(boolean pushUp, boolean pushDown, boolean pushLeft, boolean pushRight) {
        pushBits = 0;
        if (pushUp)
            pushBits |= 1;
        if (pushDown)
            pushBits |= 2;
        if (pushLeft)
            pushBits |= 4;
        if (pushRight)
            pushBits |= 8;
    }
    
    public boolean canPush(int pushInThisDirection) {
        switch (pushInThisDirection) {
            case DIR_UP:
                return (pushBits&1) != 0;
            case DIR_DOWN:
                return (pushBits&2) != 0;
            case DIR_LEFT:
                return (pushBits&4) != 0;
            case DIR_RIGHT:
                return (pushBits&8) != 0;
            default:
                throw new RuntimeException("Invalid push direction: " + pushInThisDirection);
        }
    }
}
