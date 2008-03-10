/*
 * TileAnimation.java
 * Created on January 12, 2007, 2:22 PM
 */

package ohrrpgce.data;

/**
 *
 * @author Seth N. Hetu
 */
public class TileAnimation {
    public static final int CMD_END_ANIM = 0;
    public static final int CMD_MOVE_UP = 1;
    public static final int CMD_MOVE_DOWN = 2;
    public static final int CMD_MOVE_RIGHT = 3;
    public static final int CMD_MOVE_LEFT = 4;
    public static final int CMD_PAUSE_ANIM = 5;
    public static final int CMD_CONDITIONAL = 6;
    
    public int startTileOffset;
    public int disableTagID;
    public boolean disableTagState;
    public int[] actionCommands;
    public int[] actionValues;
}
