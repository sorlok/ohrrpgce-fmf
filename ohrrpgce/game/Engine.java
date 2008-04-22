/*
 * Engine.java
 * Created on April 10, 2007, 8:04 PM
 */

package ohrrpgce.game;

/**
 * A basic game engine, with functionality for interacting with other
 *  engines. Current & planned engines include:
 *  GAME
 *  MENU
 *  BATTLE
 * @author Seth N. Hetu
 */
public abstract class Engine {
     public static final int GAME = 0;
     public static final int MENU = 1;
    
     public abstract void paintScene();
     public abstract void handleKeys(int keyStates);
     
    /**
     * Update the game state. Updates are guarenteed to occur once per tick.
     * @elapsed The actual number of ms elapsed in this tick, (possibly) for smoother updates.
     */
     public abstract void updateScene(long elapsed);
     
     
     /**
      * Called whenever an Engine that was called from this Engine returns.
      * @param stackFrame The information about which engine returned, and what results it returned with.
      */
     public abstract void communicate(Object stackFrame);
     
     
     /**
      * Called whenever we want this Engine to reset all information it's built up.
      */ 
     public abstract void reset();
     
     /**
      * Run when the engine has time free, but one DOS timer tick has not elapsed
      * @return TRUE if any action was performed, FALSE if nothing happened.
      */
     public abstract boolean runIdle();
     
     /**
      * Process the "EXIT" button being pressed; return true if you can exit.
      */
     public abstract boolean canExit();
}
