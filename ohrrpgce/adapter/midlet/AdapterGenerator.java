package ohrrpgce.adapter.midlet;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.Font;

import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.runtime.Meta;


public class AdapterGenerator implements ohrrpgce.adapter.AdapterGenerator {

        private String currGame;
        private int[] screenSize;
        
        public AdapterGenerator(int[] screenSize) {
            this.screenSize = screenSize;
        }
    
	public ImageAdapter createBlankImage(int width, int height) {
		return new ohrrpgce.adapter.midlet.ImageAdapter(width, height);
	}

	public ImageAdapter createImageAdapter(InputStream in) throws IOException {
		return new ohrrpgce.adapter.midlet.ImageAdapter(in);
	}
        

	public ImageAdapter createImageAdapter(String path) throws IOException {
            return new ohrrpgce.adapter.midlet.ImageAdapter(path);
	}

        public void closeStream(InputStream fis) {
            try {
                fis.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex.toString());
            }
        }

        public ohrrpgce.adapter.FontAdapter createErrorMessageFont() {
            return errorMsgFnt;
        }

        public ohrrpgce.adapter.FontAdapter createErrorTitleFont() {
            return errorTitleFnt;
        }

        public ohrrpgce.adapter.FontAdapter createGameTitleFont() {
            return gameTitleFnt;
        }

        public ohrrpgce.adapter.FontAdapter createProgressFont() {
            return progressFnt;
        }

	public void setGameName(String name) {
		this.currGame = name;
	}
	
	public String getGameName() {
		return currGame;
	}

        public InputStream getLump(String lumpName) {
		String internalPath = Meta.pathToGameFolder+currGame+"/"+lumpName;
		InputStream res = AdapterGenerator.class.getResourceAsStream(internalPath);
		if (res==null)
                    throw new RuntimeException("File not found: " +  internalPath);
		
		return res;
        }

        public int getScreenWidth() {
            return screenSize[0];
        }
        
        public int getScreenHeight() {
            return screenSize[1];
        }
        


	
	//Canonical Fonts
        private static final FontAdapter errorTitleFnt = new FontAdapter(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        public static final FontAdapter errorMsgFnt = new FontAdapter(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        private static final FontAdapter gameTitleFnt = new FontAdapter(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        private static final FontAdapter progressFnt = new FontAdapter(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM));

}
