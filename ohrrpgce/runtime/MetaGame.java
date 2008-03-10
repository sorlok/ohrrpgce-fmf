/*
 * MetaGame.java
 * Created on January 8, 2007, 11:56 AM
 */

package ohrrpgce.runtime;

import ohrrpgce.adapter.ImageAdapter;

/**
 * Meta information about a game
 * @author Seth N. Hetu
 */
public class MetaGame {
    public String name;
    public String fullName;
    public ImageAdapter icon;
    public int numBytes;
    public int mobileFormat; //O=just RPG, 1=zipped pngs, etc.
    public ImageAdapter errorIcon;
}
