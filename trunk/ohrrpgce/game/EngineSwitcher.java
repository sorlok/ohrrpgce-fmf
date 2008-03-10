/*
 * EngineSwitcher.java
 */

package ohrrpgce.game;

/**
 * Access to top-level commands which allow switching in (game) engines.
 * @author Seth N. Hetu
 */
public interface EngineSwitcher {
    /**
     * Switch to an engine with ID found in Engine.(NEW)
     * Note that switching to an engine automatically restarts the (new) engine.
     */
    public abstract void switchEngine(int NEW);
    
    /**
     * Called whenever an Engine wants to exit with a certain return value
     * @param returnVal An implementation-specific lump of information for the engine which called this engine.
     */
    public abstract void egress(Object returnVal);
    
    
    /**
     * Used by the current engine to get whichever engine called it.
     */
    public abstract Engine getCaller();
}
