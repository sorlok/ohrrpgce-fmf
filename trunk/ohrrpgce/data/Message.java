package ohrrpgce.data;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.game.LiteException;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;
import ohrrpgce.menu.TextBox;
import ohrrpgce.menu.TextSlice;

/**
 * The message boxes shown in OHR-created games.
 * @author Seth N. Hetu
 * @version 0.1
 */
public class Message {
        public static final int FONT_MARGIN = 2;
        public static final int FONT_SIZE = 8;
        public static final int LETTERS_PER_LINE = 16; //In the file.
        
        //Hackish, for now...
        public static int MAX_WIDTH=-1;
        public static int MAX_HEIGHT=-1;
        public static void initTextBox(int width, int height) {
            MAX_WIDTH = width;
            MAX_HEIGHT = height;
        }
    
        private static TextSlice displayBox;
        private String thisMsg;
        
        private RPG parent;
        public int boxColor;
        public int boxBorderColor;
    
        private int crX;
        private int topY;
        private int lowY;
       
        
        //Stuff to do
        public int[] tagChange1;
        public int[] tagChange2;
        public int[] jumpToBox;
        public int[] heroAddRem;
        public int[] heroSwap;
        public int[] heroLock;
        public int[] showBoxAfter;
	
        public Message(RPG parent) {
            this.parent = parent;
        }
        
        /**
         * Trigger a re-compute next time this box is drawn.
         */
        public void reset(String newText) {
        	if (MAX_WIDTH==-1 || MAX_HEIGHT==-1)
        		throw new LiteException(this, null, "Message Box constants not initialized.");
        	if (displayBox==null)
        		makeInitialDisplay();
		
        	this.thisMsg = newText;
        }
        
        
        public void loadBox() {
        	displayBox.setText(thisMsg);
        	displayBox.doLayout();
        }
	
	
        private void makeInitialDisplay() {
        	MenuFormatArgs mFormat = new MenuFormatArgs();
        	mFormat.bgColor = 0xFF000000|boxColor;
        	mFormat.borderColors = new int[]{boxBorderColor, 0x000000};
        	mFormat.borderPadding = Message.FONT_MARGIN;
        	mFormat.fillType = MenuSlice.FILL_TRANSLUCENT;
        	mFormat.xHint = 0;
        	mFormat.yHint = 0;
        	mFormat.widthHint = MAX_WIDTH;
        	mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
		
        	displayBox = new TextSlice(mFormat, "x", parent.font, false, true, true);
        	displayBox.setScrollLimit(MAX_HEIGHT);
        }
	

        public void processInput(int key) {
            if (key==NPC.DIR_UP)
            	displayBox.processInput(MenuSlice.CONNECT_TOP);
            else if (key == NPC.DIR_DOWN)
            	displayBox.processInput(MenuSlice.CONNECT_BOTTOM);
        }
        
        public void paint(int screenWidth, int screenHeight) {
            displayBox.paintAt(screenWidth/2-displayBox.getWidth()/2, 0);
        }
        

}
