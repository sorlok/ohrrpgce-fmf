/*
 * TranslucentBox.java
 */

package ohrrpgce.data;

import ohrrpgce.adapter.GraphicsAdapter;

/**
 *
 * @author Seth N. Hetu
 */
public class TranslucentBox /*implements Box*/{
    /*protected int width;
    protected int height;
    protected int[] pos = new int[]{0,0};
    protected int layoutRule = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
    
    
    //Change to private and see what crashes.
    protected int[] boxData;
    
    **
     * Create an empty box with no certain width and height. 
     * Primarily intended for use by sub-classes.
     *
    public TranslucentBox() {}
    
    **
     * Create a box with a certain background color.
     * @param bgColor AARRGGBB format; the background for the box. Fills the entire box.
     *
    public TranslucentBox (int width, int height, int bgColor) {
        resize(width, height);
        fillBackground(bgColor);
    }
    
    *
     * The position of a box is optional.
     *
    public void setPosition(int x, int y) {
        pos[0] = x;
        pos[1] = y;
    }
    
    public void setLayoutRule(int rule) {
        this.layoutRule = rule;
    }
    
    public int getLayoutRule() {
        return layoutRule;
    }
    
    public int getPosX() {
        return pos[0];
    }
    
    public int getPosY() {
        return pos[1];
    }
    
    **
     * Create a box with a certain background color and some border lines.
     * @width, height MUST assume that "borderColors.length"*2 pixels for the borders
     * @param bgColor AARRGGBB format; the background for the box. Fills the entire box.
     * @param borderColors An array of AARRGGBB values to be applies, from outer to inner, to the box.
     *
    public TranslucentBox (int width, int height, int bgColor, int[] borderColors) {
        this(width, height, bgColor);
        drawBorders(borderColors);
    }
    
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        this.boxData = new int[width*height];
    }
    
    protected void fillBackground(int bgColor) {
        //Fill in.
        for (int h=0; h<height; h++) {
            for (int w=0; w<width; w++) {
                boxData[h*width+w] = bgColor;
            }                
        }
    }
    
    protected void drawBorders(int[] borderColors) {
        //Draw the borders. We can cheat a bit here by drawing in inverse order.
        for (int i=borderColors.length-1; i>=0; i--) {
            //Horizontal
            for (int y=0+i; y<height; y+=(height-1-2*i)) {
                for (int x=0; x<width; x++) {
                     boxData[y*width+x] = borderColors[i];
                }
            }
            //Vertical
            for (int x=0+i; x<width; x+=(width-1-2*i)) {
                for (int y=0; y<height; y++) {
                     boxData[y*width+x] = borderColors[i];
                }
            }
        }
    }
    
    
    **
     * "Draw flags" contains the Graphics flags. 
     *
    public void paint(int x, int y, int drawFlags) {
        if ((drawFlags&GraphicsAdapter.RIGHT)!=0)
            x -= width;
        if ((drawFlags&GraphicsAdapter.HCENTER)!=0)
            x -= width/2;
        if ((drawFlags&GraphicsAdapter.BOTTOM)!=0)
            y -= height;
        if ((drawFlags&GraphicsAdapter.VCENTER)!=0)
            y -= height/2;
        
        GraphicsAdapter.drawRGB(boxData, 0, width, x, y, width, height, true);   
    }
    
    public int[] getTopLeftCorner() {
        return getTopLeftCorner(pos[0], pos[1], layoutRule);
    }
    
    public int[] getTopLeftCorner(int posX, int posY, int drawRule) {
        if ((drawRule&GraphicsAdapter.RIGHT)!=0)
            posX -= width;
        if ((drawRule&GraphicsAdapter.HCENTER)!=0)
            posX -= width/2;
        if ((drawRule&GraphicsAdapter.BOTTOM)!=0)
            posY -= height;
        if ((drawRule&GraphicsAdapter.VCENTER)!=0)
            posY -= height/2;
        
        return new int[]{posX, posY};
    }
    
    **
     * Paint this object, assuming it has sensible, given values for its x/y co-ordinates.
     *
    public void paint() {
    	paint(pos[0], pos[1], layoutRule);
    }
    
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }*/
}
