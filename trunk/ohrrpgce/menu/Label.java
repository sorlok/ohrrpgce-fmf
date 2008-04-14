/*
 * Label.java
 * Created on May 15, 2007, 3:50 PM
 */

package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.game.SimpleTextBox;

/**
 * Some text. Really just a wrapper for TextBox
 * @author Seth N. Hetu
 */
public class Label extends MenuItem {
    
    private Canvas bkgrdBox;
    private SimpleTextBox baseBox;
    
    
    public Label(SimpleTextBox txt) {
        this.baseBox = txt;
        setSize(txt.getWidth(), txt.getHeight());
    }
    
    /**
     * Constructs a label with a fixed size.
     */
    public Label(int setWidth, int setHeight, int bkgrd, int[] borders) {
        bkgrdBox = new Canvas(setWidth, setHeight, bkgrd, borders, Canvas.FILL_GUESS);
        setSize(setWidth, setHeight);
    }
    
    
    /**
     * Set to "null" to display none.
     */
    public void setTextBox(SimpleTextBox txt) {
        baseBox = txt;
    }
            

    public void accept() {
        //return this;
    }


    public void reset() {}

    
    protected void paint(int[] originOffset) {
        int drawFlags = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
        int x = originOffset[0]+getPosX();
        int y = originOffset[1]+getPosY();
        
        if (bkgrdBox!=null)
            bkgrdBox.paint(x, y, drawFlags);
        
        if (baseBox!=null)
            baseBox.paint(x, y, drawFlags);
    }
    


    

    

    
}
