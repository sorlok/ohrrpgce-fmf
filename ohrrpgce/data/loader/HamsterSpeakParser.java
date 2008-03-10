/*
 * HamsterSpeakParser.java
 * Created on January 19, 2007, 11:23 AM
 */

package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.RPG;

/**
 *
 * @author sethhetu
 */
public class HamsterSpeakParser extends LumpParser {

    
    public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
        System.out.println("HSP");
        return length;
    }

}
