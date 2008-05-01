package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;

public class CubeSlice extends MenuSlice {
	private ImageAdapter cubeImg;
	private char lastCubeSide;
	private char currCubeSide;

	//Liseners
	private Action subSelectionChangedListener;

	//Constructor
	public CubeSlice(MenuFormatArgs mFormat, ImageAdapter pngImage) {
		super(mFormat);
		this.cubeImg = pngImage;
		this.currCubeSide = 'L';
	}

	//Draw this slice
	protected void drawPixelBuffer(int atX, int atY) {
		if (cubeImg != null) {
			//Draw our image centered...
			int tlX = this.getPosX()
					+ (this.getWidth() / 2 - cubeImg.getWidth() / 2);
			int tlY = this.getPosY()
					+ (this.getHeight() / 2 - cubeImg.getHeight() / 2);

			GraphicsAdapter.drawImage(cubeImg, tlX, tlY);
		}
	}

	//Delegate input to "sub" sections of this Slice
	public boolean consumeInput(int direction) {
		if (direction == MenuSlice.CONNECT_LEFT
				&& (currCubeSide == 'R' || currCubeSide == 'T'))
			currCubeSide = 'L';
		else if (direction == MenuSlice.CONNECT_RIGHT
				&& (currCubeSide == 'L' || currCubeSide == 'T'))
			currCubeSide = 'R';
		else if (direction == MenuSlice.CONNECT_TOP
				&& (currCubeSide == 'L' || currCubeSide == 'R')) {
			lastCubeSide = currCubeSide;
			currCubeSide = 'T';
		} else if (direction == MenuSlice.CONNECT_BOTTOM && currCubeSide == 'T')
			currCubeSide = lastCubeSide;
		else
			return false;

		if (this.subSelectionChangedListener != null) {
			this.subSelectionChangedListener.perform(this);
		}
		return true;
	}

	
	// Allow better highlighting
	public int[] getActiveRectangle() {
		if (currCubeSide == 'L') {
			return new int[] { this.getPosX() + 17, this.getPosY() + 37, 21, 19 };
		} else if (currCubeSide == 'R') {
			return new int[] { this.getPosX() + 40, this.getPosY() + 38, 23, 20 };
		} else if (currCubeSide == 'T') {
			return new int[] { this.getPosX() + 28, this.getPosY() + 20, 24, 13 };
		} else {
			// This should never happen, but keep it just for consistency
			return super.getActiveRectangle();
		}
	}

	
	
	// Setter
	public void setSubSelectionChangedListener(Action newListener) {
		this.subSelectionChangedListener = newListener;
	}
	
	//Over-rides to allow MINIMUM width/height hints
	protected int calcMinWidth() {
		if (cubeImg != null) {
			return cubeImg.getWidth();
		} else {
			return super.calcMinWidth();
		}
	}
	protected int calcMinHeight() {
		if (cubeImg != null) {
			return cubeImg.getHeight();
		} else {
			return super.calcMinHeight();
		}
	}
}
