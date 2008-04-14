package ohrrpgce.menu;

import java.util.Vector;

import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.Message;
import ohrrpgce.game.LiteException;

public class TextSlice extends MenuSlice {
	//Stored text
	private String text;
	private String[] lines;
	
	//Properties of our font
	private ImageAdapter font;
	private int blockSize;
	private boolean shade;
	private boolean skipNLSymbol;
	
	//Hackish
	private boolean autoDiet;
	
	
	public TextSlice(MenuFormatArgs mFormat, String text, ImageAdapter font, boolean skipNLSymbol, boolean shade, boolean autoDiet) {
		super(mFormat);
		
		this.text = text;
		this.font = font;
		this.skipNLSymbol = skipNLSymbol;
		this.autoDiet = autoDiet;
		this.shade = shade;
		this.blockSize = Message.FONT_MARGIN+Message.FONT_SIZE;
		if (shade)
			blockSize++;
	}
	
	
    private boolean layoutText() {
    	//Can we?
    	if (getWidth()==0 || getHeight()==0)
    		return false;
    	
    	if (lines==null) {
    		System.out.println("fit lines");
    		fitLines();
    		System.out.println("fit lines done");
    	}
        bufferText();
        
        return true;
    }

    
    
    /**
     * Fit the "text" string into a number of entries in "lines", which 
     *  pays attention to the fact that the actual line may need to be wrapped.
     * METHOD: Use the given newlines, if possible. Else, ditch them last first.
     *  Also, ditch multiple newlines which are followed by nothing.
     */
    public void fitLines() {
        int charsPerLine = (getWidth()-mFormat.borderPadding*2-mFormat.borderColors.length*2)/blockSize;
        int currLine = 0;

        //Temporary structures
        StringBuffer sb = new StringBuffer((3*charsPerLine)/2);
        Vector res = new Vector();
        
        //Run through all characters
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            
            //At newlines, spaces, or the last character, we must check to see
            //   if we've created a line that's too long for the display.
            if (c=='\n' || c==' ' || i==text.length()-1) {
                if (sb.length()>charsPerLine) {
                    int splitOn = sb.toString().lastIndexOf(' ');
                    if (splitOn > charsPerLine/2) {
                        //If there's more than one word on this line, and it
                        //   occurs relatively near to the end of the line.
                        res.addElement(sb.toString().substring(0, splitOn));
                        currLine++;
                        sb.delete(0, splitOn+1);
                    } else {
                        //Either we've got a HUGE word on this line (and some smaller ones)
                        // or a HUGE word that won't fit no matter how many times
                        // we search for spaces.
                        res.addElement(sb.toString().substring(0, charsPerLine-1)+"-");
                        currLine++;
                        sb.delete(0, charsPerLine-1);
                    }
                    
                    //Insert a newline symbol, so the user knows we've artificially split.
                    if (!skipNLSymbol)
                        sb.insert(0, '\n');
                }
            }
            
            //If it's a newline, add this as a (natural) new line.
            //If it's the last character, add the new line.
            if (c=='\n') {
                res.addElement(sb.toString());
                currLine++;
                sb.delete(0, sb.length());
            } else {
                //Add this character.
                sb.append(c);
                
                if (i==text.length()-1 && sb.length()>0) {
                    res.addElement(sb.toString());
                    currLine++;
                }
            }
        }
        
        //Copy over
        lines = new String[res.size()];
        for (int i=0; i<lines.length; i++) {
            lines[i] = (String)res.elementAt(i);
        }
    }
    
    
    public void bufferText() {
        //For readability.
        int fs = Message.FONT_SIZE;
        int lpl = Message.LETTERS_PER_LINE;
        
        //Start anew
        this.clearPixelBuffer();
        
        //Read each line; each letter
        for (int lineNum=0; lineNum<lines.length; lineNum++) {
            char[] currLine = lines[lineNum].toCharArray();
            
            //Note that we layout one pixel lower than normal. This helps text appear more "balanced".
            int yPos = mFormat.borderPadding + mFormat.borderColors.length + 1 + blockSize*lineNum;
            int xPos = mFormat.borderPadding + mFormat.borderColors.length;
            for (int i=0; i<currLine.length; i++) {
                //Get the pixel data
                int[] letter = new int[fs*fs];
                font.getRGB(letter, 0, fs, fs*(currLine[i]%lpl), fs*(currLine[i]/lpl), fs, fs);

                //Shade
                int color = 0xFF000000;
                for (int offset=1; offset>=0; offset--) {
                    if (offset==0 || shade) {
                        //Copy each row
                        for (int y=0; y<fs; y++) {
                            for (int x=0; x<fs; x++) {
                                if ((letter[y*fs + x]&0x00FFFFFF)!=0)
                                	this.setPixel(xPos+x+offset, yPos+y+offset,  color);
                            }
                        }
                    }
   
                    color = 0xFFFFFFFF;
                }

                //Increment
                xPos += blockSize;
            }
        }
    }

    
    
	/**
	 * For a text box, this is the width of the box if it's on one line.
	 *  This is intended to be used only for single-line boxes.
	 *  Note that Text Slices have no lower padding. 
	 */
	protected int calcMinWidth() {
		return blockSize*text.length() - mFormat.borderPadding;
	}
	
	
	/**
	 * Calculate the height of a text box including word wrap
	 *  This requires the width to be determined and non-zero
	 *  Note that Text Slices have no lower padding.
	 */
	protected int calcMinHeight() {
		//Can we proceed?
		if (getWidth()==0)
			throw new LiteException(this, new IllegalArgumentException(), "A TextSlice cannot have a width of zero (did you call doVerticalLayout() before doHorizontalLayout?)");
		
		//Determine the lines in this box. Return that value
		fitLines();
	
		return blockSize*lines.length - mFormat.borderPadding + 1;
	}
	
	
	//Over-ride....
	protected void setWidth(int newWidth) {		
		super.setWidth(newWidth);
		if (layoutText() && autoDiet) {
			makeSkinny();
		}
	}
	protected void setHeight(int newHeight) {		
		super.setHeight(newHeight);
		if (layoutText() && autoDiet) {
			makeSkinny();
		}
	}
	public void setText(String text) {
		this.text = text;
		this.lines = null;
		layoutText();
	}
	private void makeSkinny() {
		//Calculate width/height
		String line = "";
		for (int i=0; i<lines.length; i++) {
			if (lines[i].length() > line.length())
				line = lines[i];
		}
		
		int amtPadded = mFormat.borderPadding*2 + mFormat.borderColors.length*2;
		int newWidth = line.length()*blockSize + amtPadded;
		int newHeight = lines.length*blockSize + amtPadded + 1;
		
		//Set, and recalc
		super.setWidth(newWidth);
		super.setHeight(newHeight);
		layoutText();
	}

}

