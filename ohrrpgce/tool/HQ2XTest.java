/*
 * HQ2XTest.java
 *
 * Created on October 23, 2007, 9:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ohrrpgce.tool;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 *
 * @author sethhetu
 */
public class HQ2XTest {
    
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: HQ2X <infile> <outfile>");
			return;
		}
	
		try {
			BufferedImage i = ImageIO.read(new File(args[0]));
			int[] rgb = new int[i.getWidth()*i.getHeight()];
			for (int pix=0; pix<i.getWidth()*i.getHeight(); pix++) {
				rgb[pix] = i.getRGB(pix%i.getWidth(), pix/i.getWidth());
				if (pix%i.getWidth()==0)
					System.out.println();
				System.out.print("0x"+Integer.toHexString(rgb[pix]).toUpperCase()+", ");
			}
			
			int[] res = HQ2X.hq2x(rgb, i.getWidth());
			BufferedImage out = new BufferedImage(i.getWidth()*2, i.getHeight()*2, BufferedImage.TYPE_INT_RGB);
			for (int y=0; y<out.getHeight(); y++) {
				for (int x=0; x<out.getWidth(); x++) {
					out.setRGB(x, y, res[y*out.getWidth()+x]);
				}
			}
			ImageIO.write(out, "png", new File(args[1]));
			
			System.out.println("Done");
			
		} catch (Exception ex)  {
			System.out.println("Error: " + ex.toString());
		}
		
	}
    
}
