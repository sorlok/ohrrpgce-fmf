package ohrrpgce.tool;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HSP2HF extends JFrame {
	private static final long[] PDP_OFFSETS = {256*256, 256*256*256, 1, 256};
	
	private JPanel toolbarPnl;
	private JList scriptNameLst;
	private JPanel scriptContentsPnl;
	private int flipflop = 1;
	
	//Scripts
	private DefaultListModel scriptNameModel;
	private Hashtable idToName;
	
	//Sources
	//private ScriptSourceRenderer scriptSrcModel;
	//private ArrayList scriptSrcInOrder;
	private Hashtable atIDToScriptSrc;
	private ScriptSrc currScriptSrc;
	
	
	public HSP2HF() {
		//Init
		super("Hamsterspeak Analysis Engine");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		
		this.initComponents();
		this.layoutComponents(this.getContentPane());
		
		this.setSize(400, 300);
		this.setVisible(true);
	}
	
	
	private void swapIn(ScriptSrc nextSrc) {
		if (currScriptSrc!=null) {
			currScriptSrc.flashID(false);
		}
		
		currScriptSrc = nextSrc;
		currScriptSrc.flashID(true);
	}
	
	
	private void promptForHSPFile() {
		//Prompt for a file
		File f =  new File("ohrrpgce/games/WANDER/WANDER.HSP");
		JFileChooser jfc = new JFileChooser(f);
		jfc.setSelectedFile(f);
		if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		FileInputStream hspFile;
		try {
			hspFile = new FileInputStream(jfc.getSelectedFile());
		} catch (Exception ex) { 
			return;
		}
		
		//Read header lump
		int maxStringWidth = -1;
		if (true) {
			Lump headerLump = new Lump(hspFile);
			if (headerLump.inError)
				System.exit(1);
			if (!headerLump.name.equals("HS")) {
				System.out.println("Errr: HS lump must come first (received: " + headerLump.name + ")");
				System.exit(1);
			}
			System.out.println("\n"+headerLump.name + "[" + headerLump.data.length + "]");
			System.out.println("-----------------");
			String validate = "HamsterSpeak";
			int compilerVersion = headerLump.data[validate.length()] + ((int)headerLump.data[validate.length()+1])<<8;
			for (int i=0; i<validate.length(); i++) {
				if (headerLump.data[i] != validate.charAt(i)) {
					System.out.println(validate + " ==> fails on [" + i + "] = " + headerLump.data[i]);
					System.exit(1);
				}
			}
			System.out.println("  " + validate + " v" + compilerVersion);
		}
		
		
		//Read scripts.txt or scripts.bin
		if (true) {
			Lump scriptsTextLump = new Lump(hspFile);
			if (scriptsTextLump.inError)
				System.exit(1);
			if (!scriptsTextLump.name.equals("SCRIPTS.TXT")) {
				System.out.println("Errr: SCRIPTS lump must come second (received: " + scriptsTextLump.name + ")");
				if (scriptsTextLump.name.equals("SCRIPTS.BIN")) {
					System.out.println("  (We can't handle binary script format yet...)");
				}
				System.exit(1);
			}
			System.out.println("\n"+scriptsTextLump.name + "[" + scriptsTextLump.data.length + "]");
			System.out.println("-----------------");
			
			//Read each name
			idToName = new Hashtable();
			atIDToScriptSrc = new Hashtable();
			Script newScript = new Script();
			StringBuilder currStr = new StringBuilder();
			int currID = 0;
			int minScriptID = Integer.MAX_VALUE;
			int maxScriptID = -1;
			for (int i=0; i<scriptsTextLump.data.length; i++) {
				char c = (char)scriptsTextLump.data[i];
				if (c!=0x0D && c!=0x0A) {
					currStr.append(c);
				} else if (c==0x0A) {
					//Interpret this string
					if (newScript.name==null) {
						newScript.name = currStr.toString();
						//System.out.println("+"+newScript.name+"+");
					} else if (newScript.id==-1) {
						newScript.id = Integer.parseInt(currStr.toString());
					} else {
						//Final phase
						if (newScript.args==null) {
							newScript.args = new int[Integer.parseInt(currStr.toString())];
							currID = 0;
						} else {
							newScript.args[currID++] = Integer.parseInt(currStr.toString());
						}
					
						//Done with this script?
						if (currID==newScript.args.length) {
							idToName.put(new Integer(newScript.id), newScript);
							if (newScript.id > maxScriptID)
								maxScriptID = newScript.id;
							else if (newScript.id < minScriptID)
								minScriptID = newScript.id;
							newScript = new Script();
						}
					}
					
					//Done with this argument
					currStr = new StringBuilder();
				}
			}
			
			//Now, load our data into a useful place
			scriptNameModel.clear();
			for (int i=1; i<=maxScriptID; i++) {
				Script s = (Script)idToName.get(new Integer(i));
				if (s==null) {
					//scriptNameModel.addElement("<script " + i + " does not exist>");
				} else { 
					scriptNameModel.addElement(s);
					
					int currStringWidth = scriptNameLst.getFontMetrics(scriptNameLst.getFont()).stringWidth(s.toString());
					if (currStringWidth > maxStringWidth) 
						maxStringWidth = currStringWidth;
				}
				
			}
			System.out.println("  Scripts min/max: " + minScriptID + " - " + maxScriptID);
		}
		
		
		try {
			while (hspFile.available()>0) {
				Lump scriptSrc = new Lump(hspFile);
				if (scriptSrc.inError)
					System.exit(1);
				String[] nameSplit = scriptSrc.name.split("\\.");
				if (nameSplit.length!=2 || (!nameSplit[1].equals("HSX") && !nameSplit[1].equals("HSZ"))) {
					if (scriptSrc.name.equals("SCRIPTS.BIN")) {
						System.out.println("Skipping: " + scriptSrc.name);
						continue;
					}
					System.out.println("Bad script lump: " + scriptSrc.name);
					System.exit(1);
				}
				((Script)idToName.get(new Integer(Integer.parseInt(nameSplit[0])))).scriptSrc = scriptSrc;			
			}
		} catch (IOException ex) {
			System.out.println("Weird error: " + ex.toString());
			ex.printStackTrace();
			System.exit(1);
		}
		
		
		
		//Re-layout and reset
		if (scriptNameModel.size()>0) {
			scriptNameLst.setSelectedIndex(0);
			scriptNameLst.ensureIndexIsVisible(0);
			
			scriptNameLst.getParent().setMinimumSize(new Dimension(maxStringWidth + 10, 1));
			
			this.setSize(new Dimension(this.getWidth(), this.getHeight()+flipflop)); //hackish...
			flipflop *= -1;
		}

	}
	
	
	
	private void loadScriptSource(byte[] data) {
		//Assume format 2
		/*System.out.println("Testing");
		System.out.println("  Offset in bytes: " + (data[0] + (data[1]<<8)));
		System.out.println("  Script Variables: " + (data[2] + (data[3]<<8)));
		System.out.println("  Script Arguments: " + (data[4] + (data[5]<<8)));
		System.out.println("  Script Format Version: " + (data[6] + (data[7]<<8)));
		System.out.println("  String Table Offset: " + Integer.toHexString(data[8]) + "  " + Integer.toHexString(data[9]) + "  " + Integer.toHexString(data[10]) + "  " + Integer.toHexString(data[11]));*/
		
		atIDToScriptSrc.clear();
		currScriptSrc = null;
		scriptContentsPnl.removeAll();
		for (int i=12; i<data.length;) {
			//Read kind, ID, length, & args; advance pointers.
			int startsAt = (i-12)/4;
			int kind = (data[i++]&0xFF) | ((((int)data[i++])*0x100)&0xFF00) | ((((int)data[i++])*0x10000)&0xFF0000) | (((int)(data[i++])*0x1000000)&0xFF000000);
		    int id = (data[i++]&0xFF) | ((((int)data[i++])*0x100)&0xFF00) | ((((int)data[i++])*0x10000)&0xFF0000) | (((int)(data[i++])*0x1000000)&0xFF000000);
			int[] args =  null;
			if (kind==2 || kind==5 || kind==6 || kind==7)
				args = new int[(data[i++]&0xFF) | ((((int)data[i++])*0x100)&0xFF00) | ((((int)data[i++])*0x10000)&0xFF0000) | (((int)(data[i++])*0x1000000)&0xFF000000)];
			else if (kind==1 || kind==3 || kind==4)
				args = null;
			else {
				System.out.println("Invalid kind: " + kind);
				System.exit(1);
			}
			if (args!=null) {
				for (int k=0; k<args.length; k++) {
					args[k] = (data[i++]&0xFF) | ((((int)data[i++])*0x100)&0xFF00) | ((((int)data[i++])*0x10000)&0xFF0000) | (((int)(data[i++])*0x1000000)&0xFF000000);
				}
			}
			
			//Cache the "kind" name.
			String kindName;
			switch (kind) {
				case 1:
					kindName = "number";
					break;
				case 2:
					kindName = "flow_control";
					break;
				case 3:
					kindName = "global_variable";
					break;
				case 4:
					kindName = "local_variable";
					break;
				case 5:
					kindName = "math";
					break;
				case 6:
					kindName = "built-in";
					break;
				case 7:
					kindName = "script";
					System.out.println("script: " + id);
					break;
				default:
					kindName = "<unknown>";
					break;
			}
			
			//Create a "source" snippet
			StringBuilder srcSnippet = new StringBuilder();
			if (kind==1) {
				srcSnippet.append(id);
			} else if (kind==3) {
				srcSnippet.append("var[" + id + "]G");
			} else if (kind==4) {
				srcSnippet.append("var[" + id + "]L");
			} else if (kind==7) {
				srcSnippet.append("<b>"+((Script)idToName.get(new Integer(id))).name + "</b>(");
				String comma = "";
				for (int k=0; k<args.length; k++) {
					srcSnippet.append(comma + "{<a href="+args[k]+">"+args[k]+"</a>}");
					comma = ",";
				}
				srcSnippet.append(");");
			} else if (kind==5) {
				switch (id) {
					case 0:
						srcSnippet.append("rand(<a href="+args[0]+">{"+args[0]+"}</a>, <a href="+args[1]+">{"+args[1]+"}</a>);");
						break;
					case 1:
						srcSnippet.append("{"+args[0]+"}**{"+args[1]+"}");
						break;
					case 2:
						srcSnippet.append("{"+args[0]+"}%{"+args[1]+"}");
						break;
					case 3:
						srcSnippet.append("{"+args[0]+"}/{"+args[1]+"}");
						break;
					case 4:
						srcSnippet.append("{"+args[0]+"}*{"+args[1]+"}");
						break;
					case 5:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}-{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 6:
						srcSnippet.append("{"+args[0]+"}+{"+args[1]+"}");
						break;
					case 7:
						srcSnippet.append("{"+args[0]+"}^{"+args[1]+"}");
						break;
					case 8:
						srcSnippet.append("{"+args[0]+"}|{"+args[1]+"}");
						break;
					case 9:
						srcSnippet.append("{"+args[0]+"}&{"+args[1]+"}");
						break;
					case 10:
						srcSnippet.append("{"+args[0]+"}=={"+args[1]+"}");
						break;
					case 11:
						srcSnippet.append("{"+args[0]+"}!={"+args[1]+"}");
						break;
					case 12:
						srcSnippet.append("{"+args[0]+"}<{"+args[1]+"}");
						break;
					case 13:
						srcSnippet.append("{"+args[0]+"}>{"+args[1]+"}");
						break;
					case 14:
						srcSnippet.append("{"+args[0]+"}<={"+args[1]+"}");
						break;
					case 15:
						srcSnippet.append("{"+args[0]+"}>={"+args[1]+"}");
						break;
					case 16:
						srcSnippet.append("{"+args[0]+"}={"+args[1]+"}");
						break;
					case 17:
						srcSnippet.append("{"+args[0]+"}+={"+args[1]+"}");
						break;
					case 18:
						srcSnippet.append("{"+args[0]+"}+={"+args[1]+"}");
						break;
					case 19:
						srcSnippet.append("!{"+args[0]+"}");
						break;
					case 20:
						srcSnippet.append("{"+args[0]+"}&&{"+args[1]+"}");
						break;
					case 21:
						srcSnippet.append("{"+args[0]+"}||{"+args[1]+"}");
						break;
					case 22:
						srcSnippet.append("{"+args[0]+"}^^{"+args[1]+"}");
						break;
				}
			} else if (kind==2) {
				String comma = null;
				switch (id) {
					case 0:
						srcSnippet.append("do(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{"+args[k]+"}");
							comma = ",";
						}
						srcSnippet.append(");");
						break;
					case 1:
						srcSnippet.append("begin (unexpected)");
						break;
					case 2:
						srcSnippet.append("end (unexpected)");
						break;
					case 3:
						srcSnippet.append("return(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{"+args[k]+"}");
							comma = ",";
						}
						srcSnippet.append(");  ");
						break;
					case 4:
						srcSnippet.append("if {"+args[0]+"} then {"+args[1]+"} else {"+args[2]+"}");
						break;
					case 5:
						srcSnippet.append("then(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{"+args[k]+"}");
							comma = ",";
						}
						srcSnippet.append(");");
						break;
					case 6:
						srcSnippet.append("else(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{"+args[k]+"}");
							comma = ",";
						}
						srcSnippet.append(");");
						break;
					case 7:
						srcSnippet.append("for(var[{"+args[0]+"}]L i={"+args[1]+"}; i!={"+args[2]+"}; i+={"+args[3]+"}) do {"+args[4]+"};");
						break;
					case 10:
						srcSnippet.append("while({"+args[0]+"}) do {"+args[1]+"}");
						break;
					case 11:
						srcSnippet.append("break {"+args[0]+"}");
						break;
					case 12:
						srcSnippet.append("continue {"+args[0]+"}");
						break;
					case 13:
						srcSnippet.append("exit_script();");
						break;
					case 14:
						srcSnippet.append("exit_returning("+args[0]+"});");
						break;
					case 15:
						srcSnippet.append("switch(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{"+args[k]+"}");
							comma = ",";
						}
						srcSnippet.append(");");
						break;
					case 16:
						srcSnippet.append("case (unexpected)");
						break;
				}
			} else if (kind==6) {
				srcSnippet.append("built-in[{"+id+"}](");
				String comma = "";
				for (int k=0; k<args.length; k++) {
					srcSnippet.append(comma + "{"+args[k]+"}");
					comma = ",";
				}
				srcSnippet.append(");");
			} else {
				//Stack trace
				srcSnippet.append(kind + ":" + id + " ");
				if (args!=null) {
					srcSnippet.append("[");
					String comma = "";
					for (int k=0; k<args.length; k++) {
						srcSnippet.append(comma + args[k]);
						comma = ",";
					}
					srcSnippet.append("]");
				}
			}
			
			//Add it to the hashtable, and to the list  
			ScriptSrc curr = new ScriptSrc(startsAt, atIDToScriptSrc.size(), kindName, srcSnippet.toString());
			atIDToScriptSrc.put(new Integer(curr.atID), curr);
			scriptContentsPnl.add(curr);
		}
	}
	


	private void initComponents() {
		//Our top panel
		toolbarPnl = new JPanel(new FlowLayout());
		JButton bt = new JButton("Open");
		bt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				promptForHSPFile();
			}
		});
		toolbarPnl.add(bt);
		
		//Left panel
		scriptNameModel = new DefaultListModel();
		scriptNameLst = new JList(scriptNameModel);
		scriptNameLst.setLayoutOrientation(JList.VERTICAL);
		scriptNameLst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptNameModel.addElement("(no scripts)");
		scriptNameLst.setBackground(toolbarPnl.getBackground());
		scriptNameLst.setBorder(BorderFactory.createTitledBorder("Scripts"));
		scriptNameLst.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				//Only deal with the last change.
				if (e.getValueIsAdjusting())
					return;
				
				//Now, load that into the panel
				Script s = (Script)scriptNameLst.getSelectedValue();
				System.out.println("\n" + s.name);
				loadScriptSource(s.scriptSrc.data);
				
				//Reset size...
				setSize(new Dimension(getWidth(), getHeight()+flipflop)); //hackish...
				flipflop *= -1;
			}
		});
		
		//Right panel
		scriptContentsPnl = new JPanel();
		scriptContentsPnl.setLayout(new GridLayout(0, 1));
		scriptContentsPnl.setBorder(BorderFactory.createTitledBorder("Source"));
	}
	
	
	
	private void layoutComponents(Container cp) {
		//Top panel
		if (true) {//Scope
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridheight = 1;
			gbc.fill = GridBagConstraints.NONE;
			gbc.ipadx = 2;
			gbc.ipady = 2;
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			cp.add(toolbarPnl, gbc);
		}
		
		//Left panel
		if (true) {//Scope
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.ipadx = 2;
			gbc.ipady = 2;
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			JScrollPane jsp = new JScrollPane(scriptNameLst, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jsp.setBorder(null);
			cp.add(jsp, gbc);
		}
		
		//Right panel
		if (true) {//Scope
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.ipadx = 2;
			gbc.ipady = 2;
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.weightx = 0.5;
			gbc.weighty = 1.0;
			JPanel jp = new JPanel(new BorderLayout());
			jp.add(scriptContentsPnl, BorderLayout.NORTH);
			//jp.add(new JPanel(), BorderLayout.CENTER);
			JScrollPane jsp = new JScrollPane(jp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			cp.add(jsp, gbc);
		}
	}
	
	class Lump {
		public String name;
		public byte[] data;
		public boolean inError;
		
		public Lump(FileInputStream ir) {
			//Name
			StringBuilder lumpName = new StringBuilder();
			try {
				for (int c=ir.read(); c!=0; c=ir.read())
					lumpName.append((char)c);
				this.name = lumpName.toString();
			} catch (IOException ex) {
				System.out.println("Error: couldn't read lump name: " + ex.toString());
				ex.printStackTrace();
				this.inError = true; 
			}
			
			//Size
			int lumpSize = 0;
			try {
				for (int i=0; i<PDP_OFFSETS.length; i++)
					lumpSize += (PDP_OFFSETS[i] * ir.read());
			} catch (IOException ex) {
				System.out.println("Error: couldn't read lump size: " + ex.toString());
				ex.printStackTrace();
				this.inError = true; 
			}
			
			//Data
			try {
				this.data = new byte[lumpSize];
				ir.read(data);
			} catch (IOException ex) {
				System.out.println("Error: couldn't read lump data: " + ex.toString());
				ex.printStackTrace();
				this.inError = true; 
			}
		}
	}
	
	
	class Script {
		public String name;
		public int id;
		public int[] args;
		public Lump scriptSrc;
		
		public Script() {
			this.id = -1;
		}
		
        /*private int convertTwosComplementInt(int val) {
        	System.out.println(Integer.toHexString(val));
            if ((val&0x8000)!=0) {
                val ^= 0xFFFF;
                val = -val-1;
            }
            return val;
        }*/
		
		public String toString() {
			return "[" + id + "]" + " " + name + "(" + args.length + " args)";
		}
	}
	
	
	class ScriptSrc extends JPanel {
		private int atID;
		private String kindName;
		private String srcSnippet;
		private int realID;
		private String bgColor;
		
		private JEditorPane scriptSrcLbl;
		private JPanel upperPnlLeft;
		
		public ScriptSrc(int atID, int realID, String kindName, String srcSnippet) {
			this.atID = atID;
			this.realID = realID;
			this.kindName = kindName;
			this.srcSnippet = srcSnippet;
			initPnl();
		}
		
		public void flashID(boolean on) {
			Color c = Color.yellow;
			if (!on)
				c = this.getBackground();
			
			upperPnlLeft.setBackground(c);
		}
		
		private void initPnl() {
			int bgColor = 0xD0FFE6;
			if (this.realID%2==0)
				bgColor = 0xBBEEFF;
			this.setBackground(new Color(bgColor));
			this.bgColor = ""+Integer.toHexString(bgColor).toUpperCase();
			//System.out.println(this.bgColor);
			this.setPreferredSize(new Dimension(300, 50));
			this.setLayout(new BorderLayout());
			this.setBorder(BorderFactory.createLineBorder(Color.darkGray, 1));
			this.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					swapIn(ScriptSrc.this);
				}
			});
			
			scriptSrcLbl = new JEditorPane("text/html", "<html><head><style type='text/css'>body {padding: 3 3 0 3; background-color: #"+this.bgColor+";}  a {text-decoration: underline; font-weight: bold; color: #FF2222;}</style></head><body><span style='font-family: Courier; font-weight: normal; font-size: 12pt;'>" + this.srcSnippet + "</span></body></html>");
			scriptSrcLbl.setMargin(new Insets(0, 0, 0, 0));
			scriptSrcLbl.setEditable(false);
			scriptSrcLbl.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent ev) {
					if (ev.getEventType()==HyperlinkEvent.EventType.ACTIVATED)  {
						try {
							int x = Integer.parseInt(ev.getDescription());
							swapIn(((ScriptSrc)atIDToScriptSrc.get(new Integer(x))));
						} catch (NumberFormatException ex) {}
					}
				}
			});
			
			JPanel lowerPnl = new JPanel(new BorderLayout());
			lowerPnl.setBackground(this.getBackground());
			//lowerPnl.setBorder(BorderFactory.createLineBorder(Color.lightGray, 2));
			JLabel srcLbl = new JLabel("Source");
			srcLbl.setFont(new Font("Arial", Font.ITALIC, 12));
			//lowerPnl.add(srcLbl, BorderLayout.NORTH);
			//lowerPnl.add(scriptSrcLbl, BorderLayout.NORTH);
			this.add(scriptSrcLbl, BorderLayout.CENTER);
			
			upperPnlLeft = new JPanel(new BorderLayout());
			upperPnlLeft.setBackground(this.getBackground());
			upperPnlLeft.setBorder(BorderFactory.createLineBorder(Color.lightGray, 2));
			JLabel atLbl = new JLabel(" @");
			atLbl.setFont(new Font("Arial", Font.BOLD, 8));
			JLabel atValLbl = new JLabel(atID+" ");
			atValLbl.setFont(new Font("Arial", Font.PLAIN, 16));
			upperPnlLeft.add(atLbl, BorderLayout.WEST);
			upperPnlLeft.add(atValLbl, BorderLayout.CENTER);
			
			JPanel upperPnlRight = new JPanel(new BorderLayout());
			upperPnlRight.setBackground(this.getBackground());
			JLabel typeLbl = new JLabel(" ");
			typeLbl.setFont(new Font("Arial", Font.ITALIC, 12));
			JLabel typeValLbl = new JLabel(kindName);
			typeValLbl.setFont(new Font("Arial", Font.ITALIC, 12));
			upperPnlRight.add(typeLbl, BorderLayout.WEST);
			upperPnlRight.add(typeValLbl, BorderLayout.CENTER);
			
			JPanel upperPanl = new JPanel(new BorderLayout());
			upperPanl.setBackground(this.getBackground());
			upperPanl.add(upperPnlLeft, BorderLayout.WEST);
			upperPanl.add(upperPnlRight, BorderLayout.CENTER);
			this.add(upperPanl, BorderLayout.NORTH);
		}
		
		public int getAtID() {
			return this.atID;
		}
		public int getRealID() {
			return this.realID;
		}
		public String getKindName() {
			return this.kindName;
		}
		public String getSrcSnippet() {
			return this.srcSnippet;
		}
	}
	
	
	public static void main(String[] args) {
		new HSP2HF();
	}
}

