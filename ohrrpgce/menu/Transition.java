/*
 * Transition.java
 * Created on April 18, 2007, 11:37 PM
 */

package ohrrpgce.menu;

import java.util.Vector;

/**
 * Allows top-level control of menu transitions.
 * @author Seth N. Hetu
 */
public abstract class Transition {
    private Vector actions; //Vector<Object[]>, where obj[0] = time, obj[1] = Action
    private int currStep;
    private int currIndex;
    
   /* public Transition() {
        init();
    }*/
    
    protected void init() {
        actions = new Vector();
        setupActions();
        reset();
    }
    
    public abstract void setupActions();
    
    /**
     * Actions must be added in monotonic order.
     */
    public void addAction(int time, Action act) {
        actions.addElement(new Object[]{new Integer(time), act});
    }
    
    public void reset() {
        currStep = 0;
        currIndex = 0;
    }
    
    public boolean step() {
        currStep++;
        if (isDone())
            throw new RuntimeException("Illegal step: this Transition has finished!");
        
        Object[] nextPair = (Object[])actions.elementAt(currIndex);
        int nextTime = ((Integer)nextPair[0]).intValue();
        if (currStep == nextTime) {
        //    System.out.println("Performing: " + currStep);
            ((Action)nextPair[1]).perform(this);
            currIndex++;
        }
        
        return isDone();
    }
    
    public boolean isDone() {
        return currIndex==actions.size();
    }
    
}
