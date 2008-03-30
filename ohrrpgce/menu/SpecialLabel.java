/*
 * SpecialLabel.java
 * Created on July 5, 2007, 10:31 AM
 */

package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;

/**
 * A hackish way to include a clip, but I don't want every menu component checking for a clipping rectangle (yet).
 *  Expect this class to disappear in the future.
 * @author Seth N. Hetu
 */
public class SpecialLabel extends Label{
    private int[] clip;
    private int[] full;
    
    public SpecialLabel(TextBox txt, int[] full) {
        super(txt);
        //this.clip = clip;
        this.full = full;
    }
    
    public void setClip(int[] clip) {
        this.clip = clip;
    }

    protected void paint(int[] originOffset) {
       // g.setColor(0x00FF00);
        //g.fillRect(clip[0], clip[1], clip[2], clip[3]);
        if (clip==null)
            throw new RuntimeException("SpecialLabel MUST call \"setClip()\"");
        GraphicsAdapter.setClip(clip[0], clip[1], clip[2], clip[3]);
        super.paint(originOffset);
        GraphicsAdapter.setClip(full[0], full[1], full[2], full[3]);
    }    

    
}
