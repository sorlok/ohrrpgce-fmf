package ohrrpgce.tool;

import java.awt.*;
import javax.swing.*;

public class HSP2HF extends JFrame {

	private JPanel toolbarPnl;
	private JList scriptNameLst;
	private JPanel scriptContentsPnl;
	
	
	public HSP2HF() {
		//Init
		super("Hamsterspeak Analysis Engine");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		
		this.initComponents();
		this.layoutComponents(this.getContentPane());
		
		this.setVisible(true);
	}
	
	
	
	
	

	private void initComponents() {
		//Our top panel
		toolbarPnl = new JPanel(new FlowLayout());
		toolbarPnl.setBackground(Color.red);
		JButton bt = new JButton("Open");
		bt.setPreferredSize(new Dimension(200, 200));
		toolbarPnl.add(bt);
		
		//Left panel
		scriptNameLst = new JList();
		scriptNameLst.add("name 1");
		scriptNameLst.add("name 2");
		scriptNameLst.setBackground(Color.green);
		scriptNameLst.setBorder(BorderFactory.createTitledBorder("Scripts"));
		
		//Right paenl
		scriptContentsPnl = new JPanel();
		scriptContentsPnl.setBackground(Color.blue);
		scriptContentsPnl.setBorder(BorderFactory.createTitledBorder("Source"));
	}
	
	private void layoutComponents(Container cp) {
		GridBagConstraints gbc = new GridBagConstraints();
		
		//Top panel
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.ipadx = 2;
		gbc.ipady = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.weightx = 1.0
		gbc.weighty = 0.0;
		cp.add(toolbarPnl, gbc);
		
		//Left panel
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.ipadx = 2;
		gbc.ipady = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.weightx = 0.25
		gbc.weighty = 1.0;
		cp.add(scriptNameLst, gbc);
		
		//Right panel
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipadx = 2;
		gbc.ipady = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.weightx = 0.75
		gbc.weighty = 1.0;
		cp.add(scriptContentsPnl, gbc);
	}
	
	
	public static void main(String[] args) {
		new HSP2HF();
	}
}

