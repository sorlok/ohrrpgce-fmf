package ohrrpgce.tool;

/**
 * Low-quality nearest-neighbor
 * @author Seth N. Hetu
 */
public class LQ2X {
	
	public static int[] lq2x(int[] rgb, int scanlength) {
		int[] res = new int[rgb.length*4];
		int scale = 2;
		
        for (int h=0; h<rgb.length/scanlength; h++) {
            for (int w=0; w<scanlength; w++) {
                int currColor = (rgb[h*scanlength+w]&0xFFFFFF); //Remove alpha
                if (currColor==0) //Transparent color.
                    continue;
                for (int yP=0; yP<scale; yP++) {
                    for (int xP=0; xP<scale; xP++) {
                        int newY = h*scale + yP;
                        int newX = w*scale + xP;
                        res[newY*scanlength*2+newX] = currColor;
                    }
                }
            }
        }
        
        return res;
	}

}
