package ohrrpgce.web;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import javax.swing.*;

import ohrrpgce.adapter.GraphicsAdapter;
import ohrrpgce.adapter.applet.*;
import ohrrpgce.game.MenuEngine;
import ohrrpgce.menu.Action;
import ohrrpgce.runtime.EngineManager;
import ohrrpgce.runtime.MetaDisplay;
import ohrrpgce.tool.HQ2X;
import ohrrpgce.tool.LQ2X;

public class Slackulator extends JFrame implements KeyListener {
	
	public static final long serialVersionUID = 1L;

	private Hashtable/*<JButton, Integer>*/ buttonVals;
	private JButton[] keypad = new JButton[12];
	private GraphicsCanvas screen;
	private GraphicsCanvas miniScreen;
	private JCheckBox outlineDoors;
	private JCheckBox outlineCharacters;
	private JCheckBox showMenuCurrentFocus;
	private JCheckBox showLargeScreen;
	private JCheckBox hq2xLargeScreen;
	//private JLabel outlineDoorLbl;
	//private JLabel outlineChrLbl;
	//private JLabel showMenuFocusLbl;
	private JPanel debugPanel;
	
	private BufferedImage buffer;
	private int SCREEN_WIDTH = 240;
	private int SCREEN_HEIGHT = 320;
	
	
	//Configs
	private boolean runMiniScreen = false;
	private boolean runDoubleScreen = true;
	private boolean highQualityScale = false;

	private EngineManager engineMgr;
	private AdapterGenerator adaptGen;
	//private GraphicsAdapter_applet graphicsAd;
	private InputAdapter inputAd;
	
	private int keyVals;
	private int antiKeys;
	private int allKeys = InputAdapter.KEY_ACCEPT | InputAdapter.KEY_CANCEL | InputAdapter.KEY_DOWN | InputAdapter.KEY_LEFT | InputAdapter.KEY_RIGHT | InputAdapter.KEY_UP;
	
	//Hackish
	//private MouseMotionListener mouseClick;

	public static void main(String[] args) {
		new Slackulator();
	}
	
	public Slackulator() {
		//super("OHRRPGCE FMF - Live Demo!");
		super("Emulator Test");
		this.setBackground(Color.BLACK);

		/*try {
			ImageAdapter.prefix = new File("").getAbsolutePath();
		} catch (SecurityException ex) {
			//If an exception is thrown the client is running remotely. 
			// So "prefix" remains empty... which is exactly what we wanted.
		}*/
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		loadComponents();
		addComponents(this.getContentPane());
		pack();
		setVisible(true);
		
		buffer = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		GraphicsAdapter.init(buffer.getGraphics(), new Action() {
			public boolean perform(Object caller) {
				swapBuffers();
				return true;
			}
		});
		adaptGen = new AdapterGenerator(new int[]{SCREEN_WIDTH, SCREEN_HEIGHT}, new Action() {
			public boolean perform(Object caller) {
				escHit(((Boolean)caller).booleanValue());
				return false;
			}
		});
		inputAd = new InputAdapter(this);
		
		this.addKeyListener(this);
		screen.addMouseListener(new MouseAdapter() { //Just in case!
			public void mouseClicked(MouseEvent e) {
				requestFocus();
			}
		});
		miniScreen.addMouseListener(new MouseAdapter() { //Just in case!
			public void mouseClicked(MouseEvent e) {
				requestFocus();
			}
		});
		
/*		this.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				requestFocus();
			}
		});*/
		this.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				requestFocus();
			}
		});
		
		//Start your engines!
		engineMgr = new EngineManager(adaptGen, inputAd);
		engineMgr.startYourEngines();
	}

	
	
	private void escHit(boolean unconditional) {
		if (!unconditional && !engineMgr.canExit())
			return;
		System.exit(1);
	}
	
	

	private void loadComponents() {
		MouseListener buttonListener = new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				JButton source = (JButton)e.getSource();
				int flag = ((Integer)buttonVals.get(source)).intValue();
				keyVals |= flag;
				antiKeys &= (allKeys^flag);
				//e.consume();
			}
			public void mouseReleased(MouseEvent e) {
				JButton source = (JButton)e.getSource();
				int flag = ((Integer)buttonVals.get(source)).intValue();
				antiKeys |= flag;
				//e.consume();
				requestFocus();
			}
		};
		
		//Initialize the buttons
		String indicate;
		String num;
		buttonVals = new Hashtable/*<JButton, Integer>*/();
		for (int i=0; i<keypad.length; i++) {
			JButton key = new JButton();
			indicate = "&nbsp;";
			num = "";

			if (i<9) {
				indicate = "CANCEL";
				num = ""+(i+1);
				buttonVals.put(key, new Integer(InputAdapter.KEY_CANCEL));
			}

			if (i==1) {
				indicate = "UP";
				buttonVals.put(key, new Integer(InputAdapter.KEY_UP));
			} else if (i==3) {
				indicate = "LEFT";
				buttonVals.put(key, new Integer(InputAdapter.KEY_LEFT));
			} else if (i==5) {
				indicate = "RIGHT";
				buttonVals.put(key, new Integer(InputAdapter.KEY_RIGHT));
			} else if (i==7) {
				indicate = "DOWN";
				buttonVals.put(key, new Integer(InputAdapter.KEY_DOWN));
			} else if (i==4) {
				indicate = "CONFIRM";
				buttonVals.put(key, new Integer(InputAdapter.KEY_ACCEPT));
			}

			switch (i) {
				case 9:
					num = "*";
					buttonVals.put(key, new Integer(0));
					break;
				case 10:
					num = "0";
					buttonVals.put(key, new Integer(0));
					break;
				case 11:
					num = "#";
					buttonVals.put(key, new Integer(0));
					break;
			}
			key.setText("<html><div align=\"center\">" + num + "<br>" + indicate + "</div></html>");
			key.addMouseListener(buttonListener);
			keypad[i] = key;
		}

		//Initialize the emulator screen
		Dimension fixedSize = new Dimension(SCREEN_WIDTH*2, SCREEN_HEIGHT*2);
		screen = new GraphicsCanvas();
		screen.setBackground(new Color(0x005500));
		screen.setPreferredSize(fixedSize);
		screen.setMinimumSize(fixedSize);
		screen.setMaximumSize(fixedSize);
		
		fixedSize = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
		miniScreen = new GraphicsCanvas();
		miniScreen.setBackground(new Color(0x005500));
		miniScreen.setPreferredSize(fixedSize);
		miniScreen.setMinimumSize(fixedSize);
		miniScreen.setMaximumSize(fixedSize);
		
		if (!runDoubleScreen)
			screen.setVisible(false);
		if (!runMiniScreen)
			miniScreen.setVisible(false);
		
		//Debug
		outlineDoors =  new JCheckBox("Outline Doors", MetaDisplay.DEBUG_OUTLINE_DOORS);
		outlineDoors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MetaDisplay.DEBUG_OUTLINE_DOORS = !MetaDisplay.DEBUG_OUTLINE_DOORS;
				requestFocus();
			}
		});
		
		outlineCharacters =  new JCheckBox("Outline NPCs", MetaDisplay.DEBUG_OUTLINE_SPRITES);
		outlineCharacters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MetaDisplay.DEBUG_OUTLINE_SPRITES = !MetaDisplay.DEBUG_OUTLINE_SPRITES;
				requestFocus();
			}
		});
		
		showMenuCurrentFocus = new JCheckBox("Follow Menu Focus", MenuEngine.DEBUG_CONTROL);
		showMenuCurrentFocus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MenuEngine.DEBUG_CONTROL = !MenuEngine.DEBUG_CONTROL;
				requestFocus();
			}
		});
		
		showLargeScreen = new JCheckBox("Large Screen", runDoubleScreen);
		showLargeScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runDoubleScreen = !runDoubleScreen;
				runMiniScreen = !runDoubleScreen;
				screen.setVisible(runDoubleScreen);
				miniScreen.setVisible(runMiniScreen);
				hq2xLargeScreen.setEnabled(runDoubleScreen);
				requestFocus();
			}
		});
		
		hq2xLargeScreen = new JCheckBox("HQ2X Scale", highQualityScale);
		hq2xLargeScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				highQualityScale = !highQualityScale;
				requestFocus();
			}
		});		

		debugPanel = new JPanel(new GridLayout(3, 2));
		//debugPanel.setBackground(Color.LIGHT_GRAY);
		debugPanel.setBorder(BorderFactory.createTitledBorder("Debug Options"));
	//	debugPanel.setPreferredSize(new Dimension(SCREEN_WIDTH, 100);
	}


	private void addComponents(Container cont) {
		JPanel southPanel = new JPanel(new GridLayout(4, 3));
		for (int i=0; i<keypad.length; i++) {
			southPanel.add(keypad[i]);
		}
		JPanel eastPanel = new JPanel(new BorderLayout());
		eastPanel.add(BorderLayout.CENTER, new JPanel());
		eastPanel.add(BorderLayout.NORTH, southPanel);

		debugPanel.add(/*BorderLayout.WEST,*/ showLargeScreen);
		debugPanel.add(/*BorderLayout.EAST,*/ hq2xLargeScreen);
		debugPanel.add(/*BorderLayout.NORTH,*/ outlineDoors);
		debugPanel.add(/*BorderLayout.CENTER,*/ outlineCharacters);
		debugPanel.add(/*BorderLayout.SOUTH,*/ showMenuCurrentFocus);
		
		
		
		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.WEST, screen, 5, SpringLayout.WEST, cont);
		layout.putConstraint(SpringLayout.NORTH, screen, 5, SpringLayout.NORTH, cont);
		layout.putConstraint(SpringLayout.SOUTH, cont, 5, SpringLayout.SOUTH, screen);
		layout.putConstraint(SpringLayout.WEST, eastPanel, 5, SpringLayout.EAST, screen);
		layout.putConstraint(SpringLayout.NORTH, eastPanel, 5, SpringLayout.NORTH, cont);
		
		
		//Temporary panel, for debugging of course!
		layout.putConstraint(SpringLayout.WEST, miniScreen, 35, SpringLayout.EAST, screen);
		layout.putConstraint(SpringLayout.NORTH, miniScreen, 20, SpringLayout.SOUTH, eastPanel);
		
		//Pseudo-temporary, for other debug options
		layout.putConstraint(SpringLayout.NORTH, debugPanel, 20, SpringLayout.SOUTH, miniScreen);
		layout.putConstraint(SpringLayout.WEST, debugPanel, 35, SpringLayout.EAST, screen);
		layout.putConstraint(SpringLayout.EAST, cont, 5, SpringLayout.EAST, debugPanel);
	//	layout.putConstraint(SpringLayout.EAST, debugPanel, 5, SpringLayout.EAST, this);

		cont.setLayout(layout);
		cont.add(/*BorderLayout.CENTER, */screen);
		cont.add(/*BorderLayout.CENTER, */miniScreen);
		cont.add(/*BorderLayout.EAST, */eastPanel);
		cont.add(debugPanel);
	}
	
	
	private void swapBuffers() {
		int[] inputRGB = buffer.getRGB(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null, 0, SCREEN_WIDTH);
		if (runMiniScreen) {
			miniScreen.swapBuffer(inputRGB, SCREEN_WIDTH);
			miniScreen.repaint();
		}
		if (runDoubleScreen) {
			int[] outputRGB = null;
			if (highQualityScale) {
				outputRGB = HQ2X.hq2x(inputRGB, SCREEN_WIDTH);
			} else {
				outputRGB = LQ2X.lq2x(inputRGB, SCREEN_WIDTH);
			}
			screen.swapBuffer(outputRGB, SCREEN_WIDTH*2);
			screen.repaint();
		}
	}
	
	private int translateKeyCode(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				return InputAdapter.KEY_LEFT;
			case KeyEvent.VK_RIGHT:
				return InputAdapter.KEY_RIGHT;
			case KeyEvent.VK_UP:
				return InputAdapter.KEY_UP;
			case KeyEvent.VK_DOWN:
				return InputAdapter.KEY_DOWN;
			case KeyEvent.VK_BACK_SPACE:
				escHit(false);
				return 0;
			case KeyEvent.VK_ESCAPE:
			//case KeyEvent.VK_ALT:
				return InputAdapter.KEY_CANCEL;
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_SPACE:
				return InputAdapter.KEY_ACCEPT;
			default: 
				return 0;
		}
	}
	
	
	public void keyPressed(KeyEvent e) {
		int keyCode = translateKeyCode(e);
		keyVals |= keyCode;
		antiKeys &= (allKeys^keyCode);
		
		//Make sure nothing else processes this event.
		if (keyCode != 0)
			e.consume();
	}
	
	public void keyReleased(KeyEvent e) {
		int keyCode = translateKeyCode(e);
		antiKeys |= keyCode;
		
		//Make sure nothing else processes this event.
		e.consume();
	}
	
	public void keyTyped(KeyEvent e) {
		//Make sure nothing else processes this event.
		e.consume();
	}
	
	
	public int getKeysAndFlush() {
		int returnVal = keyVals;
		keyVals &= (allKeys^antiKeys);
		antiKeys = 0;
		return returnVal;
	}
	
	
	class GraphicsCanvas extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private BufferedImage buffer;
		
		public GraphicsCanvas() {
			this.setIgnoreRepaint(true);
		}
		
		public void swapBuffer(int[] newRGB, int scanSize) {
			buffer.setRGB(0, 0, buffer.getWidth(), buffer.getHeight(), 
					newRGB, 0, scanSize);
		}
		
		public void setPreferredSize(Dimension preferredSize) {
			super.setPreferredSize(preferredSize);
			
			buffer = new BufferedImage(preferredSize.width, preferredSize.height, BufferedImage.TYPE_INT_RGB);
		}
		
		protected void paintComponent(Graphics g) {
			//Paint background
			super.paintComponent(g);
						
			//Paint the 2X image
			g.drawImage(buffer, 0, 0, null);
		}
	}
}


