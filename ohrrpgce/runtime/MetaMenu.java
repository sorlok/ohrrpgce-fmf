package ohrrpgce.runtime;

import java.io.IOException;

import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.applet.ImageAdapter;
import ohrrpgce.data.RPG;
import ohrrpgce.game.LiteException;
import ohrrpgce.henceforth.Int;
import ohrrpgce.menu.ImageSlice;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;

public class MetaMenu {
	
    private static final int DEFAULT_INTER_ELEMENT_SPACING = 3;
    private static final int DEFAULT_BORDER_PADDING = 2;
    
    //States, texts
    private static final int MAIN = 0;
    private static final int SPELLS = 1;
    private static final int EQUIP = 2;
    private static final int STATS = 3;
    private static final int ITEMS = 4;
    private static final int ORDER = 5;
    private static final int MAP = 6;
    private static final int SAVE = 7;
    private static final int VOLUME = 8;
    private static final int QUIT = 9;
    private static final int HERO = 10; //Label only
    
    //Main menu stuff
    private static final int[] mainTextsIDs = new int[] {ITEMS, ORDER, MAP, SAVE, VOLUME, QUIT};
    private static final int[] mainColors = new int[] {2, 3, 6, 5, 4, 7};
    private static final String[] mainImageFiles = new String[] {
        "main_icons/items.png",
        "main_icons/order.png",
        "main_icons/map.png",
        "main_icons/save.png",
        "main_icons/volume.png",
        "main_icons/quit.png",
    };
    

    
	
	public static MenuSlice buildSimpleMenu(int width, int height) {
		//First test: some simple boxes
		MenuFormatArgs mFormat = new MenuFormatArgs();
		mFormat.bgColor = 0xCC2200;
		mFormat.borderColors = new int[]{0xFF0000, 0};
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.xHint = 10;
		mFormat.yHint = 10;
		mFormat.widthHint = 50;
		mFormat.heightHint = 50;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		
		MenuSlice firstBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0x22CC00;
		mFormat.yHint = 0;
		MenuSlice secondBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0x2200CC;
		mFormat.xHint = 0;
		mFormat.yHint = 10;
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice thirdBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0xFFFFFF;
		mFormat.fillType = MenuSlice.FILL_NONE;
		mFormat.xHint = 10;
		mFormat.yHint = 0;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice fourthBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0x66CCCC22;
		mFormat.fillType = MenuSlice.FILL_TRANSLUCENT;
		mFormat.xHint = 0;
		mFormat.yHint = 10;
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice fifthBox = new MenuSlice(mFormat);
		
		firstBox.connect(secondBox, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		firstBox.connect(thirdBox, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		thirdBox.connect(fourthBox, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		fourthBox.connect(fifthBox, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		
		return firstBox;
	}
	
	
	public static MenuSlice buildHierarchicalMenu(int width, int height) {
		//Now, test interior boxes
		MenuFormatArgs mFormat = new MenuFormatArgs();
		mFormat.bgColor = 0x770077;
		mFormat.borderColors = new int[]{0xFF00FF, 0};
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.xHint = 0;
		mFormat.yHint = 0;
		mFormat.widthHint = width;
		mFormat.heightHint = height;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice topBox = new MenuSlice(mFormat);
		
		//First inner box
		mFormat.bgColor = 0x008800;
		mFormat.borderColors[0] = 0x00DD00;
		mFormat.xHint = 10;
		mFormat.yHint = 10;
		mFormat.widthHint = 42;
		mFormat.heightHint = 42;
		MenuSlice firstInner = new MenuSlice(mFormat);
		
		topBox.setTopLeftChild(firstInner);
		
		//Test center layout and also overlap...
		mFormat.bgColor = 0x000088;
		mFormat.borderColors[0] = 0x0000DD;
		mFormat.xHint = 0;
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
		MenuSlice secondInner = new MenuSlice(mFormat);
		
		firstInner.connect(secondInner, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		
		//Test more layout
		mFormat.bgColor = 0x880000;
		mFormat.borderColors[0] = 0xDD0000;
		mFormat.xHint = 10;
		mFormat.yHint = 0;
		mFormat.fromAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice thirdInner = new MenuSlice(mFormat);
		
		secondInner.connect(thirdInner, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		return topBox;
	}
	
	
	
	public static MenuSlice buildMenu(int width, int height, RPG rpg, AdapterGenerator adaptGen) {
		//Get colors. We "lighten" a color to provide our basic menu...
        int[] colorZero = rpg.getTextBoxColors(0);
        int[] colorZeroLight = new int[]{
            Math.min((((colorZero[0]&0xFF0000)/0x10000)*14)/10, 0xFF)*0x10000+
                    Math.min((((colorZero[0]&0xFF00)/0x100)*14)/10, 0xFF)*0x100+
                    Math.min(((colorZero[0]&0xFF)*14)/10, 0xFF)+0xFF000000,
            0xFF888888
        };
		
		
		//Requires a "clear" top-level box with no border, etc. I don't like it so much, but
        //   it is the "proper" way to do it.
		MenuFormatArgs mFormat = new MenuFormatArgs();
		mFormat.borderColors = new int[]{};
		mFormat.fillType = MenuSlice.FILL_NONE;
		mFormat.xHint = 0;
		mFormat.yHint = 0;
		mFormat.widthHint = width;
		mFormat.heightHint = height+1; //Because we scroll one box up later...
		MenuSlice clearBox = new MenuSlice(mFormat);
		
		//Top-half
		mFormat.bgColor = colorZeroLight[0];
		mFormat.borderColors = new int[]{colorZeroLight[1], 0};
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
		mFormat.borderPadding = DEFAULT_BORDER_PADDING;
		MenuSlice topHalfBox = new MenuSlice(mFormat);
		clearBox.setTopLeftChild(topHalfBox);
		
		//Bottom-half
		mFormat.yHint = -1;
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MAXIMUM;
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		mFormat.borderPadding = DEFAULT_BORDER_PADDING;
		MenuSlice bottomHalfBox = new MenuSlice(mFormat);
		topHalfBox.connect(bottomHalfBox, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		
		//Add list of buttons...
		mFormat.borderPadding = 0;
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
		mFormat.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
		mFormat.xHint = 0;
		mFormat.yHint = 0;
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice prevBox = null;
		for (int i=0; i<mainImageFiles.length; i++) {
            int[] colors = rpg.getTextBoxColors(mainColors[i]);
            try {
        		mFormat.bgColor = colors[0];
        		mFormat.borderColors[0] = colors[1];
            	
            	ImageSlice currBox = new ImageSlice(mFormat, adaptGen.createImageAdapter(Meta.pathToGameFolder+mainImageFiles[i]));
                currBox.setData(new Int(i));
                //firstBox.setHelperTextID(mainTextsIDs[i]);
                
                //Connect!
                if (prevBox!=null)
                	prevBox.connect(currBox, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
                else
                	topHalfBox.setTopLeftChild(currBox);
                prevBox = currBox;
            } catch (Exception ex) {
                throw new LiteException(MetaMenu.class, null, "Menu button couldn't be loaded: " + ex.toString());
            }
            
            //Next
            mFormat.xHint = DEFAULT_INTER_ELEMENT_SPACING;
		}
		
		return clearBox; 
	}
}

