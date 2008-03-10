package ohrrpgce.tool;



/*import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;*/

public class HQ2X {


	private static final int[] LUT16to32 = new int[65536];   
	static {
		for (int i=0; i<LUT16to32.length; i++)
			LUT16to32[i] = ((i & 0xF800) << 8) + ((i & 0x07E0) << 5) + ((i & 0x001F) << 3);
	}
	
	
	private static int doubleConvert(int pix32) {
		int pix16 = 0;
		// Convert 32-bit to 16-bit
		int r = ((pix32 & 0xFF0000) / 0x10000) >> 3;
		int g = ((pix32 & 0xFF00) / 0x100) >> 2;
		int b = (pix32 & 0xFF) >> 3;
		pix16 = (r << 11) + (g << 5) + b;

		// Convert back
		return LUT16to32[pix16];
	}


	
	public static int getWindow(int i, int j, int[] window, int[] copy, int[] rgb, int scanlength) {
		//First things first
		int xMinus1 = 1;
		int xPlus1 = 1;
		int yMinus1 = 1;
		int yPlus1 = 1;
		if (j==0)
			yMinus1 = 0;
		else if (j==rgb.length/scanlength-1)
			yPlus1 = 0;
		if (i==0)
			xMinus1 = 0;
		else if (i==scanlength-1)
			xPlus1 = 0;
		
		//Center column
		window[5] = rgb[j*scanlength+i];
	    window[2] = rgb[(j-yMinus1)*scanlength+i];
	    window[8] = rgb[(j+yPlus1)*scanlength+i];
		
		//Left column
		window[1] = rgb[(j-yMinus1)*scanlength+i-xMinus1];
		window[4] = rgb[(j)*scanlength+i-xMinus1];
		window[7] = rgb[(j+yPlus1)*scanlength+i-xMinus1];
		
		//Right column
		window[3] = rgb[(j-yMinus1)*scanlength+i+xPlus1];
		window[6] = rgb[(j)*scanlength+i+xPlus1];
		window[9] = rgb[(j+yPlus1)*scanlength+i+xPlus1];

		//Post-processing; remove the alpha; copy the array
		for (int q=1; q<window.length; q++) { 
			window[q] = window[q]&0xFFFFFF;
			copy[q] =  window[q];
		}
		
		//Compute the pattern flag
		int pattern = 0;
	    int flag = 1;
	    //System.out.print(":");
	    for (int k=1; k<window.length; k++) {
	        if (k==5) 
	        	continue;
	        if ( window[k] != window[5] ) {
	          if ( Doubler.isDist(window[k], window[5]))
	            pattern |= flag;
	          /*else
		        	System.out.print("\n"+flag);*/
	        } 
	        flag <<= 1;
	    }
		
	    return pattern;
	}
	
	
	public static int[] hq2x(int[] rgb, int scanlength) {
		//DEBUG
		/*int w = scanlength;
		int h = rgb.length/scanlength;
		System.out.print("{");
		for (int y=0; y<h; y++) {
			for (int x=0; x<w; x++) {
				String pix = Integer.toHexString(rgb[y*w+x]);
				System.out.print("0x");
				for (int i=0; i<8-pix.length(); i++)
					System.out.print("0");
				System.out.print(pix + ", ");
			}
			System.out.println();
		}
		System.out.print("}");*/
		
                //Scale back the green factor
                for (int i=0; i<rgb.length; i++) {
                    rgb[i] = doubleConvert(rgb[i]);
                }
            
		int[] res = new int[rgb.length*4];
		//int resAlpha = 0; //Not the best solution
		
		//Indexing starts at 1, to maintain consistency with the author's original
		//    design:
		//   +----+----+----+
		//   | w1 | w2 | w3 |
		//   +----+----+----+
		//   | w4 | w5 | w6 |
		//   +----+----+----+
		//   | w7 | w8 | w9 |
		//   +----+----+----+

		int[] window = new int[10];
		int[] copy = new int[10];
		Doubler d = new Doubler();
		
		//Move the window over every pixel
		for (int j=0; j<rgb.length/scanlength; j++) {
			//System.out.println();
			for (int i=0; i<scanlength; i++) {
				//resAlpha = (rgb[j*scanlength+i]&0xFF000000);
				int pattern = getWindow(i, j, window, copy, rgb, scanlength);
		
				int[] out = d.expand(pattern, copy);
				int tlY = j*2;
				int tlX = i*2;
				int sl = scanlength*2;
				res[tlY*sl+tlX] = out[0]; //| resAlpha;
				res[tlY*sl+tlX+1] = out[1]; //| resAlpha;
				res[(tlY+1)*sl+tlX] = out[2]; //| resAlpha;
				res[(tlY+1)*sl+tlX+1] = out[3]; //| resAlpha;
			}
		}
		
		return res;
	}
	
	
	
	
	
	


}
