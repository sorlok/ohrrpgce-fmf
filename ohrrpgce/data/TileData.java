/*
 * TileData_Core.java
 * Created on February 2, 2007, 8:34 PM
 */

package ohrrpgce.data;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.loader.TilesetParser;

/**
 * Enclosing "Union" class for drawing tiles as Images or pixels
 *    NOTE: This "core" exists for compilation without javax.microedition.lcdui.Image/Graphics
 * @author Seth N. Hetu
 */
public class TileData {
	private ImageAdapter pic;
    
    /**
     * 
     * @param data Assumed to be of type int[][]
     */
   /* public TileData_Core(Object data) {
        this.pixels = (int[][])data;
    }
    
    public int colorAt(int tileID, int pixID) {
    	return pixels[tileID][pixID];
    }*/
    
    
    
    public TileData(ImageAdapter data) {
        this.pic = data;
    }
    
    public void draw(int tileID, int destX, int destY, int fullWidth, int fullHeight) {
    	GraphicsAdapter.setClip(destX, destY, TilesetParser.TILE_SIZE, TilesetParser.TILE_SIZE);
    	GraphicsAdapter.drawImage(pic, -(tileID%TilesetParser.TILE_COLS)*TilesetParser.TILE_SIZE+destX, -(tileID/TilesetParser.TILE_COLS)*TilesetParser.TILE_SIZE+destY, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
    	GraphicsAdapter.setClip(0, 0, fullWidth, fullHeight);
    }
    
    public int colorAt(int tileID, int pixID) {
    	throw new RuntimeException("Can't call colorAt() for Image-based TileData's");
    }
}
