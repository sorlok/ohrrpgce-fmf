package ohrrpgce.data;

import ohrrpgce.data.loader.MapDataParser;

public class Map {
	//Constants
	public static final int EDGE_MODE_CROP = 0;
	public static final int EDGE_MODE_WRAP = 1;
	public static final int EDGE_MODE_DEFAULT_TILE = 2;
        
        //Constants
        public static final int PASS_BLOCK_NORTH = 1;
        public static final int PASS_BLOCK_EAST = 2;
        public static final int PASS_BLOCK_SOUTH = 4;
        public static final int PASS_BLOCK_WEST = 8;
        public static final int PASS_VEHICLE_A = 16;
        public static final int PASS_VEHICLE_B = 32;
        public static final int PASS_HARM = 64;
        public static final int PASS_OVERHEAD = 128;
	
	//Parent RPG file
	private RPG parent;
	
	//Data contained within
	private int tilesetID;
	public int[][] tiles;
        public Door[] doors;
        private NPC[] npcs;
        public int[][] passability;
        public int[][] foemap;
        public int[][] tempLoadingData1; //[x,y,active? of doors]
        public int[][] tempLoadingData2; //[src, dest, destMap, cond1, cond2 of links]
	
	//Simple data types
	public String mapName;
	public int nameDisplayTimer;
	public int wraparoundMode;
	public int defaultTile;
	public int harmTileDamage;
	public int harmTileFlash;
	public int footOffset;
	
	//Flags
	public boolean minimapAvailable;
	public boolean saveAnywhere;
	public boolean drawHerosFirst;
	
	//Needs objects
	public int ambientMusic;
	public int defaultPlotscriptTrigger;
	public int scriptArgument;
	public int afterBattlePlotscript;
	public int insteadOfBattlePlotscript;
	public int eachStepPlotscript;
	public int onKeypressPlotscript;
	
	//WIPs
	protected int loadND;
	protected int loadTP;
        
        //Extras
        private int numInstances;

	public Map(RPG parent) {
		this.parent = parent;
                this.mapName = "";
                this.numInstances = -1;
	}
        
        public int getNumNPCs() {
            if (npcs==null)
                return -1;
            return npcs.length;
        }
        
        /**
         * Returns the (helper) value which marks how many instances
         *  of an NPC there are. This includes all NPCs at all positions
         */
        public int getNumNPCInstances() {
            if (numInstances == -1) {
                //Recalc
                numInstances = 0;
                for (int i=0; i<getNumNPCs(); i++) {
                    if (getNPC(i).instances!=null)
                        numInstances += getNPC(i).instances.length;
                }
            }
            
            return numInstances;
        }
        
        public NPC getNPC(int id) {
            return npcs[id];
        }
        
        public void setNumNPCs(int num) {
		NPC[] res = new NPC[num];
		int count = 0;
		//Copy old flag names
		if (npcs != null) {
			for (; count<npcs.length && count<num; count++) {
				res[count] = npcs[count];
			}
		}
		
		//Init new flag names
		for (; count<num; count++) {
			res[count] =  new NPC(parent);
		}
		
		npcs = res;
        }
	
	public void setTileset(Tileset ts) {
		//Clear the cache
		//...is this needed?
		
		//Set it
		this.tilesetID = ts.id;
	}
	public void setTileset(int id) {
		//Clear the cache
		//...is this needed?
		
		//Set it
		this.tilesetID = id;
	}
        
        public RPG getParent() {
            return parent;
        }
        
	public Tileset getTileset(boolean loadIfNull) {
		return parent.getTileset(tilesetID, loadIfNull);
	}
	public Tileset getTileset() {
		return getTileset(true);
	}
	
	public int getHeight() {
		if (tiles==null)
			return -1;
		return tiles.length;
	}
        
        
        public boolean canMove(int fromX, int fromY, int toX, int toY) {
        	//Is this in bounds?
        	if ( fromX<0 || fromX>=getWidth()  || toX<0 || toX>=getWidth()
        	  || fromY<0 || fromY>=getHeight() || toY<0 || toY>=getHeight())
        		return false;
        	
           //We'll be lazy
            boolean can = true; //Don't change this! (see "default" case)
            if (toX-fromX==1) {
                //Moving right
                if (((passability[fromY][fromX]&PASS_BLOCK_EAST)!=0) || ((passability[toY][toX]&PASS_BLOCK_WEST)!=0))
                    can =false;
            } else if (fromX-toX==1) {
                //Moving left
                if (((passability[fromY][fromX]&PASS_BLOCK_WEST)!=0) || ((passability[toY][toX]&PASS_BLOCK_EAST)!=0))
                    can =false;
            } else if (toY-fromY==1) {
                //Moving down
                if (((passability[fromY][fromX]&PASS_BLOCK_SOUTH)!=0) || ((passability[toY][toX]&PASS_BLOCK_NORTH)!=0))
                    can =false;
            } else if (fromY-toY==1) {
                //Moving up
                if (((passability[fromY][fromX]&PASS_BLOCK_NORTH)!=0) || ((passability[toY][toX]&PASS_BLOCK_SOUTH)!=0))
                    can =false;
            } //else, we are moving from/to the same tile, so it's ok.

            return can;
        }
        
        public boolean canDismount(int fromX, int fromY, int toX, int toY, Vehicle currVehicle) {
            switch (currVehicle.dismountTo) {
                case Vehicle.PASS_ANY:
                    return true;
                case Vehicle.PASS_DEFAULT:
                    return canMove(fromX, fromY, toX, toY);
                case Vehicle.PASS_A:
                    return (passability[toY][toX]&PASS_VEHICLE_A) !=0;
                case Vehicle.PASS_B:
                    return (passability[toY][toX]&PASS_VEHICLE_B) !=0;
                case Vehicle.PASS_A_AND_B:
                    return (passability[toY][toX]&PASS_VEHICLE_A)!=0 && (passability[toY][toX]&PASS_VEHICLE_B)!=0;
                case Vehicle.PASS_A_OR_B:
                    return (passability[toY][toX]&PASS_VEHICLE_A)!=0 || (passability[toY][toX]&PASS_VEHICLE_B)!=0;
                case Vehicle.PASS_NOT_A:
                    return (passability[toY][toX]&PASS_VEHICLE_A) ==0;
                case Vehicle.PASS_NOT_B:
                    return (passability[toY][toX]&PASS_VEHICLE_B) ==0;
                case Vehicle.PASS_NOT_A_AND_NOT_B:
                    return (passability[toY][toX]&PASS_VEHICLE_A) ==0 && (passability[toY][toX]&PASS_VEHICLE_B) ==0;
                default:
                    throw new RuntimeException("Error, invalid passability: " + currVehicle.dismountTo);
            }
        }
        
	
        /**
         * Returns the width of the map, in Tiles
         */
	public int getWidth() {
		if (tiles==null || tiles[0]==null)
			return -1;
		return tiles[0].length;
	}
        
        /**
         * Returns the ID of the tile at the given offsets
         * NOTE: Currently returns 0 for animating tiles
         */
        public int tileAt(int x, int y, int[] tileOffsets) {
            if (getTileID(x, y)>=MapDataParser.PDP_OFFSETS[0]) {
                System.out.println("Error! Animating tiles weren't properly pre-parsed.");
                return 0;
            }
            
            int tileID = Tileset.moveTile(tiles[y][x], tileOffsets[0], tileOffsets[1]);
            return tileID;
           // return getTileset().tsData[tileID];
        }
        
        public int getTileID(int x, int y) {
            return tiles[y][x];
        }
        
        public void setTileID(int x, int y, int id) {
            tiles[y][x] = id;
        }
        
        public void mergeDoors() {
            if (tempLoadingData1==null || tempLoadingData2==null) //Do we have all the data?
                return;
            
            
            
            //Get an accurate count of the number of REAL links. (Note: I feel this is faster than using a Vector)
            int count =0;
            boolean foundNullRow = false;
            for (int linkID=0; linkID<tempLoadingData2.length; linkID++) {
                int[] row = tempLoadingData2[linkID];
                if (row[0]==0 && row[1]==0 && row[2]==0 && row[3]==0 && row[4]==0) {
                    if (foundNullRow)
                        continue;
                    else
                        foundNullRow = true;
                }
                
                //Check src/dest doors
                if (tempLoadingData1[row[0]][2]==0 || parent.getMap(row[2]).tempLoadingData1[row[1]][2]==0)
                    continue;
                
                count++;
            }
 
            doors = new Door[count]; 
            int currDoor = 0;
            
            foundNullRow = false;
            for (int linkID=0; linkID<tempLoadingData2.length; linkID++) {
                int[] row = tempLoadingData2[linkID];
                if (row[0]==0 && row[1]==0 && row[2]==0 && row[3]==0 && row[4]==0) {
                    if (foundNullRow)
                        continue;
                    else
                        foundNullRow = true;
                }

                //Check src/dest doors
                if (tempLoadingData1[row[0]][2]==0 || parent.getMap(row[2]).tempLoadingData1[row[1]][2]==0)
                    continue;
                
                
                Door nDr = new Door();
                nDr.posX = tempLoadingData1[row[0]][0];
                nDr.posY = tempLoadingData1[row[0]][1];
                nDr.gotoMap = row[2];
                nDr.gotoX = parent.getMap(row[2]).tempLoadingData1[row[1]][0];
                nDr.gotoY = parent.getMap(row[2]).tempLoadingData1[row[1]][1];
                nDr.tagReq1 = row[3];
                nDr.tagReq2 = row[4];
                doors[currDoor++] = nDr;
            }
        }
	
}
