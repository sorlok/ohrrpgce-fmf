/*
 * Vehicle.java
 * Created on February 20, 2007, 9:14 AM
 */

package ohrrpgce.data;

/**
 *
 * @author Seth N. Hetu
 */
public class Vehicle {

    public String name;
    public int speed;
    public int randomBattleSet;
    public int useButton;
    public int menuButton;
    public int ifRidingTag;
    public int onMount;
    public int onDismount;
    public int elevation;
    
    //Special "Vehicle terrain" tags
    public int overrideWalls;
    public int blockedBy;
    public int mountFrom;
    public int dismountTo;
    
    //What they mean
    public static final int PASS_DEFAULT = 0;
    public static final int PASS_A = 1;
    public static final int PASS_B = 2;
    public static final int PASS_A_AND_B = 3;
    public static final int PASS_A_OR_B = 4;
    public static final int PASS_NOT_A = 5;
    public static final int PASS_NOT_B = 6;
    public static final int PASS_NOT_A_AND_NOT_B = 7;
    public static final int PASS_ANY = 8;

    
    //Bitsets
    public boolean passThroughWalls;
    public boolean passThroughNPCs;
    public boolean enableNPCActivation;
    public boolean enableDoorUse;
    public boolean doNotHideLeader;
    public boolean doNotHideParty;
    public boolean dismountOneSpaceAhead;
    public boolean passWallsWhileDismounting;
    public boolean disableFlyingShadow;

}

