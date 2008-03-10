package ohrrpgce.data.loader;

import java.io.IOException;
import java.io.InputStream;

import ohrrpgce.data.RPG;

public abstract class LumpParser {
	public static final long[] PDP_OFFSETS = {256*256, 256*256*256, 1, 256};
	protected static final char BSAVE_MAGIC_NUMBER = 0xFD;
	protected static final int MXS_SEGMENTS = 4;
	
	/**
	 * Read the contents of this lump & update the RPG file.
	 * @param result The RPG file to store the result in
	 * @param input The input stream containing the lump (RPG, or de-lumped). Assume that this stream is at the correct position to begin reading. Implementations SHOULD NOT set a mark on this. 
         * @param length The number of bytes available to be read. Some implementations (i.e., zipped format) may not use this.
         * @param hookUpdater For large lumps, it is often useful to offer update messages in segments. This adapter  helps.
         * @return The number of unread bytes (for skipping, if required)
	 */
	public abstract long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater);
	
	
	//Utility methods...
	public static String readFixedString(InputStream input, int nSizeBytes, int fixedSize) {
		return readFixedString(input, nSizeBytes, fixedSize, false);
	}
	
	public static String readFixedString(InputStream input, int nSizeBytes, int fixedSize, boolean skipAlternateBytes) {
		return readFixedString(input, nSizeBytes, fixedSize, skipAlternateBytes, false);
	}
	
	public static String readFixedString(InputStream input, int nSizeBytes, int fixedSize, boolean skipAlternateBytes, boolean skipFirstByte) {
		StringBuffer sb = new StringBuffer(fixedSize);
		
		try {
			//Get string size info
			int strSize = 0;
			int mult = 1;
			for (int i=0; i<nSizeBytes; i++) {
				strSize += (mult * readByte(input));
				mult *= 0x100;
			}
		
			//Is this reasonable?
			int availBytes = fixedSize;
			if (skipAlternateBytes)
				availBytes /= 2;
			if (strSize > availBytes) {
				input.skip(fixedSize-nSizeBytes);
				return "";
			}
			
			if (skipFirstByte)
				input.skip(1);
			
			//Read the string
			for (int i=0; i<strSize; i++) {
				sb.append(readByte(input));
				
				if (skipAlternateBytes)
					input.skip(1);
			}
			
			//Forget the remaining
			int mod = 1;
			int rem = 0;
			if (skipAlternateBytes)
				mod = 2;
			if (skipFirstByte)
				rem = 1;
			input.skip(fixedSize - strSize*mod - nSizeBytes - rem);
		} catch (IOException ex) {
			//Not sure what to do....
		}
		
		return sb.toString();
	}
	
	public static char readByte(InputStream input) {
		//byte[] c = new byte[]{'\0'};
		try {
			byte[] resA = new byte[1];
			input.read(resA);
			//input.read(b)
			int res = (0xFF&resA[0]);//input.read();
			//System.out.println(res + "-->" + (char)res);
			if (res!=-1) {
				return (char)res;
			}
		} catch (IOException iex) {
                } catch (NullPointerException ptx) {System.out.println("Error: " + ptx.toString());}
        
		
		return 0;  
	}
	
	public static byte[] readBytes(InputStream input, int len) {
		byte[] c = new byte[len];
		try {
			if (input.read(c)==len)
				return c;
		} catch (IOException iex) {}
		
		return null;
	}
        
        public static int convertTwosComplementInt(int val) {
            if ((val&0x8000)!=0) {
                //System.out.println("Two's complement detected: " + Integer.toHexString(tempVal));
                val ^= 0xFFFF;
                val = -val-1;
                //System.out.println("                 Becomes: " + tempVal);
            }
            return val;
        }
        
        public static int readTwosComplementInt(InputStream input) {
            return convertTwosComplementInt(readInt(input));
        }
	
	public static int readInt(InputStream input) {
		//Little endian
		return readByte(input) + 0x100*readByte(input);
	}
	
	public static String readNTString(InputStream input) {
		StringBuffer sb = new StringBuffer(16);
		for (char c=readByte(input); c!='\0'; c = readByte(input)) {
			sb.append((char)c);
		}
		
		return sb.toString();
	}
	
	public static long readPDPWord(InputStream input) {
		long currAmt = 0;
		
		for (int i=0; i<PDP_OFFSETS.length; i++) {
			currAmt += (PDP_OFFSETS[i] * readByte(input));
		}
		
		return currAmt;
	}
	
	public static int readBSaveLength(InputStream input) {
		return readBSaveLength(input, false);
	}
	
	public static int readBSaveLength(InputStream input, boolean skipMagicNumber) {
		//Read the magic number
		if (readByte(input) != BSAVE_MAGIC_NUMBER)
			throw new RuntimeException("-----BSAVE header error: expected magic number------");
		
		//Read the segment
		readInt(input);
		
		//Read the offset. Always 0
		int offset = readInt(input);
		if (offset!=0)
			System.out.println("-----Warning: BSAVE offset should be 0, not: " + offset + "------");
		
		//Read the length
		return readInt(input);
	}

}
