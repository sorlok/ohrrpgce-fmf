/*
 * ActiveMap.java
 * Created on January 14, 2007, 1:51 PM
 */

package ohrrpgce.runtime;

import ohrrpgce.data.BattleFormation;
import ohrrpgce.data.Door;
import ohrrpgce.data.EncounterSet;
import ohrrpgce.data.Map;
import ohrrpgce.data.NPC;
import ohrrpgce.data.TileAnimation;
import ohrrpgce.data.loader.LinkedList;
import ohrrpgce.data.loader.MapParser;
import ohrrpgce.data.loader.TileAnimationParser;
import ohrrpgce.game.SimpleCanvas;
import ohrrpgce.game.LiteException;
import ohrrpgce.game.SimpleTextBox;
import ohrrpgce.menu.MenuSlice;

/**
 * Class for holding a map that's currently being displayed onscreen.
 * @author Seth N. Hetu
 */
public class ActiveMap {
    private Map currMap;
    private int[][] currMapAnimTiles;  //int[y, x] = ANIM_ID;
    private int[][] mapAnimDisplacements; //int[ANIM_ID][dX, dY], where dX and dY is the distance the tile animation has traveled so far.
    private int[] mapAnimTimers; //int[ANIM_ID], time remaining.
    private int[] mapAnimCommands; //int[ANIM_ID], curr anim command.
    private int mapNameTimer;
    public SimpleTextBox mapNameBox; //[width, height][data]
    
    private OHRRPG parent;
    private ActiveNPC[] npcs;
    //private RPGLoadSizeListener gcListener;
    
    private LinkedList[][] doors;  //[y][x][doorIDX->doorIDX-1, ..., ->door1], (stored in reverse order of activation)
    
    public ActiveMap(OHRRPG parent, Map currMap) {
        this.parent = parent;
        this.currMap = currMap;
      //  this.gcListener = gcListener;
        mapAnimDisplacements = new int[TileAnimationParser.NUM_ANIM_COMMANDS][2];
        
        setMap();
    }
    
    
    /**
     * Updates the state of the animation for a game tick.
     */
    public void tick() {
        for (int animID=0; animID<mapAnimTimers.length; animID++) {
            //Decrease the timer, if necessary
            if (mapAnimTimers[animID]>0)
                mapAnimTimers[animID]--;
            
            //Update, if applicable
            int sanityCheck = 0;
            TileAnimation currAnim = currMap.getTileset(false).getTileAnimation(animID);
            while (mapAnimTimers[animID]==0) {
                int val = currAnim.actionValues[mapAnimCommands[animID]];
                //Execute this command
                switch (currAnim.actionCommands[mapAnimCommands[animID]]) {
                    case TileAnimation.CMD_END_ANIM:
                        mapAnimCommands[animID]=-1; //So it increments to 0
                        resetAllTiles(animID);
                        break;
                    case TileAnimation.CMD_MOVE_DOWN:
                        moveAllTiles(animID, 0, val);
                        break;
                    case TileAnimation.CMD_MOVE_UP:
                        moveAllTiles(animID, 0, -val);
                        break;
                    case TileAnimation.CMD_MOVE_RIGHT:
                        moveAllTiles(animID, val, 0);
                        break;
                    case TileAnimation.CMD_MOVE_LEFT:
                        moveAllTiles(animID, -val, 0);
                        break;
                    case TileAnimation.CMD_PAUSE_ANIM:
                        mapAnimTimers[animID] = val;
                        break;
                    case TileAnimation.CMD_CONDITIONAL:
                        if (val!=0) {
                            if(currMap.getParent().tagOn(Math.abs(val))==val>0) {
                                mapAnimCommands[animID]=-1; //So it increments to 0
                                resetAllTiles(animID);
                            }
                        }
                        break;
                    default:
                        System.out.println("Error! Invalid animation imperative: " + currAnim.actionCommands[mapAnimCommands[animID]]);
                }
                
                mapAnimCommands[animID]++;
                sanityCheck++;
                
                //Wraparound - shouldn't ever be reached, but better to avoid the error.
                if (mapAnimCommands[animID] >= currAnim.actionCommands.length) {
                    //Reset the animation
                     mapAnimCommands[animID] = 0;
                     resetAllTiles(animID);
                }
                
                if (sanityCheck>TileAnimationParser.NUM_ANIM_COMMANDS) {
                    //We've looped around
                    System.out.println("WARNING: Tile animation loops forever");
                    break;
                }
            }
        }
        
        //Tick NPCs
        for (int i=0; i<getNPCs().length; i++) {
            if (getNPCs()[i].isVisible()) {
            	boolean stepsBefore = getNPCs()[i].hasStepsInBuffer(); 
                getNPCs()[i].tick();
                if (stepsBefore && !getNPCs()[i].hasStepsInBuffer())
                	parent.walkerDone(i);
            }
        }
        
        //Tick name display
        if (mapNameTimer>0)
            mapNameTimer--;
    }

    public boolean hasDoor(int tileX, int tileY) {
        return doors[tileY][tileX] != null;
    }
    
    public LinkedList doorsAt(int tileX, int tileY) {
        return doors[tileY][tileX];
    }
    
    public boolean isOverhead(int tileX, int tileY) {
        return (currMap.passability[tileY][tileX]&Map.PASS_OVERHEAD) != 0;
    }
    
    public void startDisplayTimer() {
        mapNameTimer = currMap.nameDisplayTimer;
    }
    
    public boolean isMapNameShowing() {
        return mapNameTimer>0;
    }
            
    
    /**
     * Occurs when a hero steps onto a tile.
     */
    public void tileTouched(int tileX, int tileY) {
        if (hasDoor(tileX, tileY)) {
            LinkedList.LinkedNode traverse = doorsAt(tileX, tileY).getLastNode();
            while (traverse!=null) {
                Door currDoor = currMap.doors[((Integer)traverse.data).intValue()];
                System.out.println("Curr Door (" + currDoor.tagReq1 + ")(" + currDoor.tagReq2 + ")");
                if (parent.tagCheck(currDoor.tagReq1) && parent.tagCheck(currDoor.tagReq2)) {
                    //Teleport
                    parent.teleportTo(currDoor.gotoMap, currDoor.gotoX, currDoor.gotoY);
                    break;
                }
                
                traverse = traverse.prev;
            }
        } else {
            //If there's no door, maybe there's a formation set (i.e., fight)
            int foe = currMap.foemap[tileY][tileX];
            try {
                 if (foe>=0) {
                     EncounterSet es = parent.getBaseRPG().getEncounter(foe);
                     if (parent.rand.nextInt(100) < es.likelihood) {
                         int encounterID = es.formations[parent.rand.nextInt(es.formations.length)];
                         BattleFormation battle = parent.getBaseRPG().getFormation(encounterID);
                         parent.showBattle(battle);
                         System.out.println("Fighting: " + encounterID + "  (" + battle.enemies.length + " enemies)");
                         for (int i=0; i<battle.enemies.length; i++) {
                             System.out.println("   >" + parent.getBaseRPG().getEnemy(battle.enemies[i]).name);
                         }
                     }
                 }
            } catch (Exception ex) {
                throw new RuntimeException("Error starting fight.");
            }
        }
    }
    
    /**
     * Returns the tile id for a given map tile, calculating animations as well.
     * @param tileX, tileY The tile's co-ordinates
     * @return The int[] of pixel values.
     */
    public int tileAt(int tileX, int tileY) {
    	try {
	        if (currMapAnimTiles[tileY][tileX]==-1)
	            return currMap.tileAt(tileX, tileY, new int[]{0, 0});
	        //System.out.println("Anim tile: " + tileX + "," + tileY + "  disp: " + mapAnimDisplacements[currMapAnimTiles[tileY][tileX]][0] + "," + mapAnimDisplacements[currMapAnimTiles[tileY][tileX]][1]);
	        //System.out.println("    Returns: " + currMap.tileAt(tileX, tileY, mapAnimDisplacements[currMapAnimTiles[tileY][tileX]]));
	        return currMap.tileAt(tileX, tileY, mapAnimDisplacements[currMapAnimTiles[tileY][tileX]]);
    	} catch (NullPointerException npex) {
    		String err = "";
    		if (currMap == null)
    			err = "Curr Map is null";
    		else if (currMapAnimTiles == null)
    			err = "Curr Map Anim Tiles is null";
    		throw new LiteException(this, npex, err);
    	}
    }
    
    /**
     * Set each map tile back to its composite value.
     */
    public void resetMap() {
        //Reset animations
        for (int y=0; y<currMap.getHeight(); y++) {
            for (int x=0; x<currMap.getWidth(); x++) {
                int animSet = currMapAnimTiles[y][x];
                if (animSet==-1)
                    continue;
            
                //Put the tile back to its original value.
                int newID = currMap.getTileID(x, y) - currMap.getTileset(false).getTileAnimation(animSet).startTileOffset + MapParser.TILE_ANIM_OFFSETS[animSet];
                currMap.setTileID(x, y, newID);
            }
        }
        
    }

    
    //Set each animated map tile to its "actual" (first tile in the anim) value.
    // We will transcribe to the actual animation frame at paint-time.
    private void setMap() {
        //Prepare map name box
        if (currMap.nameDisplayTimer>0)
            mapNameBox = new SimpleTextBox(" " + currMap.mapName + " ", parent.getBaseRPG().font, parent.getBaseRPG().getTextBoxColors(0)[1], parent.getBaseRPG().getTextBoxColors(0)[0], true, MenuSlice.FILL_SOLID);
        
        //Prepare animations
        currMapAnimTiles =  new int[currMap.getHeight()][currMap.getWidth()];
        mapAnimTimers = new int[MapParser.TILE_ANIM_OFFSETS.length];
        mapAnimCommands = new int[MapParser.TILE_ANIM_OFFSETS.length];
        for (int animID = MapParser.TILE_ANIM_OFFSETS.length-1; animID>=0; animID--) {
            int thresh = MapParser.TILE_ANIM_OFFSETS[animID];
            mapAnimDisplacements[animID] = new int[]{0, 0};
            for (int tY=0; tY<currMap.getHeight(); tY++) {
                currMapAnimTiles[tY] = new int[currMap.getWidth()];
                for (int tX=0; tX<currMap.getWidth(); tX++) {
                    if (currMap.getTileID(tX, tY) >= thresh) {
                        currMapAnimTiles[tY][tX] = animID;
                        TileAnimation tAnim = currMap.getTileset(false).getTileAnimation(animID);
                        if (tAnim==null) {
                            System.out.println("ERROR! Map with no animation pattern tries to use animating tiles...");
                            continue;
                        }
                        int newID = currMap.getTileID(tX, tY)-thresh + tAnim.startTileOffset;
                        currMap.setTileID(tX, tY, newID);
                    } else
                        currMapAnimTiles[tY][tX] = -1;
                }
            }
        }
        
        
        //Prepare doors shorthand
        doors =  new LinkedList[currMap.getHeight()][currMap.getWidth()];
        for (int y=0; y<currMap.getHeight(); y++)
            doors[y] = new LinkedList[currMap.getWidth()];
        
        for (int i=0; i<currMap.doors.length; i++) {
            Door currD = currMap.doors[i];
            
            //Not sure...
            if (currD.posX>=currMap.getWidth() || currD.posY>=currMap.getHeight()) {
                System.out.println("Warning: Door for map " + currMap.mapName + " is out of bounds");
                continue;
            }
            
            if (doors[currD.posY][currD.posX]==null)
                doors[currD.posY][currD.posX] = new LinkedList(Integer.MAX_VALUE);
            doors[currD.posY][currD.posX].insertIntoFront(new Integer(i));
        }

        
        //Prepare NPCs
        npcs = new ActiveNPC[currMap.getNumNPCInstances()];
        int pos = 0;
        for (int i=0; i<currMap.getNumNPCs(); i++) {
            NPC guy = currMap.getNPC(i);
            if (guy.instances==null)
                continue;
            for (int j=0; j<guy.instances.length; j++)
                npcs[pos++] = new ActiveNPC(parent, guy, guy.instances[j][0], guy.instances[j][1], guy.instances[j][2]);
        }
    }
    
    
    //Helper method
    private void moveAllTiles(int animID, int dX, int dY) {
        //Allow us to translate only those tiles that need to be painted.
        mapAnimDisplacements[animID][0] += dX;
        mapAnimDisplacements[animID][1] += dY;
    }
    
    //Helper method
    private void resetAllTiles(int animID) {
        mapAnimDisplacements[animID][0] = 0;
        mapAnimDisplacements[animID][1] = 0;
    }
    
    public ActiveNPC[] getNPCs() {
        return npcs;
    }
    
    public ActiveNPC getFirstNPCAt(int tileX, int tileY) {
        for (int i=0; i<getNPCs().length; i++) {
        //    System.out.println("NPC: " + i);
            if (getNPCs()[i].isVisible() && getNPCs()[i].getTileX()==tileX && getNPCs()[i].getTileY()==tileY)
                return getNPCs()[i];
        }
        return null;
    }
    
}
