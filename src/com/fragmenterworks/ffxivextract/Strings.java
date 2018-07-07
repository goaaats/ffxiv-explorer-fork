package com.fragmenterworks.ffxivextract;

import javax.swing.Icon;


public class Strings {

	//Dialog Titles
	public static final String DIALOG_TITLE_ABOUT = "About";
	public static final String DIALOG_TITLE_MUSICSWAPPER = "Music Swap Tool";
	public static final String DIALOG_TITLE_FILEINJECT = "File Injector";
	public static final String DIALOG_TITLE_SCDCONVERTER = "OGG to SCD Converter";
	public static final String DIALOG_TITLE_MODELVIEWER = "Model Viewer";
	public static final String DIALOG_TITLE_OUTFITTER = "Outfitter (Experimental)";
	public static final String DIALOG_TITLE_SEARCH = "Search";
	public static final String DIALOG_TITLE_SETTINGS = "Settings";
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

	//File Inject
	public static final String FILEINJECT_FRAMETITLE_ARCHIVE = "Dat index";
	public static final String FILEINJECT_PATHTOFILE = "Path to index: ";
	public static final String FILEINJECT_DEFAULTPATHTEXT = "No index selected";

	//Path to Hash Tool
	public static final String PATHTOHASH_TITLE = "Path -> Hash Calculator";
	public static final String PATHTOHASH_PATH = "Path: ";
	public static final String PATHTOHASH_FOLDER_HASH = "Folder Hash: ";
	public static final String PATHTOHASH_FILE_HASH = "File Hash: ";
	public static final String PATHTOHASH_BUTTON_HASHTHIS = "Calculate";
	public static final String PATHTOHASH_BUTTON_CLOSE = "Close";
	public static final String PATHTOHASH_INTRO = "Set a valid path in the form of \"folder/subfolder/file.ext\"\nand click calculate.";
	public static final String PATHTOHASH_ERROR_INVALID = "Not a valid path.";

	//Search Window
	public static final String SEARCH_FRAMETITLE_BYSTRING = "Search for String";
	public static final String SEARCH_FRAMETITLE_BYBYTES = "Search for Bytes";
	public static final String SEARCH_SEARCH = "Search: ";

	//File Types
	public static final String FILETYPE_FFXIV_INDEX = "FFXIV Index File (.index)";
	public static final String FILETYPE_FFXIV_INDEX2 = "FFXIV Index2 File (.index2)";
	public static final String FILETYPE_FFXIV_MUSICINDEX = "FFXIV Music Archive Index (0c0000.win32.index)";
	public static final String FILETYPE_OGG = "OGG Vorbis File (.ogg)";
	public static final String FILETYPE_FFXIV_LOG = "FFXIV Log File (.log)";

	//Menu and Menu Items
	public static final String MENU_FILE = "File";
	public static final String MENU_SEARCH = "Search";
	public static final String MENU_DATAVIEWERS = "Data Viewers";
	public static final String MENU_TOOLS = "Tools";
	public static final String MENU_OPTIONS = "Options";
	public static final String MENU_HELP = "Help";

	public static final String MENUITEM_OPEN = "Open";
	public static final String MENUITEM_CLOSE = "Close";
	public static final String MENUITEM_EXTRACT = "Extract";
	public static final String MENUITEM_EXTRACTRAW = "Extract Raw";
	public static final String MENUITEM_SEARCH = "Search";
	public static final String MENUITEM_SEARCHAGAIN = "Search Again";
	public static final String MENUITEM_MODELVIEWER = "Model Viewer";
	public static final String MENUITEM_OUTFITTER = "Outfitter (Experimental)";
	public static final String MENUITEM_MUSICSWAPPER = "Music Swapper";
	public static final String MENUITEM_FILEINJECT = "File injector";
	public static final String MENUITEM_HASHCALC = "Path -> Hash Calculator";
	public static final String MENUITEM_CEDUMPIMPORT = "Import Cheat Engine path dump";
	public static final String MENUITEM_MACROEDITOR = "Macro Editor";
	public static final String MENUITEM_LOGVIEWER = "Log Viewer";
	public static final String MENUITEM_FIND_EXH = "Find Exh Hashes";
	public static final String MENUITEM_FIND_MUSIC = "Find Music Hashes";
	public static final String MENUITEM_FIND_MAPS = "Find Map Hashes";
	public static final String MENUITEM_SETTINGS = "Settings";
	public static final String MENUITEM_ENABLEUPDATE = "Check for Updates";
	public static final String MENUITEM_QUIT = "Quit";
	public static final String MENUITEM_ABOUT = "About";
	public static final String MENUITEM_EXD_HEX_OPTION = "Show EX numbers as hex";
	public static final String MENUITEM_USE_UNLUAC = "Use Unluac instead of LuaDec";

	//Buttons
	public static final String BUTTONNAMES_BROWSE = "Browse";
	public static final String BUTTONNAMES_SET = "Set";
	public static final String BUTTONNAMES_REVERT = "Revert";
	public static final String BUTTONNAMES_SEARCH = "Search";
	public static final String BUTTONNAMES_CLOSE = "Close";
	public static final String BUTTONNAMES_GOTOFILE = "Search file in first list";

	//Errors
	public static final String ERROR_CANNOT_OPEN_INDEX = "Could not open index file. Shut off FFXIV if it is running currently.";
	public static final String ERROR_EDITIO = "Bad IOException happened. You should replace the edited index file with the backup.";

	//Misc
	public static final String MSG_MUSICSWAPPER_TITLE = "Before Using Swapper";
	public static final String MSG_FILEINJECT_TITLE = "Before Using FileInject";
	public static final String MSG_MUSICSWAPPER = "This program modifies FFXIV ARR's files. I am not responsible in the case where swapping the music files leads to a ban or damaged game. Use at your own risk.\n\nRemember to restore before patching the game as the patcher may have issues with modified files.\n	\nThe index file is automatically backed up as \"0c0000.win32.index.bak\" in the same folder as \"0c0000.win32.index\".";
	public static final String MSG_FILEINJECT = "This program modifies FFXIV ARR's files. I am not responsible in the case where swapping files leads to a ban or damaged game. Use at your own risk.\n\nRemember to restore before patching the game as the patcher may have issues with modified files.\n	\nThe index file is automatically backed up as \"<file>.index.bak\" in the same folder as \"<file>.index\".";
	public static final String MSG_OUTFITTER_TITLE = "This is still experimental!";
	public static final String MSG_OUTFITTER = "This feature is still being figured out and experimented with! Some races and items may not look correct. \nSome of the races and items do not load properly due to model reuse which is then scaled/translated. \nMost items should render properly for Hyur, male Roegadyn, and Lalafell models. Setting hair and face \nto 0 will clear them, which is useful for helmet items. \n\nAnyway, hope you have fun!";


}
