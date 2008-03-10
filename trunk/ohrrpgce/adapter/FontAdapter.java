package ohrrpgce.adapter;

public interface FontAdapter {
	//public abstract void createFont(Object item); 
	public abstract Object getFontData();
	public abstract int getFontHeight();
	public abstract int stringWidth(String str);
}
