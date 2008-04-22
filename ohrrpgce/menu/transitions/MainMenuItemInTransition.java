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
	private static final int PHASE_DONE = 3;
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
		if (!doInReverse)
			this.destTxtX = destButton.getPosX() + destButton.getWidth() - 1;
		else 
			this.destTxtX = currSubMenuLbl.getPosX() - currSubMenuLbl.getWidth();
		
		this.itemToMove = selectedButton;
		this.currLbl = currSubMenuLbl;
		this.quarterBoxWidth = (int)Math.ceil(screenWidth/2.0F);
		this.quarterBoxHeight = (int)Math.ceil(screenHeight/2.0F);
		this.quarterBoxDark = new int[quarterBoxWidth*quarterBoxHeight];
		this.topmostBox = topmostBox;
		
		if (!doInReverse)
			this.phase = PHASE_ONE;
		else
			this.phase = PHASE_TWO;
		
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
		
		//Set other values...
		this.reset();
	}
	
	
	public boolean doPaintOver() {
		if (phase==PHASE_ONE) {
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
			destOverlayY = overlaySlice.getPosY();
			overlaySlice.setClip(overlaySlice.getPosX(), overlaySlice.getPosY(), overlaySlice.getWidth()+1, overlaySlice.getHeight()+1);
			overlaySlice.forceToLocation(overlaySlice.getPosX(), destOverlayY-overlaySlice.getHeight());
			
			hackeroo = false;
		}
		
		return false;
	}
	
	public void reset() {
		done = false;
		relayoutNeeded = false;
		
		currBlackIndex = 0;
		
		//Boxes which are further away move in faster.
		speed = Math.max(defaultSpeed, (itemToMove.getPosX()-destBoxX)/darkenInterval);
	}

	public void step() {
		if (phase==PHASE_ONE) {
			currBlackIndex++;
			if (currBlackIndex>darkenInterval) {
				setupPhaseTwo();
				phase = PHASE_TWO;
			}
		} else if (phase==PHASE_TWO) {
			
			//NOTE: refactor this out into "movecloser(box, dest, modifier(-1))"
			int currBoxX = itemToMove.getPosX();
            if (currBoxX > destBoxX+speed) {
            	itemToMove.forceToLocation(currBoxX-speed, itemToMove.getPosY());
            } else if (currBoxX > destBoxX) {
            	itemToMove.forceToLocation(destBoxX, itemToMove.getPosY());
            	setupPhaseTwoPointFive();
            } else if (currBoxX != destBoxX) {
            	throw new LiteException(this, null, "Bad currBoxX: " + currBoxX + "  in regards to destBoxX: " + destBoxX);
            }
            
            int currTxtX = currLbl.getPosX();
            int currOverlayY = -5;
            if (currBoxX==destBoxX) {
                //Move the text label?
                if (currTxtX < destTxtX-defaultSpeed) {
                	currLbl.forceToLocation(currTxtX + defaultSpeed, currLbl.getPosY());
                } else if (currTxtX < destTxtX) {
                    currTxtX = destTxtX;
                    setupPhaseTwoPointSevenFive();
                } else if (currTxtX != destTxtX) {
                    throw new LiteException(this, null, "Bad currTxtX: " + currTxtX + "  in regards to destTxtX: " + destTxtX);
                }
                
                //Move the overlay?
                currOverlayY = overlaySlice.getPosY();
                if (currOverlayY < destOverlayY-defaultSpeed*3) {
                	overlaySlice.forceToLocation(overlaySlice.getPosX(), currOverlayY+defaultSpeed*3);
                } else if (currOverlayY < destOverlayY) {
                	overlaySlice.forceToLocation(overlaySlice.getPosX(), currOverlayY);
                	setupPhaseTwoPointEightish();
                } else if (currOverlayY != destOverlayY) {
                    throw new RuntimeException("Bad currBkgrdY: " + currOverlayY + "  in regards to destBkgrdY: " + destOverlayY);
                }
            }
            
			if (itemToMove.getPosX()==destBoxX && currLbl.getPosX()==destTxtX && currOverlayY==destOverlayY) {
				phase = PHASE_DONE;
			}
		} else if (phase==PHASE_DONE) {
			//Ok, a few things here...
			MenuFormatArgs mForm = new MenuFormatArgs(currLbl.getInitialFormatArgs());
			mForm.fromAnchor = GraphicsAdapter.HCENTER|GraphicsAdapter.VCENTER;
			mForm.toAnchor = GraphicsAdapter.HCENTER|GraphicsAdapter.VCENTER;
			finalItem = new TextSlice(mForm, "(Incomplete)", ((TextSlice)currLbl).getFont(), true, true, false);
			finalItem.addFocusGainedListener(new MetaMenu.MakeHighlightAction());
			overlaySlice.setTopLeftChild(finalItem);
			
			//Also layout
			done = true;
			relayoutNeeded = true;
		}
		
		
		
		
	}
	
	
	public MenuSlice getNewFocus() {
		return finalItem;
	}
	
	
	private void setupPhaseTwoPointFive() {
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
		//Place our label correctly
		currLbl.getInitialFormatArgs().fromAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.RIGHT;
		currLbl.getInitialFormatArgs().toAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.LEFT;
		currLbl.getInitialFormatArgs().xHint = -1;
		
		currLbl.setClip(null);
		
		relayoutNeeded = true;
	}
	
	
	private void setupPhaseTwoPointEightish() {
		overlaySlice.setClip(null);
		
		relayoutNeeded = true;
	}
	
	
	private void setupPhaseTwo() {		
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

