package ohrrpgce.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * This class exists to help add to a zip additional files that vary individually 
 * in their level of compression. It is not robust, and not really related to 7Zip at all.
 * @author Seth N. Hetu
 */
public class SevenZipTask extends Task {
	private String command;
	private int level;
	private String includes;
	private String excludes;
	private String archiveName;
	private String nonRecursive;
	
	
	public void execute() throws BuildException {
		if (level<0 || level>9)
			throw new BuildException("\"level\" must be between 0 and 9, not " + level);
		if (!command.equals("a") && !command.equals("add"))
			throw new BuildException("\"command\" only supports a[dd], not " + command);
		
		if (includes==null)
			includes = "";
		else
			includes = includes.replaceAll("[ ]+", " -ir!");
		if (nonRecursive==null)
			nonRecursive = "";
		else
			nonRecursive = nonRecursive.replaceAll("[ ]+", " ");
		if (excludes==null)
			excludes = "";
		else
			excludes = excludes.replaceAll("[ ]+", " -xr!");
		
		String cmd = "\"C:\\Program Files\\7Zip\\7za.exe\" " + command.charAt(0) + " " + archiveName + nonRecursive + includes  + excludes + " -mx" + level;
		log(cmd, Project.MSG_INFO);
		
		Runtime rt = Runtime.getRuntime ();
		try  {
			Process process = rt.exec(cmd);
			BufferedReader lineReader = new BufferedReader(new InputStreamReader( process.getInputStream ()));
			String line = null;
			String lastLine = null;

			int lineNum = 0;
			int compressCount = 0;
			while (((line = lineReader.readLine ()) != null)) {
				if (lineNum==0)
					log(line, Project.MSG_INFO);
				else if (line.startsWith("Compressing"))
					compressCount++;
				else
					lastLine = line;
				log(line, Project.MSG_VERBOSE);
				lineNum++;
			}
			
			if (!lastLine.toLowerCase().equals("Everything is Ok".toLowerCase()))
				throw new BuildException("Bad 7-zip message: " + lastLine );
			
			log("Compressed " + compressCount + " files.", Project.MSG_INFO);
		} catch (IOException ex)  {
			throw new BuildException(ex);
		}
	}
	
	
	public void setCommand(String cmd) {
		this.command = cmd;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public void setNonRecursiveIncludes(String nri) {
		this.nonRecursive = " " +nri.trim();
	}
	
	public void setIncludes(String includes) {
		this.includes = " " +includes.trim();
	}
	
	public void setExcludes(String excludes) {
		this.excludes = " " + excludes.trim();
	}
	
	public void setArchivename(String aName) {
		this.archiveName = aName;
	}

}
