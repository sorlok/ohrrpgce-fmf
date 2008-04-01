package ohrrpgce.runtime;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.data.RPG;
import ohrrpgce.menu.MenuFormatArgs;
import ohrrpgce.menu.MenuSlice;

public class MetaMenu {
	
    private static final int DEFAULT_INTER_ELEMENT_SPACING = 3;
    private static final int DEFAULT_BORDER_PADDING = 2;
	
	public static MenuSlice buildSimpleMenu(int width, int height) {
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
	
	
	public static MenuSlice buildHierarchicalMenu(int width, int height) {
		//Now, test interior boxes
		MenuFormatArgs mFormat = new MenuFormatArgs();
		mFormat.bgColor = 0x770077;
		mFormat.borderColors = new int[]{0xFF00FF, 0};
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.xHint = 0;
		mFormat.yHint = 0;
		mFormat.widthHint = width;
		mFormat.heightHint = height;
		mFormat.fromAnchor = GraphicsAdapter.TOP|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice topBox = new MenuSlice(mFormat);
		
		//First inner box
		mFormat.bgColor = 0x008800;
		mFormat.borderColors[0] = 0x00DD00;
		mFormat.xHint = 10;
		mFormat.yHint = 10;
		mFormat.widthHint = 42;
		mFormat.heightHint = 42;
		MenuSlice firstInner = new MenuSlice(mFormat);
		
		topBox.setTopLeftChild(firstInner);
		
		//Test center layout and also overlap...
		mFormat.bgColor = 0x000088;
		mFormat.borderColors[0] = 0x0000DD;
		mFormat.xHint = 0;
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.HCENTER;
		MenuSlice secondInner = new MenuSlice(mFormat);
		
		firstInner.connect(secondInner, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		
		//Test more layout
		mFormat.bgColor = 0x880000;
		mFormat.borderColors[0] = 0xDD0000;
		mFormat.xHint = 10;
		mFormat.yHint = 0;
		mFormat.fromAnchor = GraphicsAdapter.VCENTER|GraphicsAdapter.RIGHT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice thirdInner = new MenuSlice(mFormat);
		
		secondInner.connect(thirdInner, MenuSlice.CONNECT_RIGHT, MenuSlice.CFLAG_PAINT);
		
		return topBox;
	}
	
	
	
	public static MenuSlice buildMenu(int width, int height, RPG rpg) {
		//Get colors. We "lighten" a color to provide our basic menu...
        int[] colorZero = rpg.getTextBoxColors(0);
        int[] colorZeroLight = new int[]{
            Math.min((((colorZero[0]&0xFF0000)/0x10000)*14)/10, 0xFF)*0x10000+
                    Math.min((((colorZero[0]&0xFF00)/0x100)*14)/10, 0xFF)*0x100+
                    Math.min(((colorZero[0]&0xFF)*14)/10, 0xFF)+0xFF000000,
            0xFF888888
        };
		
		
		//Requires a "clear" top-level box with no border, etc. I don't like it so much, but
        //   it is the "proper" way to do it.
		MenuFormatArgs mFormat = new MenuFormatArgs();
		mFormat.borderColors = new int[]{};
		mFormat.fillType = MenuSlice.FILL_NONE;
		mFormat.xHint = 0;
		mFormat.yHint = 0;
		mFormat.widthHint = width;
		mFormat.heightHint = height;
		MenuSlice clearBox = new MenuSlice(mFormat);
		
		//Top-half
		mFormat.bgColor = colorZeroLight[0];
		mFormat.borderColors = new int[]{colorZeroLight[1], 0};
		mFormat.fillType = MenuSlice.FILL_SOLID;
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MINIMUM;
		MenuSlice topHalfBox = new MenuSlice(mFormat);
		clearBox.setTopLeftChild(topHalfBox);
		
		//Bottom-half
		mFormat.xHint = -1;
		mFormat.heightHint = MenuFormatArgs.HEIGHT_MAXIMUM;
		mFormat.fromAnchor = GraphicsAdapter.BOTTOM|GraphicsAdapter.LEFT;
		mFormat.toAnchor = GraphicsAdapter.TOP|GraphicsAdapter.LEFT;
		MenuSlice bottomHalfBox = new MenuSlice(mFormat);
		topHalfBox.connect(bottomHalfBox, MenuSlice.CONNECT_BOTTOM, MenuSlice.CFLAG_PAINT);
		
		//Add list of buttons...
		
		return clearBox; 
	}
	

}
