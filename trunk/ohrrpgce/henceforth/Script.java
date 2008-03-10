package ohrrpgce.henceforth;

/**
 * Light-weight class used to store a script's runtime data.
 * @author Seth N. Hetu
 */
public class Script {
	//Wait flags
	public static final int WAIT_CYCLES = 1;
	public static final int WAIT_FOR_ALL = 2;
	public static final int WAIT_FOR_HERO = 4;
	public static final int WAIT_FOR_NPC = 8;
	public static final int WAIT_FOR_KEY = 16;
	public static final int WAIT_FOR_CAMERA = 32;
	public static final int WAIT_FOR_TEXTBOX = 64;
	
	//Constants
	public static final int CONST_NORTH = 0;
	public static final int CONST_EAST = 1;
	public static final int CONST_SOUTH = 2;
	public static final int CONST_WEST = 3;
	
	public int scriptSrcID;
	public int programCounter;
	public int stackPointer;
	
	//public int tickDelay; //Tick delay is only decremented for the top script.
	public int waitFlags;
	public int waitTicksLeft;
	public int waitNpcID;

	public Int saveCount;
	
	public Script() {
		programCounter = 0;
		stackPointer = 0;
		waitFlags = 0;
		
		//tickDelay = 2;
	}
}
