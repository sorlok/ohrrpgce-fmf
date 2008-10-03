package ohrrpgce.tool;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HSP2HF extends JFrame {
	private static final long[] PDP_OFFSETS = {256*256, 256*256*256, 1, 256};
	
	private JPanel toolbarPnl;
	private JList scriptNameLst;
	private JPanel scriptContentsPnl;
	
	//Scripts
	private DefaultListModel scriptNameModel;
	private Hashtable idToName;
	
	
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
			this.setSize(new Dimension(this.getWidth(), this.getHeight()+1)); //hackish...
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
		for (int i=12; i<data.length;) {
			//Read kind, ID, length, & args; advance pointers.
			int startsAt = i-12;
			int kind = data[i++] + (data[i++]<<8) + (data[i++]<<16) + (data[i++]<<24);
			int id = data[i++] + (data[i++]<<8) + (data[i++]<<16) + (data[i++]<<24);
			int[] args =  null;
			if (kind==2 || kind==5 || kind==6 || kind==7)
				args = new int[data[i++] + (data[i++]<<8) + (data[i++]<<16) + (data[i++]<<24)];
			else if (kind==1 || kind==3 || kind==4)
				args = null;
			else {
				System.out.println("Invalid kind: " + kind);
				System.exit(1);
			}
			if (args!=null) {
				for (int k=0; k<args.length; k++) {
					args[k] = (int)data[i++] + (((int)data[i++])<<8) + (((int)data[i++])<<16) + (((int)data[i++])<<24);
				}
			}
			
			//Temp: print details
			System.out.print("@" + startsAt/4 + " ");
			if (kind==1) {
				System.out.print("<" + id + ">      ");
			} else if (kind==3) {
				System.out.print("var[" + id + "]G   ");
			} else if (kind==4) {
				System.out.print("var[" + id + "]L   ");
			} else if (kind==7) {
				System.out.println(((Script)idToName.get(new Integer(id))).name + "(");
				String comma = "";
				for (int k=0; k<args.length; k++) {
					System.out.print(comma + "{"+args[k]+"}");
					comma = ",";
				}
				System.out.print(");  ");
			} else if (kind==5) {
				switch (id) {
					case 0:
						System.out.print("rand({"+args[0]+"}, {"+args[1]+"})");
						break;
					case 1:
						System.out.print("{"+args[0]+"}**{"+args[1]+"}");
						break;
					case 2:
						System.out.print("{"+args[0]+"}%{"+args[1]+"}");
						break;
					case 3:
						System.out.print("{"+args[0]+"}/{"+args[1]+"}");
						break;
					case 4:
						System.out.print("{"+args[0]+"}*{"+args[1]+"}");
						break;
					case 5:
						System.out.print("{"+args[0]+"}-{"+args[1]+"}");
						break;
					case 6:
						System.out.print("{"+args[0]+"}+{"+args[1]+"}");
						break;
					case 7:
						System.out.print("{"+args[0]+"}^{"+args[1]+"}");
						break;
					case 8:
						System.out.print("{"+args[0]+"}|{"+args[1]+"}");
						break;
					case 9:
						System.out.print("{"+args[0]+"}&{"+args[1]+"}");
						break;
					case 10:
						System.out.print("{"+args[0]+"}=={"+args[1]+"}");
						break;
					case 11:
						System.out.print("{"+args[0]+"}!={"+args[1]+"}");
						break;
					case 12:
						System.out.print("{"+args[0]+"}<{"+args[1]+"}");
						break;
					case 13:
						System.out.print("{"+args[0]+"}>{"+args[1]+"}");
						break;
					case 14:
						System.out.print("{"+args[0]+"}<={"+args[1]+"}");
						break;
					case 15:
						System.out.print("{"+args[0]+"}>={"+args[1]+"}");
						break;
					case 16:
						System.out.print("{"+args[0]+"}={"+args[1]+"}");
						break;
					case 17:
						System.out.print("{"+args[0]+"}+={"+args[1]+"}");
						break;
					case 18:
						System.out.print("{"+args[0]+"}+={"+args[1]+"}");
						break;
					case 19:
						System.out.print("!{"+args[0]+"}");
						break;
					case 20:
						System.out.print("{"+args[0]+"}&&{"+args[1]+"}");
						break;
					case 21:
						System.out.print("{"+args[0]+"}||{"+args[1]+"}");
						break;
					case 22:
						System.out.print("{"+args[0]+"}^^{"+args[1]+"}");
						break;
				}
				System.out.print("   ");
			} else if (kind==2) {
				String comma = null;
				switch (id) {
					case 0:
						System.out.print("do(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							System.out.print(comma + "{"+args[k]+"}");
							comma = ",";
						}
						System.out.print(");  ");
						break;
					case 1:
						System.out.print("begin (unexpected)");
						break;
					case 2:
						System.out.print("end (unexpected)");
						break;
					case 3:
						System.out.println("return(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							System.out.print(comma + "{"+args[k]+"}");
							comma = ",";
						}
						System.out.print(");  ");
						break;
					case 4:
						System.out.print("if {"+args[0]+"} then {"+args[1]+"} else {"+args[2]+"}  ");
						break;
					case 5:
						System.out.print("then(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							System.out.print(comma + "{"+args[k]+"}");
							comma = ",";
						}
						System.out.print(");  ");
						break;
					case 6:
						System.out.print("else(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							System.out.print(comma + "{"+args[k]+"}");
							comma = ",";
						}
						System.out.print(");  ");
						break;
					case 7:
						System.out.print("for(var[{"+args[0]+"}]L i={"+args[1]+"}; i!={"+args[2]+"}; i+={"+args[3]+"}) do {"+args[4]+"};  ");
						break;
					case 10:
						System.out.print("while({"+args[0]+"}) do {"+args[1]+"}  ");
						break;
					case 11:
						System.out.print("break {"+args[0]+"}  ");
						break;
					case 12:
						System.out.print("continue {"+args[0]+"}  ");
						break;
					case 13:
						System.out.print("exit_script();  ");
						break;
					case 14:
						System.out.print("exit_returning("+args[0]+"});  ");
						break;
					case 15:
						System.out.print("switch(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							System.out.print(comma + "{"+args[k]+"}");
							comma = ",";
						}
						System.out.print(");  ");
						break;
					case 16:
						System.out.print("case (unexpected)");
						break;
				}
			} else if (kind==6) {
				System.out.print("built-in[{"+id+"}](");
				String comma = "";
				for (int k=0; k<args.length; k++) {
					System.out.print(comma + "{"+args[k]+"}");
					comma = ",";
				}
				System.out.print(");  ");
			} else {
				//Stack trace
				System.out.print("????   ");
				System.out.print(kind + ":" + id + " ");
				if (args!=null) {
					System.out.print("[");
					String comma = "";
					for (int k=0; k<args.length; k++) {
						System.out.print(comma + args[k]);
						comma = ",";
					}
					System.out.print("]");
				}
			}
			System.out.println();
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
			}
		});
		
		//Right paenl
		scriptContentsPnl = new JPanel();
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
			JScrollPane jsp = new JScrollPane(scriptNameLst);
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
			gbc.weightx = 0.75;
			gbc.weighty = 1.0;
			cp.add(scriptContentsPnl, gbc);
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
	
	public static void main(String[] args) {
		new HSP2HF();
	}
}

