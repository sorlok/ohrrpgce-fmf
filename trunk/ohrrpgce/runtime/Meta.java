/*
 * Meta.java
 * Created on January 8, 2007, 11:54 AM
 */

package ohrrpgce.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.InputAdapter;
import ohrrpgce.data.NPC;
import ohrrpgce.game.LiteException;


/**
 * Contains meta-information about all game files the OHR knows about, their formats, and icons.
 * @author Seth N. Hetu
 */
public class Meta {
    public static final String pathToGameFolder = "/ohrrpgce/games/";
    private static final String pathToGameList = pathToGameFolder+"game_list.txt";
    private MetaGame[] games;
    private Vector gamesSoFar;
    public boolean gameListError;
//    private boolean earlyCancel;
    private boolean allGamesLoaded;
    
    //Segmented loading
    private InputStream is;
    private BufferedInputStreamReader inFile;
    private MetaGame currGame;
    private int currProp;
    private StringBuffer sb;
    //private boolean stopMidletHook;
    
    private AdapterGenerator adaptGen;
  //  private FileAdapter fileAdapt;
    
    private int currentGameID;
    
    public boolean gamesLibraryIsEmpty() {
        return (getGames()!=null && allGamesLoaded && getGames().length==0);
    }
    
    public boolean allLoaded() {
        return allGamesLoaded;
    }
    
    public Meta(AdapterGenerator generator) {
    	this.adaptGen = generator;
	}
    
    public MetaGame[] getGames() {
        if (gamesSoFar==null)
            return null;
        
        if (games==null || (games.length!=gamesSoFar.size())) {
            games = new MetaGame[gamesSoFar.size()];
            for (int i=0; i<gamesSoFar.size(); i++) {
              games[i] = (MetaGame)gamesSoFar.elementAt(i);
            }
        }
        
        return games;
    }
    
    
    public void stopLoadingGames() {
        allGamesLoaded = true;
        if (is!=null) {
            try {
                inFile.close();
                is = null;
                System.out.println("Game input file closed.");
            } catch (IOException ex) {}
        }
    }

    
    private void addProp(MetaGame currGame, StringBuffer sb, int propID) {
        switch (propID) {
            case 0:
                currGame.name = sb.toString();
                break;
            case 1:
                currGame.fullName = sb.toString();
                break;
            case 2:
                try {
                    currGame.icon = adaptGen.createImageAdapter(pathToGameFolder + sb.toString());
                } catch (IOException ex)  {
                    currGame.icon = adaptGen.createBlankImage(10, 10);
                }
                break;
            case 3:
                try {
                    currGame.numBytes = Integer.parseInt(sb.toString());
                } catch (NumberFormatException ex) {
                    System.out.println("Bad size: (" + sb.toString() + ")");
                }
                break;
            case 4:
                try {
                    currGame.mobileFormat = Integer.parseInt(sb.toString());
                } catch (NumberFormatException ex) {
                    System.out.println("Bad number: (" + sb.toString() + ")");
                }
                break;
            case 5:
                try {
                    currGame.errorIcon = adaptGen.createImageAdapter(pathToGameFolder + sb.toString());
                } catch (Throwable ex)  {
                    currGame.errorIcon = null;
                }
                break;
            default:
                System.out.println("Error! Invalid property: " + propID);
        }
    }
    
    public void loadGamesList() {
        if (is!=null)
            throw new RuntimeException("Error! Cannot restart loading midway through.");
        
        //Find the games list.
        allGamesLoaded = false;
        is = getClass().getResourceAsStream(pathToGameList);
        if (is==null) {
            gameListError = true;
            return;
        }
        inFile = new BufferedInputStreamReader(is);
        
        //Prepare the results vector
        gamesSoFar = new Vector(5);
        currGame = new MetaGame();
        currProp=0;
        sb = new StringBuffer();
    }
    
    
    public void continueLoading() {
        if (is==null)
            throw new RuntimeException("Error! Cannot continue loading on a null input file.");
        
        //Have we reached the end of the file?
        boolean fileDone = false;
        char c = '\0';
        try {
            c = inFile.readChar();
        } catch (IOException ex) {
            throw new RuntimeException("Game_List_Read: " + ex.getClass().getName() + ":" + ex.getMessage());
        }
        
        fileDone = inFile.isDone();
        if (!fileDone) {
       //     System.out.println("Read: " + c + "  [" + (int)c + "]");
            if (c=='\n' || c=='\t') {
                //End of a property
                addProp(currGame, sb, currProp);
                currProp++;
                sb = new StringBuffer();
            } else if (c!='\r') //Silly Windows
                sb.append(c);
            
            if (c=='\n') {
                //Two newlines in a row mean EOF
                if (currGame.name==null)
                    fileDone = true;
                else {                
                    //End of a game
                    gamesSoFar.addElement(currGame);
                    currGame = new MetaGame();
                    currProp = 0;
                }
            }
        }
        
        if (fileDone) {
            //One extra game?
            if (currGame.name!=null) {
                System.out.println("Extra game: " + currGame.name);
                //In case there's no ending newline
                if (sb.length()>0)
                    addProp(currGame, sb, currProp);
                gamesSoFar.addElement(currGame);
            }
            
            stopLoadingGames();
        }    
    }
    
    
    /**
     * Paint the internal state of the library
     */
    public void paintLibrary(int width, int height) {
        //Clear
        MetaDisplay.clearCanvas(width, height);

        //We're at the main loader screen
        MetaDisplay.drawHeader(width);
        if (this.getGames()==null) {
            if (this.gameListError) {
                //The game list doesn't exist, for some reason.
            	throw new LiteException(this, null, "Invalid Game Library: OHRRPGCEFMF uses a text file to store the locations of installed games. This file (game_list.txt) was not found in the OHRRPGCEFMF.JAR file. Consequently, no games can be loaded.");
            }
            //Let the user know we're loading, in case this takes time...
            MetaDisplay.drawError(new LiteException(this, null, "Reading Library: Please wait..."), width, height);
        } else if (this.gamesLibraryIsEmpty()) {
            //No games
        	throw new LiteException(this, null, "No Games: Your games library does not contain any games. While this is not an error, it will certainly reduce the utility of this program.");
        } else {
            //Show the list of games, & the current one.
            MetaDisplay.drawGameList(this.getGames(), currentGameID, width, height);
        }
    }
    
    
    /**
     * Paint whichever game is currently selected.
     */
    public void paintCurrentGame(int width, int height) {
        //Clear
        MetaDisplay.clearCanvas(width, height);

        //Show the game & its icon
        MetaDisplay.drawHeader(width);
        MetaDisplay.drawGameInfo(getCurrentGame(), width, height);
    }
    
    
    /**
     * Return true if a game should be loaded.
     */
    public boolean navigateLibrary(int key) {
   	    //Handle only relevant input
        if (games==null || games.length==0)
        	return false;
        
        if (key==NPC.DIR_UP)
        	this.incrementCurrentGameID(-1);
        else if (key==NPC.DIR_DOWN)
        	this.incrementCurrentGameID(1);
        else if (key==InputAdapter.KEY_ACCEPT)
        	return true;
        
        return false;
    }
    
    
    public void setCurrentGameID(int val) {
    	this.currentGameID = val;
    }
    public void incrementCurrentGameID(int plus) {
    	this.currentGameID += plus;
    	if (currentGameID<0)
    		currentGameID = games.length-1;
    	else if (currentGameID>=games.length)
    		currentGameID = 0;
    }
    public int getCurrentGameID() {
    	return currentGameID;
    }
    
    public MetaGame getCurrentGame() {
    	return games[currentGameID];
    }

}

