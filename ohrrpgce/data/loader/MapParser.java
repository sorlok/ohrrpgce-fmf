package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.Map;
import ohrrpgce.data.RPG;

public class MapParser extends LumpParser {
    
        public static final int[] TILE_ANIM_OFFSETS = {160, 208};

	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
		System.out.println("Reading MAP");

		for (int i=0; i<result.getNumMaps(); i++) {
                    readMap(input, i, result);
		}
                
                return 0;
	}
        
        public void readMap(InputStream input, int mapID, RPG result) {
            Map curr = result.getMap(mapID);
            curr.setTileset(readInt(input));
            curr.ambientMusic = readInt(input)-1;
            curr.minimapAvailable = (readInt(input)!=0);
            curr.saveAnywhere = (readInt(input)!=0);
            curr.nameDisplayTimer = readInt(input);
            curr.wraparoundMode = readInt(input);
            curr.defaultTile = readInt(input);
            curr.defaultPlotscriptTrigger = readInt(input);
            curr.scriptArgument = readInt(input);
            curr.harmTileDamage = readInt(input);
            curr.harmTileFlash = readInt(input);
            curr.footOffset = readInt(input);
            curr.afterBattlePlotscript = readInt(input);
            curr.insteadOfBattlePlotscript = readInt(input);
            curr.eachStepPlotscript = readInt(input);
            curr.onKeypressPlotscript = readInt(input);
            curr.drawHerosFirst = (readInt(input)==1);
            readInt(input);
            readInt(input);
            readInt(input);
        }


}
