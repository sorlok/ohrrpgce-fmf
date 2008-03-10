package ohrrpgce.data.loader;

import java.io.InputStream;
import java.io.IOException;

import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.data.RPG;

/**
 * This class will load an XRPG file, saved with more sensible considerations.
 * Please see: {@link RPG2XRPG.java} for documentation of the XRPG format
 *
 * @author Seth N. Hetu
 */
public class SensifiedLoader extends RPGLoader {
        private AdapterGenerator adaptGen;
        //private FileAdapter loadAdapter;
        private String internalName;
        
        //Needs constant updating....
        private long totalBytes;
        private long bytesSeg; 
        private int onSeg;
        
        public SensifiedLoader(String internalName, AdapterGenerator adapter, long totalBytes) {
            this.adaptGen = adapter;
           // this.loadAdapter = adaptGen.createFileAdapter();
            this.internalName = internalName;
            this.totalBytes = totalBytes;
        }
    
    
	public void lightParse(RPG result) {
            onSeg = 0;
            
            //These lumps really should be first.
            updateSizeListeners(0, "GEN");
            new GenParser().readLump(result, adaptGen.getLump(internalName+".GEN"), 0, null);
            bytesSeg = totalBytes/13;
            updateSizeListeners((++onSeg)*bytesSeg, "MAS");
            new MasterPaletteParser().readLump(result, adaptGen.getLump(internalName+".MAS"), 0, null);
            
            //The order of the rest are less important
            updateSizeListeners((++onSeg)*bytesSeg, "PAL");
            new PaletteParser().readLump(result, adaptGen.getLump(internalName+".PAL"), 0, null);
            updateSizeListeners((++onSeg)*bytesSeg, "TAP");
            new TileAnimationParser().readLump(result, adaptGen.getLump(internalName+".TAP"), -1, null);
            updateSizeListeners((++onSeg)*bytesSeg, "VEH");
            new VehicleParser().readLump(result, adaptGen.getLump(internalName+".VEH"), -1, null);
            updateSizeListeners((++onSeg)*bytesSeg, "HRO");
            new HeroDataParser().readCompressedLump(result, adaptGen.getLump(internalName+".HRO"), -1, null);
            updateSizeListeners((++onSeg)*bytesSeg, "FOE");
            new EnemyDataParser().readCompressedLump(result, adaptGen.getLump(internalName+".FOE"), -1, null);
            updateSizeListeners((++onSeg)*bytesSeg, "ATK");
            new AttackDataParser().readCompressedLump(result, adaptGen.getLump(internalName+".ATK"), -1, null);
            updateSizeListeners((++onSeg)*bytesSeg, "EFX");
            new EncounterSetParser().readCompressedLump(result, adaptGen.getLump(internalName+".EFX"), -1, null);
            updateSizeListeners((++onSeg)*bytesSeg, "BFF");
            new BattleFormationParser().readCompressedLump(result, adaptGen.getLump(internalName+".BFF"), -1, null);
            updateSizeListeners((++onSeg)*bytesSeg, "FNT");
            try {
                ((RPG)result).font = adaptGen.createImageAdapter(adaptGen.getLump(internalName+"_FNT.PNG"));
            } catch (IOException ex2) {
                throw new RuntimeException("Error loading font: " + ex2.getMessage());
            }
            updateSizeListeners((++onSeg)*bytesSeg, "SEZ");
            new TextBoxParser().readCompressedLump(result, adaptGen.getLump(internalName+".SEZ"), 0, -1, null);
            
            //Now, read all maps
            updateSizeListeners((++onSeg)*bytesSeg, "MAP (" + result.getStartingMap() +")");
            loadMap(result, result.getStartingMap());
            updateSizeListeners(totalBytes, "<DONE>");
	}
        
	public void loadTileset(RPG result, int num) {
           new TilesetParser().readTileset(result, adaptGen.getLump(internalName+"_TIL_"+num+".PNG"), adaptGen, num);
	}

	public void loadWalkabout(RPG result, int num) {
            new PictureParser().loadSprite(result, adaptGen.getLump(internalName+".PT4_"+num), PictureParser.PT_WALKABOUT, num, false);
	}
        

        public void loadBattleSprite(RPG result, int num) {
            new PictureParser().loadSprite(result, adaptGen.getLump(internalName+".PT0_"+num), PictureParser.PT_HERO, num, false);
        }
        
        public void loadSmallEnemySprite(RPG result, int num) {
            new PictureParser().loadSprite(result, adaptGen.getLump(internalName+".PT1_"+num), PictureParser.PT_SMALL_ENEMY, num, false);
        }
        
        public void loadMediumEnemySprite(RPG result, int num) {
            new PictureParser().loadSprite(result, adaptGen.getLump(internalName+".PT2_"+num), PictureParser.PT_MEDIUM_ENEMY, num, false);
        }
        
        public void loadLargeEnemySprite(RPG result, int num) {
            new PictureParser().loadSprite(result, adaptGen.getLump(internalName+".PT3_"+num), PictureParser.PT_LARGE_ENEMY, num, false);
        }

        private String pad(int i) {
            if (i<10)
                return "00" + i;
            if (i<100)
                return "0" + i;
            return ""+i;
        }

    
        public void loadTextBoxBlock(RPG result, int blockID) {
            String suffix = ".SEZ";
            if (blockID>0)
                suffix = "_" + blockID + suffix;
            InputStream blockLump = adaptGen.getLump(internalName+suffix);
            result.initTextBoxBlock(blockID);
            
            System.out.println("Loading block: " + suffix);
            
            new TextBoxParser().readCompressedLump(result, blockLump, blockID, -1, null);
        }
        

        
        public void loadMap(RPG result, int val) {
        	InputStream mapLump = adaptGen.getLump(pad(val)+".MAP");
                result.initMap(val);
                
                //Read each sub-lump
                long currLumpSize = 0;
                MapDataParser dataParser = new MapDataParser();
                dataParser.setMapID(val);
                for(;;) {
                    //Read name and size. Stop if null.
                    String lumpName = LumpParser.readNTString(mapLump);
                    if (lumpName.length()==0) {
                        System.out.println("  + Read map[" + val + "]");
                        break;
		    }
                    currLumpSize = LumpParser.readPDPWord(mapLump);
                    long rem = 0;
                    
                    //Do something with this lump
                    if (lumpName.equals(".MN")) {    
                        new MapNameParser().readMapName(mapLump, val, result, true);
                    } else if (lumpName.equals(".MD")) {
                        new MapParser().readMap(mapLump, val, result);
                    } else if (lumpName.equals(".T")) {
                        dataParser.setTypeID('T');
                        rem = dataParser.readLump(result, mapLump, currLumpSize, null);
                    } else if (lumpName.equals(".P") || lumpName.equals(".N") || lumpName.equals(".E")) {
                        dataParser.setTypeID(lumpName.charAt(lumpName.length()-1));
                        rem = dataParser.readCompressedLump(result, mapLump, currLumpSize, null);
                    } else if (lumpName.equals(".D")) {
                        dataParser.parseNewDoorData(mapLump, val, result, currLumpSize);
                    } else {
                        System.out.println("Invalid lump name (" + lumpName.length() + ")");
                        System.out.println(lumpName);
                        break;
                    }
                    
                    //Skip any additional data
                    if (rem > 0) {
                        try {
                            mapLump.skip(rem);
                        } catch (IOException ex) {
                            System.out.println("Error skipping map data: " + ex.toString());
                        }
                    }
                }
            
        }

}




