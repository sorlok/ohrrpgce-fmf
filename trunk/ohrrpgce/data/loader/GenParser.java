package ohrrpgce.data.loader;

import java.io.InputStream;
import java.io.IOException;

import ohrrpgce.data.Passcode;
import ohrrpgce.data.RPG;

public class GenParser extends LumpParser {
	private static final int PASSCODE_BYTES_USED = 17;
	private static final int PASSCODE_BYTES_UNUSED = 1;
	private static final int PASSCODE_FIRST_BYTES = 20; 
	
	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
		System.out.println("Reading GEN lump");
		
		//Bsave nonsense
		int len = readBSaveLength(input);
		System.out.println("Len: " + len);

		//Prop: max maps
		int maxMaps = readInt(input);
		result.setNumMaps(maxMaps+1); //Idiot
		
		//Skip
		readInt(input); //Title screen index in MXS
		readInt(input); //Title music
		readInt(input); //Victory music
		readInt(input); //Battle music
		
		//Passcode info
		int passFormat = readInt(input);
		int passRotator = readInt(input);
		byte[] passEncrypt = readBytes(input, PASSCODE_BYTES_USED);
		try {
			input.skip(PASSCODE_BYTES_UNUSED);
		} catch (IOException ex) {}
		if (passFormat == 256) {
			boolean emptyPass = true;
			for (int i=0; i<passEncrypt.length; i++) {
				if (passEncrypt[i]-passRotator > ' ') {
					emptyPass = false;
				}
			}
			if (emptyPass)
				result.setPasscode(Passcode.PASSCODE_NONE);
			else
				result.setPasscode(Passcode.PASSCODE_THIRD_STYLE, passRotator, passEncrypt);
		} else 
			result.setPasscode(Passcode.PASSCODE_SECOND_SYTLE); //Not quite right... skips level 2/1 passcodes.
		
		//Skip first-style passcode data
		try {
			input.skip(PASSCODE_FIRST_BYTES);
		} catch (IOException ex) {}
		
                
		//Read some maxes
		result.setNumBattleSprites(readInt(input)+1); //PT0
		result.setNumSmallEnemySprites(readInt(input)+1); //PT1
		result.setNumMediumEnemySprites(readInt(input)+1); //PT2
		result.setNumLargeEnemySprites(readInt(input)+1); //PT3
		result.setNumWalkabouts(readInt(input)+1); //PT4
		readInt(input); //PT5
		readInt(input); //PT6
		result.setNumTilesets(readInt(input)+1); 
		result.setNumAttacks(readInt(input)+1);
		result.setNumHeroes(readInt(input)+1);
		result.setNumEnemies(readInt(input)+1); //DT1
		result.setNumFormations(readInt(input)+1);
		result.setNumPalettes(readInt(input)+1);
                result.setNumTextBoxes(readInt(input)+1);
                System.out.println("Num text boxes: " + result.getNumTextBoxes());
        result.setNumPlotscripts(readInt(input)+1);
                readInt(input); //New game plotscript
                readInt(input); //Gameover plotscript
                readInt(input); //Highest manually-numbered plotscript
                readInt(input); //Suspendstuff bits
                readInt(input); //Camera mode
                readInt(input); //Camera arg 1
                readInt(input); //Camera arg 2
                readInt(input); //Camera arg 3
                readInt(input); //Camera arg 4
                readInt(input); //Current script backdrop in mxs
                readInt(input); //Days of play
                readInt(input); //Hours of play
                readInt(input); //Minutes of play
                readInt(input); //Seconds of play
                result.setNumVehicles(readInt(input)+1); 
                result.setNumTagNames(readInt(input)+1);//Not sure if this is right.
                for (int i=0; i<43; i++)
                    readInt(input);
                ModeXScreenParser.DEBUG_MXS = readInt(input); //For now...
                
                //General bitsets
                int genBits = readInt(input);
                result.permit2xTriggeredScripts = ((genBits & 0x400 ) != 0);
        		  
                result.setStartingPosition(readInt(input), readInt(input));
                result.setStartingMap(readInt(input)); 

		
		//Skip the rest for now.
                return length - (
                          7     //BSave
                        + 7*2   //ints before password
                        + 18    //password
                        + 10*2  //old password
                        + 13*2  //up to palettes
                        + 18*2  //up to num tags
                        + 48*2  //up to start map
                );
	}

}
