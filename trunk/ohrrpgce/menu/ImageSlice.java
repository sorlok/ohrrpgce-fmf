package ohrrpgce.menu;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.RPG;
import ohrrpgce.game.LiteException;

public class ImageSlice extends MenuSlice {

	//Alternate implementation: use pngs
	private ImageAdapter pngImage;
	private int[] pngRectangle = new int[]{0,0}; //We can probably over-ride pixel buffer's rectangle in the future.
	
	//Could also use int[] arrays
	private int[] rawImage;
	private int[] rawImageRect = new int[]{0,0};
		
	public ImageSlice(MenuFormatArgs mFormat, ImageAdapter pngImage) {
		super(mFormat);
		
		this.pngImage = pngImage;
	}
	
	public ImageSlice(MenuFormatArgs mFormat, int[] rawImage, int scanlength) {
		super(mFormat);
		
		this.rawImage = rawImage;
		rawImageRect[X] = scanlength;
		rawImageRect[Y] = rawImage.length/scanlength;
	}

	
    
    protected void drawPixelBuffer(int atX, int atY) {
    	if (pngImage!=null)
    		GraphicsAdapter.drawImage(pngImage, getPosX()+pngRectangle[X], getPosY()+pngRectangle[Y]);
    	else if (rawImage!=null)
    		GraphicsAdapter.drawRGB(rawImage, 0, rawImageRect[X], getPosX()+pngRectangle[X], getPosY()+pngRectangle[Y], rawImageRect[X], rawImageRect[Y], true);
    	else
    		super.drawPixelBuffer(atX, atY);
    		
    }

    
    public void setImage(ImageAdapter pngImage) {
    	this.rawImage = null;
    	this.pngImage = pngImage;
    }
    
    public void setImage(int[] rawData) {
    	this.pngImage = null;
    	this.rawImage = rawData;
    }
    
    
    //Last save
    protected int calcMinWidth() {
    	if (pngImage!=null) {
    		return pngImage.getWidth();
    	} else if (rawImage!=null){
    		return rawImageRect[X];
    	} else {
    		return -1;
    	}
    }
    
    protected int calcMinHeight() {
    	if (pngImage!=null) {
    		return pngImage.getHeight();
    	} else if (rawImage!=null){
    		return rawImageRect[Y];
    	} else {
    		return -1;
    	}
    }
    
    
    
    //Re-center our image
    protected void setWidth(int newWidth) {
    	super.setWidth(newWidth);
    	
    	if (pngImage!=null) {
    		pngRectangle[X] = getWidth()/2 - pngImage.getWidth()/2;
    	} else if (rawImage!=null){
    		pngRectangle[X] = getWidth()/2 - rawImageRect[X]/2;
    	}
    }
    
    protected void setHeight(int newHeight) {
    	super.setHeight(newHeight);
    	
    	if (pngImage!=null) {
    		pngRectangle[Y] = getHeight()/2 - pngImage.getHeight()/2;
    	} else if (rawImage!=null){
    		pngRectangle[Y] = getHeight()/2 - rawImageRect[Y]/2;
    	}
    }
    

}
