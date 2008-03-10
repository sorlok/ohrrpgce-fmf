/*
 * HookbackListener.java
 *
 * Created on January 19, 2007, 10:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ohrrpgce.data.loader;

/**
 * Used for informing the status bar of small incremental updates.
 * @author sethhetu
 */
public class HookbackListener {
    private long numSegments;
    private long numBytes;
    private long startPos;
    private RPGLoader updater;
    private String lumpName;
    
    public HookbackListener(long bytes, long startPos, RPGLoader listener, String currLumpName) {
        numBytes = bytes;
        this.startPos = startPos;
        updater = listener;
        lumpName = currLumpName;
        numSegments = 1; //Avoid divide-by-zero errors if the user is stupid
    }
    
    public void reset(long segments) {
        numSegments = segments;
    }
    
    public void segmentDone(int id) {
        id++; //If we're done with zero, we're starting with 1
        String progress = lumpName+ " (" + id + "/" + numSegments +")";
        if (id==numSegments)
            updater.updateSizeListeners(startPos+numBytes, progress);
        else
            updater.updateSizeListeners(startPos+(id*(numBytes/numSegments)), progress);            
    }
    
}
