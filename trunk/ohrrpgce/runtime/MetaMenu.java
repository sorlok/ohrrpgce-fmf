package ohrrpgce.runtime;

import java.io.IOException;

import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.Message;
import ohrrpgce.data.RPG;
import ohrrpgce.game.LiteException;
import ohrrpgce.henceforth.Int;
import ohrrpgce.menu.Action;
import ohrrpgce.menu.FlatListSlice;
import ohrrpgce.menu.ImageSlice;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;
import ohrrpgce.menu.TextSlice;
import ohrrpgce.menu.transitions.MainMenuItemInTransition;
import ohrrpgce.menu.transitions.MenuInTransition;
import ohrrpgce.menu.transitions.Transition;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

public class MetaMenu {

	//Retrieve these AFTER calling buildMenu()
	public static MenuSlice topLeftMI;
	public static Transition menuInTrans;
	public static Transition currTransition;
	public static MenuSlice currCursor;

	//Used for laying out components
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
    private static MenuSlice[] mainMenuOverlays = new MenuSlice[mainImageFiles.length];
    private static MenuSlice[] mainMenuUpperButtons = new MenuSlice[mainImageFiles.length];
    
    
    private static MenuSlice buttonList;
    
    //Character selection
    private static FlatListSlice heroSelector;
    private static ImageSlice currHeroPicture;
    
    //Saved
    private static int width;
    private static int height;




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
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice firstBox = new MenuSlice(mFormat);

		mFormat.bgColor = 0x22CC00;
		mFormat.yHint = 0;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
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



	public static void buildMenu(int width, int height, RPG rpg, AdapterGenerator adaptGen) {
		//Save...
		MetaMenu.width = width;
		MetaMenu.height = height;
		
		//Get colors. We "lighten" a color to provide our basic menu...
        int[] colorZero = rpg.getTextBoxColors(0);
        int colorZeroLight =
            Math.min((((colorZero[0]&0xFF0000)/0x10000)*14)/10, 0xFF)*0x10000+
                    Math.min((((colorZero[0]&0xFF00)/0x100)*14)/10, 0xFF)*0x100+
                    Math.min(((colorZero[0]&0xFF)*14)/10, 0xFF)+0xFF000000;
        
        //Init our actions
        MakeHighlightAction highlightAction = new MakeHighlightAction();
      //  SaveAndRestoreMainMenu stateRestoreAction = new SaveAndRestoreMainMenu();
        
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
		topLeftMI = clearBox;

		//Top-half
		mFormat.bgColor = colorZeroLight;
		mFormat.borderColors = new int[]{colorZero[1], 0};
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

		//Buttons are in a list, so we can easily center other things on it.
		mFormat.fillType = MenuSlice.FILL_NONE;
		mFormat.xHint = 0;
		mFormat.yHint = 0;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		mFormat.widthHint = MenuFormatArgs.WIDTH_MAXIMUM;
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
		mFormat.borderPadding = 0;
		mFormat.borderColors = new int[]{};
		buttonList = new MenuSlice(mFormat);
		topHalfBox.setTopLeftChild(buttonList);
		//buttonList.setFocusLostListener(stateRestoreAction);
		//buttonList.addFocusGainedListener(stateRestoreAction);

		//Add list of buttons...
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
		mFormat.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
		mFormat.xHint = 0;
		mFormat.yHint = 0;
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		mFormat.borderColors = new int[]{0, 0};
		MenuSlice prevBox = null;
		for (int i=0; i<mainImageFiles.length; i++) {
            int[] colors = rpg.getTextBoxColors(mainColors[i]);
            try {
        		mFormat.bgColor = colors[0];
        		mFormat.borderColors[0] = colors[1];

        		//Create this button
            	ImageSlice currBox = new ImageSlice(mFormat, adaptGen.createImageAdapter(Meta.pathToGameFolder+mainImageFiles[i]));
            	currBox.setData(new Int(i));
                currBox.addFocusGainedListener(highlightAction);
                
                //Create an additional copy
                MenuFormatArgs overlayFmt = new MenuFormatArgs(mFormat);
                overlayFmt.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
                overlayFmt.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
                mainMenuUpperButtons[i] = new ImageSlice(overlayFmt, adaptGen.createImageAdapter(Meta.pathToGameFolder+mainImageFiles[i]));
                
                //Create an overlay for this button which will show when it's activated
                overlayFmt.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
                overlayFmt.widthHint = width - DEFAULT_BORDER_PADDING*2;
                overlayFmt.heightHint = height - DEFAULT_BORDER_PADDING*2;
                MenuSlice boxOverlay = new MenuSlice(overlayFmt);
                mainMenuOverlays[i] = boxOverlay;

                //Temp!
                if (mainTextsIDs[i]==QUIT) {
                	currBox.setAcceptListener(new Action() {
                		public boolean perform(Object caller) {
                			throw new RuntimeException("Testing 1 2 3");
                		}
                	});
                } else {
                	currBox.setAcceptListener(new Action() {
                		public boolean perform(Object caller) {
                			MenuSlice calledBy = (MenuSlice)caller;
                			int i = ((Int)calledBy.getData()).getValue();
                			mainMenuUpperButtons[i].getInitialFormatArgs().xHint = calledBy.getPosX();
                			mainMenuUpperButtons[i].getInitialFormatArgs().yHint = calledBy.getPosY();
                			
                			currCursor = null;
                			currTransition = new MainMenuItemInTransition(buttonList.getTopLeftChild().getPosX(), mainMenuUpperButtons[i], MetaMenu.width, MetaMenu.height, MetaMenu.topLeftMI);
                			return true;
                		}
                	});
                }
                
                //Connect!
                if (prevBox!=null) {
                	prevBox.connect(currBox, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT|MenuSlice.CFLAG_CONTROL);
                } else {
                	buttonList.setTopLeftChild(currBox);
                	buttonList.setData(currBox); //Needed for our focus listener...
                	mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
                }
                prevBox = currBox;
            } catch (Exception ex) {
                throw new LiteException(MetaMenu.class, null, "Menu button couldn't be loaded: " + ex.toString());
            }

            //Next
            mFormat.xHint = DEFAULT_INTER_ELEMENT_SPACING;
		}


		//Hero selector
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.xHint = 0;
		mFormat.yHint = DEFAULT_INTER_ELEMENT_SPACING;
		mFormat.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
		mFormat.borderPadding = Message.FONT_MARGIN;
		mFormat.bgColor = colorZeroLight;
		mFormat.borderColors = new int[]{colorZero[1], 0};
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.HCENTER;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
        String[] str = new String[rpg.getNumHeroes()];
        for (int i=0; i<str.length; i++)
            str[i] = rpg.getHero(i).name;
		heroSelector = new FlatListSlice(mFormat, str, rpg.font, true);
		heroSelector.addFocusGainedListener(highlightAction);
		buttonList.connect(heroSelector, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT|MenuSlice.CFLAG_CONTROL);
		heroSelector.setListItemChangedListener(new Action() {
			public boolean perform(Object caller) {
				Object currPic = ((Object[])currHeroPicture.getData())[heroSelector.getCurrSelectedID()];
				currHeroPicture.setImage((ImageAdapter)currPic);
				topLeftMI.doLayout(); //Not strictly necessary, since all our pictures are the same width/height.
				return true;
			}
		});
		
		//First hero's picture
        ImageAdapter pic = null;
        try {
            pic = adaptGen.createImageAdapter(Meta.pathToGameFolder + adaptGen.getGameName() + "/HERO_0.PNG");
        } catch (IOException ex) {
            throw new LiteException(MetaMenu.class, null, "IO Error making hero 0's pic: " + ex.getMessage());
        }
		
		//Current hero box
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.xHint = 0;
		mFormat.yHint = 0;
		mFormat.bgColor = colorZero[0];
		mFormat.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
		currHeroPicture = new ImageSlice(mFormat, pic);
		bottomHalfBox.setTopLeftChild(currHeroPicture);
		heroSelector.connect(currHeroPicture, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_CONTROL);
		
		//Load remainder of heroes' pictures
		currHeroPicture.setData(new Object[rpg.getNumHeroes()]);
		currHeroPicture.addFocusGainedListener(highlightAction);
        ((Object[])currHeroPicture.getData())[0] = pic;
        for (int i=1; i<rpg.getNumHeroes(); i++) {
            try {
                ((Object[])currHeroPicture.getData())[i] = adaptGen.createImageAdapter(Meta.pathToGameFolder + adaptGen.getGameName() + "/HERO_" + i +  ".PNG");
            } catch (IOException ex) {
                throw new LiteException(MetaMenu.class, null, "IO Error making hero "+i+"'s pic: " + ex.getMessage());
            }
        }

		//Transitions
		menuInTrans = new MenuInTransition(rpg, width, height);
	}
	
	
	private static class MakeHighlightAction implements Action {
    	public boolean perform(Object caller) {
    		MenuSlice calledBy = (MenuSlice)caller;
    		
    		MenuFormatArgs mf = new MenuFormatArgs();
    		mf.bgColor = 0x66FF0000;
    		mf.borderColors = new int[]{0xFF0000};
    		mf.fillType = MenuSlice.FILL_TRANSLUCENT;
    		mf.xHint = calledBy.getPosX();
    		mf.yHint = calledBy.getPosY();
    		mf.widthHint = calledBy.getWidth();
    		mf.heightHint = calledBy.getHeight();
    		currCursor = new MenuSlice(mf);
    		currCursor.doLayout();
    		return true;
    	}
    };
    
    
    /*private static class SaveAndRestoreMainMenu implements Action {
    	public boolean perform(Object caller) {
    		MenuSlice callerSlice = (MenuSlice)caller;
    		
    		if (callerSlice.getData() != null) {
    			//Restore this... be careful, this can lead to infinite loops...
    			MenuSlice prevChild = (MenuSlice)callerSlice.getData();
    			callerSlice.setData(null);
    			prevChild.moveTo();
    		} else {
    			//Save this to restore later.
    			callerSlice.setData(callerSlice.getCurrActiveChild());
    		}
    		return true;
    	}
    }
	*/
}


