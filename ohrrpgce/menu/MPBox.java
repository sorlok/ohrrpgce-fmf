/*
 * MPBox.java
 * Created on May 15, 2007, 4:17 PM
 */

package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.Message;
import ohrrpgce.data.RPG;
import ohrrpgce.data.TextBox;

/**
 * Simple box which dispplays one's current MP in a category, with a bar.
 * @author Seth N. Hetu
 */
public class MPBox extends MenuItem {
    
    private TextBox mpTxt;
    private Canvas bkgrdBox;
    
    private int[] bar;
    private int barWidth;
    
    private RPG parentGame;
    private int mpBarMargin;
    private int[] barColors;
    
    /**
     * Full control over the MP box.
     * @param mpDigits The maximum number of digits any character's mp can reach. E.g., 99->2, 255->3
     * @param mpBarMargin How many pixels to put between the border of this box and each side of the mp bar.
     * @param barColors Determines the height of the box; an entry for each horizontal line.
     */
    public MPBox(int mpDigits, int mpBarMargin, int[] barColors, RPG game, int txtBoxColorID) {
        //For later
        this.parentGame = game;
        this.mpBarMargin = mpBarMargin;
        this.barColors = barColors;
        
        //Dummy string
        String txt = "MP: /";
        for (int i=0; i<mpDigits; i++)
            txt += "99";
        int[] colors = game.getTextBoxColors(txtBoxColorID);
        
        mpTxt = new TextBox(txt, game.font, 0, 0, true, Canvas.FILL_NONE);
        int halfMargin = (mpTxt.getHeight()-Message.FONT_SIZE)/2;
        bkgrdBox = new Canvas(
                mpTxt.getWidth(),
                mpTxt.getHeight()+halfMargin+barColors.length+2,
                0xFF000000|colors[0],
                new int[]{0xFF000000|colors[1], 0xFF000000}, Canvas.FILL_SOLID);
        this.setSize(bkgrdBox.getWidth(), bkgrdBox.getHeight());
    }
    
    public MPBox(RPG game, int txtBoxColorID) {
        this(3, 4, new int[]{0xFFFF0000, 0xFFD43F3F, 0xFF8A0101, 0xFF660000}, game, txtBoxColorID);
    }
    
    
    public void setToRandom() {
        if (bar==null)
            initBar();
        
        reloadData("Random", barWidth);
    }
    
    private void reloadData(String txt, int midPointX) {        
        //First, reset the text box
        mpTxt =  new TextBox(txt, parentGame.font, 0, 0, true, Canvas.FILL_NONE);
        mpTxt.setPosition((getWidth()-mpTxt.getWidth())/2, 0);
        
        
        //First, we fill the entire bar
        int black = 0xFF000000; //Black
        for (int y=0; y<bar.length/barWidth; y++) {
            for (int x=0; x<barWidth; x++) {
                int color = 0;
                if (y==0 || y==bar.length/barWidth-1)
                    color = black;
                else
                    color = barColors[y-1]; //Vermillion. If it works, use it.
                bar[y*barWidth+x] = color;
            }
        }
        
        //Next, we fill from the midpoint to the end, making the line diagonal for fun.
        for (int y=1; y<bar.length/barWidth-1; y++) {
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
                bar[y*barWidth+x] = color;
            }
        }
        
        //Now, the little triangular bits, and the transparentcy left/right of them.
        int ptOff = 0;
        if (barColors.length%2==0)
            ptOff = 1;
        //TL
        for (int y=0; y<bar.length/barWidth/2; y++) {
            for (int x=0; x<barColors.length/2; x++) {
                if (x+y<barColors.length/2)
                    bar[y*barWidth+x] = 0;
                else if (x+y==barColors.length/2)
                    bar[y*barWidth+x] = black;
            }
        }
        //BL
        for (int y=bar.length/barWidth/2; y<bar.length/barWidth; y++) {
            for (int x=0; x<barColors.length/2; x++) {
                if (y-x+ptOff>barColors.length)
                    bar[y*barWidth+x] = 0;
                else if (y-x+ptOff==barColors.length)
                    bar[y*barWidth+x] = black;
            }
        }
        //TR
        for (int y=0; y<bar.length/barWidth/2; y++) {
            for (int x=0; x<barColors.length/2; x++) {
                if (y+x<barColors.length/2)
                    bar[y*barWidth+(barWidth-x-1)] = 0;
                else if (y+x==barColors.length/2)
                    bar[y*barWidth+(barWidth-x-1)] = black;
            }
        }
        //BR
        for (int y=bar.length/barWidth/2; y<bar.length/barWidth; y++) {
            for (int x=0; x<barColors.length/2; x++) {
                if (y-x-ptOff>barColors.length/2)
                    bar[y*barWidth+(barWidth-x-1)] = 0;
                else if (y-x-ptOff==barColors.length/2)
                    bar[y*barWidth+(barWidth-x-1)] = black;
            }
        }
    }
    
    private void initBar() {
        //Init the % bar
        barWidth = getWidth()-mpBarMargin*2;
        bar = new int[barWidth*(barColors.length+2)];
    }
    
    public void setMP(int curr, int max, boolean isFF4Type, int spellCategoryLvl) {
        if (bar==null)
            initBar();
        
        String txt = "";
        int midPointX = (curr*barWidth)/max;
        if (isFF4Type)
            txt = "Lv. " + spellCategoryLvl + ": " + curr;
        else
            txt = "MP: " + curr + "/" + max;
        
        reloadData(txt, midPointX);
    }
    
    
    
    
    protected void paint(int[] originOffset) {
        int drawFlags = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
        int x = originOffset[0]+getPosX();
        int y = originOffset[1]+getPosY();
        
        //Paint the background box & text box
        bkgrdBox.paint(x, y, drawFlags);
        mpTxt.paint(x+(getWidth()-mpTxt.getWidth())/2, y+1, drawFlags);
        
        //Paint the bar
        if (bar!=null) {
            x += mpBarMargin;
            y += mpTxt.getHeight()+1;
            GraphicsAdapter.drawRGB(bar, 0, barWidth, x, y, barWidth, bar.length/barWidth, true);
        }
    }
    
    
    
    public void accept() {
        //return this;
    }
    

    public void reset() {}
    
    
    
}
