package ohrrpgce.henceforth;

import java.util.Vector;

import ohrrpgce.data.NPC;
import ohrrpgce.game.LiteException;
import ohrrpgce.runtime.OHRRPG;

public class VirtualMachine {
	//Store all Format B source for scripts
	private int[][] bytecode;
	
	//Store all ready/waiting scripts
	private Vector scripts; //Vector of <Script>
	
	//Locally store current script's important values
	private int currScriptID;
	private int currScriptSrcID;
	private int currScriptPC;
	private int currScriptSP;
	//private int currScriptTickDelay;
	
	//The all-important stack. The extra level of indirection is probably ok.
	private SuperStack stack;
	
	//Also...
	private OHRRPG parent;

	
	//Conversion...
	public static final int HFDir2Dir(int dir) {
		switch (dir) {
			case Script.CONST_NORTH:
				return NPC.DIR_UP;
			case Script.CONST_SOUTH:
				return NPC.DIR_DOWN;
			case Script.CONST_EAST:
				return NPC.DIR_RIGHT;
			case Script.CONST_WEST:
				return NPC.DIR_LEFT;
			default:
				throw new LiteException(VirtualMachine.class, null, "Bad direction: " + dir);
		}
	}
	
	
	public VirtualMachine(OHRRPG parent) {
		this.parent = parent;
		
		stack = new SuperStack();
		scripts = new Vector();
		
		//No script
		nullCurrScript();
	}
	
	
	public void textBoxClosed() {
		for (int i=0; i<scripts.size(); i++) {
			if ((((Script)scripts.elementAt(i)).waitFlags&Script.WAIT_FOR_TEXTBOX) !=0) 
				((Script)scripts.elementAt(i)).waitFlags ^= Script.WAIT_FOR_TEXTBOX; 
		}
	}
	
	public void npcWalked(int id) {
		for (int i=0; i<scripts.size(); i++) {
			if ((((Script)scripts.elementAt(i)).waitFlags&Script.WAIT_FOR_NPC) !=0) 
				((Script)scripts.elementAt(i)).waitFlags ^= Script.WAIT_FOR_NPC; 
		}
	}
	
	
	
	public void setNumBytecodes(int num) {
		if (bytecode != null)
			throw new RuntimeException("HVM cannot re-initialize num_plotscripts from " + bytecode.length + " to " + num);
		bytecode = new int[num][];
	}
	
	
	private void nullCurrScript() {
		currScriptID = -1;
		currScriptSrcID = -1;
		currScriptPC = -1;
		currScriptSP = -1;
		//currScriptTickDelay = -1;
	}
	
	
	/**
	 * Start a new instance of the given plotscript.
	 * @param srcID ID of the plotscript to start.
	 * @param isExternal If FALSE, this is starting internally. If TRUE, it's
	 *        probably starting from a map event.
	 */
	public void startScript(int srcID, boolean isExternal) {
		//If we're running a script of the same srcID and loading externally,
		//   and a certain bit is OFF, then we don't load the script.
		if (isExternal && srcID==currScriptSrcID && !parent.getBaseRPG().permit2xTriggeredScripts)
			return;
		
		//Ok, passing one simple check, we now check if that script is in memory to begin with.
		// This is MEMORY STRATEGY 1
		try {
			if (srcID<0 || srcID>=bytecode.length)
				throw new LiteException(this, null, "Script does not exist with ID: " + srcID);
		} catch (NullPointerException ex) {
			throw new LiteException(this, null, "\"bytecode\" array is still null.");
		}
		
		if (bytecode[srcID] == null) {
			if (srcID==12)
				bytecode[srcID] = new int[]{ 0x0806082F, 0x00000003, 0x00040828, 0x00000C00, 0x00020001, 0x08340804, 0x00000C00, 0x00020001, 0x08340006, 0x00020001, 0x08340804, 0x00000C00, 0x00020001, 0x08340006, 0x00020001, 0x08340007, 0x00020001, 0x08340804, 0x00060C00, 0x00030001, 0x08340007, 0x00020001, 0x08340804, 0x00070C00, 0x00010001, 0x08340804, 0x00050801, 0x00020003, 0x00040828, 0x082A0000, 0x08260005, 0x080B083B, 0x08300808 };
			else
				bytecode[srcID] = new int[]{ 0x08000800 }; //No-op, no-op
		}
		
		//Suspend any currently-running script:
		if (currScriptID != -1) {
			Script writeback = (Script)scripts.elementAt(currScriptID);
			writeback.programCounter = currScriptPC;
			writeback.stackPointer = currScriptSP;
			
			//Prevent spurious errors from popping up.
			nullCurrScript();
		}
		
		//Now, create a new Script object and "push" it to the stack.
		Script newScr = new Script();
		newScr.scriptSrcID = srcID;
		for (int i=0; i<scripts.size(); i++) {
			if (((Script)scripts.elementAt(i)).scriptSrcID == srcID) {
				newScr.saveCount = ((Script)scripts.elementAt(i)).saveCount;
				break;
			} else if (i==scripts.size()-1)
				newScr.saveCount = new Int(1);
		}
		newScr.waitFlags = Script.WAIT_CYCLES;
		newScr.waitTicksLeft = 1;
		scripts.addElement(newScr);
		
		//"Take" the script from the stack and make it ready to run.
		switchInScript(scripts.size()-1);
	}
	
	private void switchInScript(int scrID) {
		currScriptID = scrID;
		currScriptSrcID = ((Script)scripts.elementAt(currScriptID)).scriptSrcID;
		currScriptPC = ((Script)scripts.elementAt(currScriptID)).programCounter;
		currScriptSP = ((Script)scripts.elementAt(currScriptID)).stackPointer;
		//currScriptTickDelay = ((Script)scripts.get(currScriptID)).tickDelay;
	}
	
	
	
	
	
	
	/**
	 * Called every tick.
	 */
	public void updateScripts() {
		if (currScriptID == -1)
			return;
		
		System.out.println("<Tick>");
		
		//Update "wait" commands
		Script currScriptRef = (Script)scripts.elementAt(currScriptID);
		if ((currScriptRef.waitFlags&Script.WAIT_CYCLES)!=0) {
			//Update "wait ticks" 
			currScriptRef.waitTicksLeft--;
			if (currScriptRef.waitTicksLeft <= 0)
				currScriptRef.waitFlags ^= Script.WAIT_CYCLES; 
		}
		
		
		//Loop
		while (currScriptRef.waitFlags == 0) {
			//Fetch
			int currBytecode = bytecode[currScriptSrcID][currScriptPC / 2];
			if ((currScriptPC % 2) == 0)
				currBytecode >>= 16;
			else
				currBytecode &= 0xFFFF;

			//Decode
			switch (currBytecode >> 14) {
			case 0: //Single width
				switch ((currBytecode & 0x3C00) >> 10) {
				case 0: //Short integer
					stack.push(currBytecode & 0xFF);
					System.out.println("Pushing: " + stack.top());
					break;
				case 1: //End define
					System.out.println("End define");
					break;
				case 2: //Hspeak API Call
					switch (currBytecode & 0x3FF) {
					case 0: //No-op
						System.out.println("no-op");
						break;
					case 1: //Wait
						//First:
						this.parent.initWait();
						currScriptRef.waitFlags |= Script.WAIT_CYCLES;
						currScriptRef.waitTicksLeft = stack.pop();

						System.out.println("wait: " + currScriptRef.waitTicksLeft);
						break;
					case 4: //Wait for NPC
						//First:
						this.parent.initWait();

						currScriptRef.waitFlags |= Script.WAIT_FOR_NPC;
						currScriptRef.waitNpcID = stack.pop();
						
						System.out.println("wait_npc");
						break;
					case 6: //Suspend Player
						this.parent.suspendedPlayer = true;
						
						System.out.println("sus_play");
						break;
					case 8: //Resume Player
						this.parent.suspendedPlayer = false;
						
						System.out.println("rsm_play");
						break;
					case 11: //Show text box
						this.parent.queueTextBox(stack.pop());

						System.out.println("show_txt");
						break;
					case 38: //Camera follows hero
						System.out.println("cam_hero: not implemented");
						break;
					case 40: //Pan camera
						System.out.println("pan_cam: not implemented");
						break;
					case 42: //Wait for camera
						//First:
						this.parent.initWait();

						System.out.println("wait_cam: not implemented");
						break;
					case 47: //Suspend Obstruction
						this.parent.suspendedBlockability = true;
						
						System.out.println("sus_block");
						break;
					case 48: //Resume Obstruction
						this.parent.suspendedBlockability = false;
						
						System.out.println("rsm_block");
						break;
					case 52: //Walk NPC
						this.parent.getActiveMap().getNPCs()[stack.antepenultimate()].push(VirtualMachine.HFDir2Dir(stack.penultimate()), stack.top(), true);
						stack.remove(3);
						
						System.out.println("walk_npc");
						break;
					case 59: //Wait for text box
						//First:
						this.parent.initWait();
						
						//Now....
						currScriptRef.waitFlags |= Script.WAIT_FOR_TEXTBOX;

						System.out.println("wait_txt");
						break;
					default:
						throw new RuntimeException("Bad bytecode: "
								+ Integer.toHexString(currBytecode));
					}
					break;
				case 3: //Henceforth primitive
					switch (currBytecode & 0x3FF) {
					case 0: //dup
						stack.push(stack.top());
						System.out.println("dup");
						break;
					case 1: //swap
						stack.swap();
						System.out.println("swap");
						break;
					case 2: //drop
						stack.pop();
						System.out.println("drop");
						break;
					case 3: //over
						stack.push(stack.penultimate());
						System.out.println("over");
						break;
					case 4: //rot
						stack.rotate();
						System.out.println("rot");
						break;
					case 5: //Plus
						stack.push(stack.pop() + stack.pop());
						System.out.println("plus(+)");
						break;
					case 6: //Minus
						stack.push(-stack.pop() + stack.pop());
						System.out.println("minus(-)");
						break;
					case 7: //Mult
						stack.push(stack.pop() * stack.pop());
						System.out.println("mult(*)");
						break;
					case 8: //Integer divide
						int div = stack.penultimate() / stack.top();
						stack.remove(2);
						stack.push(div);
						System.out.println("div(/)");
						break;
					default:
						throw new RuntimeException("Bad bytecode: "
								+ Integer.toHexString(currBytecode));
					}
					break;
				case 4: //User function call
					break;
				default:
					throw new RuntimeException("Bad bytecode: "
							+ Integer.toHexString(currBytecode));
				}
				break;
			case 1: //Fixed-width
				switch ((currBytecode & 0x3F00) >> 8) {
				case 0: //Full-width integer
					if (true)
						throw new RuntimeException(
								"Full width integer not yet supported.");
					break;
				default:
					throw new RuntimeException("Bad bytecode: "
							+ Integer.toHexString(currBytecode));
				}
				break;
			case 2: //Variable-width
				switch ((currBytecode & 0x3F00) >> 8) {
				case 0: //Ascii String
					if (true)
						throw new RuntimeException(
								"ASCII Strings not yet supported.");
					break;
				case 1: //Begin Define
					if (true)
						throw new RuntimeException(
								"Begin define not yet supported.");
					break;
				case 2: //Call user function
					if (true)
						throw new RuntimeException(
								"User function not yet supported.");
					break;
				case 3: //Unicode Strings
					if (true)
						throw new RuntimeException(
								"Unicode Strings not yet supported.");
					break;
				default:
					throw new RuntimeException("Bad bytecode: "
							+ Integer.toHexString(currBytecode));
				}
				break;
			default:
				throw new RuntimeException("Bad bytecode: "
						+ Integer.toHexString(currBytecode));
			}
			currScriptPC++;

			//System.out.println("     wait: " + Integer.toBinaryString(currScriptRef.waitFlags));
			
			//Done?
			if (currScriptPC >= bytecode[currScriptSrcID].length * 2) {
				System.out.println("<DONE>");
				scripts.removeElementAt(currScriptID);
				nullCurrScript();

				if (!scripts.isEmpty())
					switchInScript(scripts.size() - 1);
			}
			if (currScriptID == -1)
				return;
		}

	}
	
}






