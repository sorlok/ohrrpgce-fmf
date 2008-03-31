package ohrrpgce.runtime;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;

public class MetaMenu {
	
    private static final int DEFAULT_INTER_ELEMENT_SPACING = 3;
    private static final int DEFAULT_BORDER_PADDING = 2;
	
	public static MenuSlice buildMenu(int width, int height) {
		//First test: some simple boxes
		MenuFormatArgs mFormat = new MenuFormatArgs();
		mFormat.bgColor = 0xCC2200;
		mFormat.borderColors = new int[]{0xFF0000, 0};
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.xHint = 10;
		mFormat.yHint = 10;
		mFormat.widthHint = 50;
		mFormat.heightHint = 50;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		
		MenuSlice firstBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0x22CC00;
		mFormat.yHint = 0;
		MenuSlice secondBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0x2200CC;
		mFormat.xHint = 0;
		mFormat.yHint = 10;
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice thirdBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0xFFFFFF;
		mFormat.fillType = MenuSlice.FILL_NONE;
		mFormat.xHint = 10;
		mFormat.yHint = 0;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice fourthBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0x66CCCC22;
		mFormat.fillType = MenuSlice.FILL_TRANSLUCENT;
		mFormat.xHint = 0;
		mFormat.yHint = 10;
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice fifthBox = new MenuSlice(mFormat);
		
		firstBox.connect(secondBox, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		firstBox.connect(thirdBox, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		thirdBox.connect(fourthBox, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		fourthBox.connect(fifthBox, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		
		return firstBox;
	}

}
