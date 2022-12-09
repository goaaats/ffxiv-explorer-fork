package com.fragmenterworks.ffxivextract;

import com.fragmenterworks.ffxivextract.gui.FileManagerWindow;
import com.fragmenterworks.ffxivextract.gui.components.Update_Dialog;
import com.fragmenterworks.ffxivextract.helpers.PathSearcher;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.helpers.VersionUpdater;
import com.fragmenterworks.ffxivextract.helpers.VersionUpdater.VersionCheckObject;
import com.fragmenterworks.ffxivextract.paths.database.HashDatabase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class Main {

    public static void main(String[] args) {

        Utils.getGlobalLogger().info("Starting FFXIV Explorer...");

        // Set to windows UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Init the hash database
        File dbFile = new File("./" + Constants.DBFILE_NAME);
        boolean dbInit = false;

        if (dbFile.exists()) {
            try {
                HashDatabase.init();
                dbInit = true;
            } catch (Exception e) {
                Utils.getGlobalLogger().error("Error loading hash database.", e);
            }
        }

        if (!dbInit) {
            JOptionPane.showMessageDialog(null,
                    Constants.DBFILE_NAME + " is missing. No file or folder names will be shown... instead the file's hashes will be displayed.",
                    "Hash DB Load Error", JOptionPane.ERROR_MESSAGE);
        }

        Level currentLevel = LogManager.getRootLogger().getLevel();

        // Arguments
        if (args.length > 0) {

            // Info
            if (args.length == 1) {
                if (args[0].equals("-help"))
                    System.out.println("Commands: -help, -debug, -pathsearch");
                else if (args[0].equals("-pathsearch"))
                    System.out.println("Searches an archive for strings that start with <str>\n-pathsearch <path to index> <str>");
            }

//            if (args[0].equals("-debug") && currentLevel.intLevel() < Level.DEBUG.intLevel())
//                Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
                Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.TRACE);

            // PATHSEARCH
            if (args[0].equals("-pathsearch")) {
                if (args.length < 3) {
                    Utils.getGlobalLogger().info("Too few args for pathsearch!");
                    return;
                }

                Utils.getGlobalLogger().info("Starting Path Searcher (this will take a while)");

                try {
                    PathSearcher.doPathSearch(args[1], args[2]);
                } catch (IOException e) {
                    Utils.getGlobalLogger().error("Encountered an error while path searching.", e);
                }
                return;
            }
        }

        Utils.getGlobalLogger().info("Logging set to {}", LogManager.getRootLogger().getLevel());

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

        // Version Check (disabled in fork)
//        if (prefs.getBoolean(Constants.PREF_DO_DB_UPDATE, false)) {
//            VersionCheckObject checkObj = VersionUpdater.checkForUpdates();
//
//            if (HashDatabase.getHashDBVersion() < checkObj.currentDbVer
//                    || Constants.APP_VERSION_CODE < checkObj.currentAppVer) {
//                Update_Dialog updateDialog = new Update_Dialog(checkObj);
//                updateDialog.setLocationRelativeTo(fileMan);
//                updateDialog.setVisible(true);
//            }
//        }
    }

}
