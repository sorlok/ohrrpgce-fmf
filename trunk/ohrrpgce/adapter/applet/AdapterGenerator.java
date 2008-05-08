package ohrrpgce.adapter.applet;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import ohrrpgce.game.LiteException;
import ohrrpgce.menu.Action;
import ohrrpgce.runtime.Meta;


public class AdapterGenerator implements ohrrpgce.adapter.AdapterGenerator {

//	private FileAdapter fileAdapt;
//	private InputAdapter inputAdapt;
	//private GraphicsAdapter_applet grahicsAdapt;
	
	private FontAdapter errorMsgFont;
	private FontAdapter errorTitleFont;
	private FontAdapter gameTitleFont;
	private FontAdapter progressFont;
	
	private static final int SIZE_SMALL = 10;
	private static final int SIZE_MEDIUM = 14;
	
	private String currGame;

	private int[] screenSize;
	
	private Action closeAction;
	
	public AdapterGenerator(/*GraphicsAdapter_applet gContext, */int[] screenSize, Action closeAction) {
		//this.grahicsAdapt = gContext;
		this.screenSize = screenSize;
		this.closeAction = closeAction;
	}
	 
	public ohrrpgce.adapter.ImageAdapter createImageAdapter(InputStream in) throws IOException {
		return new ImageAdapter(in);
	}

	//Messy singletons....
	/*public FileAdapter createFileAdapter() {
		if (fileAdapt == null)
			fileAdapt = new FileAdapter();
		
		return fileAdapt;
	}
	
	public InputAdapter createInputAdapter() {
		if (inputAdapt == null)
			inputAdapt = new InputAdapter();
		
		return inputAdapt;
	}*/
	
	public ohrrpgce.adapter.ImageAdapter createImageAdapter(String path) throws IOException {
		return new ImageAdapter(path);
	}

	public ohrrpgce.adapter.ImageAdapter createBlankImage(int width, int height) {
		return new ImageAdapter(width, height);
	}
	
	public ohrrpgce.adapter.FontAdapter createErrorMessageFont() {
		if (errorMsgFont==null) {
			errorMsgFont = new FontAdapter(new Font("SansSerif", Font.PLAIN, SIZE_SMALL));
		}
		return errorMsgFont;
	}
	
	public ohrrpgce.adapter.FontAdapter createErrorTitleFont() {
		if (errorTitleFont==null) {
			errorTitleFont = new FontAdapter(new Font("SansSerif", Font.BOLD, SIZE_MEDIUM));
		}
		return errorTitleFont;
	}

	public ohrrpgce.adapter.FontAdapter createGameTitleFont() {
		if (gameTitleFont==null) {
			gameTitleFont = new FontAdapter(new Font("SansSerif", Font.PLAIN, SIZE_MEDIUM));
		}
		return gameTitleFont;
	}

	public ohrrpgce.adapter.FontAdapter createProgressFont() {
		if (progressFont==null) {
			progressFont = new FontAdapter(new Font("SansSerif", Font.BOLD, SIZE_MEDIUM));
		}
		return progressFont;
	}
	
	public void closeStream(InputStream fis) {
		try {
			fis.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public InputStream getLump(String lumpName) {
		//Should allow one to load either from the file system,
		// in the form of RPG lumps that are translated on the fly,
		// or from the internals of this jar.
		String internalPath = Meta.pathToGameFolder+currGame+"/"+lumpName;
		//System.out.println("Lump: " + ImageAdapter.prefix + internalPath);
		InputStream res = AdapterGenerator.class.getResourceAsStream(internalPath);
		if (res==null)
			throw new RuntimeException("File not found: " +  internalPath);
		
		return res;
		/*try {
			//System.out.println("Opening..." + ImageAdapter.prefix + internalPath);
			return getClass().getResourceAsStream(internalPath);
			//return new FileInputStream(ImageAdapter.prefix + internalPath);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException("File not found: " +  internalPath);
		}*/
	}
	
	public int getScreenWidth() {
		return screenSize[0];
	}
	
	public int getScreenHeight() {
		return screenSize[1];
	}
	
	public void setGameName(String name) {
		this.currGame = name;
	}
	
	public String getGameName() {
		return currGame;
	}
	
	public void exitGame(boolean unconditional) {
		closeAction.perform(new Boolean(unconditional));
	}
	
}
