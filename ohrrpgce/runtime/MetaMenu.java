package ohrrpgce.runtime;

import java.io.IOException;

import ohrrpgce.adapter.*;
import ohrrpgce.data.*;
import ohrrpgce.game.*;
import ohrrpgce.henceforth.*;
import ohrrpgce.menu.*;
import ohrrpgce.menu.transitions.*;

public class MetaMenu {

	//Retrieve these AFTER calling buildMenu()
	public static MenuSlice topLeftMI;
	public static Transition menuInTrans;
	public static Transition currTransition;
	public static MenuSlice currCursor;
	public static MakeHighlightAction highlightAction;
	public static int mode;
	private static int prevMode;

	//Used for laying out components
    public static final int DEFAULT_INTER_ELEMENT_SPACING = 3;
    private static final int DEFAULT_BORDER_PADDING = 2;

    //States, texts
    public static final int MAIN = 0;
    public static final int SPELLS = 1;
    public static final int EQUIP = 2;
    public static final int STATS = 3;
    public static final int ITEMS = 4;
    public static final int ORDER = 5;
    public static final int MAP = 6;
    public static final int SAVE = 7;
    public static final int VOLUME = 8;
    public static final int QUIT = 9;
    public static final int HERO = 10; //Label only

    //Main menu stuff
    private static final int[] mainTextsIDs = new int[] {ITEMS, ORDER, MAP, SAVE, VOLUME, QUIT};
    private static final int[] mainColors = new int[] {2, 3, 6, 5, 4, 7};
    private static final String[] mainMenuTexts = new String[] {"Items","Order","Map","Save","Volume","Quit"};
    private static final String[] mainImageFiles = new String[] {
        "main_icons/items.png",
        "main_icons/order.png",
        "main_icons/map.png",
        "main_icons/save.png",
        "main_icons/volume.png",
        "main_icons/quit.png",
    };
    private static final String imgEquip = "main_icons/equip.png";
    private static final String imgStats = "main_icons/stats.png";
    private static final String imgSpells = "main_icons/spells.png";
    
    private static MenuSlice[] mainMenuButtons = new MenuSlice[mainImageFiles.length];
    private static MenuSlice[] mainMenuUpperButtons = new MenuSlice[mainImageFiles.length];
    private static MenuSlice[] mainMenuLabels = new MenuSlice[mainImageFiles.length];
    private static MenuSlice currMenuUpperButton;
    
    private static MenuSlice buttonList;
    
    //Character selection
    private static FlatListSlice heroSelector;
    private static ImageSlice currHeroPicture;
    
    //Spells
    private static ImageSlice spellsButton;
    private static TextSlice spellsLbl;
    private static HeroSelectSlice heroUsesSpellOn;
    
    //Saved
    private static int width;
    private static int height;

    
    
    private static void doMainMenuIn(MenuSlice whichItem) {
    	int itemID = ((Int)whichItem.getData()).getValue();
		currCursor = null;
		currMenuUpperButton = mainMenuUpperButtons[itemID];
		currTransition = new MainMenuItemInTransition(whichItem, buttonList.getTopLeftChild(), currMenuUpperButton, mainMenuLabels[itemID], MetaMenu.width, MetaMenu.height, MetaMenu.topLeftMI, false);
		prevMode = mode;
		mode = mainTextsIDs[itemID];
    }
    
    public static void doMainMenuOut() {
    	int itemID = ((Int)currMenuUpperButton.getData()).getValue();
    	currCursor = null;
    	currTransition = new MainMenuItemInTransition(buttonList.getTopLeftChild(), mainMenuButtons[itemID],  currMenuUpperButton, mainMenuLabels[itemID], MetaMenu.width, MetaMenu.height, MetaMenu.topLeftMI, true);
    	mode = prevMode;
    }
    
    
    public static void resetHeroParty(RPG rpg) {
    	if (MetaMenu.heroUsesSpellOn==null)
    		return;
    	
        int hrs = Math.min(4, rpg.getNumHeroes());
        Hero[] temp = new Hero[hrs];
        for (int i=0; i<temp.length; i++)
            temp[i] = rpg.getHero(i);
        MetaMenu.heroUsesSpellOn.setHeroParty(temp, rpg, 0);
        MenuSlice curr = MetaMenu.heroUsesSpellOn.getTopLeftChild();
        while (curr!=null) {
        	curr.addFocusGainedListener(MetaMenu.highlightAction);
        	curr = curr.getConnect(MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
        }
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
        highlightAction = new MakeHighlightAction();
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
                mainMenuButtons[i] = currBox;
                
                //Create an additional copy
                MenuFormatArgs overlayFmt = new MenuFormatArgs(mFormat);
                overlayFmt.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
                overlayFmt.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
                mainMenuUpperButtons[i] = new ImageSlice(overlayFmt, adaptGen.createImageAdapter(Meta.pathToGameFolder+mainImageFiles[i]));
                mainMenuUpperButtons[i].setData(new Int(i));
                
                //Also create a label for this....
                overlayFmt.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
                overlayFmt.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
                mainMenuLabels[i] = new TextSlice(overlayFmt, mainMenuTexts[i], rpg.font, true, true, false);
                mainMenuUpperButtons[i].connect(mainMenuLabels[i], MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);

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
                			MetaMenu.doMainMenuIn((MenuSlice)caller);
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
        
        
        //Load: spells
        mFormat.xHint = DEFAULT_INTER_ELEMENT_SPACING;
        mFormat.fromAnchor = GraphicsAdapter.VCENTER | GraphicsAdapter.RIGHT;
        mFormat.toAnchor = GraphicsAdapter.VCENTER | GraphicsAdapter.LEFT;
        try {
        	spellsButton = new ImageSlice(mFormat, adaptGen.createImageAdapter(Meta.pathToGameFolder + imgSpells));
        	spellsButton.addFocusGainedListener(highlightAction);
        	spellsButton.setAcceptListener(new Action() {
        		public boolean perform(Object caller) {
        			currHeroPicture.getInitialFormatArgs().xHint = -currHeroPicture.getPosX()-currHeroPicture.getWidth()/2;
        			spellsButton.getInitialFormatArgs().fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
        			spellsButton.getInitialFormatArgs().toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
        			spellsButton.connect(spellsLbl, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
        			topLeftMI.doLayout();
        			return false;
        		}
        	});
        } catch (Exception ex) {
        	throw new LiteException(MetaMenu.class, ex, "Spells button couldn't be loaded.");
        }
        currHeroPicture.connect(spellsButton, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT|MenuSlice.CFLAG_CONTROL);
        
        //Spells label:
        mFormat.fromAnchor = GraphicsAdapter.TOP | GraphicsAdapter.RIGHT;
        mFormat.toAnchor = GraphicsAdapter.TOP | GraphicsAdapter.LEFT;
        spellsLbl = new TextSlice(mFormat, "Spells", rpg.font, true, true, false);
        
		//Transitions
		menuInTrans = new MenuInTransition(rpg, width, height);
	}
	
	
	public static class MakeHighlightAction implements Action {
    	public boolean perform(Object caller) {
    		MenuSlice calledBy = (MenuSlice)caller;
    		
    		makeHighlightAt(calledBy.getPosX(), calledBy.getPosY(), calledBy.getWidth(), calledBy.getHeight());
    		return true;
    	}
    	
    	public void makeHighlightAt(int[] rectangle) {
    		makeHighlightAt(rectangle[0], rectangle[1], rectangle[2], rectangle[3]);
    	}
    	
    	public void makeHighlightAt(int xPos, int yPos, int width, int height) {
    		MenuFormatArgs mf = new MenuFormatArgs();
    		mf.bgColor = 0x66FF0000;
    		mf.borderColors = new int[]{0xFF0000};
    		mf.fillType = MenuSlice.FILL_TRANSLUCENT;
    		mf.xHint = xPos;
    		mf.yHint = yPos;
    		mf.widthHint = width;
    		mf.heightHint = height;
    		currCursor = new MenuSlice(mf);
    		currCursor.doLayout();
    	}
    };
    
    

}


