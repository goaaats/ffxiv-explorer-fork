package com.fragmenterworks.ffxivextract;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.fragmenterworks.ffxivextract.storage.HashDatabase;
import com.fragmenterworks.ffxivextract.gui.FileManagerWindow;
import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.gui.components.Update_Dialog;
import com.fragmenterworks.ffxivextract.helpers.DatBuilder;
import com.fragmenterworks.ffxivextract.helpers.HashFinding_Utils;
import com.fragmenterworks.ffxivextract.helpers.HavokNative;
import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.PathSearcher;
import com.fragmenterworks.ffxivextract.helpers.VersionUpdater;
import com.fragmenterworks.ffxivextract.helpers.VersionUpdater.VersionCheckObject;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;

public class Main {

	public static void main(String[] args) {
		
		// Set to windows UI
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Init the hash database
		try {
			File dbFile = new File("./" + Constants.DBFILE_NAME);
			if (dbFile.exists())
				HashDatabase.init();
			else
				JOptionPane
						.showMessageDialog(
								null,
								Constants.DBFILE_NAME
										+ " is missing. No file or folder names will be shown... instead the file's hashes will be displayed.",
								"Hash DB Load Error", JOptionPane.ERROR_MESSAGE);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}		

		/*
		HashDatabase.beginConnection();
		try {
			HashDatabase.globalConnection.setAutoCommit(false);
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for (int j = 180; j <= 180; j++){
			
			
			if (j >= 120 && j <= 127)
				continue;
			if (j >= 150)
				continue;						
			
			for (int i = 0; i <= 999; i++)
			{
				
				HashDatabase.addPathToDB(String.format("ui/icon/%03d000/en/%03d%03d.tex", j, j, i), "060000", HashDatabase.globalConnection);
				HashDatabase.addPathToDB(String.format("ui/icon/%03d000/de/%03d%03d.tex", j, j, i), "060000", HashDatabase.globalConnection);
				HashDatabase.addPathToDB(String.format("ui/icon/%03d000/fr/%03d%03d.tex", j, j, i), "060000", HashDatabase.globalConnection);
				HashDatabase.addPathToDB(String.format("ui/icon/%03d000/ja/%03d%03d.tex", j, j, i), "060000", HashDatabase.globalConnection);
				System.out.println(String.format("ui/icon/%03d000/en/%03d%03d.tex", j, j, i));
			}			
			try {
				HashDatabase.commit();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		HashDatabase.closeConnection();
		
		*/
		
		// Arguments
		if (args.length > 0) {
			
			// Info
			if (args.length == 1)
			{
				if (args[0].equals("-help"))
					System.out.println("Commands: -help, -debug, -pathsearch");
				else if (args[0].equals("-pathsearch"))
					System.out.println("Searches an archive for strings that start with <str>\n-pathsearch <path to index> <str>");
			}
			
			// DEBUG ON
			if (args[0].equals("-debug")) {
				Constants.DEBUG = true;
				System.out.println("Debug Mode ON");
			}

			// PATHSEARCH
			if (args[0].equals("-pathsearch")) {
				if (args.length < 3) {
					System.out
							.println("Not enough args.");
					return;
				}

				System.out
						.println("Starting Path Searcher (this will take a while)");

				
				try {
					PathSearcher.doPathSearch(args[1], args[2]);
				} catch (IOException e) {
					System.out
							.println("There was an error searching. Stacktrace: ");
					e.printStackTrace();
				}				

				return;
			}
		}

		// Open up the main window
		FileManagerWindow fileMan = new FileManagerWindow(Constants.APPNAME);
		fileMan.setVisible(true);

		// Load Prefs
		Preferences prefs = Preferences
				.userNodeForPackage(com.fragmenterworks.ffxivextract.Main.class);
		boolean firstRun = prefs.getBoolean(Constants.PREF_FIRSTRUN, true);		
		Constants.datPath = prefs.get(Constants.PREF_DAT_PATH, null);

		// First Run
		if (firstRun) {
			prefs.putBoolean(Constants.PREF_FIRSTRUN, false);

			int n = JOptionPane
					.showConfirmDialog(
							fileMan,
							"Would you like FFXIV Extractor to check for a new hash database?",
							"Hash DB Version Check", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				prefs.putBoolean(Constants.PREF_DO_DB_UPDATE, true);
			} else
				prefs.putBoolean(Constants.PREF_DO_DB_UPDATE, false);
		}

		// Version Check
		if (prefs.getBoolean(Constants.PREF_DO_DB_UPDATE, false)) {
			VersionCheckObject checkObj = VersionUpdater.checkForUpdates();

			if (HashDatabase.getHashDBVersion() < checkObj.currentDbVer
					|| Constants.APP_VERSION_CODE < checkObj.currentAppVer) {
				Update_Dialog updateDialog = new Update_Dialog(checkObj);
				updateDialog.setLocationRelativeTo(fileMan);
				updateDialog.setVisible(true);
			}
		}

	}
	
}
