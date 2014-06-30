package ca.fraggergames.ffxivextract.models;

import java.io.IOException;

import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

public class SqPack_IndexFile {

	private SqPack_DataSegment segments[] = new SqPack_DataSegment[4];
	private SqPack_Folder packFolders[];
	private boolean noFolder = false;

	long offset;
	int size;

	public SqPack_IndexFile(String pathToIndex) throws Exception {

		LERandomAccessFile ref = new LERandomAccessFile(pathToIndex, "r");

		int sqpackHeaderLength = checkSqPackHeader(ref);	
		getSegments(ref, sqpackHeaderLength);

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

				packFolders[i].readFiles(ref);
			}
		} else {
			noFolder = true;
			packFolders = new SqPack_Folder[1];
			packFolders[0] = new SqPack_Folder(0, segments[0].getSize()/0x10, segments[0].getOffset());
			packFolders[0].readFiles(ref);
		}

		ref.close();

	}
	
	public static int checkSqPackHeader(LERandomAccessFile ref) throws IOException{
		// Check SqPack Header
		byte[] buffer = new byte[6];
		ref.readFully(buffer, 0, 6);
		if (buffer[0] != 'S' || buffer[1] != 'q' || buffer[2] != 'P'
				|| buffer[3] != 'a' || buffer[4] != 'c' || buffer[5] != 'k') {
			ref.close();
			throw new IOException("Not a SqPack file");
		}

		// Get Header Length
		ref.seek(0x0c);
		int headerLength = ref.readInt();

		ref.readInt(); // Unknown

		// Get Header Type, has to be 2 for index
		int type = ref.readInt();
		if (type != 2)
			throw new IOException("Not a index");
		
		return headerLength;
	}

	private int getSegments(LERandomAccessFile ref, int segmentHeaderStart)
			throws IOException {

		ref.seek(segmentHeaderStart);
		
		int headerLength = ref.readInt();				

		for (int i = 0; i < segments.length; i++) {
			int firstVal = ref.readInt();
			if (firstVal == 0) // Fell into padding... we done here
				break;
			else if (firstVal < 5) {
				int offset = ref.readInt();
				int size = ref.readInt();
				byte[] sha1 = new byte[20];
				ref.readFully(sha1, 0, 20);
				segments[i] = new SqPack_DataSegment(offset, size, sha1);
			} else {
				int offset = firstVal;
				int size = ref.readInt();
				byte[] sha1 = new byte[20];
				ref.readFully(sha1, 0, 20);
				segments[i] = new SqPack_DataSegment(offset, size, sha1);
			}

			ref.skipBytes(0x2C);
		}
		
		return headerLength;
	}

	public SqPack_Folder[] getPackFolders() {
		return packFolders;
	}
	
	public boolean hasNoFolders()
	{
		return noFolder;
	}
	
	public void displayIndexInfo() {
		for (int i = 0; i < getPackFolders().length; i++) {
			System.out.println("Folder: "
					+ String.format("%X",
							getPackFolders()[i].getId() & 0xFFFFFFFF));
			System.out.println("Num files: "
					+ getPackFolders()[i].getFiles().length);
			System.out.println("Files: ");
			for (int j = 0; j < getPackFolders()[i].getFiles().length; j++) {
				System.out.println("\t"
						+ String.format("%X",
								getPackFolders()[i].getFiles()[j].id)
						+ " @offset "
						+ String.format("%X",
								getPackFolders()[i].getFiles()[j].dataoffset));
			}
		}
	}

	public class SqPack_DataSegment {

		private int offset;
		private int size;
		private byte[] sha1 = new byte[20];
		
		public SqPack_DataSegment(int offset, int size, byte[] sha1)
		{
			this.offset = offset;
			this.size = size;
			this.sha1 = sha1;
		}
		
		public int getOffset()
		{
			return offset;
		}
		
		public int getSize()
		{
			return size;
		}

		public byte[] getSha1()
		{
			return sha1;
		}
	}
	
	public static class SqPack_Folder {
		
		private int id;
		private SqPack_File files[];
		private long fileIndexOffset;
		
		public SqPack_Folder(int id, int numFiles, long fileIndexOffset) {
			this.id = id;
			this.files = new SqPack_File[numFiles];
			this.fileIndexOffset = fileIndexOffset;
		}

		protected void readFiles(LERandomAccessFile ref) throws IOException{
			ref.seek(fileIndexOffset);
			for (int i = 0; i < files.length; i++)
			{			
				int id = ref.readInt();
				int id2 = ref.readInt();
				long dataoffset = ref.readInt() * 8;
				ref.readInt();
			
				files[i] = new SqPack_File(id, id2, dataoffset);			
			}
		}
		
		public int getId()
		{
			return id;
		}
		
		public SqPack_File[] getFiles()
		{
			return files;
		}
		
	}
	
	public static class SqPack_File {
		public int id, id2;
		public long dataoffset;
		
		public SqPack_File(int id, int id2, long offset)
		{
			this.id = id;
			this.id2 = id2;
			this.dataoffset = offset;
		}
		
		public int getId()
		{
			return id;		
		}
		
		public long getOffset()
		{
			return dataoffset;
		}

		public int getId2() {
			return id2;
		}
	}


	
}
