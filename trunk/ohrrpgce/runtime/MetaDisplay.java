/*
 * MetaDisplay.java
 * Created on January 9, 2007, 10:50 AM
 */

package ohrrpgce.runtime;

import java.io.IOException;
import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.BattlePrompt;
import ohrrpgce.data.Message;
import ohrrpgce.data.TileData;
import ohrrpgce.data.loader.LinkedList;
import ohrrpgce.data.loader.PictureParser;
import ohrrpgce.data.loader.TilesetParser;
import ohrrpgce.game.GameEngine;
import ohrrpgce.game.LiteException;

/**
 * Covnenience class for drawing the opening screens
 * @author Seth N. Hetu
 */
public abstract class MetaDisplay {
    //For debugging
    //public static final boolean DEBUG_CONSOLE = true;
    //public static String DEBUG_MSG = "";
    public static boolean DEBUG_OUTLINE_DOORS = true;
    public static boolean DEBUG_OUTLINE_SPRITES = true;
    
    //General header info
    private static final int margin = 10;
    private static final int maxIconSize = 48;
    
    //Header layout info
    private static ImageAdapter headerImg;
    private static int bodyStart;
    private static int percBarStart;
    
    //Cached
    private static int[] scans;
    private static int[] lastScans;
    
    //Error info
    private static String currError="";
    private static String[] errorStrings={};
    
    private static AdapterGenerator adaptGen;
    
    public static void init(AdapterGenerator aGen) {
    	adaptGen = aGen;
    	GameEngine.initFonts(aGen);
    }
    
    private static void loadHeader() {
        try {
            headerImg = adaptGen.createImageAdapter(Meta.pathToGameFolder + "ohrrpgcefmflogo.png");
        } catch (IOException ex) {
            System.out.println("Couldn't load header image");
            headerImg = adaptGen.createBlankImage(10, 10);
        }
        bodyStart = margin*2 + headerImg.getHeight();
    }
    
    public static void clearCanvas(int width, int height) {
    	GraphicsAdapter.setColor(0xFFFFFF);
    	GraphicsAdapter.fillRect(0, 0, width, height);
    }
    
    private static void drawSelectionBox(int x, int y, int w, int h, int hexColor) {
        int prevClr = GraphicsAdapter.getColor();
        if (lastScans==null || lastScans[0]!=w || lastScans[1] !=h || lastScans[2]!=hexColor) {
            scans = new int[w*h];
            for (int pix=0; pix<w*h; pix++)
                scans[pix] = 0x66000000|hexColor;
            lastScans = new int[] {w, h, hexColor};
            System.out.println("Cached rectangle");
        }
        GraphicsAdapter.drawRGB(scans, 0, w, x, y, w, h, true);
        GraphicsAdapter.setColor(hexColor);
        GraphicsAdapter.drawRect(x+1, y+1, w-2, h-2);
        GraphicsAdapter.setColor(prevClr);
    }
    
    public static void drawHeader(int width) {
        if (headerImg==null)
            loadHeader();
        
        //Draw it
        GraphicsAdapter.drawImage(headerImg, width/2, margin, GraphicsAdapter.HCENTER|GraphicsAdapter.TOP);
    }
    
    public static void drawGameList(MetaGame[] games, int currGame, int width, int height) {
        if (headerImg==null)
            loadHeader();
        
        //Draw: Games
        int sY = bodyStart;
        GraphicsAdapter.setFont(GameEngine.errorMsgFnt);
        GraphicsAdapter.setColor(0);
        for (int i=0; i<games.length; i++) {
        	GraphicsAdapter.drawString(games[i].name, margin*2, sY, GraphicsAdapter.LEFT|GraphicsAdapter.TOP);
        	GraphicsAdapter.drawRect(margin*2, sY, width-margin*4, GameEngine.errorMsgFnt.getFontHeight());
            sY += GameEngine.errorMsgFnt.getFontHeight();
        }
        
        //Draw: Currently selected game
        if (games.length>0) {
            sY = bodyStart + currGame*GameEngine.errorMsgFnt.getFontHeight();
            drawSelectionBox(margin*2, sY, width-margin*4, GameEngine.errorMsgFnt.getFontHeight(), 0xFF0000);
        }
    }
    
    public static void drawGameInfo(MetaGame currGame, int width, int height) {
          if (headerImg==null)
            loadHeader();
          
          //Draw: Icon & Title Box
          GraphicsAdapter.setFont(GameEngine.gameTitleFnt);
          GraphicsAdapter.setColor(0);
          int strWidth = GameEngine.gameTitleFnt.stringWidth(currGame.fullName);
          int gameBoxSize = GameEngine.gameTitleFnt.getFontHeight()+margin*3/2+maxIconSize;
          GraphicsAdapter.drawImage(currGame.icon, width/2-currGame.icon.getWidth()/2, bodyStart+margin/2+maxIconSize-currGame.icon.getHeight(), GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
          GraphicsAdapter.drawString(currGame.fullName, width/2-strWidth/2, bodyStart+margin+maxIconSize, GraphicsAdapter.LEFT|GraphicsAdapter.TOP);
          GraphicsAdapter.drawRect(width/2-strWidth/2-margin, bodyStart, strWidth+2*margin, gameBoxSize);
          
          //Draw: Button
          int btnX = width/2 - GameEngine.gameTitleFnt.stringWidth("Start")/2 - margin;
          int btnY = bodyStart + gameBoxSize + margin/2;
          int btnW = GameEngine.gameTitleFnt.stringWidth("Start") + 2*margin;
          int btnH = GameEngine.gameTitleFnt.getFontHeight() + margin;
          GraphicsAdapter.drawRect(btnX, btnY, btnW, btnH);
          GraphicsAdapter.drawString("Start", btnX+margin, btnY+margin/2, GraphicsAdapter.LEFT|GraphicsAdapter.TOP);
          drawSelectionBox(btnX, btnY, btnW, btnH, 0xFF0000);
          
          //To make things easier later
          percBarStart = btnY+btnH + margin + 4;
    }
    
    public static void drawLoadingSign(int width, int height, int bkgrdColor, int fregrdColor, char displayChar) {
        int boxSize = 25;
        int sX = width - boxSize + (boxSize-GameEngine.progressFnt.stringWidth(""+displayChar))/2 -1;
        int sY = (boxSize - GameEngine.progressFnt.getFontHeight())/2 + 1;
        
        //Draw: L in a circle
        GraphicsAdapter.setColor(bkgrdColor);
        GraphicsAdapter.fillArc(width-boxSize-2, 2, boxSize, boxSize, 0, 360);
        GraphicsAdapter.setColor(fregrdColor);
        GraphicsAdapter.drawArc(width-boxSize-2, 2, boxSize, boxSize, 0, 360);
        GraphicsAdapter.setColor(0xFFFFFF);
        GraphicsAdapter.setFont(GameEngine.progressFnt);
        GraphicsAdapter.drawString(""+displayChar, sX, sY, GraphicsAdapter.LEFT|GraphicsAdapter.TOP);
    }
    
    public static void drawPercentage(long numerator, long denomenator, String caption, int width, int height) {
        int marginP = 10;
        int percentHeight = 20;
        int percLoaded = (int)(numerator*100/denomenator);

        //Clear the caption area, draw the captions
        GraphicsAdapter.setColor(0xFFFFFF);
        GraphicsAdapter.fillRect(0, percBarStart-12, width, 12);
        GraphicsAdapter.setColor(0x0);
        GraphicsAdapter.setFont(GameEngine.gameTitleFnt);
        GraphicsAdapter.drawString(caption, marginP, percBarStart-12, GraphicsAdapter.LEFT|GraphicsAdapter.TOP);
        GraphicsAdapter.drawString(numerator+"", marginP+(width-2*marginP), percBarStart-12, GraphicsAdapter.RIGHT|GraphicsAdapter.TOP);
        
        //Draw percentage bar
        GraphicsAdapter.setColor(0xFFFFFF);
        GraphicsAdapter.fillRect(marginP, percBarStart, width-2*marginP, percentHeight);
        GraphicsAdapter.setColor(0x0000FF);
        GraphicsAdapter.fillRect(marginP, percBarStart, ((width-2*marginP)*percLoaded)/100, percentHeight);
        GraphicsAdapter.setColor(0x0);
        GraphicsAdapter.drawRect(marginP, percBarStart, width-2*marginP, percentHeight);
        
        //Draw the percentage
        GraphicsAdapter.setColor(0x0);
        GraphicsAdapter.setFont(GameEngine.progressFnt);
        GraphicsAdapter.drawString(percLoaded+"%", width/2, percBarStart+2, GraphicsAdapter.HCENTER|GraphicsAdapter.TOP);
        
        //Optionally draw a white % amount on top, clipping out what should be black 
        //  (white displays better on the blue progress bar.) Awesome trick.
        GraphicsAdapter.setClip(marginP, percBarStart, ((width-2*marginP)*percLoaded)/100, percentHeight);
        GraphicsAdapter.setColor(0xFFFFFF);
        GraphicsAdapter.drawString(percLoaded+"%", width/2, percBarStart+1, GraphicsAdapter.HCENTER|GraphicsAdapter.TOP);
        GraphicsAdapter.setClip(0, 0, width, height);
    }
    
    public static void drawInitialScreen(int width, int height) {
    	GraphicsAdapter.setColor(0xFFFFFF);
    	GraphicsAdapter.fillRect(0, 0, width, height);
        MetaDisplay.drawHeader(width);
        MetaDisplay.drawError(new LiteException(MetaDisplay.class, null, "Initializing: Please wait..."), width, height);
    }
    
    public static void drawError(LiteException lastError, int width, int height) {
        //In case we're out of memory:
        System.gc();
        long memInUse = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); 
        
        //Fill the background
        GraphicsAdapter.setColor(0xFFFFFF);
        GraphicsAdapter.fillRect(0, 0, width, height);
        
        int baseY = margin;
        int srcX = margin;
        
        boolean drawnIcon = false;
        if (LiteException.errorIcon!=null) {
            //Draw the (user-defined) image in the top-left corner.
            try {
                GraphicsAdapter.drawImage(LiteException.errorIcon, srcX, baseY);
                srcX += LiteException.errorIcon.getWidth() + margin;
                drawnIcon = true;
            } catch (Throwable th) {}
        }
        
        if (!drawnIcon) {
            //Draw the (embedded) image in the top-left corner.
            GraphicsAdapter.drawRGB(LiteException.getErrorImg(), 0, LiteException.errorImgWidth, srcX, baseY, LiteException.errorImgWidth, LiteException.getErrorImg().length/LiteException.errorImgWidth, true);
            srcX += LiteException.errorImgWidth + margin;
        }
        
        //Get the top-most error
        String[] currError = LiteException.peekTopError();
        String throwClass = currError[0];
        String error = currError[1];
        String message = currError[2];
        
        //Draw the top-most error's title, calling class, and exception raised (wrap only the title)
       // StringBuilder currLine = new StringBuilder();
        StringBuffer remMsg = new StringBuffer(message);
        GraphicsAdapter.setFont(GameEngine.errorTitleFont);
        GraphicsAdapter.setColor(0xFF0000);
        while (remMsg.length()>0) {
        	//Measure this line
        	int remWidth = width - srcX - margin;
        	int i = 0;
        	for (; i<remMsg.length() && remWidth>0; i++) {
        		char nextChar = remMsg.charAt(i);
        		remWidth -= GraphicsAdapter.getFont().stringWidth(nextChar+"");
        	}
        	
        	//Extract and draw this line.
                String currLine = substring(remMsg, 0, i);
        	remMsg.delete(0, i);
        	GraphicsAdapter.drawString(currLine, srcX, baseY, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
        	baseY += GraphicsAdapter.getFont().getFontHeight();
        }
        
        //Draw the top-most error's class and error class; don't wrap
        GraphicsAdapter.setFont(GameEngine.errorMsgFnt);
        GraphicsAdapter.setColor(0);
        GraphicsAdapter.drawString(throwClass, srcX, baseY, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
    	baseY += GraphicsAdapter.getFont().getFontHeight();
    	GraphicsAdapter.drawString(error, srcX, baseY, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
    	baseY += GraphicsAdapter.getFont().getFontHeight();
    	
    	//Push up to the end of the error icon, at least
    	baseY = Math.max(baseY, margin+LiteException.getErrorImg().length/LiteException.errorImgWidth) + 2*margin;
    	srcX = margin;
    	int boxTLX = srcX - margin/2;
    	int boxTLY = baseY - margin/2;
    	
    	//Draw each exception in the stack trace
    	GraphicsAdapter.setFont(GameEngine.progressFnt);
    	GraphicsAdapter.drawString("Full Trace:", srcX, baseY, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
    	baseY += GraphicsAdapter.getFont().getFontHeight() + margin;
    	GraphicsAdapter.setFont(GameEngine.errorMsgFnt);
    	
    	
    	while (LiteException.hasErrorsLeft()) {
    		currError = LiteException.remTopError();
            String temp = currError[0];
            currError[0] = currError[2];
            currError[2] = temp; 
            if (currError[0].length()>0)
            	currError[0] = "\""+currError[0]+"\"";
            
            for (int i=0; i<currError.length; i++) {
            	//Draw this line
            	StringBuffer line = new StringBuffer(currError[i]);
            	if (line.length()==0)
            		continue;
            	
            	int remWidth = width - margin*2;
            	int id = 0;
            	for (; id<line.length() && remWidth>0; id++) {
            		char nextChar = line.charAt(id);
            		remWidth -= GraphicsAdapter.getFont().stringWidth(nextChar+"");
            	}
            	
            	//Extract and draw this line.
            	String currLine = substring(line, 0, id);
            	line.delete(0, id);
            	GraphicsAdapter.drawString(currLine, srcX, baseY, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);
            	baseY += GraphicsAdapter.getFont().getFontHeight();
            }
            baseY += margin;
    	}
    	baseY += margin;
    	
    	int boxBRY = baseY - margin/2;
    	baseY += margin;
    	
    	//Draw total memory
    	GraphicsAdapter.setFont(GameEngine.progressFnt);
    	GraphicsAdapter.setColor(0x007F00);
    	GraphicsAdapter.drawString(formatMemory(memInUse)  + " used", width/2, baseY, GraphicsAdapter.TOP|GraphicsAdapter.HCENTER);
    	baseY += margin/2 + GameEngine.progressFnt.getFontHeight();
    	GraphicsAdapter.drawString(formatMemory(Runtime.getRuntime().totalMemory()) + " total", width/2, baseY, GraphicsAdapter.TOP|GraphicsAdapter.HCENTER);
    	
    	//Draw surrounding box
    	GraphicsAdapter.setColor(0);
    	GraphicsAdapter.drawRect(boxTLX, boxTLY, width-2*margin, boxBRY-boxTLY);
        
        //Flush
    	GraphicsAdapter.flushGraphics();
    }
    
    private static final String substring(StringBuffer str, int start, int end) {
        char[] buff = new char[end-start];
        str.getChars(start, end, buff, 0);
        return new String(buff);
    }
    
    private static String formatMemory(long mem) {
    	if (mem < 1024) {
    		return mem + " B";
    	}
    	
    	int rem = (int)((mem/1024F)*10);
    	mem = mem / 1024;
    	if (mem < 1024) {
    		return mem + "." + rem + " KB";
    	}

    	rem = (int)((mem/1024F)*10);
    	mem = mem / 1024;
    	if (mem < 1024) {
    		return mem + "." + rem + " MB";
    	}

    	rem = (int)((mem/1024F)*10);
    	if (rem>99)
    		rem = 0;
    	mem = mem / 1024;
    	return mem + "." + rem + " GB";
    }
    
    private static void drawDoors(int sX, int sY, int tileSize, String capt) {
        if (!DEBUG_OUTLINE_DOORS)
        	return;
    	
        //Draw a door outline, for now
        GraphicsAdapter.setColor(0xCCCC00);
        GraphicsAdapter.drawRect(sX, sY, tileSize-1, tileSize-1);
        GraphicsAdapter.setColor(0xFF0033);
        GraphicsAdapter.drawString(capt, sX, sY, GraphicsAdapter.TOP|GraphicsAdapter.LEFT);

    }
    
    public static void debugPaint(OHRRPG rpg, int width, int height) {
        //Draw the "Loading" sign while loading the cached data.
        try {
            rpg.testCaches();
            clearCanvas(width, height);
        } catch (Throwable th) {
            throw new LiteException(MetaDisplay.class, th, "CACHE ERROR");
        }
        
        int tSize = 0;
        int offsetX = 0;
        int offsetY = 0;
        int firstTileX = 0;
        int firstTileY = 0;
        int lastTileX = 0;
        int lastTileY = 0;
        try {
            //Figure out the scroll, based on the player's current location.
            tSize = TilesetParser.TILE_SIZE;
            offsetX = width/2 - rpg.getActiveHero().getPixelX() - tSize;
            offsetX = Math.min(offsetX, 0);
            offsetX = Math.max(offsetX, width - rpg.getCurrMap().getWidth()*tSize);
            offsetY = height/2 - rpg.getActiveHero().getPixelY() - tSize;
            offsetY = Math.min(offsetY, 0);
            offsetY = Math.max(offsetY, height - rpg.getCurrMap().getHeight()*tSize);
            
            //Now, get the tiles that actually need showing.
            firstTileX = (int)Math.floor(-offsetX/tSize);
            firstTileY = (int)Math.floor(-offsetY/tSize);
            lastTileX = (int)Math.floor((-offsetX+width-1)/tSize);
            lastTileY = (int)Math.floor((-offsetY+height-1)/tSize);
        } catch (Throwable th) {
        	throw new LiteException(MetaDisplay.class, th, "CALC ERROR");
        }
        
        //This check is not too costly, and it avoids ArrayIndexOutOfBounds errors that are otherwise hard to trace.
        if (lastTileX>=rpg.getCurrMap().getWidth() || lastTileY >= rpg.getCurrMap().getHeight()) {
            throw new LiteException(MetaDisplay.class, null, "Impropor Tile: " + firstTileX + "," + firstTileY + " : " + lastTileX + "," + lastTileY);
        }
       
        LinkedList overheadTiles = null;
        try {
            //Draw the tiles (don't process the alpha channel -they're just tiles.)
            overheadTiles = null;
            overheadTiles = new LinkedList(Integer.MAX_VALUE);
            for (int y=firstTileY; y<=lastTileY; y++) {
                for (int x=firstTileX; x<=lastTileX; x++) {
                    int currTileID = rpg.getActiveMap().tileAt(x, y);
                    // if (tileData!=null) { //Ignore illegal tiles
                    if (rpg.getActiveMap().isOverhead(x, y)) {
                        int doorCount = 0;
                        if (rpg.getActiveMap().hasDoor(x, y)) {
                            doorCount = rpg.getActiveMap().doorsAt(x, y).getSize();
                        }
                        overheadTiles.insertIntoFront(new Object[]{new Integer(currTileID), new Integer(tSize*x+offsetX), new Integer(tSize*y+offsetY), new Integer(x), new Integer(y), ""+doorCount});
                    } else {
                        ((TileData)rpg.getCurrMap().getTileset(false).tsData).draw(currTileID, tSize*x+offsetX, tSize*y+offsetY, width, height);
                        if (rpg.getActiveMap().hasDoor(x, y)) {
                            drawDoors(tSize*x+offsetX, tSize*y+offsetY, tSize, ""+rpg.getActiveMap().doorsAt(x, y).getSize());
                        }
                    }
                }
            }
        } catch (NullPointerException ex) {
        	String sub = "";
        	if (rpg==null)
        		sub = "rpg is null";
        	else if (rpg.getActiveMap() == null)
        		sub = "ActiveMap is null";
            throw new LiteException(MetaDisplay.class, ex, "NULL TILE: " + sub);
        } catch (Throwable th) {
        	throw new LiteException(MetaDisplay.class, th, "TILE ERROR");
        }
        
        try {
            if (rpg.getCurrMap().drawHerosFirst) {
                drawHeroes(rpg, offsetX, offsetY);
                drawNPCs(rpg, offsetX, offsetY, new int[] {firstTileX-1, lastTileX+1, firstTileY-1, lastTileY+1});
            } else {
                drawNPCs(rpg, offsetX, offsetY, new int[] {firstTileX-1, lastTileX+1, firstTileY-1, lastTileY+1});
                drawHeroes(rpg, offsetX, offsetY);            
            }
        } catch (Throwable th) {
            throw new LiteException(MetaDisplay.class, th, "SPRITE ERROR");
        }
        
        
        try {
            //Draw overhead tiles
            while (overheadTiles.getSize()>0) { //What a MESS!
                Object o = overheadTiles.removeFromBack();
                Object[] drawTile = (Object[])o;
                int tileID = ((Integer)drawTile[0]).intValue();
                int posX = ((Integer)drawTile[1]).intValue();
                int posY = ((Integer)drawTile[2]).intValue();
                int tileX = ((Integer)drawTile[3]).intValue();
                int tileY = ((Integer)drawTile[4]).intValue();
                ((TileData)rpg.getCurrMap().getTileset(false).tsData).draw(tileID, posX, posY, width, height);
                String caption = (String)drawTile[5];
                if (rpg.getActiveMap().hasDoor(tileX, tileY)) {
                    drawDoors(posX, posY, tSize, caption);
                }
            }
        } catch (Throwable th) {
            throw new LiteException(MetaDisplay.class, th, "OVERHEAD ERROR");
        }
        
        try {
            //Draw the map name?
            if (rpg.getActiveMap().isMapNameShowing()) {
                rpg.getActiveMap().mapNameBox.paint(width/2, height, GraphicsAdapter.BOTTOM|GraphicsAdapter.HCENTER);
            }
        
            //Draw the current text box
            Message box = rpg.getCurrTextBox();
            if (box!=null) {
                box.paint(width, height);
                //g.drawRGB(box.getText(), 0, width, 0, 0, width, height, true);
            } else {
                BattlePrompt bp = rpg.getCurrBattlePrompt();
                if (bp!=null)
                    bp.paint(width, height);
            }
        } catch (Throwable th) {
            throw new LiteException(MetaDisplay.class, th, "TXTBOX ERROR");
        }
    }
    
    
    /*private static void drawLetter(Graphics g, OHRRPG rpg, int x, int y, int letterValue, int width, int height) {
        int letterX = 8*(letterValue%16);
        int letterY = 8*(letterValue/16);
        
        g.setClip(x, y, 8, 8);
        g.drawImage(rpg.getFontImage(), -letterX, -letterY,  Graphics.TOP|Graphics.LEFT);
        g.setClip(0, 0, width, height);
    }*/
    
    
    //tileBounds[firstTileX, lastTileX, firstTileY, lastTileY]
    private static void drawNPCs(OHRRPG rpg, int offsetX, int offsetY, int[] tileBounds) {
        //Draw the NPCs
        for (int i=0; i<rpg.getActiveMap().getNPCs().length; i++) {
            if (rpg.getActiveMap().getNPCs()[i].isVisible() && rpg.getActiveMap().getNPCs()[i].inBounds(tileBounds))
                drawSprite(rpg.getActiveMap().getNPCs()[i], offsetX, offsetY);
            /*else
                System.out.println("Out of bounds: " + i + "   : " + rpg.getActiveMap().getNPCs()[i].getTileX() + "," + rpg.getActiveMap().getNPCs()[i].getTileY());*/
        }
    }
    
    private static void drawHeroes(OHRRPG rpg, int offsetX, int offsetY) {
        //For now, just the first hero
        //if (!rpg.isRidingVehicle() || rpg.getActiveVehicle().doNotHideLeader) //too slow
        drawSprite(rpg.getActiveHero(), offsetX, offsetY);
    }
    
    private static void drawSprite(ActiveWalker sprite, int offsetX, int offsetY) {
        //Draw any character that can "walk" with a walkabout
        int screenX = sprite.getPixelX() + offsetX;
        int screenY = sprite.getPixelY() + offsetY;
        int sX = sprite.getTileX()*TilesetParser.TILE_SIZE +offsetX+1;
        int sY = sprite.getTileY()*TilesetParser.TILE_SIZE+ offsetY+1;
        GraphicsAdapter.setColor(0x0000FF);
        if (!sprite.hasNoPicture())
        	GraphicsAdapter.drawRGB(sprite.getCurrFrame(), 0, PictureParser.PT_WALKABOUT_SIZES[0], screenX, screenY, PictureParser.PT_WALKABOUT_SIZES[0], PictureParser.PT_WALKABOUT_SIZES[1], true);
        else if (DEBUG_OUTLINE_SPRITES) {
        	GraphicsAdapter.drawLine(sX, sY, sX+PictureParser.PT_WALKABOUT_SIZES[0]-2, sY+PictureParser.PT_WALKABOUT_SIZES[1]-2);
        	GraphicsAdapter.drawLine(sX+PictureParser.PT_WALKABOUT_SIZES[0]-2, sY, sX, sY+PictureParser.PT_WALKABOUT_SIZES[1]-2);
        }
        
        if (DEBUG_OUTLINE_SPRITES)
            GraphicsAdapter.drawRect(sX, sY, PictureParser.PT_WALKABOUT_SIZES[0]-2, PictureParser.PT_WALKABOUT_SIZES[1]-2);
    }
}
