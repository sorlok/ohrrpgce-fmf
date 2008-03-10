/*
 * Enemy.java
 * Created on July 29, 2007, 5:01 PM
 */

package ohrrpgce.data;

import ohrrpgce.data.loader.PictureParser;

/**
 * Holds enemy information
 * @author Seth N. Hetu
 */
public class Enemy {
    private RPG parent;
    
    public String name;
    private int spriteID;
    public int spritePaletteID;
    public int spriteSize;
    
    public int[] normalAttacks;
    public int[] desperAttacks;
    public int[] aloneAttacks;
    
    public Enemy(RPG parent) {
        this.parent = parent;
    }
    
    public void setSprite(int id) {
        this.spriteID = id;
    }
    
    public int getSizeInPix() {
        switch(spriteSize) {
            case 0:
                return PictureParser.PT_SMALL_ENEMY_SIZES[0];
            case 1:
                return PictureParser.PT_MED_ENEMY_SIZES[0];
            case 2:
                return PictureParser.PT_LARGE_ENEMY_SIZES[0];
            default:
                throw new RuntimeException("Invalid size: " + spriteSize);
        }
    }

    public Sprite getBattlePic() {
        switch(spriteSize) {
            case 0:
                return parent.getSmallEnemySprite(spriteID);
            case 1:
                return parent.getMediumEnemySprite(spriteID);
            case 2:
                return parent.getLargeEnemySprite(spriteID);
            default:
                throw new RuntimeException("Invalid size: " + spriteSize);
        }
    }
}


