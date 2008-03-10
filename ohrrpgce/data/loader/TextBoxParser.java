/*
 * TextBoxParser.java
 * Created on February 14, 2007, 7:04 PM
 */

package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.RPG;
import ohrrpgce.data.Message;

/**
 *
 * @author Seth N. Hetu
 */
public class TextBoxParser extends LumpParser {
    
	public static final int MAX_BOXES_PER_FILE = 50;
	
    public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
        throw new RuntimeException("Text Boxes cannot currently be parsed for regular (RPG) files.");
    }
    
    public long readCompressedLump(RPG result, InputStream input, int block, long length, HookbackListener hookUpdater) {
        System.out.println("Reading text box block: " + block);
        
        for (int i=block*MAX_BOXES_PER_FILE; i<result.getNumTextBoxes()&&i<(block+1)*MAX_BOXES_PER_FILE; i++) {
            //Read general data
            Message currBox = ((RPG)result).getTextBox(i);
            String msg = readNTString(input); //We can't set it now; we don't know the color.
            readNTString(input); //Choice 1 Text
            readNTString(input); //Choice 2 Text
            readByte(input); //Special
            
            //Now, read Run-Length encoded bytes
            int zeroCounter = 0;
            int intCounter = 0;
            int currVal = 0;
            for (int stat=0; stat<29; stat++) {
                if (zeroCounter>0 ) {
                    //First, read some zeroes
                    currVal = 0;
                    zeroCounter--;
                } else if (intCounter > 0) {
                    currVal = readInt(input);
                   // System.out.println("Int: " + currVal);
                    intCounter--;
                } else {
                    zeroCounter = readByte(input);
                  // System.out.println("Zero counter: " + zeroCounter);
                    if ((zeroCounter&0x80)!=0) {
                        //We stay in int mode after this
                        zeroCounter ^= 0x80;
                        intCounter = readByte(input);
                    } else
                        intCounter = 1;
                    
                    //Get this stat
                    if (zeroCounter >0 ) {
                        currVal = 0;
                        zeroCounter--;
                    } else if (intCounter > 0) {
                        currVal = readInt(input);
                        intCounter--;
                    }
                }
                
               // System.out.println("Stat: " + stat + " : " + currVal);
                
                //Now, store our stat
                if (stat==26) {
                    int[] colors = result.getTextBoxColors(currVal);
                    currBox.boxColor = colors[0];
                    currBox.boxBorderColor = colors[1];
                } else if (stat==2) {
                    currVal = convertTwosComplementInt(currVal);
                    currBox.tagChange1 = new int[]{currVal, 0};
                    currBox.tagChange2 = new int[]{currVal, 0};
                } else if (stat==3) {
                    currBox.tagChange1[1] = convertTwosComplementInt(currVal);
                } else if (stat==4) {
                    currBox.tagChange2[1] = convertTwosComplementInt(currVal);
                } else if (stat==0) {
                    currBox.jumpToBox = new int[] {convertTwosComplementInt(currVal), 0};
                } else if (stat==1) {
                    currBox.jumpToBox[1] = convertTwosComplementInt(currVal);
                } else if (stat==9) {
                    currVal = convertTwosComplementInt(currVal);
                    currBox.heroAddRem = new int[]{currVal, 0};
                    currBox.heroSwap = new int[]{currVal, 0};
                    currBox.heroLock = new int[]{currVal, 0};
                } else if (stat==10) {
                    currBox.heroAddRem[1] = convertTwosComplementInt(currVal);
                } else if (stat==11) {
                    currBox.showBoxAfter = new int[] {convertTwosComplementInt(currVal), 0};
                } else if (stat==12) {
                    currBox.showBoxAfter[1] = convertTwosComplementInt(currVal);
                }
            }
            
            //Finally, it is appropriate to set the text.
            currBox.reset(msg);
        }
        
        return 0;
    }
    

    
}
