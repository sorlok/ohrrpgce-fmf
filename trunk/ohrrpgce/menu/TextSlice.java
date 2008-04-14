package ohrrpgce.menu;

import java.util.Vector;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.Message;
import ohrrpgce.data.NPC;
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
	
	//Auto-scroll long boxes
    private static final int SIZE_HALVED = (Message.FONT_SIZE+1)/2;
    private static final int SIZE_2X = 2*(Message.FONT_SIZE+1);
	private static final int SCROLL_INCREMENT = 9;
    private int scrollAmt;
    private int maxScrollAmt;
    private int screenHeight;
    
    //Cursor Images
    private static int[] cursorDown;
    private static int[] cursorUp;
	
	
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
	
	
	public void setScrollLimit(int scrollAt) {
		this.screenHeight = scrollAt;
	}
	
	
    private boolean layoutText() {
    	//Can we?
    	if (getWidth()==0 || getHeight()==0)
    		return false;
    	
    	if (lines==null) {
    		fitLines();
    	}
    	
    	//Note: auto-skinny needs some help here
    	if (autoDiet)
    		makeSkinny();
    	
        bufferText();
        
        scrollAmt = 0;
        maxScrollAmt = Math.max(this.getHeight()-screenHeight, 0);
        
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
			layoutText();
		}
	}
	protected void setHeight(int newHeight) {		
		super.setHeight(newHeight);
		if (layoutText() && autoDiet) {
			makeSkinny();
			layoutText();
		}
	}
	public void setText(String text) {
		this.text = text;
		this.lines = null;
		
		//Special case
		if (this.mFormat.widthHint==MenuFormatArgs.WIDTH_MINIMUM)
			setWidth(calcMinWidth());
		
		//For standalone text boxes, we need to be careful of auto-skinny....
		if (layoutText() && autoDiet) {
			makeSkinny();
			layoutText();
		}
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
	}
	
    private void scrollBox(int yPlus) {
        scrollAmt += yPlus;
        
        if (scrollAmt<0)
            scrollAmt = 0;
        if (scrollAmt>maxScrollAmt)
            scrollAmt = maxScrollAmt;
    }
    
    
    public boolean consumeInput(int direction) {
    	if (screenHeight==0)
    		return false;
    	
        if (direction==MenuSlice.CONNECT_TOP)
            scrollBox(-SCROLL_INCREMENT);
        else if (direction == MenuSlice.CONNECT_BOTTOM)
            scrollBox(SCROLL_INCREMENT);
        else
        	return false;
        return true;
    }
    
    
    public void paintAt(int x, int y) {
    	//Modify y to accomodate our scroll
    	super.paintAt(x, y-scrollAmt);
    	
        if (maxScrollAmt > 0) {
            //Draw arrows to show that the box can be scrolled.
            if (cursorDown==null)
                loadScrollCursor();
            
            int crX = x+this.getWidth() - 2 - SIZE_HALVED - SIZE_2X;
            int topY = 2+Message.FONT_SIZE/2;
            int lowY = screenHeight - 2 - SIZE_HALVED - SIZE_2X;
            
            if (scrollAmt>0) {
                //Show the "up" arrow.
            	GraphicsAdapter.drawRGB(cursorUp, 0, SIZE_2X, crX, topY, SIZE_2X, SIZE_2X, true);
            }
            if (scrollAmt<maxScrollAmt) {
                //Show the "down" arrow.
            	GraphicsAdapter.drawRGB(cursorDown, 0, SIZE_2X, crX, lowY, SIZE_2X, SIZE_2X, true);
            }
        }
    }
    
    private void loadScrollCursor() {
        int fs = Message.FONT_SIZE;
        int[] letter = new int[fs*fs];
        font.getRGB(letter, 0, fs, fs*(25%Message.LETTERS_PER_LINE), fs*(25/Message.LETTERS_PER_LINE), fs, fs);
        
        //Shade - note: this copied code combines TextBox(shade) & ImageBox(double)
        cursorDown = new int[SIZE_2X*SIZE_2X];
        cursorUp = new int[cursorDown.length];
        int color = 0xFF000000;
        for (int offset=1; offset>=0; offset--) {
            //Copy each row
            for (int y=0; y<fs; y++) {
                for (int x=0; x<fs; x++) {
                    if ((letter[y*fs + x]&0x00FFFFFF)!=0) {
                        for (int yPlus=0; yPlus<2; yPlus++) {
                            for (int xPlus=0; xPlus<2; xPlus++) {
                                cursorDown[(y+offset+yPlus)*SIZE_2X + x+offset+xPlus] = color;
                            }
                        }                            
                    }
                }
            }
            
            color = 0xFFFFFFFF;
        }
        for (int y=0; y<SIZE_2X; y++) {
            for (int x=0; x<SIZE_2X; x++) {
                cursorUp[(SIZE_2X-1-y)*SIZE_2X +x] = cursorDown[y*SIZE_2X +x];
            }
        }
    }
    

}

