/*
 * Action.java
 *
 * Created on April 19, 2007, 12:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ohrrpgce.menu;

/**
 * A generic action.
 * @author Seth N. Hetu
 */
public interface Action {
    
    /**
     * Perform an action. Returns a boolean as to whether or not that action completed. 
     */
    public abstract boolean perform(Object caller);
}
