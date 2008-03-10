package ohrrpgce.adapter.midlet;

import javax.microedition.lcdui.game.GameCanvas;

public class InputAdapter implements ohrrpgce.adapter.InputAdapter {
    public static final int ALL_MENU_BUTTONS = GameCanvas.GAME_A_PRESSED|GameCanvas.GAME_B_PRESSED|GameCanvas.GAME_C_PRESSED|GameCanvas.GAME_D_PRESSED;

    private GameCanvas parent;
	
    public InputAdapter(GameCanvas parent) {
	this.parent = parent;
    }
    
    public int getKeyStates() {
        int val = parent.getKeyStates();
        if ((val & GameCanvas.LEFT_PRESSED)!=0)            
            return InputAdapter.KEY_LEFT;
        else if ((val & GameCanvas.RIGHT_PRESSED)!=0)
            return InputAdapter.KEY_RIGHT;
        else if ((val & GameCanvas.UP_PRESSED)!=0)
            return InputAdapter.KEY_UP;
        else if ((val & GameCanvas.DOWN_PRESSED)!=0)
            return InputAdapter.KEY_DOWN;
        else if ((val & ALL_MENU_BUTTONS)!=0)
            return InputAdapter.KEY_CANCEL;
        else if ((val & GameCanvas.FIRE_PRESSED)!=0)
            return InputAdapter.KEY_ACCEPT;
        return 0;
    }
}
