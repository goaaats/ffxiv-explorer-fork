package com.fragmenterworks.ffxivextract.paths.database;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.paths.Crc64;
import com.fragmenterworks.ffxivextract.paths.CrcResult;
import com.fragmenterworks.ffxivextract.paths.PathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class HashDatabase {

    public static Connection conn = null;
    private static final String connectionString = "jdbc:sqlite:./" + Constants.DBFILE_NAME;

    private static HashMap<Long, String> folderCache;
    private static HashMap<Long, String> fileCache;

    private static HashMap<Long, FolderEntry> folderEntryCache;

    private static HashMap<Integer, HashMap<Integer, List<FileEntry>>> indexToFileMap;
    private static HashMap<Integer, HashMap<Integer, List<FolderEntry>>> indexToFolderMap;
    private static HashMap<Integer, HashMap<Integer, List<FullPathEntry>>> indexToFullMap;

    private static List<IHashUpdateListener> listeners = new ArrayList<>();

    public static void init() {
        initCollections();
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(connectionString);

            var fileStmt = conn.prepareStatement("SELECT * FROM filenames");
            var folderStmt = conn.prepareStatement("SELECT * FROM folders");

            ResultSet fileResults = fileStmt.executeQuery();
            while (fileResults.next())
                fileCache.put(fileResults.getLong(1), fileResults.getString(2));
            ResultSet folderResults = folderStmt.executeQuery();
            while (folderResults.next())
                folderCache.put(folderResults.getLong(1), folderResults.getString(2));

            var fullPathStmt = conn.prepareStatement("select indexid, fullhash, folderhash, filehash, folder, file from fullpaths");

            ResultSet rs3 = fullPathStmt.executeQuery();
            while (rs3.next()) {
                int indexId = (int) rs3.getLong(1);
                int fullHash = (int) rs3.getLong(2);
                int folderHash = (int) rs3.getLong(3);
                int fileHash = (int) rs3.getLong(4);
                long folderId = rs3.getLong(5);
                long fileId = rs3.getLong(6);

                cache(indexId, fullHash, folderHash, fileHash, folderId, fileId, false);
            }

            fileStmt.close();
            folderStmt.close();
            fullPathStmt.close();
        } catch (Exception e) {
            Utils.getGlobalLogger().error("", e);
        }
    }

    public static void addListener(IHashUpdateListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(IHashUpdateListener listener) {
        listeners.remove(listener);
    }

    private static void cache(int indexId, int fullHash, int folderHash, int fileHash, long folderId, long fileId, boolean notify) {
        FolderEntry folder = folderEntryCache.getOrDefault(folderId, null);

        if (folder == null) {
            folder = new FolderEntry(indexId, folderHash, folderCache.get(folderId));
            folderEntryCache.put(folderId, folder);
        }
        FileEntry file = new FileEntry(indexId, fileHash, fileCache.get(fileId), folder);
        folder.addFile(file);

        FullPathEntry entry = new FullPathEntry(indexId, fullHash, folder, file);

        indexToFileMap.computeIfAbsent(indexId, k -> new HashMap<>()).computeIfAbsent(fileHash, k -> new ArrayList<>()).add(file);
        indexToFolderMap.computeIfAbsent(indexId, k -> new HashMap<>()).computeIfAbsent(folderHash, k -> new ArrayList<>()).add(folder);
        indexToFullMap.computeIfAbsent(indexId, k -> new HashMap<>()).computeIfAbsent(fullHash, k -> new ArrayList<>()).add(entry);

        var folderStr = folderCache.get(folderId);
        var fileStr = fileCache.get(fileId);

        if (notify)
            for (IHashUpdateListener listener : listeners)
                listener.onHashUpdate(new HashUpdateNotification(indexId, fullHash, folderHash, fileHash, folderStr, fileStr));
    }

    private static void initCollections() {
        folderCache = new HashMap<>();
        fileCache = new HashMap<>();

        folderEntryCache = new HashMap<>();

        indexToFileMap = new HashMap<>();
        indexToFolderMap = new HashMap<>();
        indexToFullMap = new HashMap<>();
    }

    public static FileEntry getFileEntry(int indexId, int hash) {
        if (indexToFileMap.containsKey(indexId)) {
            var set = indexToFileMap.get(indexId);
            if (set.containsKey(hash)) {
                var list = set.get(hash);
                return list.get(0);
            }
        }
        return null;
    }

    public static FileEntry getFileEntry(int indexId, int folderHash, int fileHash) {
        var folderEntry = getFolderEntry(indexId, folderHash);
        if (folderEntry != null) {
            for (var entry : folderEntry.getFiles())
                if (entry.getHash() == fileHash)
                    return entry;
        }
        return getFileEntry(indexId, fileHash);
    }

    public static FolderEntry getFolderEntry(int indexId, int hash) {
        if (indexToFolderMap.containsKey(indexId)) {
            var set = indexToFolderMap.get(indexId);
            if (set.containsKey(hash)) {
                var list = set.get(hash);
                return list.get(0);
            }
        }
        return null;
    }

    public static FullPathEntry getFullPathEntry(int indexId, int hash) {
        if (indexToFullMap.containsKey(indexId)) {
            var set = indexToFullMap.get(indexId);
            if (set.containsKey(hash)) {
                var list = set.get(hash);
                return list.get(0);
            }
        }
        return null;
    }

    public static String getFile(int indexId, int hash) {
        FileEntry entry = getFileEntry(indexId, hash);
        if (entry != null)
            return entry.text;
        return String.format("~%08x", hash);
    }

    public static String getFile(int indexId, int folderHash, int fileHash) {
        FileEntry entry = getFileEntry(indexId, folderHash, fileHash);
        if (entry != null)
            return entry.text;
        return String.format("~%08x", fileHash);
    }

    public static String getFolder(int indexId, int hash) {
        FolderEntry entry = getFolderEntry(indexId, hash);
        if (entry != null)
            return entry.text;
        return String.format("~%08x", hash);
    }

    public static FullPathEntry getFullPathEntry(int indexId, int folderHash, int fileHash) {
        if (indexToFullMap.containsKey(indexId)) {
            var set = indexToFullMap.get(indexId);
            if (set.containsKey(folderHash)) {
                var list = set.get(folderHash);
                for (var entry : list)
                    if (entry.getHash() == fileHash)
                        return entry;
            }
        }
        return null;
    }

    public static String getFullPath(int indexId, int hash) {
        FullPathEntry entry = getFullPathEntry(indexId, hash);
        if (entry != null)
            return entry.folder.text + "/" + entry.file.text;
        return String.format("~%08x", hash);
    }

    public static String getFullPath(int indexId, int folderHash, int fileHash) {
        FullPathEntry entry = getFullPathEntry(indexId, folderHash, fileHash);
        if (entry != null)
            return entry.folder.text + "/" + entry.file.text;
        return String.format("~%08x", fileHash);
    }

    public static int getHashDBVersion() {
        String version = "-1";
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select * from dbinfo where key = 'version'");

            while (rs.next())
                version = rs.getString(2);

            rs.close();
            statement.close();
        } catch (SQLException e) {
            Utils.getGlobalLogger().error("", e);
            return -1;
        }

        return Integer.parseInt(version);
    }

    public static boolean addPath(String fullPath) {
        return addPaths(Collections.singletonList(fullPath)) > 0;
    }

    public static int addPaths(List<String> paths) {
        int count = 0;
        var notifications = new ArrayList<HashUpdateNotification>();
        try {
            var oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            var fileStatement = conn.prepareStatement("INSERT OR IGNORE INTO filenames VALUES(?, ?)");
            var folderStatement = conn.prepareStatement("INSERT OR IGNORE INTO folders VALUES(?, ?)");
            var statement = conn.prepareStatement("INSERT OR IGNORE INTO fullpaths VALUES(?, ?, ?, ?, ?, ?)");

            for (String line : paths) {
                String path = line.toLowerCase();
                String folder = path.substring(0, path.lastIndexOf('/'));
                String fileName = path.substring(path.lastIndexOf('/') + 1);

                var folderId = Crc64.compute(folder);
                var fileNameId = Crc64.compute(fileName);

                int indexId = PathUtils.getIndexId(path);
                CrcResult hashes = PathUtils.computeHashes(path);

                Utils.getGlobalLogger().debug("Adding entry {}", path);

                int added = 0;

                fileStatement.setLong(1, fileNameId);
                fileStatement.setString(2, fileName);

                folderStatement.setLong(1, folderId);
                folderStatement.setString(2, folder);

                statement.setInt(1, indexId);
                statement.setInt(2, hashes.fullHash);
                statement.setInt(3, hashes.folderHash);
                statement.setInt(4, hashes.fileHash);
                statement.setLong(5, folderId);
                statement.setLong(6, fileNameId);

                fileStatement.execute();
                folderStatement.execute();
                added += statement.executeUpdate();

                notifications.add(new HashUpdateNotification(indexId, hashes.fullHash, hashes.folderHash, hashes.fileHash, folder, fileName));

                if (added > 0)
                    count++;
            }

            conn.commit();
            conn.setAutoCommit(oldAutoCommit);
        } catch (Exception exc) {
            Utils.getGlobalLogger().error("An error occurred attempting to add paths.", exc);
            return count * -1;
        }

        if (notifications.size() == 1) {
            for (var listener : listeners)
                listener.onHashUpdate(notifications.get(0));
        } else {
            for (var listener : listeners)
                listener.onMultipleHashUpdate(notifications);
        }

        return count;
    }

    public static int importFilePaths(File selectedFile) {

        var paths = new ArrayList<String>();

        try {
            var lines = Files.readAllLines(selectedFile.toPath());

            for (String line : lines) {
                String path = "";
                try {
                    int indexof;
                    if ((indexof = line.indexOf(',')) == -1)
                        path = line;
                    else
                        path = line.substring(indexof + 2);

                    paths.add(path.trim());
                } catch (StringIndexOutOfBoundsException e) {
                    Utils.getGlobalLogger().error("Couldn't parse line {}", line, e);
                }
            }
        } catch (Exception exc) {
            Utils.getGlobalLogger().error("An error occurred attempting to add paths.", exc);
            return -1;
        }

        return addPaths(paths);
    }

    // This is a quick n dirty SQDB reader. Skip 0x800 bytes of header, the
    // other header data in each entry, read in hashes + path. Skip 0x108 bytes
    // per entry.
    // Entry header is: 4 bytes of nothing, some val, content type, 4 bytes of
    // nothing. Then good stuff starts.
    @SuppressWarnings("unused")
    public static void loadPathsFromSQDB(String path) {

        var paths = new ArrayList<String>();

        try {
            //TODO: if sqdb fails to load, change the endian here thanks
            EARandomAccessFile file = new EARandomAccessFile(path, "r", ByteOrder.LITTLE_ENDIAN);

            file.skipBytes(0x800); // Headers

            long folderHash;
            long fileHash;
            StringBuilder fullPath = null;
            String folder = null;
            String filename = null;

            // Skip header
            file.readInt();
            file.readInt();
            file.readInt();
            file.readInt();
            long lastStartPosition = 0;

            while (true) {
                lastStartPosition = file.getFilePointer();
                fullPath = new StringBuilder();

                while (true) {
                    byte c = file.readByte();
                    if (c == 0)
                        break;
                    else
                        fullPath.append((char) c);
                }

                paths.add(fullPath.toString());
                file.seek(lastStartPosition);

                // Check EOF
                if (file.getFilePointer() + 0x108 >= file.length()) break;
                file.skipBytes(0x108);
            }
            file.close();
        } catch (IOException e) {
            Utils.getGlobalLogger().error("Encountered an error batch-adding paths from SQDB.", e);
        }
        addPaths(paths);
    }
}
