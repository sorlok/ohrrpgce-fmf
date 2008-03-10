/*
 * ModeXScreenParser.java
 * Created on January 19, 2007, 10:53 AM
 */

package ohrrpgce.data.loader;

import java.io.InputStream;
import java.io.IOException;
import ohrrpgce.data.RPG;


/**
 *
 * @author sethhetu
 */
public class ModeXScreenParser extends LumpParser {
    public static int DEBUG_MXS; //For now...
    
    public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
        System.out.println("MXS");
        hookUpdater.reset(DEBUG_MXS);
        for (int i=0; i<DEBUG_MXS; i++) {
            try {
                input.skip(64000);
                hookUpdater.segmentDone(i);
            } catch (IOException ex) {
                throw new RuntimeException("Error skipping MXS: " + ex.toString());
            }
        }
        return 0;
    }
}
