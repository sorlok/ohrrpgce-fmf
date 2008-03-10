/*
 * Hero.java
 * Created on January 14, 2007, 6:37 PM
 */

package ohrrpgce.data;

/**
 * A hero is an amalgamation of several things- walkabout, battle sprite, 
 *    stats -through which the player can immerse himself in the game.
 * @author Seth N. Hetu
 */
public class Hero {
    public static final int STAT_HP = 0;
    public static final int STAT_MP = 1;
    public static final int STAT_STRENGTH = 2;
    public static final int STAT_ACCURACY = 3;
    public static final int STAT_DEFENSE = 4;
    public static final int STAT_DODGE_PERCENT = 5;
    public static final int STAT_MAGIC = 6;
    public static final int STAT_WILL = 7;
    public static final int STAT_SPEED = 8;
    public static final int STAT_COUNTER = 9;
    public static final int STAT_FOCUS = 10;
    public static final int STAT_EXTRA_HITS = 11;
    private static final int STAT_MAX = 12;
    
    public static final int SPELL_MP_BASED = 0;
    public static final int SPELL_FF_STYLE = 1;
    public static final int SPELL_RANDOM = 2;
    public static final int SPELL_RESERVED = 3;
    
    private RPG parent;
    
    public String name;
    private int walkaboutID;
    public int walkaboutPaletteID;
    public int defaultLevel;
    private int[][] stat_ranges;    //[stat_id][LV0,LV99]
    private int battleSpriteID;
    public int battleSpritePaletteID;
    private String[] spellNames;
    public int[] spellGroupTypes;
    public int[][] spells;
    public int[][] spellLearnLevels;
    
    //Not implemented yet
    //private int defaultWeapon;
    
    public Hero(RPG parent) {
        this.parent = parent;
        spellGroupTypes = new int[4];
        spells = new int[4][];
        spellLearnLevels = new int[4][];
        stat_ranges = new int[STAT_MAX][2];
        for (int i=0; i<STAT_MAX; i++)
            stat_ranges[i] = new int[2];
    }
    
    public RPG getParent() {
        return parent;
    }
    
    public void setWalkabout(int id) {
        this.walkaboutID = id;
    }
    
    public void setBattleSprite(int id) {
        this.battleSpriteID = id;
    }
    
    public Sprite getWalkabout() {
        return parent.getWalkabout(walkaboutID);
    }
    
    public Sprite getBattleSprite() {
        return parent.getBattleSprite(battleSpriteID);
    }

    public void setStatRange(int id, int min, int max) {
        stat_ranges[id][0] = min;
        stat_ranges[id][1] = max;
    }
    
    public int getStatMin(int id) {
        return stat_ranges[id][0];
    }
    public int getStatMax(int id) {
        return stat_ranges[id][1];
    }
    
    public String[] getSpellGroupNames() {
        return spellNames;
    }
    public void setSpellNames(String[] sn) {
        this.spellNames = sn;
    }
}
