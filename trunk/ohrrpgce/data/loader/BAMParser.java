package ohrrpgce.data.loader;

import java.io.InputStream;

import ohrrpgce.data.RPG;

public class BAMParser extends LumpParser {
	private int songID = 0;
	
	public void setSongID(int id) {
		this.songID = id;
	}


	public long readLump(RPG result, InputStream input, long length, HookbackListener hookUpdater) {
		//Relate to songID
                return length;
	}

}
