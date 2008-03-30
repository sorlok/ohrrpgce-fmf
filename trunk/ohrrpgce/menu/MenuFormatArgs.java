package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;

public class MenuFormatArgs {
	public static final int WIDTH_MINIMUM = -1;
	public static final int WIDTH_MAXIMUM = -2;
	public static final int LAYOUT_DEFAULT = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
	
	//Layout
	public int x;
	public int y;
	public int width;
	public int height;
	public int layoutRule;
	
	//Style
	public int bgColor;
    public int[] borderColors;
    public int fillType;
    
    
    //Default Constructor
    public MenuFormatArgs() {
    	this.layoutRule = LAYOUT_DEFAULT;
    }
    
    //Copy Constructor
    public MenuFormatArgs(MenuFormatArgs copyFrom) {
    	this.x = copyFrom.x;
    	this.y = copyFrom.y;
    	this.width = copyFrom.width;
    	this.height = copyFrom.height;
    	this.layoutRule = copyFrom.layoutRule;
    	this.bgColor = copyFrom.bgColor;
    	this.borderColors = new int[copyFrom.borderColors.length];
    	System.arraycopy(copyFrom.borderColors, 0, this.borderColors, 0, copyFrom.borderColors.length);
    	this.fillType = copyFrom.fillType;
    }

}
