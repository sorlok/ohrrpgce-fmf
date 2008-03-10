package ohrrpgce.data.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.data.RPG;

public class LumpLoader extends RPGLoader{
	/*private static final String[] lumpMatchers = {"ARCHINYM", "BROWSE", "BINSIZE", "DEFPASS", "ATTACK", "SONGDATA", "PLOTSCR", "GEN", "VEH", "TIL", "MXS", "HSP", "MN", "DOX", "TAP", "TMN", "MAS", "FNT", "PAL", "DOR", "SAY", "STT", "FOR", "EFS", "ITM", "MAP", "SNG", "SHO", "STF"};
	private static final LumpParser[] lumpActions = {new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new GenParser(), new IgnoreParser(), new TilesetParser(), new ModeXScreenParser(), new HamsterSpeakParser(), new MapNameParser(), new DoorParser(), new TileAnimationParser(), new IgnoreParser(), new MasterPaletteParser(), new IgnoreParser(), new PaletteParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser(), new MapParser(), new IgnoreParser(), new IgnoreParser(), new IgnoreParser()};
	private static final boolean[] requiresGen = {false, false, false, false, false, false, false, false, false, true, true, true, false, true, true, false, false, true, false, false, false, false, false, false, true, false, false, false};
	private static final LumpParser[] dtActions = {new HeroDataParser(), new EnemyDataParser(), new AttackDataParser()};
	private static PictureParser pictAction = new PictureParser();
	private static BAMParser bamAction =  new BAMParser();
	private static MapDataParser mapAction = new MapDataParser();
	//private String rpgResource;
	private int tlOff;
	
	//private FileAdapter rpgReLoader;

	//Caching
	private long tilesetOffset;
	private long[] ptOffsets;
*/
	//In case GEN isn't first...
	private Vector pendingGen;
	private AdapterGenerator adaptGen;
        
        //Also
        //private String lastReportedName="";
        private HookbackListener currHook;

	private boolean isGenRead(RPG item) {
		return (item.getPasscode() != null);
	}

	public LumpLoader(AdapterGenerator adaptGen) {
		this.adaptGen = adaptGen;
		pendingGen = new Vector();
	}


	public void lightParse(RPG result) {
/*		FileInputStream input = rpgReLoader.getRPGFile();
		long currLumpStart = 0;
		long lastLumpSize = 0;
		pendingGen.removeAllElements();
		ptOffsets = new long[PictureParser.PT_ALL.length];
                long lastLumpStart=0;
                updateSizeListeners(0, "");
		for(;;) {
			//Read the next lump's name. All lumps must have a name of length >0

                        //Inform listeners (if any)
                        currLumpStart += lastLumpSize;
			String lumpName = LumpParser.readNTString(input);
			if (lumpName.length()==0) {
                                updateSizeListeners(currLumpStart, "<DONE>");
				System.out.println("-------Successful termination.--------");
                                postProcess(result);
                                System.out.println("--------Post-processing done----------");
				break;
			}
                        lastLumpStart = currLumpStart;
                        updateSizeListeners(lastLumpStart, lumpName);

			//Get information about this lump
			currLumpStart += lumpName.length()+1;
			currLumpStart += LumpParser.PDP_OFFSETS.length;
			lastLumpSize = LumpParser.readPDPWord(input);
                        currHook = new HookbackListener(lastLumpSize, lastLumpStart, this, lumpName);


			//Hack the name into two bits
                     //   System.out.println(lumpName.length());
                       // System.out.println("  :  " + lastLumpSize);
			int dotPos = lumpName.indexOf('.');
			if (dotPos==-1) {
				//Error reading string...
                                StringBuffer sb = new StringBuffer();
				sb.append("Error: expected lump name, not [");
				for (int i=0; i<lumpName.length(); i++) {
                                        if (i>500)
                                            break;
					if (i!=0)
						sb.append(", ");
					sb.append("0x");
					if (lumpName.charAt(i)<0x10)
						sb.append("0");
					sb.append(Integer.toHexString(lumpName.charAt(i)));
                                        sb.append("("+lumpName.charAt(i)+")");
				}
				sb.append("]");
                                throw new RuntimeException(sb.toString());
			}
			String lumpExtension = lumpName.substring(dotPos+1);
			lumpName = lumpName.substring(0, dotPos);

			//Now, split on the name
			int lastDigit = lumpExtension.charAt(lumpExtension.length()-1);
			if (Character.isDigit((char)lastDigit)) {
				//It's either a PT#, DT#,  D##, L##, N##, T##, P##, E##, ##, or it's
				// a prefixed version of one of these.
				if (lumpExtension.charAt(0)=='P' && lumpExtension.charAt(1)=='T') {
					//It's the PT lump
					int id = Character.digit((char)lastDigit, 10);
					ptOffsets[id] = currLumpStart;
                                        PictureParser.currPtLump = Character.digit((char)lastDigit, 10);
					//pictAction.setPictureID(Character.getNumericValue(lastDigit));
					input = markAndRead(result, input, pictAction, currLumpStart, lastLumpSize);
				} else if (lumpExtension.charAt(0)=='D' && lumpExtension.charAt(1)=='T') {
					//It's the DT lump
                                        if (!isGenRead(result)) {
                                            System.out.println("  + Saving for later... ");
                                            pendingGen.addElement(new LumpStarter(currLumpStart, lastLumpSize, dtActions[lastDigit%4]));
                                            try {
                                                input.skip(lastLumpSize);
                                            } catch (IOException ex) {
                                                throw new RuntimeException("Error skipping: DT" + lastDigit%4);
                                            }
                                        } else {
                                            input = markAndRead(result, input, dtActions[lastDigit%4], currLumpStart, lastLumpSize);
                                        }
                                } else if (Character.isDigit(lumpExtension.charAt(0)) && lumpName.length()>1) {
					//It's a song
					bamAction.setSongID(Character.digit((char)lumpExtension.charAt(0), 10)*10 +  Character.digit((char)lastDigit, 10));
					input = markAndRead(result, input, bamAction, currLumpStart, lastLumpSize);
				} else {
					//It's one of the suite of files needed to open a map
					char ext = lumpExtension.charAt(0);
					int id = 0;
					int cutoff = 1;
					if (Character.isDigit(ext)) {
						//An RPG with over 100 maps? Really?
						ext = lumpName.charAt(0);
						cutoff = 0;
					}

					//Get the ID
					int mult = 1;
					for (int i=lumpExtension.length()-1; i>=cutoff; i--) {
						id += mult *  Character.digit((char)lumpExtension.charAt(i), 10);
						mult *= 10;
					}

					//Read it
					mapAction.setTypeID(ext);
					mapAction.setMapID(id);
					input = markAndRead(result, input, mapAction, currLumpStart, lastLumpSize);
				}
			} else {
				String search = null;
				if (lumpExtension.equals("LMP") || lumpExtension.equals("TXT") || lumpExtension.equals("BIN") || lumpExtension.equals("LST")) {
					//Disambiguate on the prefix
					search = lumpName;
				} else {
					//Disambiguate on the suffix
					search = lumpExtension;
				}

				//Search for a proper action to perform
				boolean found = false;
				for (int i=0; i<lumpMatchers.length; i++) {
					if (lumpMatchers[i].equals(search)) {
						//Save?
						if (search.equals("TIL")) {
							tilesetOffset = currLumpStart;
							tlOff = i;
						}

						//Read it now?
						if (!isGenRead(result) && requiresGen[i]) {
							System.out.println("  + Saving for later... ");
							pendingGen.addElement(new LumpStarter(currLumpStart, lastLumpSize, lumpActions[i]));
							try {
								input.skip(lastLumpSize);
							} catch (IOException ex) {
								throw new RuntimeException("Error skipping: " + search);
							}
						} else {
                                                    input = markAndRead(result, input, lumpActions[i], currLumpStart, lastLumpSize);
						}
                                                
                                                if (search.equals("GEN")) {
                                                    for (int mapID=0; mapID<result.getNumMaps(); mapID++)
                                                        result.initMap(mapID);
                                                }
                                                
						found = true;
						break;
					}
				}

				//Avoid future problems...
				if (!found) {
					System.out.println("Error! No parser exists for: " + lumpName + "  :  " + search );
					try {
						input.skip(lastLumpSize);
					} catch (IOException ex) {}
				}
			}
		}

		//Now that we're done the first pass, re-do anything that required GEN
		if (pendingGen.size() > 0)
			System.out.println("Re-scanning skipped data");
		while (pendingGen.size() > 0) {
			LumpStarter restart = (LumpStarter)pendingGen.elementAt(0);
                        pendingGen.removeElementAt(0);
			try {
				//Reset
				rpgReLoader.closeRPGFile(input);
				input = rpgReLoader.getRPGFile();

				//Go to position
				input.skip(restart.lumpStart);
			} catch (IOException ex) {
				throw new RuntimeException("Error re-reading lumps: " + ex.toString());
			}
			input = markAndRead(result, input, restart.parser, currLumpStart, restart.lumpSize);
		}

		//Finally...
		rpgReLoader.closeRPGFile(input);
		*/
	}


        public void postProcess(RPG result) {
            System.out.println("Combined door maps....");

            //Because doors are inter-linked, we must do this here.
            for (int map=0; map<result.getNumMaps(); map++)
                result.getMap(map).mergeDoors();

            for (int map=0; map<result.getNumMaps(); map++) {
                result.getMap(map).tempLoadingData1 = null;
                result.getMap(map).tempLoadingData2 = null;
            }

           //System.out.println("Warning: Door maps' y co-ords are all +1");
            for (int map=0; map<result.getNumMaps(); map++) {
                for (int d=0; d<result.getMap(map).doors.length; d++) {
                    result.getMap(map).doors[d].posY--;
                    result.getMap(map).doors[d].gotoY--; //since we computed gotoY when posY was messed up for ALL maps...
                }
            }
        }

        
        //Not used...
        public void loadMap(RPG result, int num) {throw new RuntimeException("LumpLoader does not suupport the method loadMap()");}
        public void loadTextBoxBlock(RPG result, int blockID) {throw new RuntimeException("LumpLoader does not suupport the method loadTextBoxBlock()");}
        public void loadBattleSprite(RPG result, int num) {throw new RuntimeException("LumpLoader does not suupport the method loadBattleSprite()");}
        public void loadSmallEnemySprite(RPG result, int num) {throw new RuntimeException("LumpLoader does not suupport the method loadSmallEnemySprite()");}
        public void loadMediumEnemySprite(RPG result, int num) {throw new RuntimeException("LumpLoader does not suupport the method loadMediumEnemySprite()");}
        public void loadLargeEnemySprite(RPG result, int num) {throw new RuntimeException("LumpLoader does not suupport the method loadLargeEnemySprite()");}





	public void loadTileset(RPG result, int num) {
	/*	FileInputStream input = rpgReLoader.getRPGFile();
		try {
			//input.reset();
			input.skip(tilesetOffset);
		} catch (IOException ex) {
			System.out.println("Error resetting for tileset: " + ex.toString());
		}
		((TilesetParser_Core)lumpActions[tlOff]).readTileset(result, input, num);

		//Finally...
		rpgReLoader.closeRPGFile(input);*/
	}


	public void loadWalkabout(RPG result, int num) {
	/*	ByteStreamReader input = rpgReLoader.getRPGFile();
		try {
			//input.reset();
			input.skip(ptOffsets[PictureParser.PT_WALKABOUT]);
		} catch (IOException ex) {
			System.out.println("Error resetting for walkabout: " + ex.toString());
		}
		pictAction.loadSprite(result, input, PictureParser.PT_WALKABOUT, num);

		//Finally...
		rpgReLoader.closeRPGFile(input);*/
	}


	//Perform an action, but ensure that, unless the parser reads WAY too far into this file,
    //it won't mess up the input for successive lumps.
	private InputStream markAndRead(RPG res, InputStream input, LumpParser action, long lumpStart, long lumpSize) {
		try {
			//Save the current position
                       // input.mark((int)(2*lumpSize));

			//Perform the action
			long remBytes = action.readLump(res, input, lumpSize, currHook);
                       input.skip(remBytes);
		} catch (IOException ex) {
			System.out.println("Error, (prob.) mark not supported: " + ex.toString());
		}
                return input;
	}

	private class LumpStarter {
		public long lumpStart;
		public long lumpSize;
		public LumpParser parser;

		public LumpStarter(long startOffset, long size, LumpParser parser) {
			this.lumpStart = startOffset;
			this.parser = parser;
			this.lumpSize = size;
		}
	}

}
