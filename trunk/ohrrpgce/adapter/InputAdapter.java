package ohrrpgce.adapter;

/**
 * These classes exist to abstract all "non-compliant"
 *   operations. Basically, the J2ME libraries cannot always
 *   simply be linked to a J2EE project, so these classes present
 *   a facade to input, etc. 
 * @author Seth N. Hetu
 */

public interface InputAdapter {
	//These key states are global throughout the FMF.
	// They are based slightly off of the menu key states.
    public static final int KEY_UP = 1;
    public static final int KEY_DOWN = 2;
    public static final int KEY_LEFT = 4;
    public static final int KEY_RIGHT = 8;
    public static final int KEY_ACCEPT = 16;
    public static final int KEY_CANCEL = 32;
    
    public abstract int getKeyStates();
}
