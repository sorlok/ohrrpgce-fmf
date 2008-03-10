package ohrrpgce.simple;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import ohrrpgce.simple.SimpleFontConverter.FontPropListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class SimpleFontConverter implements FontPropListener {
	//Fancy stuff
	private Shell mainShell;
	private FontPreview fPrev;
	
	//Normal stuff
	private Label fontLbl;
	private Label sizeLbl;
	private Label charLbl;
	private Label marginsLbl;
	private Label margLeftLbl;
	private Label margRightLbl;
	private Label margUpLbl;
	private Label margDownLbl;
	private Label previewLbl;
	private Label offsetLbl;
	private Label xOffLabel;
	private Label yOffLabel;
	private Label threshLbl;
	private Combo currFont;
	private Combo currSize;
	private Text enteredChars;
	private Text offsetAmt;
	private Text leftMargin;
	private Text rightMargin;
	private Text upMargin;
	private Text downMargin;
	private Text previewChar;
	private Text threshVal;
	private Text xOffVal;
	private Text yOffVal;
	private Button saveBMP;
	private Button saveFNT;
	
	private static final int PIX_PER_CHAR = 8;

	public void propertyChanged(FONT_PROP propChanged) {
		switch (propChanged) {
		case BOTTOM_MARGIN:
		case TOP_MARGIN:
		case LEFT_MARGIN:
		case RIGHT_MARGIN:
		case PREVIEW_CHAR:
		case FONT:
		case SIZE:
		case THRESHHOLD:
		case X_OFF:
		case Y_OFF:
			fPrev.fullRepaint();
		}
	}
	
	public SimpleFontConverter() {
		//Set up our SWT application
		Display disp = new Display();
		mainShell = new Shell(disp);
		mainShell.setText("Font Changerator");
		mainShell.setLayout(new FormLayout());
		
		//Add the other components
		loadComponents();
		layoutComponents();
		mainShell.pack();
		
		//For disposing
		mainShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeAll();
			}
		});
		
		//For resizing
		mainShell.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				fPrev.handleResize();
			}
		});
		
		//Display functionality
		mainShell.open();
		fPrev.handleResize();
		
		//This should DEFINITELY be in the model.
		fPrev.suppressPaints(true);
		previewChar.setText("S");
		fPrev.setPrevChar("S");
		fPrev.setCurrMarginLeft(0);
		fPrev.setCurrMarginTop(0);
		fPrev.setCurrMarginRight(1);
		fPrev.setCurrMarginBottom(1);
		fPrev.setCurrThreshhold(0.15F);
		threshVal.setText(fPrev.getCurrThreshhold()+"");		
		fPrev.setXOff(0);
		xOffVal.setText(fPrev.getXOff()+"");
		fPrev.setYOff(0);
		yOffVal.setText(fPrev.getYOff()+"");
		currFont.select(7);
		fPrev.setCurrFontName(currFont.getText());
		currSize.select(3);
		int size = 12;
		try {
			size = Integer.parseInt(currSize.getText());
		} catch (NumberFormatException ex) {
			System.out.println("Error with font size: " + currSize);
		}
		fPrev.setCurrFontSize(size);
		fPrev.suppressPaints(false);
		fPrev.fullRepaint();
		 		
		//SWT basic loop
		while (!mainShell.isDisposed()) {
			if (!disp.readAndDispatch())  {
				disp.sleep();
			}
		}
		mainShell.dispose();
		disp.dispose();
	}
	
	
	private void loadComponents() {
		fontLbl = new Label(mainShell, SWT.HORIZONTAL);
		fontLbl.setText("Font: ");
		
		sizeLbl = new Label(mainShell, SWT.HORIZONTAL);
		sizeLbl.setText("Size: ");
		
		charLbl = new Label(mainShell, SWT.HORIZONTAL);
		charLbl.setText("Characters: ");

		offsetLbl = new Label(mainShell, SWT.HORIZONTAL);
		offsetLbl.setText("Offset: ");
		
		marginsLbl = new Label(mainShell, SWT.HORIZONTAL);
		marginsLbl.setText("Margins: ");
		
		margLeftLbl = new Label(mainShell, SWT.HORIZONTAL);
		margLeftLbl.setText(" px (left");
		
		margRightLbl = new Label(mainShell, SWT.HORIZONTAL);
		margRightLbl.setText(" px (right)");
		
		margUpLbl = new Label(mainShell, SWT.HORIZONTAL);
		margUpLbl.setText(" px (top)");
		
		margDownLbl = new Label(mainShell, SWT.HORIZONTAL);
		margDownLbl.setText(" px (bottom)");
		
		previewLbl = new Label(mainShell, SWT.HORIZONTAL);
		previewLbl.setText("Preview: ");
		
		threshLbl = new Label(mainShell, SWT.HORIZONTAL);
		threshLbl.setText("Threshhold: ");
		
		xOffLabel = new Label(mainShell, SWT.HORIZONTAL);
		xOffLabel.setText("X-Off:");
		
		yOffLabel = new Label(mainShell, SWT.HORIZONTAL);
		yOffLabel.setText("Y-Off:");
		
		currFont = new Combo(mainShell, SWT.READ_ONLY|SWT.DROP_DOWN);
		for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
			currFont.add(name);
		currFont.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				fPrev.setCurrFontName(currFont.getText());
			}
		});
		
		currSize = new Combo(mainShell, SWT.DROP_DOWN);
		currSize.add("16");
		currSize.add("24");
		currSize.add("32");
		currSize.add("42");
		currSize.add("70");
		currSize.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int res = -1;
				try {
					res = Integer.parseInt(currSize.getText());
				} catch (NumberFormatException ex) {
					currSize.setText(fPrev.getCurrFontSize()+"");
					return;
				}
				fPrev.setCurrFontSize(res);
			}
		});
		currSize.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				int res = -1;
				try {
					res = Integer.parseInt(currSize.getText());
				} catch (NumberFormatException ex) {
					currSize.setText(fPrev.getCurrFontSize()+"");
					return;
				}
				fPrev.setCurrFontSize(res);				
			};
		});
		
		enteredChars = new Text(mainShell, SWT.MULTI|SWT.BORDER);
		enteredChars.setText( "! \" # $ % & ' ( ) \n* + , - . /"
				+ "\n 0 1 2 3 4 5 6 7 8 9"
				+ " \n: ; < = > ? @"
				+ " \nA B C D E F G H I J K\n L M N O P Q R S T\n U V W X Y Z"
				+ "\n[ \\ ] ^ _ `"
				+ " \na b c d e f g h i j k \nl m n o p q r s t\n u v w x y z"
				+ " \n{ | } ~"
				);
		
		offsetAmt = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		offsetAmt.setText("33");
		offsetAmt.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				try {
					Integer.parseInt(offsetAmt.getText());
				} catch (NumberFormatException ex) {
					//Bad to default, but whatever..
					offsetAmt.setText("33");
					return;
				}				
			};
		});
		
		leftMargin = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		leftMargin.setText("0");
		leftMargin.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				int res = -1;
				try {
					res = Integer.parseInt(leftMargin.getText());
				} catch (NumberFormatException ex) {
					leftMargin.setText(fPrev.getCurrMarginLeft()+"");
					return;
				}
				fPrev.setCurrMarginLeft(res);				
			};
		});

		upMargin = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		upMargin.setText("0");
		upMargin.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				int res = -1;
				try {
					res = Integer.parseInt(upMargin.getText());
				} catch (NumberFormatException ex) {
					upMargin.setText(fPrev.getCurrMarginTop()+"");
					return;
				}
				fPrev.setCurrMarginTop(res);
			}
		});
		
		rightMargin = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		rightMargin.setText("1");
		rightMargin.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				int res = -1;
				try {
					res = Integer.parseInt(rightMargin.getText());
				} catch (NumberFormatException ex) {
					rightMargin.setText(fPrev.getCurrMarginRight()+"");
					return;
				}
				fPrev.setCurrMarginRight(res);
			}
		});

		downMargin = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		downMargin.setText("1");
		downMargin.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				int res = -1;
				try {
					res = Integer.parseInt(downMargin.getText());
				} catch (NumberFormatException ex) {
					downMargin.setText(fPrev.getCurrMarginBottom()+"");
					return;
				}
				fPrev.setCurrMarginBottom(res);
			}
		});

		previewChar = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		previewChar.setText("X X");
		previewChar.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				fPrev.setPrevChar(previewChar.getText());
			}
		});
		
		threshVal = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		threshVal.setText("0.999");
		threshVal.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				float val = 0.0F;
				try {
					val = Float.parseFloat(threshVal.getText());
				} catch (NumberFormatException ex) {
					threshVal.setText(fPrev.getCurrThreshhold()+"");
					return;
				}
				fPrev.setCurrThreshhold(val);
			}
		});
		
		xOffVal = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		xOffVal.setText("99");
		xOffVal.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				int val = 0;
				try {
					val = Integer.parseInt(xOffVal.getText());
				} catch (NumberFormatException ex) {
					xOffVal.setText(fPrev.getXOff()+"");
					return;
				}
				fPrev.setXOff(val);
			}
		});
		
		yOffVal = new Text(mainShell, SWT.SINGLE|SWT.BORDER);
		yOffVal.setText("99");
		yOffVal.addFocusListener(new FocusListener() {
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {};
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
				int val = 0;
				try {
					val = Integer.parseInt(yOffVal.getText());
				} catch (NumberFormatException ex) {
					yOffVal.setText(fPrev.getYOff()+"");
					return;
				}
				fPrev.setYOff(val);
			}
		});
		
		saveBMP = new Button(mainShell, SWT.PUSH);
		saveBMP.setText("Export (BMP)");
		saveBMP.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				saveFont(false);
			}
		});
		
		saveFNT = new Button(mainShell, SWT.PUSH);
		saveFNT.setText("Save (FNT");
		saveFNT.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				saveFont(true);
			}
		});
		
		fPrev = new FontPreview(mainShell);
		fPrev.addFontPropertyListener(this);
	}
	
	
	private void layoutComponents() {
		FormData fd = null;
		
		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 5);
		fontLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(fontLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(fontLbl, 5, SWT.RIGHT);
		currFont.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(currFont, 5);
		fd.left = new FormAttachment(currFont, 0, SWT.LEFT);
		currSize.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(currSize, 0, SWT.TOP);
		fd.left = new FormAttachment(fontLbl, 0, SWT.LEFT);
		sizeLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(currSize, 5);
		fd.left = new FormAttachment(charLbl, 5);
		enteredChars.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(enteredChars, 0, SWT.TOP);
		fd.left = new FormAttachment(sizeLbl, 0, SWT.LEFT);
		charLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(enteredChars, 5);
		fd.left = new FormAttachment(0, 5);
		offsetLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(offsetLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(offsetLbl, 5, SWT.RIGHT);
		offsetAmt.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(fontLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(currFont, 20);
		marginsLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(marginsLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(marginsLbl, 5);
		leftMargin.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(leftMargin, 0, SWT.TOP);
		fd.left = new FormAttachment(leftMargin, 5);
		margLeftLbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(leftMargin, 5);
		fd.left = new FormAttachment(leftMargin, 0, SWT.LEFT);
		rightMargin.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(rightMargin, 0, SWT.TOP);
		fd.left = new FormAttachment(rightMargin, 5);
		margRightLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(rightMargin, 5);
		fd.left = new FormAttachment(rightMargin, 0, SWT.LEFT);
		upMargin.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(upMargin, 0, SWT.TOP);
		fd.left = new FormAttachment(upMargin, 5);
		margUpLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(upMargin, 5);
		fd.left = new FormAttachment(upMargin, 0, SWT.LEFT);
		downMargin.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(downMargin, 0, SWT.TOP);
		fd.left = new FormAttachment(downMargin, 5);
		margDownLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(margDownLbl, 20);
		fd.left = new FormAttachment(marginsLbl, 0, SWT.LEFT);
		previewLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(previewLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(previewLbl, 5);
		previewChar.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(previewChar, 5);
		fd.left = new FormAttachment(previewLbl, 0, SWT.LEFT);
		fd.right = new FormAttachment(fPrev, 64, SWT.LEFT);
		fd.bottom = new FormAttachment(fPrev, 64, SWT.TOP);
		fPrev.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(previewLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(previewChar, 5);
		threshLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(previewLbl, 0, SWT.TOP);
		fd.left = new FormAttachment(threshLbl, 5);
		threshVal.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(fPrev, 0, SWT.TOP);
		fd.left = new FormAttachment(fPrev, 20);
		xOffLabel.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(xOffLabel, 0, SWT.TOP);
		fd.left = new FormAttachment(xOffLabel, 5);
		xOffVal.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(xOffVal, 5);
		fd.left = new FormAttachment(xOffLabel, 0, SWT.LEFT);
		yOffLabel.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(yOffLabel, 0, SWT.TOP);
		fd.left = new FormAttachment(yOffLabel, 5);
		yOffVal.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(offsetLbl, 20);
		fd.left = new FormAttachment(offsetLbl, 0, SWT.LEFT);
		saveBMP.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(saveBMP, 0, SWT.TOP);
		fd.left = new FormAttachment(saveBMP, 5);
		saveFNT.setLayoutData(fd);
	}
	
	private void disposeAll() {
		safeDispose(fPrev);
		safeDispose(fontLbl);
		safeDispose(sizeLbl);
		safeDispose(charLbl);
		safeDispose(marginsLbl);
		safeDispose(margLeftLbl);
		safeDispose(margRightLbl);
		safeDispose(margUpLbl);
		safeDispose(margDownLbl);
		safeDispose(previewLbl);
		safeDispose(offsetLbl);
		safeDispose(currFont);
		safeDispose(currSize);
		safeDispose(enteredChars);
		safeDispose(offsetAmt);
		safeDispose(leftMargin);
		safeDispose(rightMargin);
		safeDispose(upMargin);
		safeDispose(downMargin);
		safeDispose(previewChar);
		safeDispose(saveBMP);
		safeDispose(saveFNT);
	}
	
	private void safeDispose(Widget toDispose) {
		if (toDispose!=null && !toDispose.isDisposed())
			toDispose.dispose();
	}
	
	private void ws(int val, DataOutputStream ds) throws IOException {
		ds.writeByte(val&0xFF);
		ds.writeByte(val/0x100);
	}
	
	private void saveFont(boolean ohrFormat) {
		FileDialog fd = new FileDialog(mainShell, SWT.SAVE);
		if (ohrFormat) {
			fd.setText("Name the font file");
			fd.setFilterExtensions(new String[] {"*.OHF"});
		} else {
			fd.setText("Name the image file");
			fd.setFilterExtensions(new String[] {"*.png"});
		}
		String res = fd.open();
		
		if (res==null)
			return;
		
		ArrayList<String> letters = new ArrayList<String>();
		for (String s : enteredChars.getText().replace('\n', ' ').replace('\r', ' ').split(" ")) {
			if (s.length()>0)
				letters.add(s);
		}
		
		int offset = 1;
		try {
			offset = Integer.parseInt(offsetAmt.getText());
		} catch (NumberFormatException ex) {}
		
		if (!ohrFormat) {
			//Save the bitmap
			int cols = 14;
			int rows = (int)Math.ceil((letters.size()+offset)/(double)cols);
			
			//System.out.println("Chars: " + letters.size());
			//System.out.println("Rows: " + rows + "  cols: " + cols);
			
			//Horribly slow, but I can't seem to get indexed pictures to work.
			BufferedImage img = new BufferedImage(cols*PIX_PER_CHAR, rows*PIX_PER_CHAR, BufferedImage.TYPE_INT_RGB);
			for (int y=0; y<rows; y++) {
				for (int x=0; x<cols; x++) {
					//For this letter...
					int letterID = x + y*cols;
					
					if (letterID<offset || letterID>=(letters.size()+offset)) {
						//Skip it
						for (int j=0; j<PIX_PER_CHAR; j++) {
							for (int i=0; i<PIX_PER_CHAR; i++) {
								img.setRGB((x*PIX_PER_CHAR+i), (y*PIX_PER_CHAR+j), 0);
							}
						}
					} else {
						//Inform
						//System.out.println(letterID + " of " + (letters.size()+offset));
						
						//Figure it out.
						boolean[][] charInFont = fPrev.fitFont(letters.get(letterID-offset));
						for (int j=0; j<charInFont.length; j++) {
							for (int i=0; i<charInFont[j].length; i++) {
								//System.out.println("x:" + x + "  y: " + y + "  i:" + i + " j:" + j);
								//System.out.println("  :" + (x*PIX_PER_CHAR+i) + ":" + (y*PIX_PER_CHAR+j));
								if (charInFont[j][i])
									img.setRGB((x*PIX_PER_CHAR+i), (y*PIX_PER_CHAR+j), 0xFFFFFF);
								else
									img.setRGB((x*PIX_PER_CHAR+i), (y*PIX_PER_CHAR+j), 0);
							}
						}	
					}
				}
			}
			
			
			//Save the image
			File path = new File(res);
			try {
				ImageIO.write(img, "png", path);
				System.out.println("Saved image: " + path.getAbsolutePath());
			} catch (IOException iex) {
				System.out.println("Error saving image: " + iex.toString());
			}
		} else {
			//Get an outputstream
			DataOutputStream ds = null;
			try {
				ds = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(res))));
			} catch (FileNotFoundException ex) {
				System.out.println("Error on file: " + res);
				return;
			}
			
			//BSAVE header
			try {
				ds.writeByte(0xFD); //Magic value
				ws(0x4242, ds); //Garbage?
				ws(0, ds); //offset
				ws(256*8, ds); //Size
			} catch (IOException ex) {
				System.out.println("Error writing BSAVE header: " + ex.toString());
				return;
			}
			
			//All ASCII characters
			for (int i=0; i<=0xFF; i++) {
				boolean[][] vals = null;
				if (i<offset || i>=(letters.size()+offset)) {
					//Filler
					//System.out.print("Fill");
					vals = new boolean[PIX_PER_CHAR][PIX_PER_CHAR];
					for (int r=0; r<PIX_PER_CHAR; r++) {
						vals[r] = new boolean[PIX_PER_CHAR];
					}
				} else {
					String currLetter = letters.get(i-offset);
					//System.out.print("Letter: " + currLetter);
					vals = fPrev.fitFont(currLetter);
				}
				//System.out.println("(" + vals.length + "," + vals[0].length + ")");
				
				//Now, write it! ...shoot, it's backwards
				try {
					for (int col=0; col<PIX_PER_CHAR; col++) {
						int byteVal = 0;
						int mult = 1;
						for (int row=0; row<PIX_PER_CHAR; row++) {
							int offsetCol = col - fPrev.getCurrMarginLeft();
							int offsetRow = row - fPrev.getCurrMarginTop();
							//Are we still in bounds?
							if (offsetCol>=0 && offsetRow>=0 &&offsetRow<vals.length && offsetCol<vals[row].length) {
								//Append this bit, if necessary.
								if (vals[offsetRow][offsetCol])
									byteVal |= mult;
							}
							mult<<=1;
						}
						
						ds.writeByte(byteVal);
					}
					ds.flush();
				} catch (IOException ex) {
					System.out.println("Error on character: " + ex.toString());
				}
			}
			
			
			//Done
			try {
				ds.close();
			} catch (IOException ex) {
				System.out.println("Error closing file: " + ex.toString());
				return;
			}			
			System.out.println("File saved: " + res);
			System.out.println("(Might be a problem with trailing zeros)");
			
		}
		
		
	}
	
	/**
	 * Such a terrible way to do this...
	 * @author Seth N. Hetu
	 *
	 */
	class FontPreview extends Canvas {
		//Controller
		private ArrayList<FontPropListener> fontChangeListeners;
		
		//Store the model here; it's simple
		private String currFontName;
		private int currFontSize;
		private int currMarginLeft;
		private int currMarginRight;
		private int currMarginTop;
		private int currMarginBottom;
		private float currThreshhold;
		private int xOff;
		private int yOff;
		private String prevChar="";
		
		//Miscelania (sp)
		private boolean firstTime;
		//private boolean justUpdate;
		private boolean dontPaint;
		
		//Cached
		private Image offscreen;
		private Font actualFont;
		private int tileSize;
		
		public FontPreview(Shell parent) {
			super(parent, SWT.NONE);
			
			fontChangeListeners = new ArrayList<FontPropListener>();
			firstTime = true;
						
			this.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					repaint(e.x, e.y, e.width, e.height, e.gc);
				}
			});
				
			handleResize(new Rectangle(0, 0, 1, 1));
		}
		
		private void addFontPropertyListener(FontPropListener listener) {
			fontChangeListeners.add(listener);
		}
		
		private void handleResize(Rectangle r) {
			//Weird Linux bug...
			if (r.width==0 || r.height==0)
				return;
			
			//justUpdate = false;
			redraw();
		}
		
		public void handleResize() {
			handleResize(getClientArea());
		}
		
		public void fullRepaint() {
			//justUpdate = false;
			redraw();
		}
		
		public void suppressPaints(boolean suppress) {
			dontPaint = suppress;
		}
		
		/**
		 * Turns a string into an array of booleans using the current preview size. 
		 * @param chars The string to view
		 * @return The value
		 */
		public boolean[][] fitFont(String chars) {
			return fitFont(chars, offscreen.getBounds(), actualFont, tileSize);
		}
		
		private boolean[][] fitFont(String chars, Rectangle bounds, Font fnt, int tSize) {
			int cols = bounds.width/tSize;
			int rows = bounds.height/tSize;
			boolean[][] res = new boolean[rows][cols];
			
			ImageData id = new ImageData(bounds.width, bounds.height, 8, new PaletteData(0xFF0000, 0xFF00, 0xFF));
			Image temp = new Image(Display.getCurrent(), id);
			GC draw = new GC(temp);
			
			//Draw the character
			draw.setFont(fnt);
			draw.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			draw.fillRectangle(bounds);
			draw.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			draw.drawString(chars, xOff, yOff);
			
			//Interpret it
			for (int y=0; y<rows; y++) {
				res[y] = new boolean[cols];
				for (int x=0; x<cols; x++) {
					//Iterate
					int black = 0;
					for (int j=0; j<tSize; j++) {
						for (int i=0; i<tSize; i++) {
							int currClr = temp.getImageData().getPixel(i+x*tSize, j+y*tSize);
							/*if (x==0 && y==0)
								System.out.println(currClr);*/
							
							if (currClr==0)
								black++;
						}						
					}
					
					//Decide
					res[y][x] = (black >= (currThreshhold*(tSize*tSize))); 
				}
			}
			
			return res;
		}
		
		private void repaint(int xPos, int yPos, int width, int height, GC gContext) {
			//Global over-ride
			if (dontPaint)
				return;
			
			//Some math
			Rectangle bounds = getClientArea();
			tileSize = Math.min(bounds.width, bounds.height)/PIX_PER_CHAR;
			
			//if (justUpdate) {
				//Can be used later to "cache" our font image.
			//} else {
			
			//Prepare our drawing area.
			//System.out.println("L:" + currMarginLeft + " R:" + currMarginRight + " T:" + currMarginTop + " B:" + currMarginBottom);
			offscreen = new Image(Display.getCurrent(), new Rectangle(0, 0, tileSize*((PIX_PER_CHAR-currMarginRight)-currMarginLeft), tileSize*((PIX_PER_CHAR-currMarginBottom)-currMarginTop)));
			GC g = new GC(offscreen);
			g.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			g.fillRectangle(0, 0, bounds.width, bounds.height);
			
			//Figure out our font!
			if (!firstTime) {
				g.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
				actualFont = new Font(Display.getCurrent(), currFontName, currFontSize, SWT.NORMAL);
				boolean[][] charInFont = fitFont(prevChar, offscreen.getBounds(), actualFont, tileSize);
				for (int y=0; y<charInFont.length; y++) {
					for (int x=0; x<charInFont[y].length; x++) {
						if (!charInFont[y][x])
							continue;
						
						//Fill with a different color
						g.fillRectangle(x*tileSize, y*tileSize, tileSize, tileSize);
					}
				}
				
				//Overlay the character, to be helpful
				g.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				g.setFont(new Font(Display.getCurrent(), currFontName, currFontSize, SWT.NORMAL));
				g.setAlpha(170);
				g.drawString(prevChar, xOff, yOff, true);
				g.setAlpha(255);
			}
			
			//Draw gridlines!
			g.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			g.drawRectangle(offscreen.getBounds());
			for (int i=0; i<offscreen.getBounds().width; i+=tileSize) {
				g.drawLine(i, 0, i, offscreen.getBounds().height);
			}
			for (int i=0; i<offscreen.getBounds().height; i+=tileSize) {
				g.drawLine(0, i, offscreen.getBounds().width, i);
			}
			
			//Flush the buffer to the correct offsets.
			gContext.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
			gContext.fillRectangle(bounds);
			gContext.drawImage(offscreen, currMarginLeft*tileSize, currMarginTop*tileSize);
			
			firstTime = false;
		}

		//General event-handling
		private void hanldeEvent(FONT_PROP type) {
			for (FontPropListener fp : fontChangeListeners)
				fp.propertyChanged(type);
		}
		
		//Interactive properties
		public void setCurrFontName(String currFontName) {
			this.currFontName = currFontName;
			hanldeEvent(FONT_PROP.FONT);
		}
		public void setCurrFontSize(int currFontSize) {
			this.currFontSize = currFontSize;
			hanldeEvent(FONT_PROP.SIZE);
		}
		public void setCurrMarginBottom(int currMarginBottom) {
			this.currMarginBottom = currMarginBottom;
			hanldeEvent(FONT_PROP.BOTTOM_MARGIN);
		}
		public void setCurrMarginLeft(int currMarginLeft) {
			this.currMarginLeft = currMarginLeft;
			hanldeEvent(FONT_PROP.LEFT_MARGIN);
		}
		public void setCurrMarginRight(int currMarginRight) {
			this.currMarginRight = currMarginRight;
			hanldeEvent(FONT_PROP.RIGHT_MARGIN);
		}
		public void setCurrMarginTop(int currMarginTop) {
			this.currMarginTop = currMarginTop;
			hanldeEvent(FONT_PROP.TOP_MARGIN);
		}
		public void setPrevChar(String prevChar) {
			this.prevChar = prevChar;
			hanldeEvent(FONT_PROP.PREVIEW_CHAR);
		}
		public void setCurrThreshhold (float currThreshhold) {
			this.currThreshhold = currThreshhold;
			hanldeEvent(FONT_PROP.THRESHHOLD);
		}
		public void setXOff(int xOff) {
			this.xOff = xOff;
			hanldeEvent(FONT_PROP.X_OFF);
		}
		public void setYOff(int yOff) {
			this.yOff = yOff;
			hanldeEvent(FONT_PROP.Y_OFF);
		}
		
		//Boring properties
		public String getCurrFontName() { return currFontName; }
		public int getCurrFontSize() { return currFontSize; }
		public int getCurrMarginBottom() { return currMarginBottom; }
		public int getCurrMarginLeft() { return currMarginLeft; }
		public int getCurrMarginRight() { return currMarginRight; }
		public int getCurrMarginTop() { return currMarginTop; }
		public String getPrevChar() { return prevChar; }
		public float getCurrThreshhold() { return currThreshhold; }
		public int getXOff() { return xOff; }
		public int getYOff() { return yOff; }
	}
	
	interface FontPropListener {
		public enum FONT_PROP {
			FONT,
			SIZE,
			CHARACTERS,
			LEFT_MARGIN,
			RIGHT_MARGIN,
			TOP_MARGIN,
			BOTTOM_MARGIN,
			PREVIEW_CHAR,
			THRESHHOLD,
			X_OFF,
			Y_OFF
		}
		
		public void propertyChanged(FONT_PROP propChanged);
	}
	
	
	///////////////////////////////////////////////////////////////////
	// Main method. "Application entry point", if you're a prig
	//////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		new SimpleFontConverter();
	}

}
