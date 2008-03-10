package ohrrpgce.data.loader;

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStream;

import ohrrpgce.data.RPG;

public class PictureParser extends LumpParser {
	public static final int[] PT_HERO_SIZES = {32, 40, 8};
	public static final int[] PT_SMALL_ENEMY_SIZES = {34, 34, 1};
	public static final int[] PT_MED_ENEMY_SIZES = {50, 50, 1};
	public static final int[] PT_LARGE_ENEMY_SIZES = {80, 80, 1};
	public static final int[] PT_WALKABOUT_SIZES = {20, 20, 8};
	public static final int[] PT_WEAPON_SIZES = {24, 24, 2};
	public static final int[] PT_ATTACK_SIZES = {50, 50, 3};
	public static final int[][] PT_ALL = {PT_HERO_SIZES, PT_SMALL_ENEMY_SIZES, PT_MED_ENEMY_SIZES, PT_LARGE_ENEMY_SIZES, PT_WALKABOUT_SIZES, PT_WEAPON_SIZES, PT_ATTACK_SIZES};
	
	public static final int PT_HERO = 0;
	public static final int PT_SMALL_ENEMY = 1;
	public static final int PT_MEDIUM_ENEMY = 2;
	public static final int PT_LARGE_ENEMY = 3;
	public static final int PT_WALKABOUT = 4;
	public static final int PT_WEAPON = 5;
	public static final int PT_ATTACK = 6;
        
        public static int currPtLump=-1;
			
	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            //Precache
            switch (currPtLump) {
		case PT_HERO:
		case PT_SMALL_ENEMY:
		case PT_MEDIUM_ENEMY:
		case PT_LARGE_ENEMY:
			break;
		case PT_WALKABOUT:
                    long rem = length;
                    for (int i=0; i<result.getNumWalkabouts(); i++) {
                        if (result.getWalkaboutCacheSlotsFree()==0)
                            return rem;
                        
                        System.out.println("Pre-loading walkabout: " + i);
                        result.setWalkabout(i, readFrames(input, result, PT_WALKABOUT_SIZES[0], PT_WALKABOUT_SIZES[1], PT_WALKABOUT_SIZES[2], 0, true));
                        result.getWalkabout(i); //Prime the cache
                        
                        rem -= PT_WALKABOUT_SIZES[0]*PT_WALKABOUT_SIZES[1]*PT_WALKABOUT_SIZES[2]/2;
                    }

                    return 0;
		case PT_WEAPON:
		case PT_ATTACK:
			break;
		default:
			throw new RuntimeException("Invalid pt ID: " + currPtLump + " (probably, PictureParser.currPtLump was not initialized)");
            }
            return length;
	}
	
        public void loadSprite(RPG result, InputStream input, int ptID, int spriteID) {
            loadSprite(result, input, ptID, spriteID, true);
        }
        
	public void loadSprite(RPG result, InputStream input, int ptID, int spriteID, boolean doSkip) {
		switch (ptID) {
		case PT_HERO:
                        System.out.println("Re-reading hero battle-sprite data: " + spriteID);
                        result.setBattleSprite(spriteID, readFrames(input, result, PT_ALL[ptID][0], PT_ALL[ptID][1], PT_ALL[ptID][2], spriteID, doSkip));
			break;
		case PT_SMALL_ENEMY:
                        result.setSmallEnemySprite(spriteID, readFrames(input, result, PT_ALL[ptID][0], PT_ALL[ptID][1], PT_ALL[ptID][2], spriteID, doSkip));
			break;
		case PT_MEDIUM_ENEMY:
			result.setMediumEnemySprite(spriteID, readFrames(input, result, PT_ALL[ptID][0], PT_ALL[ptID][1], PT_ALL[ptID][2], spriteID, doSkip));
			break;
		case PT_LARGE_ENEMY:
			result.setLargeEnemySprite(spriteID, readFrames(input, result, PT_ALL[ptID][0], PT_ALL[ptID][1], PT_ALL[ptID][2], spriteID, doSkip));
			break;
		case PT_WALKABOUT:
                        System.out.println("Re-reading walkabout data: " + spriteID);
			result.setWalkabout(spriteID, readFrames(input, result, PT_ALL[ptID][0], PT_ALL[ptID][1], PT_ALL[ptID][2], spriteID, doSkip));
			break;
		case PT_WEAPON:
			readFrames(input, result, PT_ALL[ptID][0], PT_ALL[ptID][1], PT_ALL[ptID][2], spriteID, doSkip);
			break;
		case PT_ATTACK:
			readFrames(input, result, PT_ALL[ptID][0], PT_ALL[ptID][1], PT_ALL[ptID][2], spriteID, doSkip);
			break;
		default:
			throw new RuntimeException("Invalid pt ID: " + ptID);
		}
	}
        
       /* private int[][] readFrames(ByteStreamReader input, RPG result, int width, int height, int numFrames, int skip) {
            return readFrames(input, result, width, height, numFrames, skip, true);
        }*/
        
	private int[][] readFrames(InputStream input, RPG result, int width, int height, int numFrames, int skip, boolean doSkip) {
		//Get to the correct position
		try {
                    if (doSkip)
			input.skip(skip*width*height*numFrames/2);
		} catch (IOException ex) {
			throw new RuntimeException("Error skipping to picture: " + skip);
		}
	
		//Init
		int[][] res = new int[numFrames][width*height];
		for (int frame=0; frame<numFrames; frame++)
			res[frame] = new int[width*height];
		
		//Load
		int secondHalf = -1;
		for (int frame=0; frame<numFrames; frame++) {
			for (int x=0; x<width; x++) {
				for (int y=0; y<height; y++) {
					//Load the current pixel from our buffer, or from the stream
					int currColor = secondHalf;
					if (secondHalf == -1) {
						//In the specification, odd-numbered final bytes are ignored.
						if (x==width-1 && y==height-1)
							continue;
						
						//Read the next two colors, save one for later.
						secondHalf = readByte(input);
						currColor = (secondHalf&0xF0)/0x10;
						secondHalf &= 0xF;
					} else
						secondHalf = -1;
					
					//Interpret the pixel later; for now, just store the index.
					res[frame][y*width+x] = currColor;
				}
			}
		}
		
		return res;
	}

}
