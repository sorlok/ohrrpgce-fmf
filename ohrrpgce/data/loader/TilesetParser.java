package ohrrpgce.data.loader;


import java.io.IOException;
import java.io.InputStream;

import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.RPG;
import ohrrpgce.game.LiteException;

public class TilesetParser extends LumpParser {
	public static final int TILE_SIZE = 20;
	public static final int TILE_ROWS = 10;
	public static final int TILE_COLS = 16;
	
	//Does nothing (cached!)
	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            if (result.getMasterPalette()==null) {
                System.out.println("Warning: cannot pre-cache tilesets (MAS hasn't been read yet).");
                hookUpdater.reset(result.getNumTilesets()); 
                for (int i=0; i<result.getNumTilesets(); i++) {
                  //  System.out.println("Skipping: " + (TILE_SIZE*TILE_ROWS * TILE_SIZE*TILE_COLS));
                    try {
                        input.skip(TILE_SIZE*TILE_ROWS * TILE_SIZE*TILE_COLS);
                    } catch (IOException ex) {
                        throw new RuntimeException("Error skipping tileset: " + i + ": " + ex.toString());
                    }
                    hookUpdater.segmentDone(i);
                }
                return 0;
            }
            
            System.out.println("ERROR! Pre-caching not implemented!");
            return length;
        }
        


    /*    public void readTileset(RPG result, FileInputStream input, int id) {
                System.out.println("Re-reading tileset data: " + id);
                readTileset(result, input, id, true);
        }*/
        
        
        public void readTileset(RPG result, InputStream input, AdapterGenerator adaptGen, int id) {
            ImageAdapter img = null;
            try {
                img = adaptGen.createImageAdapter(input);
            } catch (Exception ex) {
                System.out.println("Error on tileset resource: " + id);
                return;
            }
            
            //For now, hack the data into tiles our system can handle
          /*  int[][] res = new int[img.getWidth()/TILE_SIZE*img.getHeight()/TILE_SIZE][];
            for (int tY=0; tY<img.getHeight()/TILE_SIZE; tY++) {
                for (int tX=0; tX<img.getWidth()/TILE_SIZE; tX++) {
                    int[] rgb = new int[TILE_SIZE*TILE_SIZE];
                    img.getRGB(rgb, 0, TILE_SIZE, tX*TILE_SIZE, tY*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    res[tY*img.getWidth()/TILE_SIZE + tX] = rgb;
                }
            }*/
            
            try {
                result.setTilesetData(id, img);
            } catch (ClassCastException ex) {
                throw new RuntimeException("Error: expected class RPG, not RPG_Core");
            }
        }
        
/*	public void readTileset(RPG result, FileInputStream input, int id, boolean skipToID) {
		//Skip to the start of this tileset.
                if (skipToID) {
                    try {
                            input.skip(TILE_SIZE*TILE_ROWS * TILE_SIZE*TILE_COLS * id);
                    } catch (IOException ex) {
                            System.out.println("Error restarting to tileset: " + ex.toString());
                    }
                }
		
		//We R lazy c0ders
		if (TILE_SIZE%4 != 0) 
			throw new RuntimeException("Error reading tileset! Code assumes tiles are %4==0 pixels wide/high. Re-coding is needed!");
		
                
		//Read the tiles
		int[][] res = new int[TILE_ROWS*TILE_COLS][TILE_SIZE*TILE_SIZE];
		for (int mxOffY=0; mxOffY<MXS_SEGMENTS; mxOffY++) {
			for (int row=0; row<TILE_ROWS; row++) {
				//Init
				if (mxOffY==0) {
					for (int col=0; col<TILE_COLS; col++) {
						res[row*TILE_COLS + col] = new int[TILE_SIZE*TILE_SIZE];
					}
				}
				
				//Debug - just copy the picture.
//				for (int yPos=0; yPos<TILE_SIZE; yPos++) {
//					for (int col=0; col<TILE_COLS; col++) {
//						for (int pix=0; pix<TILE_SIZE; pix++) {
//							int val = result.getMasterPalette()[readByte(input)];
//							res[row*TILE_COLS + col][yPos*TILE_SIZE + pix] = val;
//						}
//					}
//				}
				
				//This gets messy
				for (int yPos=0; yPos<TILE_SIZE; yPos+=MXS_SEGMENTS) {
					for (int mxOffX=0; mxOffX<MXS_SEGMENTS; mxOffX++) {
						for (int col=0; col<TILE_COLS; col++) {
							for (int pix=0; pix<TILE_SIZE; pix+=MXS_SEGMENTS) {
								int val = result.getMasterPalette()[readByte(input)];
								res[row*TILE_COLS + col][(yPos+mxOffY)*TILE_SIZE + pix + mxOffX] = val;
							}
						}
					}
				}
			}
		}
		
                try {
                    
                
		//For some reason, I'm doing the math wrong....
		for (int tileID=0; tileID<TILE_COLS*TILE_ROWS; tileID++) {
			boolean[] done = new boolean[TILE_SIZE*TILE_SIZE];
			for (int cellY=0; cellY<TILE_SIZE; cellY++) {
				for (int cellX=0; cellX<TILE_SIZE; cellX++) {
					int offX = (cellX%MXS_SEGMENTS);
					int offY = (cellY%MXS_SEGMENTS);
					int newOffX = offY - offX;
					int newOffY = -offY + offX;
					
					int currID = cellY*TILE_SIZE+cellX;
					int betterID = (cellY+newOffY)*TILE_SIZE + cellX+newOffX;
					if (done[betterID])
						continue;
					done[currID] = true;
					
					int hold = res[tileID][currID];
					res[tileID][currID] = res[tileID][betterID];
					res[tileID][betterID] = hold;
				}
			}
		}
		
                } catch (Throwable ex) {
                    System.out.println("Error: " + ex.toString());
                }
                
		result.setTilesetData(id, res);
		//result.getTileset(id).tsData = res;
	}*/
}
