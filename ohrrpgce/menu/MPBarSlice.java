package ohrrpgce.menu;


import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.RPG;

public class MPBarSlice extends MenuSlice {
	
	//private RPG game;
	private int[] barColors;
	private int storedWidth;
	private int storedHeight;
	
	private int barWidth;
	private int barLength;
	
	//Cached
	private String reloadTxt;
	private int reloadMidX;
	private boolean reloadNeeded;
	
	//Inner components
	private TextSlice mpText;
	
	
	/**
	 * Userful helper constructor
	 * @param mFormat - Doesn't need bgColor, borderColors, or fillType
	 * @param game
	 * @param txtBoxColorID - Used to fill in mFormat
	 */
    public MPBarSlice(MenuFormatArgs mFormat, RPG game, int txtBoxColorID) {
        this(mFormat, 3, new int[]{0xFF0000, 0xD43F3F, 0x8A0101, 0x660000}, game.font);
        
        int[] colors = game.getTextBoxColors(txtBoxColorID);
        this.mFormat.bgColor = 0xFF000000|colors[0];
        this.mFormat.borderColors = new int[]{colors[1], 0};
        this.mFormat.fillType = MenuSlice.FILL_SOLID;
    }
    
    /**
     * Full control over the MP bar.
     * @param mpDigits The maximum number of digits any character's mp can reach. E.g., 99->2, 255->3
     * @param barColors Determines the height of the box; an entry for each horizontal line.
     */
    public MPBarSlice(MenuFormatArgs mFormat, int mpDigits, int[] barColors, ImageAdapter font) {
    	super(mFormat);
    	
    	//System-wide default
    	if (this.getInitialFormatArgs().borderPadding==0)
    		this.getInitialFormatArgs().borderPadding = 4;
    	
        //For later
      //  this.game = game;
        this.barColors = barColors;
        
        //Dummy string
        String txt = "MP: /";
        for (int i=0; i<mpDigits; i++)
            txt += "99";
         
        MenuFormatArgs mf = new MenuFormatArgs();
        mf.borderColors = new int[]{};
        mf.fillType = MenuSlice.FILL_NONE;
        mf.widthHint = MenuFormatArgs.WIDTH_MINIMUM;
        mf.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
        mpText = new TextSlice(mf, txt, font, true, true, false);
        mpText.doLayout();
        //this.setTopLeftChild(mpText);
     //   int halfMargin = (mpText.getHeight()-Message.FONT_SIZE)/2;
        this.storedWidth = mpText.getWidth();
        this.storedHeight = mpText.getHeight()+barColors.length+2 + 1;
        
        barWidth = storedWidth;
        barLength = barColors.length+2;
    }
	
    
    
    //Set the type...
    public void setToRandom() {
        reloadData("Random", barWidth);
    }
	
    public void setMP(int curr, int max, boolean isFF4Type, int spellCategoryLvl) {        
        String txt = "";
        int midPointX = (curr*barWidth)/max;
        if (isFF4Type)
            txt = "Lv. " + spellCategoryLvl + ": " + curr;
        else
            txt = "MP: " + curr + "/" + max;
        
        reloadData(txt, midPointX);
    }
    
    //Messy, but it works:
    private void reloadData(String txt, int midPointX) {    	
    	//Can we do this now?
    	if (this.getWidth()==0 || this.getHeight()==0) {
        	this.reloadTxt = txt;
        	this.reloadMidX = midPointX;
    		this.reloadNeeded = true;
    		return;
    	}
    	
        //First, reset the text box
    	mpText.setText(txt);
    	//mpText.getInitialFormatArgs().xHint = storedWidth/2 - mpText.getWidth()/2;
    	//mpText.doHorizontalLayout(new Vector(), this, new Int(0));
    	
    	//Prepare our offsets
    	int barXOff = this.getInitialFormatArgs().borderPadding + this.getInitialFormatArgs().borderColors.length;
    	int barYOff = barXOff + mpText.getHeight() + 1; 
        
        //Next, we fill the entire bar
        int black = 0xFF000000; //Black
        this.clearPixelBuffer();
        for (int y=0; y<barLength; y++) {
            for (int x=0; x<barWidth; x++) {
                int color = 0;
                if (y==0 || y==barLength-1)
                    color = black;
                else
                    color = barColors[y-1]; //Vermillion. If it works, use it.
                
                setPixel(barXOff+x, barYOff+y, color);
            }
        }
        
        //Next, we fill from the midpoint to the end, making the line diagonal for fun.
        for (int y=1; y<barLength-1; y++) {
            int sans = y-1-barColors.length/2;
            if (midPointX<=barColors.length || midPointX+barColors.length>=barWidth)
                sans = 0; //Silly, silly.
            boolean first = true;
            for (int x=midPointX-sans; x<barWidth; x++) {
                int color = 0xFFB8B8B8; //30% gray. (Nothing for this one.)
                if (first) {
                    first = false;
                    color = 0xFF707070; //50% gray = Always. Love.
                }
                setPixel(barXOff+x, barYOff+y, color);
            }
        }
        
        //Now, the little triangular bits, and the transparentcy left/right of them.
        int ptOff = 0;
        if (barColors.length%2==0)
            ptOff = 1;
        //TL
        for (int y=0; y<barLength/2; y++) {
            for (int x=0; x<barColors.length/2; x++) {
                if (x+y<barColors.length/2)
                	setPixel(barXOff+x, barYOff+y, 0);
                else if (x+y==barColors.length/2)
                	setPixel(barXOff+x, barYOff+y, black);
            }
        }
        //BL
        for (int y=barLength/2; y<barLength; y++) {
            for (int x=0; x<barColors.length/2; x++) {
                if (y-x+ptOff>barColors.length)
                	setPixel(barXOff+x, barYOff+y, 0);
                else if (y-x+ptOff==barColors.length)
                	setPixel(barXOff+x, barYOff+y, black);
            }
        }
        //TR
        for (int y=0; y<barLength/2; y++) {
            for (int x=0; x<barColors.length/2; x++) {
                if (y+x<barColors.length/2)
                	setPixel(barXOff+barWidth-x-1, barYOff+y, 0);
                else if (y+x==barColors.length/2)
                	setPixel(barXOff+barWidth-x-1, barYOff+y, black);
            }
        }
        //BR
        for (int y=barLength/2; y<barLength; y++) {
            for (int x=0; x<barColors.length/2; x++) {
                if (y-x-ptOff>barColors.length/2)
                	setPixel(barXOff+barWidth-x-1, barYOff+y, 0);
                else if (y-x-ptOff==barColors.length/2)
                	setPixel(barXOff+barWidth-x-1, barYOff+y, black);
            }
        }
    }
        
    //Important over-rides
    protected void drawPixelBuffer(int atX, int atY) {
    	super.drawPixelBuffer(atX, atY);
    	mpText.paintAt(getPosX()+getWidth()/2-mpText.getWidth()/2, getPosY()+this.getInitialFormatArgs().borderColors.length+this.getInitialFormatArgs().borderPadding);
    }
    protected void setWidth(int newWidth) {
    	super.setWidth(newWidth);
    	
    	//Reload?
    	if (reloadNeeded) {
    		reloadNeeded = false;
    		reloadData(reloadTxt, reloadMidX);
    	}
    }
    protected void setHeight(int newHeight) {
    	super.setHeight(newHeight);
    	
    	//Reload?
    	if (reloadNeeded) {
    		reloadNeeded = false;
    		reloadData(reloadTxt, reloadMidX);
    	}
    }
    protected int calcMinWidth() {
    	return storedWidth;
    }
    protected int calcMinHeight() {
    	return storedHeight;
    }
}
