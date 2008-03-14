/*
 * Box.java
 * Created on April 9, 2007, 1:15 PM
 */

package ohrrpgce.data;

import ohrrpgce.adapter.GraphicsAdapter;

/**
 * A simple class used to create a (possibly semi-transparent) box of a certain 
 *    color, with functionality for painting this box (since they can be of
 *    variable size.)
 * @author Seth N. Hetu
 * @version 0.1
 */
public class SolidBox /*implements Box */{
	/*
    protected int width;
    protected int height;
    protected int[] pos = new int[]{0,0};
    protected int layoutRule = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
    
    protected int bgColor;
    protected int[] borderColors;
    //protected int[] boxData;
    
    **
     * Create an empty box with no certain width and height. 
     * Primarily intended for use by sub-classes.
     *
    public SolidBox() {}
    
    **
     * Create a box with a certain background color.
     * @param bgColor AARRGGBB format; the background for the box. Fills the entire box.
     *
    public SolidBox (int width, int height, int bgColor) {
        resize(width, height);
        this.bgColor = bgColor;
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
    public SolidBox (int width, int height, int bgColor, int[] borderColors) {
        this(width, height, bgColor);
        
        this.bgColor = bgColor;
        this.borderColors = borderColors;
    }
    
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        //this.boxData = new int[width*height];
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
        
        
        //Draw the background box
        GraphicsAdapter.setColor(bgColor);
        GraphicsAdapter.fillRect(x, y, width, height);
        
        //Draw the borders. We can cheat a bit here by drawing in inverse order.
        for (int i=borderColors.length-1; i>=0; i--) {
            //Color
            GraphicsAdapter.setColor(borderColors[i]);
            
            //Horizontal
            for (int pixY=0+i; pixY<height; pixY+=(height-1-2*i)) {
                GraphicsAdapter.drawLine(x+0, y+pixY, x+width, y+pixY);
            }
            //Vertical
            for (int pixX=0+i; pixX<width; pixX+=(width-1-2*i)) {
                GraphicsAdapter.drawLine(x+pixX, y+0, x+pixX, y+height);
            }
        }
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
