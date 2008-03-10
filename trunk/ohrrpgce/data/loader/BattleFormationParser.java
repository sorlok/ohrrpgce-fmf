/*
 * BattleFormationLoader.java
 * Created on July 26, 2007, 2:51 AM
 */

package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.BattleFormation;
import ohrrpgce.data.RPG;

/**
 * Code for loading battle formations.
 * @author Seth N. Hetu
 */
public class BattleFormationParser extends LumpParser {
        public static final int ENEMY_X_RANGE = 250;
        public static final int ENEMY_Y_RANGE = 199;
    
    	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            throw new RuntimeException("Old-style battle formation data not supported.");
        }
        
        public long readCompressedLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            for (int id=0; id<result.getNumFormations(); id++) {
                BattleFormation bForm = result.getFormation(id);
                bForm.background = readByte(input);
                bForm.battleMusic = readByte(input);
                bForm.bkgrdFrames = readByte(input);
                bForm.bkgrdSpeed = readInt(input);
                bForm.enemies = new int[readByte(input)];
                bForm.enemyXPos = new int[bForm.enemies.length];
                bForm.enemyYPos = new int[bForm.enemies.length];
                
                for (int i=0; i<bForm.enemies.length; i++) {
                    bForm.enemies[i] = readByte(input);
                    bForm.enemyXPos[i] = readByte(input);
                    bForm.enemyYPos[i] = readByte(input);
                }
            }
            
            return 0;
        }
    
}

