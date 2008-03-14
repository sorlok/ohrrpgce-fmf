/*
 * HeroSelector.java
 * Created on June 11, 2007, 9:24 PM
 */

package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.Hero;
import ohrrpgce.data.ImageBox;
import ohrrpgce.data.RPG;
import ohrrpgce.data.SolidBox;
import ohrrpgce.data.loader.PictureParser;

/**
 * Shows the list of heroes in the party, lets you pick one.
 * @author Seth N. Hetu
 */
public class HeroSelector extends Composite {
    
    private static final int MARGIN = 3;
    private static final int[] cursor = new int[] {
        0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFF000000, 0xFF000000, 0x00000000, 0x00000000, 0x00000000,
        0x00000000, 0x00000000, 0xFF000000, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFF000000, 0x00000000, 0x00000000,
        0x00000000, 0x00000000, 0xFF000000, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFF000000, 0x00000000, 0x00000000,
        0x00000000, 0xFF000000, 0xFF000000, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFF000000, 0xFF000000, 0x00000000,
        0xFF000000, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFF000000,
        0x00000000, 0xFF000000, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFF000000, 0x00000000,
        0x00000000, 0x00000000, 0xFF000000, 0xFFFFFF33, 0xFFFFFF33, 0xFFFFFF33, 0xFF000000, 0x00000000, 0x00000000,
        0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0xFFFFFF33, 0xFF000000, 0x00000000, 0x00000000, 0x00000000,
        0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xFF000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
    };
    private static final int cursorSize = 9;
    
    private RPG game;
    private int minElements;
    private int maxElements;
    private Canvas background;
//    private int partyIDofUser;
    
    //Speed up
    private int cursorTLX;
    private int[] border;
    
    private static MenuItem[] getPartyAsButtons(Hero[] heroParty, RPG game) {
        MenuItem[] ret = new MenuItem[heroParty.length];
        for (int i=0; i<ret.length; i++) {
            ImageBox pic = new ImageBox(heroParty[i].getWalkabout().spData[4], heroParty[i].walkaboutPaletteID, game, new int[]{PictureParser.PT_WALKABOUT_SIZES[0], PictureParser.PT_WALKABOUT_SIZES[1]}, 1, 0, new int[]{}, ImageBox.SCALE_NN, Canvas.FILL_GUESS);
            ret[i] = new  Button(
                    null, //Grr...
                    pic,
                    0, //Doesn't matter.
                    new int[0] //Size matters
                    );
            ret[i].setPosition(MARGIN, 0);
        }
        return ret;
    }
    
    /** 
     * Create a new HeroSelector 
     * @param heroParty To be drawn
     * @param minElements Show at least this many
     * @param maxElements Show at most this many
     * 
     */
    public HeroSelector(RPG game, int minElements, int maxElements) {
        super();
        this.isVertical = false;
        this.minElements = minElements;
        this.maxElements = maxElements;
        this.game = game;
        border = new int[]{game.getTextBoxColors(0)[1]|0xFF000000, 0xFF000000};
        
        setSize(MARGIN+(MARGIN+PictureParser.PT_WALKABOUT_SIZES[0])*maxElements + border.length*2, 
                cursorSize+2+MARGIN*2+PictureParser.PT_WALKABOUT_SIZES[1] + border.length*2);
    }
    
    public void setHeroParty(Hero[] heroParty, int partyIDofUser) {
        //Setup
        int toShow = Math.min(maxElements, Math.max(minElements, items.length));
      //  this.partyIDofUser = partyIDofUser;
        setIDOfUser(partyIDofUser);
        items = getPartyAsButtons(heroParty, game);
        connectItems();
        currItem = items[0];
        
        //Prepare background, set size, etc.
        background = new Canvas(
                border.length*2 + MARGIN + (PictureParser.PT_WALKABOUT_SIZES[0]+MARGIN)*toShow,
                border.length*2 + PictureParser.PT_WALKABOUT_SIZES[1] + MARGIN*2 + 2 + cursorSize,
                game.getTextBoxColors(0)[0],
                border, Canvas.FILL_SOLID
                );
    }

    public void setIDOfUser(int partyIDOfUser) {
        this.cursorTLX = MARGIN + partyIDOfUser*(MARGIN+PictureParser.PT_WALKABOUT_SIZES[0]) + PictureParser.PT_WALKABOUT_SIZES[0]/2 - cursorSize/2;
    }
    
    protected void paint(int[] originOffset) {
        //Draw the background box
        int tlX = originOffset[0] + getPosX();
        int tlY = originOffset[1] + getPosY();
        int flags = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
        background.paint(tlX, tlY, flags);
        
        //Characters
        //Delegate painting to sub-components recursively.
        items[0].repaint(new int[]{tlX, tlY+cursorSize+2+border.length});
        items[0].clearPaintFlag();
        
        //Current hero cursor
        GraphicsAdapter.drawRGB(cursor, 0, cursorSize, 
                tlX+cursorTLX,
                tlY+border.length+1,
                cursorSize,
                cursorSize,
                true
                );
    }

}





