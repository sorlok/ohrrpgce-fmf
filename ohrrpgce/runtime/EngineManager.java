package ohrrpgce.runtime;

import java.util.Stack;
import ohrrpgce.adapter.*;
import ohrrpgce.data.loader.*;
import ohrrpgce.game.*;

public class EngineManager implements Runnable {
	//Engine management
    private static Engine[] engines = new Engine[2];
    private int currEngine;
    private Thread canvasThread;
    
    //Engine switching and callback
    private EngineSwitcher midletHook;
    private Stack callStack;
    
    //Stored generators
   // private GraphicsAdapter gAdapt;
    private AdapterGenerator adaptGen;
    private InputAdapter iAdapt;
    private Exception bufferedError;
	
	public EngineManager(AdapterGenerator adaptGen, InputAdapter iAdapt) {
		//Store geneators
		this.adaptGen = adaptGen;
	//	this.gAdapt = gAdapt;
		this.iAdapt = iAdapt;
		
        //Allow engines to call engines.
        callStack = new Stack();
        midletHook = new MidletHookback();
	}
	
	/**
	 * Somewhat time-consuming
	 */
	private void initEngines() {
        engines[Engine.GAME] = new GameEngine(adaptGen, new RPGLoadSizeListener() {
            public void moreBytesRead(long bytesRead, String currLumpName) {
                ((GameEngine)engines[Engine.GAME]).drawPercentage(bytesRead, currLumpName);
                 GraphicsAdapter.flushGraphics();
            }
            public void readingUncachedData() {
                ((GameEngine)engines[Engine.GAME]).drawLoadingSign(0xFF0000, 0x330000, 'L');
                GraphicsAdapter.flushGraphics();
            }
            public void callingGC() {
                ((GameEngine)engines[Engine.GAME]).drawLoadingSign(0x00FF00, 0x003300, 'G');
                GraphicsAdapter.flushGraphics();
            }
        }, midletHook);
        engines[Engine.MENU] = new MenuEngine(adaptGen, midletHook);
        
        currEngine = Engine.GAME;
	}
	
	
	public void startYourEngines() {		
        if (canvasThread == null)
            canvasThread = new Thread(this);
        if (!canvasThread.isAlive())
            canvasThread.start();
	}

	
	public Engine getCurrentEngine() {
		return engines[currEngine];
	}
	
	
	public EngineSwitcher getEngineSwitcher() {
		return midletHook;
	}
	
	
	public boolean canExit() {
		return getCurrentEngine().canExit();
	}
        
        public void notifyOfError(Exception ex) {
            bufferedError = ex;
        }
	
	
	   /**
     * Game's main loop (thread). 
     * NOTE: I'm really not sure if I have the timing down. I *think* it makes sense
     *  to stall until 55ms have passed, then save the current time into a variable and 
     *  NOT update it until we reach this point again.... but I'm not positive.
     */
    public void run() {
        //Show them something while they wait.
    	MetaDisplay.init(adaptGen);
        MetaDisplay.drawInitialScreen(adaptGen.getScreenWidth(), adaptGen.getScreenHeight());
        GraphicsAdapter.flushGraphics();
        
        //Long startup...
        initEngines();
        long prevTime = System.currentTimeMillis();
        
        //Game loop
        try {
            for (boolean isInError=false;;) {
                if (!isInError) {
                    try {
                        //hmm...
                        if (bufferedError!=null)
                            throw bufferedError;
                        
                        engines[currEngine].paintScene();
                    } catch (Throwable ex) {
                    	MetaDisplay.drawError( 
                    			new LiteException(this, ex, "Error Drawing Canvas"),
                    			adaptGen.getScreenWidth(), adaptGen.getScreenHeight());
                        isInError = true;
                    }
                }
                if (!isInError)
                	GraphicsAdapter.flushGraphics();

                //Wait for the simulated DOS timer to expire; track key states while you do this.
                // TICK!
                int keyStates = 0;
                long currTime = 0;
                do {
                    //Certain activities (e.g., loading the games library) should not
                    //  be limited by the game's timer. We call these "background actions",
                    //  and update them as fast as possible.
                	if (!engines[currEngine].runIdle()) {
                		//However... if nothing happened on this runthrough, sleep for one ms.
                		Thread.sleep(1);
                	}
                    /*if (RunnerMidlet.backgroundAction != null) {
                        if(RunnerMidlet.backgroundAction.perform(this)) {
                            System.out.println("REMOVING: Background action.");
                            RunnerMidlet.backgroundAction = null;
                        }
                    }*/
                    
                    keyStates |= iAdapt.getKeyStates(); //Handle null-time key presses
                    currTime = System.currentTimeMillis();
                    
                    //Don't sleep; just run. Delays our actions for at most 55ms
                    //Thread.sleep(1)
                } while (currTime - prevTime < OHRRPG.GAME_TICK_MS);
                
                //Process all input.
                if (!isInError) {
                    try {
                        //hmm...
                        if (bufferedError!=null)
                            throw bufferedError;
                        
                        engines[currEngine].handleKeys(keyStates);
                    } catch (Throwable ex) {
                    	MetaDisplay.drawError( 
                    			new LiteException(this, ex, "Error on Key Input"), 
                    			adaptGen.getScreenWidth(), adaptGen.getScreenHeight());
                        isInError = true;
                    }
                }

                //Update the game's internal state based on how much time has passed.
                if (!isInError) {
                    try {
                        //hmm...
                        if (bufferedError!=null)
                            throw bufferedError;
                        
                        //System.out.println("TICK");
                        engines[currEngine].updateScene(currTime - prevTime);
                    } catch (Throwable ex) {
                    	MetaDisplay.drawError( 
                    			new LiteException(this, ex, "Error Updating Game State"), 
                    			adaptGen.getScreenWidth(), adaptGen.getScreenHeight());
                        isInError = true;
                    }
                }
                prevTime = currTime;

                //Allow the KVM to detect other button presses.
                try {
                    Thread.sleep(1);
                } catch (InterruptedException iex) {}
            }
        } catch (Exception ex) {
        	MetaDisplay.drawError( 
        			new LiteException(this, ex, "Uncaught Exception"), 
        			adaptGen.getScreenWidth(), adaptGen.getScreenHeight());
        } catch (Error er) {
        	MetaDisplay.drawError( 
        			new LiteException(this, er, "Big Problem"),
        			adaptGen.getScreenWidth(), adaptGen.getScreenHeight());
        }
    }

	
	/**
	 * Class to handle switching of Engines
	 * @author Seth N. Hetu
	 */
    class MidletHookback implements EngineSwitcher  {
        public void switchEngine(int NEW) {
             //Reset the engine to be called.
             callStack.push(new Integer(currEngine));
           //  System.out.println("Memory before: " + Runtime.getRuntime().freeMemory());
             engines[NEW].reset();
           //  System.out.println("Memory after: " + Runtime.getRuntime().freeMemory());
             currEngine = NEW;
        }
        public void egress(Object returnVal) {
            Object o = callStack.pop();
            try {
                currEngine = ((Integer)o).intValue();
                engines[currEngine].communicate(returnVal);
            } catch (NullPointerException nex) {
                throw new RuntimeException("Bad object pushed onto the call stack; expecting \"Integer\", not: <null>");
            } catch (ClassCastException cex) {
                throw new RuntimeException("Bad object pushed onto the call stack; expecting \"Integer\", not: " + o.getClass().toString());
            }
        }

        public Engine getCaller() {
            try {
                if (callStack.size()==0)
                    return null;
                return engines[((Integer)callStack.peek()).intValue()];
            } catch (ClassCastException ex) {
                throw new RuntimeException("CALLER is not an Integer; rather, it is a: " + callStack.peek().getClass().toString());
            }
        }
    }
	
}
