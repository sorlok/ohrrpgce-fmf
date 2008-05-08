/*
 * GameEngine.java
 * Created on December 5, 2006, 3:06 PM
 */

package ohrrpgce.game;

import ohrrpgce.adapter.*;
import ohrrpgce.data.*;
import ohrrpgce.data.loader.*;
import ohrrpgce.menu.MenuSlice;
import ohrrpgce.runtime.*;

/**
 *
 * @author sethhetu
 */
public class GameEngine extends Engine {
	//Constants
	private static final int[] keyDetectOrder = new int[] {InputAdapter.KEY_CANCEL, InputAdapter.KEY_ACCEPT, InputAdapter.KEY_UP, InputAdapter.KEY_DOWN, InputAdapter.KEY_LEFT, InputAdapter.KEY_RIGHT};
	private static final int[] keyRealIDs = new int[]{InputAdapter.KEY_CANCEL, InputAdapter.KEY_ACCEPT, NPC.DIR_UP, NPC.DIR_DOWN, NPC.DIR_LEFT, NPC.DIR_RIGHT};
    private static final int DEFAULT_CACHE_SIZE = 4;
    private static final int MULTIPRESS_DELAY = 4;
    
    //Idle states
    private static final int IDLE_NOTHING = 0;
    private static final int IDLE_GAME_LIST = 1;
    private static final int IDLE_PLOTSCRIPT = 2;
    private int idleState;
    
    //Our four different fonts
    public static FontAdapter errorTitleFont;
    public static FontAdapter errorMsgFnt;
    public static FontAdapter gameTitleFnt;
    public static FontAdapter progressFnt;

    //Screen size
    private int width;
    private int height;
    
    //Game-related information
    private Meta metaInfo;
    private OHRRPG rpg;
    private AdapterGenerator adaptGen;
    
    //Cache loader
    private RPGLoadSizeListener loadListen;
    
    //Ambiguous usage
    //private int gameSelectTimer;
    private int currHeroID;
    
    //Handle keypresses intelligently
    private int delayTimer;
    
    //Hook back to the main game
    private EngineSwitcher midletHook;

    
    public static void initFonts(AdapterGenerator generator) {
        errorTitleFont = generator.createErrorTitleFont();
        errorMsgFnt = generator.createErrorMessageFont();
        gameTitleFnt = generator.createGameTitleFont();
        progressFnt = generator.createProgressFont();
    }
    
    
    /** Creates a new instance of GameEngine */
    public GameEngine(AdapterGenerator generator, RPGLoadSizeListener loadListen, EngineSwitcher midletHook/*, VirtualMachine hvm*/) {
        this.width = generator.getScreenWidth();
        this.height = generator.getScreenHeight();
        this.adaptGen = generator;
        this.loadListen = loadListen;
        this.midletHook = midletHook;

        metaInfo = new Meta(generator);
        Message.initTextBox(width, height);
        BattlePrompt.initPrompts(width, height);
    }

    public void paintScene() {
        if (rpg==null) {
            try {
            	metaInfo.paintLibrary(width, height);
            } catch (Exception ex) {
                throw new LiteException(this, ex, "LIB ERROR: " + ex.getClass().toString() + ": " + ex.getMessage());
            }
        } else if (rpg.getBaseRPG()==null) {
            try {
            	metaInfo.paintCurrentGame(width, height);
            } catch (Exception ex) {
                throw new RuntimeException("LIST ERROR: " + ex.getClass().toString() + ": " + ex.getMessage());
            }
        } else {
            try {
                MetaDisplay.debugPaint(rpg, width, height);
            } catch (Throwable ex) {
                throw new LiteException(this, ex, "");
            }
        }
    }

    
    /**
     * Just process raw input here; handle it somewhere else.
     * Input handling code is mostly stolen from our menu engine...
     */
    public void handleKeys(int keyStates) {
        //The key autofires if pressed for a certain length of time.
        if (keyStates==0) {
            delayTimer = 0;
        } else {
            if (delayTimer<=0) {
            	int key = -1;
            	for (int i=0; i<keyDetectOrder.length; i++) {
            		if ((keyStates&keyDetectOrder[i])!=0) {
            			key = keyRealIDs[i];
            			break;
            		}
            	}
            	if (key==-1)
            		throw new LiteException(this, null, "Somehow, no input was pressed (but expected) for input: " + keyStates);
            	
            	//Actually handle this input based on the current state of the game
            	if (rpg==null || rpg.getBaseRPG()==null)
            		handlePregameInput(key);
            	else
            		handleGameInput(key);
                
                if (delayTimer==0)
                    delayTimer = MULTIPRESS_DELAY;
            } else {
                delayTimer--;
                if (delayTimer==0)
                    delayTimer = -1; //Allow auto-fire.
            }
        }
    }
    
    
    private void handleGameInput(int key) {
    	//OHR key-detect order: MENU, ENTER, UP, DOWN, LEFT, RIGHT
    	//...but, this is presupposed by handleInput, so let's just switch on it 
    	//   and gain back some performance.
    	switch (key) {
    		case InputAdapter.KEY_CANCEL:
    			//Skip if "suspend player" bitset is on.
    			if (rpg.suspendedPlayer)
    				break;
    			
    			//Handle our auto-quit menu?
            	if (rpg.getCurrentQuitMenu()!=null)
            		rpg.hideQuitMenu();
            	else
            		midletHook.switchEngine(Engine.MENU);
    			break;
    		case InputAdapter.KEY_ACCEPT:
    			//Check active prompts first
    			if (rpg.getCurrentQuitMenu()!=null)
    				adaptGen.exitGame(true);
    			else if (rpg.getCurrTextBox()!=null)
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
    			break;
    		default:
    			if (rpg.heroCanMove()) {
    				rpg.stepHero(key);
    			} else if (rpg.getCurrTextBox()!=null) {
    				rpg.getCurrTextBox().processInput(key);
    			} else if (rpg.getCurrBattlePrompt()!=null) {
    				rpg.getCurrBattlePrompt().processInput(key);
    			}
    	}
    }
    
    
    private void handlePregameInput(int key) {
        if (rpg==null) {
            //We're at the main loader screen
        	if (metaInfo.navigateLibrary(key))
        		rpg = new OHRRPG(loadListen);
        } else {
        	//Only handle "ACCEPT" here
        	if (key!=InputAdapter.KEY_ACCEPT)
        		return;
        	
        	//Prepare to load the game
            MetaGame currentGame = metaInfo.getCurrentGame();
            System.out.println("Loading game: " + currentGame.fullName);
            metaInfo.stopLoadingGames();
            adaptGen.setGameName(currentGame.name);
            RPGLoader loader = null;
            if (currentGame.mobileFormat==0) {
            	throw new LiteException(this, new IllegalArgumentException(), "RPG lump format not supported; please convert to XRPG.");
            } else if (currentGame.mobileFormat==1) {
            	if (!currentGame.name.toUpperCase().equals(currentGame.name))
            		throw new LiteException(this, new IllegalArgumentException(), "Error! Version 1 requires that the game name be in upper case.");
            	loader = new SensifiedLoader(currentGame.name, adaptGen, currentGame.numBytes);
            } else {
                throw new LiteException(this, new IllegalArgumentException(), "Invalid RPG format: " + currentGame.mobileFormat);
            }

            //Load the game
            if (loadListen!=null)
            	loader.addSizeUpdateListener(loadListen);
            rpg.setBaseRPG(new RPG(loader, loadListen, DEFAULT_CACHE_SIZE));
            if (currentGame.errorIcon!=null)
            	LiteException.setErrorIcon(currentGame.errorIcon);

            //Start the game
            System.out.println("Game loaded");
            rpg.setCurrMap(rpg.getBaseRPG().getStartingMap());
            //DEBUG:
            //rpg.setCurrMap(1);
            System.out.println("Map Set");
            rpg.setCurrHero(currHeroID, rpg.getBaseRPG().getStartX(), rpg.getBaseRPG().getStartY());
            //DEBUG:
            //rpg.setCurrHero(currHeroID, 24,61);
            System.out.println("Hero Set");
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
     * Update the game state. Updates are guaranteed to occur once per tick.
     * @elapsed The actual number of ms elapsed in this tick, (possibly) for smoother updates.
     */
    public void updateScene(long elapsed) {
        if (rpg==null) {
            //We're at the main loader screen
            if (metaInfo.getGames()==null) {
                if (!metaInfo.gameListError) {
                    //Load the list of games.
                    System.out.println("Loading games list");
                    metaInfo.loadGamesList();
                    idleState = IDLE_GAME_LIST;
                }
            } else {
                //Interleave loading and input...
                if (!metaInfo.allLoaded()) {
                	//Load a few characters at a time
                	for (int count=0; count<5 && !metaInfo.allLoaded(); count++)
                		metaInfo.continueLoading();
                }
            }
        } else if (rpg.getBaseRPG()!=null) {
            //The game is running
            rpg.update(elapsed);
        }
    }

    public OHRRPG getRPG() {
        return rpg;
    }
    
    public void drawPercentage(long bytesLoaded, String caption) {
        MetaDisplay.drawPercentage(bytesLoaded, metaInfo.getCurrentGame().numBytes, caption, width, height);
    }

    public void drawLoadingSign(int bkgrdColor, int fgrdColor, char displayChar) {
        MetaDisplay.drawLoadingSign(width, height, bkgrdColor, fgrdColor, displayChar);
    }

    public void communicate(Object stackFrame) {
        System.out.println("ENGINE returned to GAME");
        System.out.println(stackFrame.getClass().toString());
        
        //Reset key presses
        delayTimer = MULTIPRESS_DELAY;
    }

    public void reset() {
        throw new LiteException(this, null, "GAME should never be asked to RESET.");
    }

    public boolean canExit() {
    	//Always exit if no game's chosen
    	if (rpg==null || rpg.getBaseRPG()==null)
    		return true;
    	
    	//Otherwise, we show our "quit" menu.
    	if (rpg.getCurrentQuitMenu()==null) {
    		rpg.showQuitMenu(adaptGen, width);
    		
    		//Reset key presses
    		delayTimer = MULTIPRESS_DELAY;
    	}
    	return false;
    }





}
