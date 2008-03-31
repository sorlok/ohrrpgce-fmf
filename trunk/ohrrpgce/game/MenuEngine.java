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
import ohrrpgce.menu.Transition;
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
    
    private static final int[] mainColors = new int[] {2, 3, 6, 5, 4, 7};
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
    
    //Menu control
    private MenuSlice topLeftMI;
    private boolean initDoneOnce;
    private int delayTimer;
    private boolean bufferedESC;
            
    private AdapterGenerator adaptGen;
    
    
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
    	//Temp
    	GraphicsAdapter.setColor(0x333333);
    	GraphicsAdapter.fillRect(0, 0, width, height);
    	
    	
    	topLeftMI.paintMenuSlice(-1);
    }

    public void updateScene(long elapsed) {

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
    }
    
    public RPG getRPG() {
        return ((GameEngine)midletHook.getCaller()).getRPG().getBaseRPG();
    }
    
    public OHRRPG getOHRRPG() {
        return ((GameEngine)midletHook.getCaller()).getRPG();
    }

    
    //HORRENDOUS setup routine:
    public void initMenu() {
    	topLeftMI = MetaMenu.buildMenu(width, height);
    	System.out.println("tester: 1");
    	topLeftMI.doHorizontalLayout(new Vector(), null, new Int(0));
    	System.out.println("tester: 2");
    	topLeftMI.doVerticalLayout(new Vector(), null, new Int(0));
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