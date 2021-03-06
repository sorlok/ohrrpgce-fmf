/*
 * Transition.java
 * Created on April 18, 2007, 11:37 PM
 */

package ohrrpgce.menu.transitions;

import ohrrpgce.menu.MenuSlice;

/**
 * Allows top-level control of menu transitions.
 * @author Seth N. Hetu
 */
public abstract class Transition {

	public abstract void reset();
    
    public abstract void step();

    
    /**
     * Usually returns false. Returns true (AND does painting) if 
     *  this is supposed to paint "over" the current graphics buffer. Useful for, e.g., 
     *  loading the screen. Note that returning false means that menu components will
     *  NOT be re-drawn until after this transition completes.  
     */
    public abstract boolean doPaintOver();
    
    
    public abstract boolean isDone();
    public abstract boolean requiresReLayout();
    public abstract MenuSlice getNewFocus();
    

    
}
