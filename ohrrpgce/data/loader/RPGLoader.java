package ohrrpgce.data.loader;

import java.util.Vector;
import ohrrpgce.data.RPG;

public abstract class RPGLoader {

        private Vector sizeUpdates = new Vector();
        
        public void addSizeUpdateListener(RPGLoadSizeListener listen) {
            sizeUpdates.addElement(listen);
        }
        
        protected void updateSizeListeners(long bytesRead, String currLumpName) {
            for (int i=0; i<sizeUpdates.size(); i++) {
                ((RPGLoadSizeListener)sizeUpdates.elementAt(i)).moreBytesRead(bytesRead, currLumpName);
            }
        }
    
	/**
	 * Read initial properties of an RPG. Skips cached data.
	 * @param result The RPG file to store the results in.
	 */
	public abstract void lightParse(RPG result);
	
	
	/**
	 * Load a tileset (after the initial load of the RPG file)
	 * @param result The RPG file to store the results in.
	 * @param num The id of the tileset to load
	 */
	public abstract void loadTileset(RPG result, int num);
	
	
	/**
	 * Load a hero's walkabout graphics. 
	 * @param result The RPG file to store the results in.
	 * @param num The id of the hero whose walkabout graphics we should load
	 */
	public abstract void loadWalkabout(RPG result, int num);

        public abstract void loadBattleSprite(RPG result, int num);
        
        public abstract void loadSmallEnemySprite(RPG Result, int num);
        
        public abstract void loadMediumEnemySprite(RPG Result, int num);
        
        public abstract void loadLargeEnemySprite(RPG Result, int num);
        
        public abstract void loadMap(RPG result, int num);
        
        
        public abstract void loadTextBoxBlock(RPG result, int blockID);
        
}
