/*
 * MenuEngine.java
 * Created on April 10, 2007, 8:09 PM
 */

package ohrrpgce.game;


import java.io.IOException;
import java.util.Vector;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.adapter.InputAdapter;

import ohrrpgce.data.Hero;
import ohrrpgce.data.Spell;
import ohrrpgce.data.RPG;
import ohrrpgce.henceforth.Int;
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
import ohrrpgce.menu.MenuSlice;
import ohrrpgce.menu.SpecialLabel;
import ohrrpgce.menu.TextBox;
import ohrrpgce.menu.transitions.MenuInTransition;
import ohrrpgce.menu.transitions.Transition;
import ohrrpgce.runtime.Meta;
import ohrrpgce.runtime.MetaMenu;
import ohrrpgce.runtime.OHRRPG;


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
    private MenuSlice topLeftMI;
    private Transition currTransition;
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
                } else if ((keyStates&InputAdapter.KEY_ACCEPT)!=0) {
                    System.out.println("ACCEPT");
                } else if ((keyStates&InputAdapter.KEY_UP)!=0) {
                    System.out.println("UP");
                } else if ((keyStates&InputAdapter.KEY_DOWN)!=0) {
                    System.out.println("DOWN");
                } else if ((keyStates&InputAdapter.KEY_LEFT)!=0) {
                    System.out.println("LEFT");
                } else if ((keyStates&InputAdapter.KEY_RIGHT)!=0) {
                    System.out.println("RIGHT");
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
    	if (currTransition!=null) {
    		if (currTransition.doPaintOver())
    			return;
    	}
    	
    	topLeftMI.paintMenuSlice(-1);
    }

    public void updateScene(long elapsed) {
    	//Update the current transition
        if (currTransition != null) {
            if (currTransition.step()) {
                currTransition = null;
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
        
        //Set components
        topLeftMI = MetaMenu.topLeftMI;
    	topLeftMI.doHorizontalLayout(new Vector(), null, new Int(0));
    	topLeftMI.doVerticalLayout(new Vector(), null, new Int(0));
        
        //Set transitions
        currTransition = MetaMenu.menuInTrans;
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
