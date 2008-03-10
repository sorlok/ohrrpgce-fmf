/*
 * DoorParser.java
 * Created on January 16, 2007, 6:06 PM
 */

package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.RPG;

/**
 *
 * @author Seth N. Hetu
 */
public class DoorParser extends LumpParser {

    private static final int DOORS_PER_MAP = 100;
    
    public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
        System.out.println("Reading DOX: " + length);
        
        for (int map=0; map<result.getNumMaps(); map++) {
            int[][] temp = new int[DOORS_PER_MAP][3];
            for (int dr=0; dr<DOORS_PER_MAP; dr++)
                temp[dr] = new int[3];
            
            for (int i=0; i<3; i++) {
                for (int dr=0; dr<DOORS_PER_MAP; dr++) {
                    int val = readInt(input);
                 //yes we do   if (i==3) //We don't care about door activations
                     //   continue;
                    temp[dr][i] = val;
                    if (i==2)
                    	temp[dr][i] = temp[dr][i]&0x1; //Technically, the other bits are undefined
                }
            }
            
            result.getMap(map).tempLoadingData1 = temp;
            //result.getMap(map).mergeDoors();
        }
        
        return length - (DOORS_PER_MAP*3*2*result.getNumMaps());
    }
    
}
