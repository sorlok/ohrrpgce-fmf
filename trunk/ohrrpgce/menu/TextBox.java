/*
 * TextBox.java
 * Created on April 9, 2007, 1:25 PM
 */

package ohrrpgce.menu;

import java.util.Vector;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.Message;
import ohrrpgce.game.LiteException;

/**
 * An extension to Box which allows for the placement of (possibly shadowed)
 *  text. Has support for word-wrap and hyphenation.
 * @author sethhetu
 */
public class TextBox  {
    
    private int borderColor;
    private int bkgrdColor;
    private boolean shade;
    private int transparencyFlag;
    private String rawString;
    private ImageAdapter font;
    private boolean skipNLSymbol;
    private int maxW;
    private int maxH;
    
    private String[] lines;
    
    public static int MAX_WIDTH=-1;
    public static int MAX_HEIGHT=-1;
    public static void initTextBox(int width, int height) {
        MAX_WIDTH = width;
        MAX_HEIGHT = height;
    }
    

    private Canvas background;
    
    
    private boolean calcd; //Because I'm superstitious that a null check is more costly than a boolean check.
    

    /**
     * Create a text box.
     * @param lines is how we want it to appear, with embedded \n's. The actual lines may vary.
     * @param borderColor,bkgrdColor are RRGGBB values
     * @param shade if true will draw a shadow for each character. This increases the box size.
     * @param transparency allows further control over transparency/translucency/opacity
     * @param skipNLSymbol will not display the newline symbol when a string is cut.
     * @param forcedSize overrides the MAX_WIDTH, MAX_HEIGHT default.
     */
    public TextBox(String lines, ImageAdapter font, int borderColor, int bkgrdColor, boolean shade, int transparency, boolean skipNLSymbol, int[] forcedSize) {
    //    System.out.println("new Text Box: " + lines + " t: " + transparency);
    	if (transparency==Canvas.FILL_NONE)
    		background = new Canvas(0, new int[]{}, transparency);
    	else if (transparency==Canvas.FILL_SOLID)
    		background = new Canvas(bkgrdColor, new int[]{borderColor, 0}, transparency);
    	else if (transparency==Canvas.FILL_TRANSLUCENT)
    		background = new Canvas(0xDD000000|bkgrdColor, new int[]{borderColor, 0}, transparency);
    	else if (transparency==Canvas.FILL_GUESS)
    		throw new LiteException(this, null, "Cannot \"GUESS\" a text box's background");
    	
        //Defer expensive computations until later.
        this.borderColor = borderColor;
        this.bkgrdColor = bkgrdColor;
//        System.out.println("make: " + (lines==null));
        this.rawString = lines;
        this.shade = shade;
        this.font = font;
        this.transparencyFlag = transparency;
        this.skipNLSymbol = skipNLSymbol;
        this.maxW = forcedSize[0];
        this.maxH = forcedSize[1];
    }
    
    /**
     * Create a text box.
     * @param lines is how we want it to appear, with embedded \n's. The actual lines may vary.
     * @param borderColor,bkgrdColor are RRGGBB values
     * @shade if true will draw a shadow for each character. This increases the box size.
     */
    public TextBox(String lines, ImageAdapter font, int borderColor, int bkgrdColor, boolean shade) {
        this(lines, font, borderColor, bkgrdColor, shade, Canvas.FILL_TRANSLUCENT, false, new int[]{MAX_WIDTH, MAX_HEIGHT});
    }
    
    public TextBox(String lines, ImageAdapter font, int borderColor, int bkgrdColor, boolean shade, int transparency) {
        this(lines, font, borderColor, bkgrdColor, shade, transparency, false, new int[]{MAX_WIDTH, MAX_HEIGHT});
    }

    private void calculateBox() {
        fitLines();
        initBox();
        drawText();
        calcd = true;
    }
    
    /**
     * Draw this as a message box (top-aligned).
     * @param gConext is the Graphics object.
     */
    public void paint(int offset, int screenWidth) {
        if (!calcd) {
            //System.out.println("Calculate: " + rawString);
            calculateBox();
        }
        background.paint(screenWidth/2, offset, GraphicsAdapter.TOP|GraphicsAdapter.HCENTER);
    }


    public void paint(int x, int y, int drawFlags) {
        if (!calcd) {
            //System.out.println("Calculate: " + rawString);
            calculateBox();
        }
        background.paint(x, y, drawFlags);
    }
    
    public void paint() {
    	paint(getPosX(), getPosY(), background.getLayoutRule());
    }
    

    
    
    /**
     * Fits the string in "lines" onto the "actualLines" string, which 
     *  pays attention to the fact that the actual line may need to be wrapped.
     * METHOD: Use the given newlines, if possible. Else, ditch them last first.
     *  Also, ditch multiple newlines which are followed by nothing.
     */
    public void fitLines() {
  //  	System.out.println("fit lines");
        if (MAX_WIDTH==-1 || MAX_HEIGHT==-1)
            throw new LiteException(this, null, "Message Box constants not initialized.");
        
        int blockSize = Message.FONT_MARGIN+Message.FONT_SIZE;
        if (shade)
            blockSize++;
        int charsPerLine = (maxW-Message.FONT_MARGIN)/blockSize;
        int currLine = 0;

        //Temporary structures
        StringBuffer sb = new StringBuffer((3*charsPerLine)/2);
        Vector res = new Vector();
        
   //     System.out.println("compute " +  (rawString==null));
        
        for (int i=0; i<rawString.length(); i++) {
            char c = rawString.charAt(i);
            
            //At newlines, spaces, or the last character, we must check to see
            //   if we've created a line that's too long for the display.
            if (c=='\n' || c==' ' || i==rawString.length()-1) {
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
                
                if (i==rawString.length()-1 && sb.length()>0) {
                    res.addElement(sb.toString());
                    currLine++;
                }
            }
        }
        rawString = null; //Save space.
        
  //      System.out.println("copy");
        
        //Copy over
        lines = new String[res.size()];
        for (int i=0; i<lines.length; i++) {
            lines[i] = (String)res.elementAt(i);
        }
    }
    
    
    public void initBox() {
        int maxWidth=0;
        int maxHeight=lines.length;
        for (int i=0; i<lines.length; i++) {
            if (lines[i].length()>maxWidth)
                maxWidth = lines[i].length();
        }
        
        //Convert to pixels. 
        // Actual width = characters' space + left&top margin + border margin
        int blockSize = Message.FONT_SIZE + Message.FONT_MARGIN;
        if (shade)
            blockSize++;
        maxWidth = maxWidth*blockSize  + Message.FONT_MARGIN + 4;
        maxHeight = maxHeight*blockSize  + Message.FONT_MARGIN + 4;
        
        //Create the box.
        background.setSize(maxWidth, maxHeight);
//        int alpha = 0xFF000000;
     //   if (transparencyFlag==Canvas.FILL_TRANSLUCENT)
      //      alpha = 0xDD000000;
        //if (transparencyFlag) {
            /*this.fillBackground(alpha|bkgrdColor);
            this.drawBorders(new int[] {0xFF000000|borderColor, 0xFF000000});*/
        //}
    }

    public void drawText() {
        //For readability.
        int fs = Message.FONT_SIZE;
        int lpl = Message.LETTERS_PER_LINE;
        int blockSize = Message.FONT_MARGIN+Message.FONT_SIZE;
        if (shade)
            blockSize++;
   //     System.out.println("Setting text box pixels: " + lines[0]);
        for (int lineNum=0; lineNum<lines.length; lineNum++) {
            char[] currLine = lines[lineNum].toCharArray();
            int yPos = Message.FONT_MARGIN + 2 + blockSize*lineNum; //+2 for borders
            int xPos = Message.FONT_MARGIN + 2; //+2 for borders
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
                                  //  System.out.print(".");
                                	background.setPixel(xPos+x+offset, yPos+y+offset,  color);
                            }
                        }
                    }
   
                    color = 0xFFFFFFFF;
                }
              //  System.out.println("");
                

                //Increment
                xPos += blockSize;
            }
        }
       // System.out.println("Done");
    }
    
    
    public int getWidth() {
  //  	System.out.println("GET WIDTH: " + calcd);
        if (!calcd) {
            calculateBox();
        }
        return background.getWidth();
    }

    public int getHeight() {
    //	System.out.println("GET HEIGHT: " + calcd);
        if (!calcd) {
            calculateBox();
        }
        return background.getHeight();
    }
    
    public int getPosX() {
    	return background.getPosX();
    }
    
    public int getPosY() {
    	return background.getPosY();
    }
    
    public void setPosition(int x, int y) {
    	background.setPosition(x, y);
    }
    
    public int getLayoutRule() {
    	return background.getLayoutRule();
    }
    
    public void setLayoutRule(int rule) {
    	background.setLayoutRule(rule);
    }
}
