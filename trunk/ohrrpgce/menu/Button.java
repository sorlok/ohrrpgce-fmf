/*
 * IconBox.java
 * Created on April 17, 2007, 5:32 PM
 */

package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.ImageBox;

/**
 * A box which uses an Image as its content.
 * @author Seth N. Hetu
 */
public class Button extends MenuItem {
    private Canvas background;
    private ImageAdapter foreground;
    private ImageBox merged;
    private boolean alreadyMerged;
    private int margin;
    
    private Action buttonPressedListener;
    
    /**
     * Use EITHER an Image OR an ImageBox, but not both and not neither.
     *  Either way, borderColors[] must be of the correct LENGTH, for setting the margin. Sorry!
     */
    public Button(ImageAdapter pic, ImageBox manual, int bgColor, int[] borderColors) {
        if ((pic==null && manual==null)||(pic!=null && manual!=null))
            throw new RuntimeException("Bad use of Button constructor; should be using an Image or an ImageBox, but not both!");
        if (manual!=null) {
            merged = manual;
            margin = borderColors.length;
            alreadyMerged = true;
            setSize(manual.getWidth(), manual.getHeight());
        } else {
            background = new Canvas(pic.getWidth()+borderColors.length*2, pic.getHeight()+borderColors.length*2, bgColor, borderColors, Canvas.FILL_GUESS);
            margin = borderColors.length;
            this.foreground = pic;
            setSize(background.getWidth(), background.getHeight());
        }
    }

    /*public void paint(Graphics g) {
        if (alreadyMerged) {
            merged.paint(g, getPosX()+margin, getPosY()+margin, Graphics.TOP|Graphics.LEFT);
        } else {
            background.paint(g);
            int[] pos = background.getTopLeftCorner();
            g.drawImage(foreground, pos[0]+margin, pos[1]+margin, Graphics.TOP|Graphics.LEFT);
        }
    }*/
    
    public void swapImage(ImageAdapter newImage) {
        if (alreadyMerged)
            throw new RuntimeException("Cannot set a new Image to a Button that uses ImageBoxes");
        foreground = newImage;
    }
    
    public void swapImage(ImageBox newImage) {
        if (!alreadyMerged)
            throw new RuntimeException("Cannot set a new ImageBox to a Button that uses Images");
        merged = newImage;
    }
    
    public ImageAdapter getImage() {
        if (alreadyMerged)
            throw new RuntimeException("This button was made with an ImageBox, not an Image.");
        return foreground;
    }

    public void setButtonPressedListener(Action buttonPressedListener) {
        this.buttonPressedListener = buttonPressedListener;
    }

    public void accept() {
        if (buttonPressedListener!=null)
            buttonPressedListener.perform(this);
        //return this;
    }


    protected void paint(int[] originOffset) {
        int drawFlags = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
        int x = originOffset[0]+getPosX();
        int y = originOffset[1]+getPosY();
        if (alreadyMerged) {
            merged.paint(x, y, drawFlags);
        } else {
            background.paint(x, y, drawFlags);
            int[] pos = background.getTopLeftCorner(x, y, drawFlags);
            GraphicsAdapter.drawImage(foreground, pos[0]+margin, pos[1]+margin, drawFlags);
        }
    }
   
    
    public void reset() {}
    


    
}
