package ohrrpgce.tool;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.util.regex.*;

import ohrrpgce.tool.strings.LanguagePack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class PhoneLoader {
	//Top-level control
	private Shell mainShell;
	
	//Controls
	private Menu mainMenu;
	private MenuItem langMI;
	
	//Library
	private Label libraryLbl;
	private Tree activeLibrary;
	private Button addFileBtn;
	private Button addFolderBtn;
	//private Button deleteBtn;
	private Label noGamesLbl;
	
	//Mobile
	private Label mobileLbl;
	private Table currentGames;
	//private Button updateBtn;
	private Button pathBtn;
	private Button addJarBtn;
	private Label noJarLbl;
	
	//Data
	private LanguagePack currLang;
	private ArrayList/*<String>*/ libGames;
	private ArrayList/*<String>*/ libFolders;
	//private GameStatus lastUpdatedGame;
	private GameStatus currGame;
	
	//Images
	private Image iconRED;
	private Image iconBLUE;
	private Image iconGREEN;
	
	private String newRPGName;
	private ZipOutputStream tempJar;
	private File lastImageFile;
	private String lastImageLumpName;
	private File tempDir;
	
	//Patterns
	Pattern midletVersion = Pattern.compile("MIDlet-Version: ([0-9]+\\.[0-9]+)");
	Pattern gameDescLine = Pattern.compile("([a-zA-Z0-9_.]+)\\t([a-zA-Z0-9_. ]+)\\t([a-zA-Z0-9_. ]+)\\t([0-9]+)\\t([0-9]+)");
	//Pattern nameExtractor = Pattern.compile(".*\\\\([^.]+)");
	
	public PhoneLoader() {
		//Set up our SWT application
		Display disp = new Display();
		mainShell = new Shell(disp);
		mainShell.setText("XXXXXXXXXXX");
		FormLayout fl = new FormLayout();
		fl.marginHeight = 6;
		fl.marginWidth = 6;
		mainShell.setLayout(fl);
		
		//Load Images
		iconRED = new Image(Display.getDefault(), "ohrrpgce/tool/res/RED.PNG");
		iconBLUE = new Image(Display.getDefault(), "ohrrpgce/tool/res/BLUE.PNG");
		iconGREEN = new Image(Display.getDefault(), "ohrrpgce/tool/res/GREEN.PNG");
		
		//Also
		libGames = new ArrayList/*<String>*/();
		libFolders = new ArrayList/*<String>*/();
	//	lastUpdatedGame = new GameStatus();
		currGame = new GameStatus();
		
		//Add the other components
		loadMenu();
		loadComponents();
		layoutComponents();
		changeLanguage(LanguagePack.getDefault());
		
		//Main menu, finally. (This must be last to prevent errors)
		mainShell.setMenuBar(mainMenu);
		//mainShell.pack();
		mainShell.setSize(435, 325); //I like this size.
		
		//For disposing
		mainShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeAll();
			}
		});
		
		//For resizing
		mainShell.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				//tView.handleResize();
			}
		});
		
		//Display functionality
		mainShell.open();
		//tView.handleResize();
		 		
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
		
		//Language
		langMI = new MenuItem(mainMenu, SWT.CASCADE);
		langMI.setText("XXXXXXXXXX");
		Menu languageMenu = new Menu(langMI);
		langMI.setMenu(languageMenu);
		Listener langList = new Listener() {
			public void handleEvent(Event event) {
				changeLanguage((LanguagePack)((MenuItem)event.widget).getData());
			}
		};
		//Language->(All)
		//for (LanguagePack pack : LanguagePack.getAllLanguagePacks()) {
		for (int pI=0; pI<LanguagePack.getAllLanguagePacks().length; pI++) {
			LanguagePack pack = LanguagePack.getAllLanguagePacks()[pI];
			
			MenuItem currLang = new MenuItem(languageMenu, SWT.NONE);
			currLang.setText(pack.getLanguageName());
			if (pack.getLanguagePicture()!=null)
				currLang.setImage(pack.getLanguagePicture());
			currLang.setData(pack);
			currLang.addListener(SWT.Selection, langList);
		}
	}
	
	private void loadComponents() {
		libraryLbl = new Label(mainShell, SWT.HORIZONTAL|SWT.LEFT);
		libraryLbl.setText("XXXXXXXXXXXXXXXX");
		
		activeLibrary = new Tree(mainShell, SWT.MULTI);
		activeLibrary.setVisible(false);
		activeLibrary.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == SWT.KEYPAD_CR)
					addGameToJar();
			}
		});
		activeLibrary.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				addGameToJar();
			}
		});
		
		addFileBtn = new Button(mainShell, SWT.PUSH);
		addFileBtn.setText("XXXXXX");
		addFileBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fd = new FileDialog(mainShell, SWT.OPEN|SWT.MULTI);
				fd.setText(currLang.getChooseFileText());
				fd.setFilterExtensions(new String[] {"*.RPG"});
				fd.setFilterNames(new String[] {"OHRRPGCE RPG files (.RPG)"});
				String dirPath = fd.open();
				if (dirPath == null)
					return;
				dirPath = dirPath.substring(0, dirPath.lastIndexOf('\\')+1);
				
				//for (String name : fd.getFileNames())
				for (int nI=0; nI<fd.getFileNames().length; nI++)
					libGames.add(dirPath+fd.getFileNames()[nI]);
				
				reloadTree();
			}
		});
		
		addFolderBtn = new Button(mainShell, SWT.PUSH);
		addFolderBtn.setText("XXXXXX");
		addFolderBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog fd = new DirectoryDialog(mainShell, 0);
				fd.setText(currLang.getChooseFolderText());
				String dir = fd.open();
				if (dir == null)
					return;
				
				libFolders.add(dir);
				
				reloadTree();
			}
		});
		
		addJarBtn = new Button(mainShell, SWT.PUSH);
		addJarBtn.setText("Add JAR");
		addJarBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fd = new FileDialog(mainShell, SWT.OPEN);
				fd.setText(currLang.getChooseFileText());
				fd.setFilterExtensions(new String[] {"GAME_FMF.JAR"});
				fd.setFilterNames(new String[] {"Previously Compiled Game Files"});
				String dirPath = fd.open();
				if (dirPath == null)
					return;
				
				libFolders.add(dirPath);
				
				reloadTree();
			}
		});
		
		/*deleteBtn = new Button(mainShell, SWT.PUSH);
		deleteBtn.setText("XXXXXX");
		deleteBtn.setEnabled(false);*/
		
		noGamesLbl = new Label(mainShell, SWT.SHADOW_IN|SWT.WRAP);
		noGamesLbl.setBackground(new Color(Display.getCurrent(), 0xDD, 0xDD, 0xDD));
		noGamesLbl.setAlignment(SWT.CENTER);
		noGamesLbl.setText("XXXXXXXXXXXXXXXXXXXXXXXX");
		
		noJarLbl = new Label(mainShell, SWT.SHADOW_IN|SWT.WRAP);
		noJarLbl.setBackground(new Color(Display.getCurrent(), 0xDD, 0xDD, 0xDD));
		noJarLbl.setAlignment(SWT.CENTER);
		noJarLbl.setText("XXXXXXXXXXXXXXXXXXXXXXXX");
		
		mobileLbl = new Label(mainShell, SWT.LEFT);
		mobileLbl.setText("XXXXXXXXXXXXXXXXXXXXXXX");
		
		/*updateBtn = new Button(mainShell, SWT.PUSH);
		updateBtn.setText("XXXXXXXXXXXXXXXX");
		updateBtn.setEnabled(false);*/
		
		pathBtn = new Button(mainShell, SWT.PUSH);
		pathBtn.setText("XXXXXXXXXXXX");
		pathBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				PathPrompt prompt = new PathPrompt(mainShell, currLang, "", "", false);
				currGame = new GameStatus();
				if (!prompt.prompt())
					return;
				
				//Now, update the path to the jar
				currGame.jarPath = prompt.getPathToJar();
				if (prompt.isUsingOutputPath()) {
					currGame.outputFile = prompt.getOutputPath();
					
					//Create a copy of the JAR/JAD files there...
					// This code is shoddy.
					try {
						BufferedInputStream in = new BufferedInputStream(new FileInputStream(currGame.jarPath));
						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(currGame.outputFile + "\\GAME_FMF.JAR"));
						byte[] lin = new byte[1024];
						int len = 0;
						for(;;) {
							len = in.read(lin);
							if (len <= 0)
								break;
							
							out.write(lin, 0, len);
						}
						in.close();
						out.close();

						out = new BufferedOutputStream(new FileOutputStream(currGame.outputFile + "\\GAME_FMF.JAD"));
						if (Character.isLowerCase(currGame.jarPath.charAt(currGame.jarPath.length()-1)))
							in = new BufferedInputStream(new FileInputStream(currGame.jarPath.substring(0, currGame.jarPath.length()-1)+"d"));
						else
							in = new BufferedInputStream(new FileInputStream(currGame.jarPath.substring(0, currGame.jarPath.length()-1)+"D"));
						for(;;) {
							len = in.read(lin);
							if (len <= 0)
								break;
							
							out.write(lin, 0, len);
						}
						in.close();
						out.close();
						
						currGame.outputFile = currGame.outputFile + "\\GAME_FMF.JAR";
					} catch (Exception ex) {
						System.out.println("Error copying JAR/JAD files to output directory: " + ex.toString());
					}
				} else {
					currGame.outputFile = prompt.getPathToJar();
				}
				
				//And update the games list, version, etc.
				JarFile jFile = null;
				try {
					jFile =  new JarFile(currGame.jarPath);
				} catch (IOException ex) {
					System.out.println("The jar file can't be opened: " + ex.toString());
					return;
				}
				try {
					//Version
					//System.out.println("1");
					BufferedReader br = new BufferedReader(new InputStreamReader(jFile.getInputStream(jFile.getEntry("META-INF/MANIFEST.MF"))));
		            String line;
		            while ((line = br.readLine()) != null) {
		            	Matcher m = midletVersion.matcher(line);
		            	if (m.matches())
		            		currGame.jarVersion = Float.parseFloat(m.group(1));
		            }
		            br.close();
		          //  System.out.println("2");
		            //Game's list
		            br = new BufferedReader(new InputStreamReader(jFile.getInputStream(jFile.getEntry("ohrrpgce/games/game_list.txt"))));
		            while ((line = br.readLine()) != null) {
		            	Matcher m = gameDescLine.matcher(line);
		            	if (m.matches()) {	            		
		            	//	System.out.println("3:" + line);
		            		GameInfo game = new GameInfo();
		            		game.name = m.group(1);
		            		game.fullName = m.group(2);
		            		game.pathToIconFile = m.group(3);
		            		game.numBytes = Integer.parseInt(m.group(4));
		            		game.mobileFormat = Integer.parseInt(m.group(5));
		            		
		            		//Before we go any further...
		            		if (game.mobileFormat==0) {
		            			if (jFile.getEntry("ohrrpgce/games/"+game.name) == null)
		            				continue;
		            		} else if (game.mobileFormat==1) {
		            			if(jFile.getEntry("ohrrpgce/games/"+game.name+"/"+game.name+".GEN") == null)
		            				continue;
		            		} else {
		            			System.out.println("Error! Format not supported: " + game.mobileFormat);
		            			continue;
		            		}
		            		
		            		currGame.gamePaths.add(game);
		            	}
		            }
		            br.close();
				} catch (Exception ex) {
					System.out.println("Problem reading games in current JAR...: " + ex.toString());
				}
				
				reloadTable(false);
				
			}
		});
		
		currentGames = new Table(mainShell, SWT.FULL_SELECTION);
		TableColumn tc = null;
		tc = new TableColumn(currentGames, SWT.LEFT);
		tc.setText("Game");
		tc.setWidth(150);
		tc = new TableColumn(currentGames, SWT.LEFT);
		tc.setText("Icon");
		tc.setWidth(40);
		currentGames.setHeaderVisible(false);
		currentGames.setLinesVisible(false);
		currentGames.setVisible(false);
	}
	
	private void layoutComponents() {
		FormData fd = null;
		
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.left = new FormAttachment(0);
		libraryLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(noGamesLbl, 0, SWT.LEFT);
		fd.right = new FormAttachment(noGamesLbl, 0, SWT.RIGHT);
		fd.top = new FormAttachment(noGamesLbl, 0, SWT.TOP);
		fd.bottom = new FormAttachment(noGamesLbl, 0, SWT.BOTTOM);
		activeLibrary.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(libraryLbl, 6);
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(50, -3);
		fd.bottom = new FormAttachment(addFileBtn, 0, SWT.TOP);
		noGamesLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(mobileLbl, 6);
		fd.left = new FormAttachment(mobileLbl, 0, SWT.LEFT);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(pathBtn, 0, SWT.TOP);
		noJarLbl.setLayoutData(fd);
				
		fd = new FormData();
		fd.left = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		addFileBtn.setLayoutData(fd);

		fd = new FormData();
		fd.left = new FormAttachment(addFileBtn, 6);
		fd.bottom = new FormAttachment(100);
		addFolderBtn.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(addFolderBtn, 6);
		fd.bottom = new FormAttachment(100);
		addJarBtn.setLayoutData(fd);
		
		/*fd = new FormData();
		fd.left = new FormAttachment(addJarBtn, 6);
		fd.bottom = new FormAttachment(100);
		deleteBtn.setLayoutData(fd);*/
		
		fd = new FormData();
		fd.left = new FormAttachment(noGamesLbl, 3);
		fd.top = new FormAttachment(libraryLbl, 0, SWT.TOP);
		mobileLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(mobileLbl, 0, SWT.LEFT);
		fd.bottom = new FormAttachment(100);
		pathBtn.setLayoutData(fd);
		
		/*
		fd = new FormData();
		fd.left = new FormAttachment(updateBtn, 6);
		fd.bottom = new FormAttachment(updateBtn, 0, SWT.BOTTOM);
		pathBtn.setLayoutData(fd);*/
		
		fd = new FormData();
		fd.left = new FormAttachment(noJarLbl, 0, SWT.LEFT);
		fd.right = new FormAttachment(noJarLbl, 0, SWT.RIGHT);
		fd.top = new FormAttachment(noJarLbl, 0, SWT.TOP);
		fd.bottom = new FormAttachment(noJarLbl, 0, SWT.BOTTOM);
		currentGames.setLayoutData(fd);
	}
	
	private void disposeAll() {
		safeDispose(mainMenu);
	}
	
	private void safeDispose(Widget toDispose) {
		if (toDispose!=null && !toDispose.isDisposed())
			toDispose.dispose();
	}
	
	private void lockButtons(boolean lock) {
		addFileBtn.setEnabled(!lock);
		addFolderBtn.setEnabled(!lock);
		activeLibrary.setEnabled(!lock);
		addJarBtn.setEnabled(!lock);
		pathBtn.setEnabled(!lock);
		currentGames.setEnabled(!lock);
	}
	
	private void reloadUngroupedGames() {
		if (libGames.size()==0)
			return;
		
		//Is this the first "Loose" item added?
		if (activeLibrary.getItemCount()==0 || activeLibrary.getItem(0).getText().charAt(0) != '(') {
			TreeItem ti = new TreeItem(activeLibrary, 0, 0);
			ti.setText("(" + currLang.getDefaultGroupName() + ")");
		}
		
		OUTER:
		//for (String path : libGames) {
		for (int pI=0; pI<libGames.size(); pI++) {
			String path = (String)libGames.get(pI);
			
			int splitOn = path.lastIndexOf('\\');
			String name = path.substring(splitOn+1, path.length());	
			
			//Is it already in there?
			//for (TreeItem ti : activeLibrary.getItem(0).getItems()) {
			for (int tID=0; tID<activeLibrary.getItem(0).getItems().length; tID++) {
				TreeItem ti = activeLibrary.getItem(0).getItems()[tID];
				
				if (ti.getData().equals(path))
					continue OUTER;
			}
			
			//Add it
			TreeItem newItem = new TreeItem(activeLibrary.getItem(0), 0);
			newItem.setData(path);
			newItem.setText(name);
		}	
	}
	
	private void reloadGroupedGames() {
		OUTER:
		//for (String path : libFolders) {
		for (int pI=0; pI<libFolders.size(); pI++) {
			String path = (String)libFolders.get(pI);
			
			//Does it exist already?
			//for (TreeItem ti : activeLibrary.getItems()) {
			for (int tID=0; tID<activeLibrary.getItems().length; tID++) {
				TreeItem ti = activeLibrary.getItems()[tID];
				
				if (ti.getData()==null)
					continue; //Default
				if (ti.getData().equals(path))
					continue OUTER;
			}
			
			//Does it even exist?
			File f = new File(path);
			if (!f.exists())
				continue;
			String[] filePaths = null;
			String[] rpgNames = null;
			if (path.toUpperCase().endsWith(".JAR")) {
				try {
					JarFile jFile = new JarFile(f);
					String line = "";
					BufferedReader br = new BufferedReader(new InputStreamReader(jFile.getInputStream(jFile.getEntry("ohrrpgce/games/game_list.txt"))));
					GameStatus tempGames = new GameStatus();
					while ((line = br.readLine()) != null) {
						Matcher m = gameDescLine.matcher(line);
						if (m.matches()) {
							GameInfo game = new GameInfo();
							game.name = m.group(1);
							game.fullName = m.group(2);
							game.pathToIconFile = m.group(3);
							game.numBytes = Integer.parseInt(m.group(4));
							game.mobileFormat = Integer.parseInt(m.group(5));
	            		
							tempGames.gamePaths.add(game);
		            	}
		            }
					br.close();
	            
	            	filePaths = new String[tempGames.gamePaths.size()];
	            	rpgNames = new String[tempGames.gamePaths.size()];
	            	for (int i=0; i<filePaths.length; i++) {
	            		filePaths[i] = path + "::" + ((GameInfo)tempGames.gamePaths.get(i)).name;
	            		rpgNames[i] = ((GameInfo)tempGames.gamePaths.get(i)).name;
	            	}
				} catch (Exception ex) {
					System.out.println("Uh-oh! Badly formatted jar!");
				}
			} else {
				File[] rpgs = f.listFiles(new FileFilter() {
					public boolean accept(File pathname) { 
						return pathname.getName().toUpperCase().endsWith(".RPG");
					}
				});
				filePaths = new String[rpgs.length];
				rpgNames = new String[rpgs.length];
				for (int i=0; i<filePaths.length; i++) {
					filePaths[i] = rpgs[i].getAbsolutePath();
					rpgNames[i] = rpgs[i].getName();
				}
				
			}
			
			//It doesn't exist; add it
			TreeItem ti = new TreeItem(activeLibrary, 0);
			ti.setData(path);
			if (path.lastIndexOf('\\')>5)
				ti.setText(path.substring(0, 5) + "..." + path.substring(path.lastIndexOf('\\'), path.length()));
			else
				ti.setText(path);
			
			//Add all children
			for (int i=0; i<filePaths.length; i++) {
				TreeItem child =  new TreeItem(ti, 0);
				child.setData(filePaths[i]);
				child.setText(rpgNames[i]);
			}
			
		}
		
	}
	
	private void reloadTree() {
		reloadUngroupedGames();
		reloadGroupedGames();
		
		noGamesLbl.setVisible(false);
		activeLibrary.setVisible(true);
	}
	
	private void reloadTable(boolean highlightLastEntry) {
		currentGames.removeAll();
		
		//Add the "JAR" entry itself
		TableItem ti =  new TableItem(currentGames, 0);
		ti.setText(0, "GAME_FMF.JAR");
		
		//Add the default game - give them no control
	/*	ti =  new TableItem(currentGames, 0);
		ti.setText(0, "  (Default Game)");*/
		
		int id=0;
		//for (GameInfo gI : currGame.gamePaths) {
		for (int gID=0; gID<currGame.gamePaths.size(); gID++) {
			GameInfo gI = (GameInfo)currGame.gamePaths.get(gID);
			
			ti =  new TableItem(currentGames, 0);
			ti.setText(0, "  " + gI.fullName);
			if (highlightLastEntry && id==currGame.gamePaths.size()-1)
				ti.setImage(1, iconBLUE);
				
			id++;
		}
		
		noJarLbl.setVisible(false);
		currentGames.setVisible(true);
	}
	
	private void addGameToJar() {
		if (activeLibrary.getSelection().length == 0)
			return;
		if (currGame.outputFile.length()==0)
			return;
		
		String[] dat = ((String)activeLibrary.getSelection()[0].getData()).split("::");
		String pathToFile = dat[0];
		String pathWithinFile = null;
		if (dat.length>1)
			pathWithinFile = dat[1];
		
	/*	System.out.println(pathToFile);
		if (pathWithinFile!=null)
			System.out.println(pathWithinFile);*/
		
		//Copy the game into the jar file... first, upgrade the format
		/*File f = new File(new File("").getAbsolutePath()+"\\temp");
		if (!f.exists()) {
			if (!f.mkdir()) {
				System.out.println("Error making temp directory!");
				return;
			}
		} else
			clearTempFiles(f, true);*/
		
		newRPGName = "";
		if (pathWithinFile==null) {
			lockButtons(true);
			
			//Prepare the zip file.
			newRPGName = pathToFile.substring(pathToFile.lastIndexOf('\\')+1, pathToFile.lastIndexOf('.'));
			File tempJarFile = null;
			ZipFile currFile = null;
			tempJar = null;
			tempDir = new File(currGame.outputFile.substring(0, currGame.outputFile.lastIndexOf('\\')));
			System.out.println("Temporary directory: " + tempDir.getAbsolutePath() + "  isDir(" + tempDir.isDirectory() + ")");
			try {
				currFile = new ZipFile(currGame.outputFile);
				tempJarFile = new File(currGame.outputFile + ".ZIP");
				tempJar = new ZipOutputStream(new FileOutputStream(tempJarFile, true));
			} catch (IOException ex) {
				System.out.println("Error creating temporary JAR file: " + ex.toString());
				System.out.println("  JAR file path: " + tempJarFile.exists() + " " + tempJarFile.getAbsolutePath());
				System.exit(1);
			}

			
			//Create the directory for this game.
			try {
				ZipEntry dir = new ZipEntry("ohrrpgce/games/" + newRPGName + "/");
				//System.out.println("Entry opened: " + dir.getName());
				tempJar.putNextEntry(dir);
			} catch (IOException ex) {
				System.out.println("Error creating our game's folder: " + newRPGName + " --> " + ex.toString());
				System.exit(1);
			}
			
			//Now, convert and add at the same time.
			String newFullName = RPG2XRPG.convert(new File(pathToFile), new OutputStreamCreator() {
				public OutputStream getOutputStream(String lumpName) {
					//Close the current entry.
					endEntry();
					
					//Get the next entry
					ZipEntry dir = new ZipEntry("ohrrpgce/games/" + newRPGName + "/" + lumpName);
					//System.out.println("Entry opened: " + dir.getName());
					try {
						tempJar.putNextEntry(dir);
					} catch (IOException ex) {
						System.out.println("Error adding entry for " + lumpName + " : " + ex.toString());
						return null;
					}
					return tempJar;
				}
				public File getOutputFile(String lumpName) {
					endEntry();
					
					lastImageLumpName = lumpName;
					lastImageFile = new File(tempDir.getAbsolutePath() + "\\" + lumpName);

					return lastImageFile;
				}
				public void closeOutputStream(OutputStream os) throws IOException {
					//NO!
				}
			});
			endEntry();

			
			//Next, add it to the game_list file
			GameInfo gI = new GameInfo();
			gI.name = newRPGName;
			gI.fullName = newFullName; //THIS NEEDS TO BE DONE EARLIER!!!
			gI.mobileFormat = 1;
			gI.numBytes = 1000; //Doesn't matter, so long as it's >, oh, 100 or so
			gI.pathToIconFile = ""; //Fix later
			currGame.gamePaths.add(gI);
			
			//Copy existing entries
			Enumeration/*<? extends ZipEntry>*/ allEntries = currFile.entries();
			while (allEntries.hasMoreElements()) {
				ZipEntry currEntry = (ZipEntry)allEntries.nextElement();
				if (currEntry==null)
					return;
				
				BufferedInputStream entryInput = null;
				try {
					if (!currEntry.getName().equals("ohrrpgce/games/game_list.txt"))
						entryInput = new BufferedInputStream(currFile.getInputStream(currEntry));
					else
						entryInput = new BufferedInputStream(new StringBufferInputStream(currGame.toString()));
					ZipEntry out = new ZipEntry(currEntry.getName());
					tempJar.putNextEntry(out);
					byte[] buffer = new byte[1024];
					for (;;) {
						int len = entryInput.read(buffer);
						if (len<=0)
							break;
						tempJar.write(buffer, 0, len);
					}
					
					entryInput.close();
					entryInput = null;
					tempJar.closeEntry();
				} catch (IOException ex) {
					System.out.println("Error copying zip entry: " + currEntry.getName() + " --> " + ex.toString());
					System.exit(1);
				}
			}
			
			//Close
			try {
				tempJar.close();
			} catch (IOException ex) {
				System.out.println("Error closing temp jar file: " + ex.toString());
			}
			
			//Re-write the new JAR
			try {
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(tempJarFile));
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(currGame.outputFile));
				byte[] lin = new byte[1024];
				int len = 0;
				for(;;) {
					len = in.read(lin);
					if (len <= 0)
						break;
					
					out.write(lin, 0, len);
				}
				in.close();
				out.close();
			} catch (Exception ex) {
				System.out.println("Error copying temp jar to an actual file: " + ex.toString());
				System.exit(1);
			}
			
			//And remove all temporary files; we're done!
			tempJarFile.delete();
			lockButtons(false);
		} else if (!pathWithinFile.toUpperCase().endsWith(".RPG")) {
			lockButtons(true);
//			RPG2XRPG.convert(pathToFile, outputDirectory, clearDir)
//			clearTempFiles(f, true);
			lockButtons(false);
		}
			
		
		//Hack the JAD file; we can do this in one big string, I think.
		String jadPath = currGame.outputFile.substring(0, currGame.outputFile.length()-1) + "D";
		ArrayList/*<String>*/ res = new ArrayList/*<String>*/();
		
		try {
			BufferedReader in =  new BufferedReader(new FileReader(jadPath));
			String line = "";
			for (;;) {
				line = in.readLine();
				if (line==null)
					break;
				if (line.contains("MIDlet-Jar-Size:")) {
					System.out.println("Size fix: " + line);
					line = line.replaceAll(": [0-9]+", ": "+new File(currGame.outputFile).length());
					System.out.println("        + " + line);
				}
				res.add(line);
			}
			in.close();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(jadPath));
			//for (String line2 : res)
			for (int lI=0; lI<res.size(); lI++)
				out.write((String)res.get(lI) + "\n");
			out.close();
		} catch (Exception ex) {
			System.out.println("Error hacking JAR file: " + ex.toString());
		}
		
		
		reloadTable(true);
		System.out.println("Done");
		
	}
	
	
	private void endEntry() {
		//Did we last read an image file?
		if (lastImageFile!=null) {
			System.out.println("Dealing with image file: " + lastImageLumpName);
			//Add an entry for it
			try {
				ZipEntry dir = new ZipEntry("ohrrpgce/games/" + newRPGName + "/" + lastImageLumpName);
				//System.out.println("Entry opened: " + dir.getName());
				tempJar.putNextEntry(dir);
			} catch (IOException ex) {
				System.out.println("Error adding entry for image file: " + lastImageLumpName);
			}
			
			//Copy it.
			try {
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(lastImageFile));
				byte[] lin = new byte[1024];
				int len = 0;
				for(;;) {
					len = in.read(lin);
					if (len <= 0)
						break;
					
					tempJar.write(lin, 0, len);
				}
				in.close();
			} catch (IOException ex) {
				System.out.println("Error copying raw image data: " + ex.toString());
			}
			
			lastImageFile.delete();
			lastImageFile=null;
		} 
		
		//Close the entry
		try {
			//System.out.println("  + Entry closed");
			tempJar.closeEntry();
		} catch (IOException ex) {
			System.out.println("Error closing jar entry: " + ex.toString());
		}
	}
	
	
	/*private void clearTempFiles(File f, boolean descend) {
		//Clear it
		File[] toDelete = f.listFiles();
		for (File fx : toDelete) {
			if(fx.isDirectory()) {
				if (descend)
					clearTempFiles(fx, false);
				else
					System.out.println("Couldn't delete directory: " + fx.getAbsolutePath());
			}
			fx.delete();
		}
	}*/
	
	private void changeLanguage(LanguagePack newLang) {
		currLang = newLang;
		if (currLang != null) {
			mainShell.setText(currLang.getTitlebarText());
			langMI.setText(currLang.getLanguageMenuText());
			libraryLbl.setText(currLang.getLibraryTitle());
			addFileBtn.setText(currLang.getAddFileText());
			addFolderBtn.setText(currLang.getAddFolderText());
			//deleteBtn.setText(currLang.getDeleteText());
			noGamesLbl.setText(currLang.getNoGamesInLibraryText());
			noJarLbl.setText(currLang.getNoJarFileSelectedText());
			mobileLbl.setText(currLang.getMobileTitle());
//			updateBtn.setText(currLang.getUpdateText());
			pathBtn.setText(currLang.getSetPathText());
		}
	}
	
	
	class GameStatus {
		public String jarPath;
		public float jarVersion;
		public String outputFile;
		public ArrayList/*<GameInfo>*/ gamePaths;
	//	public ArrayList<Long> gameTimestamps;
		
		public GameStatus() {
			gamePaths = new ArrayList/*<GameInfo>*/();
		//	gameTimestamps =  new ArrayList<Long>();
			jarPath = "";
			jarVersion = -1;
			outputFile = "";
		}
		
		/**
		 * Specifically for output as a game_list.txt file
		 */
		public String toString() {
			StringBuilder sb = new StringBuilder();
			//for (GameInfo g : gamePaths)
			for (int gI=0; gI<gamePaths.size(); gI++) {
				GameInfo g = (GameInfo)gamePaths.get(gI);
				sb.append(g.name + "\t").append(g.fullName + "\t").append(g.pathToIconFile + " \t").append(g.numBytes + "\t").append(g.mobileFormat + "\n");
			}
			return sb.toString();
		}
	}
	
	class GameInfo {
	    public String name;
	    public String fullName;
	    public String pathToIconFile;
	    public int numBytes;
	    public int mobileFormat; //O=just RPG, 1=zipped pngs, etc.
	}
	
	
	///////////////////////////////////////////////////////////////////
	// Main method.
	//////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		new PhoneLoader();
	}

}
