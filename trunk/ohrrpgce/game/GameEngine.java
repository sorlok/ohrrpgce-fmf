/*
 * GameEngine.java
 * Created on December 5, 2006, 3:06 PM
 */

package ohrrpgce.game;

import ohrrpgce.adapter.AdapterGenerator;
import ohrrpgce.adapter.FontAdapter;
import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.InputAdapter;
import ohrrpgce.data.BattlePrompt;
import ohrrpgce.data.NPC;
import ohrrpgce.data.RPG;
import ohrrpgce.data.TextBox;
import ohrrpgce.data.loader.RPGLoadSizeListener;
import ohrrpgce.data.loader.RPGLoader;
import ohrrpgce.data.loader.SensifiedLoader;
import ohrrpgce.henceforth.VirtualMachine;
import ohrrpgce.runtime.Meta;
import ohrrpgce.runtime.MetaDisplay;
import ohrrpgce.runtime.OHRRPG;

/**
 *
 * @author sethhetu
 */
public class GameEngine extends Engine {
    private static final int DEFAULT_CACHE_SIZE = 4;

    private int width;
    private int height;
    private OHRRPG rpg;
    private Meta metaInfo;
    private int currGameID;
    private int gameSelectTimer;
    private AdapterGenerator adaptGen;
    private RPGLoadSizeListener loadListen;
    private int currHeroID;
    private EngineSwitcher midletHook;
   // private VirtualMachine hvm;

    public static FontAdapter errorTitleFont;
    public static FontAdapter errorMsgFnt;
    public static FontAdapter gameTitleFnt;
    public static FontAdapter progressFnt;

    private static final int IDLE_NOTHING = 0;
    private static final int IDLE_GAME_LIST = 1;
    private static final int IDLE_PLOTSCRIPT = 2;
    private int idleState;

    /** Creates a new instance of GameEngine */
    public GameEngine(AdapterGenerator generator, RPGLoadSizeListener loadListen, EngineSwitcher midletHook/*, VirtualMachine hvm*/) {
        this.width = generator.getScreenWidth();
        this.height = generator.getScreenHeight();
        this.adaptGen = generator;
        this.loadListen = loadListen;
        this.midletHook = midletHook;
       // this.hvm = hvm;
        metaInfo = new Meta(generator);
        TextBox.initTextBox(width, height);
        BattlePrompt.initPrompts(width, height);
    }


    public static void initFonts(AdapterGenerator generator) {
        errorTitleFont = generator.createErrorTitleFont();
        errorMsgFnt = generator.createErrorMessageFont();
        gameTitleFnt = generator.createGameTitleFont();
        progressFnt = generator.createProgressFont();
    }


    public void paintScene() {
        if (rpg==null) {
            try {
                //Clear
                MetaDisplay.clearCanvas(width, height);

                //We're at the main loader screen
                MetaDisplay.drawHeader(width);
                if (metaInfo.getGames()==null) {
                    if (metaInfo.gameListError) {
                        //The game list doesn't exist, for some reason.
                    	throw new LiteException(this, null, "Invalid Game Library: OHRRPGCEFMF uses a text file to store the locations of installed games. This file (game_list.txt) was not found in the OHRRPGCEFMF.JAR file. Consequently, no games can be loaded.");
                    }
                    //Let the user know we're loading, in case this takes time...
                    MetaDisplay.drawError(new LiteException(this, null, "Reading Library: Please wait..."), width, height);
                } else if (metaInfo.gamesLibraryIsEmpty()) {
                    //No games
                	throw new LiteException(this, null, "No Games: Your games library does not contain any games. While this is not an error, it will certainly reduce the utility of this program.");
                } else {
                    //Show the list of games, & the current one.
                    MetaDisplay.drawGameList(metaInfo.getGames(), currGameID, width, height);
                }
            } catch (Exception ex) {
                throw new RuntimeException("LIB ERROR: " + ex.getClass().toString() + ": " + ex.getMessage());
            }
        } else if (rpg.getBaseRPG()==null) {
            try {
                //Clear
                MetaDisplay.clearCanvas(width, height);

                //Show the game & its icon
                MetaDisplay.drawHeader(width);
                MetaDisplay.drawGameInfo(metaInfo.getGames()[currGameID], width, height);
            } catch (Exception ex) {
                throw new RuntimeException("LIST ERROR: " + ex.getClass().toString() + ": " + ex.getMessage());
            }
        } else {
            //We're in the loop!
            try {
                MetaDisplay.debugPaint(rpg, width, height);
            } catch (Throwable ex) {
                //if (MetaDisplay.DEBUG_CONSOLE)
                //    MetaDisplay.DEBUG_MSG = ex.getMessage();
                throw new LiteException(this, ex, "");
            }
        }

    }


    public OHRRPG getRPG() {
        return rpg;
    }

    public void handleKeys(int keyStates) {
        if (rpg==null) {
            //We're at the main loader screen
            if (metaInfo.getGames()==null) {
            } else if (metaInfo.getGames().length==0) { //Don't let them mess up on no input...
            } else {
                //We're at the list of games
                if (gameSelectTimer==0) {
                    if ((keyStates&InputAdapter.KEY_UP) !=0) {
                        currGameID--;
                        if (currGameID<0)
                            currGameID = metaInfo.getGames().length-1;
                        gameSelectTimer = 750;
                    }
                    if ((keyStates&InputAdapter.KEY_DOWN) !=0) {
                        currGameID++;
                        if (currGameID==metaInfo.getGames().length)
                            currGameID = 0;
                        gameSelectTimer = 750;
                    }
                    if ((keyStates&InputAdapter.KEY_ACCEPT) !=0) {
                        //Load the current game
                        rpg = new OHRRPG(loadListen);
                        gameSelectTimer = 750;
                    }
                } else {
                    //Timeout by key release?
                    if ((keyStates&InputAdapter.KEY_UP) + (keyStates&InputAdapter.KEY_DOWN)==0)
                        gameSelectTimer = 0;
                }
            }
        } else if (rpg.getBaseRPG()==null) {
            if (gameSelectTimer==0) {
                if ((keyStates&InputAdapter.KEY_ACCEPT) !=0) {
                    //Select a loader for this game.
                    System.out.println("Loading game: " + metaInfo.getGames()[currGameID].fullName);
                    metaInfo.stopLoadingGames();
                    adaptGen.setGameName(metaInfo.getGames()[currGameID].name);
                    RPGLoader loader = null;
                    if (metaInfo.getGames()[currGameID].mobileFormat==0) {
                    	throw new RuntimeException("RPG lump format not supported; please convert to XRPG.");
               /*         loader = new LumpLoader(new RPGLoadAdapter() {
                        	public ByteStreamReader getRPGFile() {
                        		return new ByteStreamReader(this.getClass().getResourceAsStream(Meta.pathToGameFolder+metaInfo.getGames()[currGameID].name));
                        	}
                                public ByteStreamReader getLump(String lumpName) {
                                    throw new RuntimeException("Lump Loaders can't get individual lump files with the getLump() method.");
                                }
                        });*/
                    } else if (metaInfo.getGames()[currGameID].mobileFormat==1) {
						if (!metaInfo.getGames()[currGameID].name.toUpperCase().equals(metaInfo.getGames()[currGameID].name)) {
							throw new RuntimeException("Error! Version 1 requires that the game name be in upper case.");
						}
                        loader = new SensifiedLoader(metaInfo.getGames()[currGameID].name, adaptGen, metaInfo.getGames()[currGameID].numBytes);
                    } else {
                        throw new RuntimeException("Invalid RPG format: " + metaInfo.getGames()[currGameID].mobileFormat);
                    }

                    //Load the game
                    if (loadListen!=null)
                        loader.addSizeUpdateListener(loadListen);
                    rpg.setBaseRPG(new RPG(loader, loadListen, DEFAULT_CACHE_SIZE));

                    if (metaInfo.getGames()[currGameID].errorIcon!=null)
                        LiteException.setErrorIcon(metaInfo.getGames()[currGameID].errorIcon);

                    System.out.println("Game loaded");
                    //rpg.setCurrMap(rpg.getBaseRPG().getStartingMap());
                    //DEBUG:
                    rpg.setCurrMap(1);
                    System.out.println("Map Set");
                    //rpg.setCurrHero(currHeroID, rpg.getBaseRPG().getStartX(), rpg.getBaseRPG().getStartY());
                    //DEBUG:
                    rpg.setCurrHero(currHeroID, 24,61);
                    System.out.println("Hero Set");


                    gameSelectTimer = 750;
                }
            } else {
                //Timeout by key release?
                if ((keyStates&InputAdapter.KEY_ACCEPT) ==0)
                    gameSelectTimer = 0;
            }
        } else {
            //OHR key-detect order: MENU, ENTER, UP, DOWN, LEFT, RIGHT
            if ((keyStates&InputAdapter.KEY_CANCEL)!=0 && !rpg.suspendedPlayer) {
                if (gameSelectTimer==0) {
                    midletHook.switchEngine(Engine.MENU);
                    gameSelectTimer = 1;
                }
            } else if ((keyStates&InputAdapter.KEY_ACCEPT) !=0) {
                if (gameSelectTimer==0) {
                    if (rpg.getCurrTextBox()!=null)
                        rpg.endTextBox();
                    else if (rpg.getCurrBattlePrompt()!=null)
                        rpg.endBattlePrompt();
                    else  if (!rpg.suspendedPlayer) { //Don't do anything on suspend
                        boolean interact = true;
                        if (rpg.isRidingVehicle())
                            interact = !rpg.tryAndDismount();
                        if (interact)
                            rpg.getActiveHero().interact();
                    }
                    gameSelectTimer = 1;
                }
            } else {
                gameSelectTimer = 0;

                if (rpg.heroCanMove()) {
                    if ((keyStates&InputAdapter.KEY_UP) !=0) {
                        rpg.stepHero(NPC.DIR_UP);
                    } else if ((keyStates&InputAdapter.KEY_DOWN) !=0) {
                        rpg.stepHero(NPC.DIR_DOWN);
                    } else if ((keyStates&InputAdapter.KEY_LEFT) !=0) {
                        rpg.stepHero(NPC.DIR_LEFT);
                    } else if ((keyStates&InputAdapter.KEY_RIGHT) !=0) {
                        rpg.stepHero(NPC.DIR_RIGHT);
                    }
                } else if (rpg.getCurrTextBox()!=null) {
                    if ((keyStates&InputAdapter.KEY_UP) !=0) {
                        rpg.getCurrTextBox().processInput(NPC.DIR_UP);
                    } else if ((keyStates&InputAdapter.KEY_DOWN) !=0) {
                        rpg.getCurrTextBox().processInput(NPC.DIR_DOWN);
                    } else if ((keyStates&InputAdapter.KEY_LEFT) !=0) {
                        rpg.getCurrTextBox().processInput(NPC.DIR_LEFT);
                    } else if ((keyStates&InputAdapter.KEY_RIGHT) !=0) {
                        rpg.getCurrTextBox().processInput(NPC.DIR_RIGHT);
                    }
                } else if (rpg.getCurrBattlePrompt()!=null) {
                    if ((keyStates&InputAdapter.KEY_UP) !=0) {
                        rpg.getCurrBattlePrompt().processInput(NPC.DIR_UP);
                    } else if ((keyStates&InputAdapter.KEY_DOWN) !=0) {
                        rpg.getCurrBattlePrompt().processInput(NPC.DIR_DOWN);
                    } else if ((keyStates&InputAdapter.KEY_LEFT) !=0) {
                        rpg.getCurrBattlePrompt().processInput(NPC.DIR_LEFT);
                    } else if ((keyStates&InputAdapter.KEY_RIGHT) !=0) {
                        rpg.getCurrBattlePrompt().processInput(NPC.DIR_RIGHT);
                    }
                }
            }

        }
    }

    public boolean runIdle() {
    	switch (idleState) {
    		case IDLE_NOTHING:
    			return false;
    		case IDLE_GAME_LIST:
    			if (metaInfo.allLoaded())
    				idleState = IDLE_NOTHING;
    			else
    				metaInfo.continueLoading();
    			break;
    		case IDLE_PLOTSCRIPT:
    			break;
    		default:
    			throw new RuntimeException("Bad idle state: " + idleState);
    	}

    	return true;
    }


    /**
     * Update the game state. Updates are guarenteed to occur once per tick.
     * @elapsed The actual number of ms elapsed in this tick, (possibly) for smoother updates.
     */
    public void updateScene(long elapsed) {
        if (rpg==null) {
            //We're at the main loader screen
            if (metaInfo.getGames()==null) {
                if (metaInfo.gameListError) {
                } else {
                    //Load the list of games.
                    System.out.println("Loading games list");
                    metaInfo.loadGamesList();
                    idleState = IDLE_GAME_LIST;
                }
            } else {
                //Update our crude timer
                if (gameSelectTimer>0)
                    gameSelectTimer = (int)Math.max(0, gameSelectTimer-elapsed);

                //Interleave loading and input...
                if (!metaInfo.allLoaded())
                    metaInfo.continueLoading();
            }
        } else if (rpg.getBaseRPG()==null) {
                //Update our crude timer
                if (gameSelectTimer>0)
                    gameSelectTimer = (int)Math.max(0, gameSelectTimer-elapsed);
        } else {
            //The game is running
            rpg.update(elapsed);
        }
    }

    public void drawPercentage(long bytesLoaded, String caption) {
        MetaDisplay.drawPercentage(bytesLoaded, metaInfo.getGames()[currGameID].numBytes, caption, width, height);
    }

    public void drawLoadingSign(int bkgrdColor, int fgrdColor, char displayChar) {
        MetaDisplay.drawLoadingSign(width, height, bkgrdColor, fgrdColor, displayChar);
    }

    public void communicate(Object stackFrame) {
        System.out.println("ENGINE returned to GAME");
        System.out.println(stackFrame.getClass().toString());
    }

    public void reset() {
        throw new RuntimeException("GAME should never be asked to RESET.");
    }

    public boolean canExit() {
        return true;
    }





}
