/*
 * MenuEngine.java
 * Created on April 10, 2007, 8:09 PM
 */

package ohrrpgce.game;


import java.io.IOException;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.adapter.InputAdapter;

import ohrrpgce.data.Hero;
import ohrrpgce.data.Spell;
import ohrrpgce.data.RPG;
import ohrrpgce.menu.Action;
import ohrrpgce.menu.Button;
import ohrrpgce.menu.Canvas;
import ohrrpgce.menu.Composite;
import ohrrpgce.menu.FlatList;
import ohrrpgce.menu.HeroSelector;
import ohrrpgce.menu.Label;
import ohrrpgce.menu.List;
import ohrrpgce.menu.MPBox;
import ohrrpgce.menu.MenuItem;
import ohrrpgce.menu.SpecialLabel;
import ohrrpgce.menu.TextBox;
import ohrrpgce.menu.transitions.Transition;
import ohrrpgce.runtime.Meta;
import ohrrpgce.runtime.OHRRPG;


/**
 * The OHRRPGCEFMF's menu.
 * NOTE: There is an alternative way to handle painting, one that is probably better and
 *  def. faster. Build a tree of components to paint with "repaint", and use this to 
 *  handle painting. This saves a whole bunch of dead-end recursions, _especially_
 *  with Composites. Whenever connect() or disconnect() is called, this tree can
 *  be re-computed. 
 * @author Seth N. Hetu 
 */
/*public class Old_MenuEngine extends Engine {  
    //Debug
    public static boolean DEBUG_CONTROL = false;
    
    //Constants
    private static final int MARGIN = 4;
    private static final int MULTIPRESS_DELAY = 12;
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
    private static final int[] mainColors = new int[] {
        2,
        3,
        6,
        5,
        4,
        7
    };
    private static final String[] mainTexts = new String[] {
        "Items",
        "Order",
        "Map",
        "Save",
        "Volume",
        "Quit"
    };
    private static final String[] helperTexts = new String[] {
        "",
        "Spells",
        "Equipment",
        "Statistics",
        "Items",
        "Order",
        "Map",
        "Save",
        "Volume",
        "Quit",
        "Hero"
    };
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
    
    
    //Meta-type info
    private EngineSwitcher midletHook;
    private int width;
    private int height;
    private int itemReturnPos;
    
    private boolean dirtyHighlight;
    private boolean bufferedESC;

    //Locus of control
    private MenuItem topLeftMI;
    private MenuItem currMI;
    private Transition currTransition;
    private MenuItem nextMI;
    private boolean fadingIn;
    
    
    //We'll actually implement highlight shapes using our own event handlers.
    // That way, we test events, and at the same time do not limit others
    // from making their own cursor (a hand, etc.)
    private Canvas currCursor;
    
    //Backgrounds
    private Canvas topBkgrd;
    private Canvas btmBkgrd;
    private Canvas boxOverlay;
    
    private Canvas blackOverlay;
    private Canvas colorOverlay;
   
    private int colorOverlayRGB;
    private static final int colorInIntervals = 8; //Ticks
    private static final int darkenInterval = 4; //Ticks
    private static final int defaultSpeed = 20; //pix/tick
   
    
    //TONS of menu controls
    private Composite mainMenu;
    private Button[] mainMenuButtons = new Button[mainImageFiles.length];
    private FlatList heroName;
    private Button heroPicture;
    private Button heroEquip;
    private Button heroSpells;
    private Button heroStats;
    private FlatList helperTxt;
    //Spells:
    private Label lblSpells;
    private MPBox currSpellMP;
    private FlatList currSpellGroup;
    private HeroSelector heroUsesSpellOn;
    private List spellList;
    private Label spellDesc;
    //All "top-level" controls
    private Button currItemBtn;
    private SpecialLabel currItemTxt;
    //"Volume"
    private Label tempVol;
    
    //Multiple uses
    private Action makeHighlightListener;
    private Action nullMoveListener;
    private Action mainMenuListener;
    private Action cancelMainMenuListener;
    private Action cancelSpellsMenuListen;
    private Action exitMenu;
    
    //Menu control
    private boolean initDoneOnce;
    private int delayTimer;
    private int mode;
    private int lastMode;
    private int lastHeroID;
            
    
    private AdapterGenerator adaptGen;
    
    
    public Old_MenuEngine(AdapterGenerator adaptGen, EngineSwitcher midletHook) {
        this.width = adaptGen.getScreenWidth();
        this.height = adaptGen.getScreenHeight();
        this.midletHook = midletHook;
        this.adaptGen = adaptGen;
    }
    
    private void makeHighlight(MenuItem itemToHighlight) {
        makeHighlightAt(new int[]{
            itemToHighlight.getLastPaintedOffsetX()+itemToHighlight.getPosX(), 
            itemToHighlight.getLastPaintedOffsetY()+itemToHighlight.getPosY(),
            itemToHighlight.getWidth(),
            itemToHighlight.getHeight()
        });
        
        //Update flatlist
        helperTxt.selectElement(itemToHighlight.getHelperTextID());
    }
    
    //coOrds = [x, y, w, h]
    private void makeHighlightAt(int[] coOrds) {
        currCursor = new Canvas(coOrds[2], coOrds[3],
                0x66FF0000,
                new int[]{0xFFFF0000}, Canvas.FILL_TRANSLUCENT
                );
        currCursor.setPosition(coOrds[0], coOrds[1]);
    }
    
    ///
    /// Pass commands down to the current menu item.
    ///
    public void handleKeys(int keyStates) {
        //Dont' allow the user to mess up any animations.
        if (currTransition!=null)
            return;
        
        //The key autofires if pressed for a certain length of time.
        if (!bufferedESC && (keyStates==0)) {
            delayTimer = 0;
        } else {
            if (delayTimer<=0) {
                //OHR key-detect order: MENU, ENTER, UP, DOWN, LEFT, RIGHT
                if (bufferedESC || (keyStates&InputAdapter.KEY_CANCEL)!=0) {
                    System.out.println("CANCEL");
                    bufferedESC = false;
                    if (mode == MAIN)
                        exitMenu.perform(null); //Top-level exit first.
                    else if (mode == SPELLS)
                        cancelSpellsMenuListen.perform(null);
                    else if (mode==ITEMS || mode==ORDER || mode==MAP || mode==SAVE || mode==VOLUME || mode==QUIT)
                        cancelMainMenuListener.perform(null);
                    else
                    currMI.cancel();
                } else if ((keyStates&InputAdapter.KEY_ACCEPT)!=0) {
                    System.out.println("ACCEPT");
                    currMI.accept();
                } else if ((keyStates&InputAdapter.KEY_UP)!=0) {
                    System.out.println("UP");
                    currMI = currMI.processInput(MenuItem.CONNECT_TOP);
                } else if ((keyStates&InputAdapter.KEY_DOWN)!=0) {
                    System.out.println("DOWN");
                    currMI = currMI.processInput(MenuItem.CONNECT_BOTTOM);
                } else if ((keyStates&InputAdapter.KEY_LEFT)!=0) {
                    System.out.println("LEFT");
                    currMI = currMI.processInput(MenuItem.CONNECT_LEFT);
                } else if ((keyStates&InputAdapter.KEY_RIGHT)!=0) {
                    System.out.println("RIGHT");
                    currMI = currMI.processInput(MenuItem.CONNECT_RIGHT);
                } else 
                    throw new RuntimeException("Somehow, no input was pressed (but expected) for input: " + keyStates);
                
                //Allow the user to easily override default movement.
                if (nextMI != null) {
                    currMI = nextMI;
                    nextMI = null;
                }
                
                if (delayTimer==0)
                    delayTimer = MULTIPRESS_DELAY;
            } else {
                delayTimer--;
                if (delayTimer==0)
                    delayTimer = -1; //Allow auto-fire.
            }
        }
    }
    

    public void paintScene() {
        //Super-easy fade-in
        if (fadingIn) {
            MenuInTransition trans = (MenuInTransition)currTransition;
            int[] clr0 = getRPG().getTextBoxColors(0);
            int boxBounds = Math.max(width, height);
            int xStart = (width-boxBounds)/2;
            int yStart = (height-boxBounds)/2;
            
            //Draw the faded background
            if (colorOverlay != null) {
                colorOverlay.paint();
            }
            
            //Draw the arc
            GraphicsAdapter.setColor(clr0[0]);
            GraphicsAdapter.fillArc(xStart, yStart, boxBounds, boxBounds, 0, -trans.getAngle());
                    
            return;
        }
        
        
        //Paint the background box(es)
      //  System.out.println("Painting background");
        topBkgrd.paint();
        btmBkgrd.paint();
        
        //Paint all menu components
        topLeftMI.repaint(new int[]{0, 0});
        topLeftMI.clearPaintFlag();
        
        //Minor hack
       // System.out.println("hacking...");
        GraphicsAdapter.setColor(getRPG().getTextBoxColors(0)[1]);
        GraphicsAdapter.drawLine(0, 0, 0, height);
        GraphicsAdapter.drawLine(width, 0, width, height);
        GraphicsAdapter.setColor(0);
        GraphicsAdapter.drawLine(1, 0, 1, height);
        GraphicsAdapter.drawLine(width-1, 0, width-1, height);
        
        //Are we accessing a top-level menu item?
        if (currItemBtn!=null) {
            //Paint the middle-ground
            blackOverlay.paint();
            if (boxOverlay!=null) {
            	GraphicsAdapter.setClip(boxOverlay.getPosX(), mainMenu.getPosY()+mainMenu.getHeight(), boxOverlay.getWidth(), boxOverlay.getHeight());
                boxOverlay.paint();
                GraphicsAdapter.setClip(0, 0, width, height);
            }
            
            //Paint all top-level components
            currItemBtn.repaint(new int[]{0, 0});
            currItemBtn.clearPaintFlag();
        } 
        
        //Highlight it!
        if (dirtyHighlight) {
            if (currMI==spellList)
                makeHighlightAt(spellList.getCurrItemRectangle());
            else
                makeHighlight(currMI.getActiveSubItem());
            dirtyHighlight = false;
        }
        if (currCursor!=null)
            currCursor.paint();
        
        //For clarity
        if (DEBUG_CONTROL) {
        	GraphicsAdapter.setColor(0x000000);
        	GraphicsAdapter.fillRect(0, height-GraphicsAdapter.getFont().getFontHeight()-4, width, GraphicsAdapter.getFont().getFontHeight()+4);
            String s = currMI.getClass().getName();
            int w = GraphicsAdapter.getFont().stringWidth(s);
            GraphicsAdapter.setColor(0x00FF00);
            GraphicsAdapter.drawString(s, width/2-w/2, height-GraphicsAdapter.getFont().getFontHeight()-2, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
        }
        

    }

    public void updateScene(long elapsed) {
        if (currTransition!=null) {
            //UPDATE THE CURRENT TRANSITION.
            if (currTransition.step()) {
                currTransition = null; //We're done.
                fadingIn = false;
            }
        }
    }

    public void communicate(Object stackFrame) {
        throw new RuntimeException("MENU shouldn't be calling any other Engines.");
    }

    public void reset() {
        System.out.println("MENU was called, and asked to RESET.");
        if (!initDoneOnce) {
        	try {
        		initMenu();
        		initDoneOnce = true;
        	} catch (Exception ex) {
        		throw new LiteException(this, ex, "Menu failed on INIT");
        	}
        }
        
        //In case the party's changed
        spellList.reset();
        int hrs = Math.min(4, getRPG().getNumHeroes());
        Hero[] temp = new Hero[hrs];
        for (int i=0; i<temp.length; i++)
            temp[i] = getRPG().getHero(i);
        heroUsesSpellOn.setHeroParty(temp, 0);
        for (int i=0; i<heroUsesSpellOn.getNumSubItems(); i++) {
            heroUsesSpellOn.getSubItem(i).setFocusGainedListener(makeHighlightListener);
        }
        
        
        //Start at the top.
        mainMenu.reset();
        currMI = mainMenu;
        currMI.moveTo();
        mode = MAIN;
        //Minor flaw: The actual last_painted location of currMI hasn't been 
        // set yet (it's never been painted). So, we hack it in here.)
        currCursor.setPosition(currCursor.getPosX()+mainMenu.getPosX(), mainMenu.getPosY()) ;
        
        //Just to be safe
        lastHeroID = -1;
        delayTimer = 1;
        
        //Finally...
        currTransition = new MenuInTransition();
        fadingIn = true;
    }
    
    public RPG getRPG() {
        return ((GameEngine)midletHook.getCaller()).getRPG().getBaseRPG();
    }
    
    public OHRRPG getOHRRPG() {
        return ((GameEngine)midletHook.getCaller()).getRPG();
    }
    
    
    private void reloadSpellData() {
        //First, set the current MP box correctly.
        if (lastHeroID != heroName.getCurrItemID()) {
            lastHeroID = heroName.getCurrItemID();
            currSpellGroup.reloadItemSet(getRPG().getHero(heroName.getCurrItemID()).getSpellGroupNames());
        }
        int currSpellType = getRPG().getHero(heroName.getCurrItemID()).spellGroupTypes[currSpellGroup.getCurrItemID()];
        if (currSpellType == Hero.SPELL_MP_BASED)
            currSpellMP.setMP(42, 123, false, currSpellGroup.getCurrItemID());
        else if (currSpellType == Hero.SPELL_FF_STYLE)
            currSpellMP.setMP(8, 10, true, currSpellGroup.getCurrItemID());
        else if (currSpellType == Hero.SPELL_RANDOM)
            currSpellMP.setToRandom();
        else
            throw new RuntimeException("Not implemented: \"ITEM\" spells");
        
        int[] spellIDs = getRPG().getHero(heroName.getCurrItemID()).spells[currSpellGroup.getCurrItemID()];
        Spell[] spells = new Spell[spellIDs.length];
        for (int i=0; i<spells.length; i++)
            spells[i] = getRPG().getAttack(spellIDs[i]);
        
        spellDesc.setData(new TextBox[spells.length]);
        
        String[] names = new String[spells.length];
        int[] mps = new int[spells.length];
        boolean[] canUses = new boolean[spells.length];
        
        for (int i=0; i<spells.length; i++) {
            names[i] = spells[i].attackName;
            mps[i] = spells[i].mpCost;
            canUses[i] = spells[i].useableOutsideBattle;
        }

        currSpellGroup.setPosition(
                currSpellMP.getWidth()/2 - currSpellGroup.getWidth()/2,
                MARGIN);
        spellList.setPosition(
                -(currSpellGroup.getPosX()+currSpellMP.getPosX()+heroSpells.getPosX()+heroPicture.getWidth()/2)+MARGIN,
              //  currSpellGroup.getWidth()/2 - width/2,
                MARGIN);
        
        heroUsesSpellOn.setIDOfUser(heroName.getCurrItemID());
                
        spellList.setData(spells);
        spellList.setItems(names, mps, canUses, (getRPG().getHero(heroName.getCurrItemID()).spellGroupTypes[currSpellGroup.getCurrItemID()] == Hero.SPELL_FF_STYLE));
    }
    
    
    //HORRENDOUS setup routine:
    public void initMenu() {
        //Init our actions
        makeHighlightListener = new Action() {
          public boolean perform(Object caller)   {
              System.out.println("A MI has gained focus: " + caller.getClass().getName().toString());
              MenuItem mi = null;
              try {
                  mi = (MenuItem)caller;
              } catch (ClassCastException ex) {
                  throw new RuntimeException("makeHighlight() called on a non-MenuItem! " + ex.toString());
              }
              makeHighlight(mi.getActiveSubItem());
              
              //Doesn't matter
              return true;
          }
        };
        nullMoveListener = new Action() {
            public boolean perform(Object caller) {
                return false;
            }
        };
        mainMenuListener = new Action() {
          public boolean perform(Object caller) {
                //Create the relevant top-level controls
                int currItemID = ((Integer)mainMenu.getActiveSubItem().getData()).intValue();
                int[] clr = getRPG().getTextBoxColors(mainColors[currItemID]);
                boxOverlay = new Canvas(
                        width-MARGIN*2, height-(mainMenu.getPosY()+mainMenu.getHeight()+MARGIN),
                        clr[0], new int[]{clr[1], 0}, Canvas.FILL_SOLID
                );
                boxOverlay.setPosition(MARGIN, -boxOverlay.getHeight()+mainMenu.getPosY()+mainMenu.getHeight()+2);
                
                //Re-create components from scratch.
                currItemBtn = new Button((((Button)mainMenu.getActiveSubItem()).getImage()), null,  0xFF000000|clr[0], new int[]{0xFF000000|clr[1], 0xFF000000});
                currItemTxt = new SpecialLabel(
                        new TextBox(mainTexts[currItemID], getRPG().font, clr[1], clr[0], true, Canvas.FILL_SOLID),
                        new int[]{0, 0, width, height}
                        );
                itemReturnPos = mainMenu.getActiveSubItem().getLastPaintedOffsetX()+mainMenu.getActiveSubItem().getPosX();
                currItemBtn.setPosition(itemReturnPos, MARGIN);
                currItemTxt.setPosition(-currItemTxt.getWidth()+2, (currItemBtn.getHeight()-currItemTxt.getHeight())/2);
                currItemTxt.setClip(new int[]{MARGIN+currItemBtn.getWidth()-1, currItemBtn.getPosY()+currItemTxt.getPosY(), currItemTxt.getWidth(), currItemTxt.getHeight()});
                //currItemBtn.connect(currItemTxt, MenuItem.CONNECT_RIGHT);
                
                //Choice-specific data...
                lastMode = mode;
                MenuItem connectLater = null;
                switch(currItemID) {
                    case 0:
                        //Items
                        connectLater = tempVol;
                        
                        mode = ITEMS;
                        break;
                    case 1:
                        //Order
                        connectLater = tempVol;
                        
                        mode = ORDER;
                        break;
                    case 2:
                        //Save
                        connectLater = tempVol;
                        
                        mode = SAVE;
                        break;
                    case 3:
                        //Map
                        connectLater = tempVol;
                        
                        mode = MAP;
                        break;
                    case 4:
                        //Volume
                        connectLater = tempVol;
                        
                        mode = VOLUME;
                        break;
                    case 5:
                        //Quit
                        connectLater = tempVol;
                        
                        //DEBUG:
                        if (true)
                        	throw new LiteException(this, null, "Testing free memory...");
                        
                        mode = QUIT;
                        break;
                }
                
                currCursor = null;
                
                currTransition = new ButtonMoveTransition(connectLater);
                
                return true;
          }  
        };
        cancelMainMenuListener = new Action() {
            public boolean perform(Object caller) {
                //Kill top-level control
                if (currItemBtn.getConnect(MenuItem.CONNECT_BOTTOM)!=null) {
                    currItemBtn.getConnect(MenuItem.CONNECT_BOTTOM).clearPaintFlag();
                    currItemBtn.disconnect(MenuItem.CONNECT_BOTTOM);
                }
                currCursor = null;
        
                currTransition = new ButtonRevertTransition();
                
                return true;
            }
        };
        cancelSpellsMenuListen = new Action() {
            public boolean perform(Object caller) {
                heroSpells.clearPaintFlag();
                heroSpells.disconnect(MenuItem.CONNECT_BOTTOM);
                
                heroPicture.connect(heroEquip, MenuItem.CONNECT_LEFT);
                heroPicture.connect(heroStats, MenuItem.CONNECT_BOTTOM);
                
                heroPicture.setFocusGainedListener(null);

                currCursor = null;
                currTransition =  new SpellShiftBackTransition();

                mode = MAIN;
                
                return true;
            }
        };
        exitMenu = new Action() {
            public boolean perform(Object caller) {
                midletHook.egress(new Integer(42));
                return true;
            }
        };

        
        /////////////////////////////////////////////////
        // Section 1, Main Menu
        /////////////////////////////////////////////////
        //Slightly more top-level
        helperTxt = new FlatList(this, helperTexts, 0, MARGIN, false);
        //Main Menu of buttons
        for (int i=0; i<mainMenuButtons.length; i++) {
            int[] colors = getRPG().getTextBoxColors(mainColors[i]);
            try {
                mainMenuButtons[i] = new Button(adaptGen.createImageAdapter(Meta.pathToGameFolder + mainImageFiles[i]), null, 0xFF000000|colors[0], new int[]{0xFF000000|colors[1], 0xFF000000});
                mainMenuButtons[i].setData(new Integer(i));
                mainMenuButtons[i].setButtonPressedListener(mainMenuListener);
                switch (i) {
                    case 0:
                        mainMenuButtons[i].setHelperTextID(ITEMS);
                        break;
                    case 1:
                        mainMenuButtons[i].setHelperTextID(ORDER);
                        break;
                    case 2:
                        mainMenuButtons[i].setHelperTextID(MAP);
                        break;
                    case 3:
                        mainMenuButtons[i].setHelperTextID(SAVE);
                        break;
                    case 4:
                        mainMenuButtons[i].setHelperTextID(VOLUME);
                        break;
                    case 5:
                        mainMenuButtons[i].setHelperTextID(QUIT);
                        break;
                }
            } catch (IOException ex) {
                throw new RuntimeException("Menu button couldn't be loaded: " + ex.toString());
            }
            
            mainMenuButtons[i].setFocusGainedListener(makeHighlightListener);
        }
        mainMenu = new Composite(mainMenuButtons, Composite.HORIZONTAL);
        
        //List of Hero's names:
        String[] str = new String[getRPG().getNumHeroes()];
        for (int i=0; i<str.length; i++)
            str[i] = getRPG().getHero(i).name;
        heroName = new FlatList(this, str, 0, MARGIN);
        heroName.setListItemChangedListener(new Action() {
            public boolean perform(Object caller) {
                Object[] pics = (Object[])heroPicture.getData();
                heroPicture.swapImage((ImageAdapter)pics[heroName.getCurrItemID()]);
                
                if (mode == SPELLS)
                    reloadSpellData();
                else
                    spellList.setData(null);
                
                //Doesn't matter
                return true;
            }
        });
        heroName.setFocusGainedListener(makeHighlightListener);
        heroName.setHelperTextID(HERO);
        
        //Button for each hero's pictures
     //   Hero hr = getRPG().getHero(0);
        int[] colors2 = getRPG().getTextBoxColors(0);

        ImageAdapter pic = null;
        try {
            pic = adaptGen.createImageAdapter(Meta.pathToGameFolder + adaptGen.getGameName() + "/HERO_0.PNG");
        } catch (IOException ex) {
            throw new RuntimeException("IO Error making hero 0's pic: " + ex.getMessage());
        }
        heroPicture = new Button(
                    pic, 
                    null, //Grr...
                    0, //Doesn't matter (in this case)
                    new int[] {0xFF000000|colors2[1], 0xFF000000}
                );
        heroPicture.setData(new Object[getRPG().getNumHeroes()]);
        heroPicture.setFocusGainedListener(makeHighlightListener);
        ((Object[])heroPicture.getData())[0] = pic;
        for (int i=1; i<getRPG().getNumHeroes(); i++) {
           // hr = getRPG().getHero(i);
            try {
                ((Object[])heroPicture.getData())[i] = adaptGen.createImageAdapter(Meta.pathToGameFolder + adaptGen.getGameName() + "/HERO_" + i +  ".PNG");
            } catch (IOException ex) {
                throw new RuntimeException("IO Error making hero "+i+"'s pic: " + ex.getMessage());
            }
        }
        
        //Hero-specific menu buttons
        int[] clr0 = getRPG().getTextBoxColors(0);
        try {
            heroEquip = new Button(adaptGen.createImageAdapter(Meta.pathToGameFolder + imgEquip), null, 0xFF000000|clr0[0], new int[]{0xFF000000|clr0[1], 0xFF000000});
            heroSpells = new Button(adaptGen.createImageAdapter(Meta.pathToGameFolder + imgSpells), null, 0xFF000000|clr0[0], new int[]{0xFF000000|clr0[1], 0xFF000000});
            heroStats = new Button(adaptGen.createImageAdapter(Meta.pathToGameFolder + imgStats), null, 0xFF000000|clr0[0], new int[]{0xFF000000|clr0[1], 0xFF000000});
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't find icon for equip/spells/stats: " + ex.toString());
        }
        heroEquip.setFocusGainedListener(makeHighlightListener);
        heroEquip.setHelperTextID(EQUIP);
        heroSpells.setFocusGainedListener(makeHighlightListener);
        heroSpells.setHelperTextID(SPELLS);
        heroStats.setFocusGainedListener(makeHighlightListener);
        heroStats.setHelperTextID(STATS);
        heroSpells.setButtonPressedListener(new Action() {
            public boolean perform(Object caller) {
                heroEquip.clearPaintFlag();
                heroStats.clearPaintFlag();
                
                //heroPicture.setPosition(-heroName.getPosX()-heroPicture.getWidth()/2, heroPicture.getPosY());
                //heroSpells.setPosition(heroSpells.getPosX(), 0);
                lblSpells.setPosition(width, lblSpells.getPosY());
                
               // heroPicture.disconnect(MenuItem.CONNECT_LEFT);
               // heroPicture.disconnect(MenuItem.CONNECT_BOTTOM);
                
                heroSpells.connect(lblSpells, MenuItem.CONNECT_RIGHT);
                //heroSpells.connect(currSpellMP, MenuItem.CONNECT_BOTTOM);
                
                if (spellList.getData()==null) {
                    //Reload spell data
                  //  currSpellGroup.reset();
                    reloadSpellData();
                }
                
                heroPicture.setFocusGainedListener(new Action() {
                    public boolean perform(Object o) {
                        nextMI = currSpellGroup.moveTo();
                        
                        return false;
                    }
                });

                currCursor = null;
                currTransition =  new SpellShiftTransition();

                mode = SPELLS;
                
                return true;
            }
        });
        
        /////////////////////////////////////////////////
        // Section 2, Spells
        /////////////////////////////////////////////////
        lblSpells = new Label(new TextBox("Spells", getRPG().font, clr0[1], clr0[0], true, Canvas.FILL_SOLID));
        currSpellGroup = new FlatList(this, getOHRRPG().getCurrHero().getSpellGroupNames(), 0, MARGIN);
        currSpellGroup.setListItemChangedListener(new Action() {
            public boolean perform(Object o) {                    
                reloadSpellData();
                return true;
            }
        });
        currSpellGroup.setFocusGainedListener(makeHighlightListener);
    //    currSpellGroup.setCancelListener(cancelSpellsMenuListen);
       // currSpellGroup.blockConnection(MenuItem.CONNECT_TOP);
        currSpellMP = new MPBox(getRPG(), 0);
        currSpellMP.setFocusGainedListener(new Action() {
           public boolean perform(Object o) {
               nextMI = heroName.moveTo();
                       
               return false;
           } 
        });
        //currSpellMP.setMP(getOHRRPG().getActiveHero().getHP(), getOHRRPG().getActiveHero().getMaxHP(), false, -1);
        heroUsesSpellOn = new HeroSelector(getRPG(), 4, 4);
        int remHeight = height - (mainMenu.getPosY()+mainMenu.getHeight()+heroName.getPosY()+heroName.getHeight()+heroPicture.getPosY()+heroPicture.getHeight());
        Action slItemChanged = new Action() {
            public boolean perform(Object o) {
                //Fix Description
                if (((TextBox[])spellDesc.getData()).length==0) {
                    spellDesc.setTextBox(null);
                } else {
                    TextBox currBox = ((TextBox[])spellDesc.getData())[spellList.getCurrItemID()];
                    if (currBox==null) {
                        currBox = new TextBox(
                                ((Spell[])spellList.getData())[spellList.getCurrItemID()].spellDescription,
                                getRPG().font, 0, 0, true, Canvas.FILL_NONE, true,
                                new int[]{spellDesc.getWidth(), spellDesc.getHeight()});
                        ((TextBox[])spellDesc.getData())[spellList.getCurrItemID()] = currBox;
                    }
                    spellDesc.setTextBox(currBox);
                }
                
                //Redo Highlight
                makeHighlightAt(spellList.getCurrItemRectangle());
                
                //Doesn't matter
                return true;
            } 
        };
        spellList =  new List(width-heroUsesSpellOn.getWidth()-MARGIN*2+1, remHeight-MARGIN*2, 0xFF000000|clr0[0], new int[]{0xFF000000|clr0[1], 0xFF000000}, 2, getRPG().font);
        spellList.setListItemChangedListener(slItemChanged);
        spellList.setFocusGainedListener(slItemChanged);
        spellList.setFocusLostListener(new Action() {
            public boolean perform(Object caller) {
                spellDesc.setTextBox(null);
                return true;
            }
        });
      //  spellList.setCancelListener(cancelSpellsMenuListen);
        spellDesc = new Label(heroUsesSpellOn.getWidth(), spellList.getHeight()-heroUsesSpellOn.getHeight()+1, 0xFF000000|clr0[0], new int[]{0xFF000000|clr0[1], 0xFF000000});
        spellDesc.setFocusGainedListener(makeHighlightListener);
        
        
        /////////////////////////////////////////////////
        // Volume
        /////////////////////////////////////////////////     
        tempVol = new Label(new TextBox("(incomplete)", getRPG().font, 0xFFFF00FF, 0xFF770066, true));
        tempVol.setFocusGainedListener(makeHighlightListener);
        //tempVol.setCancelListener(cancelMainMenuListener);
        
        /////////////////////////////////////////////////
        // Connections & Layout
        /////////////////////////////////////////////////
        mainMenu.setPosition(0, MARGIN);
        for (int i=0; i<mainMenuButtons.length; i++) 
            mainMenuButtons[i].setPosition(MARGIN, 0);
        heroName.setPosition(width/2-heroName.getWidth()/2, MARGIN);
        heroPicture.setPosition(width/2-heroPicture.getWidth()/2-heroName.getPosX(), 2*MARGIN);
        heroEquip.setPosition(MARGIN, heroPicture.getHeight()/2-heroEquip.getHeight()/2);
        heroSpells.setPosition(MARGIN, heroPicture.getHeight()/2-heroEquip.getHeight()/2);
        heroStats.setPosition(heroPicture.getWidth()/2-heroEquip.getWidth()/2, MARGIN);
        lblSpells.setPosition(MARGIN, 0);
        currSpellGroup.setPosition(
                currSpellMP.getWidth()/2 - currSpellGroup.getWidth()/2,
                MARGIN);
        currSpellMP.setPosition(
                (width - heroPicture.getWidth()/2)/2 - currSpellMP.getWidth()/2 - heroSpells.getPosX(),
                heroPicture.getHeight()-heroSpells.getHeight()-currSpellGroup.getHeight()-currSpellGroup.getPosY()-currSpellMP.getHeight());
        spellDesc.setPosition(-1, 0);
        heroUsesSpellOn.setPosition(0, -1);
        tempVol.setPosition(width/2 - tempVol.getWidth()/2, height/3);
        helperTxt.setPosition((width-heroName.getWidth())/2-helperTxt.getWidth()-MARGIN, height-mainMenu.getPosY()-mainMenu.getHeight()-heroName.getPosY()-helperTxt.getHeight()-MARGIN);
        mainMenu.connect(heroName, MenuItem.CONNECT_BOTTOM);
        heroName.connect(heroPicture, MenuItem.CONNECT_BOTTOM);
        heroName.connect(helperTxt, MenuItem.CONNECT_RIGHT);
        heroEquip.connect(heroPicture, MenuItem.CONNECT_RIGHT);
        heroSpells.connect(heroPicture, MenuItem.CONNECT_LEFT);
        heroStats.connect(heroPicture, MenuItem.CONNECT_TOP);
        currSpellMP.connect(currSpellGroup, MenuItem.CONNECT_BOTTOM);
        currSpellGroup.connect(spellList, MenuItem.CONNECT_BOTTOM);
        spellList.connect(spellDesc, MenuItem.CONNECT_RIGHT);
        spellDesc.connect(heroUsesSpellOn, MenuItem.CONNECT_BOTTOM);
        spellList.blockConnection(MenuItem.CONNECT_RIGHT);
        heroName.blockConnection(MenuItem.CONNECT_RIGHT);
        
        //Minor hack to have a proper background:
        int[] temp = getRPG().getTextBoxColors(0);
        int[] colors = new int[]{
            Math.min((((temp[0]&0xFF0000)/0x10000)*14)/10, 0xFF)*0x10000+
                    Math.min((((temp[0]&0xFF00)/0x100)*14)/10, 0xFF)*0x100+
                    Math.min(((temp[0]&0xFF)*14)/10, 0xFF)+0xFF000000,
            0xFF888888
        };
        int combinedHeight = 
                mainMenu.getPosY()+
                mainMenu.getActiveSubItem().getPosY()+mainMenu.getActiveSubItem().getHeight()+
                heroName.getPosY()+heroName.getHeight()+
                heroPicture.getPosY()/2
                +1;
        topBkgrd = new Canvas(width, combinedHeight, colors[0], new int[]{colors[1], 0xFF000000}, Canvas.FILL_SOLID);
        btmBkgrd = new Canvas(width, height-combinedHeight+1, colors[0], new int[]{colors[1], 0xFF000000}, Canvas.FILL_SOLID);
        btmBkgrd.setPosition(0, combinedHeight-1);
        //Pre-compute our "darkening" effect.
        // Edit: Nope! This is a big space-waster!
        colorOverlayRGB = clr0[0];

        
        //SET
        topLeftMI = mainMenu;
        
        
        

    }
    
    
    ///
    /// Note that this class disregards Transition's object-orientedness, since
    ///  it's a bit clumsy & wasteful.
    ///
    class ButtonMoveTransition extends Transition {
        private MenuItem finalConnect;
        private Canvas savedBoxOverlay;
        
        private static final int PHASE_ONE = 1;
        private static final int PHASE_TWO = 2;
        private static final int PHASE_DONE = 3;
        private int phase;
      //  private boolean phaseFirstStep;
        
        private int currBlackIndex;
        private int destBoxX;
        private int destTxtX;
        private int destBkgrdY;
        private int speed;
        
        public ButtonMoveTransition(MenuItem finalConnect) {
            this.finalConnect = finalConnect;
            //init();
            //setupActions();
            reset();
        }
        
        public void reset() {
            phase = PHASE_ONE;
           // phaseFirstStep = true;
            
            currBlackIndex = 0;
            destBoxX = MARGIN;
            destTxtX = -1;
            destBkgrdY = mainMenu.getPosY()+mainMenu.getHeight();
            savedBoxOverlay = boxOverlay;
            boxOverlay = null;
            
            //We don't want them waiting forever for the "Quit" button.
            speed = Math.max(defaultSpeed, (currItemBtn.getPosX()-destBoxX)/darkenInterval);
        }
        
        public boolean isDone() {
            return phase==PHASE_DONE;
        }
        
        public boolean step() {
            switch (phase) {
                case PHASE_ONE:
                    //Are we done?
                    int currBoxX = currItemBtn.getPosX();
                    if (currBlackIndex==darkenInterval && currBoxX==destBoxX) {
                        currItemBtn.connect(currItemTxt, MenuItem.CONNECT_RIGHT);
                        boxOverlay = savedBoxOverlay;
                        
                        phase = PHASE_TWO;
                   //     phaseFirstStep = true;
                        break;
                    }
                    
                    //Darken the box?
                    if (currBlackIndex<darkenInterval) {
                        int alpha = (currBlackIndex*0xBB)/(darkenInterval-1);
                        blackOverlay = new Canvas(width, height, alpha*0x1000000, new int[]{}, Canvas.FILL_GUESS);
                        
                        currBlackIndex++;
                    }
                    
                    //Move the button?
                  //  if (phaseFirstStep) 
                 //       phaseFirstStep = false;
                 //   else {
                        if (currBoxX > destBoxX+speed) {
                            currItemBtn.setPosition(currItemBtn.getPosX()-speed, currItemBtn.getPosY());
                        } else if (currBoxX > destBoxX) {
                            currItemBtn.setPosition(destBoxX, currItemBtn.getPosY());
                        } else if (currBoxX != destBoxX) {
                            throw new RuntimeException("Bad currBoxX: " + currBoxX + "  in regards to destBoxX: " + destBoxX);
                        }
                   // }
                    break;
                case PHASE_TWO:
                    int currTxtX = currItemTxt.getPosX();
                    int currBkgrdY = boxOverlay.getPosY();
                    if (currTxtX==destTxtX && currBkgrdY==destBkgrdY) {
                        currItemBtn.connect(finalConnect, MenuItem.CONNECT_BOTTOM);
                        currItemBtn.blockConnection(MenuItem.CONNECT_BOTTOM);
                        currMI = finalConnect.moveTo();
                        dirtyHighlight = true;
                        //blackOverlay = null;
                        
                        phase = PHASE_DONE;
                    //    phaseFirstStep = true;
                        break;
                    }
                    
                    //Move the text field?
                //    if (phaseFirstStep) 
               //         phaseFirstStep = false;
                 //   else {
                        if (currTxtX < destTxtX-defaultSpeed*2) {
                            currItemTxt.setPosition(currItemTxt.getPosX()+defaultSpeed*2, currItemTxt.getPosY());
                        } else if (currTxtX < destTxtX) {
                            currItemTxt.setPosition(destTxtX, currItemTxt.getPosY());
                        } else if (currTxtX != destTxtX) {
                            throw new RuntimeException("Bad currTxtX: " + currTxtX + "  in regards to destTxtX: " + destTxtX);
                        }
                        
                        //Move the background box?
                        if (currBkgrdY < destBkgrdY-defaultSpeed*5) {
                            boxOverlay.setPosition(boxOverlay.getPosX(), boxOverlay.getPosY()+defaultSpeed*5);
                        } else if (currBkgrdY < destBkgrdY) {
                            boxOverlay.setPosition(boxOverlay.getPosX(), destBkgrdY);
                        } else if (currBkgrdY != destBkgrdY) {
                            throw new RuntimeException("Bad currBkgrdY: " + currBkgrdY + "  in regards to destBkgrdY: " + destBkgrdY);
                        }
                  //  }

                    
                    break;
                default:
                    throw new RuntimeException("Illegal step: this Transition has finished!");
            }
            
            return isDone();
        }
        
        public void setupActions() {}
    }
    
   ///
     /// Also disregards Transition's built-in functionality.
    ///
    class ButtonRevertTransition extends Transition {
     //   private MenuItem finalConnect;
        private Canvas savedBoxOverlay;
        
        private static final int PHASE_ONE = 1;
        private static final int PHASE_TWO = 2;
        private static final int PHASE_DONE = 3;
        private int phase;
      //  private boolean phaseFirstStep;
        
        private int currBlackIndex;
        private int destBoxX;
        private int destTxtX;
        private int destBkgrdY;
        private int speed;
        
        public ButtonRevertTransition() {
        //    this.finalConnect = finalConnect;
            //init();
            //setupActions();
            reset();
        }
        
        public void reset() {
            phase = PHASE_ONE;
            
            currBlackIndex = darkenInterval-1;
            destBoxX = itemReturnPos;
            destTxtX = -currItemTxt.getWidth();
            destBkgrdY = -boxOverlay.getHeight()+mainMenu.getPosY()+mainMenu.getHeight()+2;
            
            //We don't want them waiting forever for the "Quit" button.
            speed = Math.max(defaultSpeed, (destBoxX-currItemBtn.getPosX())/darkenInterval);
        }
        
        public boolean isDone() {
            return phase==PHASE_DONE;
        }
        
        public boolean step() {
            switch (phase) {
                case PHASE_ONE:
                    int currTxtX = currItemTxt.getPosX();
                    int currBkgrdY = boxOverlay.getPosY();
                    if (currTxtX==destTxtX && currBkgrdY==destBkgrdY) {
                        savedBoxOverlay = boxOverlay;
                        boxOverlay = null;
                        currItemBtn.disconnect(MenuItem.CONNECT_RIGHT);
                        //currItemBtn.blockConnection(MenuItem.CONNECT_BOTTOM);
                        //currMI = finalConnect.moveTo();
                        //dirtyHighlight = true;
                        
                        phase = PHASE_TWO;
                        break;
                    }
                    
                    //Move the text field?
                    if (currTxtX > destTxtX+(defaultSpeed*3)/2) {
                        currItemTxt.setPosition(currItemTxt.getPosX()-(defaultSpeed*3)/2, currItemTxt.getPosY());
                    } else if (currTxtX > destTxtX) {
                        currItemTxt.setPosition(destTxtX, currItemTxt.getPosY());
                    } else if (currTxtX != destTxtX) {
                        throw new RuntimeException("Bad currTxtX: " + currTxtX + "  in regards to destTxtX: " + destTxtX);
                    }

                    //Move the background box?
                    if (currBkgrdY > destBkgrdY+defaultSpeed*5) {
                        boxOverlay.setPosition(boxOverlay.getPosX(), boxOverlay.getPosY()-defaultSpeed*5);
                    } else if (currBkgrdY > destBkgrdY) {
                        boxOverlay.setPosition(boxOverlay.getPosX(), destBkgrdY);
                    } else if (currBkgrdY != destBkgrdY) {
                        throw new RuntimeException("Bad currBkgrdY: " + currBkgrdY + "  in regards to destBkgrdY: " + destBkgrdY);
                    }
                    break;
                case PHASE_TWO:
                    //Are we done?
                    int currBoxX = currItemBtn.getPosX();
                    if (currBlackIndex==-1 && currBoxX==destBoxX) {
                        currItemBtn = null;
                        boxOverlay = savedBoxOverlay;
                        
                        //Return control
                        mode = lastMode;
                        currMI = mainMenu;
                        currMI.moveTo();

                        dirtyHighlight = true;
                        blackOverlay = null;
                        
                        phase = PHASE_DONE;
                        break;
                    }
                    
                    //Darken the box?
                    if (currBlackIndex>=0) {
                        int alpha = (currBlackIndex*0xBB)/(darkenInterval-1);
                        blackOverlay = new Canvas(width, height, alpha*0x1000000, new int[]{}, Canvas.FILL_GUESS);
                        currBlackIndex--;
                    }
                    
                    //Move the button?
                    if (currBoxX < destBoxX-speed) {
                        currItemBtn.setPosition(currItemBtn.getPosX()+speed, currItemBtn.getPosY());
                    } else if (currBoxX < destBoxX) {
                        currItemBtn.setPosition(destBoxX, currItemBtn.getPosY());
                    } else if (currBoxX != destBoxX) {
                        throw new RuntimeException("Bad currBoxX: " + currBoxX + "  in regards to destBoxX: " + destBoxX);
                    }
                    break;
                default:
                    throw new RuntimeException("Illegal step: this Transition has finished!");
            }
            
            return isDone();
        }
        
        public void setupActions() {}
    }
    
    
    ///
    /// Also disregards Transition's built-in functionality.
    ///
    class SpellShiftTransition extends Transition {
        private int destHeroPicX;
        private int destSpellsBtnY;
        private int destSpellsTxtX;
        
        private static final int PHASE_ONE = 1;
        private static final int PHASE_TWO = 2;
        private static final int PHASE_DONE = 3;
        private int phase;
        
        public SpellShiftTransition() {
            reset();
        }
        
        public void reset() {
            phase = PHASE_ONE;
            destSpellsTxtX = MARGIN;
            destSpellsBtnY = 0;
            destHeroPicX = -heroName.getPosX()-heroPicture.getWidth()/2;
        }
        
        public boolean isDone() {
            return phase==PHASE_DONE;
        }
        
         public boolean step() {
             int currHeroPicX = heroPicture.getPosX();
             int currSpellsBtnY = heroSpells.getPosY();
             int currSpellsTxtX = lblSpells.getPosX();
             
             switch (phase) {
                 case PHASE_ONE:
                     //Are we done?
                     if (currHeroPicX == destHeroPicX) {
                        //Move on to the next phase
                        heroPicture.disconnect(MenuItem.CONNECT_LEFT);
                        heroPicture.disconnect(MenuItem.CONNECT_BOTTOM);
                        phase = PHASE_TWO;
                        break;
                     }
                     
                    //Move the hero's picture?
                    if (currHeroPicX > destHeroPicX+defaultSpeed*3) {
                        heroPicture.setPosition(heroPicture.getPosX()-defaultSpeed*3, heroPicture.getPosY());
                    } else if (currHeroPicX > destHeroPicX) {
                        heroPicture.setPosition(destHeroPicX, heroPicture.getPosY());
                    } else if (currHeroPicX != destHeroPicX) {
                        throw new RuntimeException("Bad currHeroPicX: " + currHeroPicX + "  in regards to destHeroPicX: " + destHeroPicX);
                    }
                        
                     break;
                 case PHASE_TWO:
                     //Are we done?
                     if (currSpellsBtnY==destSpellsBtnY && currSpellsTxtX==destSpellsTxtX) {
                        heroSpells.connect(currSpellMP, MenuItem.CONNECT_BOTTOM);
                        heroName.disconnect(MenuItem.CONNECT_RIGHT);
                        currMI = spellList.moveTo();
                        dirtyHighlight = true;
                        
                        phase = PHASE_DONE;
                        break;
                     }
                     
                    //Move the spells button?
                    if (currSpellsBtnY > destSpellsBtnY+defaultSpeed) {
                        heroSpells.setPosition(heroSpells.getPosX(), heroSpells.getPosY()-defaultSpeed);
                    } else if (currSpellsBtnY > destSpellsBtnY) {
                        heroSpells.setPosition(heroSpells.getPosX(), destSpellsBtnY);
                    } else if (currSpellsBtnY != destSpellsBtnY) {
                        throw new RuntimeException("Bad currSpellsBtnY: " + currSpellsBtnY + "  in regards to destSpellsBtnY: " + destSpellsBtnY);
                    }
                     
                    //Move the "Spells" text box?
                    if (currSpellsTxtX > destSpellsTxtX+defaultSpeed*5) {
                        lblSpells.setPosition(lblSpells.getPosX()-defaultSpeed*5, lblSpells.getPosY());
                    } else if (currSpellsTxtX > destSpellsTxtX) {
                        lblSpells.setPosition(destSpellsTxtX, lblSpells.getPosY());
                    } else if (currSpellsTxtX != destSpellsTxtX) {
                        throw new RuntimeException("Bad currSpellsTxtX: " + currSpellsTxtX + "  in regards to destSpellsTxtX: " + destSpellsTxtX);
                    }
                     
                     break;
                default:
                    throw new RuntimeException("Illegal step: this Transition has finished!");
             }
          
             return isDone();
         }
        
        public void setupActions() {}
    }

    
    ///
    /// Also disregards Transition's built-in functionality.
    ///
    class SpellShiftBackTransition extends Transition {
        private int destHeroPicX;
        private int destSpellsBtnY;
        private int destSpellsTxtX;
        
        private static final int PHASE_ZERO = 0;
        private static final int PHASE_ONE = 1;
        private static final int PHASE_TWO = 2;
        private static final int PHASE_DONE = 3;
        private int phase;
       
        
        public SpellShiftBackTransition() {
            reset();
        }
        
        public void reset() {
            phase = PHASE_ZERO;
            destSpellsTxtX = width;
            destSpellsBtnY = heroPicture.getHeight()/2-heroEquip.getHeight()/2;
            destHeroPicX = width/2-heroPicture.getWidth()/2-heroName.getPosX();
        }
        
        public boolean isDone() {
            return phase==PHASE_DONE;
        }
        
         public boolean step() {
             int currHeroPicX = heroPicture.getPosX();
             int currSpellsBtnY = heroSpells.getPosY();
             int currSpellsTxtX = lblSpells.getPosX();
             
             switch (phase) {
                 case PHASE_ZERO:
                     heroName.connect(helperTxt, MenuItem.CONNECT_RIGHT);
                     phase = PHASE_ONE;
                     break;
                 case PHASE_ONE:
                     //Are we done?
                     if (currSpellsBtnY==destSpellsBtnY && currSpellsTxtX==destSpellsTxtX) {
                        heroSpells.disconnect(MenuItem.CONNECT_RIGHT);
                        
                        phase = PHASE_TWO;
                        break;
                     }
                     
                    //Move the spells button?
                     if (currSpellsTxtX > destSpellsTxtX/2) {
                         if (currSpellsBtnY < destSpellsBtnY-defaultSpeed) {
                             heroSpells.setPosition(heroSpells.getPosX(), heroSpells.getPosY()+defaultSpeed);
                         } else if (currSpellsBtnY < destSpellsBtnY) {
                             heroSpells.setPosition(heroSpells.getPosX(), destSpellsBtnY);
                         } else if (currSpellsBtnY != destSpellsBtnY) {
                             throw new RuntimeException("Bad currSpellsBtnY: " + currSpellsBtnY + "  in regards to destSpellsBtnY: " + destSpellsBtnY);
                         }
                     }
                     
                    //Move the "Spells" text box?
                    if (currSpellsTxtX < destSpellsTxtX-defaultSpeed*5) {
                        lblSpells.setPosition(lblSpells.getPosX()+defaultSpeed*5, lblSpells.getPosY());
                    } else if (currSpellsTxtX < destSpellsTxtX) {
                        lblSpells.setPosition(destSpellsTxtX, lblSpells.getPosY());
                    } else if (currSpellsTxtX != destSpellsTxtX) {
                        throw new RuntimeException("Bad currSpellsTxtX: " + currSpellsTxtX + "  in regards to destSpellsTxtX: " + destSpellsTxtX);
                    }
                     
                     break;
                 case PHASE_TWO:
                     //Are we done?
                     if (currHeroPicX == destHeroPicX) {
                        //Move on to the next phase
                        currMI = heroPicture.moveTo();
                        dirtyHighlight = true;
                        phase = PHASE_DONE;
                        heroPicture.setFocusGainedListener(makeHighlightListener);
                        break;
                     }
                     
                    //Move the hero's picture?
                    if (currHeroPicX < destHeroPicX-defaultSpeed*3) {
                        heroPicture.setPosition(heroPicture.getPosX()+defaultSpeed*3, heroPicture.getPosY());
                    } else if (currHeroPicX < destHeroPicX) {
                        heroPicture.setPosition(destHeroPicX, heroPicture.getPosY());
                    } else if (currHeroPicX != destHeroPicX) {
                        throw new RuntimeException("Bad currHeroPicX: " + currHeroPicX + "  in regards to destHeroPicX: " + destHeroPicX);
                    }
                        
                     break;

                default:
                    throw new RuntimeException("Illegal step: this Transition has finished!");
             }
          
             return isDone();
         }
        
        public void setupActions() {}
    }
    
    
    ///
    /// Also disregards Transition's built-in functionality.
     ///
    class MenuInTransition extends Transition {
        private static final int PHASE_ONE = 1;
        private static final int PHASE_TWO = 2;
        private static final int PHASE_DONE = 3;
        private int phase;
       
        private int currAngle;
        private int currFadeIndex;
        private static final int maxAngle = 360;
        private static final int angleIncr = maxAngle/((colorInIntervals*3)/2);
        
        public MenuInTransition() {
            reset();
        }
        
        public void reset() {
            currAngle = 0;
            currFadeIndex = 0;
            phase = PHASE_ONE;
        }
        
        public boolean isDone() {
            return phase==PHASE_DONE;
        }
        
        public int getAngle() {
            return currAngle;
        }
   ///  public int getColorOverlayIndex() {
        ///    return currFadeIndex;
      ///
        
         public boolean step() {
             switch (phase) {
                 case PHASE_ONE:
                     //Are we done?
                     if (currAngle==maxAngle && currFadeIndex==colorInIntervals-1) {
                        colorOverlay = null;
                        phase = PHASE_DONE;
                        break;
                     }
                     
                    //Lengthen our arc
                     if (currAngle<maxAngle) {
                         currAngle+=angleIncr;
                         if (currAngle+angleIncr>maxAngle)
                             currAngle = maxAngle;
                     }
                     
                    //Darken our coloring
                    if (currAngle>angleIncr*(colorInIntervals+2) && currFadeIndex<colorInIntervals-1) {
                        currFadeIndex++;
                        
                        int alpha = (currFadeIndex*0xBB)/(colorInIntervals-1);
                        colorOverlay = new Canvas(width, height, alpha*0x1000000+colorOverlayRGB, new int[]{}, Canvas.FILL_TRANSLUCENT);
                    }
                     
                     break;

                default:
                    throw new RuntimeException("Illegal step: this Transition has finished!");
             }
          
             return isDone();
         }
        
        public void setupActions() {}
    }

    public boolean canExit() {
        bufferedESC = true;
        return false;
    }

    
    public boolean runIdle() {
    	//Maybe we can load components for sub-menus in segments?
    	return false;
    }


}*/
