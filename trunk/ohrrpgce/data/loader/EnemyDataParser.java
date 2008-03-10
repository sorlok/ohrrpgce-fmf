package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.Enemy;
import ohrrpgce.data.RPG;

public class EnemyDataParser extends LumpParser {
        private static final int STAT_MAX = 12;
        private static final int BITSET_MAX = 40;
    
	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            throw new RuntimeException("Old-style enemy data not supported.");
        }

        
        public long readCompressedLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
            //Useful lookup table
            int[] maskLookup = new int[16];
            maskLookup[0] = 1;
            for (int i=1; i<16; i++) {
                maskLookup[i] = (maskLookup[i-1]<<1)+1;
            }
            
            //Read the header
            int[] commonWidths = new int[STAT_MAX];
            boolean[] skipBits = new boolean[BITSET_MAX];
            boolean[] skipBitVals = new boolean[BITSET_MAX];
            for (int i=0; i<STAT_MAX; i+=2) {
                int nextByte = readByte(input);
                commonWidths[i] = nextByte>>4;
                commonWidths[i+1] = nextByte&0xF;
            }
            for (int i=0; i<BITSET_MAX; i+=4) {
                int nextByte = readByte(input);
                int mask = 0x80;
                for (int bit=0; bit<8; bit++) {
                    if (bit%2==0)
                        skipBits[i/2] = ((nextByte&mask)!=0);
                    else
                        skipBits[i/2] = ((nextByte&mask)!=0);
                    mask >>= 1;
                }
            }
            
            //Read the records
            for (int enID=0; enID<result.getNumEnemies(); enID++) {
                Enemy curr = result.getEnemy(enID);
                
                //Name, picture
                curr.name = readNTString(input);
                curr.setSprite(readByte(input));
                
                //Palette, PicSize, NumReg
                int nextByte = 0x100*readByte(input)+readByte(input);
                curr.spritePaletteID = nextByte>>5;
                curr.spriteSize = (nextByte&0x18)>>3;
                curr.normalAttacks = new int[nextByte&0x7];
                
                //Desperation attacks, alone attacks, thievery
                nextByte = readByte(input);
                curr.desperAttacks = new int[nextByte>>5];
                curr.aloneAttacks = new int[(nextByte&0x1C)>>2];
                int thieve = nextByte&0x3;
                
                //Items to steal?
                if (thieve>0) {
                    int regStealPerc = readByte(input);
                    if (regStealPerc>0) {
                        int regStealItem = readByte(input);
                    }
                    int rareStealPerc = readByte(input);
                    if (rareStealPerc>0) {
                        int rareStealItem = readByte(input);
                    }
                }
                
                //Battle rewards
                readInt(input); //Experience
                readInt(input); //Gold
                
                //Common stat widths & Item drops
                int[] statWidths = null;
                nextByte = readByte(input);
                if ((nextByte&0x80)!=0)
                    statWidths = commonWidths;
                int itemDropPerc = nextByte&0x7F;
                if (itemDropPerc>0)
                    readByte(input); //Item dropped
                nextByte = readByte(input);
                int rareDropPerc = nextByte&0x7F;
                if (rareDropPerc>0)
                    readByte(input); //Item dropped: rare
                
                //Stat widths?
                if (statWidths==null) {
                    statWidths = new int[STAT_MAX];
                    for (int i=0; i<STAT_MAX; i+=2) {
                        nextByte = readByte(input);
                        statWidths[i] = nextByte>>4;
                        statWidths[i+1] = nextByte&0xF;
                    }
                }
                
                //Now! The only mildly complex part. 
                // First: Enemy stats!
                int segment = readByte(input);
                int bitsLeft = 8;
                for (int stat=0; stat<STAT_MAX; stat++) {
                    int statBits = statWidths[stat] + 1;
                    int statMask = maskLookup[statBits-1];
                    
                    //Do we have enough bits for this number?
                    while (statBits>bitsLeft) {
                        segment = (segment<<8) + readByte(input);
                        bitsLeft += 8;
                    }
                    
                    //Read the value
                    bitsLeft -= statBits;
                    int val = ((segment>>bitsLeft)&statMask);
                    //SET the value
                }
                
                // Second: bitsets
                for (int bit=0; bit<BITSET_MAX; bit++) {
                    boolean val = skipBitVals[bit];
                    if (!skipBits[bit]) {
                        //Do we have enough bits for this number?
                        if (bitsLeft==0) {
                            segment = readByte(input);
                            bitsLeft += 8;
                        }
                        bitsLeft--;
                        val = (segment&(1<<bitsLeft))!=0;
                        //SET the bit
                    }
                }
                
                
                //I suppose spawn pairs can be a bit complex...
                nextByte = readByte(input);
                int numSpawn = nextByte>>4;
                if (numSpawn > 0) {
                    //Read the first spawn pair...
                    int spawnID = nextByte&0xF;
                    int spawnVal = readByte(input);
                    int countToSpawn = readByte(input);
                    //SET
                    
                    int rem = -1;
                    for (int i=1; i<numSpawn; i++) {
                        if (rem==-1) {
                            nextByte = readByte(input);
                            spawnID = nextByte>>4;
                            spawnVal = (nextByte&0xF)<<4;
                            nextByte = readByte(input);
                            spawnVal += (nextByte>>4);
                            rem = spawnVal&0xF;
                            //SET
                        } else {
                            spawnID = rem;
                            spawnVal = readByte(input);
                            rem = -1;
                        }
                    }
                }
                
                //Attacks are simple
                for (int i=0; i<curr.normalAttacks.length; i++)
                    curr.normalAttacks[i] = readByte(input);
                for (int i=0; i<curr.desperAttacks.length; i++)
                    curr.desperAttacks[i] = readByte(input);
                for (int i=0; i<curr.aloneAttacks.length; i++)
                    curr.aloneAttacks[i] = readByte(input);
            }
            
            return 0;
        }
}








