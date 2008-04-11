package ohrrpgce.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ohrrpgce.game.LiteException;

/**
 * Convenience class for buffering input.
 * @author Seth N. Hetu
 */
public class BufferedInputStreamReader {
	private static final int BUFFER_SIZE = 1024;
	
	private InputStreamReader inFile;
	private char[] readBuffer = new char[BUFFER_SIZE];
	private int readBufferSize;
	private int readBufferPos;
	private boolean done;
	
	public BufferedInputStreamReader(InputStream inStream) {
		this.inFile = new InputStreamReader(inStream);
	}
	
	public void close() throws IOException {
		inFile.close();
	}
	
	public boolean isDone() {
		return done;
	}
	
	public char readChar() throws IOException {
		if (isDone())
			throw new LiteException(this, new IllegalArgumentException(), "Cannot read file: at EOF");
		else if (readBufferPos==readBufferSize) {
			//Reload buffer
			readBufferSize = inFile.read(readBuffer);
			readBufferPos = 0;
			if (readBufferSize==-1)
				done = true;
		}
		
		//Now, read this...
		char c = '\0';
		if (!isDone()) {
			c = readBuffer[readBufferPos++];
			
			if (readBufferSize<BUFFER_SIZE && readBufferPos==readBufferSize)
				done = true;
		}
		
		return c;
	}
}
