package ohrrpgce.tool.strings;

import org.eclipse.swt.graphics.Image;

public abstract class LanguagePack {
	private static final LanguagePack[] allPacks = {new EnglishLanguagePack(), new ChineseLanguagePack(), new MyanmarLanguagePack()};
	
	//Avoid reflection
	public static LanguagePack[] getAllLanguagePacks() {
		return allPacks;
	}
	
	public static LanguagePack getDefault() {
		return allPacks[0];
	}
	
	
	/** Text for the work "Okay" */
	public abstract String getOk();

	/** Text for the work "Cancel" */
	public abstract String getCancel();
	
	
	
	/** Text to display at the top of the form, and in the task bar. */
	public abstract String getTitlebarText();
	
	/** Native name of the language */
	public abstract String getLanguageName();
	
	/** Actual translation of the word "Language" */
	public abstract String getLanguageMenuText();
	
	/** Sometimes, this is needed **/
	public abstract Image getLanguagePicture();

	
	/** Text for the "Library" label */
	public abstract String getLibraryTitle();
	
	/** Text for the button used to add a file to the library */
	public abstract String getAddFileText();
	
	/** Text for the button used to add a folder to the library */
	public abstract String getAddFolderText();
	
	/** Text for the button used to delete a file/folder from the library */
	public abstract String getDeleteText();
	
	/** Error text that shows up if there's no games in the library */
	public abstract String getNoGamesInLibraryText();
	
	/** Error text that shows up if there's no valid path the the GAME_FMF.JAR file */
	public abstract String getNoJarFileSelectedText();
	
	/** What should the default group (the one used for games added as files, not folders) be called? */
	public abstract String getDefaultGroupName();
	
	
	/** Text shown in the "Add File" dialog */
	public abstract String getChooseFileText();
	
	/** Text shown in the "Add Folder" dialog */
	public abstract String getChooseFolderText();
	
	/** Text shown at the top of the right panel (current game) */
	public abstract String getMobileTitle();
	
	/** Text of the "update" button, used to save the current JAR, updated from the file system. */
	public abstract String getUpdateText();
	
	/** Text of the button used to set the path to the GAME_FMF.JAR file and (optionally) output directory. */
	public abstract String getSetPathText();
	
	
	//////////////////////////
	///// For the prompt box:
	//////////////////////////
	
	/** The title of this form */
	public abstract String getPathBoxTitle();
	
	/** Template for the label instructing one to enter a path to {GAME_FMF.JAR} */
	public abstract String getPathToGameTemplate();
	
	/** Template for the radio button which forces one to have no output directory, and simply overwrite {GAME_FMF.JAR} */
	public abstract String getJustOverwriteGameTemplate();
	
	/** Text instructing one to enter a path for the program to output the newly compiled game. */
	public abstract String getPathToOutputText();
}
