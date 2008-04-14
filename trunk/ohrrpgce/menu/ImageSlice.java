package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.RPG;

public class ImageSlice extends MenuSlice {

	//Alternate implementation: use pngs
	private ImageAdapter pngImage;
	private int[] pngRectangle = new int[]{0,0}; //We can probably over-ride pixel buffer's rectangle in the future.
	
	
	/*
	 * Constructor
	 * @param mFormat Standard format args. Width & height set to "MINIMUM" is the default
	 * @param rpg Current RPG
	 * @param imgData image pixels; indices into:
	 * @param palette palette for our image data
	 * @param bounds [x,y,width,height] of our SOURCE image
	 */
	/*public ImageSlice(MenuFormatArgs mFormat, RPG rpg, int[] imgData, int palette, int[] bounds) {
		super(mFormat);
	}*/
		//This isn't right.... I need to re-think images, not just copy code. 
	/*	if (this.mFormat.widthHint == MenuFormatArgs.WIDTH_MINIMUM)
			this.mFormat.widthHint = bounds[WIDTH];
		if (this.mFormat.heightHint == MenuFormatArgs.HEIGHT_MINIMUM)
			this.mFormat.heightHint = bounds[HEIGHT];
		
		overlayImage(imgData, palette, rpg, bounds);*/
	
	
	
    /*private void overlayImage(int[] imgData, int palette, RPG rpg, int[] dest) {
        //System.out.println("dest: " + dest[0] + ","  + dest[1] + ","  + dest[2] + "," + dest[3]);
        int startX = Math.max(0, dest[X])-dest[X];
        int startY = Math.max(0, dest[Y])-dest[Y];
        int endX = Math.min(getWidth(), dest[0]+dest[2])-dest[0];
        int endY = Math.min(getHeight(), dest[1]+dest[3])-dest[1];
        //System.out.println("From: " + startX + "," + startY + " to " + endX + "," + endY);
        for (int y=startY; y<endY; y++) {
            for (int x=startX; x<endX; x++) {
                int currColor = rpg.getIndexedColor(palette, imgData[y*dest[2]+x]);
                if ((currColor&0xFF000000)!=0)
                	setPixel(x+dest[0], y+dest[1], currColor);
            }
        }
    }*/
	
	public ImageSlice(MenuFormatArgs mFormat, ImageAdapter pngImage) {
		super(mFormat);
		
		this.pngImage = pngImage;
	}
    
    protected void drawPixelBuffer(int atX, int atY) {
    	if (pngImage==null)
    		super.drawPixelBuffer(atX, atY);
    	else
    		GraphicsAdapter.drawImage(pngImage, getPosX()+pngRectangle[X], getPosY()+pngRectangle[Y]);
    }

    
    public void setImage(ImageAdapter pngImage) {
    	this.pngImage = pngImage;
    }
    
    
    //Last save
    protected int calcMinWidth() {
    	if (pngImage!=null) {
    		return pngImage.getWidth();
    	} else {
    		return -1;
    	}
    }
    
    protected int calcMinHeight() {
    	if (pngImage!=null) {
    		return pngImage.getHeight();
    	} else {
    		return -1;
    	}
    }
    
    
    
    //Re-center our image
    protected void setWidth(int newWidth) {
    	super.setWidth(newWidth);
    	
    	if (pngImage!=null) {
    		pngRectangle[X] = getWidth()/2 - pngImage.getWidth()/2;
    	} else {
    		
    	}
    }
    
    protected void setHeight(int newHeight) {
    	super.setHeight(newHeight);
    	
    	if (pngImage!=null) {
    		pngRectangle[Y] = getHeight()/2 - pngImage.getHeight()/2;
    	} else {
    		
    	}
    }
    

}
