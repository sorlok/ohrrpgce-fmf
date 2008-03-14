/*
 * ImageBox.java
 * Created on April 17, 2007, 12:19 AM
 */

package ohrrpgce.data;

import ohrrpgce.menu.Canvas;
import ohrrpgce.tool.HQ2X;

/**
 * A box which displays an image inside it.
 * @author Seth N. Hetu
 */
public class ImageBox extends Canvas {
    
    public static final int SCALE_NN = 0;
    public static final int SCALE_HQ2X = 1;
    
    
    /**
     * Init an image box of a specified width/height, presumably to be built with overlayImage()
     */
    public ImageBox(int width, int height, int bgColor, int[] borders, int fillType) {
        super(width, height, bgColor, borders, fillType);
    }
    
    
    /**
     * dest[] = [x, y, width, height], width and height MUST be the image's native width/height
     */
    public void overlayImage(int[] imgData, int palette, RPG rpg, int[] dest) {
        //System.out.println("dest: " + dest[0] + ","  + dest[1] + ","  + dest[2] + "," + dest[3]);
        int startX = Math.max(0, dest[0])-dest[0];
        int startY = Math.max(0, dest[1])-dest[1];
        int endX = Math.min(getWidth(), dest[0]+dest[2])-dest[0];
        int endY = Math.min(getHeight(), dest[1]+dest[3])-dest[1];
        //System.out.println("From: " + startX + "," + startY + " to " + endX + "," + endY);
        for (int y=startY; y<endY; y++) {
            for (int x=startX; x<endX; x++) {
                int currColor = rpg.getIndexedColor(palette, imgData[y*dest[2]+x]);
                if ((currColor&0xFF000000)!=0)
                	setPixel(getWidth()*(y+dest[1])+x+dest[0], currColor);
            }
        }
    }
    
    
    /**
     * Create a Box with an image inside of it.
     * @param imageData The raw image data.
     * @param palette The palette to use
     * @param rpg The rpg to pull palette info (and the image, if necessary) from.
     * @param dimensions The base width and height of the image, not including scale.
     * @param scale How much to (linearly) scale the image by.
     * @param bgColor,borderColors See documentation for Box
     */
    public ImageBox(int[] imageData, int palette, RPG rpg, int[] dimensions, int scale, int bgColor, int[] borderColors, int scaleAlgorithm, int fillType) {
        super(dimensions[0]*scale+borderColors.length*2, dimensions[1]*scale+borderColors.length*2, bgColor, borderColors, fillType);
        if (scaleAlgorithm==SCALE_HQ2X && scale==2) {
            //Prepare
            int[] oldData = new int[dimensions[0]*dimensions[1]];
            for (int h=0; h<dimensions[1]; h++) {
                for (int w=0; w<dimensions[0]; w++) {
                    int currColor = rpg.getIndexedColor(palette, imageData[h*dimensions[0]+w]);
                    if ((currColor&0xFF000000)==0) //Transparent color.
                        oldData[h*dimensions[0]+w] = bgColor;
                    else
                        oldData[h*dimensions[0]+w] = currColor;
                }
            }
            
            //Convert & Save
            oldData = HQ2X.hq2x(oldData, dimensions[0]);
            for (int h=0; h<dimensions[1]*2; h++) {
                for (int w=0; w<dimensions[0]*2; w++) {
                    int newY = borderColors.length + h;
                    int newX = borderColors.length + w;
                    setPixel(newY*getWidth()+newX, oldData[h*dimensions[0]*2 + w]);
                }
            }
        } else {
            for (int h=0; h<dimensions[1]; h++) {
                for (int w=0; w<dimensions[0]; w++) {
                    int currColor = rpg.getIndexedColor(palette, imageData[h*dimensions[0]+w]);
                    if ((currColor&0xFF000000)==0) //Transparent color.
                        continue;
                    for (int yP=0; yP<scale; yP++) {
                        for (int xP=0; xP<scale; xP++) {
                            int newY = borderColors.length + h*scale + yP;
                            int newX = borderColors.length + w*scale + xP;
                            setPixel(newY*getWidth()+newX, currColor);
                        }
                    }
                }
            }
        }
    }
    
    
    
    
}
