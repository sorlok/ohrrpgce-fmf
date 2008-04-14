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
        private static final int SIZE_HALVED = (FONT_SIZE+1)/2;
        private static final int SIZE_2X = 2*(FONT_SIZE+1);
        private static final int SCROLL_AMT = 9;
        
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
    
        private int scrollAmt;
        private int maxScrollAmt;
        private int crX;
        private int topY;
        private int lowY;
        
        private static int[] cursorDown;
        private static int[] cursorUp;
        
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
        	scrollAmt = 0;
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
        }
	
        
        /**
         * NOTE: This method will not scroll before the first call to paint()
         */
        private void scrollBox(int yPlus) {
            scrollAmt += yPlus;
            
            if (scrollAmt<0)
                scrollAmt = 0;
            if (scrollAmt>maxScrollAmt)
                scrollAmt = maxScrollAmt;
        }
        
        public void processInput(int key) {
            if (key==NPC.DIR_UP)
                scrollBox(-SCROLL_AMT);
            else if (key == NPC.DIR_DOWN)
                scrollBox(SCROLL_AMT);
        }
        
        public void paint(int screenWidth, int screenHeight) {
        	
            displayBox.paintAt(screenWidth/2-displayBox.getWidth()/2, -scrollAmt/2);
            
            maxScrollAmt = Math.max(displayBox.getHeight()-screenHeight+1, 0);
            
            if (maxScrollAmt > 0) {
                //Draw arrows to show that the box can be scrolled.
                if (cursorDown==null)
                    loadScrollCursor();
                
                crX = screenWidth/2+displayBox.getWidth()/2 - 2 - SIZE_HALVED - SIZE_2X;
                topY = 2+FONT_SIZE/2;
                lowY = screenHeight - 2 - SIZE_HALVED - SIZE_2X;
                
                if (scrollAmt>0) {
                    //Show the "up" arrow.
                	GraphicsAdapter.drawRGB(cursorUp, 0, SIZE_2X, crX, topY, SIZE_2X, SIZE_2X, true);
                }
                if (scrollAmt<maxScrollAmt) {
                    //Show the "down" arrow.
                	GraphicsAdapter.drawRGB(cursorDown, 0, SIZE_2X, crX, lowY, SIZE_2X, SIZE_2X, true);
                }
            }
        }
        
        private void loadScrollCursor() {
            int fs = FONT_SIZE;
            int[] letter = new int[fs*fs];
            parent.font.getRGB(letter, 0, fs, fs*(25%LETTERS_PER_LINE), fs*(25/LETTERS_PER_LINE), fs, fs);
            
            //Shade - note: this copied code combines TextBox(shade) & ImageBox(double)
            cursorDown = new int[SIZE_2X*SIZE_2X];
            cursorUp = new int[cursorDown.length];
            int color = 0xFF000000;
            for (int offset=1; offset>=0; offset--) {
                //Copy each row
                for (int y=0; y<fs; y++) {
                    for (int x=0; x<fs; x++) {
                        if ((letter[y*fs + x]&0x00FFFFFF)!=0) {
                            for (int yPlus=0; yPlus<2; yPlus++) {
                                for (int xPlus=0; xPlus<2; xPlus++) {
                                    cursorDown[(y+offset+yPlus)*SIZE_2X + x+offset+xPlus] = color;
                                }
                            }                            
                        }
                    }
                }
                
                color = 0xFFFFFFFF;
            }
            for (int y=0; y<SIZE_2X; y++) {
                for (int x=0; x<SIZE_2X; x++) {
                    cursorUp[(SIZE_2X-1-y)*SIZE_2X +x] = cursorDown[y*SIZE_2X +x];
                }
            }
        }
}
