/*
 * ActiveHero.java
 * Created on February 8, 2007, 10:01 PM
 */

package ohrrpgce.runtime;

import ohrrpgce.data.Hero;
import ohrrpgce.data.NPC;

/**
 *
 * @author Seth N. Hetu
 */
public class ActiveHero extends ActiveWalker {
    private Hero currHero;
    private static final int HERO_SPEED = 4; //Wiki:"The player moves 4 pixels each step. Only values that divide evenly into 20 are available."
    
    private int level = 0;
    
    //[Hero.stat_id][curr, max_natural, curr_bonuses, (later:battle_bonuses)]
    //For hp/mp, this is easy. For attack, max_natural==curr, and curr_bonuses is from magical potions, etc.
    private int[][] curr_stats;
    
    
    public ActiveHero(OHRRPG parent, Hero currHero, int posX, int posY) {
        super(parent);
        this.currHero = currHero;
        this.position = new int[] {posX, posY};
        this.speed = OHRRPG.getWalkSpeed(HERO_SPEED);
        this.nativeSpeed = speed;
        setGraphics(currHero.getWalkabout(), currHero.walkaboutPaletteID);
        
        //Current stats
        curr_stats = new int[Hero.STAT_MAX][4];
        for (int i=0; i<Hero.STAT_MAX; i++) {
        	curr_stats[i] = new int[4];
        	curr_stats[i][0] = getParent().figureStat(currHero.getStatMin(i), currHero.getStatMax(i), level);
        	curr_stats[i][1] = curr_stats[i][0];
        	curr_stats[i][2] = 0;
        	curr_stats[i][3] = 0;
        }
    }    
    

    protected void stepNearlyDone() {
        //Track npcs that are stepped on
        ActiveNPC firstNPC = getParent().getActiveMap().getFirstNPCAt(getTileX(), getTileY());
        if (firstNPC!=null && !firstNPC.activateByTalk()) {
            firstNPC.activateScript();
        }
    }
    
    //Hero maintenance issues
    protected void stepDone(boolean moved) {
        //Track npcs that are stepped on
        ActiveNPC firstNPC = getParent().getActiveMap().getFirstNPCAt(getTileX(), getTileY());
        if (firstNPC!=null && !firstNPC.activateByTalk()) {
            firstNPC.heroActivated();
        }
    }

    /**
     * Interact with the tile in front of you.
     */
    public void interact() {
        int toX = getTileX();
        int toY = getTileY();
        if (getDirection()==NPC.DIR_UP)
            toY--;
        else if (getDirection()==NPC.DIR_RIGHT)
            toX++;
        else if (getDirection()==NPC.DIR_LEFT)
            toX--;
        else if (getDirection()==NPC.DIR_DOWN)
            toY++;
        
        ActiveNPC firstNPC = getParent().getActiveMap().getFirstNPCAt(toX, toY);
        if (firstNPC!=null) {
            firstNPC.heroActivated();
        }
        
    }
    
    
    protected boolean performStep(ActiveNPC blockingNPC, ActiveHero currHero, int[] toTile) {
        if (blockingNPC==null || !blockingNPC.blocks(false)) {
            return true;
        } else {
            //Push the NPC?
            if (blockingNPC!=null)
                blockingNPC.push(getDirection());
            return false;
        }
    }
    
    
    protected boolean isHero() {
        return true;
    }
    
    public int getStatCurrVal(int id) {
        return curr_stats[id][0];
    }
    
    public int getStatMaxValue(int id, boolean withBonuses) {
    	if (withBonuses)
    		return curr_stats[id][1];
    	else
    		return curr_stats[id][1] + curr_stats[id][2] + curr_stats[id][3];
    }
    
    public void setStatCurrValue(int id, int val) {
    	curr_stats[id][0] = val;
    }
    
    public void boostStatCurrValue(int id, int amt) {
    	curr_stats[id][0] += amt;
    }
    
    


}
