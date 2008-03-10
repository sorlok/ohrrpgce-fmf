/*
 * Composite.java
 * Created on May 1, 2007, 7:53 PM
 */

package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;


/**
 * A class for holding rows or columns of menu items.
 *  SUPER-IMPORTANT NOTE: A Composite can certainly contain other composites,
 *  and in principle a composite can contain its parent. However, the getActiveSubComponent()
 *  method (used as of 5/07 for drawing cursors) will recurse forever; in this case, a 
 *  "buck stops here" variable should be created and checked before returning currItem.getASC()
 * @author Seth N. Hetu
 */
public class Composite extends MenuItem {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    
    protected boolean isVertical;
    protected MenuItem[] items;
    protected MenuItem currItem;
    
    protected Composite() {
        items = new MenuItem[0];
    }
    
    public Composite(MenuItem[] items, int orientation) {
        if (items.length==0)
            throw new RuntimeException("A composite must have at least one item.");
        this.items = items;
        if (orientation==HORIZONTAL)
            isVertical = false;
        else if (orientation==VERTICAL)
            isVertical = true;
        else
            throw new RuntimeException("Composite orientation must be either HORIZONTAL or VERTICAL."); 
       
        connectItems();
        currItem = items[0];
        
        setSize(-1, items[0].getHeight());
    }
    


    protected void connectItems() {
        if (items.length==1)
            return;
        
        //Now, connect all items
       // int totalWidth = 0;
        for (int i=0; i<items.length-1; i++) {
            if (isVertical)
                items[i].connect(items[i+1], MenuItem.CONNECT_BOTTOM);
            else
                items[i].connect(items[i+1], MenuItem.CONNECT_RIGHT);
        }
        if (isVertical)
            items[items.length-1].connect(items[0], MenuItem.CONNECT_BOTTOM);
        else
            items[items.length-1].connect(items[0], MenuItem.CONNECT_RIGHT);
        
    }
    
    public MenuItem processInput(int direction) {
        if (isVertical) {
            if (direction==MenuItem.CONNECT_RIGHT||direction==MenuItem.CONNECT_LEFT)
                return super.processInput(direction);
            else
                currItem = currItem.processInput(direction);
        } else {
            if (direction==MenuItem.CONNECT_TOP||direction==MenuItem.CONNECT_BOTTOM)
                return super.processInput(direction);
            else
                currItem = currItem.processInput(direction);            
        }
        return this;    
    }

    public void reset() {
        currItem = items[0];
    }

    public void accept() {
        currItem.accept();
       // return this;
    }

    public MenuItem cancel() {
        currItem.cancel();
        return this;
    }

    protected void paint(int[] originOffset) {
        //Delegate painting to sub-components recursively.
        items[0].repaint(new int[]{originOffset[0]+getPosX(), originOffset[1]+getPosY()});
        items[0].clearPaintFlag();
    }
    

    public MenuItem getActiveSubItem() {
        return currItem.getActiveSubItem();
    }
    
    
    public MenuItem moveTo() {
        super.moveTo();
        currItem.moveTo();
        return this;
    }
    
    public int getNumSubItems() {
        return items.length;
    }
    
    public MenuItem getSubItem(int id) {
        return items[id];
    }

    

    
}
