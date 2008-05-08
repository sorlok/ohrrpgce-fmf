package ohrrpgce.runtime;

import java.io.IOException;

import ohrrpgce.adapter.*;
import ohrrpgce.data.*;
import ohrrpgce.data.loader.PictureParser;
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
	public static MenuSlice currQuickCursor;
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
    private static MenuSlice[] mainMenuOverlayChildren = new MenuSlice[mainImageFiles.length];
    private static MenuSlice currMenuUpperButton;
    
    private static MenuSlice buttonList;
    
    //Character selection
    private static FlatListSlice heroSelector;
    private static ImageSlice currHeroPicture;
    
    //Spells
    private static ImageSlice spellsButton;
    private static TextSlice spellsLbl;
    private static MenuSlice spellsLvlBigBox;
    private static MenuSlice spellsUsageBigBox;
    private static MPBarSlice currSpellMP;
    private static FlatListSlice currSpellGroup;
    private static ListSlice currSpellList;
    private static TextSlice currSpellDescription;
    private static HeroSelectSlice heroUsesSpellOn;
    
    //Quit
    private static MenuSlice cachedQuitMenu;
    private static MenuSlice quitButton;
    
    //Stats
    private static ImageSlice statsButton;
    
    //Equip
    private static ImageSlice equipButton;
    
    //Saved
    private static int width;
    private static int height;
    
    //Useful
    private static SubMenuInTransition lastSubMenuTransition;
    
    //For our tutorial
    private static SlideDownTransition mySlideTrans;

    
    
    private static void doMainMenuIn(MenuSlice whichItem) {
    	int itemID = ((Int)whichItem.getData()).getValue();
    	
    	//Special case
    	if (mainTextsIDs[itemID]==QUIT) {
    		quitButton.getInitialFormatArgs().fromAnchor=GraphicsAdapter.HCENTER|GraphicsAdapter.VCENTER;
    		quitButton.getInitialFormatArgs().toAnchor=GraphicsAdapter.HCENTER|GraphicsAdapter.VCENTER;
           	quitButton.clearFocusGainedListeners();
           	quitButton.addFocusGainedListener(highlightAction);
    	}
    	
		currCursor = null;
		currMenuUpperButton = mainMenuUpperButtons[itemID];
		currTransition = new MainMenuItemInTransition(whichItem, buttonList.getTopLeftChild(), mainMenuOverlayChildren[itemID], (itemID==0), currMenuUpperButton, mainMenuLabels[itemID], MetaMenu.width, MetaMenu.height, MetaMenu.topLeftMI, false);
		prevMode = mode;
		mode = mainTextsIDs[itemID];
    }
    
    public static void doMainMenuOut() {
    	int itemID = ((Int)currMenuUpperButton.getData()).getValue();
    	currCursor = null;
    	currTransition = new MainMenuItemInTransition(buttonList.getTopLeftChild(), mainMenuButtons[itemID], mainMenuOverlayChildren[itemID], (itemID==0), currMenuUpperButton, mainMenuLabels[itemID], MetaMenu.width, MetaMenu.height, MetaMenu.topLeftMI, true);
    	mode = prevMode;
    }
    
    private static void doSpellsMenuIn() {
    	currCursor = null;
    	prevMode = mode;
    	mode = SPELLS;

    	Action finalConnectFwd = new Action() {
    		public boolean perform(Object caller) {
    			spellsButton.connect(spellsLbl, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);		
    			currHeroPicture.connect(spellsLvlBigBox, MenuSlice.CONNECT_LEFT, MenuSlice.CFLAG_PAINT);
    			currHeroPicture.connect(spellsUsageBigBox, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
    			return true;
    		}
    	};
    	
    	Action finalConnectRev = new Action() {
    		public boolean perform(Object caller) {
    			spellsButton.disconnect(MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);		
    			currHeroPicture.disconnect(MenuSlice.CONNECT_LEFT, MenuSlice.CFLAG_PAINT);
    			currHeroPicture.disconnect(MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
    			return true;
    		}
    	};
    	lastSubMenuTransition = new SubMenuInTransition(currSpellGroup, currHeroPicture, -currHeroPicture.getWidth()/2, currHeroPicture.getPosY(), 20*3, spellsButton, -currHeroPicture.getWidth()/2+(spellsButton.getPosX()-currHeroPicture.getPosX()), currHeroPicture.getPosY(), 20, finalConnectFwd, finalConnectRev);
    	currTransition = lastSubMenuTransition;
    }
    
    public static void doSubMenuOut() {
    	currCursor = null;
    	lastSubMenuTransition.setupReverse();
    	currTransition = lastSubMenuTransition;
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

    
    
    private static MenuSlice makeSubMenu(RPG rpg, Hero heroData, String heroName, int heroBGColor, int heroBorderColor, int heroShadowColor) {
    	//Let's make a box for our "sub" menu
    	MenuFormatArgs mf = new MenuFormatArgs();
    	mf.bgColor = heroBGColor;
    	mf.borderColors = new int[]{0x333333, heroBorderColor};
    	mf.fillType = MenuSlice.FILL_SOLID;
    	mf.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
    	mf.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
    	mf.xHint = 10;
    	mf.widthHint = 100;
    	mf.heightHint = 150;
    	mf.borderPadding = 0;
    	MenuSlice subMenuSlice = new MenuSlice(mf);
    	
    	//Our label & pic will be transparent, and laid out within THIS box:
    	mf.borderColors = new int[]{heroShadowColor, heroBorderColor};
    	mf.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
    	mf.xHint = -2;
    	mf.yHint = -2;
    	mf.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
    	mf.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
    	mf.borderPadding = 3;
    	MenuSlice captionSlice = new MenuSlice(mf);
    	
    	//int[] data for our hero
    	int[] heroWalkabout = heroData.getWalkabout().spData[4];
    	int jamesWalkPalette = heroData.walkaboutPaletteID;
    	int walkaboutWidth = 20;
    	
    	//Hero's Pic
    	mf.fillType = MenuSlice.FILL_NONE;
    	mf.borderColors = new int[0];
    	mf.borderPadding = 0;
    	mf.xHint = 0;
    	mf.yHint = 0;
    	ImageSlice heroPic = new ImageSlice(mf, heroWalkabout, jamesWalkPalette, rpg, walkaboutWidth);
    	
    	//Hero's Name
    	mf.xHint = 3;
    	mf.fromAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.RIGHT;
    	mf.toAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.LEFT;
    	TextSlice heroText = new TextSlice(mf, heroName, rpg.font, true, true, false);
    	
    	//Connect them
    	subMenuSlice.setTopLeftChild(captionSlice);
    	captionSlice.setTopLeftChild(heroPic);
    	heroPic.connect(heroText, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
    	
    	return subMenuSlice;
    }
    
    /**
     * For now, this is used only to create the floating Quit menu; we'll find a 
     *   way to tuck this into the main menu later.
     */
    public static MenuSlice createQuitMenu(AdapterGenerator adaptGen, RPG rpg, int width) {
    	if (cachedQuitMenu==null) {
	    	int spacing = 10;
	    	MenuFormatArgs mFormat = new MenuFormatArgs();
	    	String mainIconsPath = Meta.pathToGameFolder + "main_icons\\";
	    	
	    	//Pull out our images
	    	ImageAdapter imageL = null;
	    	ImageAdapter imageM = null;
	    	ImageAdapter imageR = null;
	    	ImageAdapter imgC = null;
	    	try {
	    		imageL = adaptGen.createImageAdapter(mainIconsPath + "temp_large.png");
	    		imageR = adaptGen.createImageAdapter(mainIconsPath + "temp_large.png");
	    		imageM = adaptGen.createImageAdapter(mainIconsPath + "quit_large.png");
	    		imgC = adaptGen.createImageAdapter(Meta.pathToGameFolder + "hand.png");
	    	} catch (IOException ex) {
	    		throw new LiteException(MenuSlice.class, ex, "Couldn't make quit menu images");
	    	}
	    	
	    	//Make our cursor
	    	mFormat.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
	    	mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
	    	mFormat.borderColors = new int[]{};
	    	mFormat.fillType = MenuSlice.FILL_NONE;
	    	MetaMenu.currQuickCursor = new ImageSlice(mFormat, imgC);
	    	MetaMenu.currQuickCursor.doLayout();
	    	
	    	//Button 2
	    	mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
	    	mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
	    	quitButton = new ImageSlice(mFormat, imageM);
	    	quitButton.setAcceptListener(new CloseAction(adaptGen));
	    	
	    	//Button 1
	    	mFormat.xHint = -spacing;
	    	mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
	    	mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
	    	MenuSlice buttonL = new ImageSlice(mFormat, imageL);
	    	quitButton.connect(buttonL, MenuSlice.CONNECT_LEFT, MenuSlice.CFLAG_PAINT);
	    	
	    	//Button 3
	    	mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
	    	mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
	    	MenuSlice buttonR = new ImageSlice(mFormat, imageR);
	    	quitButton.connect(buttonR, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
	    	
	    	//Button 2's label
	    	mFormat.xHint = 2;
	    	mFormat.yHint = -1 - Message.FONT_SIZE/2;
	    	mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.HCENTER;
	    	mFormat.toAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.HCENTER;
	    	TextSlice labelM = new TextSlice(mFormat, "QUIT", rpg.font, true, false, false);
	    	labelM.forceTextColor(0);
	    	quitButton.connect(labelM, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
	    	
	    	//Top-most box:
	    	int[] quitColor = rpg.getTextBoxColors(mainColors[5]);
	    	mFormat.xHint = 0;
	    	mFormat.yHint = 0;
	    	mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
	    	mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
	    	mFormat.fillType = MenuSlice.FILL_SOLID;
	    	mFormat.bgColor = quitColor[0];
	    	mFormat.borderColors = new int[]{quitColor[1], 0};    	
	    	mFormat.borderPadding = spacing;
	    	//Note: we cheat on borders because WIDTH_MINIMUM on horizontally-centered sub-components doesn't work right & isn't an easy fix.
	    	mFormat.widthHint = imageL.getWidth()+imageM.getWidth()+imageR.getWidth()+spacing*4+mFormat.borderColors.length*2;
	    	cachedQuitMenu = new MenuSlice(mFormat);
    	}
    	
    	//Reset child if necessary...
    	cachedQuitMenu.setTopLeftChild(quitButton);
		quitButton.getInitialFormatArgs().fromAnchor=GraphicsAdapter.HCENTER|GraphicsAdapter.TOP;
		quitButton.getInitialFormatArgs().toAnchor=GraphicsAdapter.HCENTER|GraphicsAdapter.TOP;
    	quitButton.clearFocusGainedListeners();
    	quitButton.addFocusGainedListener(new Action() {
    		public boolean perform(Object caller) {
    			MenuSlice thisBox = (MenuSlice)caller;
    			MetaMenu.currQuickCursor.forceToLocation(thisBox.getPosX() - MetaMenu.currQuickCursor.getWidth() + thisBox.getWidth()/3, thisBox.getPosY() - MetaMenu.currQuickCursor.getHeight() + thisBox.getHeight()/3);
    			return false;
    		}
    	});
    	
    	//Force the layout
    	cachedQuitMenu.doLayout();
		int widthOffset = width/2 - cachedQuitMenu.getWidth()/2;
		cachedQuitMenu.getInitialFormatArgs().xHint = widthOffset;
		cachedQuitMenu.getInitialFormatArgs().yHint = widthOffset;
		cachedQuitMenu.doLayout();
		
		//Might as well do this now
		quitButton.moveTo();
    	
		//All done; return it.
    	return cachedQuitMenu;
    }
    
    
    public static void buildMenuTUTORIAL(int width, int height, RPG rpg, AdapterGenerator adaptGen) {
    	//Large overlay
    	MenuFormatArgs mfClear = new MenuFormatArgs();
    	mfClear.bgColor = 0x333333;
    	mfClear.fillType = MenuSlice.FILL_SOLID;
    	mfClear.borderColors = new int[]{};
    	mfClear.widthHint = width;
    	mfClear.heightHint = height;
    	mfClear.borderPadding = 10;
    	MenuSlice largeClearBox = new MenuSlice(mfClear);
    	
    	//Intialize our cursor
    	String workingDir = Meta.pathToGameFolder;
    	MenuFormatArgs mfCursor = new MenuFormatArgs();
    	mfCursor.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
    	mfCursor.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
    	mfCursor.borderColors = new int[]{};
    	mfCursor.fillType = MenuSlice.FILL_NONE;
    	ImageAdapter cursorImg = null;
    	try {
    		cursorImg = adaptGen.createImageAdapter(workingDir+"hand.png");
    	} catch (IOException ex) {
    		cursorImg = adaptGen.createBlankImage(10, 10);
    	}
    	MetaMenu.currCursor =  new ImageSlice(mfCursor, cursorImg);
    	MetaMenu.currCursor.doLayout();
    	
    	//Image for our cube
    	ImageAdapter box1Pic = null;
    	try {
    		box1Pic = adaptGen.createImageAdapter(workingDir + "box.png");
    	} catch (IOException ex) {
    		box1Pic = adaptGen.createBlankImage(10, 10);
    	}
    	
    	//Our new slice initializes like any other
    	MenuFormatArgs mf = new MenuFormatArgs();
    	mf.fillType = MenuSlice.FILL_NONE;
    	mf.borderColors = new int[]{};
    	mf.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
    	mf.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
    	CubeSlice blueGraphic = new CubeSlice(mf, box1Pic);
    	
    	// Our cube will share the same highlight and focus listeners
		Action makeHighlightAction = new Action() {
			// makeHighlight() was covered earlier and hasn't changed.
			private void makeHighlight(MenuSlice calledBy) {
				// Put the cursor somewhere near the middle of this slice's
				// sub-component
				int[] rectangle = calledBy.getActiveRectangle();
				MetaMenu.currCursor.forceToLocation(rectangle[0] + rectangle[2]
						/ 2 - MetaMenu.currCursor.getWidth(), rectangle[1]
						+ rectangle[3] / 2 - MetaMenu.currCursor.getHeight()
						/ 2);
			}

			private void startSlideIn(CubeSlice calledBy) {
				// We only transition if we're moving to a NEW menu slice... we
				// need to fail silently on the "focus gained" call.
				MenuSlice oldMenu = calledBy.getConnect(
						MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
				MenuSlice newMenu = null;
				MenuSlice[] savedMenus = (MenuSlice[]) calledBy.getData();
				if (calledBy.getCubeSide() == 'L')
					newMenu = savedMenus[0];
				else if (calledBy.getCubeSide() == 'R')
					newMenu = savedMenus[1];
				else if (calledBy.getCubeSide() == 'T')
					newMenu = savedMenus[2];
				if (newMenu.equals(oldMenu))
					return;

				// A bit wordy, but now that that's over, let's fire up our
				// transition
				if (mySlideTrans == null)
					mySlideTrans = new SlideDownTransition(calledBy);
				mySlideTrans.setTargets(newMenu, oldMenu);
				mySlideTrans.reset();
				MetaMenu.currTransition = mySlideTrans;
			}

			public boolean perform(Object caller) {
				CubeSlice calledBy = (CubeSlice) caller;
				makeHighlight(calledBy);
				startSlideIn(calledBy);
				
			    return true; //Return value doesn't matter.
			}
		};
		blueGraphic.setSubSelectionChangedListener(makeHighlightAction);
		blueGraphic.addFocusGainedListener(makeHighlightAction);

    	
    	// Add our data objects
    	MenuSlice bobSlice = makeSubMenu(rpg, rpg.getHero(0), "Bob", 0xA8E61D, 0x22B14C, 0x000000);
    	MenuSlice jamesSlice = makeSubMenu(rpg, rpg.getHero(1), "James", 0xFFC20E, 0xFF7E00, 0xBD8A00);
    	MenuSlice dustySlice = makeSubMenu(rpg, rpg.getHero(3), "Dusty", 0xB5A5D5, 0x6F3198, 0x000000);
    	blueGraphic.setData( new MenuSlice[]{jamesSlice, dustySlice, bobSlice} );

    	//Connect & set Children
    	largeClearBox.setTopLeftChild(blueGraphic);    	
    	blueGraphic.connect(jamesSlice, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
    	
    	//Some bookkeeping
    	MetaMenu.topLeftMI = largeClearBox;
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

                //Summon sub menu!
               	currBox.setAcceptListener(new Action() {
               		public boolean perform(Object caller) {
               			MetaMenu.doMainMenuIn((MenuSlice)caller);
               			return true;
               		}
               	});
               	
               	if (mainTextsIDs[i]==QUIT) {
               		//Make our quit menu
               		createQuitMenu(adaptGen, rpg, width);
               		
               		//Create this menu detached.
               		mainMenuOverlayChildren[i] = MetaMenu.quitButton;
               	} else {
    				mFormat.fromAnchor = GraphicsAdapter.HCENTER|GraphicsAdapter.VCENTER;
    				mFormat.toAnchor = GraphicsAdapter.HCENTER|GraphicsAdapter.VCENTER;
               		mainMenuOverlayChildren[i] = new TextSlice(mFormat, "(Incomplete)", rpg.font, true, true, false);
               		mainMenuOverlayChildren[i].addFocusGainedListener(highlightAction);
               	}
               	
                
                //Connect!
                if (prevBox!=null) {
                	prevBox.connect(currBox, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT|MenuSlice.CFLAG_CONTROL);
                } else {
                	buttonList.setTopLeftChild(currBox);
                	buttonList.setData(currBox); //Needed for our focus listener...
                }
            	mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
            	mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
            	
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
        
        
        //Placeholders
        mFormat.yHint = DEFAULT_INTER_ELEMENT_SPACING;
        mFormat.fromAnchor = GraphicsAdapter.HCENTER | GraphicsAdapter.BOTTOM;
        mFormat.toAnchor = GraphicsAdapter.HCENTER | GraphicsAdapter.TOP;
        try {
        	statsButton = new ImageSlice(mFormat, adaptGen.createImageAdapter(Meta.pathToGameFolder + imgStats));
        	statsButton.addFocusGainedListener(highlightAction);
        	
            mFormat.xHint = -DEFAULT_INTER_ELEMENT_SPACING;
            mFormat.yHint = 0;
            mFormat.fromAnchor = GraphicsAdapter.VCENTER | GraphicsAdapter.LEFT;
            mFormat.toAnchor = GraphicsAdapter.VCENTER | GraphicsAdapter.RIGHT;
        	equipButton = new ImageSlice(mFormat, adaptGen.createImageAdapter(Meta.pathToGameFolder + imgEquip));
        	equipButton.addFocusGainedListener(highlightAction);
        } catch (Exception ex) {
        	throw new LiteException(MetaMenu.class, ex, "Stats/Equip button(s) couldn't be loaded.");
        }
        currHeroPicture.connect(statsButton, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT|MenuSlice.CFLAG_CONTROL);
        currHeroPicture.connect(equipButton, MenuSlice.CONNECT_LEFT, MenuSlice.CFLAG_PAINT|MenuSlice.CFLAG_CONTROL);
        
        
        //Load: spells
        mFormat.xHint = DEFAULT_INTER_ELEMENT_SPACING;
        mFormat.yHint = 0;
        mFormat.fromAnchor = GraphicsAdapter.VCENTER | GraphicsAdapter.RIGHT;
        mFormat.toAnchor = GraphicsAdapter.VCENTER | GraphicsAdapter.LEFT;
        try {
        	spellsButton = new ImageSlice(mFormat, adaptGen.createImageAdapter(Meta.pathToGameFolder + imgSpells));
        	spellsButton.addFocusGainedListener(highlightAction);
        	spellsButton.setAcceptListener(new Action() {
        		public boolean perform(Object caller) {
        			MetaMenu.doSpellsMenuIn();
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
        
        //Next stuff requires a box
        mFormat.fillType = MenuSlice.FILL_NONE;
        mFormat.borderColors = new int[]{};
        mFormat.borderPadding = 0;
        mFormat.widthHint = MenuFormatArgs.WIDTH_MAXIMUM;
        mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
        mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.RIGHT;
        mFormat.toAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
        mFormat.xHint = 0;
        spellsLvlBigBox = new MenuSlice(mFormat);
        
        //MP bar
        mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
        mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
        mFormat.widthHint = MenuFormatArgs.WIDTH_MINIMUM;        
        currSpellMP = new MPBarSlice(mFormat, rpg, 0);
        currSpellMP.setToRandom();
        spellsLvlBigBox.setTopLeftChild(currSpellMP);
        
        //Spell group
		mFormat.bgColor = colorZeroLight;
		mFormat.borderColors = new int[]{colorZero[1], 0};
		mFormat.fillType = MenuSlice.FILL_SOLID;
        mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.HCENTER;
        mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
        mFormat.yHint = DEFAULT_INTER_ELEMENT_SPACING;
        currSpellGroup = new FlatListSlice(mFormat, new String[]{"T"}, rpg.font, true);
        currSpellGroup.addFocusGainedListener(highlightAction);
        currSpellMP.connect(currSpellGroup, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
        
        //The bottom components also require a box...
        mFormat.fillType = MenuSlice.FILL_NONE;
        mFormat.borderColors = new int[]{};
        mFormat.borderPadding = 0;
        mFormat.bgColor = 0xFF0000;
        mFormat.widthHint = width-2*(bottomHalfBox.getInitialFormatArgs().borderPadding+bottomHalfBox.getInitialFormatArgs().borderColors.length)+1;
        mFormat.heightHint = MenuFormatArgs.HEIGHT_MAXIMUM;
        mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.HCENTER;
        mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
        mFormat.xHint = bottomHalfBox.getInitialFormatArgs().borderPadding+bottomHalfBox.getInitialFormatArgs().borderColors.length-1;
        mFormat.yHint = DEFAULT_INTER_ELEMENT_SPACING;
        spellsUsageBigBox = new MenuSlice(mFormat);
        
        //Hero selector
		mFormat.bgColor = colorZeroLight;
		mFormat.borderColors = new int[]{colorZero[1], 0};
		mFormat.fillType = MenuSlice.FILL_SOLID;
        mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.RIGHT;
        mFormat.toAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.RIGHT;
        mFormat.xHint = 0;
        mFormat.yHint = 0;
        mFormat.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
        mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
        int hrs = Math.min(4, rpg.getNumHeroes());
        Hero[] temp = new Hero[hrs];
        for (int i=0; i<temp.length; i++)
            temp[i] = rpg.getHero(i);
        HeroSelectSlice heroSl = new HeroSelectSlice(mFormat, rpg, 4, 4);
        heroSl.setHeroParty(temp, rpg, 1);
        spellsUsageBigBox.setTopLeftChild(heroSl);
        
        //Spell list
        mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
        mFormat.toAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.RIGHT;
        mFormat.xHint = 1;
        mFormat.yHint = 0;
        mFormat.widthHint = MenuFormatArgs.WIDTH_MAXIMUM;
        mFormat.heightHint = MenuFormatArgs.HEIGHT_MAXIMUM;
        currSpellList = new ListSlice(mFormat, rpg.font);
        heroSl.connect(currSpellList, MenuSlice.CONNECT_LEFT, MenuSlice.CFLAG_PAINT);
        
        //Description
        mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
        mFormat.toAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
        mFormat.xHint = 0;
        mFormat.yHint = 0;
        currSpellDescription = new TextSlice(mFormat, "t", rpg.font, true, true, false);
        heroSl.connect(currSpellDescription, MenuSlice.CONNECT_TOP, MenuSlice.CFLAG_PAINT);
        
		//Transitions
		menuInTrans = new MenuInTransition(rpg, width, height);
	}

	
	public static class CloseAction implements Action {
		private AdapterGenerator exitHook;
		public CloseAction(AdapterGenerator exitHook) {
			this.exitHook = exitHook;
		}
		public boolean perform(Object caller) {
			exitHook.exitGame(true);
			return false;
		}
	}
	
	
	public static class MakeHighlightAction implements Action {
    	public boolean perform(Object caller) {
    		MenuSlice calledBy = (MenuSlice)caller;
    		
    		makeHighlightAt(calledBy.getActiveRectangle());
    		return true;
    	}
    	
    	private void makeHighlightAt(int[] rectangle) {
    		makeHighlightAt(rectangle[0], rectangle[1], rectangle[2], rectangle[3]);
    	}
    	
    	private void makeHighlightAt(int xPos, int yPos, int width, int height) {
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


