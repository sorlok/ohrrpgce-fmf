/*
 * EncounterSetParser.java
 * Created on July 25, 2007, 12:16 AM
 */

package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.EncounterSet;
import ohrrpgce.data.RPG;

/**
 * EFX
 * @author Seth N. Hetu
 */
public class EncounterSetParser extends LumpParser {
    
    	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            throw new RuntimeException("Old-style encounter set data not supported.");
        }
        
        public long readCompressedLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            result.setNumEncounters(readByte(input));
            
            for (int id=0; id<result.getNumEncounters(); id++) {
                EncounterSet eSet = result.getEncounter(id);
                eSet.formations =  new int[readByte(input)];
                
                if (eSet.formations.length > 0)
                    eSet.likelihood = readByte(input);
                
                for (int i=0; i<eSet.formations.length; i++) {
                    eSet.formations[i] = readByte(input);
                }
            }
            
            return 0;
        }
        
}
