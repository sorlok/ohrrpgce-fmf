/*
 * MenuItem.java
 * Created on April 18, 2007, 11:08 PM
 */

package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;

/**
 * Rather than do an iterative mess of a menu, we'll have some object-oriented
 *  design. The atomic unit of this is the MenuItem.
 * @author Seth N. Hetu
 */
public abstract class MenuItem {
    public static final int CONNECT_TOP = 0;
    public static final int CONNECT_BOTTOM = 1;
    public static final int CONNECT_LEFT = 2;
    public static final int CONNECT_RIGHT = 3;
    public static final int CONNECT_HORIZONTAL = 0;
    public static final int CONNECT_VERTICAL = 1;
    
    private int helperTextID = 0;
    private int[] position = new int[2];
    protected int[] dimension = new int[2];
    protected int[] lastPaintedOffset = new int[2];
    private MenuItem[] connections = new MenuItem[4];
    private boolean[] blocked = new boolean[]{false, false, false, false};
    private boolean paintedFlag;

    //Event handlers specific to all menu items
    private Action focusGainedListener;
    private Action focusLostListener;    //If this returns "false", then don't move!
    private Action cancelListener;
    
    //User-specific, a la SWT
    private Object data;
    
    
    public void blockConnection(int dir) {
        blocked[dir] = true;
    }
   
            
    
    /**
     * Clears the paint flag for THIS AND ALL CONNECTED components.
     *  Note that, to prevent infinite recursion, all connected components
     *  which do not have the paint flag set are not recursed on.
     *  In other words, if A -> B -> C, and A.alreadyPainted, ~B.alreadyPainted,
     *  C.alreadyPainted, calling clearPaintFlag() on A will NOT reach C.
     */
    public void clearPaintFlag() {
        //Self
        this.paintedFlag = false;
        
        //Recurse
        for (int i=0; i<connections.length; i++) {
            if (connections[i]!=null && connections[i].alreadyPainted())
                connections[i].clearPaintFlag();
        }
    }
    public boolean alreadyPainted() {
        return paintedFlag;
    }
    
    
    //Position
    public void setPosition(int x, int y) {
        position[0] = x;
        position[1] = y;
    }
    public int getPosX() {
        return position[0];
    }
    public int getPosY() {
        return position[1];
    }
    
    //Dimension
    public void setSize(int width, int height) {
        dimension[0] = width;
        dimension[1] = height;
    }
    public int getWidth() {
        if (dimension[0]==-1)
            throw new RuntimeException("getWidth() undefined for type: " + getClass().getName().toString());
        return dimension[0];
    }
    public int getHeight() {
        if (dimension[1]==-1)
            throw new RuntimeException("getHeight() undefined for type: " + getClass().getName().toString());
        return dimension[1];
    }

    public void setFocusGainedListener(Action focusGainedListener) {
        this.focusGainedListener = focusGainedListener;
    }

    public void setFocusLostListener(Action focusLostListener) {
        this.focusLostListener = focusLostListener;
    }
    
    public void setCancelListener(Action cancelListener) {
        this.cancelListener = cancelListener;
    }
    
    
    //Connections
    /**
     * Connect a second item to this menu item.
     * @param secondary The item to connect to this.
     * @param connectOn Either of CONNECT_TOP/BOTTOM/LEFT/RIGHT.
     */
    public void connect(MenuItem secondary, int connectOn) {
        int converse = converse(connectOn);
        if (converse==-1)
            throw new RuntimeException("MenuItem cannot be connected: connectOn set to: " + connectOn);
        if (getConnect(connectOn)!=null || secondary.getConnect(converse)!=null)
            throw new RuntimeException("MenuItem cannot be connected: An item is already connected on: " + connectOn  + " (or: " + converse + ")");

        //Connection applies to both objects.
        this.setConnect(secondary, connectOn);
        secondary.setConnect(this, converse);
    }
    
    
    public void disconnect(int disconOn) {
        int converse = converse(disconOn);
        if (converse==-1)
            throw new RuntimeException("MenuItem cannot be disconnected: disconOn set to: " + disconOn);        
        if (getConnect(disconOn)==null || getConnect(disconOn).getConnect(converse)==null)
            throw new RuntimeException("MenuItem cannot be disconnected: no item connected to: " + disconOn + " (or: " + converse + ")");
        getConnect(disconOn).remConnect(converse);
        this.remConnect(disconOn);
    }
    
    //Assumed to be safe
    protected void setConnect(MenuItem secondary, int connectOn) {
        connections[connectOn] = secondary;
    }
    protected void remConnect(int disconOn) {
        connections[disconOn] = null;
    }
    public MenuItem getConnect(int dir) {
        return connections[dir];
    }
    
    
    private int converse(int dir) {
        switch (dir) {
            case CONNECT_TOP:
                return CONNECT_BOTTOM;
            case CONNECT_BOTTOM:
                return CONNECT_TOP;
            case CONNECT_LEFT:
                return CONNECT_RIGHT;
            case CONNECT_RIGHT:
                return CONNECT_LEFT;
            default:
                return -1;
        }
    }
    
    
    public MenuItem moveTo() {
        if (focusGainedListener!=null)
            focusGainedListener.perform(this);
        //Err... later.
        
        return this;
    }
   
    
    /**
     * Triggers the paint method of THIS AND ALL CONNECTED components exactly once.
     *    Note that a call to repaint() will only be successful if the alreadyPainted()
     *    flag is OFF for this component.
     * @param g The current graphics context
     * @param originOffset [+x,+y], how far from the top-left corner this component starts.
     */
    public void repaint(int[] originOffset) {
        //Prevent an infinite loop
        if (alreadyPainted())
            return;
        
    /*    System.out.println("Printing: " + getClass().getName().toString());
        System.out.println("    origin: " + originOffset[0] + "," + originOffset[1]);
        System.out.println("    (x,y) " + getPosX() + "," + getPosY());
        System.out.println("    (w,h) " + "___" + "," + getHeight());*/
        
        //Paint self
     //   System.out.println("Painting self: " + this.getClass().getName().toString());
        paint(originOffset);
      //  System.out.println(" >done");
        lastPaintedOffset[0] = originOffset[0];
        lastPaintedOffset[1] = originOffset[1];
        paintedFlag = true;
        
        //RE-paint neighbors - recurse
        if (connections[CONNECT_RIGHT]!=null)
            connections[CONNECT_RIGHT].repaint(new int[]{
                originOffset[0]+getPosX()+getWidth(),
                originOffset[1]+getPosY()
            });
        if (connections[CONNECT_BOTTOM]!=null)
            connections[CONNECT_BOTTOM].repaint(new int[]{
                originOffset[0]+getPosX(),
                originOffset[1]+getPosY()+getHeight()
            });
        if (connections[CONNECT_LEFT]!=null) {
           /* System.out.println(getWidth());
            System.out.println(getWidth());
            System.out.println("  "+originOffset[0]);
            System.out.println("  "+getPosX());
            System.out.println("  "+connections[CONNECT_LEFT].getPosX());
            System.out.println("  "+connections[CONNECT_LEFT].getWidth());*/
            connections[CONNECT_LEFT].repaint(new int[]{
                originOffset[0]+getPosX()-2*connections[CONNECT_LEFT].getPosX()-connections[CONNECT_LEFT].getWidth(),
                originOffset[1]+getPosY()
            });
        }
        if (connections[CONNECT_TOP]!=null)
            connections[CONNECT_TOP].repaint(new int[]{
                originOffset[0]+getPosX(),
                originOffset[1]+getPosY()-2*connections[CONNECT_TOP].getPosY()-connections[CONNECT_TOP].getHeight()
            });        
    }
    
    
    //////////////////////////////
    //Un-implemented functionality
    //////////////////////////////
    
    /**
     * A MenuItem should be able to paint itself given just a 
     *  graphics context and an offset. The offset is passed here since it can
     *  dynamically change over time (e.g., if a FlatList has an item added to it
     *  that's longer than the current max item.)
     * @param g The Grahpics object to paint onto
     * @param originOffset [+x,+y], how far from the top-left corner this component starts.
     */
    protected abstract void paint(int[] originOffset);
    
    
    /**
     * A MenuItem responds when keys are pressed.
     * @param direction The key that was pressed. This must already be pre-processed.
     * @return The current active menu item.
     */
    public MenuItem processInput(int direction) {
        if (getConnect(direction)==null || blocked[direction])
            return this;
        
        boolean doMove = true;
        if (focusLostListener!=null)
            doMove = focusLostListener.perform(this);
        if (doMove) {
            getConnect(direction).moveTo();
            return getConnect(direction);
        } else {
            return this;
        }

    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getLastPaintedOffsetX() {
        return lastPaintedOffset[0];
    }
   
    public int getLastPaintedOffsetY() {
        return lastPaintedOffset[1];
    }
    
    public int getHelperTextID() {
        return helperTextID;
    }
    public void setHelperTextID(int val) {
        helperTextID = val;
    }

    
    /**
     * Called whenever the user hits a menu button (interpreted as cancel)
     */
    public MenuItem cancel() {
        if (cancelListener!=null)
            cancelListener.perform(this);

        return this;
    }

    
    /**
     * Called whenever the user hits 5 (enter)
     */
    public abstract void accept();
    
    
    /**
     * When this item is called for the first time, or whenever deemed appropriate,
     *  this function handles whatever is necessary to set it back to its original state.
     *  Layout data and connections are NOT reset.
     */
    public abstract void reset();
    
    
    /*
     * Composable sub-classes should over-ride this and return the
     * MenuItem which is actually the current focus. This allows highlights
     * to be drawn in a suitable fasion, etc.
     */
    public MenuItem getActiveSubItem() {
        return this;
    }

}
