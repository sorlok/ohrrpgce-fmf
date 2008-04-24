package ohrrpgce.menu.transitions;

import ohrrpgce.menu.Action;
import ohrrpgce.menu.MenuSlice;


/**
 * This transition was made with some thought: it should be able to handle
 *   all sub-menu transitions. Call it once, then call setupReverse() then call
 *   it again.
 * @author Seth N. Hetu
 */
public class SubMenuInTransition extends Transition {
	//Constants
	public static final int PHASE_ONE = 1;
	public static final int PHASE_TWO = 2;
	public static final int PHASE_DONE = 3;
	
	//Saves...
	private MenuSlice currHeroPicture;
	private MenuSlice buttonToMove;
	private MenuSlice[] savedConnections = new MenuSlice[4];
	
	//Targets
	private int heroPicRevX;
	private int heroPicRevY;
	private int heroPicDestX;
	private int heroPicDestY;
	private int heroPicSpeed;
	private int buttonRevX;
	private int buttonRevY;
	private int buttonDestX;
	private int buttonDestY;
	private int buttonSpeed;
	
	//Ahh... what a mess
	private int heroPicXHint;
	private int heroPicYHint;
	private int buttonXHint;
	private int buttonYHint;
	
	private int phase;
	private MenuSlice finalFocus;
	private boolean done;
	private boolean inReverse;
	private boolean relayoutNeeded;
	
	private Action finalConnectFwd;
	private Action finalConnectRev;

	
	public SubMenuInTransition(MenuSlice finalFocus, MenuSlice currHeroPic, int destPicX, int destPicY, int heroPicSpeed, MenuSlice buttonToMove, int destBtnX, int destBtnY, int buttonSpeed, Action connectFwd, Action connectRev) {
		//Setup: hero picture
		this.currHeroPicture = currHeroPic;
		this.heroPicRevX = currHeroPic.getPosX();
		this.heroPicRevY = currHeroPic.getPosY();
		this.heroPicDestX = destPicX;
		this.heroPicDestY = destPicY;
		this.heroPicSpeed = heroPicSpeed;
		
		//Setup: button to move
		this.buttonToMove = buttonToMove;
		this.buttonRevX = buttonToMove.getPosX();
		this.buttonRevY = buttonToMove.getPosY();
		this.buttonDestX = destBtnX;
		this.buttonDestY = destBtnY;
		this.buttonSpeed = buttonSpeed;
		
		//Save: hinting information
		this.heroPicXHint = currHeroPic.getInitialFormatArgs().xHint;
		this.heroPicYHint = currHeroPic.getInitialFormatArgs().yHint;
		this.buttonXHint = buttonToMove.getInitialFormatArgs().xHint;
		this.buttonYHint = buttonToMove.getInitialFormatArgs().yHint;
		
		//Save: actions
		this.finalConnectFwd = connectFwd;
		this.finalConnectRev = connectRev;

		this.finalFocus = finalFocus;
	
		this.phase = PHASE_ONE;
		this.inReverse = false;
		
		initDone();
		
//		System.out.println("Moving spells: " + buttonRevY + " to " + buttonDestY + " at speed: " + speed);
	}
	
	
	public void setupReverse() {
		this.buttonDestX = this.buttonRevX-(heroPicRevX-heroPicDestX);
		this.buttonDestY = this.buttonRevY-(heroPicRevY-heroPicDestY);
		this.heroPicDestX = this.heroPicRevX;
		this.heroPicDestY = this.heroPicRevY;
		
		this.phase = PHASE_TWO;
		this.finalFocus = currHeroPicture;
		this.inReverse = true;
		this.done = false;
		
		initDone();
	}
	
	
	private boolean moveCloser(MenuSlice component, int destX, int destY, int speed) {
		//Already there?
		if (component.getPosX()==destX && component.getPosY()==destY)
			return false;
		
		//MoveX
		int stepX = component.getPosX();
		if (component.getPosX()>destX) {
			stepX-=speed;
			if (stepX<destX)
				stepX = destX;
		} else if (component.getPosX()<destX) {
			stepX+=speed;
			if (stepX>destX)
				stepX = destX;
		}
		
		//MoveY
		int stepY = component.getPosY();
		if (component.getPosY()>destY) {
			stepY-=speed;
			if (stepY<destY)
				stepY = destY;
		} else if (component.getPosY()<destY) {
			stepY+=speed;
			if (stepY>destY)
				stepY = destY;
		}
		
		//Actually move
		component.forceToLocation(stepX, stepY);
		return (stepX==destX && stepY==destY);
	}
	
	

	public void step() {
		if (phase==PHASE_ONE) {
			//Phase one: move the current hero's picture
			int diffX = currHeroPicture.getPosX();
			int diffY = currHeroPicture.getPosY();
			if (moveCloser(currHeroPicture, heroPicDestX, heroPicDestY, heroPicSpeed)) {
				phaseOneDone();
				if (inReverse)
					phase = PHASE_DONE;
				else
					phase = PHASE_TWO;
			}
			
			//Also, move the button to catch up
			diffX -= currHeroPicture.getPosX();
			diffY -= currHeroPicture.getPosY();
			buttonToMove.forceToLocation(buttonToMove.getPosX()-diffX, buttonToMove.getPosY()-diffY);
		} else if (phase==PHASE_TWO) {
			//Phase two: move the button in question
			if (moveCloser(buttonToMove, buttonDestX, buttonDestY, buttonSpeed)) {
				phaseTwoDone();
				if (inReverse)
					phase = PHASE_ONE;
				else
					phase = PHASE_DONE;
			}
		} else {
			finalPhaseDone();
			done = true;
			relayoutNeeded = true;
		}
	}
	
	
	
	private void initDone() {
		if (inReverse) {
			//Disconnect
			finalConnectRev.perform(this);
		} else {
			//Disconnect
			for (int i=0; i<savedConnections.length; i++) {
				//Disconnect it?
				MenuSlice currConnect = currHeroPicture.getConnect(i, MenuSlice.CFLAG_PAINT);
				if (currConnect==null || currConnect.equals(buttonToMove))
					continue;
				
				//Make sure to save it...
				currHeroPicture.disconnect(i, MenuSlice.CFLAG_PAINT);
				savedConnections[i] = currConnect;
			}
		}
	}
	
	
	private void finalPhaseDone() {
		if (inReverse) {
			//Re-connect necessary components
			for (int i=0; i<savedConnections.length; i++) {
				MenuSlice toConnect = savedConnections[i];
				if (toConnect==null)
					continue;
				
				//Okay, connect it
				currHeroPicture.connect(toConnect, i, MenuSlice.CFLAG_PAINT);
			}
			
			//Restore our components; initial layout hints
			currHeroPicture.getInitialFormatArgs().xHint = heroPicXHint;
			currHeroPicture.getInitialFormatArgs().yHint = heroPicYHint;
			buttonToMove.getInitialFormatArgs().xHint = buttonXHint;
			buttonToMove.getInitialFormatArgs().yHint = buttonYHint;
		} else {
			//Connect our next bit
			finalConnectFwd.perform(this);
			
			//Now, mess with our components' format information so this isn't undone
			//   by the first re-layout.
			currHeroPicture.getInitialFormatArgs().xHint -= (heroPicRevX-heroPicDestX);
			currHeroPicture.getInitialFormatArgs().yHint -= (heroPicRevY-heroPicDestY);
			buttonToMove.getInitialFormatArgs().xHint -= ((buttonRevX-buttonDestX) - (heroPicRevX-heroPicDestX));
			buttonToMove.getInitialFormatArgs().yHint -= ((buttonRevY-buttonDestY) - (heroPicRevY-heroPicDestY));
		}
	}
	
	
	
	private void phaseTwoDone() {
		if (inReverse) {
		} else {
		}
	}
	
	
	private void phaseOneDone() {
		if (inReverse) {
		} else {
		}
	}
	
	
	
	//Mostly useless helpers
	public boolean doPaintOver() {
		return false;
	}
	public MenuSlice getNewFocus() {
		return finalFocus;
	}
	public boolean isDone() {
		return done;
	}
	public boolean requiresReLayout() {
		if (relayoutNeeded) {
			relayoutNeeded = false;
			return true;
		}
		return false;
	}
	public void reset() {
	}


}
