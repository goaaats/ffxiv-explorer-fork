package ca.fraggergames.ffxivextract;


public class Strings {
		
	//Dialog Titles
	public static final String DIALOG_TITLE_ABOUT = "About";
	public static final String DIALOG_TITLE_MUSICSWAPPER = "Music Swap Tool";
	public static final String DIALOG_TITLE_SEARCH = "Search";
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
	
	//Search Window
	public static final String SEARCH_FRAMETITLE_BYSTRING = "Search for String";
	public static final String SEARCH_FRAMETITLE_BYBYTES = "Search for Bytes";
	public static final String SEARCH_SEARCH = "Search: ";
	
	//File Types
	public static final String FILETYPE_FFXIV_INDEX = "FFXIV Index File (.index)";
	public static final String FILETYPE_FFXIV_MUSICINDEX = "FFXIV Music Archive Index (0c0000.win32.index)";
	
	//Menu and Menu Items
	public static final String MENU_FILE = "File";
	public static final String MENU_SEARCH = "Search";
	public static final String MENU_TOOLS = "Tools";
	public static final String MENU_OPTIONS = "Options";
	public static final String MENU_HELP = "Help";
	
	public static final String MENUITEM_OPEN = "Open";
	public static final String MENUITEM_CLOSE = "Close";
	public static final String MENUITEM_EXTRACT = "Extract";
	public static final String MENUITEM_EXTRACTRAW = "Extract Raw";
	public static final String MENUITEM_SEARCH = "Search";
	public static final String MENUITEM_SEARCHAGAIN = "Search Again";
	public static final String MENUITEM_MUSICSWAPPER = "Music Swapper";
	public static final String MENUITEM_MACROEDITOR = "Macro Editor (NOT DONE)";
	public static final String MENUITEM_LOGVIEWER = "Log Viewer (NOT DONE)";
	public static final String MENUITEM_ENABLEUPDATE = "Check for Updates";
	public static final String MENUITEM_QUIT = "Quit";
	public static final String MENUITEM_ABOUT = "About";
	
	//Buttons
	public static final String BUTTONNAMES_BROWSE = "Browse";
	public static final String BUTTONNAMES_SET = "Set";
	public static final String BUTTONNAMES_REVERT = "Revert";
	public static final String BUTTONNAMES_SEARCH = "Search";
	public static final String BUTTONNAMES_CLOSE = "Close";
	
	//Errors
	public static final String ERROR_CANNOT_OPEN_INDEX = "Could not open index file. Shut off FFXIV if it is running currently.";
	public static final String ERROR_EDITIO = "Bad IOException happened. You should replace the edited index file with the backup.";
	
	//Misc
	public static final String MSG_MUSICSWAPPER_TITLE = "Before Using Swapper";
	public static final String MSG_MUSICSWAPPER = "This program modifies FFXIV ARR's files. I am not responsible in the case where swapping the music files leads to a ban or damaged game. Use at your own risk.\n\nRemember to restore before patching the game as the patcher may have issues with modified files.\n	\nThe index file is automatically backed up as \"0c0000.win32.index.bak\" in the same folder as \"0c0000.win32.index\".";
	
	
}
	