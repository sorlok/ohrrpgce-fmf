package ohrrpgce.tool;

import ohrrpgce.tool.strings.LanguagePack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 *Mirrored from SWT's MessageBox to prompt the user for the GAME_FMF.JAR file & (possibly) output directory.
 */
public class PathPrompt extends Dialog {
	private String pathToJar="";
	private String outputPath="";
	private LanguagePack currLang;
	private boolean dontUseOut;
	
	//Anonymous workaround
	private Shell shell;
	private Text jarPathTxt;
	private Button jarPathBtn;
	private Text outPathTxt;
	private Button outPathBtn;
	private Button dontUseOutputPath;
	private boolean result;
	
	//Properties
	public String getPathToJar() { return pathToJar; }
	public boolean isUsingOutputPath() { return dontUseOut; } 
	public String getOutputPath() { return outputPath; }
	//public boolean hitOkay() { return result; }
	
	
	public PathPrompt (Shell parent, LanguagePack currLang, String currPathToJar, String currOutputPath, boolean useOutputPath) {
		super(parent, SWT.APPLICATION_MODAL);
		this.currLang = currLang;
		this.pathToJar = currPathToJar;
		if (useOutputPath)
			this.outputPath = currOutputPath;
	}
	
	
	//Make the box visible
	public boolean prompt () {
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(currLang.getPathBoxTitle());
		result = false;

		//Simple layout
		shell.setLayout(new GridLayout(2, false));
		
		//Prepare components - JAR
		Label gamePathLbl = new Label(shell, SWT.LEFT|SWT.HORIZONTAL);
		gamePathLbl.setText(String.format(currLang.getPathToGameTemplate(), new Object[] {"GAME_FMF.JAR"}));
		jarPathTxt = new Text(shell, SWT.SINGLE|SWT.LEFT|SWT.BORDER);
		jarPathTxt.setText(pathToJar);
		jarPathBtn = new Button(shell, SWT.PUSH);
		jarPathBtn.setText("...");
		jarPathBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Please browse to the GAME_FMF.JAR file...");
				fd.setFilterExtensions(new String[] {"GAME_FMF.JAR"});
				fd.setFilterNames(new String[] {"OHRRPGCE_FMF GAME File"});
				String path = fd.open();
				if (path == null)
					return;
				
				jarPathTxt.setText(path);
				pathToJar = jarPathTxt.getText();
			}
		});
		
		//Prepare components - OUT
		Label outPathLbl = new Label(shell, SWT.LEFT|SWT.HORIZONTAL);
		outPathLbl.setText(currLang.getPathToOutputText());
		dontUseOutputPath = new Button(shell, SWT.CHECK);
		dontUseOutputPath.setText(String.format(currLang.getJustOverwriteGameTemplate(), new Object[] {"GAME_FMF.JAR"}));
		dontUseOutputPath.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if(((Button)event.widget).getSelection()) {
					//No output path
					outPathTxt.setEnabled(false);
					outPathBtn.setEnabled(false);
				} else {
					//Custom output path
					outPathTxt.setEnabled(true);
					outPathBtn.setEnabled(true);						
				}
				
				dontUseOut = !dontUseOutputPath.getSelection();
			}
		});
		outPathTxt = new Text(shell, SWT.SINGLE|SWT.LEFT|SWT.BORDER);
		outPathTxt.setText(outputPath);
		outPathBtn = new Button(shell, SWT.PUSH);
		outPathBtn.setText("...");
		outPathBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog dd = new DirectoryDialog(shell, 0);
				dd.setText("Please choose a directory to output these files into.");
				String dir = dd.open();
				if (dir == null)
					return;
				
				outPathTxt.setText(dir);
				outputPath = outPathTxt.getText();
			}
		});
		
		//Simple control
		if (outputPath.length()>0) {
			dontUseOutputPath.setSelection(false);
			outPathTxt.setEnabled(true);
			outPathBtn.setEnabled(true);	
		} else {
			dontUseOutputPath.setSelection(true);
			outPathTxt.setEnabled(false);
			outPathBtn.setEnabled(false);
		}
		
		//Prepare components - Ok/Cancel
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText(currLang.getOk());
		ok.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				result = true;
				shell.dispose();
			}
		});
		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText(currLang.getCancel());
		cancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});
		
		//More control
		shell.setDefaultButton(ok);
		//How do I set the escape key? No matter....
		
		//Layout components
		gamePathLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		jarPathTxt.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 1));
		jarPathBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		outPathLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		dontUseOutputPath.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		outPathTxt.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 1));
		outPathBtn.setLayoutData(new GridData(SWT.CENTER, GridData.CENTER, false, false, 1, 1));
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
