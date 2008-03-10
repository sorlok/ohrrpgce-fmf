package ohrrpgce.tool;


/**
 * In an effort to preserve most of the original code of the HQ2X algorithm (presumably because the 
 * 		optimizations made in the c++ code will carry over to some extent when hotspot compiles it)
 *      minimal changes were made. The result, specifically the switch statement used for each of the 
 *      256 possible combinations, was huge. So this class encapsulates most of the code needed
 *      to turn a point and its window into four interpolated points.
 * @author Seth N. Hetu
 * @author Maxim Stepin ( maxst@hiend3d.com )
 */
 final class Doubler {

	 //Useful constants
	 private static final int yMask = 0xFF0000;
	 private static final int uMask = 0x00FF00;
	 private static final int vMask = 0x0000FF;
	 private static final int threshY = 0x300000;
	 private static final int threshU = 0x000700;
	 private static final int threshV = 0x000006;
	 
	//Instance variables - should make this thread safe
	private int[] output = new int[4];
	private int[] window;
	
	
	//Utility functions
	public static boolean isDist(int rgb1, int rgb2)
	{
		  int yuv1 = rgb2yuv(rgb1);
		  int yuv2 = rgb2yuv(rgb2);
		  return ( ( Math.abs((yuv1 & yMask) - (yuv2 & yMask)) > threshY ) ||
		           ( Math.abs((yuv1 & uMask) - (yuv2 & uMask)) > threshU ) ||
		           ( Math.abs((yuv1 & vMask) - (yuv2 & vMask)) > threshV ) );
	}
	public static int rgb2yuv(int rgb) {
		int r = (rgb&0xFF0000)/0x10000;
		int g = (rgb&0xFF00)/0x100;
		int b = (rgb&0xFF);
			
		int y = Math.min(Math.abs(r * 2104 + g * 4130 + b * 802 + 4096 + 131072) >> 13, 235);
		int u = Math.min(Math.abs(r * -1214 + g * -2384 + b * 3598 + 4096 + 1048576) >> 13, 240);
		int v = Math.min(Math.abs(r * 3598 + g * -3013 + b * -585 + 4096 + 1048576) >> 13, 240);
			
		return y*0x10000 + u*0x100 + v;
	}
	
	public static int yuv2rgb(int yuv) { 
		//If dead-code elimination's disabled, then we're screwed. 
		int y = (yuv & 0xFF0000) / 0x10000;
		int u = (yuv & 0xFF00) / 0x100;
		int v = (yuv & 0xFF);

		int c = y - 16;
		int d = u - 128;
		int e = v - 128;

		int r = clip((298 * c + 409 * e + 128) >> 8);
		int g = clip((298 * c - 100 * d - 208 * e + 128) >> 8);
		int b = clip((298 * c + 516 * d + 128) >> 8);
			
		return r*0x10000 + g*0x100 + b;
	}	
	private static int clip(int rgb) {
		if (rgb<0)
			return 0;
		else if (rgb>255)
			return 255;
		return rgb;
	}
	
	


	private int Interp1(int c1, int c2)
	{
	  return (c1*3+c2) >> 2;
	}

	private int Interp2(int c1, int c2, int c3)
	{
	  return (c1*2+c2+c3) >> 2;
	}

	/*private int Interp5(int c1, int c2)
	{
	  return (c1+c2) >> 1;
	}*/

	private int Interp6(int c1, int c2, int c3)
	{
	  //return (c1*5+c2*2+c3)/8;

	  return ((((c1 & 0x00FF00)*5 + (c2 & 0x00FF00)*2 + (c3 & 0x00FF00) ) & 0x0007F800) +
	                 (((c1 & 0xFF00FF)*5 + (c2 & 0xFF00FF)*2 + (c3 & 0xFF00FF) ) & 0x07F807F8)) >> 3;
	}

	private int Interp7(int c1, int c2, int c3)
	{
	  //return (c1*6+c2+c3)/8;

	  return ((((c1 & 0x00FF00)*6 + (c2 & 0x00FF00) + (c3 & 0x00FF00) ) & 0x0007F800) +
	                 (((c1 & 0xFF00FF)*6 + (c2 & 0xFF00FF) + (c3 & 0xFF00FF) ) & 0x07F807F8)) >> 3;
	}

	private int Interp9(int c1, int c2, int c3)
	{
	  //return (c1*2+(c2+c3)*3)/8;

	  return ((((c1 & 0x00FF00)*2 + ((c2 & 0x00FF00) + (c3 & 0x00FF00))*3 ) & 0x0007F800) +
	                 (((c1 & 0xFF00FF)*2 + ((c2 & 0xFF00FF) + (c3 & 0xFF00FF))*3 ) & 0x07F807F8)) >> 3;
	}

	private int Interp10(int c1, int c2, int c3)
	{
	  //return (c1*14+c2+c3)/16;

	  return ((((c1 & 0x00FF00)*14 + (c2 & 0x00FF00) + (c3 & 0x00FF00) ) & 0x000FF000) +
	                 (((c1 & 0xFF00FF)*14 + (c2 & 0xFF00FF) + (c3 & 0xFF00FF) ) & 0x0FF00FF0)) >> 4;
	}



	private void PIXEL00_0(){
	output[0] = window[5];
	}

	private void PIXEL00_10(){
	output[0] = Interp1(window[5], window[1]);
	}

	private void PIXEL00_11(){
	output[0] = Interp1(window[5], window[4]);
	}

	private void PIXEL00_12(){
	output[0] = Interp1(window[5], window[2]);
	}

	private void PIXEL00_20(){
	output[0] = Interp2(window[5], window[4], window[2]);
	}

	private void PIXEL00_21(){
	output[0] = Interp2(window[5], window[1], window[2]);
	}

	private void PIXEL00_22(){
	output[0] = Interp2(window[5], window[1], window[4]);
	}

	private void PIXEL00_60(){
	output[0] = Interp6(window[5], window[2], window[4]);
	}

	private void PIXEL00_61(){
	output[0] = Interp6(window[5], window[4], window[2]);
	}

	private void PIXEL00_70(){
	output[0] = Interp7(window[5], window[4], window[2]);
	}

	private void PIXEL00_90(){
	output[0] = Interp9(window[5], window[4], window[2]);
	}

	private void PIXEL00_100(){
	output[0] = Interp10(window[5], window[4], window[2]);
	}

	private void PIXEL01_0(){
	output[1] = window[5];
	}

	private void PIXEL01_10(){
	output[1] = Interp1(window[5], window[3]);
	}

	private void PIXEL01_11(){
	output[1] = Interp1(window[5], window[2]);
	}

	private void PIXEL01_12(){
	output[1] = Interp1(window[5], window[6]);
	}

	private void PIXEL01_20(){
	output[1] = Interp2(window[5], window[2], window[6]);
	}

	private void PIXEL01_21(){
	output[1] = Interp2(window[5], window[3], window[6]);
	}

	private void PIXEL01_22(){
	output[1] = Interp2(window[5], window[3], window[2]);
	}

	private void PIXEL01_60(){
	output[1] = Interp6(window[5], window[6], window[2]);
	}

	private void PIXEL01_61(){
	output[1] = Interp6(window[5], window[2], window[6]);
	}

	private void PIXEL01_70(){
	output[1] = Interp7(window[5], window[2], window[6]);
	}

	private void PIXEL01_90(){
	output[1] = Interp9(window[5], window[2], window[6]);
	}

	private void PIXEL01_100(){
	output[1] = Interp10(window[5], window[2], window[6]);
	}

	private void PIXEL10_0(){
	output[2] = window[5];
	}

	private void PIXEL10_10(){
	output[2] = Interp1(window[5], window[7]);
	}

	private void PIXEL10_11(){
	output[2] = Interp1(window[5], window[8]);
	}

	private void PIXEL10_12(){
	output[2] = Interp1(window[5], window[4]);
	}

	private void PIXEL10_20(){
	output[2] = Interp2(window[5], window[8], window[4]);
	}

	private void PIXEL10_21(){
	output[2] = Interp2(window[5], window[7], window[4]);
	}

	private void PIXEL10_22(){
	output[2] = Interp2(window[5], window[7], window[8]);
	}

	private void PIXEL10_60(){
	output[2] = Interp6(window[5], window[4], window[8]);
	}

	private void PIXEL10_61(){
	output[2] = Interp6(window[5], window[8], window[4]);
	}

	private void PIXEL10_70(){
	output[2] = Interp7(window[5], window[8], window[4]);
	}

	private void PIXEL10_90(){
	output[2] = Interp9(window[5], window[8], window[4]);
	}

	private void PIXEL10_100(){
	output[2] = Interp10(window[5], window[8], window[4]);
	}

	private void PIXEL11_0(){
	output[3] = window[5];
	}

	private void PIXEL11_10(){
	output[3] = Interp1(window[5], window[9]);
	}

	private void PIXEL11_11(){
	output[3] = Interp1(window[5], window[6]);
	}

	private void PIXEL11_12(){
	output[3] = Interp1(window[5], window[8]);
	}

	private void PIXEL11_20(){
	output[3] = Interp2(window[5], window[6], window[8]);
	}

	private void PIXEL11_21(){
	output[3] = Interp2(window[5], window[9], window[8]);
	}

	private void PIXEL11_22(){
	output[3] = Interp2(window[5], window[9], window[6]);
	}

	private void PIXEL11_60(){
	output[3] = Interp6(window[5], window[8], window[6]);
	}

	private void PIXEL11_61(){
	output[3] = Interp6(window[5], window[6], window[8]);
	}

	private void PIXEL11_70(){
	output[3] = Interp7(window[5], window[6], window[8]);
	}

	private void PIXEL11_90(){
	output[3] = Interp9(window[5], window[6], window[8]);
	}

	private void PIXEL11_100(){
	output[3] = Interp10(window[5], window[6], window[8]);
	}






	public int[] expand(int pattern, int[] window) {
		this.window = window;

	      switch (pattern)
	      {
	        case 0:
	        case 1:
	        case 4:
	        case 32:
	        case 128:
	        case 5:
	        case 132:
	        case 160:
	        case 33:
	        case 129:
	        case 36:
	        case 133:
	        case 164:
	        case 161:
	        case 37:
	        case 165:
	        {
	          PIXEL00_20();
	          PIXEL01_20();
	          PIXEL10_20();
	          PIXEL11_20();
	          break;
	        }
	        case 2:
	        case 34:
	        case 130:
	        case 162:
	        {
	          PIXEL00_22();
	          PIXEL01_21();
	          PIXEL10_20();
	          PIXEL11_20();
	          break;
	        }
	        case 16:
	        case 17:
	        case 48:
	        case 49:
	        {
	          PIXEL00_20();
	          PIXEL01_22();
	          PIXEL10_20();
	          PIXEL11_21();
	          break;
	        }
	        case 64:
	        case 65:
	        case 68:
	        case 69:
	        {
	          PIXEL00_20();
	          PIXEL01_20();
	          PIXEL10_21();
	          PIXEL11_22();
	          break;
	        }
	        case 8:
	        case 12:
	        case 136:
	        case 140:
	        {
	          PIXEL00_21();
	          PIXEL01_20();
	          PIXEL10_22();
	          PIXEL11_20();
	          break;
	        }
	        case 3:
	        case 35:
	        case 131:
	        case 163:
	        {
	          PIXEL00_11();
	          PIXEL01_21();
	          PIXEL10_20();
	          PIXEL11_20();
	          break;
	        }
	        case 6:
	        case 38:
	        case 134:
	        case 166:
	        {
	          PIXEL00_22();
	          PIXEL01_12();
	          PIXEL10_20();
	          PIXEL11_20();
	          break;
	        }
	        case 20:
	        case 21:
	        case 52:
	        case 53:
	        {
	          PIXEL00_20();
	          PIXEL01_11();
	          PIXEL10_20();
	          PIXEL11_21();
	          break;
	        }
	        case 144:
	        case 145:
	        case 176:
	        case 177:
	        {
	          PIXEL00_20();
	          PIXEL01_22();
	          PIXEL10_20();
	          PIXEL11_12();
	          break;
	        }
	        case 192:
	        case 193:
	        case 196:
	        case 197:
	        {
	          PIXEL00_20();
	          PIXEL01_20();
	          PIXEL10_21();
	          PIXEL11_11();
	          break;
	        }
	        case 96:
	        case 97:
	        case 100:
	        case 101:
	        {
	          PIXEL00_20();
	          PIXEL01_20();
	          PIXEL10_12();
	          PIXEL11_22();
	          break;
	        }
	        case 40:
	        case 44:
	        case 168:
	        case 172:
	        {
	          PIXEL00_21();
	          PIXEL01_20();
	          PIXEL10_11();
	          PIXEL11_20();
	          break;
	        }
	        case 9:
	        case 13:
	        case 137:
	        case 141:
	        {
	          PIXEL00_12();
	          PIXEL01_20();
	          PIXEL10_22();
	          PIXEL11_20();
	          break;
	        }
	        case 18:
	        case 50:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_20();
	          PIXEL11_21();
	          break;
	        }
	        case 80:
	        case 81:
	        {
	          PIXEL00_20();
	          PIXEL01_22();
	          PIXEL10_21();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 72:
	        case 76:
	        {
	          PIXEL00_21();
	          PIXEL01_20();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_22();
	          break;
	        }
	        case 10:
	        case 138:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_21();
	          PIXEL10_22();
	          PIXEL11_20();
	          break;
	        }
	        case 66:
	        {
	          PIXEL00_22();
	          PIXEL01_21();
	          PIXEL10_21();
	          PIXEL11_22();
	          break;
	        }
	        case 24:
	        {
	          PIXEL00_21();
	          PIXEL01_22();
	          PIXEL10_22();
	          PIXEL11_21();
	          break;
	        }
	        case 7:
	        case 39:
	        case 135:
	        {
	          PIXEL00_11();
	          PIXEL01_12();
	          PIXEL10_20();
	          PIXEL11_20();
	          break;
	        }
	        case 148:
	        case 149:
	        case 180:
	        {
	          PIXEL00_20();
	          PIXEL01_11();
	          PIXEL10_20();
	          PIXEL11_12();
	          break;
	        }
	        case 224:
	        case 228:
	        case 225:
	        {
	          PIXEL00_20();
	          PIXEL01_20();
	          PIXEL10_12();
	          PIXEL11_11();
	          break;
	        }
	        case 41:
	        case 169:
	        case 45:
	        {
	          PIXEL00_12();
	          PIXEL01_20();
	          PIXEL10_11();
	          PIXEL11_20();
	          break;
	        }
	        case 22:
	        case 54:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_20();
	          PIXEL11_21();
	          break;
	        }
	        case 208:
	        case 209:
	        {
	          PIXEL00_20();
	          PIXEL01_22();
	          PIXEL10_21();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 104:
	        case 108:
	        {
	          PIXEL00_21();
	          PIXEL01_20();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_22();
	          break;
	        }
	        case 11:
	        case 139:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_21();
	          PIXEL10_22();
	          PIXEL11_20();
	          break;
	        }
	        case 19:
	        case 51:
	        {
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL00_11();
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL00_60();
	            PIXEL01_90();
	          }
	          PIXEL10_20();
	          PIXEL11_21();
	          break;
	        }
	        case 146:
	        case 178:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	            PIXEL11_12();
	          }
	          else
	          {
	            PIXEL01_90();
	            PIXEL11_61();
	          }
	          PIXEL10_20();
	          break;
	        }
	        case 84:
	        case 85:
	        {
	          PIXEL00_20();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL01_11();
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL01_60();
	            PIXEL11_90();
	          }
	          PIXEL10_21();
	          break;
	        }
	        case 112:
	        case 113:
	        {
	          PIXEL00_20();
	          PIXEL01_22();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL10_12();
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL10_61();
	            PIXEL11_90();
	          }
	          break;
	        }
	        case 200:
	        case 204:
	        {
	          PIXEL00_21();
	          PIXEL01_20();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	            PIXEL11_11();
	          }
	          else
	          {
	            PIXEL10_90();
	            PIXEL11_60();
	          }
	          break;
	        }
	        case 73:
	        case 77:
	        {
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL00_12();
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL00_61();
	            PIXEL10_90();
	          }
	          PIXEL01_20();
	          PIXEL11_22();
	          break;
	        }
	        case 42:
	        case 170:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	            PIXEL10_11();
	          }
	          else
	          {
	            PIXEL00_90();
	            PIXEL10_60();
	          }
	          PIXEL01_21();
	          PIXEL11_20();
	          break;
	        }
	        case 14:
	        case 142:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	            PIXEL01_12();
	          }
	          else
	          {
	            PIXEL00_90();
	            PIXEL01_61();
	          }
	          PIXEL10_22();
	          PIXEL11_20();
	          break;
	        }
	        case 67:
	        {
	          PIXEL00_11();
	          PIXEL01_21();
	          PIXEL10_21();
	          PIXEL11_22();
	          break;
	        }
	        case 70:
	        {
	          PIXEL00_22();
	          PIXEL01_12();
	          PIXEL10_21();
	          PIXEL11_22();
	          break;
	        }
	        case 28:
	        {
	          PIXEL00_21();
	          PIXEL01_11();
	          PIXEL10_22();
	          PIXEL11_21();
	          break;
	        }
	        case 152:
	        {
	          PIXEL00_21();
	          PIXEL01_22();
	          PIXEL10_22();
	          PIXEL11_12();
	          break;
	        }
	        case 194:
	        {
	          PIXEL00_22();
	          PIXEL01_21();
	          PIXEL10_21();
	          PIXEL11_11();
	          break;
	        }
	        case 98:
	        {
	          PIXEL00_22();
	          PIXEL01_21();
	          PIXEL10_12();
	          PIXEL11_22();
	          break;
	        }
	        case 56:
	        {
	          PIXEL00_21();
	          PIXEL01_22();
	          PIXEL10_11();
	          PIXEL11_21();
	          break;
	        }
	        case 25:
	        {
	          PIXEL00_12();
	          PIXEL01_22();
	          PIXEL10_22();
	          PIXEL11_21();
	          break;
	        }
	        case 26:
	        case 31:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_22();
	          PIXEL11_21();
	          break;
	        }
	        case 82:
	        case 214:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_21();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 88:
	        case 248:
	        {
	          PIXEL00_21();
	          PIXEL01_22();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 74:
	        case 107:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_21();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_22();
	          break;
	        }
	        case 27:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_10();
	          PIXEL10_22();
	          PIXEL11_21();
	          break;
	        }
	        case 86:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_21();
	          PIXEL11_10();
	          break;
	        }
	        case 216:
	        {
	          PIXEL00_21();
	          PIXEL01_22();
	          PIXEL10_10();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 106:
	        {
	          PIXEL00_10();
	          PIXEL01_21();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_22();
	          break;
	        }
	        case 30:
	        {
	          PIXEL00_10();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_22();
	          PIXEL11_21();
	          break;
	        }
	        case 210:
	        {
	          PIXEL00_22();
	          PIXEL01_10();
	          PIXEL10_21();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 120:
	        {
	          PIXEL00_21();
	          PIXEL01_22();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_10();
	          break;
	        }
	        case 75:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_21();
	          PIXEL10_10();
	          PIXEL11_22();
	          break;
	        }
	        case 29:
	        {
	          PIXEL00_12();
	          PIXEL01_11();
	          PIXEL10_22();
	          PIXEL11_21();
	          break;
	        }
	        case 198:
	        {
	          PIXEL00_22();
	          PIXEL01_12();
	          PIXEL10_21();
	          PIXEL11_11();
	          break;
	        }
	        case 184:
	        {
	          PIXEL00_21();
	          PIXEL01_22();
	          PIXEL10_11();
	          PIXEL11_12();
	          break;
	        }
	        case 99:
	        {
	          PIXEL00_11();
	          PIXEL01_21();
	          PIXEL10_12();
	          PIXEL11_22();
	          break;
	        }
	        case 57:
	        {
	          PIXEL00_12();
	          PIXEL01_22();
	          PIXEL10_11();
	          PIXEL11_21();
	          break;
	        }
	        case 71:
	        {
	          PIXEL00_11();
	          PIXEL01_12();
	          PIXEL10_21();
	          PIXEL11_22();
	          break;
	        }
	        case 156:
	        {
	          PIXEL00_21();
	          PIXEL01_11();
	          PIXEL10_22();
	          PIXEL11_12();
	          break;
	        }
	        case 226:
	        {
	          PIXEL00_22();
	          PIXEL01_21();
	          PIXEL10_12();
	          PIXEL11_11();
	          break;
	        }
	        case 60:
	        {
	          PIXEL00_21();
	          PIXEL01_11();
	          PIXEL10_11();
	          PIXEL11_21();
	          break;
	        }
	        case 195:
	        {
	          PIXEL00_11();
	          PIXEL01_21();
	          PIXEL10_21();
	          PIXEL11_11();
	          break;
	        }
	        case 102:
	        {
	          PIXEL00_22();
	          PIXEL01_12();
	          PIXEL10_12();
	          PIXEL11_22();
	          break;
	        }
	        case 153:
	        {
	          PIXEL00_12();
	          PIXEL01_22();
	          PIXEL10_22();
	          PIXEL11_12();
	          break;
	        }
	        case 58:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_11();
	          PIXEL11_21();
	          break;
	        }
	        case 83:
	        {
	          PIXEL00_11();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_21();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 92:
	        {
	          PIXEL00_21();
	          PIXEL01_11();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 202:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          PIXEL01_21();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          PIXEL11_11();
	          break;
	        }
	        case 78:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          PIXEL01_12();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          PIXEL11_22();
	          break;
	        }
	        case 154:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_22();
	          PIXEL11_12();
	          break;
	        }
	        case 114:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_12();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 89:
	        {
	          PIXEL00_12();
	          PIXEL01_22();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 90:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 55:
	        case 23:
	        {
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL00_11();
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL00_60();
	            PIXEL01_90();
	          }
	          PIXEL10_20();
	          PIXEL11_21();
	          break;
	        }
	        case 182:
	        case 150:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	            PIXEL11_12();
	          }
	          else
	          {
	            PIXEL01_90();
	            PIXEL11_61();
	          }
	          PIXEL10_20();
	          break;
	        }
	        case 213:
	        case 212:
	        {
	          PIXEL00_20();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL01_11();
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL01_60();
	            PIXEL11_90();
	          }
	          PIXEL10_21();
	          break;
	        }
	        case 241:
	        case 240:
	        {
	          PIXEL00_20();
	          PIXEL01_22();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL10_12();
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL10_61();
	            PIXEL11_90();
	          }
	          break;
	        }
	        case 236:
	        case 232:
	        {
	          PIXEL00_21();
	          PIXEL01_20();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	            PIXEL11_11();
	          }
	          else
	          {
	            PIXEL10_90();
	            PIXEL11_60();
	          }
	          break;
	        }
	        case 109:
	        case 105:
	        {
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL00_12();
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL00_61();
	            PIXEL10_90();
	          }
	          PIXEL01_20();
	          PIXEL11_22();
	          break;
	        }
	        case 171:
	        case 43:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	            PIXEL10_11();
	          }
	          else
	          {
	            PIXEL00_90();
	            PIXEL10_60();
	          }
	          PIXEL01_21();
	          PIXEL11_20();
	          break;
	        }
	        case 143:
	        case 15:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	            PIXEL01_12();
	          }
	          else
	          {
	            PIXEL00_90();
	            PIXEL01_61();
	          }
	          PIXEL10_22();
	          PIXEL11_20();
	          break;
	        }
	        case 124:
	        {
	          PIXEL00_21();
	          PIXEL01_11();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_10();
	          break;
	        }
	        case 203:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_21();
	          PIXEL10_10();
	          PIXEL11_11();
	          break;
	        }
	        case 62:
	        {
	          PIXEL00_10();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_11();
	          PIXEL11_21();
	          break;
	        }
	        case 211:
	        {
	          PIXEL00_11();
	          PIXEL01_10();
	          PIXEL10_21();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 118:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_12();
	          PIXEL11_10();
	          break;
	        }
	        case 217:
	        {
	          PIXEL00_12();
	          PIXEL01_22();
	          PIXEL10_10();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 110:
	        {
	          PIXEL00_10();
	          PIXEL01_12();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_22();
	          break;
	        }
	        case 155:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_10();
	          PIXEL10_22();
	          PIXEL11_12();
	          break;
	        }
	        case 188:
	        {
	          PIXEL00_21();
	          PIXEL01_11();
	          PIXEL10_11();
	          PIXEL11_12();
	          break;
	        }
	        case 185:
	        {
	          PIXEL00_12();
	          PIXEL01_22();
	          PIXEL10_11();
	          PIXEL11_12();
	          break;
	        }
	        case 61:
	        {
	          PIXEL00_12();
	          PIXEL01_11();
	          PIXEL10_11();
	          PIXEL11_21();
	          break;
	        }
	        case 157:
	        {
	          PIXEL00_12();
	          PIXEL01_11();
	          PIXEL10_22();
	          PIXEL11_12();
	          break;
	        }
	        case 103:
	        {
	          PIXEL00_11();
	          PIXEL01_12();
	          PIXEL10_12();
	          PIXEL11_22();
	          break;
	        }
	        case 227:
	        {
	          PIXEL00_11();
	          PIXEL01_21();
	          PIXEL10_12();
	          PIXEL11_11();
	          break;
	        }
	        case 230:
	        {
	          PIXEL00_22();
	          PIXEL01_12();
	          PIXEL10_12();
	          PIXEL11_11();
	          break;
	        }
	        case 199:
	        {
	          PIXEL00_11();
	          PIXEL01_12();
	          PIXEL10_21();
	          PIXEL11_11();
	          break;
	        }
	        case 220:
	        {
	          PIXEL00_21();
	          PIXEL01_11();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 158:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_22();
	          PIXEL11_12();
	          break;
	        }
	        case 234:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          PIXEL01_21();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_11();
	          break;
	        }
	        case 242:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_12();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 59:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_11();
	          PIXEL11_21();
	          break;
	        }
	        case 121:
	        {
	          PIXEL00_12();
	          PIXEL01_22();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 87:
	        {
	          PIXEL00_11();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_21();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 79:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_12();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          PIXEL11_22();
	          break;
	        }
	        case 122:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 94:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 218:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 91:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 229:
	        {
	          PIXEL00_20();
	          PIXEL01_20();
	          PIXEL10_12();
	          PIXEL11_11();
	          break;
	        }
	        case 167:
	        {
	          PIXEL00_11();
	          PIXEL01_12();
	          PIXEL10_20();
	          PIXEL11_20();
	          break;
	        }
	        case 173:
	        {
	          PIXEL00_12();
	          PIXEL01_20();
	          PIXEL10_11();
	          PIXEL11_20();
	          break;
	        }
	        case 181:
	        {
	          PIXEL00_20();
	          PIXEL01_11();
	          PIXEL10_20();
	          PIXEL11_12();
	          break;
	        }
	        case 186:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_11();
	          PIXEL11_12();
	          break;
	        }
	        case 115:
	        {
	          PIXEL00_11();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_12();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 93:
	        {
	          PIXEL00_12();
	          PIXEL01_11();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 206:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          PIXEL01_12();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          PIXEL11_11();
	          break;
	        }
	        case 205:
	        case 201:
	        {
	          PIXEL00_12();
	          PIXEL01_20();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_10();
	          }
	          else
	          {
	            PIXEL10_70();
	          }
	          PIXEL11_11();
	          break;
	        }
	        case 174:
	        case 46:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_10();
	          }
	          else
	          {
	            PIXEL00_70();
	          }
	          PIXEL01_12();
	          PIXEL10_11();
	          PIXEL11_20();
	          break;
	        }
	        case 179:
	        case 147:
	        {
	          PIXEL00_11();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_10();
	          }
	          else
	          {
	            PIXEL01_70();
	          }
	          PIXEL10_20();
	          PIXEL11_12();
	          break;
	        }
	        case 117:
	        case 116:
	        {
	          PIXEL00_20();
	          PIXEL01_11();
	          PIXEL10_12();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_10();
	          }
	          else
	          {
	            PIXEL11_70();
	          }
	          break;
	        }
	        case 189:
	        {
	          PIXEL00_12();
	          PIXEL01_11();
	          PIXEL10_11();
	          PIXEL11_12();
	          break;
	        }
	        case 231:
	        {
	          PIXEL00_11();
	          PIXEL01_12();
	          PIXEL10_12();
	          PIXEL11_11();
	          break;
	        }
	        case 126:
	        {
	          PIXEL00_10();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_10();
	          break;
	        }
	        case 219:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_10();
	          PIXEL10_10();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 125:
	        {
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL00_12();
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL00_61();
	            PIXEL10_90();
	          }
	          PIXEL01_11();
	          PIXEL11_10();
	          break;
	        }
	        case 221:
	        {
	          PIXEL00_12();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL01_11();
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL01_60();
	            PIXEL11_90();
	          }
	          PIXEL10_10();
	          break;
	        }
	        case 207:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	            PIXEL01_12();
	          }
	          else
	          {
	            PIXEL00_90();
	            PIXEL01_61();
	          }
	          PIXEL10_10();
	          PIXEL11_11();
	          break;
	        }
	        case 238:
	        {
	          PIXEL00_10();
	          PIXEL01_12();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	            PIXEL11_11();
	          }
	          else
	          {
	            PIXEL10_90();
	            PIXEL11_60();
	          }
	          break;
	        }
	        case 190:
	        {
	          PIXEL00_10();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	            PIXEL11_12();
	          }
	          else
	          {
	            PIXEL01_90();
	            PIXEL11_61();
	          }
	          PIXEL10_11();
	          break;
	        }
	        case 187:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	            PIXEL10_11();
	          }
	          else
	          {
	            PIXEL00_90();
	            PIXEL10_60();
	          }
	          PIXEL01_10();
	          PIXEL11_12();
	          break;
	        }
	        case 243:
	        {
	          PIXEL00_11();
	          PIXEL01_10();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL10_12();
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL10_61();
	            PIXEL11_90();
	          }
	          break;
	        }
	        case 119:
	        {
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL00_11();
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL00_60();
	            PIXEL01_90();
	          }
	          PIXEL10_12();
	          PIXEL11_10();
	          break;
	        }
	        case 237:
	        case 233:
	        {
	          PIXEL00_12();
	          PIXEL01_20();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_100();
	          }
	          PIXEL11_11();
	          break;
	        }
	        case 175:
	        case 47:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_100();
	          }
	          PIXEL01_12();
	          PIXEL10_11();
	          PIXEL11_20();
	          break;
	        }
	        case 183:
	        case 151:
	        {
	          PIXEL00_11();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_100();
	          }
	          PIXEL10_20();
	          PIXEL11_12();
	          break;
	        }
	        case 245:
	        case 244:
	        {
	          PIXEL00_20();
	          PIXEL01_11();
	          PIXEL10_12();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_100();
	          }
	          break;
	        }
	        case 250:
	        {
	          PIXEL00_10();
	          PIXEL01_10();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 123:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_10();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_10();
	          break;
	        }
	        case 95:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_10();
	          PIXEL11_10();
	          break;
	        }
	        case 222:
	        {
	          PIXEL00_10();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_10();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 252:
	        {
	          PIXEL00_21();
	          PIXEL01_11();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_100();
	          }
	          break;
	        }
	        case 249:
	        {
	          PIXEL00_12();
	          PIXEL01_22();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_100();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 235:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_21();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_100();
	          }
	          PIXEL11_11();
	          break;
	        }
	        case 111:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_100();
	          }
	          PIXEL01_12();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_22();
	          break;
	        }
	        case 63:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_100();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_11();
	          PIXEL11_21();
	          break;
	        }
	        case 159:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_100();
	          }
	          PIXEL10_22();
	          PIXEL11_12();
	          break;
	        }
	        case 215:
	        {
	          PIXEL00_11();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_100();
	          }
	          PIXEL10_21();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 246:
	        {
	          PIXEL00_22();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          PIXEL10_12();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_100();
	          }
	          break;
	        }
	        case 254:
	        {
	          PIXEL00_10();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_100();
	          }
	          break;
	        }
	        case 253:
	        {
	          PIXEL00_12();
	          PIXEL01_11();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_100();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_100();
	          }
	          break;
	        }
	        case 251:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          PIXEL01_10();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_100();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 239:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_100();
	          }
	          PIXEL01_12();
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_100();
	          }
	          PIXEL11_11();
	          break;
	        }
	        case 127:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_100();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_20();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_20();
	          }
	          PIXEL11_10();
	          break;
	        }
	        case 191:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_100();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_100();
	          }
	          PIXEL10_11();
	          PIXEL11_12();
	          break;
	        }
	        case 223:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_20();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_100();
	          }
	          PIXEL10_10();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_20();
	          }
	          break;
	        }
	        case 247:
	        {
	          PIXEL00_11();
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_100();
	          }
	          PIXEL10_12();
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_100();
	          }
	          break;
	        }
	        case 255:
	        {
	          if (isDist(window[4], window[2]))
	          {
	            PIXEL00_0();
	          }
	          else
	          {
	            PIXEL00_100();
	          }
	          if (isDist(window[2], window[6]))
	          {
	            PIXEL01_0();
	          }
	          else
	          {
	            PIXEL01_100();
	          }
	          if (isDist(window[8], window[4]))
	          {
	            PIXEL10_0();
	          }
	          else
	          {
	            PIXEL10_100();
	          }
	          if (isDist(window[6], window[8]))
	          {
	            PIXEL11_0();
	          }
	          else
	          {
	            PIXEL11_100();
	          }
	          break;
	        }
	        
	  }
	      return output;
	}
 }
