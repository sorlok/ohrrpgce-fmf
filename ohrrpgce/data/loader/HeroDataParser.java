package ohrrpgce.data.loader;

import java.io.InputStream;
import java.io.IOException;
import ohrrpgce.data.Hero;
import ohrrpgce.data.RPG;


public class HeroDataParser extends LumpParser {
	private static final int DT0_HERO_NAME_MAX_SIZE = 34;
	private static final int DT0_HERO_NAME_NUM_SIZE_BYTES = 2;

	
	public long readCompressedLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
		int numHeroes = readByte(input);
                result.setNumHeroes(numHeroes); //Avoid a null problem elsewhere
		int multipliersMultiplexed = readByte(input); 
		int[] mpMult = new int[] {(multipliersMultiplexed&0xC0)/0x40+1, (multipliersMultiplexed&0x30)/0x10+1};
		int[] genMult = new int[] {(multipliersMultiplexed&0x0C)/0x04+1, (multipliersMultiplexed&0x03)+1};
		//System.out.println("Test MP: " + mpMult[0] + ":" + mpMult[1]);
		//System.out.println("Test Gen: " + genMult[0] + ":" + genMult[1]);
		for (int id=0; id<numHeroes; id++) {
			//Get the name; at the same time, determine if this is a valid hero
			Hero currH = result.getHero(id);
			String name = "";
			int firstByte = readByte(input);
			if (firstByte==0xFF)
				continue;
			else if (firstByte!='\0') //Non-null heroes with null names are ok.
				name = (char)firstByte + readNTString(input);
                       // System.out.println("Loading hero: " + name);
			currH.name = name;
			
			//General data
                        currH.setBattleSprite(readByte(input));
                        currH.battleSpritePaletteID = readByte(input); 
                        currH.setWalkabout(readByte(input));
                        currH.walkaboutPaletteID = readByte(input);
                        currH.defaultLevel = readByte(input);
                        readByte(input); //Default weapon
                        
                        //Level curves
                        currH.setStatRange(Hero.STAT_HP, readInt(input), readInt(input));
                        currH.setStatRange(Hero.STAT_MP, readByte(input)*mpMult[0], readByte(input)*mpMult[1]);
                        for (int i=Hero.STAT_STRENGTH; i<=Hero.STAT_EXTRA_HITS; i++) {
                            currH.setStatRange(i, readByte(input)*genMult[0], readByte(input)*genMult[1]);
                           // System.out.println("Stat: " + i + " : " + currH.getStatMin(i) + "-->" + currH.getStatMax(i));
                        }

                        //Spell data
                        int numSpellsMultiplexed = readByte(input)*0x10000 + readByte(input)*0x100 + readByte(input);
                        int[] numSpells = new int[] {
                            //XXXX1111 12222233 33344444
                            (numSpellsMultiplexed&0xF8000)/0x8000,
                            (numSpellsMultiplexed&0x7C00)/0x400,
                            (numSpellsMultiplexed&0x3E0)/0x20,
                            (numSpellsMultiplexed&0x1F),
                        };
                      //  System.out.println("   Num spells: " + Integer.toBinaryString(numSpellsMultiplexed));)
                      //  System.out.println("Hero spells for: " + currH.name);
                        for (int i=0; i<numSpells.length; i++) {
                          //  System.out.println("Group " + i);
                            currH.spells[i] = new int[numSpells[i]];
                            currH.spellLearnLevels[i] = new int[numSpells[i]];
                            for (int k=0; k<numSpells[i]; k++) {
                                currH.spells[i][k] = readByte(input) - 1;
                                //System.out.println("Spell: " + currH.spells[i][k]);
                                currH.spellLearnLevels[i][k] = readByte(input)-1;
                            }
                        }

                        //Hero bitsets
                        readByte(input);
                        readByte(input);
                        readByte(input);
                        readByte(input);
                        
                        //Spell list names
                        String[] sNames = new String[] {
                            readNTString(input),
                            readNTString(input),
                            readNTString(input),
                            readNTString(input)
                        };
                        int sNameCount = 0;
                        for (int i=0; i<sNames.length; i++) {
                            if (sNames[i].length()>0)
                                sNameCount++;
                        }
                        {
                            String[] res = new String[sNameCount];
                            sNameCount = 0;
                            for (int i=0; i<sNames.length; i++) {
                                if (sNames[i].length()>0)
                                    res[sNameCount++] = sNames[i];
                            }
                            currH.setSpellNames(res);
                        }
                        

                        //Spell list types
                        currH.spellGroupTypes[0] = readByte(input);
                        currH.spellGroupTypes[1] = readByte(input);
                        currH.spellGroupTypes[2] = readByte(input);
                        currH.spellGroupTypes[3] = readByte(input);

                        //Hero tags
                        readByte(input);
                        readByte(input);
                        readByte(input);
                        readByte(input);
                        readByte(input);

                        //Weapon co-ordinates
                        readInt(input);
                        readInt(input);
                        readInt(input);
                        readInt(input);
		}
		
		return 0;
	}
	
	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            for (int i=0; i<result.getNumHeroes(); i++) {
                Hero currH = result.getHero(i);
                currH.name = readFixedString(input, DT0_HERO_NAME_NUM_SIZE_BYTES, DT0_HERO_NAME_MAX_SIZE, true);
                
                readInt(input); //Hero sprite
                readInt(input); //Hero sprite palette
                
                currH.setWalkabout(readInt(input));
                currH.walkaboutPaletteID = readInt(input);
                
                currH.defaultLevel = readInt(input);
                if (currH.defaultLevel==0xFFFF)
                	currH.defaultLevel=0xFF;
                readInt(input); //Default weapon
                
                currH.setStatRange(Hero.STAT_HP, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_MP, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_STRENGTH, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_ACCURACY, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_DEFENSE, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_DODGE_PERCENT, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_MAGIC, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_WILL, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_SPEED, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_COUNTER, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_FOCUS, readInt(input), readInt(input));
                currH.setStatRange(Hero.STAT_EXTRA_HITS, readInt(input), readInt(input));
                
                try {
                    input.skip(636 - 47*2);
                } catch (IOException ex) {}
                
            }
            
            return 0;
        }

}
