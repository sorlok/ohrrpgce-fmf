/*
 * FlatList.java
 * Created on April 19, 2007, 12:19 AM
 */

package ohrrpgce.menu;

import java.util.Vector;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.Box;
import ohrrpgce.data.SolidBox;
import ohrrpgce.data.TextBox;
import ohrrpgce.game.MenuEngine;

/**
 * A Flat List is a list a list of Strings that one can iterate through by 
 *   pressing left/right (alt. up/down). The current choice appears in the 
 *   visible display area; other options are hidden.
 * @author Seth N. Hetu
 */
public class FlatList extends MenuItem {
    private MenuEngine engine;
    private Vector itemText; //Vector of boxes; we assume the text is implicit.
    private String currLongestText;
    private Box bkgrdBox; //For obvious reasons
    private int textBoxColor;
    private int currItem;
    private int margin;
    
    private boolean useArrows;
    private static Box lArrow;
    private static Box rArrow;
    
    private Action listItemChangedListener;
    private Action resizeListener;
    
    
    /**
     * Create a FlatList
     * @param parent The MenuEngine we can use to get RPG information from.
     * @param items The items to be in the list.
     * @param textBoxColor The text box ID color for the this list.
     * @param margin the upper and left margins.
     * @param useArrows If true, displays < and > arrows to the left & right of this list.
     */
    public FlatList(MenuEngine parent, String[] items, int textBoxColor, int margin, boolean useArrows) {
        this.engine = parent;
        this.textBoxColor = textBoxColor;
        this.margin = margin;
        this.useArrows = useArrows;
       
        reloadItemSet(items);
    }
    
    public FlatList(MenuEngine parent, String[] items, int textBoxColor, int margin) {
        this(parent, items, textBoxColor, margin, true);
    }
    
    
    private void makeBoxes(int size) {
        int[] clr0 = engine.getRPG().getTextBoxColors(0);
      //  lArrow = new ArrowBox(size, , ArrowBox.LEFT);
        lArrow = new TextBox("<", engine.getRPG().font, clr0[1], clr0[0], true, TextBox.TRANSP_OPAQUE);
        rArrow = new TextBox(">", engine.getRPG().font, clr0[1], clr0[0], true, TextBox.TRANSP_OPAQUE);
      //  rArrow = new ArrowBox(size, engine.getRPG().getTextBoxColors(0), ArrowBox.RIGHT);
    }
    
    
    private String getLongestText(String[] newItems) {
        currLongestText = "X"; //give our box a font-dependant height, at least
        for (int i=0; i<newItems.length; i++) {
            if (newItems[i].length()>currLongestText.length())
                currLongestText = newItems[i];
        }
        return currLongestText;
    }
    
    
    public void reloadItemSet(String[] newItems) {
        currItem = 0;
        //Figure out the longest string in the list
        itemText = new Vector();
        String currLongestText = getLongestText(newItems);
        
        //Create the background box
        TextBox dummy = new TextBox(currLongestText, engine.getRPG().font, 0, 0, true);
        int[] colors = engine.getRPG().getTextBoxColors(textBoxColor);
        if (rArrow==null)
            makeBoxes(dummy.getHeight());
        bkgrdBox = new SolidBox(dummy.getWidth(), dummy.getHeight(), 0xFF000000|colors[0], new int[]{0xFF000000|colors[1], 0xFF000000});
        setSize(bkgrdBox.getWidth(), bkgrdBox.getHeight());
        setPosition(margin, margin);

        //Now, just add the items.
        for (int i=0; i<newItems.length; i++) {
            addItem(newItems[i]);
        }

        //Finally
        setSize(bkgrdBox.getWidth(), bkgrdBox.getHeight());
    }
    
    
    public void addItem(String item) {
        if (item.length()>currLongestText.length())
            throw new RuntimeException("FlatList cannot be dynamically resized to acoomodate \"" + item + "\"");
        itemText.addElement(new TextBox(item, engine.getRPG().font, 0, 0, true, TextBox.TRANSP_CLEAR));
    }

    
    public void setListItemChangedListener(Action listItemChangedListener) {
        this.listItemChangedListener = listItemChangedListener;
    }

    public void setResizeListener(Action resizeListener) {
        this.resizeListener = resizeListener;
    }    

    
    
    public void reset() {
        currItem = 0;
    }

    public MenuItem processInput(int direction) {
        switch (direction) {
            case MenuItem.CONNECT_LEFT:
                currItem--;
                if (currItem<0)
                    currItem = itemText.size()-1;
                if (listItemChangedListener!=null)
                    listItemChangedListener.perform(this);
                    return this;
            case MenuItem.CONNECT_RIGHT:
                currItem++;
                if (currItem==itemText.size())
                    currItem = 0;
                if (listItemChangedListener!=null)
                    listItemChangedListener.perform(this);
                    return this;
            default:
                return super.processInput(direction); //Just change location.
        }
    }

    public void accept() {
        //return this;
    }
    
    public int getNumItems() {
        return itemText.size();
    }
    
    public int getCurrItemID() {
        return currItem;
    }
    
    public void selectElement(int id) {
        if (id<0 || (itemText.size()!=0 && id>=itemText.size()))
            throw new RuntimeException("Cannot set FlatList of size " + itemText.size() + " to element " + id);
        currItem = id;
        if (listItemChangedListener!=null)
            listItemChangedListener.perform(this);
    }

    protected void paint(int[] originOffset) {
        //Paint our two boxes.
        bkgrdBox.paint(originOffset[0]+getPosX(), originOffset[1]+getPosY(), GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
        if (itemText.size()>0) //Sometimes we don't have any text; e.g., a hero with no spell groups at all!
            ((Box)itemText.elementAt(currItem)).paint(originOffset[0]+getPosX()+getWidth()/2, originOffset[1]+getPosY()+1, GraphicsAdapter.TOP|GraphicsAdapter.HCENTER);
        
        //Paint arrows?
        if (useArrows) {
            lArrow.paint(originOffset[0]+getPosX()-lArrow.getWidth(), originOffset[1]+getPosY(), GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
            rArrow.paint(originOffset[0]+getPosX()+getWidth(), originOffset[1]+getPosY(), GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
        }
    }
    
}

