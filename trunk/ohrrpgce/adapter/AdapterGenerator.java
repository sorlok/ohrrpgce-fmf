package ohrrpgce.adapter;

import java.io.IOException;
import java.io.InputStream;

public interface AdapterGenerator {
	public abstract ImageAdapter createImageAdapter(InputStream in) throws IOException;
	public abstract ImageAdapter createImageAdapter(String path) throws IOException;
	public abstract ImageAdapter createBlankImage(int width, int height);
	
	public abstract FontAdapter createErrorTitleFont();
	public abstract FontAdapter createErrorMessageFont();
	public abstract FontAdapter createGameTitleFont();
	public abstract FontAdapter createProgressFont();
	
	public abstract InputStream getLump(String lumpName);
	public abstract void closeStream(InputStream fis);
	
	public abstract int getScreenWidth();
	public abstract int getScreenHeight();
	
	public abstract void setGameName(String name);
	public abstract String getGameName();
	
	public abstract void exitGame(boolean unconditional);
	
	//public abstract FileAdapter createFileAdapter();
	//public abstract InputAdapter createInputAdapter();
}
