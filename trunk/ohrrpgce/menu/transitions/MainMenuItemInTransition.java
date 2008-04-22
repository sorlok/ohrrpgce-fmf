package ohrrpgce.menu.transitions;

import java.util.Arrays;
import java.util.List;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.game.LiteException;
import ohrrpgce.menu.ImageSlice;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;
import ohrrpgce.menu.TextSlice;
import ohrrpgce.runtime.MetaMenu;

public class MainMenuItemInTransition extends Transition {
	private static final int defaultSpeed = 20; //pix/tick
	private static final int darkenInterval = 4; //Ticks
	
	private static final int PHASE_ONE = 1;
	private static final int PHASE_TWO = 2;
	private static final int PHASE_THREE = 3;
	private static final int PHASE_DONE = 4;
	private int phase;

	private int[] quarterBoxDark;
	private int quarterBoxWidth;
	private int quarterBoxHeight;
	
	private MenuSlice topmostBox;
	private MenuSlice itemToMove;
	private MenuSlice currLbl;
	private MenuSlice overlaySlice;
	private int destBoxX;
	private int destTxtX;
	private int destOverlayY;
		
	private int currBlackIndex;
	private int speed;
	
	private boolean doInReverse;
	
	//Alpha
	int alphaInterval = 0x55;
	int currLayerCombinedAlpha;
	
	private boolean done;
	private boolean relayoutNeeded;
	
	private boolean hackeroo;
	
	private int globalMod;
	
	private MenuSlice finalItem;
	
	
	
	/**
	 * There's many ways to do transitions, but we'll do it this way:
	 *   1) Calling the constructor sets up all menu components
	 *   2) Calling step() (when it will return false) will un-set all menu components.
	 *   3) All animation is handled by updating components in "step".
	 * This way, all Transition control is located in THIS object, not half here and
	 *   half in the Menu Engine setup code. 
	 * @param destBoxX
	 * @param selectedButton
	 */
	public MainMenuItemInTransition(MenuSlice srcButton, MenuSlice destButton, MenuSlice selectedButton, MenuSlice currSubMenuLbl, int screenWidth, int screenHeight, MenuSlice topmostBox, boolean doInReverse) {
		this.doInReverse = doInReverse;
		
		if (!doInReverse) {
			selectedButton.getInitialFormatArgs().xHint = srcButton.getPosX();
			selectedButton.getInitialFormatArgs().yHint = srcButton.getPosY();
		}
		
		this.destBoxX = destButton.getPosX(); 
		if (!doInReverse) {
			this.destTxtX = destButton.getPosX() + destButton.getWidth() - 1;
			globalMod = 1;
		} else { 
			this.destTxtX = currSubMenuLbl.getPosX() - currSubMenuLbl.getWidth();
			globalMod = -1;
		}
		
		this.itemToMove = selectedButton;
		this.currLbl = currSubMenuLbl;
		this.quarterBoxWidth = (int)Math.ceil(screenWidth/2.0F);
		this.quarterBoxHeight = (int)Math.ceil(screenHeight/2.0F);
		this.quarterBoxDark = new int[quarterBoxWidth*quarterBoxHeight];
		this.topmostBox = topmostBox;
		
		if (!doInReverse)
			this.phase = PHASE_ONE;
		else
			this.phase = PHASE_THREE;
		
		//Connect our components and get an initial layout...
		if (!doInReverse) {
			itemToMove.disconnect(MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
			topmostBox.connect(itemToMove, MenuSlice.CONNECT_TOP, MenuSlice.CFLAG_PAINT);
			topmostBox.doLayout();
			topmostBox.disconnect(MenuSlice.CONNECT_TOP, MenuSlice.CFLAG_PAINT);
		}
		
		//Init our black value
		if (!doInReverse) {
			currLayerCombinedAlpha = 0xFF;
			Arrays.fill(quarterBoxDark, alphaInterval<<24);
		}
		
		//Finally..
		if (doInReverse) {
			overlaySlice = itemToMove.getConnect(MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
			overlaySlice.setTopLeftChild(null);
			destOverlayY = -overlaySlice.getHeight();
			finalItem = destButton;
		}
		
		//Set other values...
		this.reset();
	}
	
	
	public boolean doPaintOver() {
		if (phase==PHASE_ONE) {
			if (doInReverse && currBlackIndex == darkenInterval) {
				//Slightly hackish
				hackeroo = true;
				return false;
			}
			
			//Draw our ever-darkening boxes
			for (int y=0; y<2; y++) {
				for (int x=0; x<2; x++) {
					GraphicsAdapter.drawRGB(quarterBoxDark, 0, quarterBoxWidth, x*quarterBoxWidth, y*quarterBoxHeight, quarterBoxWidth, quarterBoxHeight, true);
				}
			}
			
			//Track this layer's alpha value
			currLayerCombinedAlpha = 0xFF&((currLayerCombinedAlpha*(0xFF-alphaInterval))/0xFF);
			
			//Paint the current item on top of the dark overlay
			itemToMove.paintAt(itemToMove.getPosX(), itemToMove.getPosY());
			
			return true;
		} else if (hackeroo) {
			if (!doInReverse) {			
				destOverlayY = overlaySlice.getPosY();
				overlaySlice.setClip(overlaySlice.getPosX(), overlaySlice.getPosY(), overlaySlice.getWidth()+1, overlaySlice.getHeight()+1);
				overlaySlice.forceToLocation(overlaySlice.getPosX(), destOverlayY-overlaySlice.getHeight());
			} else 
				itemToMove.disconnect(MenuSlice.CONNECT_LEFT, MenuSlice.CFLAG_PAINT);
			
			hackeroo = false;
		}
		
		return false;
	}
	
	public void reset() {
		done = false;
		relayoutNeeded = false;
		
		if (!doInReverse)
			currBlackIndex = 0;
		else 
			currBlackIndex = darkenInterval;
		
		//Boxes which are further away move in faster.
		speed = Math.max(defaultSpeed, (itemToMove.getPosX()-destBoxX)/darkenInterval);
	}
	
	
	
	//mod:1 = move right/down, -1 = move left/up
	//returns "true" if the item "just reached" the location
	private boolean moveCloser(MenuSlice box, int dest, int moveSpeed, boolean isY, int modifier) {
		//Are we there?
		int magnitude = isY ? box.getPosY() : box.getPosX();
		if (magnitude==dest)
			return false;
		
		//Are we far away?
		int oneStep = dest - modifier*moveSpeed;
        if (modifier*magnitude < modifier*oneStep) {
        	if (!isY)
        		box.forceToLocation(box.getPosX()+modifier*moveSpeed, box.getPosY());
        	else
        		box.forceToLocation(box.getPosX(), box.getPosY()+modifier*moveSpeed);
        	return false;
        }
        
        //Are we at the right spot?
        if (modifier*magnitude < modifier*dest) {
        	if (!isY)
        		box.forceToLocation(dest, box.getPosY());
        	else
        		box.forceToLocation(box.getPosX(), dest);
        	return true;
        }
        
        //Error otherwise
        throw new LiteException(this, null, "Bad magnitude: " + magnitude + "  in regards to destination: " + dest);
	}
	
	private boolean moveCloserX(MenuSlice box, int destX, int moveSpeed, int modifier) {
		return moveCloser(box, destX, moveSpeed, false, modifier);
	}
	
	private boolean moveCloserY(MenuSlice box, int destY, int moveSpeed, int modifier) {
		return moveCloser(box, destY, moveSpeed, true, modifier);
	}
	

	public void step() {
		if (phase==PHASE_ONE) {
			if (!doInReverse) {
				currBlackIndex++;
				if (currBlackIndex>darkenInterval) {
					setupPhaseTwo();
					phase = PHASE_TWO;
				}
			} else {
				currBlackIndex--;
				if (currBlackIndex<=0) {
					setupPhaseTwo();
					phase = PHASE_DONE;
				}
			}
		} else if (phase==PHASE_TWO) {
			//Move our button
			if (moveCloserX(itemToMove, destBoxX, speed, -globalMod)) {
				setupPhaseTwoPointFive();
				if (!doInReverse)
					phase = PHASE_THREE;
				else
					phase = PHASE_ONE;
			}
		} else if (phase==PHASE_THREE) {
			//Move other components?
			int cachedOverlayY = overlaySlice.getPosY();
			int cachedLabelX = currLbl.getPosX();
     
			//Move the text label?
            if (moveCloserX(currLbl, destTxtX, defaultSpeed, globalMod)) {
            	setupPhaseTwoPointSevenFive();
            }
            
            //Move the overlay?
            if (moveCloserY(overlaySlice, destOverlayY, defaultSpeed*3, globalMod)) {
            	setupPhaseTwoPointEightish();
            }
			if (cachedOverlayY==destOverlayY && cachedLabelX==destTxtX) {
				if (!doInReverse)
					phase = PHASE_DONE;
				else
					phase = PHASE_TWO;
			}
		} else if (phase==PHASE_DONE) {
			//Ok, a few things here...
			if (!doInReverse) {
				MenuFormatArgs mForm = new MenuFormatArgs(currLbl.getInitialFormatArgs());
				mForm.fromAnchor = GraphicsAdapter.HCENTER|GraphicsAdapter.VCENTER;
				mForm.toAnchor = GraphicsAdapter.HCENTER|GraphicsAdapter.VCENTER;
				finalItem = new TextSlice(mForm, "(Incomplete)", ((TextSlice)currLbl).getFont(), true, true, false);
				finalItem.addFocusGainedListener(new MetaMenu.MakeHighlightAction());
				overlaySlice.setTopLeftChild(finalItem);
			} else {
				itemToMove.connect(currLbl, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
			}
			
			//Also layout
			done = true;
			relayoutNeeded = true;
		}
		
	}
	
	
	public MenuSlice getNewFocus() {
		return finalItem;
	}
	
	
	private void setupPhaseTwoPointFive() {
		if (doInReverse) {
			
			return;
		}
		
		//Place our label correctly
		currLbl.getInitialFormatArgs().fromAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.LEFT;
		currLbl.getInitialFormatArgs().toAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.RIGHT;
		currLbl.getInitialFormatArgs().xHint = 0;
		currLbl.getInitialFormatArgs().yHint = 0;
		itemToMove.getInitialFormatArgs().xHint = itemToMove.getPosX();
		itemToMove.connect(currLbl, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		//Make a new overlay, and size it correctly
		MenuFormatArgs mForm = new MenuFormatArgs(currLbl.getInitialFormatArgs());
		mForm.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mForm.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		mForm.widthHint = topmostBox.getWidth() - 2*itemToMove.getPosX();
		mForm.heightHint = topmostBox.getHeight() - (itemToMove.getPosY()+itemToMove.getHeight()) - 1 - itemToMove.getPosX();
		overlaySlice = new MenuSlice(mForm);
		itemToMove.connect(overlaySlice, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		
		//Clipping
		currLbl.setClip(destTxtX, itemToMove.getPosY(), topmostBox.getWidth(), itemToMove.getHeight());
		
		relayoutNeeded = true;
		hackeroo = true;
	}

	
	private void setupPhaseTwoPointSevenFive() {
		if (doInReverse) {
			itemToMove.disconnect(MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
			return;
		}
		
		//Place our label correctly
		currLbl.getInitialFormatArgs().fromAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.RIGHT;
		currLbl.getInitialFormatArgs().toAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.LEFT;
		currLbl.getInitialFormatArgs().xHint = -1;
		
		//currLbl.setClip(null);
		
		relayoutNeeded = true;
	}
	
	
	private void setupPhaseTwoPointEightish() {
		if (doInReverse) {
			itemToMove.disconnect(MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
			return;
		}
		
		overlaySlice.setClip(null);
		
		relayoutNeeded = true;
	}
	
	
	private void setupPhaseTwo() {
		if (doInReverse) {
			topmostBox.getTopLeftChild().disconnect(MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
			return;
		}
		
		//Set up our black overlays
		quarterBoxDark = null;
		int[] darkerBox = new int[quarterBoxHeight*quarterBoxWidth];
		Arrays.fill(darkerBox, (0xFF-currLayerCombinedAlpha)<<24);

		//Our quarter box.
		MenuFormatArgs mf = new MenuFormatArgs();
		mf.widthHint = quarterBoxWidth;
		mf.heightHint = quarterBoxHeight;
		mf.borderColors =  new int[]{};
		mf.fillType = MenuSlice.FILL_NONE;
		ImageSlice box1 = new ImageSlice(mf, darkerBox, quarterBoxWidth);
		topmostBox.getTopLeftChild().connect(box1, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		box1.connect(itemToMove, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		mf.fromAnchor = GraphicsAdapter.RIGHT|GraphicsAdapter.TOP;
		ImageSlice box2 = new ImageSlice(mf, darkerBox, quarterBoxWidth);
		box1.connect(box2, MenuSlice.CONNECT_TOP, MenuSlice.CFLAG_PAINT);
		
		mf.fromAnchor = GraphicsAdapter.LEFT|GraphicsAdapter.BOTTOM;
		ImageSlice box3 = new ImageSlice(mf, darkerBox, quarterBoxWidth);
		box2.connect(box3, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		mf.fromAnchor = GraphicsAdapter.LEFT|GraphicsAdapter.TOP;
		mf.toAnchor = GraphicsAdapter.RIGHT|GraphicsAdapter.TOP;
		ImageSlice box4 = new ImageSlice(mf, darkerBox, quarterBoxWidth);
		box3.connect(box4, MenuSlice.CONNECT_TOP, MenuSlice.CFLAG_PAINT);
		
		relayoutNeeded = true;
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
	

}

