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
    private int hp;
    private int hpBonus;
    
    
    public ActiveHero(OHRRPG parent, Hero currHero, int posX, int posY) {
        super(parent);
        this.currHero = currHero;
        this.position = new int[] {posX, posY};
        this.speed = OHRRPG.getWalkSpeed(HERO_SPEED);
        this.nativeSpeed = speed;
        setGraphics(currHero.getWalkabout(), currHero.walkaboutPaletteID);
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
    public int getHP() {
        return hp;
    }
    public int getMaxHP() {
        return getLevelHP() + hpBonus;
    }
    public void setHP(int val) {
        hp = val;
    }
    public void boostMaxHP(int val) {
        hpBonus += val;
    }
    public int getLevelHP() {
        return getParent().figureHP(currHero.getStatMin(Hero.STAT_HP), currHero.getStatMax(Hero.STAT_HP), level);
    }
    
    


}
