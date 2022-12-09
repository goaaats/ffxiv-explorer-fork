package com.fragmenterworks.ffxivextract.models.sqpack.index;

import com.fragmenterworks.ffxivextract.gui.components.Loading_Dialog;
import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.FileTools;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.SqPack_DatFile;
import com.fragmenterworks.ffxivextract.models.sqpack.SqPackVersionInfo;
import com.fragmenterworks.ffxivextract.models.sqpack.model.SqPackFile;
import com.fragmenterworks.ffxivextract.models.sqpack.model.SqPackFolder;
import com.fragmenterworks.ffxivextract.paths.PathUtils;
import com.fragmenterworks.ffxivextract.paths.database.HashDatabase;
import com.fragmenterworks.ffxivextract.paths.database.HashUpdateNotification;
import com.fragmenterworks.ffxivextract.paths.database.IHashUpdateListener;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SqPackIndexFile implements IHashUpdateListener {

	private static final Map<String, SqPackIndexFile> cachedIndexes = new HashMap<>();
	private static final Map<String, SqPack_DatFile> datFileCache = new HashMap<>();

	private final String indexFilePath;
	private final int indexId;
	private final ByteOrder byteOrder;
	private final SqPackVersionInfo versionInfo;
	private final IndexFileInfo fileInfo;
	private final List<HashElement32> hashes32;
	private final List<HashElement64> hashes64;
	private final List<Synonym32> synonyms32;
	private final List<Synonym64> synonyms64;

	private final HashMap<String, SqPackFolder> folderCache;
	private final List<SqPackFolder> folders;
	private final List<SqPackFile> files;

	private final List<Path> correspondingDatFiles;

	private int itemsLoaded = 0;
	private int itemsUnhashed = 0;

	private List<IIndexUpdateListener> listeners = new ArrayList<>();

	public static SqPackIndexFile read(String filePath, JProgressBar progressBar, JLabel label) throws FileNotFoundException {
		if (cachedIndexes.containsKey(filePath)) {
			return cachedIndexes.get(filePath);
		} else {
			SqPackIndexFile index = new SqPackIndexFile(filePath, progressBar, label);
			cachedIndexes.put(filePath, index);
			return index;
		}
	}

	public static SqPackIndexFile read(String filePath) throws FileNotFoundException {
		return read(filePath, null, null);
	}

	public static void removeFromCache(SqPackIndexFile index) {
		cachedIndexes.remove(index.indexFilePath);
	}

	private SqPackIndexFile(String filePath, JProgressBar progressBar, JLabel loadLabel) throws FileNotFoundException {

		indexFilePath = filePath;
		HashDatabase.addListener(this);
		listeners = new ArrayList<>();

		// Determine index file ID
		var fileName = Paths.get(filePath).getFileName().toString();
		var idPart = fileName.indexOf('.');
		this.indexId = Integer.parseInt(fileName.substring(0, idPart), 16);

		// Determine platform and endianness
		byte platform = FileTools.peek(filePath, 8, 1)[0];
		byteOrder = platform == 1 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
		EARandomAccessFile file = new EARandomAccessFile(filePath, "r", byteOrder);

		// Read final fields here, then populate hash content afterwards
		versionInfo = SqPackVersionInfo.read(file);
		fileInfo = IndexFileInfo.read(file);
		hashes32 = new ArrayList<>();
		hashes64 = new ArrayList<>();
		synonyms32 = new ArrayList<>();
		synonyms64 = new ArrayList<>();
		var t = System.currentTimeMillis();
		loadContent(file, progressBar, loadLabel);
		var e = System.currentTimeMillis() - t;
		Utils.getGlobalLogger().info("Index file {} loaded in {} ms", String.format("%06x", indexId), e);

		folderCache = new HashMap<>();
		folders = new ArrayList<>();
		files = new ArrayList<>();
		t = System.currentTimeMillis();
		parseContent(progressBar, loadLabel);
		e = System.currentTimeMillis() - t;
		Utils.getGlobalLogger().info("Index file {} parsed in {} ms", String.format("%06x", indexId), e);

		// Set up dat file access
		var datFileFormat = filePath.replace("index2", "dat%d");
		datFileFormat = datFileFormat.replace("index", "dat%d");
		correspondingDatFiles = new ArrayList<>();

		for(int i = 0; ; i++) {
			var datFilePath = String.format(datFileFormat, i);
			var datPath = Paths.get(datFilePath);
			if (datPath.toFile().exists()) {
				correspondingDatFiles.add(datPath);
			} else {
				break;
			}
		}
	}

	private void loadContent(EARandomAccessFile file, JProgressBar progressBar, JLabel loadLabel) {
		if (fileInfo.indexType == 0) {
			int hashCount = fileInfo.indexDataSize / 16;
			int synonymCount = fileInfo.collisionSize / 256;

			if (progressBar != null) {
				progressBar.setMaximum(hashCount + synonymCount);
				progressBar.setValue(0);
			}

			for (int i = 0; i < hashCount; i++) {
				if (progressBar != null) {
					progressBar.setValue(i);
					loadLabel.setText("Loading hashes: " + i + "/" + hashCount);
				}

				HashElement64 element = HashElement64.read(file);
				if (element.hash == -1) continue;
				hashes64.add(element);
			}

			for (int i = 0; i < synonymCount; i++) {
				if (progressBar != null) {
					progressBar.setValue(hashCount + i);
					loadLabel.setText("Loading synonyms: " + i + "/" + synonymCount);
				}

				Synonym64 element = Synonym64.read(file);
				if (element.hash == -1) continue;
				synonyms64.add(element);
			}
		} else if (fileInfo.indexType == 2) {
			int hashCount = fileInfo.indexDataSize / 8;
			int synonymCount = fileInfo.collisionSize / 256;

			if (progressBar != null) {
				progressBar.setMaximum(hashCount + synonymCount);
				progressBar.setValue(0);
			}

			for (int i = 0; i < hashCount; i++) {
				if (progressBar != null) {
					progressBar.setValue(i);
					loadLabel.setText("Loading hashes: " + i + "/" + hashCount);
				}

				HashElement32 element = HashElement32.read(file);
				if (element.hash == -1) continue;
				hashes32.add(element);
			}

			for (int i = 0; i < synonymCount; i++) {
				if (progressBar != null) {
					progressBar.setValue(hashCount + i);
					loadLabel.setText("Loading synonyms: " + i + "/" + synonymCount);
				}

				Synonym32 element = Synonym32.read(file);
				if (element.hash == -1) continue;
				synonyms32.add(element);
			}
		}
	}

	private void parseElement64(HashElement64 element) {

		if (element.isSynonym())
			return;

		String folderName = null;
		String fileName = null;

		var folderHash = element.getFolderHash();
		var fileHash = element.getFileHash();

		var entry = HashDatabase.getFullPathEntry(indexId, folderHash, fileHash);

		// There's no full path with these hashes, but we can name them separately
		if (entry == null) {
			folderName = HashDatabase.getFolder(indexId, folderHash);
			fileName = HashDatabase.getFile(indexId, folderHash, fileHash);
		} else {
			folderName = entry.getFolderName();
			fileName = entry.getFileName();
		}

		String finalFolderName = folderName;
		SqPackFolder folder = folderCache.getOrDefault(finalFolderName, null);
		if (folder == null) {
			folder = new SqPackFolder(element, element.getFolderHash(), folderName);
			folderCache.put(folder.getName(), folder);
		}

		var file = new SqPackFile(element, element.getFileHash(), fileName, folder);
		folder.getFiles().add(file);
		files.add(file);

		var unhashed = 0;
		if (file.getName().charAt(0) != '~') unhashed++;
		if (folder.getName().charAt(0) != '~') unhashed++;

		itemsLoaded += 2;
		itemsUnhashed += unhashed;
	}

	private void parseElement32(HashElement32 element) {

		if (element.isSynonym())
			return;

		String folderName = null;
		String fileName = null;
		String fullName = null;

		var entry = HashDatabase.getFullPathEntry(indexId, element.hash);

		// If there's no full path with these hashes we must fall back
		// to a single name which will be used as the "file name"
		if (entry == null) {
			// This is only used for the string format. Keeps it one place
			fullName = HashDatabase.getFullPath(indexId, element.hash);
		} else {
			folderName = entry.getFolderName();
			fileName = entry.getFileName();
		}

		// if fullName is null, folderName and fileName are populated
		if (fullName == null) {
			var crc = PathUtils.computeHashesWithLower(folderName + "/" + fileName);
			SqPackFolder folder = folderCache.getOrDefault(folderName, null);
			if (folder == null) {
				folder = new SqPackFolder(element, crc.folderHash, folderName);
				folderCache.put(folder.getName(), folder);
			}

			var file = new SqPackFile(element, crc.fileHash, fileName, folder);
			folder.getFiles().add(file);
			files.add(file);
			itemsUnhashed += 1;
		} else {
			var file = new SqPackFile(element, element.hash, fullName, null);
			files.add(file);
		}

		itemsLoaded += 1;
	}

	private void parseSynonym(SynonymElement synonym) {
		var fullPath = synonym.path;
		var lastSlash = fullPath.lastIndexOf('/');
		var folderName = fullPath.substring(0, lastSlash);
		var fileName = fullPath.substring(lastSlash + 1);

		var crc = PathUtils.computeHashesWithLower(folderName + "/" + fileName);

		SqPackFolder folder = folderCache.getOrDefault(folderName, null);
		if (folder == null) {
			folder = new SqPackFolder(synonym, crc.folderHash, folderName);
			folderCache.put(folder.getName(), folder);
		}

		var file = new SqPackFile(synonym, crc.fileHash, fileName, folder);
		folder.getFiles().add(file);
		files.add(file);

		itemsLoaded += 2;
		itemsUnhashed += 2;
	}

	private void parseContent(JProgressBar progressBar, JLabel loadLabel) {

		if (fileInfo.indexType == 0) {
			if (progressBar != null) {
				progressBar.setMaximum(synonyms64.size() + hashes64.size());
				progressBar.setValue(0);
			}

			for (var synonym : synonyms64) {
				if (progressBar != null)
					progressBar.setValue(progressBar.getValue() + 1);
				if (loadLabel != null)
					loadLabel.setText("Parsing: " + progressBar.getValue() + "/" + progressBar.getMaximum());
				parseSynonym(synonym);
			}

			for (var element : hashes64) {
				if (progressBar != null)
					progressBar.setValue(progressBar.getValue() + 1);
				if (loadLabel != null)
					loadLabel.setText("Parsing: " + progressBar.getValue() + "/" + progressBar.getMaximum());

				parseElement64(element);
			}
		} else if (fileInfo.indexType == 2) {

			if (progressBar != null) {
				progressBar.setMaximum(synonyms32.size() + hashes32.size());
				progressBar.setValue(0);
			}

			for (var synonym : synonyms32) {
				if (progressBar != null)
					progressBar.setValue(progressBar.getValue() + 1);
				if (loadLabel != null)
					loadLabel.setText("Parsing: " + progressBar.getValue() + "/" + progressBar.getMaximum());
				parseSynonym(synonym);
			}

			for (var element : hashes32) {
				if (progressBar != null)
					progressBar.setValue(progressBar.getValue() + 1);
				if (loadLabel != null)
					loadLabel.setText("Parsing: " + progressBar.getValue() + "/" + progressBar.getMaximum());

				parseElement32(element);
			}
		}

		folders.addAll(folderCache.values());
		sortAll();
	}

	private void sortAll() {
		folders.sort(Comparator.comparing(SqPackFolder::getName));
		for (var folder : folders)
			folder.getFiles().sort(Comparator.comparing(SqPackFile::getName));
		files.sort(Comparator.comparing(SqPackFile::getName));
	}

	private boolean handleHashUpdate(HashUpdateNotification notification) {
		Utils.getGlobalLogger().debug("Received hash update notification " + notification);

		if (notification.indexId != this.indexId) return false;

		boolean dirty = false;

		for (var folder : folders) {
			if (folder.getHash() == notification.folderHash) {
				if (folder.getName().charAt(0) == '~') {
					folder.setName(notification.folder);
					dirty = true;
				}


				for (var file : folder.getFiles()) {
					if (file.getName().charAt(0) != '~') continue;
					if (file.getHash() == notification.fileHash) {
						file.setName(notification.fileName);
						dirty = true;
						break;
					}
				}
				break;
			}
		}

		for (var file : files) {
			if (file.getName().charAt(0) != '~') continue;
			if (file.getParent() != null) continue;
			if (file.getHash() == notification.fullHash) {
				var folder = folderCache.getOrDefault(notification.folder, null);
				if (folder == null) {
					folder = new SqPackFolder(file.getElement(), notification.folderHash, notification.folder);
					folderCache.put(folder.getName(), folder);
					folders.add(folder);
				}
				folder.getFiles().add(file);
				file.setName(notification.fileName);
				file.setParent(folder);
				dirty = true;
				break;
			}
		}
		return dirty;
	}

	private void sendHashUpdate() {
		sortAll();

		for (var listener : listeners)
			listener.onIndexUpdate();
	}

	@Override
	public void onHashUpdate(HashUpdateNotification notification) {
		if (handleHashUpdate(notification))
			sendHashUpdate();
	}

	@Override
	public void onMultipleHashUpdate(List<HashUpdateNotification> notifications) {
		var dirty = false;
		for (var notification : notifications)
			dirty |= handleHashUpdate(notification);
		if (dirty)
			sendHashUpdate();
	}

	public SqPackIndexFile getIndexForIdFromSameRepo(int targetIndexId) throws FileNotFoundException {
		var targetFile = indexFilePath;
		var indexIdStr = String.format("%06X", indexId);
		var targetIndexIdStr = String.format("%06X", targetIndexId);
		targetFile = targetFile.replace(indexIdStr, targetIndexIdStr);

		var ourExpac = PathUtils.getExpacString(indexId);
		var targetExpac = PathUtils.getExpacString(targetIndexId);
		if (!ourExpac.equals(targetExpac)) {
			targetFile = targetFile.replace(ourExpac, targetExpac);
		}

		if (cachedIndexes.containsKey(targetFile)) {
			return cachedIndexes.get(targetFile);
		} else {
			SqPackIndexFile index = new SqPackIndexFile(targetFile, null, null);
			cachedIndexes.put(targetFile, index);
			return index;
		}
	}

	public boolean fileExists(String s) {
		var hashes = PathUtils.computeHashesWithLower(s);
		return fileExists(hashes.folderHash, hashes.fileHash) || fileExists(hashes.fullHash);
	}

	public boolean fileExists(int folderHash, int fileHash) {
		for (var element : hashes64) {
			if (element.getFolderHash() == folderHash && element.getFileHash() == fileHash) {
				return true;
			}
		}
		return false;
	}

	public boolean fileExists(int fullHash) {
		for (var element : hashes32) {
			if (element.hash == fullHash) {
				return true;
			}
		}
		return false;
	}

	public byte[] extractFile(String s) {
		var hashes = PathUtils.computeHashesWithLower(s);

		if (!hashes64.isEmpty()) {
			return extractFile(hashes.folderHash, hashes.fileHash);
		} else {
			return extractFile(hashes.fullHash);
		}
	}

	public byte[] extractFile(String s, Loading_Dialog load) {
		var hashes = PathUtils.computeHashesWithLower(s);

		if (!hashes64.isEmpty()) {
			return extractFile(hashes.folderHash, hashes.fileHash, load);
		} else {
			return extractFile(hashes.fullHash, load);
		}
	}

	public byte[] extractFile(int folderHash, int fileHash) {
		for (var element : hashes64) {
			if (element.getFolderHash() == folderHash && element.getFileHash() == fileHash) {
				return extractFile(element);
			}
		}
		return null;
	}

	public byte[] extractFile(int folderHash, int fileHash, Loading_Dialog load) {
		for (var element : hashes64) {
			if (element.getFolderHash() == folderHash && element.getFileHash() == fileHash) {
				return extractFile(element, load);
			}
		}
		return null;
	}

	public byte[] extractFile(int fullHash) {
		for (var element : hashes32) {
			if (element.hash == fullHash) {
				return extractFile(element);
			}
		}
		return null;
	}

	public byte[] extractFile(int fullHash, Loading_Dialog load) {
		for (var element : hashes32) {
			if (element.hash == fullHash) {
				return extractFile(element, load);
			}
		}
		return null;
	}

	public byte[] extractFile(HashElement element) {
		return extractFile(element, null);
	}

	public byte[] extractFile(HashElement element, Loading_Dialog loadingDialog) {
		var offset = element.getOffset();
		var dat = element.getDatafileId();
		try {
			var datFilePath = correspondingDatFiles.get(dat).toString();
			SqPack_DatFile datFile;
			if (datFileCache.containsKey(datFilePath)) {
				datFile = datFileCache.get(datFilePath);
			} else {
				datFile = new SqPack_DatFile(correspondingDatFiles.get(dat).toString(), byteOrder);
				datFileCache.put(datFilePath, datFile);
			}
			return datFile.extractFile(offset, loadingDialog);
		} catch (Exception e) {
			Utils.getGlobalLogger().error("Failed to open dat file: {}", correspondingDatFiles.get(dat).toString(), e);
		}
		return null;
	}

	public int getContentType(String s) {
		var hashes = PathUtils.computeHashesWithLower(s);

		if (!hashes64.isEmpty()) {
			return getContentType(hashes.folderHash, hashes.fileHash);
		} else {
			return getContentType(hashes.fullHash);
		}
	}

	public int getContentType(int folderHash, int fileHash) {
		for (var element : hashes64) {
			if (element.getFolderHash() == folderHash && element.getFileHash() == fileHash) {
				return getContentType(element);
			}
		}
		return -1;
	}

	public int getContentType(int fullHash) {
		for (var element : hashes32) {
			if (element.hash == fullHash) {
				return getContentType(element);
			}
		}
		return -1;
	}

	public int getContentType(HashElement element) {
		var offset = element.getOffset();
		var dat = element.getDatafileId();
		try {
			var datFilePath = correspondingDatFiles.get(dat).toString();
			if (datFileCache.containsKey(datFilePath)) {
				var datFile = datFileCache.get(datFilePath);
				return datFile.getContentType(offset);
			} else {
				var datFile = new SqPack_DatFile(correspondingDatFiles.get(dat).toString(), byteOrder);
				datFileCache.put(datFilePath, datFile);
				return datFile.getContentType(offset);
			}
		} catch (Exception e) {
			Utils.getGlobalLogger().error("Failed to open dat file: {}", correspondingDatFiles.get(dat).toString(), e);
		}
		return -1;
	}

	public boolean isBigEndian() {
		return byteOrder == ByteOrder.BIG_ENDIAN;
	}

	public ByteOrder getEndian() {
		return byteOrder;
	}

	public int getIndexId() {
		return indexId;
	}

	public List<SqPackFolder> getFolders() {
		return folders;
	}

	public List<SqPackFile> getFiles() {
		return files;
	}

	public int getNumItemsUnhashed() {
		return itemsUnhashed;
	}

	public int getNumItemsLoaded() {
		return itemsLoaded;
	}

	public void addListener(IIndexUpdateListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IIndexUpdateListener listener) {
		listeners.remove(listener);
	}
}