package ohrrpgce.menu.transitions;

import java.util.Arrays;
import java.util.List;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.menu.ImageSlice;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;

public class MainMenuItemInTransition extends Transition {
	private static final int defaultSpeed = 20; //pix/tick
	private static final int darkenInterval = 4; //Ticks
	
	private static final int PHASE_ONE = 1;
	private static final int PHASE_TWO = 2;
	private int phase;

	private int[] quarterBoxDark;
	private int quarterBoxWidth;
	private int quarterBoxHeight;
	
	private MenuSlice topmostBox;
	private MenuSlice itemToMove;
	private int destBoxX;
	
	private int currBlackIndex;
	private int speed;
	
	//Alpha
	int alphaInterval = 0x55;
	int currLayerCombinedAlpha;
	
	private boolean done;
	private boolean relayoutNeeded;
	
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
	public MainMenuItemInTransition(int destBoxX, MenuSlice selectedButton, int screenWidth, int screenHeight, MenuSlice topmostBox) {
		this.destBoxX = destBoxX;
		this.itemToMove = selectedButton;
		this.quarterBoxWidth = (int)Math.ceil(screenWidth/2.0F);
		this.quarterBoxHeight = (int)Math.ceil(screenHeight/2.0F);
		this.quarterBoxDark = new int[quarterBoxWidth*quarterBoxHeight];
		this.topmostBox = topmostBox;
		
		this.phase = PHASE_ONE;
		
		
		//Init our black value
		currLayerCombinedAlpha = 0xFF;
		Arrays.fill(quarterBoxDark, alphaInterval<<24);
		
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
			//System.out.println(Integer.toHexString(currLayerCombinedAlpha));
			
			return true;
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
			
			//temp
			done = true;
		}
		
		
		
		
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
		topmostBox.connect(box1, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		mf.fromAnchor = GraphicsAdapter.RIGHT|GraphicsAdapter.TOP;
		ImageSlice box2 = new ImageSlice(mf, darkerBox, quarterBoxWidth);
		box1.connect(box2, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		mf.fromAnchor = GraphicsAdapter.LEFT|GraphicsAdapter.BOTTOM;
		ImageSlice box3 = new ImageSlice(mf, darkerBox, quarterBoxWidth);
		box1.connect(box3, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		
		mf.fromAnchor = GraphicsAdapter.RIGHT|GraphicsAdapter.TOP;
		ImageSlice box4 = new ImageSlice(mf, darkerBox, quarterBoxWidth);
		box3.connect(box4, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		relayoutNeeded = true;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public boolean requiresReLayout() {
		relayoutNeeded = !relayoutNeeded;
		return !relayoutNeeded;
	}
	

}

