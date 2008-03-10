package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.Door;
import ohrrpgce.data.Map;
import ohrrpgce.data.NPC;
import ohrrpgce.data.RPG;

public class MapDataParser extends LumpParser {
	private char typeID = '\0';
	private int mapID = 0;
        
        private static final int DOOR_LINKS_PER_MAP = 200;
	
	public void setTypeID(char id) {
		this.typeID = id;
	}
	
	public void setMapID(int id) {
		this.mapID = id;
	}
        
        
        public void parseNewDoorData(InputStream input, int mapID, RPG result, long recordSize) {
            int numDoors = (int)(recordSize/(7*2));
            Door[] res = new Door[numDoors];
            for (int i=0; i<numDoors; i++)
                res[i] = new Door();
            
            //Read data
            for (int prop=0; prop<7; prop++) {
                for (int doorID=0; doorID<numDoors; doorID++) {
                    int val = -1;
                    if (prop==5 || prop==6)
                        val = readTwosComplementInt(input);
                    else
                        val = readInt(input);
                    switch (prop) {
                        case 0:
                            res[doorID].posX = val;
                            break;
                        case 1:
                            res[doorID].posY = val;
                            break;
                        case 2:
                            res[doorID].gotoX = val;
                            break;
                        case 3:
                            res[doorID].gotoY = val;
                            break;
                        case 4:
                            res[doorID].gotoMap = val;
                            break;
                        case 5:
                            res[doorID].tagReq1 = val;
                            break;
                        case 6:
                            res[doorID].tagReq2 = val;
                            break;
                    }
                }
            }
            
            
            //No need to safe insert, but we'll do it anyways
            if (mapID >= result.getNumMaps())
                result.setNumMaps(mapID+1);
            result.getMap(mapID).doors = res;
        }

        
        public long readCompressedLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
  		switch (typeID) {
		case 'P':
                {
			//Safe insert
			if (mapID >= result.getNumMaps())
				result.setNumMaps(mapID+1);
                        
			//Read the passability map
			int width = result.getMap(mapID).getWidth();
			int height = result.getMap(mapID).getHeight();  
                        
                        if (width==-1 || height==-1) {
                            throw new RuntimeException("Cannot read passability data before reading tile data!");
                        }

			int[][] res = new int[height][width];
                        int CHECK = 1;
                        int nextByte = readByte(input);
                        int bitsLeft = 8;
			for (int y=0; y<height; y++) {
				res[y] = new int[width];
				for (int x=0; x<width; x++) {
                                    //Special case:
                                    if (length==0) {
                                        res[y][x] = 0;
                                        continue;
                                    }
                                    res[y][x] = nextByte&0x80;

                                    //Increment
                                    nextByte<<=1;
                                    bitsLeft--;
                                    if (bitsLeft==0 && (y!=height-1 || x!=width-1)) {
                                        nextByte = readByte(input);
                                        CHECK++;
                                       // System.out.println("UP");
                                        bitsLeft = 8;
                                        //    if ((CHECK >= length-2) && (bitsLeft==8))
                                       //          System.out.println("Last: " + Integer.toBinaryString(nextByte));
                                    }

                                    if (res[y][x]==0x80) {
                                        //Read 8 bits
                                        res[y][x] = 0;
                                        for (int i=7; i>=0; i--) {
                                           // System.out.println("[" + i + "]   " + Integer.toBinaryString(((nextByte&0x80)/0x80)*(1<<i)));
                                            res[y][x] |= ((nextByte&0x80)/0x80)*(1<<i);

                                            //Increment
                                            nextByte<<=1;
                                            bitsLeft--;
                                            if (bitsLeft==0 && (y!=height-1 || x!=width-1)) {
                                                nextByte = readByte(input);
                                             //   System.out.println("UP");
                                                CHECK++;
                                                bitsLeft = 8;
                                            }
                                        }
                                        
                                      //  System.out.println(Integer.toBinaryString(res[y][x]));
                                    }
                                    
                               //     System.out.print(Integer.toBinaryString(res[y][x]) + ",");
				}
                                //System.out.println("");
                                
			}
       
                        while (CHECK < length) {
                            System.out.println("Sanity check failed for \"P\" sub-lump! " + CHECK + "/" + length);
                            CHECK++;
                            readByte(input);
                        }
                    
			result.getMap(mapID).passability = res;
                        return 0;
                }
		case 'E':
                    //Safe insert
                    if (mapID >= result.getNumMaps())
			result.setNumMaps(mapID+1);
                    
                    //Read the foemap - dictionary
                    int[] dict = new int[readByte(input)];
                    for (int i=0; i<dict.length; i++) {
                        dict[i] = readByte(input)-1;
                      //  System.out.println("dict: " + dict[i]);
                    }
                    
                    //Prepare the foemap
                    int width = result.getMap(mapID).getWidth();
                    int height = result.getMap(mapID).getHeight();
                    int[][] res = new int[height][width];
                    int CHECK = dict.length + 1;
                    
                    if (dict.length==1) {
                        //Special case
                        for (int y=0; y<height; y++) {
                            res[y] = new int[width];
                            for (int x=0; x<width; x++) {
                                res[y][x] = dict[0];
                            }
                        }
                    } else {
                        //Read the foemap - entries
                        int nextByte = readByte(input);
                        CHECK++;
                        int bitsLeft = 8;
                        for (int y=0; y<height; y++) {
                            res[y] = new int[width];
                            for (int x=0; x<width; x++) {
                                // System.out.println(x + "/" + width + "  :  " + y + "/" + height);
                                
                                //Get the ID of this tile.
                                int currID = -1;
                                boolean currBit;
                                do {
                                    currID++;
                                    currBit = ((0x80&nextByte)!=0);
                                    nextByte <<= 1;
                                    bitsLeft--;
                                    if (bitsLeft==0 && CHECK<length) { //Don't read a new byte if we're at the end of the file.
                                        //   System.out.println("  BYTE");
                                        nextByte = readByte(input);
                                        //System.out.println(Integer.toBinaryString(nextByte) + " (" + Integer.toHexString(nextByte));
                                        CHECK++;
                                        bitsLeft = 8;
                                    }
                                } while (currBit);
                                
                                res[y][x] = dict[currID];
                            }
                        }
                    }
                    
                    while (CHECK < length) {
                        System.out.println("Sanity check failed for \"E\" sub-lump! " + CHECK + "/" + length);
                        CHECK++;
                        readByte(input);
                    }
                    
                    //Set the foemap
		    result.getMap(mapID).foemap = res;
                    return 0;
		case 'N':
                       Map currMap = result.getMap(mapID);
                       int npcNum = readByte(input);
                       currMap.setNumNPCs(npcNum);
                       
                       //Read each NPC
                       for (int i=0; i<npcNum; i++) {
                           int npcID = readByte(input);
                           int neededNPCCount = npcID-i+npcNum;
                           if (neededNPCCount>currMap.getNumNPCs()) {
                               System.out.println("WARNING: Null NPC's detected!");
                               currMap.setNumNPCs(neededNPCCount);
                           }
                           
                           //Set general properties
                           NPC guy = currMap.getNPC(npcID);
                           guy.setWalkabout(readInt(input)-1);
                           guy.walkaboutPaletteID = readInt(input);
                           guy.movePattern = readInt(input);
                           guy.speed = readInt(input);
                           guy.textBox = readInt(input);
                           readInt(input); //When activated action
                           readInt(input); //Given item #
                           int pushBits = readInt(input); //Pushability
                           switch (pushBits) {
                               case 0:
                                   guy.setPushability(false, false, false, false);
                                   break;
                               case 1:
                                   guy.setPushability(true, true, true, true);
                                   break;
                               case 2:
                                   guy.setPushability(false, false, true, true);
                                   break;
                               case 3:
                                   guy.setPushability(true, true, false, false);
                                   break;
                               case 4:
                                   guy.setPushability(true, false, false, false);
                                   break;
                               case 5:
                                   guy.setPushability(false, false, false, true);
                                   break;
                               case 6:
                                   guy.setPushability(false, true, false, false);
                                   break;
                               case 7:
                                   guy.setPushability(false, false, true, false);
                                   break;
                               default:
                                   throw new RuntimeException("Invalid pushability code: " + pushBits);
                           }
                           guy.activation = readInt(input);
                         /*  if (i==5) {
                               System.out.println("Message box for NPC 5: " + guy.textBox);
                           }*/
                           guy.appearTag1 = readTwosComplementInt(input);
                           guy.appearTag2 = readTwosComplementInt(input);
                           readInt(input); //Usable...
                           guy.plotscript = readInt(input);
                           readInt(input); //Script argument
                           guy.vehicleID = readInt(input)-1; //Vehicle #
                           
                           //Set instances (activations)
                           guy.instances = new int[readByte(input)][];
                           for (int q=0; q<guy.instances.length; q++) 
                               guy.instances[q] =  new int[] {readInt(input), readInt(input), readByte(input)};
                       }
                       
                       return 0;
		default:
			throw new RuntimeException("Invalid compressed type: " + typeID + "(" + (int)typeID + ")");
		}        
        }
        

	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
                int width=0;
                int height=0;
                int[][] res=null;
                
		switch (typeID) {
		case 'T':
			//Read tilemap
			readBSaveLength(input);
			width = readInt(input);
			height = readInt(input);
			res = new int[height][width];
                        
                        //Reading a whole series at once is faster
                        //  than reading each byte individually.
                        byte[] tempMap = readBytes(input, width*height);
			for (int y=0; y<height; y++) {
				res[y] = new int[width];
				for (int x=0; x<width; x++) {
					res[y][x] = 0xFF&tempMap[y*width+x];
				}
			}
                        tempMap = null;
			
			//Safe insert
			if (mapID >= result.getNumMaps())
				result.setNumMaps(mapID+1);
			result.getMap(mapID).tiles = res;
                        return 0;
		case 'P':
			//Read the passability map
			readBSaveLength(input);
			width = readInt(input);
			height = readInt(input);
			res = new int[height][width];
                        
                        //Reading a whole series at once is faster
                        //  than reading each byte individually.
                        tempMap = readBytes(input, width*height);
			for (int y=0; y<height; y++) {
				res[y] = new int[width];
				for (int x=0; x<width; x++) {
					res[y][x] = 0xFF&tempMap[y*width+x];
				}
			}
                        tempMap = null;
                    
			//Safe insert
			if (mapID >= result.getNumMaps())
				result.setNumMaps(mapID+1);
			result.getMap(mapID).passability = res;
                        return 0;
		case 'E':
			//Something with mapID
                    return length;
		case 'D':
                        //Read the door-link info
                        readBSaveLength(input);
                        int[][] temp = new int[DOOR_LINKS_PER_MAP][5];
                        for (int i=0; i<DOOR_LINKS_PER_MAP; i++) 
                            temp[i] = new int[5];
                        
                        for (int p=0; p<5; p++) {
                            for (int i=0; i<DOOR_LINKS_PER_MAP; i++)
                                temp[i][p] = readInt(input);
                        }
                    
                        result.getMap(mapID).tempLoadingData2 = temp;
                       // result.getMap(mapID).mergeDoors();
                        return 0;
		case 'L':
                    throw new RuntimeException("ERROR: Old lump reading of NPCs not supported...");
		case 'N':
                    throw new RuntimeException("ERROR: Old lump reading of NPCs not supported...");
		default:
			throw new RuntimeException("Invalid type: " + typeID + "(" + (int)typeID + ")");
		}
	}

}
