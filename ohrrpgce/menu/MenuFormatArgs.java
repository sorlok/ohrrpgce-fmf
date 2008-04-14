package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;

public class MenuFormatArgs {
	public static final int WIDTH_MINIMUM = -1;
	public static final int WIDTH_MAXIMUM = -2;
	public static final int HEIGHT_MINIMUM = -1;
	public static final int HEIGHT_MAXIMUM = -2;
	//public static final int LAYOUT_DEFAULT = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
	
	//Layout
	public int xHint;
	public int yHint;
	public int widthHint;
	public int heightHint;
	public int fromAnchor;
	public int toAnchor;
	public int borderPadding; //Only matters for MINIMUM sized components
	
	//Style
	public int bgColor;
    public int[] borderColors;
    public int fillType;
    
    
    //Default Constructor
    public MenuFormatArgs() {
    	fromAnchor = GraphicsAdapter.TOP | GraphicsAdapter.LEFT;
    	toAnchor = GraphicsAdapter.TOP | GraphicsAdapter.LEFT;
    }
    
    //Copy Constructor
    public MenuFormatArgs(MenuFormatArgs copyFrom) {
    	this.xHint = copyFrom.xHint;
    	this.yHint = copyFrom.yHint;
    	this.widthHint = copyFrom.widthHint;
    	this.heightHint = copyFrom.heightHint;
    	this.fromAnchor = copyFrom.fromAnchor;
    	this.toAnchor = copyFrom.toAnchor;
    	this.bgColor = copyFrom.bgColor;
    	this.borderPadding = copyFrom.borderPadding;
    	this.borderColors = new int[copyFrom.borderColors.length];
    	System.arraycopy(copyFrom.borderColors, 0, this.borderColors, 0, copyFrom.borderColors.length);
    	this.fillType = copyFrom.fillType;
    }

}
