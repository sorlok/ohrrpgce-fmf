package ohrrpgce.tool;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface OutputStreamCreator {

	public OutputStream getOutputStream(String lumpName);
	public File getOutputFile(String lumpName);
	public void closeOutputStream(OutputStream os) throws IOException;
	
}
