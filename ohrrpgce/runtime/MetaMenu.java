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
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		
		MenuSlice firstBox = new MenuSlice(mFormat);
		
		mFormat.bgColor = 0x22CC00;
		MenuSlice secondBox = new MenuSlice(mFormat);
		
		firstBox.connect(secondBox, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		return firstBox;
	}

}
