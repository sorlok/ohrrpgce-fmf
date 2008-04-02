package ohrrpgce.adapter.applet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import ohrrpgce.game.LiteException;


public class ImageAdapter implements ohrrpgce.adapter.ImageAdapter {

	//public static String prefix = "";
	
	private BufferedImage img;

	public ImageAdapter(InputStream in) throws IOException {
		img = ImageIO.read(in);
	}
	
	public ImageAdapter(String path) throws IOException {
		try {
			img = ImageIO.read(ImageAdapter.class.getResourceAsStream(path));
		} catch (IllegalArgumentException ex) {
			throw new LiteException(this, ex, "Null("+path+")");
		}
	}
	
	public ImageAdapter(int width, int height) {
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	
	public Object getInternalImage() {
		return img;
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
		img.getRGB(x, y, width, height, rgbData, offset, scanlength);
	}
	
	public int getHeight() {
		return img.getHeight();
	}
	
	public int getWidth() {
		return img.getWidth();
	}
}
