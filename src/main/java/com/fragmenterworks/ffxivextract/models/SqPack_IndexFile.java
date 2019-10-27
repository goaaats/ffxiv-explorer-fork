package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.gui.components.Loading_Dialog;
import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.LERandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.storage.HashDatabase;

import javax.swing.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SqPack_IndexFile {

    private final String path;
    private ByteOrder endian;

    private final SqPack_DataSegment[] segments = new SqPack_DataSegment[4];
    private SqPack_Folder[] packFolders;
    private boolean noFolder = false;

    private boolean isFastloaded = false;

    private static final Map<String, SqPack_IndexFile> cachedIndexes = new HashMap<String, SqPack_IndexFile>();

    /**
     * @param pathToIndex
     * @param fastLoad
     * @return
     */
    public static SqPack_IndexFile createIndexFileForPath(String pathToIndex, boolean fastLoad) {
        //Fast load will blindly load all files regardless of folder
        String cacheKey = pathToIndex + (fastLoad ? ":fast" : "");
        if (cachedIndexes.containsKey(cacheKey)) {
            return cachedIndexes.get(cacheKey);
        } else {
            try {
                SqPack_IndexFile indexFile = new SqPack_IndexFile(pathToIndex, fastLoad);
                cachedIndexes.put(cacheKey, indexFile);
                return indexFile;
            } catch (IOException e) {
                Utils.getGlobalLogger().error(e);
            }
        }
        return null;
    }

    /**
     * Constructor. Primarily used by the FileManagerWindow to handle gui stuff. Loads all file info + structure info and names.
     *
     * @param pathToIndex         Path to the SqPack index file you wish to open
     * @param prgLoadingBar       The progress bar to increment
     * @param lblLoadingBarString The progress bar text to increment
     */
    public SqPack_IndexFile(String pathToIndex, JProgressBar prgLoadingBar, JLabel lblLoadingBarString) throws IOException {

        path = pathToIndex;

        LERandomAccessFile lref = new LERandomAccessFile(pathToIndex, "r");
        RandomAccessFile bref = new RandomAccessFile(pathToIndex, "r");

        int sqpackHeaderLength = checkSqPackHeader(lref, bref);
        if (sqpackHeaderLength < 0)
            return;

        EARandomAccessFile ref = new EARandomAccessFile(pathToIndex, "r", endian);

        getSegments(ref, sqpackHeaderLength);

        // Check if we have a folder segment, if not... load files only
        if (segments[3] != null && segments[3].offset != 0) {
            int offset = segments[3].getOffset();
            int size = segments[3].getSize();
            int numFolders = size / 0x10;

            if (prgLoadingBar != null)
                prgLoadingBar.setMaximum(segments[0].getSize() / 0x10);

            if (lblLoadingBarString != null)
                lblLoadingBarString.setText("0%");

            packFolders = new SqPack_Folder[numFolders];

            for (int i = 0; i < numFolders; i++) {
                ref.seek(offset + (i * 16)); // Every folder offset header is 16
                // bytes

                int id = ref.readInt();
                int fileIndexOffset = ref.readInt();
                int folderSize = ref.readInt();
                int numFiles = folderSize / 0x10;
                ref.readInt(); // Skip

                packFolders[i] = new SqPack_Folder(id, numFiles, fileIndexOffset);
                packFolders[i].readFiles(ref, prgLoadingBar, lblLoadingBarString, false);
            }
        } else {
            noFolder = true;

            if (prgLoadingBar != null)
                prgLoadingBar.setMaximum((pathToIndex.contains("index2") ? 2 : 1) * segments[0].getSize() / 0x10);

            if (lblLoadingBarString != null)
                lblLoadingBarString.setText("0%");

            packFolders = new SqPack_Folder[1];
            packFolders[0] = new SqPack_Folder(0, (pathToIndex.contains("index2") ? 2 : 1) * segments[0].getSize() / 0x10, segments[0].getOffset());
            packFolders[0].readFiles(ref, prgLoadingBar, lblLoadingBarString, pathToIndex.contains("index2"));
        }

        ref.close();

    }

    /**
     * Constructor.
     *
     * @param pathToIndex Path to the SqPack index file you wish to open
     * @param fastLoad    Setting this to true will load only file info of the archive, and omit it's structure and file/folder names.
     */
    public SqPack_IndexFile(String pathToIndex, boolean fastLoad) throws IOException {

        path = pathToIndex;

        LERandomAccessFile lref = new LERandomAccessFile(pathToIndex, "r");
        RandomAccessFile bref = new RandomAccessFile(pathToIndex, "r");

        int sqpackHeaderLength = checkSqPackHeader(lref, bref);
        if (sqpackHeaderLength < 0)
            return;

        EARandomAccessFile ref = new EARandomAccessFile(pathToIndex, "r", endian);
        getSegments(ref, sqpackHeaderLength);

        //Fast load will blindly load all files regardless of folder
        if (fastLoad) {
            isFastloaded = true;

            noFolder = true;
            packFolders = new SqPack_Folder[1];
            packFolders[0] = new SqPack_Folder(0, (pathToIndex.contains("index2") ? 2 : 1) * segments[0].getSize() / 0x10, segments[0].getOffset());

            ref.seek(segments[0].getOffset());

            for (int i = 0; i < packFolders[0].files.length; i++) {
                if (!pathToIndex.contains("index2")) {
                    int id = ref.readInt();
                    int id2 = ref.readInt();
                    long dataoffset = ref.readInt();
                    ref.readInt();

                    packFolders[0].getFiles()[i] = new SqPack_File(id, id2, dataoffset, false);
                } else {
                    int id = ref.readInt();
                    long dataoffset = ref.readInt();
                    packFolders[0].getFiles()[i] = new SqPack_File(id, -1, dataoffset, false);
                }
            }
        } else {
            // Check if we have a folder segment, if not... load files only
            if (segments[3] != null) {
                int offset = segments[3].getOffset();
                int size = segments[3].getSize();
                int numFolders = size / 0x10;

                packFolders = new SqPack_Folder[numFolders];

                for (int i = 0; i < numFolders; i++) {
                    ref.seek(offset + (i * 16)); // Every folder offset header is 16
                    // bytes

                    int id = ref.readInt();
                    int fileIndexOffset = ref.readInt();
                    int folderSize = ref.readInt();
                    int numFiles = folderSize / 0x10;
                    ref.readInt(); // Skip

                    packFolders[i] = new SqPack_Folder(id, numFiles,
                            fileIndexOffset);

                    packFolders[i].readFiles(ref, false);
                }
            } else {
                noFolder = true;
                packFolders = new SqPack_Folder[1];
                packFolders[0] = new SqPack_Folder(0, (pathToIndex.contains("index2") ? 2 : 1) * segments[0].getSize() / 0x10, segments[0].getOffset());
                packFolders[0].readFiles(ref, pathToIndex.contains("index2"));
            }
        }

        ref.close();

    }

    /**
     * Checks the sqpack header, will also advance the file pointer by it's size.
     */
	private int checkSqPackHeader(LERandomAccessFile ref, RandomAccessFile bref) throws IOException {
        // Check SqPack Header
        byte[] buffer = new byte[6];
        byte[] bigBuffer = new byte[6];

        ref.readFully(buffer, 0, 6);
        bref.readFully(bigBuffer, 0, 6);

        if (buffer[0] != 'S' || buffer[1] != 'q' || buffer[2] != 'P'
                || buffer[3] != 'a' || buffer[4] != 'c' || buffer[5] != 'k') {
            ref.close();

            Utils.getGlobalLogger().error("SqPack magic was incorrect.");

            StringBuilder s = new StringBuilder();
            for (int i = 0; i < 6; i++)
                s.append(String.format("%X", buffer[i]));
            String strMagic = new String(buffer);
            Utils.getGlobalLogger().debug("Magic was 0x{} // {}", s.toString(), strMagic);
            return -1;
        }

        // Get Header Length
        ref.seek(0x0c);
        bref.seek(0x0c);
        int headerLength = ref.readInt();
        int bHeaderLength = bref.readInt();

        ref.readInt(); // Unknown
        bref.readInt();

        // Get Header Type, has to be 2 for index
        int type = ref.readInt();
        int bType = bref.readInt();

        if (type != 2 && bType != 2) {
            Utils.getGlobalLogger().error("SqPack type was incorrect.");
            Utils.getGlobalLogger().debug("Type was LE: {}, BE: {}", type, bType);
            return -1;
        }

        if (type == 2) {
            endian = ByteOrder.LITTLE_ENDIAN;
            return headerLength;
        }
        endian = ByteOrder.BIG_ENDIAN;
        return bHeaderLength;
    }

    private int getSegments(EARandomAccessFile ref, int segmentHeaderStart) throws IOException {

        ref.seek(segmentHeaderStart);

        int headerLength = ref.readInt();

        for (int i = 0; i < segments.length; i++) {
            int firstVal = ref.readInt();
            int offset = ref.readInt();
            int size = ref.readInt();
            byte[] sha1 = new byte[20];
            ref.readFully(sha1, 0, 20);
            segments[i] = new SqPack_DataSegment(offset, size, sha1);

            if (i == 0)
                ref.skipBytes(0x4);
            ref.skipBytes(0x28);
        }

        return headerLength;
    }

    /**
     * Returns the folders in this archive.
     */
    public SqPack_Folder[] getPackFolders() {
        return packFolders;
    }

    /**
     * Returns if this archive has no folders (usually the case with index2 and will be for fastloads)
     */
    public boolean hasNoFolders() {
        return noFolder;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < getPackFolders().length; i++) {
            b.append("Folder: ");
//			b.append(String.format("%X", getPackFolders()[i].getId()));
            b.append(getPackFolders()[i].getName());
            b.append("\n");

            b.append("Num files: ");
            b.append(getPackFolders()[i].getFiles().length);
            b.append("\n");

            b.append("Files:\n");

            for (int j = 0; j < getPackFolders()[i].getFiles().length; j++) {
                b.append("\t");
//				b.append(String.format("%X", getPackFolders()[i].getFiles()[j].id));
                b.append(getPackFolders()[i].getFiles()[j].getName());

                b.append(" @ offset ");

                b.append(String.format("%X", getPackFolders()[i].getFiles()[j].dataoffset));
                b.append("\n");
            }
        }

        return b.toString();
    }

    class SqPack_DataSegment {

        private final int offset;
        private final int size;
        private byte[] sha1 = new byte[20];

        SqPack_DataSegment(int offset, int size, byte[] sha1) {
            this.offset = offset;
            this.size = size;
            this.sha1 = sha1;
        }

        int getOffset() {
            return offset;
        }

        int getSize() {
            return size;
        }

        public byte[] getSha1() {
            return sha1;
        }
    }

    public static class SqPack_Folder {

        private final int id;
        private final SqPack_File[] files;
        private final long fileIndexOffset;
        private String name;

        SqPack_Folder(int id, int numFiles, long fileIndexOffset) {
            this.id = id;
            this.files = new SqPack_File[numFiles];
            this.fileIndexOffset = fileIndexOffset;
            this.name = HashDatabase.getFolder(id);
            if (this.name == null)
                this.name = String.format("~%x", id);
            //else
            //HashDatabase.flagFolderNameAsUsed(id);
        }

        void readFiles(EARandomAccessFile ref, JProgressBar prgLoadingBar, JLabel lblLoadingBarString, boolean isIndex2) throws IOException {
            ref.seek(fileIndexOffset);

            for (int i = 0; i < files.length; i++) {
                if (!isIndex2) {
                    int id, id2;

                    // This should be done with shifting as the hash(es) are actually a long
                    if (ref.isBigEndian()) {
                        id2 = ref.readInt();
                        id = ref.readInt();
                    } else {
                        id = ref.readInt();
                        id2 = ref.readInt();
                    }

                    long dataoffset = ref.readInt();
                    ref.readInt();

                    files[i] = new SqPack_File(id, id2, dataoffset, true);

                    if (prgLoadingBar != null)
                        prgLoadingBar.setValue(prgLoadingBar.getValue() + 1);

                    if (lblLoadingBarString != null)
                        lblLoadingBarString.setText((int) (prgLoadingBar.getPercentComplete() * 100) + "%");
                } else {
                    int id = ref.readInt();
                    long dataoffset = ref.readInt();
                    files[i] = new SqPack_File(id, -1, dataoffset, true);

                    if (prgLoadingBar != null)
                        prgLoadingBar.setValue(prgLoadingBar.getValue() + 1);

                    if (lblLoadingBarString != null)
                        lblLoadingBarString.setText((int) (prgLoadingBar.getPercentComplete() * 100) + "%");

                }
            }
        }

        void readFiles(EARandomAccessFile ref, boolean isIndex2) throws IOException {
            ref.seek(fileIndexOffset);

            for (int i = 0; i < files.length; i++) {
                if (!isIndex2) {
                    int id = ref.readInt();
                    int id2 = ref.readInt();
                    long dataoffset = ref.readInt();
                    ref.readInt();

                    files[i] = new SqPack_File(id, id2, dataoffset, true);
                } else {
                    int id = ref.readInt();
                    long dataoffset = ref.readInt();
                    files[i] = new SqPack_File(id, -1, dataoffset, true);
                }
            }
        }

        public int getId() {
            return id;
        }

        public SqPack_File[] getFiles() {
            return files;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class SqPack_File {
        public final int id;
		final int id2;
        public final long dataoffset;
        private String name;

        public SqPack_File(int id, int id2, long offset, boolean loadNames) {
            this.id = id;
            this.id2 = id2;
            this.dataoffset = offset;

            //For Index2
            //if (id2 == -1)
            //this.name = HashDatabase.getFullpath(id);
            //else

            if (loadNames) {

                if (id2 != -1)
                    this.name = HashDatabase.getFileName(id);
                if (this.name == null)
                    this.name = String.format("~%x", id);
            }
            //else
            //HashDatabase.flagFileNameAsUsed(id);
        }

        public int getId() {
            return id;
        }

        public long getOffset() {
            return dataoffset;
        }

        public int getId2() {
            return id2;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName2() {
            String name = null;
            if (id2 != -1)
                name = HashDatabase.getFileName(id);
            if (name == null)
                name = String.format("~%x", id);
            return name;
        }
    }

    /**
     * Gets the content type of the file at the given offset.
     */
    public int getContentType(long dataOffset) throws IOException {
        String pathToOpen = path;

        int datNum = getDatNum(dataOffset);
        long realOffset = getOffsetInBytes(dataOffset);

        pathToOpen = pathToOpen.replace("index2", "dat" + datNum);
        pathToOpen = pathToOpen.replace("index", "dat" + datNum);

        SqPack_DatFile datFile = new SqPack_DatFile(pathToOpen, endian);
        int contentType = datFile.getContentType(realOffset);
        datFile.close();
        return contentType;
    }

    /**
     * Extracts the file at the specified path.
     */
    public byte[] extractFile(String path) throws IOException {
        String folder = path.substring(0, path.lastIndexOf("/"));
        String file = path.substring(path.lastIndexOf("/") + 1);

        return extractFile(folder, file);
    }

    /**
     * Extracts the file at the specified folder with the given filename.
     */
    public byte[] extractFile(String foldername, String filename) throws IOException {
        if (getPath().contains("index2")) {
            String fullPath = foldername + "/" + filename;
            int hash = HashDatabase.computeCRC(fullPath.getBytes(), 0, fullPath.getBytes().length);
            for (SqPack_File f : getPackFolders()[0].getFiles()) {
                if (f.getId() == hash)
                    return extractFile(f.getOffset());
            }
        } else {
            int hash1 = HashDatabase.computeCRC(foldername.getBytes(), 0, foldername.getBytes().length);

            if (!isFastloaded) {
                for (SqPack_Folder f : getPackFolders()) {
                    if (f.getId() == hash1) {

                        int hash2 = HashDatabase.computeCRC(filename.getBytes(), 0, filename.getBytes().length);
                        for (SqPack_File file : f.getFiles()) {
                            if (file.id == hash2) {
                                return extractFile(file.getOffset());
                            }
                        }

                        break;
                    }
                }
            } else {
                for (int i = 0; i < packFolders[0].getFiles().length; i++) {
                    SqPack_File file = packFolders[0].getFiles()[i];
                    if (file.getId2() == hash1) {
                        int hash2 = HashDatabase.computeCRC(filename.getBytes(), 0, filename.getBytes().length);
                        if (file.getId() == hash2)
                            return extractFile(file.getOffset());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extracts the file at the specified offset.
     */
	private byte[] extractFile(long dataoffset) throws IOException {
        return extractFile(dataoffset, null);
    }

    /**
     * Extracts the file at the specified offset... loading bar info also given.
     */
    public byte[] extractFile(long dataOffset, Loading_Dialog loadingDialog) throws IOException {

        String pathToOpen = path;

        int datNum = getDatNum(dataOffset);
        long realOffset = getOffsetInBytes(dataOffset);

        pathToOpen = pathToOpen.replace("index2", "dat" + datNum);
        pathToOpen = pathToOpen.replace("index", "dat" + datNum);

        SqPack_DatFile datFile = new SqPack_DatFile(pathToOpen, endian);
        byte[] data = datFile.extractFile(realOffset, loadingDialog);
        datFile.close();
        return data;
    }

    public int getDatNum(long dataOffset) {
        if (endian == ByteOrder.BIG_ENDIAN)
            return 0;
        return (int) ((dataOffset & 0x000F) / 2);
    }

    public long getOffsetInBytes(long dataOffset) {
        if (endian == ByteOrder.BIG_ENDIAN)
            return dataOffset * 128;    //128 byte alignment

        dataOffset -= dataOffset & 0x000F;
        return dataOffset * 8;            //8 byte alignment
    }

    /**
     * Returns the index file's name.
     */
    public String getName() {

        if (path.lastIndexOf("/") == -1) {
            String frontStripped = path.substring(path.lastIndexOf("\\") + 1);
            return frontStripped.substring(0, frontStripped.indexOf("."));
        }

        String frontStripped = path.substring(path.lastIndexOf("/") + 1);
        return frontStripped.substring(0, frontStripped.indexOf("."));

    }

    public String getPath() {
        return path;
    }

    public Calendar getDatTimestmap(int datNum) throws IOException {
        String pathToOpen = path;

        //Get the correct data number
        pathToOpen = pathToOpen.replace("index2", "dat" + datNum);
        pathToOpen = pathToOpen.replace("index", "dat" + datNum);

        SqPack_DatFile datFile = new SqPack_DatFile(pathToOpen, endian);
        Calendar timestamp = datFile.getTimeStamp();
        datFile.close();
        return timestamp;
    }

    public boolean isBigEndian() {
        return endian == ByteOrder.BIG_ENDIAN;
    }

    public ByteOrder getEndian() {
        return endian;
    }
}
