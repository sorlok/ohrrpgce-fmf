package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.Message;
import ohrrpgce.game.LiteException;


/**
 * A vertical list of items
 *  Note that these lists are ALWAYS shadowed.
 * These lists maintain their own pixel buffers. This might seem wasteful (space-wise)
 *  but it's relatively fast to draw when scrolled. 
 * @author Seth N. Hetu
 */
public class ListSlice extends MenuSlice {
	//Ugh. Why did I like int arrays so much?
    private static final int[] validCursor = new int[] {
        0x00000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0x00000000, 0x00000000, 
        0xFFFFFFFF, 0xFFFF7373, 0xFFFF6666, 0xFFFF4545, 0xFFFF1212, 0xFFFFFFFF, 0x00000000, 
        0xFFFFFFFF, 0xFFFF4949, 0xFFFF3434, 0xFFFF0B0B, 0xFFD70000, 0xFFFFFFFF, 0xFF000000, 
        0xFFFFFFFF, 0xFFFF0F0F, 0xFFEE0000, 0xFFC60000, 0xFF9E0000, 0xFFFFFFFF, 0xFF000000, 
        0xFFFFFFFF, 0xFFCB0000, 0xFFA10000, 0xFF800000, 0xFF6B0000, 0xFFFFFFFF, 0xFF000000, 
        0x00000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFF000000, 
        0x00000000, 0x00000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0x00000000, 
    };
    private static final int[] invalidCursor = new int[] {
        0x00000000, 0xFF5C5C5C, 0xFF5C5C5C, 0xFF5C5C5C, 0xFF5C5C5C, 0x00000000, 0x00000000, 
        0xFF5C5C5C, 0xFFB9B9B9, 0xFFB2B2B2, 0xFFA2A2A2, 0xFF888888, 0xFF5C5C5C, 0x00000000, 
        0xFF5C5C5C, 0xFFA4A4A4, 0xFF999999, 0xFF858585, 0xFF6B6B6B, 0xFF5C5C5C, 0xFF000000, 
        0xFF5C5C5C, 0xFF878787, 0xFF777777, 0xFF636363, 0xFF4F4F4F, 0xFF5C5C5C, 0xFF000000, 
        0xFF5C5C5C, 0xFF656565, 0xFF505050, 0xFF404040, 0xFF353535, 0xFF5C5C5C, 0xFF000000, 
        0x00000000, 0xFF5C5C5C, 0xFF5C5C5C, 0xFF5C5C5C, 0xFF5C5C5C, 0xFF000000, 0xFF000000, 
        0x00000000, 0x00000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0x00000000, 
    };
    private static final int cursWidth = 7;
    private static final int cursHeight = 7;
    
    
    //Private fields
    private ImageAdapter font;
    private int onscreenItemCount;
    private int currItemID;
    private int maxItems;
    private int scrollOffset;
    private int onscreenPixels;
    
    //Easier to do it this way
    private int[] foreground;
    private int[] foregroundSize = new int[]{0,0};
    
    //Additional listeners
    private Action listItemChangedListener;
    
    
    public ListSlice(MenuFormatArgs mForm, ImageAdapter font) {
    	super(mForm);
    	if (mForm.widthHint<=0 || mForm.heightHint<=0)
    		throw new LiteException(this, new IllegalArgumentException(), "ListSlice cannot use any width hints.");
    	if (mForm.borderPadding==0)
    		mForm.borderPadding = 2;
    	
    	onscreenItemCount = mForm.widthHint/(Message.FONT_SIZE+1+mForm.borderPadding);
    	this.font = font;
    }
    
    public void setListItemChangedListener(Action listItemChangedListener) {
        this.listItemChangedListener = listItemChangedListener;
    }
    
    public void reset() {
        currItemID = 0;
    }
    
    public void clearItems() {
        foreground = null;
    }
    
    
    
    public void setItems(String[] names, int[] costs, boolean[] valids, boolean levelBasedMP) {
        //Get the maximum number of digits for spell cost.
        int maxCostDigits = 0;
        maxItems = names.length;
        onscreenPixels = Math.min(maxItems, onscreenItemCount)*(Message.FONT_SIZE+1+getInitialFormatArgs().borderPadding);
        if (!levelBasedMP) {
            for (int i=0; i<costs.length; i++) {
                if (costs[i] > maxCostDigits)
                    maxCostDigits = costs[i];
            }
            maxCostDigits = new String(""+maxCostDigits).length();
        }
        
        int maxNameDigits = (getInitialFormatArgs().widthHint-getInitialFormatArgs().borderColors.length*2-getInitialFormatArgs().borderPadding)/(Message.FONT_SIZE+1) - maxCostDigits - 1;
        foregroundSize[0] = (maxNameDigits+maxCostDigits+1)*(Message.FONT_SIZE+1)+getInitialFormatArgs().borderPadding;
        foregroundSize[1] = names.length*(Message.FONT_SIZE+1+getInitialFormatArgs().borderPadding);
        foreground = new int[foregroundSize[0]*foregroundSize[1]];
        
        int startY = getInitialFormatArgs().borderPadding;
        for (int itemID=0; itemID<names.length; itemID++) {
            //Build the cost backwards
            if (!levelBasedMP) {
                String cost = new String(""+costs[itemID]);
                for (int costDig=0; costDig<cost.length(); costDig++) {
                    int startX = (maxCostDigits-costDig-1)*(Message.FONT_SIZE+1) + getInitialFormatArgs().borderPadding;
                    drawShadedLetter(cost.charAt(cost.length()-costDig-1), startX, startY, foreground, foregroundSize[0], valids[itemID]);
                }
            }
            
            //Draw the seperator (cursor)
            int startX = maxCostDigits*(Message.FONT_SIZE+1) + getInitialFormatArgs().borderPadding;
            int[] toDraw = invalidCursor;
            if (valids[itemID])
                toDraw = validCursor;
            for (int y=0; y<cursHeight; y++) {
                for (int x=0; x<cursWidth; x++) {
                    foreground[(startY+y)*foregroundSize[0] + startX+x] = toDraw[y*cursWidth+x];
                }
            }
            startX += (Message.FONT_SIZE+1);
            
            //Draw the remaining text
            for (int i=0; i<names[itemID].length(); i++) {
                drawShadedLetter(names[itemID].charAt(i), startX, startY, foreground, foregroundSize[0], valids[itemID]);

                startX += (Message.FONT_SIZE+1);
            }
            
            //Increment for the next line.
            startY += (getInitialFormatArgs().borderPadding+Message.FONT_SIZE+1);
        }
    }
    
    
    
    
    
    private void drawShadedLetter(int letterID, int ulX, int ulY, int[] drawOnto, int drawAreaWidth, boolean enabled) {
        //Get letter data
        int fs = Message.FONT_SIZE;
        int lpl = Message.LETTERS_PER_LINE;
        int[] letter = new int[fs*fs];
        
        font.getRGB(letter, 0, fs, fs*(letterID%lpl), fs*(letterID/lpl), fs, fs);
        
        //Shade
        int color = 0xFF000000;
        for (int offset=1; offset>=0; offset--) {
            //Copy each row
            for (int y=0; y<fs; y++) {
                for (int x=0; x<fs; x++) {
                    if ((letter[y*fs + x]&0x00FFFFFF)!=0)
                        drawOnto[(ulY+y+offset)*drawAreaWidth + ulX+x+offset] = color;
                }
            }

            if (enabled)
                color = 0xFFFFFFFF;
            else
                color = 0xFF5C5C5C;
        }
    }
    
    
    
    //Helpful getters
    public int getNumItems() {
        return maxItems;
    }
    public int getCurrItemID() {
        return currItemID;
    }
    public int[] getCurrItemRectangle() {
        int tlX = getPosX()+getInitialFormatArgs().borderColors.length;
        int tlY = getPosY()+getInitialFormatArgs().borderColors.length+(Message.FONT_SIZE+1+getInitialFormatArgs().borderPadding)*currItemID;
        int w = getWidth()-getInitialFormatArgs().borderColors.length*2;
        int h = Message.FONT_SIZE+1+getInitialFormatArgs().borderPadding;
        return new int[]{tlX, tlY, w, h};
    }
    
    
    //Important over-rides
    public boolean consumeInput(int direction) {
    	if (maxItems==0)
    		return false;
    	
        if (direction==MenuSlice.CONNECT_TOP && currItemID!=0) {
            currItemID--;
            recalcScroll();
            if (listItemChangedListener!=null)
                listItemChangedListener.perform(this);
            return true;
        } else if (direction==MenuItem.CONNECT_BOTTOM && currItemID!=maxItems-1) {
            currItemID++;
            recalcScroll();
            if (listItemChangedListener!=null)
                listItemChangedListener.perform(this);
            return true;
        }
        return false;
    }    
    private void recalcScroll() {
        //All items fit on-screen
        if (maxItems <= onscreenItemCount) {
            scrollOffset = 0;
            return;
        }
        
        //NOTE: It's the OFFSET into the array where we'll begin drawing.
        //Not yet implemented...
        throw new LiteException(this, null, "recalcScroll() in List.java not implemented.");
    }
    protected void drawPixelBuffer(int atX, int atY) {
    	if (foreground != null) {
            //No need to clip if we do this properly.
            try {
            	GraphicsAdapter.drawRGB(foreground, scrollOffset, foregroundSize[0], getPosX()+getInitialFormatArgs().borderColors.length, getPosY()+getInitialFormatArgs().borderColors.length, foregroundSize[0], onscreenPixels, true);
            } catch (Exception ex)  {
                throw new LiteException(this, null, "Error drawing List display area.");
            }
        }
    }
    
    
    
}


