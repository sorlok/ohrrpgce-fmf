package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.RPG;

/**
 * An extension of the LumpParser class that simply ignores all input.
 * @author sethhetu
 */
public class IgnoreParser extends LumpParser {

	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            return length;
        }

}
