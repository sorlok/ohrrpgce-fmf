/*
 * RPGLoadSizeListener.java
 *
 * Created on January 9, 2007, 7:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ohrrpgce.data.loader;

/**
 *
 * @author sethhetu
 */
public interface RPGLoadSizeListener {
    public abstract void moreBytesRead(long bytesRead, String currLumpName);
    public abstract void readingUncachedData();
    public abstract void callingGC();
}
