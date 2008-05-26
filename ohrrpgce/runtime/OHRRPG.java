/*
 * RPG.java
 * Created on January 8, 2007, 11:47 AM
 */

package ohrrpgce.runtime;

import java.util.Random;

import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.BattleFormation;
import ohrrpgce.data.BattlePrompt;
import ohrrpgce.data.Hero;
import ohrrpgce.data.Map;
import ohrrpgce.data.NPC;
import ohrrpgce.data.RPG;
import ohrrpgce.data.Message;
import ohrrpgce.data.Vehicle;
import ohrrpgce.data.loader.RPGLoadSizeListener;
import ohrrpgce.henceforth.VirtualMachine;
import ohrrpgce.menu.MenuSlice;

/**
 * Runtime RPG object.
 * @author Seth N. Hetu
 */
public class OHRRPG {
    private RPG baseRPG;
    
    private ActiveMap mapAnimator;
    private ActiveHero heroAnimator;
    private ActiveNPC talkingNPC;
    private ActiveNPC ridingNPC;

    private int currMap;
    private Message currTextBox;
    private BattlePrompt currBattleBox;
    private Vehicle currVehicle;
    private MenuSlice currQuitMenu;
    
    public Random rand;
    private RPGLoadSizeListener gcListener;
    
	//Each game maintains its own VM
	private VirtualMachine hvm;
    
    //NEEDS TO BE REPLACED
    private int currHero;
    //WITH
    private int[] currParty = new int[]{};
    
   // private boolean[] mapsLoaded;
    
    
    //Runtime data
    public boolean suspendedPlayer;
    public boolean suspendedBlockability;
    
    
    
    //Virtual Machine Stuff
    private int nextTxtBox;
    
    public static final int GAME_TICK_MS = 55; //'cause I keep forgetting. 
    public static int WALK_SPEED_UP = 0;
    private static final int[] ALLOWED_WALK_SPEEDS = {0, 1, 2, 4, 5, 10, 20};
    
    public static int getWalkSpeed(int defaultSpeed) {
        //First, clip the current walk speed to a valid one
        int loc;
        for (loc=0; loc<ALLOWED_WALK_SPEEDS.length; loc++) {
            if (ALLOWED_WALK_SPEEDS[loc] >= defaultSpeed )
                break;
        }
        
        //Now, speed up/slow down as required
        loc += WALK_SPEED_UP;
        if (loc >= ALLOWED_WALK_SPEEDS.length) {
            System.out.println("Warning! Wraparound of walking speed!");
            loc -= ALLOWED_WALK_SPEEDS.length;
        }
        
        return ALLOWED_WALK_SPEEDS[loc];
    }
    
        public int figureStat(int min, int max, int currLevel) {
            //For now...
            return (currLevel*(max-min))/99 + min;
        }
    
        public OHRRPG(RPGLoadSizeListener gcListener) {
            this.gcListener = gcListener;
    		hvm = new VirtualMachine(this);
    		nextTxtBox = -1;
    		
    		suspendedPlayer = false;
    		suspendedBlockability = false;
        }
        
    /*
     * Not sure if this is a good way of testing this... basically, we need this
     *  method to "draw" all cached data while the previous data's still onscreen.
     *  Thus; we overlay a "Loading" sign without clearing the screen first.
     */
    public void testCaches() {
        //Pull all cached data
        getCurrMap().getTileset();
    }

    public void teleportTo(int mapID, int posX, int posY) {
        System.out.println("Teleport to: " + mapID + ":" + posX + "," + posY);
        if (mapID == getCurrMapID()) {
            System.out.println("  NO NEED TO RELOAD MAP.");
        } else {
            setCurrMap(mapID);
            getActiveMap().startDisplayTimer();
        }
        setCurrHero(currHero, posX, posY);
        
    }
    
    
	/**
	 * Start a new instance of the given plotscript.
	 * @param srcID The ID of the plotscript.
	 */
	public void startPlotscriptExtern(int srcID) {
		hvm.startScript(srcID, true);
	}
	
    
     public void update(long elapsed) {
         int totalTicks = 1; //In case we need to migrate this into a parameter later (maybe for frameskip?)
         
         for (int ticks=0; ticks<totalTicks; ticks++) {
        	 //If we're showing the quit menu, don't update anything else
        	 if (currQuitMenu!=null)
        		 continue;
        	 
        	 //Update all scripts
        	 hvm.updateScripts();
        	 
             //Update all map animations
             mapAnimator.tick();
             
             //Update the hero
             heroAnimator.tick();
         }
     }
     
     public void stepHero(int direction) {
         boolean stepped = true;
         if (suspendedBlockability)
        	 getActiveHero().forceStep(direction);
         else
        	 stepped = getActiveHero().step(direction);
         
         if (currVehicle!=null) {
             if (stepped)
                ridingNPC.forceStep(direction);
             else //Turn him in that direction
                ridingNPC.turnTo(direction);
         }
     }
     
     public void rideVehicle(ActiveNPC caller, int vehicleID) {
         ridingNPC = caller;
         getActiveHero().forceStep(getActiveHero().getDirection());
         currVehicle = baseRPG.getVehicle(vehicleID);
         
         //Set speeds
         getActiveHero().setSpeed(getWalkSpeed(currVehicle.speed));
         ridingNPC.setSpeed(getWalkSpeed(currVehicle.speed));
         
         //Make the hero invisible, if need be
         if (!currVehicle.doNotHideLeader)
             getActiveHero().tempHide(true);
     }
     
     /**
      * @return true if dismounting happened; false otherwise
      */
     public boolean tryAndDismount() {
         //Figure out which tile we'll dismount on
     //    System.out.println("A");
         int fromX = ridingNPC.getTileX();
         int fromY = ridingNPC.getTileY();
         int toX = fromX;
         int toY = fromY;
       //  System.out.println("B");
         if(getCurrVehicle().dismountOneSpaceAhead) {
             switch(ridingNPC.getDirection()) {
                 case NPC.DIR_DOWN:
                     toY++;
                     break;
                 case NPC.DIR_UP:
                     toY--;
                     break;
                 case NPC.DIR_RIGHT:
                     toX++;
                     break;
                 case NPC.DIR_LEFT:
                     toX--;
                     break;
             }
         }
         
         //Actual dismount, if possible
         if (getCurrMap().canDismount(fromX, fromY, toX, toY, getCurrVehicle())) {
             //Change the game state
             getActiveHero().resetSpeed();
             ridingNPC.resetSpeed();
             if (!currVehicle.doNotHideLeader)
                getActiveHero().tempHide(false);
             
             System.out.println("Dismount");
             
             //In the case of "boat" vehicles
             if (getCurrVehicle().dismountOneSpaceAhead) {
                 getActiveHero().forceStep(getActiveHero().getDirection());
             }
             
             //Final game state change
             currVehicle = null;
             return true;
         }
         
         return false;
     }
     
      public void showTextBox(ActiveNPC caller, int id) {
          showTextBox(caller, id, true);
      }
      
      
      public void showBattle(BattleFormation bat) {
          talkingNPC = null; //Could be called later...
          currBattleBox = new BattlePrompt(this.getBaseRPG());
          currBattleBox.setEncounterData(bat);
      }
      
     
      /**
       * Show a text box, pausing the NPC who this text box is attached to.
       * @param caller The NPC which the hero just talked to.
       * @param id The id of the text box to show.
       * @param doPause If true, the caller is paused.
       */
     public void showTextBox(ActiveNPC caller, int id, boolean doPause) {
    	 System.out.println("Show text box: " + id + " of " + getBaseRPG().getNumTextBoxes());
    	 
         //Do we jump to another box instead?
         Message txtBox = baseRPG.getTextBox(id);
         if (txtBox.jumpToBox[0]!=0 && tagCheck(txtBox.jumpToBox[0])) {
             if (txtBox.jumpToBox[1] > 0) {
                 System.out.println("  Jump to: " + txtBox.jumpToBox[1]);
                 showTextBox(caller, txtBox.jumpToBox[1]);
             } else {
                 //TO ADD: Plotscript trigger here
                 System.out.println("Plotscript instead: " + -txtBox.jumpToBox[1]);
                 //startPlotscriptExtern(-txtBox.jumpToBox[1]);
             }
         } else {
             //Regular behavior
             if (doPause && caller!=null)
                caller.schedulePause();
            currTextBox = txtBox;
            currTextBox.loadBox();
            talkingNPC = caller;
         }
     }
     
     public void endTextBox() {
         //Change tags
         if (currTextBox.tagChange1[0]!=0 && tagCheck(currTextBox.tagChange1[0])) {
             setTag(currTextBox.tagChange1[1]);
             setTag(currTextBox.tagChange2[1]);
         }
         //Manipulate party
         if (currTextBox.heroAddRem[0]!=0 && tagCheck(currTextBox.heroAddRem[0])) {
             int val = currTextBox.heroAddRem[1];
             if (val>0)
                addHeroToParty(val);
             else
                 removeHeroFromParty(-val);
         }
         
          if (currTextBox.showBoxAfter[0]!=0 && tagCheck(currTextBox.showBoxAfter[0])) {
             int toShow = currTextBox.showBoxAfter[1];
             if (toShow<0) {
                 System.out.println("Plotscript: " + Math.abs(toShow));
                 currTextBox = null;
                 if (talkingNPC!=null)
                	 talkingNPC.unpause();
             } else 
                showTextBox(talkingNPC, toShow, false);
          } else {
            currTextBox = null;
            if (talkingNPC!=null)
            	talkingNPC.unpause();
          }
          
          //Update the VM
          hvm.textBoxClosed();
     }
     
     public void endBattlePrompt() {
         currBattleBox = null;
         if (talkingNPC!=null)
             talkingNPC.unpause();
     }
     
     public ImageAdapter getFontImage() {
         return baseRPG.font;
     }
     
     public boolean heroCanMove() {
         return currTextBox==null && currBattleBox==null && currQuitMenu==null && !suspendedPlayer;
     }
     
     /**
      * Check if a tag is in the required state
      * @param tagID The id of the tag to check. 0 implies no tag (pass)
      * @param checkON If true, check if this tag is on. Else, check if it's off.
      */
     public boolean tagCheck(int tagID, boolean checkON) {
       //  System.out.println("Tag check: " + tagID);
        if (tagID==0) 
            return true; //Not always the case!
        
        return baseRPG.tagOn(tagID)==checkON;
     }
    
     /**
      * Same as above, but ohrTag follows the <0 OFF, >0 ON rule
      */
     public boolean tagCheck(int ohrTag) {
        return tagCheck(Math.abs(ohrTag), ohrTag>0);
     }
     
     public void setTag(int ohrTag) {
        setTag(Math.abs(ohrTag), ohrTag>0);
     }
     
     public void setTag(int tagID, boolean setOn) {
         if (tagID==0 || tagID==1)
             return;
         baseRPG.setTag(tagID, setOn);
     }
     
    //Properties
    public void setBaseRPG(RPG baseRPG) { 
        this.baseRPG = baseRPG; 
        rand = new Random(System.currentTimeMillis());
        
        //Loaded, so...
        this.hvm.setNumBytecodes(baseRPG.getNumPlotscripts());
        
        /*mapsLoaded = new boolean[baseRPG.getNumMaps()];
        mapsLoaded[baseRPG.getStartingMap()] = true;*/
    }
    public RPG getBaseRPG() { return baseRPG; }
    public int getCurrMapID() { return currMap; }
    public Map getCurrMap() { return baseRPG.getMap(currMap); }
    public ActiveMap getActiveMap() { return mapAnimator; }
    public ActiveHero getActiveHero() { return heroAnimator; }
    public void setCurrMap(int id) {
        if (mapAnimator!=null) {
            mapAnimator.resetMap();
        }
        
        if (Runtime.getRuntime().freeMemory() < Runtime.getRuntime().totalMemory()/2) {
            System.out.println("Map forced GC");
            mapAnimator = null;
            getBaseRPG().freeMap(currMap);
            gcListener.callingGC();
            System.gc();
        }
        
        this.currMap = id; 
        mapAnimator = new ActiveMap(this, getCurrMap());
    }
    public void setCurrHero(int id, int startX, int startY) {
        currHero = id;
        heroAnimator = new ActiveHero(this, baseRPG.getHero(id), startX, startY);
        addHeroToParty(currHero);
    }
    public Hero getCurrHero() {
        return baseRPG.getHero(currHero);
    }
    public Message getCurrTextBox() {
        return currTextBox;
    }
    public BattlePrompt getCurrBattlePrompt() {
        return currBattleBox;
    }
    public boolean isRidingVehicle() {
        return currVehicle!=null;
    }
    public Vehicle getCurrVehicle() {
        return currVehicle;
    }
    public MenuSlice getCurrentQuitMenu() {
    	return currQuitMenu;
    }
    
    
    public Hero[] getHeroParty() {
        Hero[] currPartyOfHeroes = new Hero[currParty.length];
        for (int i=0; i<currParty.length; i++)
            currPartyOfHeroes[i] = getBaseRPG().getHero(currParty[i]);
        return currPartyOfHeroes;
    }
    
    public void addHeroToParty(int heroID) {
        int[] newArray = new int[currParty.length+1];
        for (int i=0; i<currParty.length; i++) {
            if (currParty[i]==heroID)
                return;
            newArray[i] = currParty[i];
        }
        
        newArray[currParty.length] = heroID;
        currParty = newArray;
    }
    
    public void removeHeroFromParty(int heroID) {
        int[] newArray = new int[currParty.length-1];
        int adder = 0;
        for (int i=0; i<currParty.length; i++) {
            if (currParty[i]==heroID) {
                adder = -1;
            } else {
                if (adder!=0 || i<currParty.length-1)
                    newArray[i+adder] = currParty[i];
                else
                    return; //The hero wasn't in the party.
            }
        }

        currParty = newArray;
    }
    
    
    public void showQuitMenu(AdapterGenerator adaptGen, int width) {
    	//Re-create each time; shouldn't really be that costly.
    	currQuitMenu = MetaMenu.createQuitMenu(adaptGen, this.baseRPG, width);
    }
    
    public void hideQuitMenu() {
    	currQuitMenu = null;
    }
    
    
    
    ///////////////////////////////
    ///////Virtual Machine Commands
    ///////////////////////////////
    
    /**
     * Queue text box; this will show on the next "wait" command
     */
    public void queueTextBox(int id) {
    	nextTxtBox = id;
    }
    
    public void initWait() {
    	if (nextTxtBox != -1) {
    		showTextBox(null, nextTxtBox);
    		nextTxtBox = -1;
    	}
    }
    
    public void walkerDone(int walkID) {
    	hvm.npcWalked(walkID);
    }
    
    
}
