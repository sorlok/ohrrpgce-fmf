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
	private JButton compileBtn;
	private JPanel scriptContentsPnl;
	private int flipflop = 1;
	
	//Scripts
	private DefaultListModel scriptNameModel;
	private Hashtable idToName;
	private Script currScript;
	
	//Sources
	//private ScriptSourceRenderer scriptSrcModel;
	//private ArrayList scriptSrcInOrder;
	private JScrollPane srcScrollPnl;
	private Hashtable atIDToScriptSrc;
	private ScriptSrc currScriptSrc;
	
	//See end of file (too messy)
	//private static final String[] built_in = {}
	
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
	
	
	private void crossCompileScript() {
		//First, verify
		ScriptSrc sID0 = null;
		Object[] scripts = atIDToScriptSrc.values().toArray();
		boolean oneError = false;
		for (int i=0; i<scripts.length; i++) {
			ScriptSrc s = (ScriptSrc)scripts[i];
			if (s.getArgs()!=null)  {
				for (int k=0; k<s.getArgs().length; k++) {
					if (atIDToScriptSrc.get(new Integer(s.getArgs()[k]))==null) {
						//Error
						s.flashError(true);
						oneError = true;
					}
				}
			}
			if (s.atID==0)
				sID0 = s;
			else
				System.out.println(s.toHFString(true));
		}
		
		if (oneError) {
			System.out.println("Error! ID out of range...");
			scriptContentsPnl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.red), "Source"));
			return;
		}
		
		System.out.println("\n#Init local variables");
		for (int i=currScript.args.length-1; i>=0; i--) {
			System.out.print(""+i);
			System.out.print("   swap");
			System.out.println("   @L[]");
		}
		
		System.out.println("\n#Main script loop");
		System.out.println(sID0.toHFString(false));
		
		System.out.println("Ready to begin!");
		
	}
	
	
	private void loadScriptSource(byte[] data) {
		//Assume format 2
		//System.out.println("Testing");
		int byteOffsetOfHeader = ((data[0]&0xFF) + ((data[1]<<8)&0xFF00));
		System.out.println("  Header offset in bytes: " + byteOffsetOfHeader);
		
		/*System.out.println("  Script Variables: " + (data[2] + (data[3]<<8)));
		System.out.println("  Script Arguments: " + (data[4] + (data[5]<<8)));
		System.out.println("  Script Format Version: " + (data[6] + (data[7]<<8)));
		System.out.println("  String Table Offset: " + Integer.toHexString(data[8]) + "  " + Integer.toHexString(data[9]) + "  " + Integer.toHexString(data[10]) + "  " + Integer.toHexString(data[11]));*/
		
		atIDToScriptSrc.clear();
		currScriptSrc = null;
		scriptContentsPnl.removeAll();
		int offsetWords = 0;
		for (int i=byteOffsetOfHeader; i<data.length;) {
			//Read kind, ID, length, & args; advance pointers.
			int kind = (data[i++]&0xFF) | ((((int)data[i++])*0x100)&0xFF00) | ((((int)data[i++])*0x10000)&0xFF0000) | (((int)(data[i++])*0x1000000)&0xFF000000);
		    int id = (data[i++]&0xFF) | ((((int)data[i++])*0x100)&0xFF00) | ((((int)data[i++])*0x10000)&0xFF0000) | (((int)(data[i++])*0x1000000)&0xFF000000);
		    int startsAt = offsetWords;
		    offsetWords += 2;
			int[] args =  null;
			if (kind==2 || kind==5 || kind==6 || kind==7) {
				args = new int[(data[i++]&0xFF) | ((((int)data[i++])*0x100)&0xFF00) | ((((int)data[i++])*0x10000)&0xFF0000) | (((int)(data[i++])*0x1000000)&0xFF000000)];
				offsetWords++;
			} else if (kind==1 || kind==3 || kind==4)
				args = null;
			else {
				System.out.println("Invalid kind: " + kind);
				System.exit(1);
			}
			if (args!=null) {
				for (int k=0; k<args.length; k++) {
					args[k] = (data[i++]&0xFF) | ((((int)data[i++])*0x100)&0xFF00) | ((((int)data[i++])*0x10000)&0xFF0000) | (((int)(data[i++])*0x1000000)&0xFF000000);
					offsetWords++;
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
				srcSnippet.append("<span style='font-weight: bold; color: #0000CC;'>"+id+"</span>");
			} else if (kind==3) {
				srcSnippet.append("var[" + id + "]G");
			} else if (kind==4) {
				srcSnippet.append("var[" + id + "]L");
			} else if (kind==7) {
				Script scr = (Script)idToName.get(new Integer(id));
				srcSnippet.append("<a href=S"+scr.id+">"+scr.name + "</a>(");
				String comma = "";
				for (int k=0; k<args.length; k++) {
					srcSnippet.append(comma + "{<a href="+args[k]+">"+args[k]+"</a>}");
					comma = ",";
				}
				srcSnippet.append(");");
			} else if (kind==5) {
				switch (id) {
					case 0:
						srcSnippet.append("rand({<a href="+args[0]+">"+args[0]+"</a>}, {<a href="+args[1]+">"+args[1]+"</a>});");
						break;
					case 1:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}**{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 2:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}%{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 3:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}/{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 4:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}*{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 5:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}-{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 6:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}+{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 7:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}^{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 8:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}|{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 9:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}&{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 10:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}=={<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 11:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}!={<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 12:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}&lt;{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 13:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}&gt;{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 14:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}&lt;={<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 15:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}&gt;={<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 16:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}={<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 17:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}+={<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 18:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}-={<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 19:
						srcSnippet.append("!{<a href="+args[0]+">"+args[0]+"</a>}");
						break;
					case 20:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}&amp;&amp;{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 21:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}||{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 22:
						srcSnippet.append("{<a href="+args[0]+">"+args[0]+"</a>}^^{<a href="+args[1]+">"+args[1]+"</a>}");
						break;
				}
			} else if (kind==2) {
				String comma = null;
				switch (id) {
					case 0:
						srcSnippet.append("do(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{<a href="+args[k]+">"+args[k]+"</a>}");
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
							srcSnippet.append(comma + "<a href="+args[k]+">{"+args[k]+"</a>}");
							comma = ",";
						}
						srcSnippet.append(");  ");
						break;
					case 4:
						srcSnippet.append("if {<a href="+args[0]+">"+args[0]+"</a>} then {<a href="+args[1]+">"+args[1]+"</a>} else {<a href="+args[2]+">"+args[2]+"</a>}");
						break;
					case 5:
						srcSnippet.append("then(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{<a href="+args[k]+">"+args[k]+"</a>}");
							comma = ",";
						}
						srcSnippet.append(");");
						break;
					case 6:
						srcSnippet.append("else(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{<a href="+args[k]+">"+args[k]+"</a>}");
							comma = ",";
						}
						srcSnippet.append(");");
						break;
					case 7:
						srcSnippet.append("for(var[{<a href="+args[0]+">"+args[0]+"</a>}] = {<a href="+args[1]+">"+args[1]+"</a>}; var!={<a href="+args[2]+">"+args[2]+"</a>}; var+={<a href="+args[3]+">"+args[3]+"</a>}) do {<a href="+args[4]+">"+args[4]+"</a>};");
						break;
					case 10:
						srcSnippet.append("while({<a href="+args[0]+">"+args[0]+"</a>}) do {<a href="+args[1]+">"+args[1]+"</a>}");
						break;
					case 11:
						srcSnippet.append("break {<a href="+args[0]+">"+args[0]+"</a>}");
						break;
					case 12:
						srcSnippet.append("continue {<a href="+args[0]+">"+args[0]+"</a>}");
						break;
					case 13:
						srcSnippet.append("exit_script();");
						break;
					case 14:
						srcSnippet.append("exit_returning({<a href="+args[0]+">"+args[0]+"</a>});");
						break;
					case 15:
						srcSnippet.append("switch(");
						comma = "";
						for (int k=0; k<args.length; k++) {
							srcSnippet.append(comma + "{<a href="+args[k]+">"+args[k]+"</a>}");
							comma = ",";
						}
						srcSnippet.append(");");
						break;
					case 16:
						srcSnippet.append("case (unexpected)");
						break;
				}
			} else if (kind==6) {
				if (id<built_in.length && id>=0)
					srcSnippet.append("<span style='font-weight: bold;'>"+built_in[id]+"</span>(");
				else
					srcSnippet.append("<span style='font-weight: bold;'>built_in["+id+"]</span>(");
				String comma = "";
				for (int k=0; k<args.length; k++) {
					srcSnippet.append(comma + "{<a href="+args[k]+">"+args[k]+"</a>}");
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
			ScriptSrc curr = new ScriptSrc(startsAt, atIDToScriptSrc.size(), kindName, srcSnippet.toString(), kind, id, args);
			atIDToScriptSrc.put(new Integer(curr.atID), curr);
			scriptContentsPnl.add(curr);
		}
	}
	

	
	private void loadScriptSrc(Script s) {
		System.out.println("\n" + s.name);
		currScript = s;
		loadScriptSource(s.scriptSrc.data);
		
		//un-scroll
		srcScrollPnl.getVerticalScrollBar().setValue(0);
		
		//Reset size...
		setSize(new Dimension(getWidth(), getHeight()+flipflop)); //hackish...
		flipflop *= -1;
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
	
		compileBtn = new JButton("Cross-Compile");
		compileBtn.setEnabled(false);
		compileBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				crossCompileScript();
			}
		});
		toolbarPnl.add(compileBtn);
		
		//Left panel
		scriptNameModel = new DefaultListModel();
		scriptNameLst = new JList(scriptNameModel);
		scriptNameLst.setLayoutOrientation(JList.VERTICAL);
		scriptNameLst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptNameModel.addElement("(no scripts)");
		scriptNameLst.setBackground(toolbarPnl.getBackground());
		scriptNameLst.setFont(new Font("Arial", Font.PLAIN, 12));
		scriptNameLst.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Scripts"));
		scriptNameLst.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				//Only deal with the last change.
				if (e.getValueIsAdjusting())
					return;
				
				//Now, load that into the panel
				loadScriptSrc((Script)scriptNameLst.getSelectedValue());
				compileBtn.setEnabled(true);
				scriptContentsPnl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Source"));
			}
		});
		
		//Right panel
		scriptContentsPnl = new JPanel();
		scriptContentsPnl.setLayout(new GridLayout(0, 1));
		scriptContentsPnl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Source"));
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
			srcScrollPnl = new JScrollPane(jp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			srcScrollPnl.getVerticalScrollBar().setUnitIncrement(50);
			cp.add(srcScrollPnl, gbc);
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
		
		//For compiling
		private int hsz_kind;
		private int hsz_id;
		private int[] hsz_args;
		private ArrayList hf_commands;
		
		private JEditorPane scriptSrcLbl;
		private JPanel upperPnlLeft;
		
		public ScriptSrc(int atID, int realID, String kindName, String srcSnippet, int hsz_kind, int hsz_id, int[] hsz_args) {
			this.atID = atID;
			this.realID = realID;
			this.kindName = kindName;
			this.srcSnippet = srcSnippet;
			initPnl();
			
			this.hsz_kind = hsz_kind;
			this.hsz_id = hsz_id;
			this.hsz_args = hsz_args;
			initCompiler();
		}
		
		public void flashID(boolean on) {
			Color c = Color.yellow;
			if (!on)
				c = this.getBackground();
			
			upperPnlLeft.setBackground(c);
		}
		
		public void flashError(boolean on) {
			Color c = Color.red;
			if (!on)
				c = this.getBackground();
			
			upperPnlLeft.setBackground(c);
		}
		
		
		private void initCompiler() {
			//Prepare our source
			hf_commands = new ArrayList();
			switch(hsz_kind) {
				case 1:
					//"Number"
					hf_commands.add(""+hsz_id);
					break;
				case 2:
					//"Flow Control"
					switch (hsz_id) {
						case 0:
							//"do"
							hf_commands.add("do_start");
							for (int i=0; i<hsz_args.length; i++)
								hf_commands.add("\\LOC"+hsz_args[i]+"()");
							hf_commands.add("do_end");
							break;
						default:
							System.out.println("Still deciding on flow commands!");
					}
					break;
				case 3:
					hf_commands.add(""+hsz_id);
					hf_commands.add("G[]@");
					break;
				case 4:
					hf_commands.add(""+hsz_id);
					hf_commands.add("L[]@");
					break;
				case 5:
					char lgMod = 'L';
					int temp = hsz_args[0];
					if (hsz_id==16 && hsz_args[0]<0) {
						hsz_args[0] = -hsz_args[0]-1;
						lgMod = 'G';
					}
					
					for (int i=0; i<hsz_args.length; i++)
						hf_commands.add("\\LOC"+hsz_args[i]+"()");
					
					hsz_args[0] = temp;
					
					switch (hsz_id) {
						case 0:
							hf_commands.add("random");
							break;
						case 1:
							hf_commands.add("<undefined>");
							break;
						case 2:
							hf_commands.add("<undefined>");
							break;
						case 3:
							hf_commands.add("/");
							break;
						case 4:
							hf_commands.add("*");
							break;
						case 5:
							hf_commands.add("-");
							break;
						case 6:
							hf_commands.add("+");
							break;
						case 7:
							hf_commands.add("<undefined>");
							break;
						case 8:
							hf_commands.add("<undefined>");
							break;
						case 9:
							hf_commands.add("<undefined>");
							break;
						case 10:
							hf_commands.add("<undefined>");
							break;
						case 11:
							hf_commands.add("<undefined>");
							break;
						case 12:
							hf_commands.add("<undefined>");
							break;
						case 13:
							hf_commands.add("<undefined>");
							break;
						case 14:
							hf_commands.add("<undefined>");
							break;
						case 15:
							hf_commands.add("<undefined>");
							break;
						case 16:
							hf_commands.add("@"+lgMod+"[]");
							break;
						case 17:
							hf_commands.add("<undefined>");
							break;
						case 18:
							hf_commands.add("<undefined>");
							break;
						case 19:
							hf_commands.add("<undefined>");
							break;
						case 20:
							hf_commands.add("<undefined>");
							break;
						case 21:
							hf_commands.add("<undefined>");
							break;
						case 22:
							hf_commands.add("<undefined>");
							break;
					}
					break;
				case 6:
					for (int i=0; i<hsz_args.length; i++)
						hf_commands.add("\\LOC"+hsz_args[i]+"()");
					
					hf_commands.add("\\BLT"+hsz_id+"()");
					break;
				case 7:
					for (int i=0; i<hsz_args.length; i++)
						hf_commands.add("\\LOC"+hsz_args[i]+"()");
					
					hf_commands.add("\\LOC"+hsz_id+"()");
					break;
			}
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
							int x = 0;
							boolean script = false;
							if (ev.getDescription().charAt(0)=='S') {
								x = Integer.parseInt(ev.getDescription().substring(1));
								script = true;
							} else {
								x = Integer.parseInt(ev.getDescription());
							}
							
							if (script)
								loadScriptSrc((Script)idToName.get(new Integer(x)));
							else
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
		public int[] getArgs() {
			return hsz_args;
		}
		public String toHFString(boolean wrap) {
			StringBuilder sb = new StringBuilder();
			if (wrap)
				sb.append("\\LOC"+atID+"{\n");
			for (int i=0; i<hf_commands.size(); i++) {
				if (wrap)
					sb.append("  ");
				sb.append(hf_commands.get(i).toString()+"\n");
			}
			if (wrap)
				sb.append("}\n");
			return sb.toString();
		}
	}
	
	
	public static void main(String[] args) {
		new HSP2HF();
	}
	
	
	private static final String[] built_in = {"noop", "wait", "waitforall", 
		"waitforhero", "waitfornpc", "suspendnpcs", "suspendplayer", "resumenpcs", 
		"resumeplayer", "waitforkey", "walkhero", "showtextbox", "checktag", "settag", 
		"#INVALID", "usedoor", "fightformation", "getitem", "deleteitem", "leader", 
		"getmoney", "losemoney", "paymoney", "unequip", "forceequip", "setheroframe", 
		"setNPCframe", "suspendoverlay", "playsong", "stopsong", "keyval", 
		"rankincaterpillar", "showbackdrop", "showmap", "dismountvehicle", "useNPC", 
		"#INVALID", "useshop", "camerafollowshero", "camerafollowsnpc", "pancamera", 
		"focuscamera", "waitforcamera", "herox", "heroy", "npcx", "npcy", 
		"suspendobstruction", "resumeobstruction", "suspendherowalls", 
		"suspendNPCwalls", "resumeherowalls", "walknpc", "setherodirection", 
		"setnpcdirection", "getdefaultweapon", "setdefaultweapon", 
		"suspendcatapillar", "resumecatapillar", "waitfortextbox", "equipwhere", 
		"teleporttomap", "suspendrandomenemys", "resumerandomenemys", "getherostat", 
		"resumeoverlay", "addhero", "deletehero", "swapouthero", "swapinhero", 
		"roominactiveparty", "lockhero", "unlockhero", "gameover", "setdeathscript", 
		"fadescreenout", "fadescreenin", "showvalue", "alterNPC", "shownovalue", 
		"currentmap", "setherospeed", "inventory", "setherostat", "suspendboxadvance", 
		"resumeboxadvance", "advancetextbox", "setheroposition", "setNPCposition", 
		"swapbyposition", "findhero", "checkequipment", "daysofplay", "hoursofplay", 
		"minutesofplay", "resumeNPCwalls", "setheroz", "readmapblock", 
		"writemapblock", "readpassblock", "writepassblock", "NPCdirection,", 
		"herodirection", "resetpalette", "tweakpalette", "readcolor", "writecolor", 
		"updatepalette", "seedrandom", "greyscalepalette", "setheropicture", 
		"setheropalette", "getheropicture", "getheropalette", "readglobal", 
		"writeglobal", "heroiswalking", "NPCiswalking", "suspendcaterpillar", 
		"resumecaterpillar", "NPCreference", "NPCatspot", "getNPCID", "NPCcopycount", 
		"changeNPCID", "createNPC", "destroyNPC", "teachspell", "forgetspell", 
		"readspell", "writespell", "knowsspell", "canlearnspell", "herobyslot", 
		"herobyrank", "puthero", "putnpc", "putcamera", "heropixelx", "heropixely", 
		"npcpixelx", "npcpixely", "camerapixelx", "camerapixely", "loadtileset", 
		"pickhero", "renameherobyslot", "readgeneral", "writegeneral", "#INVALID", 
		"statusscreen", "showminimap", "spellsmenu", "itemsmenu", "equipmenu", 
		"savemenu", "#INVALID", "ordermenu", "teammenu", "initmouse", "mousepixelx", 
		"mousepixely", "mousebutton", "putmouse", "mouseregion", "npcatpixel", 
		"saveinslot", "lastsaveslot", "suspendrandomenemies", "resumerandomenemies", 
		"savemenu", "saveslotused", "importglobals", "exportglobals", "loadfromslot", 
		"deletesave", "runscriptbyid", "NPCiswalking", "readgmap", "writegmap", 
		"mapwidth", "mapheight", "readNPC", "setherolevel", "giveexperience", 
		"herolevelled", "spellslearnt", "getmusicvolume", "setmusicvolume", 
		"getformationsong", "setformationsong", "heroframe", "npcframe", "npcextra", 
		"setnpcextra", "loadsound", "freesound", "playsound", "pausesound", 
		"stopsound", "system", "system", "system", "current", "get", "set", "get", 
		"get", "get", "get", "showstring", "clearstring", "appendascii", 
		"appendnumber", "copystring", "concatenatestrings", "stringlength", 
		"deletechar", "replacechar", "asciifromstring", "positionstring", 
		"setstringbit", "getstringbit", "stringcolor", "stringx", "stringy", 
		"systemday", "systemmonth", "systemyear", "stringcompare", "readenemydata", 
		"writeenemydata", "trace", "getsongname", "loadmenu", "keyispressed"
	};
}

