package ca.fraggergames.ffxivextract;

import javax.swing.Icon;

public class Strings {
		
	//Dialog Titles
	public static final String DIALOG_TITLE_ABOUT = "About";
	public static final String DIALOG_TITLE_MUSICSWAPPER = "Music Swap Tool (EXPERIMENTAL)";
	public static final String DIALOG_TITLE_ERROR = "Error";
	
	//About Dialog
	public static final String ABOUTDIALOG_VERSION = "Version:";
	public static final String ABOUTDIALOG_GITVERSION = "Git Commit:";

	//Music Swapper
	public static final String MUSICSWAPPER_FRAMETITLE_ARCHIVE = "Music Archive";
	public static final String MUSICSWAPPER_FRAMETITLE_SWAPPING = "Swapping";
	public static final String MUSICSWAPPER_PATHTOFILE = "Path to music pack: ";
	public static final String MUSICSWAPPER_DEFAULTPATHTEXT = "Point to 0c0000.win32.index";
	public static final String MUSICSWAPPER_FROMID = "From Id:";
	public static final String MUSICSWAPPER_TOID = "Set to:";
	public static final String MUSICSWAPPER_ORIGINALID = "Original Id:";
	public static final String MUSICSWAPPER_CURRENTOFFSET = "Currently set to offset: %08X";
	public static final String MUSICSWAPPER_CURRENTSETTO = "Currently set to: ";
	
	//File Types
	public static final String FILETYPE_FFXIV_INDEX = "FFXIV Index File (.index)";
	public static final String FILETYPE_FFXIV_MUSICINDEX = "FFXIV Music Archive Index (0c0000.win32.index)";
	
	//Menu and Menu Items
	public static final String MENU_FILE = "File";
	public static final String MENU_TOOLS = "Tools";
	public static final String MENU_HELP = "Help";
	
	public static final String MENUITEM_OPEN = "Open";
	public static final String MENUITEM_CLOSE = "Close";
	public static final String MENUITEM_EXTRACT = "Extract";
	public static final String MENUITEM_EXTRACTRAW = "Extract Raw";
	public static final String MENUITEM_MUSICSWAPPER = "Music Swapper (EXPERIMENTAL)";
	public static final String MENUITEM_QUIT = "Quit";
	public static final String MENUITEM_ABOUT = "About";
	
	//Buttons
	public static final String BUTTONNAMES_BROWSE = "Browse";
	public static final String BUTTONNAMES_SET = "Set";
	public static final String BUTTONNAMES_REVERT = "Revert";	
	
	//Errors
	public static final String ERROR_CANNOT_OPEN_INDEX = "Could not open index file. Shut off FFXIV if it is running currently.";
	public static final String ERROR_EDITIO = "Bad IOException happened. You should replace the edited index file with the backup.";
	
	
}
	