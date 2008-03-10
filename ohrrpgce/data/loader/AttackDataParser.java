package ohrrpgce.data.loader;


import java.io.InputStream;

import ohrrpgce.data.RPG;
import ohrrpgce.data.Spell;

public class AttackDataParser extends LumpParser {

	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            throw new RuntimeException("Old-style attack data not supported.");
        }
        
        public long readCompressedLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            for (int id=0; id<result.getNumAttacks(); id++) {
                Spell currAttack = result.getAttack(id);
                
                //Strings
                currAttack.attackName = readNTString(input);
              //  System.out.println("   >" + currAttack.attackName);
                currAttack.attackCaption = readNTString(input);
                currAttack.spellDescription = readNTString(input);
                
                //Fixed-length stuff
                readByte(input); //Anim palette
                readInt(input); //Anim pict, Base Attack
                readByte(input); //Attacker Anim, Attack Anim
                readInt(input); //Anim Pattern, Caption Delay
                readInt(input); //Caption display time, wasted
                readByte(input); //Target Class, Target Setting, wasted
                readByte(input); //Target Stat, Base Def. Stat
                readInt(input); //Attack Delay, number of hits
                readByte(input);readByte(input);readByte(input); //Damage, Aim, Chain%, Chain
                readInt(input); //Extra dmg, some bitsets
                currAttack.mpCost = LumpParser.readTwosComplementInt(input); //mp cost
              //  System.out.println("MP: " + currAttack.mpCost);
                LumpParser.readTwosComplementInt(input); //hp cost
                LumpParser.readTwosComplementInt(input); //money cost
                
                //Bitsets!
                readByte(input);
                readByte(input);
                readByte(input);
                readByte(input);
                readByte(input);
                readByte(input);
                readByte(input);
                currAttack.useableOutsideBattle = ((readByte(input)&0x8)!=0); //Is it backwards? 0_0
                
                //Tag nonsense
                int tagLump = readByte(input);
              //  System.out.println("     >" + Integer.toBinaryString(tagLump));
                if ((tagLump&0xE0)!=0) {
                    readInt(input); //Tag to set
                    if ((tagLump&0x2)==0)
                        readInt(input); //Tag check
                }
                if ((tagLump&0x1C)!=0) {
                    readInt(input); //Tag to set
                    if ((tagLump&0x1)==0)
                        readInt(input); //Tag check
                }
            }
            
            return 0;
        }

}

