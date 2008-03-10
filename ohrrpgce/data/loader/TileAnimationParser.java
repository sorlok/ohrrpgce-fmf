/*
 * TileAnimationParser.java
 * Created on January 12, 2007, 2:34 PM
 */

package ohrrpgce.data.loader;

import java.io.InputStream;
import java.io.IOException;

import ohrrpgce.data.RPG;
import ohrrpgce.data.TileAnimation;
import ohrrpgce.data.Tileset;

/**
 *
 * @author sethhetu
 */
public class TileAnimationParser extends LumpParser {
    public static final int NUM_ANIM_COMMANDS = 9;
    
    public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
        System.out.println("Reading tileset animations");
        

        
        //HACK: Tile animations cut out at the last one EDITED; there may be
        //     less than one for each map. So, for the un-lumped reader,
        //     we need to actually check if we've reached the end of the file.
        byte[] nextInt = new byte[2];
        int tsID=0;
        
        try {
            while (input.read(nextInt)!=0 && tsID<result.getNumTilesets()) {
              //  System.out.println("TS: " + tsID );
              //  System.out.println("Val: " + (int)nextInt[0] + ":" + (int)nextInt[1]);
                Tileset ts = result.getTileset(tsID, false);
                for (int i=0; i<MapParser.TILE_ANIM_OFFSETS.length; i++) {
                    TileAnimation ta = new TileAnimation();
                    int val = nextInt[0] + 0x100*nextInt[1];
                    if (i!=0)
                        val = readInt(input);
                    ta.startTileOffset = val;
                    ta.disableTagID = readInt(input);
                    ta.actionCommands = new int[NUM_ANIM_COMMANDS];
                    ta.actionValues = new int[NUM_ANIM_COMMANDS];

                    for (int x=0; x<NUM_ANIM_COMMANDS; x++)
                        ta.actionCommands[x] = readInt(input);
                    for (int x=0; x<NUM_ANIM_COMMANDS; x++)
                        ta.actionValues[x] = readInt(input);

                    ts.setTileAnimation(i, ta);
                }

                tsID++;
                
                length -= (NUM_ANIM_COMMANDS*2+2)*MapParser.TILE_ANIM_OFFSETS.length*2;
                if (length==0) {
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Error reading tile animations: " + ex.toString());
        }
        
        //Output if we stopped early.
        if (tsID < result.getNumTilesets()-1) {
            System.out.println("WARNING: tileset data ends at " + tsID + " with no indication in GEN.");
        }
        
        /*
        for (int tsID=0; tsID<result.getNumTilesets(); tsID++) {
            Tileset ts = result.getTileset(tsID, false);
            for (int i=0; i<MapParser.TILE_ANIM_OFFSETS.length; i++) {
                TileAnimation ta = new TileAnimation();
                ta.startTileOffset = readInt(input);
                ta.disableTagID = readInt(input);
                ta.actionCommands = new int[NUM_ANIM_COMMANDS];
                ta.actionValues = new int[NUM_ANIM_COMMANDS];
                
                for (int x=0; x<NUM_ANIM_COMMANDS; x++)
                    ta.actionCommands[x] = readInt(input);
                for (int x=0; x<NUM_ANIM_COMMANDS; x++)
                    ta.actionValues[x] = readInt(input);
                
                ts.setTileAnimation(i, ta);
            }
            
            length -= (NUM_ANIM_COMMANDS*2+2)*MapParser.TILE_ANIM_OFFSETS.length*2;
            if (length==0) {
                System.out.println("WARNING: tileset data ends at " + tsID + " with no indication in GEN.");
                break;
            }
        }*/
        
        return 0;
    }
    
}
