package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.RPG;
import ohrrpgce.data.loader.LumpParser;

/**
 * Simple parser that reads map names.
 * @author Seth N. Hetu
 */
public class MapNameParser extends LumpParser {
	private static final int MAP_NAME_NUM_SIZE_BYTES = 1;
	private static final int MAP_NAME_RECORD_SIZE = 80;

	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
		System.out.println("Reading map names!");
		
		for (int i=0; i<result.getNumMaps(); i++) {
                    readMapName(input, i, result, false);
		}
                return 0;
	}
        
        
        public void readMapName(InputStream input, int mapID, RPG result, boolean isNullString) {
            String name = "";
            if (isNullString)
                name = readNTString(input);
            else
                name = readFixedString(input, MAP_NAME_NUM_SIZE_BYTES, MAP_NAME_RECORD_SIZE);
            result.getMap(mapID).mapName = name;
        }

}
