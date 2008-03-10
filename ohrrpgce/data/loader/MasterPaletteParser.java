package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.RPG;

public class MasterPaletteParser extends LumpParser {
	private static final int MASTER_PAL_NUM_COLORS = 256;
	
	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
		System.out.println("MAS");
		
		//Bsave nonsense
		readBSaveLength(input);

		//Now, read pixel data (half-width, for some reason)
		int[] res = new int[MASTER_PAL_NUM_COLORS];
		/*byte[] reds = new byte[MASTER_PAL_NUM_COLORS];
		byte[] greens = new byte[MASTER_PAL_NUM_COLORS];
		byte[] blues = new byte[MASTER_PAL_NUM_COLORS];*/
		for (int i=0; i<MASTER_PAL_NUM_COLORS; i++) {
			res[i] =  scale(readInt(input))*0x10000 
					+ scale(readInt(input))*0x100 
					+ scale(readInt(input));
		}
		result.setMasterPalette(res);
		
		//There may be additional trash data, but we ignore it.
                return length - (7 + 2*3*MASTER_PAL_NUM_COLORS);
	}
	
	private int scale(int val63) {
		return val63*255/63;
	}

}
