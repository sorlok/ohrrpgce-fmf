package ohrrpgce.menu;

import java.util.Vector;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.Hero;
import ohrrpgce.data.RPG;
import ohrrpgce.data.loader.PictureParser;
import ohrrpgce.henceforth.Int;


public class HeroSelectSlice extends MenuSlice {
	
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
    private static final int innerSpacing = 3;
    
    //Fields
    private int minElements;
    private int maxElements;
    private ImageSlice[] partyPictures;
    private int cursorTLX;
    
    
    private static ImageSlice[] getPartyAsButtons(Hero[] heroParty, RPG rpg) {
    	ImageSlice[] ret = new ImageSlice[heroParty.length];
    	MenuFormatArgs mf = new MenuFormatArgs();
    	mf.borderColors = new int[]{};
    	mf.bgColor = 0xFF0000;
    	mf.fillType = MenuSlice.FILL_NONE;
    	mf.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
    	mf.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
    	mf.yHint = cursorSize+2;
        for (int i=0; i<ret.length; i++) {
        	ret[i] = new ImageSlice(mf, heroParty[i].getWalkabout().spData[4], heroParty[i].walkaboutPaletteID, rpg, PictureParser.PT_WALKABOUT_SIZES[0]);
        	
        	if (i==0) {
        		mf.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
            	mf.xHint = innerSpacing;
            	mf.yHint = 0;
        	} else {
        		ret[i-1].connect(ret[i], MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
        	}
        }
        return ret;
    }
    
    
    
    public HeroSelectSlice(MenuFormatArgs mFormat, RPG game, int minElements, int maxElements) {
    	super(mFormat);
    	
        this.minElements = minElements;
        this.maxElements = maxElements;
    }
    

    public void setHeroParty(Hero[] heroParty, RPG rpg, int partyIDofUser) {
        //Setup
    	partyPictures = getPartyAsButtons(heroParty, rpg);
        setIDOfUser(partyIDofUser);
        this.setTopLeftChild(partyPictures[0]);
    }
    
    public void setIDOfUser(int partyIDOfUser) {
        this.cursorTLX = mFormat.borderPadding + partyIDOfUser*(innerSpacing+PictureParser.PT_WALKABOUT_SIZES[0]) + PictureParser.PT_WALKABOUT_SIZES[0]/2 - cursorSize/2;
    }
    
    protected void drawPixelBuffer(int atX, int atY) {
    	super.drawPixelBuffer(atX, atY);
    	
    	//Draw the cursor
//    	System.out.println(getPosX()+cursorTLX + "," + getPosY()+mFormat.borderColors.length+1);
        GraphicsAdapter.drawRGB(cursor, 0, cursorSize, 
                getPosX()+cursorTLX,
                getPosY()+mFormat.borderColors.length+1,
                cursorSize,
                cursorSize,
                true
                );
    }
    
    //Important over-rides
/*    protected void setHeight(int newHeight) {
    	super.setHeight(newHeight + cursorSize+2);
    }*/
    
    /*protected int calcMinWidth() {
        return (innerSpacing+PictureParser.PT_WALKABOUT_SIZES[0])*maxElements - innerSpacing;
    }
    
    protected int calcMinHeight() {
    	return cursorSize+2+PictureParser.PT_WALKABOUT_SIZES[1];    
    }*/
    
    
}
