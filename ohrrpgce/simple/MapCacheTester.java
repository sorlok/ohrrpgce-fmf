package ohrrpgce.simple;

import java.io.File;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import ohrrpgce.data.*;
import ohrrpgce.data.loader.*;

public class MapCacheTester {
	//Fancy stuff
	private Shell mainShell;
	private TilesetView tView;
	
	//Normal stuff
	private Menu mainMenu;
	private Label tilesetsLbl;
	private Label mapLbl;
	private Label tsForMapLbl;
	private Label spWalkabout;
	private Combo tilesetsCmb;
	private Combo mapsCmb;
	private Combo walkaboutCombo;
	private Button viewTSBtn;
	private Button viewMapTSBtn;
	private Button viewWalkaboutBtn;
	private Text lastCache;
	private Text currCache;
	
	//Constants
	private static final int CACHE_SIZE = 4;
	
	//Model
	private RPG res = null;
	private String rpgFilePath;
	
	public MapCacheTester() {
		//Set up our SWT application
		Display disp = new Display();
		mainShell = new Shell(disp);
		mainShell.setText("Map Cache Tester");
		FormLayout fl = new FormLayout();
		fl.marginHeight = 5;
		fl.marginWidth = 5;
		mainShell.setLayout(fl);
		
		//Add the other components
		loadMenu();
		loadComponents();
		layoutComponents();
		
		//Main menu, finally. (This must be last to prevent errors)
		mainShell.setMenuBar(mainMenu);
		mainShell.pack();
		
		//For disposing
		mainShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				tView.setSprite(null);
				disposeAll();
			}
		});
		
		//For resizing
		mainShell.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				tView.handleResize();
			}
		});
		
		//Display functionality
		mainShell.open();
		tView.handleResize();
		 		
		//SWT basic loop
		while (!mainShell.isDisposed()) {
			if (!disp.readAndDispatch())  {
				disp.sleep();
			}
		}
		mainShell.dispose();
		disp.dispose();
	}
	
	private void loadMenu() {
		//Main menu bar
		mainMenu = new Menu(mainShell, SWT.BAR);
		
		//File
		MenuItem fileMI = new MenuItem(mainMenu, SWT.CASCADE);
		fileMI.setText("File");
		Menu fileMenu = new Menu(fileMI);
		fileMI.setMenu(fileMenu);
		//File->Load
		MenuItem openMI = new MenuItem(fileMenu, SWT.NONE);
		openMI.setText("Open...");
		openMI.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fd = new FileDialog(mainShell, SWT.SAVE); 
				fd.setText("Please select game");
				fd.setFilterExtensions(new String[] {"*.RPG"});
				String path = fd.open();
				if (path != null) {
					File f = new File(path);
					if (f.exists()) {
						loadRPG(f);
					}
				}
			}
		});
		//File->WH
		MenuItem openSMSMI = new MenuItem(fileMenu, SWT.NONE);
		openSMSMI.setText("Wandering Hamster");
		openSMSMI.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				loadRPG(new File("C:\\Program Files\\eclipse\\projects\\OHRRPGCE RPG Reader\\ohrrpgce\\games\\wander.rpg"));
			}
		});
	}
	
	private void loadComponents() {
		tilesetsLbl = new Label(mainShell, SWT.HORIZONTAL);
		tilesetsLbl.setText("Tileset: ");
		
		mapLbl = new Label(mainShell, SWT.HORIZONTAL);
		mapLbl.setText("Map: ");
		
		tsForMapLbl = new Label(mainShell, SWT.HORIZONTAL);
		tsForMapLbl.setText("Tileset:     ");
		
		spWalkabout = new Label(mainShell, SWT.HORIZONTAL);
		spWalkabout.setText("Walkabout: ");
		
		tilesetsCmb = new Combo(mainShell, SWT.DROP_DOWN|SWT.READ_ONLY);
		tilesetsCmb.setText("Select a tileset");
		tilesetsCmb.setEnabled(false);
		
		mapsCmb = new Combo(mainShell, SWT.DROP_DOWN|SWT.READ_ONLY);
		mapsCmb.setText("Select a map");
		mapsCmb.setEnabled(false);
		mapsCmb.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int index = mapsCmb.getSelectionIndex();
				if (index==-1)
					return;
				Tileset ts = res.getMap(index).getTileset(false);
				if (ts==null)
					tsForMapLbl.setText("Tileset: <error>");
				else
					tsForMapLbl.setText("Tileset: " + ts.id);
			}
		});
		
		walkaboutCombo =  new Combo(mainShell, SWT.DROP_DOWN|SWT.READ_ONLY);
		walkaboutCombo.setText("");
		walkaboutCombo.setEnabled(false);
		
		viewTSBtn = new Button(mainShell, SWT.PUSH);
		viewTSBtn.setText("View");
		viewTSBtn.setEnabled(false);
		viewTSBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int id = tilesetsCmb.getSelectionIndex();
				if (id==-1)
					return;
				
				//Hack!
				//System.out.println("Error! Caching disabled!");
				//res.loadTS(id);
				tView.setTileset(res.getTileset(id));
				tView.setMap(null);
				mapsCmb.select(-1);
				tView.recalc();
				lastCache.setText("Last Cache:"+currCache.getText().replaceFirst("Current Cache:", ""));
				currCache.setText("Current Cache:\n"+res.getTSStrings());
			}
		});
		
		viewMapTSBtn = new Button(mainShell, SWT.PUSH);
		viewMapTSBtn.setText("View");
		viewMapTSBtn.setEnabled(false);
		viewMapTSBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int id = mapsCmb.getSelectionIndex();
				if (id==-1)
					return;
				
				//A bit hackeneyed...
				tView.setTileset(res.getMap(id).getTileset());
				tView.setMap(res.getMap(id));
				tView.recalc();
				lastCache.setText("Last Cache:"+currCache.getText().replaceFirst("Current Cache:", ""));
				currCache.setText("Current Cache:\n"+res.getTSStrings());
			}
		});
		
		viewWalkaboutBtn = new Button(mainShell, SWT.PUSH);
		viewWalkaboutBtn.setText("View");
		viewWalkaboutBtn.setEnabled(false);
		viewWalkaboutBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int id = walkaboutCombo.getSelectionIndex();
				if (id==-1)
					return;
				
				tView.setSprite(res.getWalkabout(id));
				tView.recalc();
			}
		});
		
		StringBuilder sb =  new StringBuilder();
		for (int i=0; i<CACHE_SIZE; i++) {
			sb.append("\n------");
		}
		sb.append("\n[]");
		
		lastCache = new Text(mainShell, SWT.MULTI|SWT.BORDER|SWT.READ_ONLY);
		lastCache.setText("Last Cache:" + sb.toString());

		currCache = new Text(mainShell, SWT.MULTI|SWT.BORDER|SWT.READ_ONLY);
		currCache.setText("Current Cache:" + sb.toString());
		
		tView = new TilesetView(mainShell);
	}
	
	private void layoutComponents() {
		FormData fd = null;
		
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.left = new FormAttachment(0);
		tilesetsLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(tilesetsLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(tilesetsLbl);
		tilesetsCmb.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(tilesetsLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(tilesetsCmb);
		viewTSBtn.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(tilesetsCmb, 5);
		fd.left = new FormAttachment(tilesetsLbl, 0, SWT.LEFT);
		mapLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(mapLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(mapLbl);
		mapsCmb.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(mapLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(mapsCmb);
		tsForMapLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(mapLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(tsForMapLbl);
		viewMapTSBtn.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(mapsCmb, 5);
		fd.left = new FormAttachment(tilesetsLbl, 0, SWT.LEFT);
		spWalkabout.setLayoutData(fd);
		
		fd =  new FormData();
		fd.top = new FormAttachment(spWalkabout, 0, SWT.TOP);
		fd.left = new FormAttachment(spWalkabout);
		walkaboutCombo.setLayoutData(fd);
		
		fd =  new FormData();
		fd.top = new FormAttachment(spWalkabout, 0, SWT.TOP);
		fd.left = new FormAttachment(walkaboutCombo);
		viewWalkaboutBtn.setLayoutData(fd); 
		
		fd = new FormData();
		fd.left = new FormAttachment(0);
		fd.top = new FormAttachment(walkaboutCombo, 15);
		fd.bottom = new FormAttachment(currCache, -15, SWT.TOP);
		fd.right = new FormAttachment(100, -10);
		fd.width = TilesetParser.TILE_COLS*TilesetParser.TILE_SIZE;
		fd.height = TilesetParser.TILE_ROWS*TilesetParser.TILE_SIZE;
		tView.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(50, -2);
		fd.bottom =  new FormAttachment(100);
		lastCache.setLayoutData(fd);

		fd = new FormData();
		fd.left = new FormAttachment(lastCache, 5);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
		currCache.setLayoutData(fd);
	}
	
	private void disposeAll() {
		safeDispose(tView);
		safeDispose(tilesetsLbl);
		safeDispose(mapLbl);
		safeDispose(tsForMapLbl);
		safeDispose(tilesetsCmb);
		safeDispose(mapsCmb);
		safeDispose(viewTSBtn);
		safeDispose(viewMapTSBtn);
		safeDispose(lastCache);
		safeDispose(currCache);
		safeDispose(mainMenu);
	}
	
	private void safeDispose(Widget toDispose) {
		if (toDispose!=null && !toDispose.isDisposed())
			toDispose.dispose();
	}
	
	
	private void loadRPG(File rpgFile) {
	/*	this.rpgFilePath = rpgFile.getAbsolutePath();
		res = new RPG(new LumpLoader(new filead() {
			public ByteStreamReader getRPGFile() {
				try {
					return new ByteStreamReader(new BufferedInputStream(new FileInputStream(rpgFilePath)));
				} catch (FileNotFoundException ex) {
					System.out.println("File not found: " + rpgFilePath);
					return null;
				}
			}
			public ByteStreamReader getLump(String lumpName) {
				throw new RuntimeException("getLump() not defined for this adapter.");
			}
		}), null, CACHE_SIZE);
		
		//Reset data
		mapsCmb.removeAll();
		tilesetsCmb.removeAll();
		walkaboutCombo.removeAll();
		tView.setMap(null);
		tView.setTileset(null);
		tView.setSprite(null);
		tView.refigure();
		
		//Password protected?
		boolean passCheck = true;
		if (res.getPasscode().isProtected()) {
			passCheck = new PasswordBox(mainShell, res.getPasscode()).prompt();
			if (!passCheck) {
				MessageBox mb = new MessageBox(mainShell, SWT.OK|SWT.ICON_WARNING);
				mb.setText("Password Mismatch");
				mb.setMessage("The password you entered was not the one we were looking for...");
				mb.open();
			}
		}
		
		//Reload data
		if (passCheck) {
			for (int i=0; i<res.getNumMaps(); i++) {
				mapsCmb.add("[" + i + "] " + res.getMap(i).mapName);
			}
			for (int i=0; i<res.getNumTilesets(); i++) {
				tilesetsCmb.add("[" + i + "]");
			}
			for (int i=0; i<res.getNumWalkabouts(); i++) {
				walkaboutCombo.add("[" + i + "]");
			}
		}
			
		//Set enabled/disabled
		tilesetsCmb.setEnabled(passCheck);
		mapsCmb.setEnabled(passCheck);
		walkaboutCombo.setEnabled(passCheck);
		viewTSBtn.setEnabled(passCheck);
		viewMapTSBtn.setEnabled(passCheck);
		viewWalkaboutBtn.setEnabled(passCheck);*/
	}
	
	
	/**
	 * Simple inner class for displaying a tileset
	 * @author Seth N. Hetu
	 */
	class TilesetView extends Canvas {
		//Miscelania (sp)
		private boolean firstTime;
		
		//Cached
		private Image offscreen;
		private Font actualFont;
		private int tileSize = TilesetParser.TILE_SIZE;
		
		//For now
		private Tileset currTileset;
		private Map currMap;
		private Sprite currSprite;
		private Image[] spritePanels;
		private int currFrame;
		private Thread spriteAnimUpdater;
		
		public TilesetView(Shell parent) {
			super(parent, SWT.NONE);
			
			firstTime = true;
						
			this.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					repaint(e.x, e.y, e.width, e.height, e.gc);
				}
			});
				
			handleResize(new Rectangle(0, 0, 1, 1));
		}
		
		public void setTileset(Tileset ts) {
			this.currTileset = ts;
		}
		
		public void setMap(Map mp) {
			this.currMap = mp;
		}
		
		public void setSprite(Sprite sp) {
			currSprite = sp;
			if (sp==null)
				spritePanels = null;
			if (spriteAnimUpdater!=null)
				spriteAnimUpdater.interrupt();
		}
		
		public void recalc() {
			refigure();
			redraw();
		}
		
		private void handleResize(Rectangle r) {
			//Weird Linux bug...
			if (r.width==0 || r.height==0)
				return;
			
			//justUpdate = false;
			redraw();
		}
		
		public void handleResize() {
			if (!this.isDisposed())
				handleResize(getClientArea());
		}
		
		public void fullRepaint() {
			redraw();
		}
		
		private void refigure() {
			//Refigure the walkabout, if any
			if (currSprite!=null) {
				currFrame = 0;
				spritePanels = new Image[currSprite.spData.length];
				for (int i=0; i<spritePanels.length; i++) {
					Image res = new Image(Display.getCurrent(), PictureParser.PT_WALKABOUT_SIZES[0],  PictureParser.PT_WALKABOUT_SIZES[1]);
					GC currG = new GC(res);
					for (int y=0; y<PictureParser.PT_WALKABOUT_SIZES[1]; y++) {
						for (int x=0; x<PictureParser.PT_WALKABOUT_SIZES[0]; x++) {
							int val = currSprite.spData[i][y*PictureParser.PT_WALKABOUT_SIZES[0]+x];
							Color curr = new Color(Display.getCurrent(), (val&0xFF0000)/0x10000, (val&0xFF00)/0x100, val&0xFF); 
							currG.setBackground(curr);
							currG.fillRectangle(x, y, 1, 1);
						}
					}

					spritePanels[i] = res;
				}
				
				//Start a thread - not really necessary; we could always
				//   have one running instead of constantly interrupting. 
				//   But oh well.
				// FIX THIS LATER! It's a pain!
				spriteAnimUpdater = new Thread(new Runnable() {
					public void run() {
						for (;;) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException ex) {
								break;
							}
							
							//Update anim
							currFrame++;
							if (currFrame >= spritePanels.length)
								currFrame = 0;
							
							//Repaint
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									if (!isDisposed())
										redraw();
								}
							});
						}
					}
				}); 
				spriteAnimUpdater.start();
			}
			
			//Prepare our drawing area.
			int width = TilesetParser.TILE_COLS*tileSize;
			int height = TilesetParser.TILE_ROWS*tileSize;
			offscreen = new Image(Display.getCurrent(), new Rectangle(0, 0, width, height));
			GC g = new GC(offscreen);
			g.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			g.fillRectangle(0, 0, width, height);
			
			if (currTileset==null)
				return;
			
			//Fill the tiles.
			for (int row=0; row<TilesetParser.TILE_ROWS; row++) {
				for (int col=0; col<TilesetParser.TILE_COLS; col++) {
					//Drawing a tile is a pain (with SWT) but having int values is useful for J2ME, so we leave it.				
					int posX = col*tileSize;
					int posY = row*tileSize;
					for (int x=0; x<TilesetParser.TILE_SIZE*TilesetParser.TILE_SIZE; x++) {
						int val = currTileset.tsData.colorAt(row*TilesetParser.TILE_COLS+col, x);
						Color curr = new Color(Display.getCurrent(), (val&0xFF0000)/0x10000, (val&0xFF00)/0x100, val&0xFF); 
						g.setBackground(curr);
						g.fillRectangle(x%tileSize+posX , x/tileSize+posY, 1, 1);
					} 
				}
			}
			
			//Draw map or tiles, depending
			if (currMap!=null) {
				Image mapView = new Image(Display.getCurrent(), new Rectangle(0, 0, currMap.getWidth()*tileSize, currMap.getHeight()*tileSize));
				GC mG = new GC(mapView);
				mG.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
				mG.fillRectangle(0, 0, currMap.getWidth()*tileSize, currMap.getHeight()*tileSize);
				
				//Copy the tiles
				for (int y=0; y<currMap.getHeight(); y++) {
					for (int x=0; x<currMap.getWidth(); x++) {
						//For now, we skip special tiles
						if (currMap.tiles[y][x] > 159)
							continue;
						int offX = (currMap.tiles[y][x]%TilesetParser.TILE_COLS) * tileSize;
						int offY = (currMap.tiles[y][x]/TilesetParser.TILE_COLS) * tileSize;
						mG.drawImage(offscreen, offX, offY, tileSize, tileSize, x*tileSize, y*tileSize, tileSize, tileSize);
					}
				}
				
				//Swap in the map
				offscreen = mapView;
			} else {
				//Draw the gridlines
				g.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
				for (int row=0; row<TilesetParser.TILE_ROWS; row++) {
					for (int col=0; col<TilesetParser.TILE_COLS; col++) { 
						g.drawRectangle(col*tileSize, row*tileSize, tileSize, tileSize);
					}
				}
				
				//Final border crops a bit.
				g.drawRectangle(0, 0, width-1, height-1);
			}
		}
		
		private void repaint(int xPos, int yPos, int width, int height, GC gContext) {
			//Fill the background
			Rectangle bounds = getClientArea();
			gContext.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
			gContext.fillRectangle(bounds);
			
			//Redraw the cached image
			if (!firstTime && currTileset!=null) {
				gContext.drawImage(offscreen, 0, 0);
			}
			
			//Redraw the sprite, if any
			if (spritePanels != null) {
				int sX = bounds.width/2 - PictureParser.PT_WALKABOUT_SIZES[0]/2;
				int sY = bounds.height/2 - PictureParser.PT_WALKABOUT_SIZES[1]/2;
				gContext.drawImage(spritePanels[currFrame], sX, sY);
			}

			firstTime = false;
		}
	}
	
	
	//Mirrored from SWT's MessageBox to prompt the user for a password
	class PasswordBox extends Dialog {
		private Passcode check;
		private boolean result;
		
		//Anonymous workaround
		private Shell shell;
		private Text pass;
		
		public PasswordBox (Shell parent, Passcode checker) {
			super(parent, SWT.APPLICATION_MODAL);
			this.check = checker;
		}
		
		//Make the box visible
		public boolean prompt () {
			Shell parent = getParent();
			shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			shell.setText("Password Required");
			result = false;

			//Simple layout
			shell.setLayout(new GridLayout(2, true));
			
			//Prepare components
			Label instruct = new Label(shell, SWT.LEFT|SWT.HORIZONTAL);
			instruct.setText("This game is password protected. \n    Please enter password:");
			pass = new Text(shell, SWT.SINGLE|SWT.LEFT|SWT.PASSWORD|SWT.BORDER);
			Button ok = new Button(shell, SWT.PUSH);
			ok.setText("Ok");
			ok.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					result = check.checkPassword(pass.getText());
					shell.dispose();
				}
			});
			Button cancel = new Button(shell, SWT.PUSH);
			cancel.setText("Cancel");
			cancel.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					shell.dispose();
				}
			});
			
			//More control
			shell.setDefaultButton(ok);
			//How do I set the escape key? No matter....
			
			//Layout components
			instruct.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
			pass.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
			ok.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 1, 1));
			cancel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
			
			//Regular loop
			shell.pack();
			shell.open();
			Display display = parent.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) display.sleep();
			}
			return result;

		}
	}

		
	
	///////////////////////////////////////////////////////////////////
	// Main method.
	//////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		new MapCacheTester();
	}

}
