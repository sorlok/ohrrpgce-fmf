package ohrrpgce.simple;

import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;


public class RPGReader {
	private static final long[] PDP_OFFSETS = {256*256, 256*256*256, 1, 256};
	private static final char[] CR_LF = {0x0D, 0x0A};
	private static final int BROWSE_RECORD_LEN = 40;
	private static final int BROWSE_NUM_SIZE_BYTES = 2;
	private static final int BSAVE_HEADER_LEN = 7;
	private static final char BSAVE_MAGIC_NUMBER = 0xFD;
	private static final String[] BINSIZE_MAP = {"ATTACK.BIN", "STF", "SONGDATA.BIN", "SFXDATA.BIN"};
	private static final char[] BAM_HEADER = {67, 66, 77, 70};
	private static final int DEFPASS_RECORD_LEN = 322;
	private static final int VEH_RECORD_SIZE = 80;
	private static final int VEH_NAME_LEN = 16;
	private static final int VEH_NAME_NUM_SIZE_BYTES = 1;
	private static final int MXS_RECORD_SIZE = 64000;
	private static final int TILESET_RECORD_SIZE = 320 * 200;
	private static final int MAP_NAME_RECORD_SIZE = 80;
	private static final int MAP_NAME_NUM_SIZE_BYTES = 1;
	private static final int DOORS_PER_MAP = 100;
	private static final int TAP_RECORD_SIZE = 40;
	private static final int TAP_NUM_ACTIONS = 9;
	private static final int TAG_NAME_RECORD_SIZE = 42;
	private static final int TAG_NAME_NUM_SIZE_BYTES = 2;
	private static final int MASTER_PAL_NUM_COLORS = 256;
	//private static final int MASTER_PAL_NUM_COLOR_BITS = 6;
	private static final int MASTER_PAL_WASTED_BYTES = 7;
	private static final int FONT_NUM_BYTES = 8;
	private static final char[] PAL_HEADER_MAGIC_NUMBER = {0x5c, 0x11};
	private static final int PAL_HEADER_LEN = 16;
	private static final int PAL_RECORD_SIZE = 16;
	private static final int SAY_RECORD_SIZE = 400;
	private static final int SAY_LINE_SIZE = 38;
	private static final int SAY_LINES_PER_BOX = 8;
	private static final int STRING_SINGLE_WIDTH = 11;
	//private static final int STRING_NUM_SIZE_BYTES = 1;
	private static final int STRING_MAX_WIDTH = 3;
	private static final String[] PT_SPRITE_TYPE = {"Hero graphics", "Small Enemies", "Medium Enemies", "Large Enemies", "Walkabouts", "Weapons", "Attacks"};
	private static final int[][] PT_SPRITE_DIMENSIONS = { {32, 40, 8}, {34, 34, 1}, {50, 50, 1}, {80, 80, 1}, {20, 20, 8}, {24, 24, 2}, {50, 50, 3} };
	private static final int DT0_HERO_FRAME_SIZE = 636;
	private static final int DT0_HERO_NAME_MAX_SIZE = 34;
	private static final int DT0_HERO_NAME_NUM_SIZE_BYTES = 2;
	private static final int DT0_ENEMY_FRAME_SIZE = 320;
	private static final int DT0_ENEMY_NAME_NUM_SIZE_BYTES = 2;
	private static final int DT0_ENEMY_NAME_MAX_SIZE = 34;
	private static final int FORMATION_RECORD_LENGTH = 80;
	private static final int FORM_MAX_ENEMIES = 4;
	private static final int EFS_RECORD_LENGTH = 50;
	private static final int EFS_MAX_FORMATIONS = 20;
	private static final int ITEM_FRAME_SIZE = 200;
	private static final int ITEM_NAME_INFO_NUM_SIZE_BYTES = 2;
	private static final int ITEM_NAME_MAX_BYTES = 18;
	private static final int ITEM_INFO_MAX_BYTES = 72;
	private static final int MAP_FRAME_SIZE = 40;
	private static final int SONG_NUM = 100;
	private static final int SHOP_FRAME_SIZE = 40;
	private static final int SHOP_NAME_NUM_SIZE_BYTES = 2;
	private static final int SHOP_NAME_MAX_BYTES = 32;
	private static final int SHOP_NUM_ITEMS = 50;
	private static final int SHOP_ITEM_FRAME_SIZE = 84;
	private static final int SHOP_ITEM_NAME_NUM_SIZE_BYTES = 2;
	private static final int SHOP_ITEM_NAME_MAX_SIZE = 34;
	private static final int PLOTSCRIPT_FRAME_SIZE = 40;
	private static final int PLOTSCRIPT_NAME_NUM_SIZE_BYTES = 2;
	private static final int PLOTSCRIPT_NAME_MAX_BYTES = 38;
	
	private static final int FULLSCREEN_IMAGE_WIDTH = 320;
	private static final int FULLSCREEN_IMAGE_HEIGHT = 200;
	private static final int MXS_SEGMENTS = 4;
	
	//Saved for later
	private ArrayList/*<int[]>*/ fullscreenBackdrops = new ArrayList/*<int[]>*/();
	private IndexColorModel masterPalette;
	
	/*private static final int[] STRING_WIDTHS_WIKI = { //From the wiki
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 
		1, 3, 3, 3, 2, 2, 1, 1, 1, 1, 1, 1, 3, 2, 2, 2, 2, 2, 2, 2, 2
	};
	private static final int[] STRING_WIDTHS_WH = { //From wandering hamster
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1, 3, 
		3, 3, 2, 2, 1, 1, 1, 1, 1, 1, 3, 2
	};*/
	
	private File in;
	private BufferedInputStream input;
	private boolean isEOF;
	
	public RPGReader(File in) {
		this.in = in;
		try {
			input = new BufferedInputStream(new BufferedInputStream(new FileInputStream(in)));
		} catch (FileNotFoundException ex) {}
	}
	
	public void parse() {
		isEOF = false;
		String internalName =  "";
		while (true) {
			//Read the next lump's name
			String lumpName = readNTString();
			if (isEOF) {
				System.out.println("-------Successful termination.--------");
				break;
			}
			System.out.println(lumpName);
			
			//Read the number of bytes 
			long numBytes = readPDPWord();
			System.out.println("("+numBytes+")");
			
			if (lumpName.equals("ARCHINYM.LMP")) {
				//The internal name
				internalName = readCRLFString();
				numBytes -= (internalName.length() + CR_LF.length);
				System.out.println("  + Internal name: " + internalName);
				
				//The version of custom/rpgfix used to create this.
				String versionStr = readCRLFString();
				numBytes -= (versionStr.length() + CR_LF.length);
				System.out.println("  + Version Info: " + versionStr);
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.equals("BROWSE.TXT")) {
				//The long name of the game
				String longName = readFixedString(BROWSE_NUM_SIZE_BYTES, BROWSE_RECORD_LEN);
				numBytes -= BROWSE_RECORD_LEN;
				System.out.println("  + Long Name: " + longName);
				
				//The about line of our game
				String aboutLine = readFixedString(BROWSE_NUM_SIZE_BYTES, BROWSE_RECORD_LEN);
				numBytes -= BROWSE_RECORD_LEN;
				System.out.println("  + About Line: " + aboutLine);
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.equals("BINSIZE.BIN")) {
				//Size of future lumps
				for (int i=0; i<BINSIZE_MAP.length; i++) {
					if (numBytes>0) {
						int sizeVal = readInt();
						numBytes -= 2;
						System.out.println("  + Size of lump [" + BINSIZE_MAP[i] + "] is: " + sizeVal);
					} else {
						System.out.println("  + Warning! Missing size info for: [" + BINSIZE_MAP[i] + "]");
					}
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.equals("DEFPASS.BIN")) {
				//Default pass information
				while (numBytes>=DEFPASS_RECORD_LEN) {
					ignoreBytes(DEFPASS_RECORD_LEN);
					numBytes -= DEFPASS_RECORD_LEN;
					System.out.println("  + Default tile information read (Incomplete parse)");
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".GEN")) {
				//Bsave nonsense
				int len = readBSaveLength();
				numBytes -= BSAVE_HEADER_LEN;

				//Before we go any further
				sizeCheck(numBytes-len);
				
				//For now
				System.out.println("  + .GEN header checks out. (Incomplete parse)");
				ignoreBytes(len);
			} else if (lumpName.endsWith(".VEH")) {
				//Vehicle information
				while (numBytes >= VEH_RECORD_SIZE) {
					Vehicle veh = readVehicle();
					veh.print("  + ", "     + ");
					numBytes -= VEH_RECORD_SIZE;
				}

				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".TIL")) {
				//Tilesets
				while (numBytes >= TILESET_RECORD_SIZE) {
					ignoreBytes(TILESET_RECORD_SIZE);
					numBytes -= TILESET_RECORD_SIZE;
					System.out.println("  + Ignoring tileset. (Incomplete parse)");
				}

				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".MXS")) {
				//Full screen backdrops
				while (numBytes >= MXS_RECORD_SIZE) {
					//Create an image for this backdrop
					//BufferedImage img = new BufferedImage(FULLSCREEN_IMAGE_WIDTH, FULLSCREEN_IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, );
					//WritableRaster r = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, FULLSCREEN_IMAGE_WIDTH, FULLSCREEN_IMAGE_HEIGHT, MXS_SEGMENTS, null);
					//r.setPixels(0, 0, FULLSCREEN_IMAGE_WIDTH, FULLSCREEN_IMAGE_HEIGHT, readRawBytes(FULLSCREEN_IMAGE_WIDTH*FULLSCREEN_IMAGE_HEIGHT));
					fullscreenBackdrops.add(readMXData(FULLSCREEN_IMAGE_WIDTH,FULLSCREEN_IMAGE_HEIGHT));
					
					//r.setPixel(0, 0, readRawBytes(FULLSCREEN_IMAGE_WIDTH*FULLSCREEN_IMAGE_HEIGHT));
					
					numBytes -= MXS_RECORD_SIZE;
					System.out.println("  + Backdrop(" + fullscreenBackdrops.size() + ") raster saved");
				}

				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".HSP")) {
				//Hamsterspeak definitions
				String hsHead = readNTString();
				if (!hsHead.equals("HS")) {
					System.out.println("  + Warning: expected HS header, not: " + hsHead);
				}
				long hspLen = readPDPWord(); //This only matters for this header...
				numBytes -= (hsHead.length()+1);
				numBytes -= PDP_OFFSETS.length;
								
				//Quick check
				//sizeCheck(numBytes-hspLen);
				//System.out.println("  + HS Header");
				
				//Hamsterspeak header
				String name = readNTString();
				int version = name.charAt(name.length()-1);
				name = name.substring(0, name.length()-1);
				numBytes -= (name.length() + 2);
				System.out.println("  + " + name + "(" + version  + ")");
				
				//Scripts
				String scriptHead = readNTString();
				if (!scriptHead.equals("SCRIPTS.TXT")) {
					System.out.println("  + Warning: expected SCRIPTS.TXT, not: " + scriptHead);
				}
				hspLen = readPDPWord();
				numBytes -= (scriptHead.length()+1);
				numBytes -= PDP_OFFSETS.length;

				//System.out.println("::" + numBytes);
				//System.out.println("::" + hspLen);
				
				//Quick check
				//sizeCheck(numBytes-hspLen);
				//System.out.println("  + Scripts:");
				
				//Scripts
				while (hspLen > 0) {
					int totalSub = 0;
					String scrName = readCRLFString();
					totalSub +=  (scrName.length() + CR_LF.length);
					String scrID = readCRLFString();
					totalSub +=  (scrID.length() + CR_LF.length);
					String scrNumArgs = readCRLFString();
					totalSub +=  (scrNumArgs.length() + CR_LF.length);
					int num = Integer.parseInt(scrNumArgs);
					String[] defaultArgs = new String[num];
					for (int i=0; i<num; i++) {
						defaultArgs[i] = readCRLFString();
						totalSub +=  (defaultArgs[i].length() + CR_LF.length);
					}
					
					//Decrement
					hspLen -= totalSub;
					numBytes -= totalSub;
					
					//Show
					StringBuilder sb = new StringBuilder(scrName.length()*3);
					sb.append("  + Script[" + scrID + "] \"" + scrName + "\" (" + num + ") defaults[");
					String sep = "";
					//for (String s : defaultArgs) {
					for (int sI=0; sI<defaultArgs.length; sI++) {
						sb.append(sep + defaultArgs[sI]);
						sep = ", ";
					}
					sb.append("]");
					System.out.println(sb.toString());
				}
				
				//Skip the rest - compiled scripts
				ignoreBytes(numBytes);
			} else if (lumpName.endsWith(".MN")) {
				//Map names. Easy!
				while (numBytes >= MAP_NAME_RECORD_SIZE) {
 					String name = readFixedString(MAP_NAME_NUM_SIZE_BYTES, MAP_NAME_RECORD_SIZE);
					numBytes -= MAP_NAME_RECORD_SIZE;
					System.out.println("  + Map: " + name);
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".DOX")) {
				//Door info
				int doorRecordSize = DOORS_PER_MAP * 3 * 2; //x,y,used ints, two bytes per int.
				while (numBytes >= doorRecordSize) {
					ignoreBytes(doorRecordSize);
					numBytes -= doorRecordSize;
				}
				
				System.out.println("  + Door info skipped. (Incomplete parse)");
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".DT0")) {
				//Hero info
				while (numBytes >= DT0_HERO_FRAME_SIZE) {
					String currName = readFixedString(DT0_HERO_NAME_NUM_SIZE_BYTES, DT0_HERO_NAME_MAX_SIZE, true);
					if (currName.length()>0)
						System.out.println("  + Skipping info for hero \"" + currName + "\" (Incomplete parse)");
					ignoreBytes(DT0_HERO_FRAME_SIZE-DT0_HERO_NAME_MAX_SIZE);
					
					numBytes -= DT0_HERO_FRAME_SIZE;
				}
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".DT1")) {
				//Enemy Data
				while (numBytes >= DT0_ENEMY_FRAME_SIZE) {
					String currName = readFixedString(DT0_ENEMY_NAME_NUM_SIZE_BYTES, DT0_ENEMY_NAME_MAX_SIZE, true);
					if (currName.length()>0)
						System.out.println("  + Skipping info for enemy \"" + currName + "\" (Incomplete parse)");
					ignoreBytes(DT0_ENEMY_FRAME_SIZE-DT0_ENEMY_NAME_MAX_SIZE);
					
					numBytes -= DT0_ENEMY_FRAME_SIZE;
				}
				//Check
				sizeCheck(numBytes);				
			} else if (lumpName.endsWith(".DT6")) {
				//We don't care...
				ignoreBytes(numBytes);
				System.out.println("  + Additional attack data skipped. (Incomplete parse)");
			} else if (lumpName.endsWith("ATTACK.BIN")) {	
				//We don't care...
				ignoreBytes(numBytes);
				System.out.println("  + Attack data skipped. (Incomplete parse)");				
			} else if (lumpName.matches("[^.]+\\.[0-9]?[0-9]")) {
				//It's a BAM song.
				String unpadded = lumpName.substring(lumpName.length()-2, lumpName.length());
				if (unpadded.charAt(0) == '.')
					unpadded = Character.toString(unpadded.charAt(1));
				int id = Integer.parseInt(unpadded);
				System.out.println("  + Song: " + id);
				
				//Is this a valid BAM file?
				numBytes -= BAM_HEADER.length;
				if (!readBAMHeader()) {
					System.out.println("  + Warning! Invalid BAM header");
					ignoreBytes(numBytes);
				} 
				
				//We don't care.
				System.out.println("  + Skipping BAM file. (Incomplete parse)");
				ignoreBytes(numBytes);
			} else if (lumpName.endsWith(".TAP")) {
				//Tile animation info
				while (numBytes >= TAP_RECORD_SIZE) {
					int startTile = readInt();
					int disableIf = readInt();
					int[] actionTypes = new int[TAP_NUM_ACTIONS];
					int[] actionVals = new int[TAP_NUM_ACTIONS];
					for (int i=0; i<TAP_NUM_ACTIONS; i++) {
						actionTypes[i] = readInt();
					}
					for (int i=0; i<TAP_NUM_ACTIONS; i++) {
						actionVals[i] = readInt();
					}
					numBytes -= TAP_RECORD_SIZE;
					
					//Print...
					StringBuilder sb = new StringBuilder(150);
					sb.append("  + Tile anims (from tile " + startTile + ") ");
					if (disableIf>0) {
						sb.append("(disable if " + disableIf + " is ON) ");
					} else if (disableIf<0) {
						sb.append("(disable if " + -disableIf + " is OFF) ");
					}
					sb.append("Actions: [");
					String sep = "";
					TAP_ACT_LOOP:
					for (int i=0; i<TAP_NUM_ACTIONS; i++) {
						int currType = actionTypes[i];
						int currValue = actionVals[i];
						
						switch (currType) {
						case 0:
							sb.append(sep + "(end)");
							break TAP_ACT_LOOP;
						case 1:
							sb.append(sep + "(move up " + currValue + ")");
							break;
						case 2:
							sb.append(sep + "(move down " + currValue + ")");
							break;
						case 3:
							sb.append(sep + "(move right " + currValue + ")");
							break;
						case 4:
							sb.append(sep + "(move left " + currValue + ")");
							break;
						case 5:
							sb.append(sep + "(stop for " + currValue + " ticks)");
							break;
						case 6:
							sb.append(sep + "(continue if " + Math.abs(currValue));
							if (currValue > 0) 
								sb.append(" is ON)");
							else
								sb.append(" is OFF)");
							break;
						default:
							sb.append(sep + "<invalid type: " + currType + ">");
							break;
						}
						
						sep = ", ";
					}
					sb.append("]");
					System.out.println(sb.toString());
				}

				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".TMN")) {
				//Tag names
				System.out.println("  + Tag: <OFF>");
				System.out.println("  + Tag: <ON>");
				while (numBytes >= TAG_NAME_RECORD_SIZE) {
					//Get the name
					String tName = readFixedString(TAG_NAME_NUM_SIZE_BYTES, TAG_NAME_RECORD_SIZE, true);
					tName = tName.replaceAll(Character.toString('\0'), "");
					numBytes -= TAG_NAME_RECORD_SIZE;
					System.out.println("  + Tag: " + tName);
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".MAS")) {
				//Master palette
				//Bsave nonsense
				int len = readBSaveLength();
				numBytes -= BSAVE_HEADER_LEN;

				//Before we go any further
				sizeCheck(numBytes-len);
				
				//Now!
				byte[] reds = new byte[MASTER_PAL_NUM_COLORS];
				byte[] greens = new byte[MASTER_PAL_NUM_COLORS];
				byte[] blues = new byte[MASTER_PAL_NUM_COLORS];
				StringBuilder sb = new StringBuilder(1600);
				sb.append("  + Master Palette: [");
				String sep = "";
				for (int i=0; i<MASTER_PAL_NUM_COLORS; i++) {
					reds[i] = (byte)readInt();
					greens[i] = (byte)readInt();
					blues[i] = (byte)readInt();
					Color curr = new Color(reds[i], greens[i], blues[i]);
					
					//Whoops...
					reds[i] = (byte)(reds[i]*255/63);
					greens[i] = (byte)(greens[i]*255/63);
					blues[i] = (byte)(blues[i]*255/63);
					
					sb.append(sep + curr.getHex());
					sep = ", ";
					numBytes -= 3*2;
				}
				sb.append("]");
				System.out.println(sb.toString());
				
				//Create the master palette
				masterPalette = new IndexColorModel(4, MASTER_PAL_NUM_COLORS, reds, greens, blues);
				
				//Remaining bytes
				if (numBytes == MASTER_PAL_WASTED_BYTES) {
					ignoreBytes(MASTER_PAL_WASTED_BYTES);
					numBytes -= MASTER_PAL_WASTED_BYTES;
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".FNT")) {
				//Bsave nonsense
				int len = readBSaveLength();
				numBytes -= BSAVE_HEADER_LEN;

				//Before we go any further
				sizeCheck(numBytes-len);
				
				//Now, read the font.
				System.out.println("  + Font Data (skipping empty letters)" );
				StringBuilder[] currRows = new StringBuilder[FONT_NUM_BYTES+2];
				int numStoredFonts = 0;
				for (int i=0; i<currRows.length; i++)
					currRows[i] = new StringBuilder((FONT_NUM_BYTES+3) * FONT_NUM_BYTES);
				while (numBytes >= FONT_NUM_BYTES) {
					byte[] fontData = new byte[FONT_NUM_BYTES];
					for (int i=0; i<FONT_NUM_BYTES; i++) {
						fontData[i] = readByte();
						numBytes -= 1;
					}
					Font f = new Font(fontData);
					if (!f.isEmpty()) {					
						String[] rows = f.getRows();
						for (int i=0; i<rows.length; i++) {
							currRows[i].append(rows[i]);
						}
						numStoredFonts++;
						
						if (numStoredFonts==FONT_NUM_BYTES) {
							//Flush our print buffer
							for (int i=0; i<currRows.length; i++) {
								System.out.println(currRows[i]);
								currRows[i] = new StringBuilder((FONT_NUM_BYTES+3) * FONT_NUM_BYTES);
							}
							
							numStoredFonts = 0;
						}
					}
				}
				
				//Flush our print buffer
				if (numStoredFonts>0) {
					//Flush our print buffer
					for (int i=0; i<currRows.length; i++) {
						System.out.println(currRows[i]);
					}
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".PAL")) {
				//Palette header
				numBytes -= resolvePaletteHeader();
				
				//Palette info
				while (numBytes >= PAL_RECORD_SIZE) {
					ignoreBytes(PAL_RECORD_SIZE);
					numBytes -= PAL_RECORD_SIZE;
				}
				
				System.out.println("  + Skipping palettes. (Incomplete parse)");
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".DOR")) {
				//Easiest lump ever...
				System.out.println("  + Obsolete!");
				ignoreBytes(numBytes);
			} else if (lumpName.endsWith(".SAY")) {
				//Message boxes
				while (numBytes >= SAY_RECORD_SIZE) {
					StringBuilder sb = new StringBuilder();
					String nl = "";
					for (int i=0; i<SAY_LINES_PER_BOX; i++) {
						String line = readNTString(SAY_LINE_SIZE);
						if (line.length()>0) {
							sb.append(nl + line);
							nl = "\n";
						}
					}
					if (sb.length()>0) {
						System.out.println("---------------Message-------------------");
						System.out.println(sb.toString());
						System.out.println("----------------------------------------");
					}
					
					//Skip info for now
					ignoreBytes(SAY_RECORD_SIZE - (SAY_LINES_PER_BOX*SAY_LINE_SIZE));
					
					numBytes -= SAY_RECORD_SIZE;
				}

				//Check
				sizeCheck(numBytes);		
			} else if (lumpName.endsWith(".STT")) {
				//Strings
				String currStr = null; //= new StringBuilder();
				int maxWidth = STRING_SINGLE_WIDTH * STRING_MAX_WIDTH;
				while (numBytes > 0) {
					//The method provided by the wiki (arbitrary-length strings)
					//  is exact, but I prefer a hueristical approach.
					int nextSize = readByte();
					
					//Is this reasonable?
					if (currStr != null) {
						if (nextSize>maxWidth || nextSize==0) { //Our heuristic.
							//This was a character... ignore some bytes.
							ignoreBytes(STRING_SINGLE_WIDTH-1);
							numBytes -= STRING_SINGLE_WIDTH;
							continue;
						}
					} else
						currStr = "";
					
					//Check
					if (nextSize > numBytes) {
						//We messed up somewhere....
						throw new RuntimeException("  + Error! Invalid size: " + nextSize);
					}
					
					//So read the string!
					int roundedUp = ((nextSize/STRING_SINGLE_WIDTH)+1)*STRING_SINGLE_WIDTH;
					currStr = readString(nextSize);
					if ((numBytes-nextSize)>=STRING_SINGLE_WIDTH)  {
						ignoreBytes(roundedUp-nextSize-1);
						numBytes -= roundedUp;
					} else {
						ignoreBytes(numBytes-nextSize);
						numBytes = 0;
					}

					System.out.println("  + String["+roundedUp+"]: " + currStr);
					currStr = "";
				}

				//SO essential
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".FOR")) {
				//Enemy formations
				while (numBytes >= FORMATION_RECORD_LENGTH) {
					StringBuilder sb = new StringBuilder();
					sb.append("  + Enemy formation [");
					String sep = "";
					boolean skip = true;
					for (int i=0; i<FORM_MAX_ENEMIES; i++) {
						int type = readInt()-1;
						int eX = readInt();
						int eY = readInt();
						readInt();
						if (type >= 0) {
							sb.append(sep + type + "@" + eX + "," + eY);
							sep = ", ";
							skip = false;
						}
					}
					sb.append("] {");
					int bkgrd = readInt();
					int battleMusic = readInt()-1;
					int animFrames = readInt()+1;
					int animSpeed = readInt();
					sb.append(bkgrd + "," + battleMusic + "," + animFrames + "," + animSpeed + "} (Incomplete parse.)");
					ignoreBytes(FORMATION_RECORD_LENGTH - 20*2);
					
					if (!skip)
						System.out.println(sb.toString());
					numBytes -= FORMATION_RECORD_LENGTH;
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".EFS")) {
				//Enemy formation sets
				while (numBytes >= EFS_RECORD_LENGTH) {
					int bFreq = readInt();
					StringBuilder sb = new StringBuilder();
					sb.append("  + Formation set (freq:" + bFreq + "%) [");
					String sep = "";
					boolean skip = true;
					for (int i=0; i<EFS_MAX_FORMATIONS; i++) {
						int formNum = readInt()-1;
						if (formNum >= 0) {
							sb.append(sep + formNum);
							sep = ", ";
							skip = false;
						}
					}
					sb.append("]");
					ignoreBytes(EFS_RECORD_LENGTH - EFS_MAX_FORMATIONS*2 - 2);
					
					if (!skip)
						System.out.println(sb.toString());
					
					numBytes -= EFS_RECORD_LENGTH;
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".ITM")) {
				//Item info
				while (numBytes >= ITEM_FRAME_SIZE) {
					String itemName = readFixedString(ITEM_NAME_INFO_NUM_SIZE_BYTES, ITEM_NAME_MAX_BYTES, true);
					String itemInfo = readFixedString(ITEM_NAME_INFO_NUM_SIZE_BYTES, ITEM_INFO_MAX_BYTES, true);
					
					if (itemName.length()>0 || itemInfo.length()>0)
						System.out.println("  + Skipping info for item [" + itemName + "] \"" + itemInfo + "\" (Incomplete parse.)");
					
					ignoreBytes(ITEM_FRAME_SIZE - ITEM_NAME_MAX_BYTES - ITEM_INFO_MAX_BYTES);
					numBytes -= ITEM_FRAME_SIZE;
				}

				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".MAP")) {
				//Map info
				while (numBytes >= MAP_FRAME_SIZE) {
					ignoreBytes(MAP_FRAME_SIZE);
					numBytes -= MAP_FRAME_SIZE;
					System.out.println("  + Skipping map. (Incomplete parse)");
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".SNG")) {
				//Song files
				for (int i=0; i<SONG_NUM; i++) {
					String sTitle = readCRLFString();
					numBytes -= (sTitle.length()+CR_LF.length);
					if (sTitle.replaceAll("\"", "").length() > 0)  {
						System.out.println("  + Song: " + sTitle);
					}
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".SHO")) {
				//General shop info
				boolean oneEmptyShop = false;
				while (numBytes >= SHOP_FRAME_SIZE) {
					if (oneEmptyShop) {
						//Weird glitch...
						ignoreBytes(SHOP_FRAME_SIZE);
						numBytes -= SHOP_FRAME_SIZE;
						continue;
					}
					String shopName = readFixedString(SHOP_NAME_NUM_SIZE_BYTES, SHOP_NAME_MAX_BYTES, true);
					ignoreBytes(SHOP_FRAME_SIZE - SHOP_NAME_MAX_BYTES);
					numBytes -= SHOP_FRAME_SIZE;
					if (shopName.length()==0)
						oneEmptyShop = true;
					else 
						System.out.println("  + Skipping shop: \"" + shopName + "\"");
				}
				
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith(".STF")) {
				//Stuff in shops!
				while (numBytes >= SHOP_ITEM_FRAME_SIZE) {
					StringBuilder sb = new StringBuilder();
					sb.append("  + Shop Items: [");
					String sep = "";
					boolean skipRem = false;
					for (int i=0; i<SHOP_NUM_ITEMS && numBytes>=SHOP_ITEM_FRAME_SIZE; i++) {
						if (skipRem) {
							//Weird glitch with un-initialized data... not sure what's going on.
							ignoreBytes(SHOP_ITEM_FRAME_SIZE);
							numBytes -= SHOP_ITEM_FRAME_SIZE;
							continue;
						}
						
						String itName = readFixedString(SHOP_ITEM_NAME_NUM_SIZE_BYTES, SHOP_ITEM_NAME_MAX_SIZE, true);
						//char[] cArr = readString(SHOP_ITEM_NAME_MAX_SIZE).toCharArray();
						//itName = "(" + (int)cArr[0] + ")" + cArr[2] + "" + cArr[4];
						//ignoreBytes(SHOP_ITEM_NAME_MAX_SIZE);
						int type = readInt();
						int numInStock = readInt();
						ignoreBytes(SHOP_ITEM_FRAME_SIZE - SHOP_ITEM_NAME_MAX_SIZE - 2*2);
						numBytes -= SHOP_ITEM_FRAME_SIZE;
						if (itName.length() > 0) {
							sb.append(sep + itName);
							if (type==1)
								sb.append("[Hero]");
							if (numInStock>0)
								sb.append("(num:" + numInStock + ")");
							sep = ", ";
						} else {
							//sb.append("<garbage>");
							skipRem = true;
						}
					}
					sb.append("] (Incomplete parse)");
					System.out.println(sb.toString());
				}
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.endsWith("PLOTSCR.LST")) {
				//Plotscripting names
				while (numBytes >= PLOTSCRIPT_FRAME_SIZE) {
					int plID = readInt();
					String plName = readFixedString(PLOTSCRIPT_NAME_NUM_SIZE_BYTES, PLOTSCRIPT_NAME_MAX_BYTES);					
					numBytes -= PLOTSCRIPT_FRAME_SIZE;
					if (plName.length() > 0)
						System.out.println("  + Plotscript[" + plID + "] is \"" + plName + "\"");
				}
				
				//Check
				sizeCheck(numBytes);				
			} else if (lumpName.matches("[^.]+\\.PT[0-9]")) {
				//Sprite data
				int id = Integer.parseInt(Character.toString(lumpName.charAt(lumpName.length()-1)));
				System.out.println("  + Sprite info [" + PT_SPRITE_TYPE[id] + "]");
				int width = PT_SPRITE_DIMENSIONS[id][0];
				int height = PT_SPRITE_DIMENSIONS[id][1];
				int frames = PT_SPRITE_DIMENSIONS[id][2];
				int bytes_per_frame = (((width * height) / 2) * frames);
				
				int count = 0;
				while (numBytes >= bytes_per_frame) {
					count++;
					ignoreBytes(bytes_per_frame);
					numBytes -= bytes_per_frame;
				}
				System.out.println("  + Skipping " + count + " sprites. (Incomplete parse)");
						
				//Check
				sizeCheck(numBytes);
			} else if (lumpName.matches("[^.]+\\.[DLN][0-9][0-9]")) {
				//Door/NPC maps
				char type = lumpName.charAt(lumpName.length()-3);
				int id = Integer.parseInt(lumpName.substring(lumpName.length()-2, lumpName.length()));
				System.out.println("  + Map info (" + type + ") for map ["+id+"]");
				
				//Bsave nonsense
				int len = readBSaveLength();
				numBytes -= BSAVE_HEADER_LEN;

				//Before we go any further
				sizeCheck(numBytes-len);
				
				//Now...
				System.out.println("  + Ignoring map data (Incomplete parse)");
				ignoreBytes(numBytes);
			} else if (lumpName.matches("[^.]+\\.[TPE][0-9][0-9]")) {
				//Tile map, etc.
				char type = lumpName.charAt(lumpName.length()-3);
				int id = Integer.parseInt(lumpName.substring(lumpName.length()-2, lumpName.length()));
				System.out.println("  + Map info (" + type + ") for map ["+id+"]");
				
				//Bsave nonsense
				int len = readBSaveLength();
				numBytes -= BSAVE_HEADER_LEN;

				//Before we go any further
				sizeCheck(numBytes-len);
				
				//Size
				int width = readInt();
				int height = readInt();
				System.out.println("  + Size: " + width + "x" + height);
				numBytes -= 4;
				
				//Now...
				System.out.println("  + Ignoring map data (Incomplete parse)");
				ignoreBytes(numBytes);
				
				//Also...
				sizeCheck(numBytes - (width*height));
			} else {
				//Just ignore all data for now.
				ignoreBytes(numBytes);
			}
			
			if (isEOF) {
				System.out.println("----ERROR! Terminated early!----");
				break;
			}
		}
		
		
		//Done parsing. Now, test our images...
		int i=0;
		//for (int[] indices : fullscreenBackdrops) {
		for (int iI=0; iI<fullscreenBackdrops.size(); iI++) {
			int[] indices = (int[])fullscreenBackdrops.get(iI);
			
			//Create the image
			
			//RenderedImage img = new BufferedImage(FULLSCREEN_IMAGE_WIDTH, FULLSCREEN_IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, masterPalette);
			//((BufferedImage)img).setRGB(0, 0, FULLSCREEN_IMAGE_WIDTH, FULLSCREEN_IMAGE_HEIGHT, pix, 0, FULLSCREEN_IMAGE_WIDTH);
			
			/*Image img = Toolkit.getDefaultToolkit().createImage(
	                new MemoryImageSource(FULLSCREEN_IMAGE_WIDTH, FULLSCREEN_IMAGE_HEIGHT,
	                masterPalette, pix, 0, FULLSCREEN_IMAGE_WIDTH));*/

			//Mostly from:
			//http://www.exampledepot.com/egs/java.awt.image/Mandelbrot2.html?l=rel
           /* DataBuffer dbuf = new DataBufferByte(pix, FULLSCREEN_IMAGE_WIDTH*FULLSCREEN_IMAGE_HEIGHT, 0);
            int bitMasks[] = new int[]{(byte)0xf};
            SampleModel sampleModel = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_BYTE, FULLSCREEN_IMAGE_WIDTH, FULLSCREEN_IMAGE_HEIGHT, bitMasks);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, dbuf, null);
            RenderedImage img = new BufferedImage(masterPalette, raster, false, null);*/
			
			//Horribly slow, but I can't seem to get indexed pictures to work.
			BufferedImage img = new BufferedImage(FULLSCREEN_IMAGE_WIDTH, FULLSCREEN_IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
			for (int x=0; x<FULLSCREEN_IMAGE_WIDTH; x++) {
				for (int y=0; y<FULLSCREEN_IMAGE_HEIGHT; y++) {
					int val = indices[y*FULLSCREEN_IMAGE_WIDTH+x];
					if (val>0)
						img.setRGB(x, y, masterPalette.getRGB(val));
					else
						img.setRGB(x, y, 0xFF0000);
				}
			}
			
			
			//Save the image
			File path = new File(in.getParent() + "\\" + internalName + "\\" + internalName + "_mxs_" + (i++) + ".png");
			try {
				ImageIO.write(img, "png", path);
				System.out.println("Saved image: " + path.getAbsolutePath());
			} catch (IOException iex) {
				System.out.println("Error saving image: " + iex.toString());
			}
		}
		
	}
	
	private void sizeCheck(long num) {
		if (num!=0) {
			System.out.println("----Size error: " + num + " remaining.----");
			if (num > 0) {
				ignoreBytes(num);
			} else {
				throw new RuntimeException("----Size error: " + num + " remaining.----");
			}
		}
	}

	private void ignoreBytes(long num) {
		for (int i=0; i<num; i++) {
			readByte();
		}
	}
	
	private String readString(int numBytes) {
		byte[] res = new byte[numBytes];
		
		for (int i=0; i<numBytes; i++) {
			res[i] = readByte();
		}
		
		return new String(res);
	}
	
	private int[] readMXData(int width, int height) {
		int[] res = new int[width*height];
		for (int mxOff=0; mxOff<MXS_SEGMENTS; mxOff++) {
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x+=MXS_SEGMENTS) {
					res[y*width + x+mxOff] = readByte();
				}
			}
		}
		return res;
		/*
		char[] res = new char[num];
		try {
			if (input.read(res, 0, num)<num)
				isEOF = true;
		} catch (IOException iex) {
			isEOF = true;
		}
		
		//Should be a better way to convert this...
		byte[] bytes = new byte[num];
		for (int i=0; i<res.length; i++)
			bytes[i] = (byte)res[i];
		return bytes;*/
	}
	
	private byte readByte() {
		byte[] c = new byte[]{'\0'};
		try {
			if (input.read(c, 0, 1)<1)
				isEOF = true;
			return c[0];
		} catch (IOException iex) {
			isEOF = true;
			return 0;
		}
	}

	//returns the number of bytes read
	private int resolvePaletteHeader() {
		byte b = readByte();
		if (b == BSAVE_MAGIC_NUMBER) {
			readBSaveLength(true);
			return BSAVE_HEADER_LEN;
		} else if (b == PAL_HEADER_MAGIC_NUMBER[0] && readByte()==PAL_HEADER_MAGIC_NUMBER[1]) {
			//Read the rest of the header
			/*int lastPalette = */readInt();
			ignoreBytes(PAL_HEADER_LEN-4);
			return PAL_HEADER_LEN;
		} else {
			throw new RuntimeException("-----PAL header error: expected magic number------");
		}
	}
	
	private int readBSaveLength() {
		return readBSaveLength(false);
	}
	
	private int readBSaveLength(boolean skipMagicNumber) {
		//Read the magic number
		if (readByte() != BSAVE_MAGIC_NUMBER)
			throw new RuntimeException("-----BSAVE header error: expected magic number------");
		
		//Read the segment
		readInt();
		
		//Read the offset. Always 0
		int offset = readInt();
		if (offset!=0)
			System.out.println("-----Warning: BSAVE offset should be 0, not: " + offset + "------");
		
		//Read the length
		return readInt();
	}
	
	private int readInt() {
		//Little endian
		return readByte() + 0x100*readByte();
	}

	private String readNTString() {
		StringBuilder sb = new StringBuilder(16);
		for (byte c=readByte(); c!='\0'; c = readByte()) {
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	private String readNTString(int fixedNumBytes) {
		StringBuilder sb = new StringBuilder(fixedNumBytes);
		for (int i=0; i<fixedNumBytes; i++) {
			byte c=readByte();
			if (c!='\0')
				sb.append(c);
		}
		
		return sb.toString();
	}

	private String readCRLFString() {
		StringBuilder sb = new StringBuilder(16);
		for (byte c=readByte(); c!=CR_LF[1]; c = readByte()) {
			//We assume that all CR's are followed by LF's. This is reasonable.
			if (c!=CR_LF[0])
				sb.append(c);
		}
		
		return sb.toString();
	}
	
	private String readFixedString(int nSizeBytes, int fixedSize) {
		return readFixedString(nSizeBytes, fixedSize, false);
	}
	
	private String readFixedString(int nSizeBytes, int fixedSize, boolean skipAlternateBytes) {
		return readFixedString(nSizeBytes, fixedSize, skipAlternateBytes, false);
	}
	
	private String readFixedString(int nSizeBytes, int fixedSize, boolean skipAlternateBytes, boolean skipFirstByte) {
		StringBuilder sb = new StringBuilder(fixedSize);
		
		//Get string size info
		int strSize = 0;
		int mult = 1;
		for (int i=0; i<nSizeBytes; i++) {
			strSize += (mult * readByte());
			mult *= 0x100;
		}
	
		//Is this reasonable?
		int availBytes = fixedSize;
		if (skipAlternateBytes)
			availBytes /= 2;
		if (strSize > availBytes) {
			ignoreBytes(fixedSize-nSizeBytes);
			return "";
		}
		
		if (skipFirstByte)
			ignoreBytes(1);
		
		//Read the string
		for (int i=0; i<strSize; i++) {
			sb.append(readByte());
			
			if (skipAlternateBytes)
				ignoreBytes(1);
		}
		
		//Forget the remaining
		int mod = 1;
		int rem = 0;
		if (skipAlternateBytes)
			mod = 2;
		if (skipFirstByte)
			rem = 1;
		ignoreBytes(fixedSize - strSize*mod - nSizeBytes - rem);
		
		return sb.toString();
	}
	
	private long readPDPWord() {
		long currAmt = 0;
		
		for (int i=0; i<PDP_OFFSETS.length; i++) {
			currAmt += (PDP_OFFSETS[i] * readByte());
		}
		
		return currAmt;
	}
	
	private boolean readBAMHeader() {
		boolean valid = true;
		for (int i=0; i<BAM_HEADER.length; i++) {
			if (readByte() != BAM_HEADER[i])
				valid = false;
		}
		return valid;
	}
	
	
	public Vehicle readVehicle() {
		Vehicle res = new Vehicle();
		
		//Read the name & speed
		res.name = readFixedString(VEH_NAME_NUM_SIZE_BYTES, VEH_NAME_LEN);
		res.speed = readInt();
		
		//Read the bitsets.
		int bits = readInt();
		int mask = 1;
		for (int i=0; i<res.bitsets.length; i++) {
			res.bitsets[i] = (bits&mask)==1;
			mask <<= 1;
		}
		readInt(); //second set not used
		
		//More properties. Should be enums, but oh well
		res.randomBattles = readInt();
		res.useButton = readInt();
		res.menuButton = readInt();
		res.ifRidingTag = readInt();
		res.onMount = readInt();
		res.onDismount = readInt();
		res.overrideWalls = readInt();
		res.blockedBy = readInt();
		res.mountFrom = readInt();
		res.dismountTo = readInt();
		res.elevation = readInt();
		
		//Unused properties
		ignoreBytes(18*2);
		
		return res;
	}
	
	public void close(){
		try {
			input.close();
		} catch (IOException ex) {}
	}
	
	public static void main(String[] args) {
		if (args.length!=1) {
			throw new RuntimeException("Usage: RPGReader <input_file>");
		}
		
		String path = args[0];
		File in = new File(path);
		
		if (in == null) {
			throw new RuntimeException("Error with file: " + path);
		}
		
		System.out.println("***********Reading file: " + path + "***********");
		RPGReader reader = new RPGReader(in);
		reader.parse();
		reader.close();
		
	}
	
	
	class Vehicle {
		//Properties
		public String name;
		public int speed;
		public int randomBattles;
		public int useButton;
		public int menuButton;
		public int ifRidingTag;
		public int onMount;
		public int onDismount;
		public int overrideWalls;
		public int blockedBy;
		public int mountFrom;
		public int dismountTo;
		public int elevation;
		
		//Bitflags
		public boolean[] bitsets = {false, false, false, false, false, false, false, false, false}; 
		public boolean passThroughWalls() { return bitsets[0]; }
		public boolean passThroughNPCs() { return bitsets[1]; }
		public boolean enableNPCActivation() { return bitsets[2]; }
		public boolean enableDoorUse() { return bitsets[3]; }
		public boolean dontHideLeader() { return bitsets[4]; }
		public boolean dontHideParty() { return bitsets[5]; }
		public boolean dismountPlusOneSpace() { return bitsets[6]; }
		public boolean passWallsWhileDismounting() { return bitsets[7]; }
		public boolean disableFlyingShadow() { return bitsets[8]; }
		
		public void print(String header, String prefix) {
			System.out.println(header + "Vehicle: " + name);
			System.out.println(prefix + "Speed: " + speed);
			
			System.out.print(prefix + "Random Battles: ");
			if (randomBattles==-1)
				System.out.println("disabled");
			else if (randomBattles==0)
				System.out.println("enabled");
			else
				System.out.println("formation set: " + randomBattles);
			
			System.out.print(prefix + "Use button: ");
			System.out.println(getButtonTxt(useButton));
			
			System.out.print(prefix + "Menu button: ");
			System.out.println(getButtonTxt(menuButton));
			
			System.out.print(prefix + "If riding, tag: ");
			if (ifRidingTag==0)
				System.out.println("(N/A)");
			else if (ifRidingTag>0) 
				System.out.println(ifRidingTag + " ON");
			else
				System.out.println(-ifRidingTag + " OFF");
			
			System.out.print(prefix + "On mount: ");
			System.out.println(getMountTxt(onMount));
			
			System.out.print(prefix + "On dismount: ");
			System.out.println(getMountTxt(onDismount));
			
			System.out.print(prefix + "Override walls: ");
			System.out.println(getOverrideTxt(overrideWalls));

			System.out.print(prefix + "Blocked by: ");
			System.out.println(getOverrideTxt(blockedBy));

			System.out.print(prefix + "Mount from: ");
			System.out.println(getOverrideTxt(mountFrom));
			
			System.out.print(prefix + "Dismount to: ");
			System.out.println(getOverrideTxt(dismountTo));
			
			System.out.println(prefix + "Elevation: " + elevation);
			
			System.out.println(prefix + "Speical Properties: ");
			if (passThroughWalls())
				System.out.println(prefix + " : " + "PassThroughWalls");
			if (passThroughNPCs())
				System.out.println(prefix + " : " + "PassThroughNPCs");
			if (enableNPCActivation())
				System.out.println(prefix + " : " + "EnableNPCActivation");
			if (enableDoorUse())
				System.out.println(prefix + " : " + "EnableDoorUse");
			if (dontHideLeader())
				System.out.println(prefix + " : " + "DontHideLeader");
			if (dontHideParty())
				System.out.println(prefix + " : " + "DontHideParty");
			if (dismountPlusOneSpace())
				System.out.println(prefix + " : " + "DismountPlusOneSpace");
			if (passWallsWhileDismounting())
				System.out.println(prefix + " : " + "PassWallsWhileDismounting");
			if (disableFlyingShadow())
				System.out.println(prefix + " : " + "DisableFlyingShadow");
		}
		
		private String getButtonTxt(int code) {
			if (code==-2)
				return "disabled";
			else if (code==-1)
				return "menu";
			else if (code==0)
				return "dismount";
			else
				return "Run plotscript: " + code;
		}
		
		private String getMountTxt(int code) {
			if (code==0)
				return "(N/A)";
			else if (code > 0)
				return "show textbox: " + code;
			else
				return "plotscript trigger (" + -code + ")";
		}
		
		private String getOverrideTxt(int code) {
			switch (code) {
			case 0:
				return "default";
			case 1:
				return "vehicle A";
			case 2:
				return "vehicle B";
			case 3:
				return "vehicles A and B";
			case 4:
				return "vehicles A or B";
			case 5:
				return "not vehicle A";
			case 6:
				return "not vehicle B";
			case 7:
				return "neither vehicles A nor B";
			case 8:
				return "everywhere";
			default:
				return "<invalid code: " + code + ">";
			}
		}
	}
	
	
	class Color {
		private String color;
		public Color(int r, int g, int b) {
			color = getHex(r) + getHex(g) + getHex(b);
		}
		
		private String getHex(int val) {
			//Normalize over 0x00 to 0xFF
			String s = Integer.toHexString(val*255/63);
			if (s.length()==1)
				return "0" + s;
			
			return s;
		}
		
		public String getHex() {
			return color;
		}
	}

	
	class Font {
		private String[] rows;
		private boolean emptyChar;
		
		public Font(byte[] columns) {
			emptyChar = true;
			rows = new String[columns.length+2];
			int mask = 0x1;
			for (int row=0; row<rows.length; row++) {
				//Ceiling rows?
				if (row==0 || row==rows.length-1) {
					rows[row] = "--";
					for (int i=0; i<columns.length; i++)
						rows[row] = rows[row] + "-";
					continue;
				}
				
				//Else, data!
				rows[row] = "|";
				for (int col=0; col<columns.length; col++) {
					if ((columns[col]&mask)!=0) {
						rows[row] = rows[row] + "*";
						emptyChar = false;
					} else
						rows[row] = rows[row] + " ";
				}
				rows[row] = rows[row] + "|";
				
				//Increment
				mask <<= 1;
			}
		}
		
		//Assume square fonts
		public String[] getRows() {
			return rows;
		}
		
		public boolean isEmpty() {
			return emptyChar;
		}
	}	
}
