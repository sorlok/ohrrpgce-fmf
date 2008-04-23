package ohrrpgce.menu;

import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.game.LiteException;
import ohrrpgce.henceforth.Int;


/**
 * A Flat List is a list of Strings that one can iterate through by 
 *   pressing left/right (alt. up/down). The current choice appears in the 
 *   visible display area; other options are hidden.
 * @author Seth N. Hetu
 */
public class FlatListSlice extends MenuSlice {
	private static TextSlice arrowL;
	private static TextSlice arrowR;
	
	private String[] items;
	private TextSlice mainTextSlice;
	private ImageAdapter font;
	private boolean useArrows;
	private Int currItem;
	
	//Helps with layout
	private int antiMargin;
	private int maxTextWidth;
	
	private Action listItemChangedListener;
	
	public FlatListSlice(MenuFormatArgs mForm, String[] items, ImageAdapter font, boolean useArrows) {
		super(mForm);
		this.items = items;
		this.font = font;
		this.useArrows = useArrows;
		this.currItem = new Int(0, new Action() {
			public boolean perform(Object caller) {
				if (currItem.getValue()<0 || currItem.getValue()>=FlatListSlice.this.items.length)
					return false;
				mainTextSlice.setText(FlatListSlice.this.items[currItem.getValue()]);
				mainTextSlice.doLayout();
				return true;
			}
		});
		
		//Flat Lists only  make sense if tiny...
		if (mForm.widthHint!=MenuFormatArgs.WIDTH_MINIMUM || mForm.heightHint!=MenuFormatArgs.HEIGHT_MINIMUM)
			throw new LiteException(this, null, "FlatListSlice must have width and height of MINIMUM.");
		
		antiMargin = mForm.borderPadding + mForm.borderColors.length;
		initTextSlices(mForm);
	}
	
	
	private void initTextSlices(MenuFormatArgs copyFormat) {
		//Get the longest string
		String max = "";
		for (int i=0; i<items.length; i++) {
			if (items[i].length() > max.length())
				max = items[i];
		}
		MenuFormatArgs specialFormat = new MenuFormatArgs(copyFormat);
		specialFormat.fillType = MenuSlice.FILL_NONE;
		specialFormat.borderColors = new int[]{};
		specialFormat.borderPadding = 0;
		mainTextSlice = new TextSlice(specialFormat, max, font, true, true, false);
		mainTextSlice.doLayout();
		maxTextWidth = mainTextSlice.getWidth();
		
		if (FlatListSlice.arrowL==null) {
			FlatListSlice.arrowL = new TextSlice(copyFormat, "<", font, true, true, false);
			FlatListSlice.arrowL.doLayout();
			FlatListSlice.arrowR = new TextSlice(copyFormat, ">", font, true, true, false);
			FlatListSlice.arrowR.doLayout();
		}
		
		currItem.setValue(0);
	}

	
	//Untested!
	public void resetItems(String[] newItems) {
		this.items = newItems;
		initTextSlices(this.getInitialFormatArgs());
	}
	
	
	protected void drawPixelBuffer(int atX, int atY) {
		//Draw arrows & current text box
		int currX = getPosX();
		if (useArrows) {
			FlatListSlice.arrowL.paintAt(currX, getPosY());
			currX += FlatListSlice.arrowL.getWidth()-1;
		}
		mainTextSlice.paintAt(currX + antiMargin + maxTextWidth/2-mainTextSlice.getWidth()/2, getPosY()+antiMargin);
		currX += maxTextWidth-1 + antiMargin*2;
		if (useArrows) {
			FlatListSlice.arrowR.paintAt(currX, getPosY());
		}
	}
	
    public void setListItemChangedListener(Action listItemChangedListener) {
        this.listItemChangedListener = listItemChangedListener;
    }
	
    public void selectElement(int id) {
        if (id<0 || (items.length!=0 && id>=items.length))
            throw new LiteException(this, new IllegalArgumentException(), "Cannot set FlatList of size " + items.length + " to element " + id);
        currItem.setValue(id);
        if (listItemChangedListener!=null)
            listItemChangedListener.perform(this);
    }
    
    public int getCurrSelectedID() {
    	return currItem.getValue();
    }
    
    public boolean consumeInput(int direction) {
        switch (direction) {
            case MenuSlice.CONNECT_LEFT:
                currItem.minusOne();
                if (currItem.getValue()<0)
                    currItem.setValue(items.length-1);
                if (listItemChangedListener!=null)
                    listItemChangedListener.perform(this);
                    return true;
            case MenuSlice.CONNECT_RIGHT:
                currItem.plusOne();
                if (currItem.getValue()>=items.length)
                    currItem.setValue(0);
                if (listItemChangedListener!=null)
                    listItemChangedListener.perform(this);
                    return true;
            default:
                return false;
        }
    }
	
	
	
	/**
	 * Avoid errors later
	 */
	public void setTopLeftChild(MenuSlice child) {
		throw new LiteException(this, null, "FlatListSlices cannot have child components.");
	}
	
	protected int calcMinWidth() {
		int minWidth = maxTextWidth;
		if (useArrows)
			minWidth += FlatListSlice.arrowL.getWidth() + FlatListSlice.arrowR.getWidth() - 2;

		return minWidth;
	}
	
	protected int calcMinHeight() {
		return FlatListSlice.arrowL.getHeight() - antiMargin*2;
	}
	

}
