package ohrrpgce.adapter;

/**
 * These classes exist to abstract all "non-compliant"
 *   operations. Basically, the J2ME libraries cannot always
 *   simply be linked to a J2EE project, so these classes present
 *   a facade to input, etc. 
 * @author Seth N. Hetu
 */
public interface ImageAdapter {
	
	//public abstract void createInternalImage(InputStream in) throws IOException;
	
	public abstract void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height);
	public abstract Object getInternalImage();
	public abstract int getWidth();
	public abstract int getHeight();
}
