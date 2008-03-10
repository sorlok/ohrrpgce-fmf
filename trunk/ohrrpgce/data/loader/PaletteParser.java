package ohrrpgce.data.loader;

import java.io.InputStream;
import java.io.IOException;

import ohrrpgce.data.RPG;

public class PaletteParser extends LumpParser {

	public static final int NUM_COLORS = 16;
	private static final char[] PAL_HEADER_MAGIC_NUMBER = {0x5c, 0x11};
	private static final int PAL_HEADER_LEN = 16;
	
	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
		//Confusing!
		int newSize = resolvePaletteHeader(input);
              if (newSize!=-1)
                    result.setNumPalettes(newSize+1);
		
		//Palette info
        System.out.println("Num palettes: " + result.getNumPalettes());
		for (int i=0; i<result.getNumPalettes(); i++) {
			int[] palInfo = new int[NUM_COLORS];
			for (int pix=0; pix<NUM_COLORS; pix++) {
				palInfo[pix] = readByte(input);
			}
			result.setPalette(i, palInfo);
		}
                return 0;
	}

        //-1 = bsave, otherwise, new number of palettes
	private int resolvePaletteHeader(InputStream input) {
		char b = readByte(input);
		if (b == BSAVE_MAGIC_NUMBER) {
			readBSaveLength(input, true);
		} else if (b == PAL_HEADER_MAGIC_NUMBER[0] && readByte(input)==PAL_HEADER_MAGIC_NUMBER[1]) {
			//Read the rest of the header
			int lastPalette = readInt(input);
			try {
				input.skip(PAL_HEADER_LEN-4);
			} catch (IOException ex) {
				throw new RuntimeException("Error skipping remainder of header: " + ex.toString());
			}
                        return lastPalette;
		} else {
			throw new RuntimeException("-----PAL header error: expected magic number------");
		}
                return -1;
	}
}
