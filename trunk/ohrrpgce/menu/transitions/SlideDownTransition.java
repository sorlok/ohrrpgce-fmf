package ohrrpgce.menu.transitions;

import ohrrpgce.menu.CubeSlice;
import ohrrpgce.menu.MenuSlice;

public class SlideDownTransition extends Transition {

	private CubeSlice blueGraphic;
	
	//Slices to transition in and out (over) respectively
	private MenuSlice newSlice;
	private MenuSlice oldSlice;

	public SlideDownTransition(CubeSlice blueGraphic) {
		this.blueGraphic = blueGraphic;
	}

	public void setTargets(MenuSlice newMenu, MenuSlice oldMenu) {
		this.newSlice = newMenu;
		this.oldSlice = oldMenu;
	}

	public void reset() {
		// The old menu is currently connected to the right. Move it to the top,
		// and put our new slice on the right
		blueGraphic.disconnect(MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		blueGraphic.connect(oldSlice, MenuSlice.CONNECT_TOP,
				MenuSlice.CFLAG_PAINT);
		blueGraphic.connect(newSlice, MenuSlice.CONNECT_RIGHT,
				MenuSlice.CFLAG_PAINT);

		// Our new menu is currently in its "final" location. Let's move it up by its height in pixels:
		// We'll also set its clip here. Note that we use oldSlice's get() methods, because we know for
		//  sure that they're accurate (they were painted at least once).
		newSlice.getInitialFormatArgs().yHint -= oldSlice.getHeight();
		newSlice.setClip(oldSlice.getPosX(), oldSlice.getPosY(), oldSlice.getWidth(), oldSlice.getHeight());
	}

	public boolean isDone() {
		boolean done = newSlice.getPosY() == oldSlice.getPosY();
		if (done) {
			newSlice.setClip(null);
			blueGraphic.disconnect(MenuSlice.CONNECT_TOP, MenuSlice.CFLAG_PAINT);
		}
		return done;
	}

	 public void step() {
		int speed = 10;
		int displacement = oldSlice.getPosY() - newSlice.getPosY();
		if (displacement != 0) {
			int amt = Math.min(displacement, speed);
			newSlice.getInitialFormatArgs().yHint += amt;
		}
	}

	 //Throway methods
	 public boolean requiresReLayout() { return true; }
	 public boolean doPaintOver() { return false; }
	 public MenuSlice getNewFocus() { return null; }

}
