package ohrrpgce.tool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import ohrrpgce.data.loader.LumpParser;
import ohrrpgce.data.loader.PictureParser;


/**
 * Static class provided to convert an RPG file to an XRPG file.
 * Essentially, the algorithm to create this pseudo-lump does so in this way:
 * 	X	1) Unlump everything (may be inlined)
 *	X	2) .PT4, .PT6 become .PT4_X (X=ID) (because it uses alt. palettes)
 *	X	3) .PT0, .PT1, .PT2, .PT3, .PT5 can remain as-is (they don't get too big)
 *	X	4) .FNT to a _FNT.PNG
 *	X	5) .SAY in a better format: explicit newlines instead of reserved space, terminate with '\0'
 *	X	6) .TIL becomes _TIL_X.PNG (X=ID)
 *	X	7) .MXS becomes _MXS_X.PNG (X=ID)
 *	X	8) ARCHINYM.LMP, BROWSE.TXT -> HEADER.LMP. (Allow for expansion; ignore remaining bytes)
 *	X	9) BINSIZE.BIN -> drop
 *	X	10) .MAP & .MN & .D## & .DOX & .T## & .P## & .E## & .L## & .N## -> ###.MAP (just re-lump as .T, .P, etc.)
 *	X	11) Within ###.MAP, .D##& .DOX combine to form .D
 *  X   12) Map re-fixes:
 *  X    	a) .MN becomes a null-terminated string
 *  X    	b) .E uses Huffman coding, since maps us. only have 1-2 formation sets, and lots 0's.
 *  X    		i) Special case: if only one entry, don't encode it
 *  X    	c) .P sets 0 to be "0", and and 1XXXXXXXX to be "not 0"
 *  X    	d) .N removes the extra 1920 unused bytes at the end, also remove multiple all-zero entries
 *  X    	e) .L removes null IDs.
 *  X 		f) .L is absorbed into .N. After int 14, there follows a byte for the number of locations of that NPC.
 *  X			i)  Then, there follows (int=xPos, int=yPos, byte=walking frame
 *  X			ii) Change speed setting to actual speed. Otherwise, no further compression is needed (it's insignificant, compared to .T)
 *  X                       a) Note that npc pictures are incremented by 1. So an id of zero now means "no picture"
 *  X			iv) To accomodate null NPCs; add an "NPCID" field. One byte.
 *  X			v) It'll also be necessary to have as the first byte in the lump the number of NPCs.
 *  X           vi) NPCs' y-locations are decreased by 1, to save time later.
 *  X	13) DT0 is renamed to .HRO, and compressed as so:
 *  X		a The second byte of the file is now a combination of 4 scaling factors, all -1,  bit(11223344); 
 *  X		b) All VSTRs are replaced with \0 terminated strings (Name, Spell list names)
 *  X		c) Offsets 17-22 (sprites/paletes and default level/weapon) are now one byte.
 *  X			i) If default level (21) is 0xFFFF, it becomes 0xFF. This represents "party average"
 *  X		d) Stats (25-46, which excludes HP) are scaled according to the scaling factors:
 *  X			i) scale factor 1 scales offset 25; factor 2 scales offset 26 (MP)
 *  X			ii) scale factors 3 & 4 scale every odd/even pair thereafter (level0, level99)
 *  X		e) Spell data(47-238) is in bytes, not ints. Also, there's a 3-byte header:
 *  X			i) bit(XXXX1111 12222233 33344444) has the number of spells in each list (1-4)
 *  X				+ Note that NO entries of [0,0] are allowed; James increments the spell ID by one, which helps. 
 *  X			ii) Any list with zero spells does not have an entry, obviously.
 *  X			iii) The _name_ of any list with zero spells still exists, however.
 *  X		f) Heroes now have only four bitset bytes, not 6
 *  X		g) Unused offsets(239, 287, 301-317) are removed
 *  X		g) Offets (288-296) are now one byte
 *  X		h) Cut out heroes that have no stats. If the length of a hero's name is 0xFF, that hero is not used.
 *  X			i) First byte of the file is now "number of heroes" (to read, even if null)
 *  X	14) SEZ can use Run-Length encoding for zeroes.
 *  X		a) Also, remove non-necessary newlines.
 *  X		b) use bit(BXXX XXXX), where X is the next string of bytes which are zero, and B(0) says to re-enter run-length mode after this.
 *  X			i) If B is 1, then the next byte is the number of ints to read after this.
 *  X		c) This all starts after the text lines. However, we must:
 *  X			i)put both Strings(15) and the choice bitset after the text
 *  X			ii) Remove all non-used bytes/ints
 *  X		d) Also, split into a new file (.SEZ, _1.SEZ, _2.SEZ) every 50 lines, so we don't spend forever loading.
 *  X   15) ATTACK.BIN and .DT6 are just brute-forced into .ATK. Fields are re-organized as necessary to align nicely on byte boundaries.
 *  X       a) All strings are moved to the top of the file.
 *  X       b) "Tag Check #1" (and 2) are optional, and there's a bit for them. 
 *  X       c) Just check the wiki for more info (http://gilgamesh.hamsterrepublic.com/wiki/ohrrpgce/index.php/ATK)
 *  X	16) EFS becomes EFX
 *  X		a) Starts with a byte; num_records
 *  X		b) All fields become bytes
 *  X		c) Lump begins with 1 byte counting how many records exist
 *  X	17) FOR becomes BFF
 *  X		a) Bkgrd, battle music, anim frames, anim speed are first
 *  X		b) All above sans anim speed are bytes
 *  X		c) Then, 1 byte for number of enemies, 3 bytes per enemy
 *  X	18) DT1 becomes FOE
 *  X		a) See wiki... lots of small changes 
 *
 * @author Seth N. Hetu
 */
public class RPG2XRPG {

	private static final Pattern mapSegMatcher = Pattern.compile("[^.]+\\.([TPEDLN])([0-9][0-9])");
	private static final Pattern mapSegMatcherOver100 = Pattern.compile("([0-9][0-9][0-9])\\.([TPEDLN])");
	private static final int[] spellNameStartingOffsets = new int[]{243*2, 254*2, 265*2, 276*2};
	
	
	private static final int GEN_MAP_INFO = 0;
	private static final int MAP_NAME = 1;
	private static final int DOOR_IDS = 2;
	private static final int DOOR_LINKS  = 3;
	private static final int TILES = 4;
	private static final int PASSABILITY = 5;
	private static final int ENEMY_MAP = 6;
	private static final int NPC_LOCATIONS = 7;
	private static final int NPC_DATA = 8;
	private static final int ATTACK_DATA = 9;
	private static final int FORMATION_SETS = 10;
	private static final int BATTLE_FORMATIONS = 11;
	private static final int ENEMY_STATS = 12;
	private static final int MAP_SEGMENT_MAX = 13;

	//Tilesets, maps, master palette, mxs backdrops
	private static ArrayList/*<int[][]>*/ mapLumps = new ArrayList/*<int[][]>*/();
	private static ArrayList/*<int[][]>*/ tilesets = new ArrayList/*<int[][]>*/();
	private static ArrayList/*<int[][]>*/ backdrops = new ArrayList/*<int[][]>*/();
	private static int[] masterPalette;
	private static String tilesetLumpName;
	private static String mxsLumpName;

	//Header
	private static String internalName;
	private static String customVersion;
	private static String longName;
	private static String aboutLine;
	
	//Attack data
	private static byte[] attBin;
	private static byte[] dt6;
	private static String dt6Name;
	
	//Gathered data
	private static double[][] mapCompression; //[avg before, avg after][sub-lump (MN, E, etc)]
	private static double numberOfMaps;
	private static double heroBytesBefore;
	private static double heroBytesAfter;
	private static double sayBytesBefore;
	private static double sayBytesAfter;
	private static double attBytesBefore;
	private static double attBytesAfter;
	private static double formSetBytesBefore;
	private static double formSetBytesAfter;
	private static double battleFormBytesBefore;
	private static double battleFormBytesAfter;
	private static double enemyBytesBefore;
	private static double enemyBytesAfter;
	private static ArrayList/*<Boolean>*/ walkaboutTracer;
	private static ArrayList/*<int[]>*/ heroPicIndexed;
	private static ArrayList/*<int[]>*/ heroIndices; //[pic, pal]
	private static ArrayList/*<int[]>*/ savedPalettes; //SHOULD store palette info.
	
	private static OutputStreamCreator outGetter;
	private static String outDirPath;

	private static int getPTSegmentSize(int ptLumpID) {
		switch(ptLumpID) {
		case 0:
			return 5120;
		case 1:
			return 578;
		case 2:
			return 1250;
		case 3:
			return 3200;
		case 4:
			return 1600;
		case 5:
			return 576;
		case 6:
			return 3750;
		default:
				return -1;
		}
	}

	private static int char2Seg(char c) {
		if (c=='D')
			return DOOR_LINKS;
		else if (c=='P')
			return PASSABILITY;
		else if (c=='E')
			return ENEMY_MAP;
		else if (c=='L')
			return NPC_LOCATIONS;
		else if (c=='N')
			return NPC_DATA;
		else if (c=='T')
			return TILES;
		else {
			System.out.println("ERROR: Invalid map tag: " + c + " (" + (int)c + ")");
			return -1;
		}
	}
	

	/**
	 * Converts an RPG file to the "XPRG" format.
	 * @param inputFile The .RPG file to convert
	 * @param outputDirectory The output directory; all files will be placed here.
	 * @param clearDir If true, clear the output directory before moving files into it.
	 * @return The long name of the game (for convenience)
	 */
	public static String convert(File inputFile, File outputDirectory, boolean clearDir) {
		outputDirectory.mkdir();
		File[] toDelete = outputDirectory.listFiles();
		if (toDelete.length>0) {
			System.out.println("Listed directory contains files!");
		}
		if (clearDir) {
			//for (File f : toDelete)
			for (int i=0; i<toDelete.length; i++)
				toDelete[i].delete();
		}

		if (!outputDirectory.isDirectory())
			System.out.println("Warning: output should be to a directory.");
		
		RPG2XRPG.outDirPath = outputDirectory.getAbsolutePath();

		return RPG2XRPG.convert(inputFile, new OutputStreamCreator() {
			public OutputStream getOutputStream(String lumpName) {
				try {
					return 	new FileOutputStream(getOutputFile(lumpName));
				} catch (FileNotFoundException ex) {
					System.out.println("File not found: " + lumpName);
					return null;
				}
			}
			public File getOutputFile(String lumpName) {
				return 	new File(outDirPath+ "\\" + lumpName);
			}
			public void closeOutputStream(OutputStream os) throws IOException {
				os.close();
			}
		});
	}
	
	
	
	/**
	 * Converts an RPG file to the "XPRG" format.
	 * @param inputFile The .RPG file to convert
	 * @param outGetter An interface to help tell the translator where to save each lump. Helps when converting to, say, a zip file (prevents creation of a temporary directory.)
	 * @return The long name of the game (for convenience)
	 */
	public static String convert(File inputFile, OutputStreamCreator outGetter) {
		RPG2XRPG.outGetter = outGetter;
	
		FileInputStream input = null;
		try {
			input = new FileInputStream(inputFile);
		} catch (FileNotFoundException ex) {
			System.out.println("Error: Couldn't find input file: " + inputFile.getAbsolutePath());
			return "";
		}

		long currLumpStart = 0;
		long lastLumpSize = 0;
		for (;;) {
			//Update start location
			currLumpStart += lastLumpSize;

			String lumpName = LumpParser.readNTString(input);
			if (lumpName.length() == 0) {
				System.out.println("-------Entire lump has been read--------");
				break;
			}
			
		//	System.out.println(lumpName);

			//Get information about this lump
			currLumpStart += lumpName.length() + 1;
			currLumpStart += LumpParser.PDP_OFFSETS.length;
			lastLumpSize = LumpParser.readPDPWord(input);

			//Hack the name into two bits
			int dotPos = lumpName.indexOf('.');
			if (dotPos == -1) {
				//Error reading string...
				System.out.print("Error: expected lump name, not [");
				for (int i = 0; i < lumpName.length(); i++) {
					if (i != 0)
						System.out.print(", ");
					System.out.print("0x");
					if (lumpName.charAt(i) < 0x10)
						System.out.print("0");
					System.out.print(Integer.toHexString(lumpName.charAt(i)));
					System.out.print("(" + lumpName.charAt(i) + ")");
				}
				System.out.println("]");
				return "";
			}


			// Match the name; conditionally do something with it.
			if (lumpName.endsWith(".PT0")) {
				input = splitPtLump(lumpName, input, getPTSegmentSize(0), lastLumpSize);
			} else if (lumpName.endsWith(".PT1")) {
				input = splitPtLump(lumpName, input, getPTSegmentSize(1), lastLumpSize);
			} else if (lumpName.endsWith(".PT2")) {
				input = splitPtLump(lumpName, input, getPTSegmentSize(2), lastLumpSize);
			} else if (lumpName.endsWith(".PT3")) {
				input = splitPtLump(lumpName, input, getPTSegmentSize(3), lastLumpSize);
			} else if (lumpName.endsWith(".PT4")) {
				input = splitPtLump(lumpName, input, getPTSegmentSize(4), lastLumpSize);
			} else if (lumpName.endsWith(".PT6")) {
				input = splitPtLump(lumpName, input, getPTSegmentSize(6), lastLumpSize);
			} else if (lumpName.endsWith(".FNT")) {
				input = font2png(lumpName, input);
			} else if (lumpName.endsWith(".DT0")) {
				input = compressDTHeroLump(lumpName, input, lastLumpSize);				
			} else if (lumpName.endsWith(".SAY")) {
				input = compressSay(lumpName, input, lastLumpSize);
			} else if (lumpName.endsWith(".TIL")) {
				input = splitTilesets(lumpName, input, lastLumpSize);
			} else if (lumpName.endsWith(".MXS")) {
				input = splitBackdrops(lumpName, input, lastLumpSize);
			} else if (lumpName.endsWith(".MAS")) {
				input = readAndCopyMasterPalette(lumpName, input, lastLumpSize);
			} else if (lumpName.endsWith(".PAL")) {
				input = readAndCopyOtherPalettes(lumpName, input, lastLumpSize);
			} else if (lumpName.endsWith(".DT6")) {
				dt6 = new byte[(int)lastLumpSize];
				dt6Name = lumpName;
				try {
					input.read(dt6);
				} catch (IOException ex) {
					System.out.println("Error reading .DT6: " + ex.toString());
					continue;
				}
				
				if (attBin != null)
					compressAttack(lumpName);
			} else if (lumpName.equals("ATTACK.BIN")) {
				attBin = new byte[(int)lastLumpSize];
				try {
					input.read(attBin);
				} catch (IOException ex) {
					System.out.println("Error reading ATTACK.BIN: " + ex.toString());
					continue;
				}
				
				if (dt6 != null)
					compressAttack(dt6Name);
			} else if (lumpName.equals("BROWSE.TXT")) {
				byte[] browse = new byte[(int)lastLumpSize];
				try {
					input.read(browse);
				} catch (IOException ex) {
					System.out.println("Error reading BROWSE.TXT: " + ex.toString());
					continue;
				}

				for (int i=0; i<2; i++) {
					StringBuilder sb = new StringBuilder();
					int len=-1;
					for (int pos=40*i; pos<40*(i+1); pos++) {
						if (pos==40*i)
							len = browse[pos];
						else if (pos==40*i+1)
							len += browse[pos]*0x100;
						else {
							if (len==0)
								break;
							sb.append(browse[pos]);
							len--;
						}
					}

					if (i==0)
						longName = sb.toString();
					else if (i==1)
						aboutLine = sb.toString();
				}

			} else if (lumpName.equals("ARCHINYM.LMP")) {
				byte[] arch = new byte[(int)lastLumpSize];
				try {
					input.read(arch);
				} catch (IOException ex) {
					System.out.println("Error reading ARCHINYM.LMP: " + ex.toString());
					continue;
				}

				StringBuilder sb = new StringBuilder();
				int pos=0;
				for (;pos<arch.length; pos++) {
					if (arch[pos] == 0x0D)
						break;
					sb.append(arch[pos]);
				}
				internalName = sb.toString();
				sb = new StringBuilder();
				pos+=2; //Past the newline; on to the next line

				for (;pos<arch.length; pos++) {
					if (arch[pos] == 0x0D)
						break;
					sb.append(arch[pos]);
				}
				customVersion = sb.toString();
			} else if (lumpName.equals("BINSIZE.BIN")) {
				System.out.println("Dropping the BINSIZE.BIN lump.");
				try {
					input.read(new byte[(int)lastLumpSize]);
				} catch (IOException ex) {
					System.out.println("   + Error doing that.");
				}
			} else if (lumpName.endsWith(".DOR")) {
				System.out.println("Dropping the .DOR lump.");
				try {
					input.read(new byte[(int)lastLumpSize]);
				} catch (IOException ex) {
					System.out.println("   + Error doing that.");
				}
			} else if (lumpName.endsWith(".MAP")) {
				input = combineMapData(GEN_MAP_INFO, input, -1, lastLumpSize);
			} else if (lumpName.endsWith(".MN")) {
				//System.out.println("    Map Name");
				input = combineMapData(MAP_NAME, input, -1, lastLumpSize);
			} else if (lumpName.endsWith(".DOX")) {
				input = combineMapData(DOOR_IDS, input, -1, lastLumpSize);
			} else if (lumpName.endsWith(".EFS")) {
				input = compressFormationSets(lumpName, input, lastLumpSize);
			} else if (lumpName.endsWith(".FOR")) {
				input = compressBattleFormations(lumpName, input, lastLumpSize);
			} else if (lumpName.endsWith(".DT1")) {
				input = compressDTEnemies(lumpName, input, lastLumpSize);
			} else {
				Matcher mat1 = mapSegMatcher.matcher(lumpName);
				Matcher mat2 = mapSegMatcherOver100.matcher(lumpName);
				if (mat1.matches()) {
					int seg = char2Seg(mat1.group(1).charAt(0));
					int mapID = Integer.parseInt(mat1.group(2));
					input = combineMapData(seg, input, mapID, lastLumpSize);
				} else if (mat2.matches()) {
					int seg = char2Seg(mat1.group(2).charAt(0));
					int mapID = Integer.parseInt(mat1.group(1));
					input = combineMapData(seg, input, mapID, lastLumpSize);
				} else {
					//Just copy it over
					input = copyLump(lumpName, input, lastLumpSize);
				}
			}
		}

		//Free space, if needed
		try {
			input.close();
		} catch (IOException ex) {
			System.out.println("Couldn't close input file");
			return "";
		}

		//Combine necessary data
		postProcess(inputFile);
		
		//Output specifics
		System.out.println("Space saved: ");
		System.out.println("-----------------------------------------");
		double totalBefore = 0;
		double totalAfter = 0;
		mapCompression[0][DOOR_IDS] = heroBytesBefore;
		mapCompression[1][DOOR_IDS] = heroBytesAfter;
		mapCompression[0][NPC_LOCATIONS] = sayBytesBefore;
		mapCompression[1][NPC_LOCATIONS] = sayBytesAfter;
		mapCompression[0][ATTACK_DATA] = attBytesBefore;
		mapCompression[1][ATTACK_DATA] = attBytesAfter;
		mapCompression[0][FORMATION_SETS] = formSetBytesBefore;
		mapCompression[1][FORMATION_SETS] = formSetBytesAfter;
		mapCompression[0][BATTLE_FORMATIONS] = battleFormBytesBefore;
		mapCompression[1][BATTLE_FORMATIONS] = battleFormBytesAfter;
		mapCompression[0][ENEMY_STATS] = enemyBytesBefore;
		mapCompression[1][ENEMY_STATS] = enemyBytesAfter;
		//for (MAP_SEGMENT ms : MAP_SEGMENT.values()) {
		for (int ms=0; ms<MAP_SEGMENT_MAX; ms++) {
			if (ms == (DOOR_IDS))
				System.out.print("DT0 (Heroes):\t");
			else if (ms == (NPC_DATA))
				System.out.print(".N + .L:\t");
			else if (ms == (NPC_LOCATIONS))
				System.out.print(".SAY:\t");
			else if (ms == (ATTACK_DATA))
				System.out.print("ATTACK.BIN + .DT6:\t");
			else if (ms == (FORMATION_SETS))
				System.out.print(".EFS:\t");
			else if (ms == (BATTLE_FORMATIONS))
				System.out.print(".FOR:\t");
			else if (ms == (ENEMY_STATS))
				System.out.print(".DT1 (Enemies):\t");
			else
				System.out.print(ms + ":\t");
			System.out.println((int)(mapCompression[0][ms]) + "-->" + (int)(mapCompression[1][ms]) + "\t(" + floatPrint((mapCompression[0][ms]-mapCompression[1][ms])*100/mapCompression[0][ms]) + "%)");
			
			totalBefore += mapCompression[0][ms];
			totalAfter += mapCompression[1][ms];
		}
		System.out.println("-----------------------------------------");
		System.out.println("TOTAL: \t" + (int)(totalBefore) + "-->" + (int)(totalAfter) + "\t(" + floatPrint((totalBefore-totalAfter)*100/totalBefore) + "%)");
		
		return longName;

	}

	
	private static String floatPrint(double val) {
		return (int)val + "." + (int)((Math.abs(val)*100) - ((int)Math.abs(val))*100); 
	}
	
	

	//Pad an integer with "pad" number of zeroes
	private static String pad(int num, int pad) {
		StringBuilder res = new StringBuilder();
		for (int len = Integer.toString(num).length(); len<pad; len++)
			res.append("0");
		res.append(num);

		return res.toString();
	}

	//Encode the value in PDP_endian notation
	private static byte[] getPDP(int val) {
		return new byte[] {(byte)((val&0xFF0000)/0x10000), (byte)((val&0xFF000000)/0x1000000), (byte)((val&0xFF)), (byte)((val&0xFF00)/0x100)};
	}

	private static FileInputStream splitPtLump(String lumpName, FileInputStream input, int segmentSize, long numBytes) {
		int id=0;
		//Track which lumps are entirely empty
		if (lumpName.endsWith(".PT4"))
			walkaboutTracer = new ArrayList/*<Boolean>*/();
		if (lumpName.endsWith(".PT0"))
			heroPicIndexed = new ArrayList/*<int[]>*/();

		while (numBytes >= segmentSize) {
			//Read/Write
			byte[] buf = new byte[segmentSize];
			try {
				input.read(buf);
				boolean write = true;
				if (lumpName.endsWith(".PT4")) {
					boolean empty = true;
					for (int i=0; i<buf.length; i++) {
						if (buf[i] != 0) {
							empty = false;
							break;
						}
					}
					walkaboutTracer.add(new Boolean(empty));
					if (empty) {
						System.out.println("Note: Walkabout number " + walkaboutTracer.size() + " is empty!");
						write = false;
					}
					
				}
				
				//Are we dealing with the PT0 lump?
				if (lumpName.endsWith(".PT0")) {
					//Cache the first frame for use in our HQ2X algorithm.
					int width = PictureParser.PT_HERO_SIZES[0];
					int height = PictureParser.PT_HERO_SIZES[1];
					int[] thisFrame = new int[width*height];
					
					//Load
					int secondHalf = -1;
					int currDuplex = 0;
					for (int x=0; x<width; x++) {
						for (int y=0; y<height; y++) {
							// Load the current pixel from our buffer, or from the stream
							int currColor = secondHalf;
							if (secondHalf == -1) {
								// In the specification, odd-numbered finalbytes are ignored.
								if (x==width-1 && y==height-1)
									continue;
									
								// Read the next two colors, save one for later.
								secondHalf = buf[currDuplex++];
								currColor = (secondHalf&0xF0)/0x10;
								secondHalf &= 0xF;
							} else
								secondHalf = -1;
								
							// Interpret the pixel later; for now, just store the index.					
							thisFrame[y*width+x] = currColor;
						}
					}
					
				/*	for (int i=0; i<thisFrame.length; i++) {
						System.out.println("This frame pix: " + Integer.toHexString(thisFrame[i]));
					}*/

					heroPicIndexed.add(thisFrame);
				}
				
				if (write) {
					byte[] res = new byte[buf.length];
					for (int i=0; i<res.length; i++)
						res[i] = (byte)buf[i];
					
					OutputStream os = outGetter.getOutputStream(lumpName + "_" + id);
					os.write(res);
					outGetter.closeOutputStream(os);
				}
			} catch (Exception ex) {
				System.out.println("Error reading/writing PT lump: " + ex.toString());
				return input;
			}

			//Increment
			numBytes -= segmentSize;
			id++;
		}

		return input;
	}



	private static FileInputStream font2png(String lumpName, FileInputStream input) {
		lumpName = lumpName.replace('.', '_') + ".PNG";

		//BSaveHeader
		try {
			input.read(new byte[7]);
		} catch (IOException ex) {
			System.out.println("Error skipipng font's BSave: " + ex.toString());
			return input;
		}

		int cols = 16;
		int rows = 16;
		int PIX_PER_CHAR = 8;

		//Horribly slow, but I can't seem to get indexed pictures to work.
		BufferedImage img = new BufferedImage(cols*PIX_PER_CHAR, rows*PIX_PER_CHAR, BufferedImage.TYPE_INT_RGB);
		for (int y=0; y<rows; y++) {
			for (int x=0; x<cols; x++) {
				//For this letter...
			//	int letterID = x + y*cols;

				for (int pixX=0; pixX<PIX_PER_CHAR; pixX++) {
					int currByte = -1;
					try {
						currByte = input.read();
					} catch (IOException ex) {
						System.out.println("Error reading font: " + ex.toString());
						return input;
					}
					int currLoc = 1;
					for (int pixY=0; pixY<PIX_PER_CHAR; pixY++) {
						if ((currByte&currLoc)==0)
							img.setRGB((x*PIX_PER_CHAR+pixX), (y*PIX_PER_CHAR+pixY), 0);
						else
							img.setRGB((x*PIX_PER_CHAR+pixX), (y*PIX_PER_CHAR+pixY), 0xFFFFFF);

						currLoc *= 2;
					}
				}
			}
		}


		//Save the image
		File path = outGetter.getOutputFile(lumpName);
		try {
			ImageIO.write(img, "png", path);
			System.out.println("Saved font");
		} catch (IOException iex) {
			System.out.println("Error saving image: " + iex.toString());
		}

		return input;
	}

	
	private static int getAttackInt(int recordID, int idOfInt) {
		byte[] resArr = null;
		int intOffset = 0;
		int recordSize = 0;
		if (idOfInt < 40) {
			//It's in dt6
			resArr = dt6;
			intOffset = idOfInt*2;
			recordSize = 40*2;
		} else {
			//It's in ATTACK.BIN
			resArr = attBin;
			intOffset = (idOfInt-40)*2;
			recordSize = 60*2;
		}
		
		return resArr[recordID*recordSize + intOffset] + 0xFF*resArr[recordID*recordSize + intOffset + 1];
	}
	

	private static void compressAttack(String lumpName) {
		//Data collection
		attBytesBefore = dt6.length + attBin.length;
		attBytesAfter = 0;
		
		//Prepare our lump
		ArrayList/*<byte[]>*/ allAttacks = new ArrayList/*<byte[]>*/();
		if (attBin.length%120!=0 || dt6.length%80!=0 || dt6.length/80!=attBin.length/120) {
			System.out.println("Conflicting DT6 and ATTACK.BIN sizes: " + dt6.length + ","+ attBin.length );
			System.exit(1);
		}
		for (int id=0; id<attBin.length/120; id++) {
			//Compute the space our byte[] array should reserve for tags
			int tagSizes = 1;
			boolean tagCond1IsNever = getAttackInt(id, 60) == 0;
			boolean tagCheck1IsNone = getAttackInt(id, 61) == 0;
			boolean tagCond2IsNever = getAttackInt(id, 63) == 0;
			boolean tagCheck2IsNone = getAttackInt(id, 64) == 0;
			if (!tagCond1IsNever) { //If not "Never"
				if (tagCheck1IsNone) { //If "None"
					tagSizes += 2;
				} else {
					tagSizes += 4;
				}
			}
			if (!tagCond2IsNever) { //If not "Never"
				if (tagCheck2IsNone) { //If "None"
					tagSizes += 2;
				} else {
					tagSizes += 4;
				}
			}
			
			//Compute the space our byte[] array should reserve for strings -save the strings, too.
			char[] attName = new char[getAttackInt(id, 24)];
			for (int i=0; i<attName.length; i++) {
				attName[i] = (char)(0xFF&getAttackInt(id, 26+i)); //First int is unused
			}
			byte[] attCapt = new byte[getAttackInt(id, 37)];
			for (int i=0; i<attCapt.length; i++) {
				//Border-split
				if (i<4)
					attCapt[i] = dt6[id*40*2 + 38*2 + i];
				else
					attCapt[i] = attBin[id*60*2 + i-4];
					//attCapt[i] = dt6[id*40*2 + 37*2 + i];
//				attCapt[i] = (char)(0xFF&getAttackInt(id, 38+i));
			}
			char[] attDesc = new char[getAttackInt(id, 73)];
			for (int i=0; i<attDesc.length; i++) {
				attDesc[i] = (char)attBin[id*60*2 + (74-40)*2 + i];
			}
			
			// Create our record
			byte[] res = new byte[attName.length + 1 + attCapt.length + 1
					+ attDesc.length + 1 + 31 + tagSizes];
			int currID = 0;

			// Name
			for (int i = 0; i < attName.length; i++) {
				res[currID++] = (byte) attName[i];
			}
			res[currID++] = 0;

			// Caption
			for (int i = 0; i < attCapt.length; i++) {
				res[currID++] = (byte) attCapt[i];
			}
			res[currID++] = 0;

			// Description
			for (int i = 0; i < attDesc.length; i++) {
				res[currID++] = (byte) attDesc[i];
			}
			res[currID++] = 0;

			// General Info
			int nextDatum;
			{
				nextDatum = (0xFF & getAttackInt(id, 1)); // Palette
				res[currID++] = (byte) nextDatum;
			}
			{
				nextDatum = (((0x7FF & getAttackInt(id, 0)) * 0x20) // Picture
				+ (0x1F & getAttackInt(id, 7))); // Base Attack
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
			}
			{
				nextDatum = (((0xF & getAttackInt(id, 14)) * 0x10) // Attacker Animation
							+ (0xF & getAttackInt(id, 15))); // Attack Animation
				res[currID++] = (byte) nextDatum;
			}
			{
				nextDatum = (((0x3 & getAttackInt(id, 2)) * 0x4000) // Animation pattern
							+ (0x3FFF & getAttackInt(id, 57))); // Caption delay
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
			}
			{
				nextDatum = ((0x7FFF & (LumpParser
						.convertTwosComplementInt(getAttackInt(id, 2)) + 1)) * 0x2); // Caption Display Time + 1
						// Wasted
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
			}
			{
				nextDatum = (((0xF & getAttackInt(id, 3)) * 0x10) // Target Class
							+ ((0x7 & getAttackInt(id, 4)) * 0x2)); // Target Setting
							// Wasted
				res[currID++] = (byte) nextDatum;
			}
			{
				nextDatum = (((0xF & getAttackInt(id, 18)) * 0x10) // Target stat
							+ (0xF & getAttackInt(id, 58))); // Base defense stat
				res[currID++] = (byte) nextDatum;
			}
			{
				nextDatum = (((0x7FF & getAttackInt(id, 16)) * 0x20) // Attack Delay
							+ (0x1F & getAttackInt(id, 17))); // Number of hits
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
			}
			{
				nextDatum = (((0x7 & getAttackInt(id, 5)) * 0x200000) // Damage equation
						+ ((0x7 & getAttackInt(id, 6)) * 0x40000) // Aim Math
						+ ((0x7F & getAttackInt(id, 13)) * 0x800) // Chain rate
						+ (0x7FF & getAttackInt(id, 12))); // Chain-to-attack
				res[currID++] = (byte) ((0xFF0000 & nextDatum) / 0x10000);
				res[currID++] = (byte) ((0xFF00 & nextDatum) / 0x100);
				res[currID++] = (byte) (nextDatum & 0xFF);
			}
			{
				nextDatum = (((0x7FF & (LumpParser
						.convertTwosComplementInt(getAttackInt(id, 11)) + 100)) * 0x20) // Extra Damage % +100
				+ ((0xF8 & getAttackInt(id, 65)) / 0x8)); // Additional Bitsets
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
			}
			{
				nextDatum = getAttackInt(id, 8); // MP Cost
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
			}
			{
				nextDatum = getAttackInt(id, 9); // HP Cost
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
			}
			{
				nextDatum = getAttackInt(id, 10); // Money Cost
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
			}
			//System.out.println("Bitsets: " + new String(attName));
			for (int i = 0; i < 8; i++) {
				nextDatum =  dt6[id*40*2 + 20*2+i];// , 20 + i); // Bitset
				//System.out.println("  " + Integer.toHexString(nextDatum));
				//System.out.println("  " + (nextDatum / 0x100));
				res[currID++] = (byte) (nextDatum & 0xFF);
				//res[currID++] = (byte) (nextDatum / 0x100);
			}
			{
				nextDatum = (0x7 & getAttackInt(id, 60)) * 0x20; // Tag condition 1
				nextDatum += (0x7 & getAttackInt(id, 63)) * 0x4; // Tag condition 2
				if (tagCheck1IsNone)
					nextDatum += 0x2; // No tag check 1
				if (tagCheck2IsNone)
					nextDatum += 0x1; // No tag check 2
				res[currID++] = (byte) (nextDatum & 0xFF);
			}
			if (!tagCond1IsNever) {
				nextDatum = getAttackInt(id, 59); // Tag to set 1
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
				if (!tagCheck1IsNone) {
					nextDatum = getAttackInt(id, 61); // Tag check 1
					res[currID++] = (byte) (nextDatum & 0xFF);
					res[currID++] = (byte) (nextDatum / 0x100);
				}
			}
			if (!tagCond2IsNever) {
				nextDatum = getAttackInt(id, 62); // Tag to set 2
				res[currID++] = (byte) (nextDatum & 0xFF);
				res[currID++] = (byte) (nextDatum / 0x100);
				if (!tagCheck2IsNone) {
					nextDatum = getAttackInt(id, 64); // Tag check 2
					res[currID++] = (byte) (nextDatum & 0xFF);
					res[currID++] = (byte) (nextDatum / 0x100);
				}
			}
			
			allAttacks.add(res);
			attBytesAfter += res.length;
		}
		
		

		// Prepare output - No need to split now...
		try {
			OutputStream os = outGetter.getOutputStream(lumpName.replaceAll("[.]DT6", ".ATK"));
			for (int id = 0; id < allAttacks.size(); id++) {
				byte[] line = (byte[])allAttacks.get(id);
				os.write(line);
			}
			outGetter.closeOutputStream(os);
		} catch (Exception ex) {
			System.out.println("Error creating ATK lump: " + ex.toString());
		}
	}

	
	private static FileInputStream compressDTEnemies(String lumpName, FileInputStream input, long numBytes) {
		//Data collection
		enemyBytesBefore = numBytes;
		enemyBytesAfter = 0;
		
		//Simple data collection
		Hashtable/*<String, Integer>*/ widths = new Hashtable/*<String, Integer>*/();
		int[] bitsetCounters = new int[40];
		
		//Draw out our data; gather information
		ArrayList/*<byte[]>*/ enemies = new ArrayList/*<byte[]>*/();
		ArrayList/*<boolean[]>*/ enemyBitsets = new ArrayList/*<boolean[]>*/();
		while (numBytes >= 320 ) {
			try {
				//Save enemy stats for later (minimize errors, too)
				byte[] stats = new byte[320];
				boolean[] bits = new boolean[40];
				input.read(stats);
				enemies.add(stats);
				enemyBitsets.add(bits);
												
				//Create a custom string to represent this stat's width.
				StringBuilder sb = new StringBuilder();
				String sep = "";
				for (int i=0; i<12; i++) {
					int nextWidth = Integer.toBinaryString(stats[(62+i)*2] + 0x100*stats[(62+i)*2+1]).length();
					sb.append(sep + nextWidth);
					sep = ":";
				}
				if (!widths.containsKey(sb.toString()))
					widths.put(sb.toString(), new Integer(1));
				else
					widths.put(sb.toString(), new Integer(((Integer)widths.get(sb.toString())).intValue()+1));
				
				//Store bitset patterns as well.
				// -forget this, we're hard-coding it. Stupid reversed-byte-width-bitsets
				int counter=0;
				int currByte = 0;
				for (int byteID=0; byteID<5; byteID++) {
					if (byteID<4)
						currByte = stats[74*2 + byteID];
					else
						currByte = (stats[74*2+7]>>6) + (stats[74*2+8]<<2); 
					for (int and=1; and<0xFF; and<<=1) {
						bits[counter] = ((currByte&and)!=0);
						bitsetCounters[counter] = bits[counter]?bitsetCounters[counter]+1:bitsetCounters[counter]-1;
						counter++;
					}
				}
				if (counter != 40)
					throw new RuntimeException("Bad final counter: " + counter);
			} catch (IOException ex) {
				System.out.println("Error reading DT1 lump: " + ex.getMessage());
				System.exit(1);
			}
			
			numBytes -= 320;
		}
		
		//Compute (some) header stats
		String bestString = "";
		int[] commonWidths = new int[12];
		boolean[] skipBits = new boolean[40];
		int highestCount = -1;
		Object[] keySet = widths.keySet().toArray();
		for (int kI = 0; kI<keySet.length; kI++) {
			String width = (String)keySet[kI];
			if (((Integer)widths.get(width)).intValue() > highestCount) {
				bestString = width;
				highestCount = ((Integer)widths.get(width)).intValue();
			}
		}
		System.out.println("Common widths: " + bestString + "  appeared " + highestCount + " times.");
		String[] matches = bestString.split(":");
		if (matches.length != commonWidths.length)
			throw new RuntimeException("ERROR: String does not have " + commonWidths.length + " groups: " + bestString);
		for (int i=0; i<commonWidths.length; i++)
			commonWidths[i] = Integer.parseInt(matches[i])-1;
		
		//Our result variables
		byte[] header = new byte[16];
		int headIndex =  0;
		ArrayList/*<byte[]>*/ records = new ArrayList/*<byte[]>*/();
		
		//Compute remaining header stats; save the header
		for (int i=0; i<commonWidths.length; i+=2) {
			int nextByte = 0x10*commonWidths[i] + commonWidths[i+1];
			header[headIndex++] = (byte)nextByte;
		}
		for (int bit=0; bit<bitsetCounters.length; bit+=4) {
			int mult = 0x80;
			int nextByte = 0;
			for (int val=0; val<4; val++) {
				int bC = bitsetCounters[bit+val];
				//System.out.println(bC + ":" + enemies.size() + ":" + Integer.toBinaryString(mult));
				if (Math.abs(bC) == enemies.size()) {
					nextByte |= mult;
					mult >>= 1;
					if (bC>0)
						nextByte |= mult;
					mult >>= 1;
				} else
					mult >>= 2;
			}
			header[headIndex++] = (byte)nextByte;
		}
		if (headIndex != 16) {
			throw new RuntimeException("Bad header size: " + headIndex);
		}
		
		
		//Compute enemy stats & save
		int counter = 0;
		//for (byte[] foe : enemies) {
		for (int fI = 0; fI<enemies.size(); fI++) {
			byte[] foe = (byte[])enemies.get(fI);
			
			//Temp
			ArrayList/*<Byte>*/ enBytes =  new ArrayList/*<Byte>*/();
			
			//Enemy name
			int size = foe[0];
			for (int i=0; i<size; i++) {
				enBytes.add(new Byte((byte)foe[(i+1)*2]));
			}
			enBytes.add(new Byte((byte)0));
			
			//Prepare attack data
			ArrayList/*<Byte>*/ atkRegular = new ArrayList/*<Byte>*/();
			ArrayList/*<Byte>*/ atkDesp = new ArrayList/*<Byte>*/();
			ArrayList/*<Byte>*/ atkAlone = new ArrayList/*<Byte>*/();
			for (int i=0; i<5; i++) {
				int atk = foe[(92+i)*2];
				if (atk!=0)
					atkRegular.add(new Byte((byte)atk));
			}
			for (int i=0; i<5; i++) {
				int atk = foe[(97+i)*2];
				if (atk!=0)
					atkDesp.add(new Byte((byte)atk));
			}
			for (int i=0; i<5; i++) {
				int atk = foe[(102+i)*2];
				if (atk!=0)
					atkAlone.add(new Byte((byte)atk));
			}
			
			//Enemy Picture
			enBytes.add(new Byte((byte)foe[53*2]));
			
			//Next two bytes
			int palette = foe[54*2] + 0x100*foe[54*2+1];
			int picSize = foe[55*2];
			int nextVal = (palette&0x7FF)*0x20 + (picSize&0x3)*0x8 + (atkRegular.size()&0x7);
			enBytes.add(new Byte((byte)(nextVal>>8)));
			enBytes.add(new Byte((byte)(nextVal&0xFF)));
			
			//Next byte
			int thievability = LumpParser.convertTwosComplementInt(foe[17*2] + 0x100*foe[17*2+1])+1;
			nextVal = (atkDesp.size()&0x7)*0x20 + (atkAlone.size()&0x7)*0x4 + (thievability&0x3);  
			enBytes.add(new Byte((byte)nextVal));
			
			//Thievability... if required
			if (thievability>0) {
				//Regular item
				int itemStealPerc = foe[19*2];
				enBytes.add(new Byte((byte)itemStealPerc));
				if (itemStealPerc>0) {
					int itemToSteal = foe[18*2];
					enBytes.add(new Byte((byte)itemToSteal));
				}
				
				//Rare item
				int rareStealPerc = foe[21*2];
				enBytes.add(new Byte((byte)rareStealPerc));
				if (rareStealPerc>0) {
					int rareSteal = foe[20*2];
					enBytes.add(new Byte((byte)rareSteal));
				}
			}
			
			//Experience & gold don't compress... blah...
			enBytes.add(new Byte((byte)foe[57*2]));
			enBytes.add(new Byte((byte)foe[57*2+1]));
			enBytes.add(new Byte((byte)foe[56*2]));
			enBytes.add(new Byte((byte)foe[56*2+1]));	
			
			//Get stat widths
			int[] statWidths = new int[12];
			boolean isCommon = true;
			for (int i=0; i<statWidths.length; i++) {
				statWidths[i] = Integer.toBinaryString(foe[(62+i)*2] + 0x100*foe[(62+i)*2+1]).length()-1;
				if (statWidths[i] != commonWidths[i])
					isCommon = false;
			}
			
			//Next byte
			int itemDropPerc = foe[59*2];
			nextVal = 0;
			if (isCommon)
				nextVal = 0x80;
			nextVal |= (0x7F&itemDropPerc);
			enBytes.add(new Byte((byte)nextVal));
			
			//Item reward
			if (itemDropPerc>0)
				enBytes.add(new Byte((byte)foe[58*2]));
			
			//Rare item
			int rareDropPerc = foe[61*2];
			enBytes.add(new Byte((byte)(0x7F&rareDropPerc)));
			if (rareDropPerc>0)
				enBytes.add(new Byte((byte)foe[60*2]));
			
			//Stat widths
			if (!isCommon) {
				for (int i=0; i<statWidths.length; i+=2) {
					nextVal = 0x10*statWidths[i] + statWidths[i+1];
					enBytes.add(new Byte((byte)nextVal));
				}
			}
			
			//Enemy statistics
			ArrayList/*<Integer>*/ currByte = new ArrayList/*<Integer>*/();
			for (int x=0; x<statWidths.length; x++) {
				String val = Integer.toBinaryString(foe[(62+x)*2] + 0x100*foe[(62+x)*2+1]);
				
				//Now, break it down
				//for (char c : val.toCharArray()) {
				for (int cI=0; cI<val.length(); cI++) {
					char c = val.charAt(cI);
					if (c == '0')
						currByte.add(new Integer(0));
					else if (c=='1')
						currByte.add(new Integer(1));
					else
						throw new RuntimeException("Bad char: " + c + " (" + (int)c + ")");
				}
					
				//Finally, set a byte (or more), if necessary
				while (currByte.size()>7) {
					int byteVal = 0;
					int[] bits = new int[8];
					for (int i=0; i<8 && currByte.size()>0; i++)
						bits[i] = ((Integer)currByte.remove(0)).intValue();
					
					for (int i=0; i<8; i++)
						byteVal += (bits[i]*(1<<(7-i))); 

					enBytes.add(new Byte((byte)byteVal));
				}
			}
			
			//Enemy bitsets
			boolean[] bitset = (boolean[])enemyBitsets.get(counter++);
			for (int bitPos = 0; bitPos<bitset.length; bitPos++) {
				if (!skipBits[bitPos]) {
					if (bitset[bitPos])
						currByte.add(new Integer(1));
					else
						currByte.add(new Integer(0));
				}
			}
			//Finally, flush the bit buffer
			while (currByte.size()>0) {
				int byteVal = 0;
				int[] bits = new int[8];
				for (int i=0; i<8 && currByte.size()>0; i++)
					bits[i] = ((Integer)currByte.remove(0)).intValue();
				
				for (int i=0; i<8; i++)
					byteVal += (bits[i]*(1<<(7-i))); 

				enBytes.add(new Byte((byte)byteVal));
			}
			
			//Compute enemy spawn pairs
			ArrayList/*<int[]>*/ spawnPairs = new ArrayList/*<int[]>*/();
			for (int spID=0; spID<12; spID++) {
				int enToSpawn = foe[(79+spID)*2];
				if (enToSpawn>0)
					spawnPairs.add(new int[]{spID, enToSpawn-1});
			}
			if (spawnPairs.size()==0)
				enBytes.add(new Byte((byte)0));
			else {
				//Add the first one
				int nextByte = spawnPairs.size()*0x10 + (0xF&(((int[])spawnPairs.get(0))[0]));
				enBytes.add(new Byte((byte)nextByte));
				enBytes.add(new Byte((byte)((int[])spawnPairs.get(0))[1]));
				enBytes.add(new Byte((byte)foe[91*2]));
				
				//Add the rest
				int rem = -1;
				for (int i=1; i<spawnPairs.size(); i++) {
					if (rem==-1) {
						rem = (((int[])spawnPairs.get(i))[1]&0xF)*0x10;
						nextVal = (((int[])spawnPairs.get(i))[0])*0x10 + (((int[])spawnPairs.get(i))[1]&0xF0)/0x10;
						enBytes.add(new Byte((byte)nextVal));
					} else {
						nextVal = rem + ((int[])spawnPairs.get(i))[0];
						enBytes.add(new Byte((byte)nextVal));
						enBytes.add(new Byte((byte)((int[])spawnPairs.get(i))[1]));
						rem = -1;
					}
				}
				if (rem!=-1)
					enBytes.add(new Byte((byte)rem));
			}
			
			//Regular Attacks
			//for (byte atk : atkRegular)
			for (int aI=0; aI<atkRegular.size(); aI++)
				enBytes.add(atkRegular.get(aI));
			//for (byte atk : atkDesp)
			for (int aI=0; aI<atkDesp.size(); aI++)
				enBytes.add(atkDesp.get(aI));
			//for (byte atk : atkAlone)
			for (int aI=0; aI<atkAlone.size(); aI++)
				enBytes.add(atkAlone.get(aI));
			
			
			//Convert back & save
			byte[] next = new byte[enBytes.size()];
			for (int i=0; i<enBytes.size(); i++)
				next[i] = ((Byte)enBytes.get(i)).byteValue();
			records.add(next);
		}
		
		//Prepare output
		try {
			OutputStream os = outGetter.getOutputStream(lumpName.replaceAll("[.]DT1", ".FOE"));
			
			//Header
			os.write(header);
			enemyBytesAfter += header.length;
			
			//Each enemy
			//for (byte[] foe : records) {
			for (int fI=0; fI<records.size(); fI++) {
				byte[] foe = (byte[])records.get(fI);
				os.write(foe);
				enemyBytesAfter += foe.length;				
			}
			
			outGetter.closeOutputStream(os);
		} catch (Exception ex) {
			System.out.println("Error creating FOE lump: " + ex.toString());
		}

		return input;
	}

	
	
	private static FileInputStream compressBattleFormations(String lumpName, FileInputStream input, long numBytes) {
		//Data collection
		battleFormBytesBefore = numBytes;
		battleFormBytesAfter = 0;
		
		//Prepare our lump
		ArrayList/*<Byte>*/ records = new ArrayList/*<Byte>*/();
		while (numBytes >= 80 ) {
			int genStats = records.size();
			int numEnemies = 0;
			records.add(new Byte((byte)0)); //Placeholder
			records.add(new Byte((byte)0)); //Placeholder
			records.add(new Byte((byte)0)); //Placeholder
			records.add(new Byte((byte)0)); //Placeholder
			records.add(new Byte((byte)0)); //Placeholder
			records.add(new Byte((byte)0)); //Placeholder - Num battles
			
			try {
				//Read each battle
				for (int i=0; i<8; i++) {
					byte enemyType = (byte)input.read();
					input.read();
					
					byte enemyX = (byte)input.read();
					input.read();
					
					byte enemyY = (byte)input.read();
					input.read();
					
					input.read();
					input.read();
					
					//Store this?
					if (enemyType>0) {
						records.add(new Byte((byte)(enemyType-1)));
						records.add(new Byte(enemyX));
						records.add(new Byte(enemyY));
						
						numEnemies++;
					}
				}
				
				//Read general battle stuff
				records.set(genStats, new Byte((byte)input.read()));
				input.read();
				
				//Battle music - DOESN'T WORK FOR -1 (same as map)
				records.set(genStats+1, new Byte((byte)input.read()));
				input.read();
				
				//Background anim frames
				records.set(genStats+2, new Byte((byte)input.read()));
				input.read();
				
				//Background anim speed
				records.set(genStats+3, new Byte((byte)input.read()));
				records.set(genStats+4, new Byte((byte)input.read()));
				
				//Num enemies
				records.set(genStats+5, new Byte((byte)numEnemies));
				
				//Wasted
				for (int i=0; i<4; i++) {
					input.read();
					input.read();
				}
				
			} catch (IOException ex) {
				System.out.println("Error reading FOR lump: " + ex.getMessage());
				System.exit(1);
			}
			
			numBytes -= 80;
		}
			
		byte[] res = new byte[records.size()];
		for (int i=0; i<res.length; i++)
			res[i] = ((Byte)records.get(i)).byteValue();
		battleFormBytesAfter = res.length;
		
		//Prepare output
		try {
			OutputStream os = outGetter.getOutputStream(lumpName.replaceAll("[.]FOR", ".BFF"));
			os.write(res);
			outGetter.closeOutputStream(os);
		} catch (Exception ex) {
			System.out.println("Error creating BFF lump: " + ex.toString());
		}

		return input;
	}
	
	
	private static FileInputStream compressFormationSets(String lumpName, FileInputStream input, long numBytes) {
		//Data collection
		formSetBytesBefore = numBytes;
		formSetBytesAfter = 0;
		
		//Prepare our lump
		ArrayList/*<Byte>*/ records = new ArrayList/*<Byte>*/();
		records.add(new Byte((byte)0)); //Placeholder
		int numValidSets = 0;
		int zeroRuns = 0;
		while (numBytes >= 50 ) {
			int savedIndex = records.size();
			int numValidEntries = 0;
			records.add(new Byte((byte)0)); //Placeholder
			
			try {
				//Read the battle frequency
				records.add(new Byte((byte)input.read()));
				input.read();
				
				//Read each enemy formation entry
				for (int i=0; i<20; i++) {
					byte nextFor = (byte)(input.read());
					input.read();
					
					if (nextFor>0) {
						records.add(new Byte((byte)(nextFor-1)));
						numValidEntries++;
					}
				}
				
				//Read wasted bytes
				for (int i=0; i<4; i++) {
					input.read();
					input.read();
				}
			} catch (IOException ex) {
				System.out.println("Error reading EFS lump: " + ex.getMessage());
				System.exit(1);
			}
			
			if (numValidEntries>0) {
				records.set(savedIndex, new Byte((byte)numValidEntries));
				numValidSets++;
				zeroRuns = 0;
			} else {
				//Remove this record's frequency -just leave the size
				records.remove(records.size()-1);
				numValidSets++;
				zeroRuns++;
			}
			
			numBytes -= 50;
		}
		
		//Remove trailing records which are empty
		while (zeroRuns>0) {
			records.remove(records.size()-1);
			numValidSets--;
			zeroRuns--;
		}
		
		records.set(0, new Byte((byte)numValidSets));
		byte[] res = new byte[records.size()];
		for (int i=0; i<res.length; i++)
			res[i] = ((Byte)records.get(i)).byteValue();
		formSetBytesAfter = res.length;
		
		//Prepare output
		try {
			OutputStream os = outGetter.getOutputStream(lumpName.replaceAll("[.]EFS", ".EFX"));
			os.write(res);
			outGetter.closeOutputStream(os);
		} catch (Exception ex) {
			System.out.println("Error creating EFX lump: " + ex.toString());
		}

		return input;
	}

	
	private static FileInputStream compressSay(String lumpName, FileInputStream input, long numBytes) {
		//Data collection
		sayBytesBefore = numBytes;
		sayBytesAfter = 0;
		
		//Prepare our lump
		ArrayList/*<byte[]>*/ allBoxes = new ArrayList/*<byte[]>*/(); 
		while (numBytes >= 400) {
			//Read the string (and the null at the end)
			char[] tempRes =  new char[400];
			byte[] allLines = new byte[38*8+1];
			int[] remData = new int[29];
			int intPos = 0;
			try {
				//Read text
				StringBuilder sb = new StringBuilder(38*9);
				input.read(allLines);
				
				//Read first few ints
				for (int i=0; i<21; i++)
					remData[intPos++] = input.read() + 0x100*input.read();
				
				//Deal with next two bytes
				input.read();
				int boxChoice = input.read();
				
				//Deal with next string
				int[] ch = new int[15];
				for (int i=0; i<ch.length; i++)
					ch[i] = input.read();
				char[] choice1 = trimNTString(ch).toCharArray();
				
				//Next int & byte
				remData[intPos++] = input.read() + 0x100*input.read();
				input.read();
				
				//Next choice name
				for (int i=0; i<ch.length; i++)
					ch[i] = input.read();
				char[] choice2 = trimNTString(ch).toCharArray();
				
				//Next two ints
				remData[intPos++] = input.read() + 0x100*input.read();
				input.read(); 
				input.read();
				
				//Next six ints
				for (int i=0; i<6; i++)
					remData[intPos++] = input.read() + 0x100*input.read();
				
				//Next int
				input.read(); 
				input.read();

				//Translate
				for (int line=0; line<8; line++) {
					for (int i=0; i<38; i++) {
						byte c = allLines[line*38+i];
						if (c != '\0')
							sb.append(c);
					}
					if (line!=7)
						sb.append("\n");
				}
				
				//Put this in our result array
				int pos = 0;
				if (true) { //Scope it out
					String str = sb.toString();
					int lastIndex = sb.length()-1;
					while (str.charAt(lastIndex)=='\n' && lastIndex>=0)
						lastIndex--;
					
					for (int i=0; i<=lastIndex; i++) 
						tempRes[pos++] = (char)str.charAt(i);
					tempRes[pos++] = '\0';
				}
				
				//Our two strings & bitset
				for (int i=0; i<choice1.length; i++)
					tempRes[pos++] = (char)choice1[i];
				for (int i=0; i<choice2.length; i++)
					tempRes[pos++] = (char)choice2[i];
				tempRes[pos++] = (char)boxChoice;

				//The remaining output
			//	System.out.println("Pos before: " + pos);
				int numZeroes = 0;
				int numInts = 0;
				int intRunPointer = -1;
				boolean dontFlushZeroes = false;
				for (int i=0; i<remData.length; i++) {
					int currInt = remData[i];
					if (currInt==0) {
						if (intRunPointer!=-1) {
							tempRes[intRunPointer] = (char)numInts;
							//System.out.println("Count: " + (int)tempRes[intRunPointer]);
							intRunPointer = -1;
						}
							
						
						numZeroes++;
						//System.out.println("  + " + numZeroes);
						dontFlushZeroes = false;
						numInts = 0;

						if (i==remData.length-1) {
							tempRes[pos++] = (char)numZeroes;
						}
					} else {
						//First, determine if this is just a sporadic case
						if (i!=remData.length-1 && remData[i+1] != 0) {
							numZeroes |= 0x80;
						}
						
						//Now, encode the byte (zeroes)
						if (!dontFlushZeroes)
							tempRes[pos++] = (char)numZeroes;
						
						//For next time
						if (intRunPointer==-1 && (numZeroes&0x80)!=0) {
							dontFlushZeroes = true;
							intRunPointer = pos;
							pos++; //Save space for later
						}
						
						//Encode the byte (value)
						tempRes[pos++] = (char)(currInt&0xFF);
						tempRes[pos++] = (char)((currInt&0xFF00)/0x100);
						
						//Increment
						numZeroes = 0;
						numInts++;
					}
				}
			//	System.out.println("Pos after: " + pos);
				
				
				//Save for output
				sayBytesAfter += pos;
				byte[] res = new byte[pos];
				for (int i=0; i<pos; i++) {
				//	System.out.println(tempRes[i] + ":" + (int)tempRes[i]);
					res[i] = (byte)tempRes[i];
				}
				allBoxes.add(res);
			} catch (IOException ex) {
				System.out.println("Error reading SEZ lump: " + ex.toString());
				return input;
			}

			numBytes -= 400;
		}
		
		//Prepare output - Split every 50 boxes into a seperate file.
		try {
			String postFix = ".SEZ";
			int MAX_BOXES_PER_FILE = 50; //CHEAT!
			int numBoxFiles = (int)Math.ceil(((double)allBoxes.size())/MAX_BOXES_PER_FILE);
			for (int i=0; i<numBoxFiles; i++) {
				OutputStream os = outGetter.getOutputStream(lumpName.replaceAll(".SAY", postFix));
			
				for (int bx=0; bx<MAX_BOXES_PER_FILE; bx++) {
					int id = i*MAX_BOXES_PER_FILE + bx;
					if (id >= allBoxes.size())
						break;
					
					os.write((byte[])allBoxes.get(id));
				}
				
				postFix = "_" + (i+1) + ".SEZ"; //i+1 for the NEXT file.
				outGetter.closeOutputStream(os);
			}
			
		} catch (Exception ex) {
			System.out.println("Error creating SEZ lump: " + ex.toString());
		}

		return input;
	}

	private static int scale(int val63) {
		return val63*255/63;
	}

	private static final FileInputStream copyLump(String lumpName, FileInputStream input, long numBytes) {
		byte[] data = new byte[(int)numBytes];
		byte[] realData =  new byte[data.length];
		try {
			input.read(data);
			for (int i=0; i<data.length; i++)
				realData[i] = (byte)data[i];
			OutputStream os = outGetter.getOutputStream(lumpName);
			os.write(realData);
			outGetter.closeOutputStream(os);
		} catch (Exception ex) {
			System.out.println("Error reading/creating/writing/closing lump: " + ex.toString());
			return input;
		}

		return input;
	}
	
	private static final FileInputStream compressDTHeroLump(String lumpName, FileInputStream input, long numBytes) {
		heroBytesBefore = numBytes;
		heroIndices = new ArrayList/*<int[]>*/();
		
		int[] multipliers = new int[]{1, 1};
		int[] mp_multipliers = new int[]{1, 1};
		int[] tempRes = new int[(int)numBytes];
		byte[][] heroFrames = new byte[(int)numBytes/636][];
		int currHero = 0;
		while (numBytes >= 636) {
			byte[] hero = new byte[636];
			
			//First, check if this is a valid hero
			try {
				input.read(hero);
			} catch (IOException ex)  {
				System.out.println("Error reading hero: " + ex.toString());
				return input;
			}
			boolean badHero = true;
			for (int i=0; i<636; i++) {
				if (hero[i] != 0) {
					badHero = false;
					break;
				}
			}
			if (badHero) {
				heroFrames[currHero++] = null;
			} else {
				heroFrames[currHero++] = hero;
			
				for (int i=23*2+1; i<47*2; i+=2) {
					int val = ((int)0x100*hero[i] + (int)hero[i-1]);
					if (hero[i]!=0) {
						int offset = ((i-1)/2);
						if (offset==23 || offset==24)
							continue; //HP
						else if (offset==25 || offset==26) { //MP
							//Adjust
							offset -= 25;
							if (val < 255)
								val = 1;
							else if (val < 510)
								val = 2;
							else if (val < 765)
								val = 3;
							else
								val = 4;
							
							if (val > mp_multipliers[offset])
								mp_multipliers[offset] = val;
						} else {
							//Any other stat
							if (offset%2==0)
								offset = 1;
							else
								offset = 0;
							if (val < 255)
								val = 1;
							else if (val < 510)
								val = 2;
							else if (val < 765)
								val = 3;
							else
								val = 4;
							
							if (val > mp_multipliers[offset])
								mp_multipliers[offset] = val;
						}
					}
				}
			
				for (int i=47*2+1; i<239*2; i+=2) {
					if (hero[i]!=0)
						throw new RuntimeException("Overflow[" + ((i-1)/2) + "]   " + ((int)0x100*hero[i] + (int)hero[i-1]));
				}
				for (int i=288*2+1; i<297*2; i+=2) {
					if (hero[i]!=0) 
						throw new RuntimeException("Overflow[" + ((i-1)/2) + "]   " + ((int)0x100*hero[i] + (int)hero[i-1]));
				}
			}
			
			numBytes -= 636;
		}
		int multiplexedMultiplier = (mp_multipliers[0]-1)*0x40 + (mp_multipliers[1]-1)*0x10 + (multipliers[0]-1)*0x4+ (multipliers[1]-1);
		System.out.println("Multipliers: " + multipliers[0] + ":" + multipliers[1]);
		System.out.println("Multipliers(MP): " + mp_multipliers[0] + ":" + mp_multipliers[1]);		
		System.out.println("Multiplexed: " + Integer.toBinaryString(multiplexedMultiplier));
		
		//Start to fill in our array
		tempRes[0] = currHero;
		tempRes[1] = multiplexedMultiplier;
		int currPos = 2;
		//for (byte[] hero : heroFrames) {
		for (int hI=0; hI<heroFrames.length; hI++) {
			byte[] hero = heroFrames[hI];
			
			//Special case: null heroes. Allows for null heroes in the middle of a set of ok heroes
			if (hero==null) {
				heroIndices.add(new int[]{0, 0});
				tempRes[currPos++] = 0xFF;
				continue;
			}
			
			//Name
			String heroName = vString2NTString(hero, 0);
			currPos = placeString(heroName.toCharArray(), tempRes, currPos);
			
			//Hero sprite, etc.
			for (int i=17*2; i<23*2; i+=2) {
				int val = hero[i] + 0x100*hero[i+1];
				
				if (i==17*2)
					heroIndices.add(new int[]{val, -1}); //Save the hero's sprite
				else if (i==18*2)
					((int[])heroIndices.get(heroIndices.size()-1))[1] = val; //Save the hero's picture
				
				if (i==21*2 && val==0xFFFF) {
					//Special case for level: "Average of party.
					val = 0xFF;
				}
				
				tempRes[currPos++] = val;
			}
			
			//Start HP, max HP, as-is
			tempRes[currPos++] = hero[23*2];
			tempRes[currPos++] = hero[23*2+1];
			tempRes[currPos++] = hero[24*2];
			tempRes[currPos++] = hero[24*2+1];
			
			//Stats, divided by X, 0<X<4
			for (int i=25*2; i<47*2; i+=2) {
				int val = hero[i] + 0x100*hero[i+1];
				int divisor = 0;
				if (i==25*2)
					divisor = mp_multipliers[0];
				else if (i==26*2)
					divisor = mp_multipliers[1];
				else if (i%2==1)
					divisor = multipliers[0];
				else
					divisor = multipliers[1];
				tempRes[currPos++] = val/divisor;
				
			}
			
			//Now, for the less-complex-than-it-looks spell list
			int[] numSpells = new int[] {0, 0, 0, 0};
			int headerPos = currPos;
			currPos += 3;
			for (int list=0; list<4; list++) {
				//boolean allNullsFound = false;
				for (int spID=0; spID<24; spID++) {
					int val1 = hero[list*24*2*2 + spID*2*2 + 47*2];
					int val2 = hero[list*24*2*2 + spID*2*2 + 47*2 + 2];
					if (val1==0 /*&& val2==0*/) {
						continue;
					}
					
					//Okay, it passed. Now, save it.
					numSpells[list]++;
					tempRes[currPos++] = val1;
					tempRes[currPos++] = val2;
				}
			}
			    
			//Go back and fix the spell header
			int multiplexedHeader = numSpells[0]*0x8000 + numSpells[1]*0x400 + numSpells[2]*0x20 + numSpells[3];
			tempRes[headerPos] = multiplexedHeader/0x10000;
			tempRes[headerPos+1] = (multiplexedHeader&0xFF00)/0x100;
			tempRes[headerPos+2] = multiplexedHeader&0xFF;
			/*if (heroName.equals("Gisli\0")) {
				System.out.println("Header: " + Integer.toBinaryString(multiplexedHeader));
				System.out.println("Header1: " + Integer.toBinaryString(tempRes[headerPos]));
				System.out.println("Header2: " + Integer.toBinaryString(tempRes[headerPos+1]));
				System.out.println("Header3: " + Integer.toHexString(tempRes[headerPos+2]));
				System.out.println(headerPos+3);
			}*/
			
			//Skip unused byte, move on to hero bitsets
			for (int i=0; i<4; i++)
				tempRes[currPos++] = hero[240*2 + i];
			
			//Spell names
			for (int i=0; i<4; i++) {
				String spellName = vString2NTString(hero, spellNameStartingOffsets[i]);
				currPos = placeString(spellName.toCharArray(), tempRes, currPos);
			}
			
			//Skip unused byte, reduce next four ints to bytes (spell types)
			// Can probably compress further, but we'll skip it for now.
			//Then, reduce the next 5 ints to bytes (tags, length, etc.)
			for (int i=288*2; i<297*2; i+=2) {
				int val = hero[i] + 0x100*hero[i+1];
				tempRes[currPos++] = val;
			}
			
			//Final values, as ints. Skip remaining byts.
			tempRes[currPos++] = hero[297*2];
			tempRes[currPos++] = hero[297*2+1];
			tempRes[currPos++] = hero[298*2];
			tempRes[currPos++] = hero[298*2+1];
			tempRes[currPos++] = hero[299*2];
			tempRes[currPos++] = hero[299*2+1];
			tempRes[currPos++] = hero[300*2];
			tempRes[currPos++] = hero[300*2+1];
		}
		
		//Cull ending null heroes
		while (tempRes[currPos-1] == 0xFF) {
			currPos--;
			currHero--;
			heroIndices.remove(heroIndices.size()-1);
		}
		tempRes[0] = currHero;
		
		//Actual result array
		byte[] result = new byte[currPos];
		for (int i=0; i<currPos; i++) {
			result[i] = (byte)tempRes[i];
		}
		heroBytesAfter = result.length;
		
		//And now, save it as .HRO
		//System.out.println(heroBytesBefore + " --> " + heroBytesAfter  + " (" + (heroBytesBefore-heroBytesAfter)/heroBytesBefore + ")");
		
		try {
			OutputStream os = outGetter.getOutputStream(lumpName.replaceAll("\\.DT0", ".HRO"));
			os.write(result);
			outGetter.closeOutputStream(os);
		} catch (Exception ex) {
			System.out.println("Error creating/writing/closing HRO lump: " + ex.toString());
			return input;
		}	
		
		return input;
	}
	
	
	private static int placeString(char[] chars, int[] output, int start) {
		for (int i=0; i<chars.length; i++) {
			output[start + i] = chars[i];
		}
		return start + chars.length;
	}
	
	private static String trimNTString(int[] chars) {
		StringBuilder sb = new StringBuilder(chars.length+2);
		for (int i=0; i<chars.length; i++) {
			if (chars[i] != 0)
				sb.append((char)chars[i]);
		}
		sb.append('\0');
		return sb.toString();
	}
	
	
	private static String vString2NTString(byte[] input, int start) {
		StringBuilder sb = new StringBuilder(17);
		int pos = start + 2;
		for (int nameBytes = input[start]; nameBytes>0; nameBytes--) {
			sb.append((char)input[pos]);
			pos+=2;
		}
		sb.append('\0');
		return sb.toString();
	}
	
	
	private static final FileInputStream readAndCopyMasterPalette(String lumpName, FileInputStream input, long numBytes) {
		//Bsave nonsense
		byte[] masPal = new byte[(int)numBytes];
		try {
			input.read(masPal);
		} catch (IOException ex)  {
			System.out.println("Error reading master palette: " + ex.toString());
			return input;
		}
		byte[] res = new byte[masPal.length];
		for (int i=0; i<res.length; i++)
			res[i] = (byte)masPal[i];

		//Now, read pixel data (half-width, for some reason)
		masterPalette = new int[256];
		int startOff = 7;
		for (int i=0; i<256; i++) {
			masterPalette[i] =  scale(masPal[startOff] + 0x100*masPal[startOff+1])*0x10000
					+ scale(masPal[startOff+2] + 0x100*masPal[startOff+3])*0x100
					+ scale(masPal[startOff+4] + 0x100*masPal[startOff+5]);

			startOff += 6;
		}

		//Now, copy the master palette to the output directory.
		try {
			OutputStream os = outGetter.getOutputStream(lumpName);
			os.write(res);
			outGetter.closeOutputStream(os);
		} catch (Exception ex) {
			System.out.println("Error creating/writing/closing MAS lump: " + ex.toString());
			return input;
		}

		return input;
	}
	
	
	private static final FileInputStream readAndCopyOtherPalettes(String lumpName, FileInputStream input, long numBytes) {
		savedPalettes = new ArrayList/*<int[]>*/();
		
		//Bsave+PAL nonsense
		byte[] currPal = new byte[(int)numBytes];
		try {
			input.read(currPal);
		} catch (IOException ex)  {
			System.out.println("Error reading master palette: " + ex.toString());
			return input;
		}
		byte[] res = new byte[currPal.length];
		for (int i=0; i<res.length; i++)
			res[i] = (byte)currPal[i];

		//Now, read pixel data
		int onByte = 7;
		if ((currPal[0]==0x5C) && (currPal[1]==0x11)) {
			onByte = 16;
		} else
			throw new RuntimeException("ERROR, outdated palette lump: " + Integer.toHexString(currPal[0]) + "," + Integer.toHexString(currPal[1]));
		while (onByte < numBytes) {
			int[] thisPal = new int[16];
			for (int i=0; i<16; i++) {
				thisPal[i] = currPal[onByte+i];
			}
			savedPalettes.add(thisPal);
			
			onByte += 16;
		}

		//Now, copy the palette lump to the output directory.
		try {
			OutputStream os = outGetter.getOutputStream(lumpName);
			os.write(res);
			outGetter.closeOutputStream(os);
		} catch (Exception ex) {
			System.out.println("Error creating/writing/closing MAS lump: " + ex.toString());
			return input;
		}

		return input;
	}
	

	private static FileInputStream splitTilesets(String lumpName, FileInputStream input, long numBytes) {
		tilesetLumpName = lumpName;
		return splitOnModeX(input, numBytes, tilesets);
	}

	private static FileInputStream splitBackdrops(String lumpName, FileInputStream input, long numBytes) {
		mxsLumpName = lumpName;
		return splitOnModeX(input, numBytes, backdrops);
	}

	private static FileInputStream splitOnModeX(FileInputStream input, long numBytes, ArrayList/*<int[][]>*/ store) {
		//We R lazy c0ders
		int TILE_SIZE = 20;
		int TILE_COLS = 16;
		int TILE_ROWS = 10;
		int MXS_SEGMENTS = 4;
		if (TILE_SIZE%4 != 0) {
			System.out.println("Error reading tileset! Code assumes tiles are %4==0 pixels wide/high. Re-coding is needed!");
			return input;
		}

		//Read the tiles
		while (numBytes >= 64000) {
			int[][] res = new int[TILE_ROWS*TILE_COLS][TILE_SIZE*TILE_SIZE];
			for (int mxOffY=0; mxOffY<MXS_SEGMENTS; mxOffY++) {
				for (int row=0; row<TILE_ROWS; row++) {
					//Init
					if (mxOffY==0) {
						for (int col=0; col<TILE_COLS; col++) {
							res[row*TILE_COLS + col] = new int[TILE_SIZE*TILE_SIZE];
						}
					}

					//This gets messy
					for (int yPos=0; yPos<TILE_SIZE; yPos+=MXS_SEGMENTS) {
						for (int mxOffX=0; mxOffX<MXS_SEGMENTS; mxOffX++) {
							for (int col=0; col<TILE_COLS; col++) {
								for (int pix=0; pix<TILE_SIZE; pix+=MXS_SEGMENTS) {
									int val = -1;
									try {
										val = input.read();
									} catch (IOException ex) {
										System.out.println("Error reading tilesets: " + ex.toString());
										return input;
									}
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

	        store.add(res);
			numBytes -= 64000;
		}

		return input;

	}



	private static OutputStream writeLump(String lumpName, int[] lumpBytes, OutputStream writer) {
		byte[] mn = new byte[lumpName.length() + 1];
		for (int i = 0; i <= lumpName.length(); i++) {
			if (i == lumpName.length())
				mn[i] = 00;
			else
				mn[i] = (byte) lumpName.charAt(i);
		}
		byte[] actual_bytes = new byte[lumpBytes.length];
		for (int i = 0; i < lumpBytes.length; i++)
			actual_bytes[i] = (byte) lumpBytes[i];
		byte[] mn_s = getPDP(actual_bytes.length);
		try {
			writer.write(mn);
			writer.write(mn_s);
			writer.write(actual_bytes);
		} catch (IOException ex) {
		}

		return writer;
	}


	//Combine data that's more complex than your average lump
	private static void postProcess(File inputFile) {
		//Create the new header lump
		OutputStream writer2 = outGetter.getOutputStream("HEADER.LMP");
		if (writer2==null)
			System.out.println("Error writing file: HEADER.LMP");
		//Write the ARCH lump
		if (true) {
			int[] arch = new int[internalName.length() + customVersion.length() + 2];
			for (int i=0; i<internalName.length(); i++)
				arch[i] = internalName.charAt(i);
			arch[internalName.length()] = '\0';
			for (int i=0; i<customVersion.length(); i++)
				arch[internalName.length()+1+i] = customVersion.charAt(i);
			arch[internalName.length()+customVersion.length()+1] = '\0';
			writer2 = writeLump(".ARCH", arch, writer2);
		}
		//Write the BROWSE lump
		if (true) {
			int[] browse = new int[longName.length() + aboutLine.length() + 2];
			for (int i=0; i<longName.length(); i++)
				browse[i] = longName.charAt(i);
			browse[longName.length()] = '\0';
			for (int i=0; i<aboutLine.length(); i++)
				browse[longName.length()+1+i] = aboutLine.charAt(i);
			browse[longName.length()+aboutLine.length()+1] = '\0';
			writer2 = writeLump(".BROWSE", browse, writer2);
		}
		try {
			outGetter.closeOutputStream(writer2);
		} catch (IOException ex) {}

		
		//Cache the HQ2X'd version of our hero's graphics.
		for (int i=0; i<heroIndices.size(); i++) {
			int[] currPixels = (int[])heroPicIndexed.get(((int[])heroIndices.get(i))[0]);
			int[] palette = (int[])savedPalettes.get(((int[])heroIndices.get(i))[1]);
			int bkgrd = 0x000055; //cheating for now...
			
			System.out.println("Hero: " + i);
			
			//Build the picture up.
			for (int pix=0; pix<currPixels.length; pix++) {
				if (currPixels[pix] == 0)
					currPixels[pix] = bkgrd;
				else {
					//System.out.println("Pixel: " + currPixels[pix]);
					currPixels[pix] = masterPalette[palette[currPixels[pix]]];
				}
			}
			
			//Convert it & save
			BufferedImage img = new BufferedImage(PictureParser.PT_HERO_SIZES[0]*2, PictureParser.PT_HERO_SIZES[1]*2, BufferedImage.TYPE_INT_RGB);
			currPixels = HQ2X.hq2x(currPixels, PictureParser.PT_HERO_SIZES[0]);
			for (int y=0; y<img.getHeight(); y++) {
				for (int x=0; x<img.getWidth(); x++) {
					img.setRGB(x, y, currPixels[y*img.getWidth()+x]);
				}
			}
			File path = outGetter.getOutputFile("HERO_" + i + ".PNG");
			try {
				ImageIO.write(img, "png", path);
			} catch (IOException iex) {
				System.out.println("Error saving hero image: " + iex.toString());
			}
			
		}
		
		

		//Copy all tilesets/backdrops to PNGs
		Object[] stores = new Object[] {tilesets, backdrops};
		String[] outNames = new String[] {tilesetLumpName, mxsLumpName};
		for (int q=0; q<stores.length; q++) {
			ArrayList store = (ArrayList)stores[q];
			String name = outNames[q];

			for (int i=0; i<store.size(); i++) {
				BufferedImage img = new BufferedImage(16*20, 10*20, BufferedImage.TYPE_INT_RGB);
				int[][] data = (int[][])store.get(i);

				//Tilesets index the master palette
				for (int tRow=0; tRow<10; tRow++) {
					for (int tCol=0; tCol<16; tCol++) {
						for (int yPos=0; yPos<20; yPos++) {
							for (int xPos=0; xPos<20; xPos++) {
								int val = masterPalette[data[tRow*16+tCol][yPos*20+xPos]];
								img.setRGB(tCol*20+xPos, tRow*20+yPos, val);
							}
						}
					}
				}

				//Save the image
				File path = outGetter.getOutputFile(name.replaceAll(".TIL", "_TIL_").replaceAll(".MXS", "_MXS_") + i + ".PNG");
				try {
					ImageIO.write(img, "png", path);
				} catch (IOException iex) {
					System.out.println("Error saving image: " + iex.toString());
				}
			}
		}

		//Get a valid count
		ArrayList/*<Object[]>*/ tempDoorData = new ArrayList/*<Object[]>*/();
		//int id = 0;
		for (int id=0; id<mapLumps.size(); id++) {
			int[][] map = (int[][])mapLumps.get(id);

			//Sometimes, we get partial data (e.g., they created a map, made some doors, then deleted it.)
			boolean partial = false;
			StringBuilder sb = new StringBuilder("<");
			String sep = "";
			//for (MAP_SEGMENT ms : MAP_SEGMENT.values()) {
			for (int ms=0; ms<MAP_SEGMENT_MAX; ms++) {
				if (map[ms]==null && ms!=ATTACK_DATA && ms!=FORMATION_SETS && ms!=BATTLE_FORMATIONS && ms!=ENEMY_STATS) {
					partial = true;
					sb.append(sep + ms);
					sep = ", ";
				}
			}
			sb.append(">");
			if (partial) {
				System.out.println("Warning: Partial data for map [" + id + "], missing: " + sb.toString());
				//id++;
				continue;
			}

			tempDoorData.add(new Object[2]);

			//id++;
		}


		//First, to all maps
		for (int mapID=0; mapID<tempDoorData.size(); mapID++) {
			int[][] line = (int[][])mapLumps.get(mapID);

			//First, fix that annoying y co-ordinate bug
			for (int i=200; i<400; i+=2) {
				int val = line[DOOR_IDS][i] + 0x100*line[DOOR_IDS][i+1] - 1;
				line[DOOR_IDS][i] = val&0xFF;
				line[DOOR_IDS][i+1] = (val&0xFF00)/0x100;
			}

			//Now, prepare our "temporary" data
			int[][] tempLoadingData2 = new int[200][];
			for (int i=0; i<tempLoadingData2.length; i++)
				tempLoadingData2[i] = new int[5];
			for (int prop=0; prop<5; prop++) {
				for (int i=0; i<tempLoadingData2.length; i++)
					tempLoadingData2[i][prop] = line[DOOR_LINKS][prop*400+2*i+7] + 0x100*line[DOOR_LINKS][prop*400+2*i+8];
			}

			//TEST
			/*if (mapID==0) {
				System.out.println("DATA: 0");
				for (int i=7; i<line[MAP_SEGMENT.DOOR_LINKS.getValue()].length; i+=400) {
					System.out.print("[");
					String sep = "";
					for (int val=0; val<400; val+=2) {
						System.out.print(sep + line[MAP_SEGMENT.DOOR_LINKS.getValue()][val+i]+":"+line[MAP_SEGMENT.DOOR_LINKS.getValue()][val+i+1]);
						sep = ", ";
					}
					System.out.println();
				}
			}*/
			//END_TEST

			int[][] tempLoadingData1 = new int[100][];
			for (int i=0; i<tempLoadingData1.length; i++)
				tempLoadingData1[i] = new int[3];
			for (int prop=0; prop<3; prop++) {
				for (int i=0; i<tempLoadingData1.length; i++) {
					tempLoadingData1[i][prop] = line[DOOR_IDS][prop*200+2*i] + 0x100*line[DOOR_IDS][prop*200+2*i+1];

					if (prop==2)
						tempLoadingData1[i][prop] = tempLoadingData1[i][prop]&0x1; //Make it easier to read later.
				}
			}

			((Object[])tempDoorData.get(mapID))[0] = tempLoadingData1;
			((Object[])tempDoorData.get(mapID))[1] = tempLoadingData2;
		}

		//Now, build and save each map
		mapCompression = new double[2][];
		numberOfMaps = tempDoorData.size();
		mapCompression[0] = new double[MAP_SEGMENT_MAX];
		mapCompression[1] = new double[MAP_SEGMENT_MAX];
		for (int mapID=0; mapID<tempDoorData.size(); mapID++) {
			int[][] map = (int[][])mapLumps.get(mapID);
			int[][] tempLoadingData1 = (int[][])((Object[])tempDoorData.get(mapID))[0];
			int[][] tempLoadingData2 = (int[][])((Object[])tempDoorData.get(mapID))[1];
			
			System.out.println("Reading map: " + mapID + ", ");
			
			mapCompression[0][DOOR_LINKS] += (tempLoadingData1.length*tempLoadingData1[0].length + tempLoadingData2.length*tempLoadingData2[0].length)/numberOfMaps;
			//TEST
			/*if (mapID==2) {
				System.out.println("OUTPUT OF MAP: " + mapID);
				System.out.println("---------LINKS-----------");
				for (int y=0; y<tempLoadingData2.length; y++) {
					String sep = "";
					for (int x=0; x<tempLoadingData2[y].length; x++) {
						System.out.print(sep + tempLoadingData2[y][x]);
						sep = "\t";
					}
					System.out.println();
				}
				System.out.println("---------DOORS----------");
				for (int y=0; y<tempLoadingData1.length; y++) {
					String sep = "";
					for (int x=0; x<tempLoadingData1[y].length; x++) {
						System.out.print(sep + tempLoadingData1[y][x]);
						sep = "\t";
					}
					System.out.println();
				}
				System.out.println("-------------------------");
			}*/
			//END_TEST

			//Now, the general algorithm from before
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
                if (tempLoadingData1[row[0]][2]==0 || ((int[][])((Object[])tempDoorData.get(row[2]))[0])[row[1]][2]==0)
                    continue;

                count++;
            }

            //Actually save the doors
            int currDoor = 0;
            int[] doorBytes = new int[count*14];
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
                if (tempLoadingData1[row[0]][2]==0 || ((int[][])((Object[])tempDoorData.get(row[2]))[0])[row[1]][2]==0)
                    continue;

                //Set the values

                //Source X:
                doorBytes[2*currDoor] = tempLoadingData1[row[0]][0] & 0xFF;
                doorBytes[2*currDoor+1] = (tempLoadingData1[row[0]][0] & 0xFF00)/0x100;

                //Source Y:
                doorBytes[count*2 + 2*currDoor] = tempLoadingData1[row[0]][1] & 0xFF;
                doorBytes[count*2 + 2*currDoor+1] = (tempLoadingData1[row[0]][1] & 0xFF00)/0x100;

                //Dest X:
                doorBytes[2*count*2 + 2*currDoor] = ((int[][])((Object[])tempDoorData.get(row[2]))[0])[row[1]][0] & 0xFF;
                doorBytes[2*count*2 + 2*currDoor+1] = (((int[][])((Object[])tempDoorData.get(row[2]))[0])[row[1]][0] & 0xFF00)/0x100;

                //Dest Y:
                doorBytes[3*count*2 + 2*currDoor] = ((int[][])((Object[])tempDoorData.get(row[2]))[0])[row[1]][1] & 0xFF;
                doorBytes[3*count*2 + 2*currDoor+1] = (((int[][])((Object[])tempDoorData.get(row[2]))[0])[row[1]][1] & 0xFF00)/0x100;

                //Dest Map:
                doorBytes[4*count*2 + 2*currDoor] = row[2] & 0xFF;
                doorBytes[4*count*2 + 2*currDoor+1] = (row[2] & 0xFF00)/0x100;

                //Tag 1:
                doorBytes[5*count*2 + 2*currDoor] = row[3] & 0xFF;
                doorBytes[5*count*2 + 2*currDoor+1] = (row[3] & 0xFF00)/0x100;

                //Tag 2:
                doorBytes[6*count*2 + 2*currDoor] = row[4] & 0xFF;
                doorBytes[6*count*2 + 2*currDoor+1] = (row[4] & 0xFF00)/0x100;

                currDoor++;
            }

			//Save our data
			OutputStream writer = outGetter.getOutputStream(pad(mapID, 3)+".MAP");
			if (writer==null) {
				System.out.println("Error writing file: " + pad(mapID, 3)+".MAP");
				//id++;
				continue;
			}
			mapCompression[1][DOOR_LINKS] += (doorBytes.length)/numberOfMaps;
			
			//Post-process the name lump
			int[] mapName = postProcessMapName(map[MAP_NAME]);
			mapCompression[0][MAP_NAME] += map[MAP_NAME].length/numberOfMaps;
			mapCompression[1][MAP_NAME] += mapName.length/numberOfMaps;

			//Fix the foe map, using Huffman coding
			int[] foeData = postProcessFoemap(map[ENEMY_MAP]);
			mapCompression[0][ENEMY_MAP] += map[ENEMY_MAP].length/numberOfMaps;
			mapCompression[1][ENEMY_MAP] += foeData.length/numberOfMaps;
			
			//Fix the passability lump, using faux-Huffman coding 
			//map5 = mapID==5;
			int[] passabilityMap = postProcessPassability(map[PASSABILITY]);
			mapCompression[0][PASSABILITY] += map[PASSABILITY].length/numberOfMaps;
			mapCompression[1][PASSABILITY] += passabilityMap.length/numberOfMaps;			
			
			//Fix the NPC definitions: remove the dummy 1920 bytes at the end; only allow one all-zero entry
			int[] npcDefs = combineNPCData(map[NPC_DATA], map[NPC_LOCATIONS]);
			mapCompression[0][NPC_DATA] += map[NPC_DATA].length/numberOfMaps + map[NPC_LOCATIONS].length/numberOfMaps;
			mapCompression[1][NPC_DATA] += npcDefs.length/numberOfMaps;
			
			//Fix the NPC locations: remove all null IDs
			/*int[] npcLocs = postProcessNPCLocations(map[MAP_SEGMENT.NPC_LOCATIONS.getValue()]);
			mapCompression[0][MAP_SEGMENT.NPC_LOCATIONS.getValue()] += map[MAP_SEGMENT.NPC_LOCATIONS.getValue()].length/numberOfMaps;
			mapCompression[1][MAP_SEGMENT.NPC_LOCATIONS.getValue()] += npcLocs.length/numberOfMaps;*/
			
			//Un-fixed data
			mapCompression[0][TILES] += map[TILES].length/numberOfMaps;
			mapCompression[1][TILES] += map[TILES].length/numberOfMaps;
			mapCompression[0][GEN_MAP_INFO] += map[GEN_MAP_INFO].length/numberOfMaps;
			mapCompression[1][GEN_MAP_INFO] += map[GEN_MAP_INFO].length/numberOfMaps;
			
			////////////////////////////////////////
			// WRITE
			////////////////////////////////////////
			
			//Write the name lump
			writer = writeLump(".MN", mapName, writer);

			//Write the general map data lump
			writer = writeLump(".MD", map[GEN_MAP_INFO], writer);

			//Write the non-door map data
			writer = writeLump(".T", map[TILES], writer);
			writer = writeLump(".P", passabilityMap, writer);
			writer = writeLump(".N", npcDefs, writer);
			//writer = writeLump(".L", npcLocs, writer);
			writer = writeLump(".E", foeData, writer);

			//Write the door data
			writer = writeLump(".D", doorBytes, writer);

			try {
				outGetter.closeOutputStream(writer);
			} catch (IOException ex) {}
			//id++;
		}
	}

	
	private static int[] postProcessMapName(int[] rawData) {
		int len = rawData[0];
		int[] res = new int[len+1];
		for (int pos=1; pos<=len; pos++)
			res[pos-1] = (int)rawData[pos];
		res[len] = '\0';
		return res;
	}
	
	
	private static int[] combineNPCData(int[] nData, int[] locations) {
		//Count NPCs
		int validNPCs=0;
		boolean foundAllNullRow = false;
		for (int npc=0; npc<36; npc++) {
			boolean allZero = true;
			for (int i=0; i<30; i++) {
				if (nData[7 + npc*30 + i]!=0) {
					allZero = false;
					break;
				}
			}
			
			if (allZero && foundAllNullRow)
				continue;
			else
				foundAllNullRow = true;
			
			validNPCs++;
		}
		
		//Count locations
		ArrayList/*<int[]>*/ locData = new ArrayList/*<int[]>*/();
		for (int id=0; id<300; id++) {
			int npcID = locations[2*300*2 + 2*id +7] + 0x100*locations[2*300*2 + 2*id +8];
			if (npcID >0 ) {
				int x = locations[2*id +7] + 0x100*locations[2*id +8];
				int y = locations[300*2 + 2*id +7] + 0x100*locations[300*2 + 2*id +8] - 1;
				int dir = locations[3*300*2 + 2*id +7] + 0x100*locations[3*300*2 + 2*id +8];
				locData.add(new int[] {x, y, npcID-1, dir});
			}
		}
		
		//System.out.println("Valid count of NPCs: " + validNPCs + " at " + locData.size() + " locations.");
		
		
		//Compress our data into a temp vector (one part lazy, one part caution).
		int[] tempRes = new int[nData.length+locations.length];
		tempRes[0] = validNPCs;
		int currPos = 1;
		foundAllNullRow = false;
		for (int npcID=0; npcID<36; npcID++) {
			boolean allZero = true;
			for (int i=0; i<30; i++) {
				if (nData[7 + npcID*30 + i]!=0) {
					allZero = false;
					break;
				}
			}
			
			if (allZero && foundAllNullRow)
				continue;
			else
				foundAllNullRow = true;
			
			//This NPC is valid. Enter data for it, combining with the location data in .L
			//System.out.println("  NPC: " + npcID);
			tempRes[currPos++] = npcID;
			for (int i=0; i<30; i++) {
				int val = nData[7 + npcID*30 + i];
				if (i==6) {
					//Translate the speed
					if (val==3)
						tempRes[currPos++] = 10;
					else
						tempRes[currPos++] = val;

					continue;
				} else if (i==0) {
					if (((Boolean)walkaboutTracer.get(val)).booleanValue()) {
						//System.out.println("   NPC " + npcID + " has a null graphic");
						tempRes[currPos++] = 0;
						continue;
					} else {
						tempRes[currPos++] = val + 1;
						continue;
					}
				}
				tempRes[currPos++] = val;
			}
			int sizeLoc = currPos;
			int currSize = 0;
			currPos++; //Fill in later
			for (int row=0; row<locData.size(); row++) {
				if (((int[])locData.get(row))[2] != npcID)
					continue;
				int[] rowData = (int[])locData.remove(row);
				row--;
				currSize++;
				
				//System.out.println("    (" + rowData[0] + "," + rowData[1] +")");
				
				//Enter it: X
				tempRes[currPos++] = rowData[0]&0xFF;
				tempRes[currPos++] = (rowData[0]&0xFF00)/0x100;
				//Enter it: Y
				tempRes[currPos++] = rowData[1]&0xFF;
				tempRes[currPos++] = (rowData[1]&0xFF00)/0x100;
				//Enter it: Direction
				tempRes[currPos++] = rowData[3];
			}
			tempRes[sizeLoc] = currSize;
		}
		
		//Sanity check
		if (locData.size()>0) {
			System.out.println("Error! Some residual data in .L");
			//for (int[] res : locData)
			for (int rI=0; rI<locData.size(); rI++) {
				int[] res = (int[])locData.get(rI);
				System.out.println("  " + res[2] + ": (" + res[0] + "," + res[1] +")");
			}
		}
		
		//Now, prepare our return data
		int[] res = new int[currPos];
		for (int i=0; i<currPos; i++)
			res[i] = tempRes[i];
		return res;
	}

	private static int[] postProcessPassability(int[] rawData) {
		int width = rawData[7] + 0x100*rawData[8];
		int height = rawData[9] + 0x100*rawData[10];
		
		//Get a count
		int numZero = 0;
		for (int i=0; i<width*height; i++) {
			if(rawData[i + 11] == 0)
				numZero++;
		}
		
		//Special case: no walls. Else, prepare our return value
		if (numZero == width*height)
			return new int[] {};
		int[] res = new int[(int)(Math.ceil(((width*height-numZero)*9 + numZero)/8.0))];

		//Populate: body (the easy way)
		ArrayList/*<Integer>*/ currByte = new ArrayList/*<Integer>*/();
		int pos = 0;
		for (int tileID=0; tileID<width*height; tileID++) {
			int val = rawData[tileID + 11];
			
			//Now, break it down
			if (val==0)
				currByte.add(new Integer(0));
			else {
				currByte.add(new Integer(1));
				StringBuilder sb = new StringBuilder();
				for (int i=7; i>=0; i--) {
					if ((val&(1<<i))==0) {
						currByte.add(new Integer(0));
						sb.append("0");
					} else {
						currByte.add(new Integer(1));
						sb.append("1");
					}
				}
				//if (map5)
					//System.out.println(sb.toString());
			}
			
			//Finally, set a byte, if necessary
			boolean lastTileFlag = tileID==width*height-1;
			while (currByte.size()>7 || lastTileFlag) {
				
				if (tileID==width*height-1) {
				System.out.print("Last byte("+lastTileFlag+") ");
				//for (int item : currByte)
				for (int iI=0; iI<currByte.size(); iI++)
					System.out.print(((Integer)currByte.get(iI)).intValue());
				System.out.println();
				}
				
				int byteVal = 0;
				int[] bits = new int[8];
				for (int i=0; i<8 && currByte.size()>0; i++)
					bits[i] = ((Integer)currByte.remove(0)).intValue();
				
				for (int i=7; i>=0; i--)
					byteVal += bits[7-i]*(1<<i); 

				res[pos++] = byteVal;
				//System.out.println("     : " + Integer.toBinaryString(byteVal));
				lastTileFlag = false;
			}
		}
		
		//System.out.println("Sanity check: " + pos + "/" + res.length);
		return res;
	}
	
	private static int[] postProcessFoemap(int[] rawData) {
		int width = rawData[7] + 0x100*rawData[8];
		int height = rawData[9] + 0x100*rawData[10];
		int[] tileData = new int[width*height];
		HashMap/*<Integer, Integer>*/ valCounts = new HashMap/*<Integer, Integer>*/();
		for (int i=0; i<width*height; i++) {
			int val = rawData[i + 11];
			if (!valCounts.containsKey(new Integer(val)))
				valCounts.put(new Integer(val), new Integer(0));
			valCounts.put(new Integer(val), new Integer(((Integer)valCounts.get(new Integer(val))).intValue()+1));
			tileData[i] = val;
		}
		
		//Sort it
		//System.out.println("Dictionary:");
		ArrayList/*<int[]>*/ sortedCounts = new ArrayList/*<int[]>*/();
		int bodySize = 0; //Number of bits in the body
		while (!valCounts.isEmpty()) {
			int[] max = new int[2];
			max[0] = -1;
			Object[] keySet = valCounts.keySet().toArray();
			for (int keyI=0; keyI<keySet.length; keyI++) {
				Integer key = (Integer)keySet[keyI];
				if (((Integer)valCounts.get(key)).intValue() > max[1]) {
					max[0] = key.intValue();
					max[1] = ((Integer)valCounts.get(key)).intValue();
				}
			}
			valCounts.remove(new Integer(max[0]));
			sortedCounts.add(max);
			//System.out.println("Entry: " + max[0] + "   (count: " + max[1] + ")");
			bodySize += sortedCounts.size() * max[1];
		}
		
		if (sortedCounts.size()==1) {
			//Special case (implicit)
			return new int[] {1, ((int[])sortedCounts.get(0))[0]};
		}
		
		//Create the result set
		int[] res = new int[
		        1								//Number of entries in dictionary
		      + sortedCounts.size()				//Each dictionary entry   
		      + (int)(Math.ceil(bodySize/8.0))	//Number of bits, rounded up
		];
		
		//Populate: header
		res[0] = sortedCounts.size();
		int pos = 1;
		//for (int[] pair : sortedCounts) {
		for (int pI=0; pI<sortedCounts.size(); pI++) {
			int[] pair = (int[])sortedCounts.get(pI);
			res[pos++] = pair[0];
		}
		
		//Populate: body (the easy way)
		ArrayList/*<Integer>*/ currByte = new ArrayList/*<Integer>*/();
		for (int tileID=0; tileID<tileData.length; tileID++) {
			int val = tileData[tileID];
			
			//Find it
			int huff=0;
			for (huff=0; huff<sortedCounts.size(); huff++) {
				if (((int[])sortedCounts.get(huff))[0] == val)
					break;
			}
			if (huff==sortedCounts.size())
				throw new RuntimeException("Error! No foemap entry for: " + val);
			
			//Now, break it down
			for (int i=0; i<=huff; i++) {
				if (i==huff)
					currByte.add(new Integer(0));
				else
					currByte.add(new Integer(1));
			}
			
			//Finally, set a byte (or more), if necessary
			boolean oneOffCheck = tileID==tileData.length-1;
			while (currByte.size()>7 || oneOffCheck) {
				int byteVal = 0;
				int[] bits = new int[8];
				for (int i=0; i<8 && currByte.size()>0; i++)
					bits[i] = ((Integer)currByte.remove(0)).intValue();
				
				for (int i=0; i<8; i++)
					byteVal += (bits[i]*(1<<(7-i))); 

				res[pos++] = byteVal;
				oneOffCheck = false;
			}
		}
		
		//System.out.println("Sanity check: " + pos + "/" + res.length);
		return res;
	}
	

	private static FileInputStream combineMapData(int mapPiece, FileInputStream input, int currMap, long currSize) {
		int sizeOfMiniLump=0;
		boolean read = false;
		switch (mapPiece) {
		case DOOR_IDS:
			if (!read) {
				sizeOfMiniLump = 600;
				read = true;
			}
		case GEN_MAP_INFO:
			if (!read) {
				sizeOfMiniLump = 40;
				read = true;
			}
		case MAP_NAME:
			if (!read) {
				sizeOfMiniLump = 80;
				read = true;
			}
			int id=0;
			System.out.println("   Size: " + currSize + "/" + sizeOfMiniLump);
			while (currSize>=sizeOfMiniLump) {
				//Make room, if necessary
				if (mapLumps.size() == id)
					mapLumps.add(new int[MAP_SEGMENT_MAX][]);

				//Read the data directly into the maps lump
				byte[] readData = new byte[sizeOfMiniLump];
				try {
					input.read(readData);
				} catch (IOException ex) {}
				int[] resData = new int[readData.length];
				for (int i=0; i<readData.length; i++)
					resData[i] = readData[i];

				((int[][])mapLumps.get(id))[mapPiece] = resData;

				id++;
				currSize -= sizeOfMiniLump;

			}
			break;
		case DOOR_LINKS:
		case TILES:
		case PASSABILITY:
		case ENEMY_MAP:
		case NPC_LOCATIONS:
		case NPC_DATA:
			//Make sure we've enough maps for the general data.
			while (mapLumps.size() <= currMap)
				mapLumps.add(new int[MAP_SEGMENT_MAX][]);

			//Read the data directly into the maps lump
			byte[] readData = new byte[(int)currSize];
			try {
				input.read(readData);
			} catch (IOException ex) {}
			int[] resData = new int[readData.length];
			for (int i=0; i<readData.length; i++)
				resData[i] = readData[i];

			((int[][])mapLumps.get(currMap))[mapPiece] = resData;
			break;
		}

		return input;
	}



	/**
	 * @param args [0] = input file (.RPG) [1] = output file (.XRPG) [2] = empty output directory?
	 */
	public static void main(String[] args) {
		if (args.length!=3)
			throw new RuntimeException("Usage: RPG2XPRG <input file> <output path> <clear output directory?>");

		RPG2XRPG.convert(new File(args[0]), new File(args[1]), Boolean.parseBoolean(args[2]));
		System.out.println("Run completed");

	}

}
