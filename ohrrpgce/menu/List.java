/*
 * List.java
 * Created on June 12, 2007, 3:45 PM
 */

package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.Message;
import ohrrpgce.game.SimpleCanvas;

/**
 * A vertical list of items
 *  Note that these lists are ALWAYS shadowed.
 * @author Seth N. Hetu
 */
public class List extends MenuItem {
    
    private int margin;
    private ImageAdapter font;
   // private int[] size;
    private int numBorders;
    
    private SimpleCanvas background;
    private int[] foreground;
    private int[] foregroundSize = new int[2];
    
    private int onscreenItems;
    private int onscreenPixels;
    private int scrollOffset;
    
    private boolean levelBasedMP;
    
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
    
    private Action listItemChangedListener;
    private int currItem;
    private int maxItems;
    
    
    public List(int prefWidth, int prefHeight, int bkgrdColor, int[] borderColors, int margin, ImageAdapter font) {
        this.margin = margin;
        this.font = font;
        setSize(prefWidth, prefHeight);
        this.numBorders = borderColors.length;
        background = new SimpleCanvas(prefWidth, prefHeight, bkgrdColor, borderColors, MenuSlice.FILL_GUESS);
        onscreenItems = prefHeight/(Message.FONT_SIZE+1+margin);
        
    }
    
    
    public void setListItemChangedListener(Action listItemChangedListener) {
        this.listItemChangedListener = listItemChangedListener;
    }
    
    public void reset() {
        currItem = 0;
    }
    
    public MenuItem processInput(int direction) {
        switch (direction) {
            case MenuItem.CONNECT_TOP:
                if (currItem==0 || maxItems==0) {
                    return super.processInput(direction);
                }
                currItem--;
                recalcScroll();
                if (listItemChangedListener!=null)
                    listItemChangedListener.perform(this);
                    return this;
            case MenuItem.CONNECT_BOTTOM:
                if (currItem==maxItems-1  || maxItems==0) {
                    return super.processInput(direction);
                }
                currItem++;
                recalcScroll();
                if (listItemChangedListener!=null)
                    listItemChangedListener.perform(this);
                    return this;
            default:
                return super.processInput(direction); //Just change location.
        }
    }

    public void accept() {
        //return this;
    }

    
    public int getNumItems() {
        return maxItems;
    }
    
    public int getCurrItemID() {
        return currItem;
    }
    
    private void recalcScroll() {
        //All items fit on-screen
        if (maxItems <= onscreenItems) {
            scrollOffset = 0;
            return;
        }
        
        
        //NOTE: It's the OFFSET into the array where we'll begin drawing.
        throw new RuntimeException("recalcScroll() in List.java not implemented.");
        
    }
    
    
    public int[] getCurrItemRectangle() {
        int tlX = getLastPaintedOffsetX()+getPosX()+numBorders;
        int tlY = getLastPaintedOffsetY()+getPosY()+numBorders+(Message.FONT_SIZE+1+margin)*currItem;
        int w = getWidth()-numBorders*2;
        int h = Message.FONT_SIZE+1+margin;
        return new int[]{tlX, tlY, w, h};
    }
    
    
    
    
    public void clearItems() {
        foreground = null;
    }
    
    public void setItems(String[] names, int[] costs, boolean[] valids, boolean levelBasedMP) {
        //Get the maximum number of digits for spell cost.
        int maxCostDigits = 0;
        maxItems = names.length;
        onscreenPixels = Math.min(maxItems, onscreenItems)*(Message.FONT_SIZE+1+margin);
        if (!levelBasedMP) {
            for (int i=0; i<costs.length; i++) {
                if (costs[i] > maxCostDigits)
                    maxCostDigits = costs[i];
            }
            maxCostDigits = new String(""+maxCostDigits).length();
        }
        
        int maxNameDigits = (getWidth()-numBorders*2-margin)/(Message.FONT_SIZE+1) - maxCostDigits - 1;
        foregroundSize[0] = (maxNameDigits+maxCostDigits+1)*(Message.FONT_SIZE+1)+margin;
        foregroundSize[1] = names.length*(Message.FONT_SIZE+1+margin);
        foreground = new int[foregroundSize[0]*foregroundSize[1]];
        
        int startY = margin;
        for (int itemID=0; itemID<names.length; itemID++) {
            //Build the cost backwards
            if (!levelBasedMP) {
                String cost = new String(""+costs[itemID]);
                for (int costDig=0; costDig<cost.length(); costDig++) {
              //      System.out.println("int startX = (maxNameDigits-costDig-1)*(Message.FONT_SIZE+1) + margin;");
               //     System.out.println(maxNameDigits + "," + costDig + "," + Message.FONT_SIZE + "," + margin);
                    int startX = (maxCostDigits-costDig-1)*(Message.FONT_SIZE+1) + margin;
                    drawShadedLetter(cost.charAt(cost.length()-costDig-1), startX, startY, foreground, foregroundSize[0], valids[itemID]);
                }
            }
            
            //Draw the seperator (cursor)
            int startX = maxCostDigits*(Message.FONT_SIZE+1) + margin;
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
            startY += (margin+Message.FONT_SIZE+1);
        }
        
        //Test:
      /*  for (int row=0; row<20; row++) {
            for (int i=0; i<foregroundSize[0]; i++) {
                if (foreground[row*foregroundSize[0]+i]!=0)
                    System.out.print("X");
                else
                    System.out.print(".");
            }
            System.out.println();
        }*/
    }
    
    
    
    
    
    private void drawShadedLetter(int letterID, int ulX, int ulY, int[] drawOnto, int drawAreaWidth, boolean enabled) {
       // System.out.println("Width: " + drawAreaWidth);
       // System.out.println("start: " + ulX + "," + ulY);
       // System.exit(1);
        
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
    
    
    
    protected void paint(int[] originOffset) {
        int tlX = originOffset[0]+getPosX();
        int tlY = originOffset[1]+getPosY();
        
        //Paint our two boxes.
        background.paint(tlX, tlY, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
        if (foreground != null) {
            //No need to clip if we do this properly.
            //int[] oldClip = new int[]{g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight()};
            //g.setClip(tlX, tlY, size[0], size[1]);
            try {
            	GraphicsAdapter.drawRGB(foreground, scrollOffset, foregroundSize[0], tlX+numBorders, tlY+numBorders, foregroundSize[0], onscreenPixels, true);
            } catch (Exception ex)  {
                throw new RuntimeException("Error drawing List display area.");
            }
            //g.setClip(oldClip[0], oldClip[1], oldClip[2], oldClip[3]);
        }
    }
    
    
}


