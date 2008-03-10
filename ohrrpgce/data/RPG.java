package ohrrpgce.data;

import ohrrpgce.adapter.ImageAdapter;
import ohrrpgce.data.loader.LinkedList;
import ohrrpgce.data.loader.PaletteParser;
import ohrrpgce.data.loader.RPGLoadSizeListener;
import ohrrpgce.data.loader.RPGLoader;
import ohrrpgce.data.loader.TextBoxParser;
import ohrrpgce.game.LiteException;
import ohrrpgce.henceforth.VirtualMachine;


/**
 * General class for RPG data
 * @author Seth N. Hetu
 */
public class RPG {
	//Internal data
	protected Map[] maps;
	protected Tileset[] tilesets;
	protected Sprite[] walkabouts;
        protected Sprite[] battleSprites;
        protected Sprite[] smallEnemySprites;
        protected Sprite[] mediumEnemySprites;
        protected Sprite[] largeEnemySprites;
	protected Hero[] heroes;
        protected Enemy[] enemies;
        protected Spell[] attacks;
        protected Vehicle[] vehicles;
        protected EncounterSet[] enemyEncounters;
        protected BattleFormation[] enemyFormations;
	protected Passcode passcodeInfo;
	protected int[] masterPalette;
	protected int[][] sixteenColorPalettes;
	protected boolean[] tags = new boolean[1000]; //This constant needs to go...
	protected String[] tagNames;
	protected int[] startingPos;
	protected int startMapID;
	private int numPlotscripts = -1;
	
    public ImageAdapter font;
	protected Message[] textBoxes;
	
	
	//General bits
	public boolean permit2xTriggeredScripts;

	
	//Segmented loading fields
	protected RPGLoader loader;
	protected LinkedList tilesetCache;
	protected LinkedList walkaboutCache;
	protected LinkedList backdropCache;
	protected RPGLoadSizeListener listener;
	
        
	public RPG(RPGLoader loader, RPGLoadSizeListener listener, int cacheSize) {
		this.loader = loader;
                this.listener = listener;
		
		//No caching means all-caching.
		if (cacheSize==0)
			cacheSize = Integer.MAX_VALUE;
		
		//Init caches
		tilesetCache = new LinkedList(cacheSize);
		walkaboutCache = new LinkedList(8*cacheSize); //32 NPCs per map. Is dynamic caching really necessary at this point?
		backdropCache = new LinkedList(cacheSize);
		
		//Load
		if (loader!=null)
			loader.lightParse(this);
	}
   
        
        /**
         * Translates a color id (e.g., "1") to an int[] pair of the form {background, border}.
         *  Uses the OHR's crazy scheme of background = id*16+18, border = id*16+28
         */
        public int[] getTextBoxColors(int id) {
            return new int[]{masterPalette[id*16+18], masterPalette[id*16+28]};
        }
        
	public int getNumVehicles() {
		if (vehicles==null)
			return -1;
		return vehicles.length;
	}
	
	public Vehicle getVehicle(int id) {
	    return vehicles[id];
	}
        
	
	public void setNumVehicles(int num) {
		Vehicle[] res = new Vehicle[num];
		int count = 0;
		//Copy old vehicles
		if (vehicles != null) {
			for (; count<vehicles.length && count<num; count++) {
				res[count] = vehicles[count];
			}
		}
		
		//Init new vehicles
		for (; count<num; count++) {
			res[count] =  new Vehicle();
		}
		
		vehicles = res;
	}
	
	
	public void setNumPlotscripts(int num) {
		numPlotscripts = num;
	}
	
	public int getNumPlotscripts() {
		if (numPlotscripts==-1)
			throw new RuntimeException("Num plotscripts has not been initialized.");
		return numPlotscripts;
	}
	
	
	
        
	public int getNumEncounters() {
		if (enemyEncounters==null)
			return -1;
		return enemyEncounters.length;
	}
	
	public EncounterSet getEncounter(int id) {
	    return enemyEncounters[id];
	}
        
	
	public void setNumEncounters(int num) {
		EncounterSet[] res = new EncounterSet[num];
		int count = 0;
		//Copy old encounters
		if (enemyEncounters != null) {
			for (; count<enemyEncounters.length && count<num; count++) {
				res[count] = enemyEncounters[count];
			}
		}
		
		//Init new encounters
		for (; count<num; count++) {
			res[count] =  new EncounterSet();
		}
		
		enemyEncounters = res;
	}
        
        
	public int getNumFormations() {
		if (enemyFormations==null)
			return -1;
		return enemyFormations.length;
	}
	
	public BattleFormation getFormation(int id) {
	    return enemyFormations[id];
	}
        
	
	public void setNumFormations(int num) {
		BattleFormation[] res = new BattleFormation[num];
		int count = 0;
		//Copy old formations
		if (enemyFormations != null) {
			for (; count<enemyFormations.length && count<num; count++) {
				res[count] = enemyFormations[count];
			}
		}
		
		//Init new formations
		for (; count<num; count++) {
			res[count] =  new BattleFormation();
		}
		
		enemyFormations = res;
	}
        
        
	public int getNumAttacks() {
		if (attacks==null)
			return -1;
		return attacks.length;
	}
	
	public Spell getAttack(int id) {
	    return attacks[id];
	}
        
	
	public void setNumAttacks(int num) {
		Spell[] res = new Spell[num];
		int count = 0;
		//Copy old attacks
		if (attacks != null) {
			for (; count<attacks.length && count<num; count++) {
				res[count] = attacks[count];
			}
		}
		
		//Init new attacks
		for (; count<num; count++) {
			res[count] =  new Spell();
		}
		
		attacks = res;
	}
        
	
	public Passcode getPasscode() {
		return passcodeInfo;
	}
	
	public void setPasscode(int format) {
		setPasscode(format, -1, null);
	}
	
	public void setPasscode(int format, int rotator, byte[] encryptedPasscode) {
		Passcode res = new Passcode(format);
		if (format==Passcode.PASSCODE_THIRD_STYLE) {
			res.setRotator(rotator);
			res.setPasscode(encryptedPasscode);
		}
		this.passcodeInfo = res;
	}
	
        public void freeMap(int id) {
            maps[id] = null;
        }
        public void initMap(int id) {
            maps[id] = new Map(this);
        }
	public Map getMap(int id) {
		try {
            if (maps[id]==null) {
                //Inform listener (if any)
                if (listener!=null)
                    listener.readingUncachedData();
                loader.loadMap(this, id);
            }
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new LiteException(this, ex, "Error in getMap() - there is no map with id: " +id);
		}
	    return maps[id];
	}
	public int getNumMaps() {
		return maps.length;
	}
        
        public void setNumHeroes(int num) {
            heroes = new Hero[num];
            for (int i=0; i<heroes.length; i++) {
                heroes[i] = new Hero(this);
            }
        }
        
        public Hero getHero(int id) {
            return heroes[id];
        }

        
        public int getNumHeroes() {
            return heroes.length;
        }

        
        public void setNumEnemies(int num) {
            enemies = new Enemy[num];
            for (int i=0; i<enemies.length; i++) {
                enemies[i] = new Enemy(this);
            }
        }
        
        public Enemy getEnemy(int id) {
            return enemies[id];
        }

        
        public int getNumEnemies() {
            return enemies.length;
        }


        
        public void setSmallEnemySprite(int id, int[][] spData) {
		smallEnemySprites[id].spData = spData;
	}
        public void setMediumEnemySprite(int id, int[][] spData) {
		mediumEnemySprites[id].spData = spData;
	}
        public void setLargeEnemySprite(int id, int[][] spData) {
		largeEnemySprites[id].spData = spData;
	}
        
        public Sprite getSmallEnemySprite(int id) {
            if (id >= getNumSmallEnemySprites())
                return null;
            
            Sprite res = smallEnemySprites[id];
            if (res.spData==null) {
                //Inform listener (if any)
                if (listener!=null)
                    listener.readingUncachedData();
                
                //Load sprite data.
                loader.loadSmallEnemySprite(this, id);
            }
            return res;
        }
        
        public Sprite getMediumEnemySprite(int id) {
            if (id >= getNumMediumEnemySprites())
                return null;
            
            Sprite res = mediumEnemySprites[id];
            if (res.spData==null) {
                //Inform listener (if any)
                if (listener!=null)
                    listener.readingUncachedData();
                
                //Load sprite data.
                loader.loadMediumEnemySprite(this, id);
            }
            return res;
        }
        
        public Sprite getLargeEnemySprite(int id) {
            if (id >= getNumLargeEnemySprites())
                return null;
            
            Sprite res = largeEnemySprites[id];
            if (res.spData==null) {
                //Inform listener (if any)
                if (listener!=null)
                    listener.readingUncachedData();
                
                //Load sprite data.
                loader.loadLargeEnemySprite(this, id);
            }
            return res;
        }
        
        
        
        public int getNumSmallEnemySprites() {
            return smallEnemySprites.length;
        }
        public int getNumMediumEnemySprites() {
            return mediumEnemySprites.length;
        }
        public int getNumLargeEnemySprites() {
            return largeEnemySprites.length;
        }
        
        public void setNumSmallEnemySprites(int num) {
		Sprite[] res = new Sprite[num];
		int count = 0;
		//Copy old enemy sprites
		if (smallEnemySprites != null) {
			for (; count<smallEnemySprites.length && count<num; count++) {
				res[count] = smallEnemySprites[count];
			}
		}
		
		//Init new sprites
		for (; count<num; count++) {
			res[count] =  new Sprite(count);
		}
		
		smallEnemySprites = res;
        }
        public void setNumMediumEnemySprites(int num) {
		Sprite[] res = new Sprite[num];
		int count = 0;
		//Copy old enemy sprites
		if (mediumEnemySprites != null) {
			for (; count<mediumEnemySprites.length && count<num; count++) {
				res[count] = mediumEnemySprites[count];
			}
		}
		
		//Init new sprites
		for (; count<num; count++) {
			res[count] =  new Sprite(count);
		}
		
		mediumEnemySprites = res;
        }
        public void setNumLargeEnemySprites(int num) {
		Sprite[] res = new Sprite[num];
		int count = 0;
		//Copy old enemy sprites
		if (largeEnemySprites != null) {
			for (; count<largeEnemySprites.length && count<num; count++) {
				res[count] = largeEnemySprites[count];
			}
		}
		
		//Init new sprites
		for (; count<num; count++) {
			res[count] =  new Sprite(count);
		}
		
		largeEnemySprites = res;
        }        
        
	
	public void setWalkabout(int id, int[][] spData) {
		walkabouts[id].spData = spData;
	}

        public void setBattleSprite(int id, int[][] spData) {
		battleSprites[id].spData = spData;
	}
        
        public Sprite getBattleSprite(int id) {
            if (id >= getNumBattleSprites())
                return null;
            
            Sprite res = battleSprites[id];
            if (res.spData==null) {
                //Inform listener (if any)
                if (listener!=null)
                    listener.readingUncachedData();
                
                //Load sprite data.
                loader.loadBattleSprite(this, id);
            }
            return res;
        }
	
	public Sprite getWalkabout(int id) {
		if (id >= getNumWalkabouts())
			return null;
		
		Sprite res = walkabouts[id];		
		if (res.spData == null) {
                        //Inform listener (if any)
                        if (listener!=null)
                            listener.readingUncachedData();
                        
			//Load its sprite data
			loader.loadWalkabout(this, id);
			
			//Add it to the cache.
			Integer moved = (Integer)walkaboutCache.insertIntoFront(new Integer(id));
			if (moved!=null) {
				//Un-cache the sprite that was stored here before.
				walkabouts[moved.intValue()].spData = null;
			}
		} else {
			//Move it to the front of the cache.
                        //NOTE: Though only 99% effecient, DON'T attempt to change this (for now)
			walkaboutCache.removeItem(new Integer(id));
			walkaboutCache.insertIntoFront(new Integer(id));
		}
                
		return res;
	}
	
	public int getNumWalkabouts() {
		return walkabouts.length;
	}
	
/*	public void setTilesetData(int id, int[][] tsData) {
		tilesets[id].tsData = new TileData(tsData);
	}*/

	
	public Tileset getTileset(int id) {
		return getTileset(id, true);
	}
	
	public int getNumPalettes() {
		return sixteenColorPalettes.length;
	}
	
	public void setNumPalettes(int num) {
		sixteenColorPalettes = new int[num][PaletteParser.NUM_COLORS];
	}
	
	public void setPalette(int id, int[] links) {
		sixteenColorPalettes[id] = links;
	}
	
	/**
	 * Retrieves a color from one of the 16-color palettes
	 * @param palette The palette to use
	 * @param colorID The index of the color we want
	 * @return The RGB value from the master palette
	 */
	public int getIndexedColor(int palette, int colorID) {
            int alpha = 0xFF000000;
            if (colorID==0)
                alpha = 0x00000000; //Clear
            
            return alpha | getMasterPalette()[sixteenColorPalettes[palette][colorID]];
	}
	
	/**
	 * Retrieves the transparent color for a given 16-color palette
	 * @param palette The palette to use
	 * @return The transparent color (color at index 0)
	 */
	public int getTransparentColor(int palette) {
		return getMasterPalette()[sixteenColorPalettes[palette][0]];
	}
	
	//If loadIfNull is false, skip caching, etc.
	public Tileset getTileset(int id, boolean loadIfNull) {
		if (id >= getNumTilesets())
			return null;
		
		Tileset res = tilesets[id];
		if (!loadIfNull)
			return res;
		
		if (res.tsData == null) {
                        //Inform listener (if any)
                        if (listener!=null)
                            listener.readingUncachedData();
                    
			//Load its tileset data
			loader.loadTileset(this, id);
			
			//Add it to the cache.
			Integer moved = (Integer)tilesetCache.insertIntoFront(new Integer(id));
			if (moved!=null) {
				//Un-cache the tileset that was stored here before.
				tilesets[moved.intValue()].tsData = null;
			}
		} else {
			//Move it to the front of the cache.
			tilesetCache.removeItem(new Integer(id));
			tilesetCache.insertIntoFront(new Integer(id));
		}
		
		return res;
	}
	
	public int getNumTilesets() {
		return tilesets.length;
	}
	
	public int[] getMasterPalette() {
		return masterPalette;
	}
	
	public void setMasterPalette(int[] pal) {
		this.masterPalette = pal;
	}

        public void setNumTagNames(int num) {
		String[] res = new String[num];
		int count = 0;
		//Copy old flag names
		if (tagNames != null) {
			for (; count<tagNames.length && count<num; count++) {
				res[count] = tagNames[count];
			}
		}
		
		//Init new flag names
		for (; count<num; count++) {
			res[count] =  "";
		}
		
		tagNames = res;
        }
        
        //These two functions should really be in the OHRRPG class...
        public void setTag(int num, boolean setOn) {
            tags[num] = setOn;
        }
        public boolean tagOn(int num) {
            if (num<tags.length)
                return tags[num];
            else {
                System.out.println("Warning: Tag " + num + " is out of bounds.");
                return false;
            }
        }
	
	/**
	 * Resizes the map array, keeping old entries. Any new entries are initialized to blank maps.
	 * @param num The number of maps. So if your maps are numbered 0-17, you have 18 maps
	 */
	public void setNumMaps(int num) {
		Map[] res = new Map[num];
		int count = 0;
		//Copy old maps
		if (maps != null) {
			for (; count<maps.length && count<num; count++) {
				res[count] = maps[count];
			}
		}
		
		//Init new maps
		for (; count<num; count++) {
			res[count] =  null;
		}
		
		maps = res;
	}

	/**
	 * Resizes the tilesets array, keeping old entries. Any new entries are initialized to blank tilesets.
	 * @param num The number of tilesets. So if your tilesets are numbered 0-17, you have 18 tilesets
	 */
	public void setNumTilesets(int num) {
		Tileset[] res = new Tileset[num];
		int count = 0;
		//Copy old tilesets
		if (tilesets != null) {
			for (; count<tilesets.length && count<num; count++) {
				res[count] = tilesets[count];
			}
		}
		
		//Init new tilesets
		for (; count<num; count++) {
			res[count] =  new Tileset(count);
		}
		
		tilesets = res;
	}

        public void setStartingPosition(int x, int y) {
            startingPos = new int[] {x, y};
        }
        
        public int getStartX() {
            return startingPos[0];
        }

        public int getStartY() {
            return startingPos[1];
        }
        
        public void setStartingMap(int id) {
            this.startMapID = id;
        }
        
        public int getStartingMap() {
            return startMapID;
        }

        
	/**
	 * Resizes the walkabouts array, keeping old entries. Any new entries are initialized to blank sprites.
	 * @param num The number of sprites. So if your walkabouts are numbered 0-17, you have 18 sprites
	 */
	public void setNumWalkabouts(int num) {
		Sprite[] res = new Sprite[num];
		int count = 0;
		//Copy old walkabouts
		if (walkabouts != null) {
			for (; count<walkabouts.length && count<num; count++) {
				res[count] = walkabouts[count];
			}
		}
		
		//Init new sprites
		for (; count<num; count++) {
			res[count] =  new Sprite(count);
		}
		
		walkabouts = res;
	}
        
        public int getNumBattleSprites() {
            return battleSprites.length;
        }
        
        public void setNumBattleSprites(int num) {
		Sprite[] res = new Sprite[num];
		int count = 0;
		//Copy old battle sprites
		if (battleSprites != null) {
			for (; count<battleSprites.length && count<num; count++) {
				res[count] = battleSprites[count];
			}
		}
		
		//Init new sprites
		for (; count<num; count++) {
			res[count] =  new Sprite(count);
		}
		
		battleSprites = res;
        }
	
	/*class TSDataWrapper {
		public int id;
		public int[][] tsData;
		public TSDataWrapper(int id) {
			this.id = id;
		}
		public boolean equals(Object other) {
			return ((TSDataWrapper)other).id == this.id;
		}
	}*/
	
	//Display method (temp)
	public String getTSStrings() {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<tilesetCache.getMaxSize(); i++) {
			Integer n = (Integer)tilesetCache.getItemNumber(i);
			if (n==null) 
				sb.append("------\n");
			else
				sb.append("TS: "+n.intValue()+"\n");
		}
		
		sb.append("[");
		String sep = "";
                for (int i=0; i<tilesets.length; i++) {
                    Tileset ts = tilesets[i];
                    sb.append(sep + Character.toUpperCase(new Boolean(ts.tsData!=null).toString().charAt(0)));
                    sep = ", ";
		}
		sb.append("]");
		
		return sb.toString();
	}

        
        public int getTilesetCacheSlotsFree() {
            return tilesetCache.getMaxSize() - tilesetCache.getSize();
        }

        public int getWalkaboutCacheSlotsFree() {
            return walkaboutCache.getMaxSize() - walkaboutCache.getSize();
        }

        
        public void setTilesetData(int id, ImageAdapter tsData) {
    		tilesets[id].tsData = new TileData(tsData);
        }
        
    	public int getNumTextBoxes() {
    		if (textBoxes==null)
    			return -1;
    		return textBoxes.length;
    	}
    	
    	public Message getTextBox(int id) {
                if (textBoxes[id]==null) {
                    //Inform listener (if any)
                    if (listener!=null)
                        listener.readingUncachedData();
                    loader.loadTextBoxBlock(this, id/TextBoxParser.MAX_BOXES_PER_FILE);
                }
    	    return textBoxes[id];
    	}
            
            public void initTextBoxBlock(int blockID) {
                for (int id=blockID*TextBoxParser.MAX_BOXES_PER_FILE; id<(blockID+1)*TextBoxParser.MAX_BOXES_PER_FILE && id<getNumTextBoxes(); id++)
                    textBoxes[id] = new Message(this);
            }
            
    	
    	public void setNumTextBoxes(int num) {
    		Message[] res = new Message[num];
    		int count = 0;
    		//Copy old maps
    		if (textBoxes != null) {
    			for (; count<textBoxes.length && count<num; count++) {
    				res[count] = textBoxes[count];
    			}
    		}
    		
    		//Init new text boxes
    		for (; count<num; count++) {
    			res[count] =  null;
    		}
    		
    		textBoxes = res;
    	}
}
