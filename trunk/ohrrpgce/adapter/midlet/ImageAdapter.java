package ohrrpgce.adapter.midlet;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Image;
import ohrrpgce.game.LiteException;

public class ImageAdapter implements ohrrpgce.adapter.ImageAdapter {

        private Image img;
    
	public ImageAdapter(InputStream in) throws IOException {
            img = Image.createImage(in);
	}
	
	public ImageAdapter(String path) throws IOException {
            try {
                    img = Image.createImage(ImageAdapter.class.getResourceAsStream(path));
            } catch (IllegalArgumentException ex) {
                    throw new LiteException(this, ex, "Null("+path+")");
            }
	}
	
	public ImageAdapter(int width, int height) {
            img = Image.createImage(width, height);
	}
	
	public int getHeight() {
            return img.getHeight();
	}

	public Object getInternalImage() {
            return img;
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
            img.getRGB(rgbData, offset, scanlength, x, y, width, height);
	}

	public int getWidth() {
            return img.getWidth();
	}

}
