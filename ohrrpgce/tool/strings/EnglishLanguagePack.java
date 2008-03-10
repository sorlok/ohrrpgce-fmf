package ohrrpgce.tool.strings;

import org.eclipse.swt.graphics.Image;

public class EnglishLanguagePack extends LanguagePack {

	//////////////////////////
	// General Strings 
	//////////////////////////
	public String getOk() {
		return "Ok";
	}
	
	public String getCancel() {
		return "Cancel";
	}
	
	
	///////////////////////////////////
	// Main Form
	///////////////////////////////////
	
	public String getLanguageMenuText() {
		return "Language";
	}

	public String getLanguageName() {
		return "English";
	}
	
	public Image getLanguagePicture() {
		return null;
	}
	
	public String getTitlebarText() {
		return "OHRRPGCE Phone Loader";
	}

	public String getLibraryTitle() {
		return "Library";
	}
	
	public String getAddFileText() {
		return "Add File";
	}
	
	public String getAddFolderText() {
		return "Add Folder";
	}
	
	public String getDeleteText() {
		return "Delete";
	}
	
	public String getNoGamesInLibraryText() {
		return "There are no games in your library. " +
				"\nPlease click \"Add File\" or \"Add Folder\" " +
				"to load some games into your library.";
	}
	
	public String getNoJarFileSelectedText() {
		return "No valid path to the GAME_FMF.JAR file was found." +
				"\nPlease click the \"Path...\" button to set " +
				"this path yourself."; 
	}
	
	public String getDefaultGroupName() {
		return "Loose Games";
	}
	
	public String getChooseFileText() {
		return "Please select a game file";
	}
	
	public String getChooseFolderText() {
		return "Please select a game folder";
	}
	
	public String getMobileTitle() {
		return "Mobile";
	}
	
	public String getUpdateText() {
		return "Update";
	}
	
	public String getSetPathText() {
		return "Path..";
	}
	
	
	/////////////////////////
	// Path Sub-Form
	/////////////////////////
	public String getJustOverwriteGameTemplate() {
		return "Just overwrite %1$2s";
	}
	
	public String getPathBoxTitle() {
		return "Path Selector";
	}
	
	public String getPathToGameTemplate() {
		return "Path to %1$2s:";
	}
	
	public String getPathToOutputText() {
		return "Output Folder:";
	}
}
