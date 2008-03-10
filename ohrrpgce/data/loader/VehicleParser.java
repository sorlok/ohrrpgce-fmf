/*
 * VehicleLoader.java
 * Created on February 20, 2007, 9:23 AM
 */

package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.RPG;
import ohrrpgce.data.Vehicle;

/**
 *
 * @author Seth N. Hetu
 */
public class VehicleParser extends LumpParser {
    
    private static final int VEHICLE_NAME_BYTES = 16;
    
    public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
        for (int i=0; i<result.getNumVehicles(); i++) {
            Vehicle veh = result.getVehicle(i);
            veh.name = readFixedString(input, 1, VEHICLE_NAME_BYTES);
            int spd = readInt(input);
            if (spd==3)
                spd = 10;
            veh.speed = spd;
            int bits = readInt(input); //Bitsets
            readInt(input); //Bitsets
            veh.passThroughWalls =(bits&1)!=0;
            veh.passThroughNPCs =(bits&2)!=0;
            veh.enableNPCActivation =(bits&4)!=0;
            veh.enableDoorUse =(bits&8)!=0;
            veh.doNotHideLeader =(bits&16)!=0;
            veh.doNotHideParty =(bits&32)!=0;
            veh.dismountOneSpaceAhead =(bits&64)!=0;
            veh.passWallsWhileDismounting =(bits&128)!=0;
            veh.disableFlyingShadow =(bits&256)!=0;
            
            //Remaining data
            veh.randomBattleSet = readTwosComplementInt(input);
            veh.useButton = readTwosComplementInt(input);
            veh.menuButton = readTwosComplementInt(input);
            veh.ifRidingTag = readTwosComplementInt(input);
            veh.onMount = readTwosComplementInt(input);
            veh.onDismount = readTwosComplementInt(input);
            veh.overrideWalls = readInt(input);
            veh.blockedBy = readInt(input);
            veh.mountFrom = readInt(input);
            veh.dismountTo = readInt(input);
            veh.elevation = readInt(input);
            
            //Nulls
            for (int x=0; x<18; x++)
                readInt(input);
        }
        
        return 0;
    }
    
}






