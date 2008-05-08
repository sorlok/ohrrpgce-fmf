/*
 * MenuEngine.java
 * Created on April 10, 2007, 8:09 PM
 */

package ohrrpgce.game;

import ohrrpgce.adapter.*;
import ohrrpgce.data.*;
import ohrrpgce.menu.*;
import ohrrpgce.runtime.*;



/**
 * The OHRRPGCEFMF's menu.
 * @author Seth N. Hetu 
 */
public class MenuEngine extends Engine {  
    //Debug
    public static boolean DEBUG_CONTROL = false;
    
    //Constants
    private static final int MULTIPRESS_DELAY = 12;
    

    
    private static final String imgEquip = "main_icons/equip.png";
    private static final String imgStats = "main_icons/stats.png";
    private static final String imgSpells = "main_icons/spells.png";

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

    
    
    //Meta-type info
    private AdapterGenerator adaptGen;
    private EngineSwitcher midletHook;
    private int width;
    private int height;
    
    //Menu control
    //private MenuSlice topLeftMI;
    //private Transition currTransition;
    private boolean initDoneOnce;
    private int delayTimer;
    private boolean bufferedESC;
    
            
    
    
    
    public MenuEngine(AdapterGenerator adaptGen, EngineSwitcher midletHook) {
        this.width = adaptGen.getScreenWidth();
        this.height = adaptGen.getScreenHeight();
        this.midletHook = midletHook;
        this.adaptGen = adaptGen;
    }
    
    
    /**
     * Pass commands down to the current menu item.
     */
    public void handleKeys(int keyStates) {
        //Dont' allow the user to mess up any animations.
        if (MetaMenu.currTransition!=null)
            return;
    	
        //The key autofires if pressed for a certain length of time.
        if (!bufferedESC && (keyStates==0)) {
            delayTimer = 0;
        } else {
            if (delayTimer<=0) {
                //OHR key-detect order: MENU, ENTER, UP, DOWN, LEFT, RIGHT
                if (bufferedESC || (keyStates&InputAdapter.KEY_CANCEL)!=0) {
                    System.out.println("CANCEL");
                    
                    //The action taken depends on the menu's current state. 
                    if (MetaMenu.mode == MetaMenu.MAIN)
                    	midletHook.egress(new Integer(42));
                    else if (MetaMenu.mode==MetaMenu.ITEMS || MetaMenu.mode==MetaMenu.ORDER || MetaMenu.mode==MetaMenu.MAP || MetaMenu.mode==MetaMenu.SAVE || MetaMenu.mode==MetaMenu.VOLUME || MetaMenu.mode==MetaMenu.QUIT) {
                    	//Do stuff
                    	MetaMenu.doMainMenuOut();
                    } else if (MetaMenu.mode==MetaMenu.SPELLS || MetaMenu.mode==MetaMenu.STATS || MetaMenu.mode==MetaMenu.EQUIP) {
                    	MetaMenu.doSubMenuOut();
                    } else
                    	MetaMenu.topLeftMI.cancel();
                    bufferedESC = false;
                } else if ((keyStates&InputAdapter.KEY_ACCEPT)!=0) {
                    System.out.println("ACCEPT");
                    MetaMenu.topLeftMI.accept();
                } else if ((keyStates&InputAdapter.KEY_UP)!=0) {
                    System.out.println("UP");
                    MetaMenu.topLeftMI.processInput(MenuSlice.CONNECT_TOP);
                } else if ((keyStates&InputAdapter.KEY_DOWN)!=0) {
                    System.out.println("DOWN");
                    MetaMenu.topLeftMI.processInput(MenuSlice.CONNECT_BOTTOM);
                } else if ((keyStates&InputAdapter.KEY_LEFT)!=0) {
                    System.out.println("LEFT");
                    MetaMenu.topLeftMI.processInput(MenuSlice.CONNECT_LEFT);
                } else if ((keyStates&InputAdapter.KEY_RIGHT)!=0) {
                    System.out.println("RIGHT");
                    MetaMenu.topLeftMI.processInput(MenuSlice.CONNECT_RIGHT);
                } else 
                    throw new RuntimeException("Somehow, no input was pressed (but expected) for input: " + keyStates);
                
                
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
    	//Self-painting transitions?
    	if (MetaMenu.currTransition!=null) {
    		if (MetaMenu.currTransition.doPaintOver())
    			return;
    	}
    
    	//Paint the menu
    	MetaMenu.topLeftMI.paintMenuSlice(-1);
    	
    	//Paint the cursor
    	if (MetaMenu.currCursor!=null)
    		MetaMenu.currCursor.paintMenuSlice(-1);
    }

    public void updateScene(long elapsed) {
    	//Update the current transition
        if (MetaMenu.currTransition != null) {
            MetaMenu.currTransition.step();
            try {
            	if (MetaMenu.currTransition.requiresReLayout()) {
            		MetaMenu.topLeftMI.doLayout();
            	}
            } catch (Exception ex) {
            	throw new LiteException(this, ex, "Transition Layout Error");
            }
            if (MetaMenu.currTransition.isDone()) {
            	if (MetaMenu.currTransition.getNewFocus()!=null) {
            		MetaMenu.currTransition.getNewFocus().moveTo();
            	}
            	MetaMenu.currTransition = null;
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
        		MetaMenu.buildMenu(width, height, getRPG(), adaptGen);
        		
        		initDoneOnce = true;
        	} catch (Exception ex) {
        		throw new LiteException(this, ex, "Menu failed on INIT");
        	}
        }
        
        //Reset hero party, if necessary
        MetaMenu.resetHeroParty(getRPG());
        
        //Set components
        MetaMenu.topLeftMI.doLayout();
        MetaMenu.topLeftMI.moveTo();
        
        //Set transitions
        MetaMenu.currTransition = MetaMenu.menuInTrans;
        MetaMenu.mode = MetaMenu.MAIN;
        
        //Reset key presses
        delayTimer = MULTIPRESS_DELAY;
    }
    
    public RPG getRPG() {
        return ((GameEngine)midletHook.getCaller()).getRPG().getBaseRPG();
    }
    
    public OHRRPG getOHRRPG() {
        return ((GameEngine)midletHook.getCaller()).getRPG();
    }
    
    
    public boolean canExit() {
        bufferedESC = true;
        return false;
    }

    
    public boolean runIdle() {
    	//Maybe we can load components for sub-menus in segments?
    	return false;
    }


}
