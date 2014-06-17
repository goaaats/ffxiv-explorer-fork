package ca.fraggergames.ffxivextract.models;

import java.io.IOException;

import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;

import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

public class SqPack_IndexFile {

	private SqPack_Folder packFolders[];
	
	long offset;
	int size;	
	
	public SqPack_IndexFile(String pathToIndex) throws Exception{
		
		byte[] buffer = new byte[6];

			LERandomAccessFile ref = new LERandomAccessFile(pathToIndex, "r");
			ref.readFully(buffer, 0, 6);
			//Check Header
			if (buffer[0] == 'S' && buffer[1] == 'q' && buffer[2] == 'P'
					&& buffer[3] == 'a' && buffer[4] == 'c' && buffer[5] == 'k') {
				ref.seek(0x14);
				
				//If type isn't 2, this is not an index
				int type = ref.readInt();
				if (type != 2)
					throw new IOException("Not a index");
				
				
				ref.seek(0x04e4);
				
				int offset = ref.readInt();
				int size = ref.readInt();
				int numFolders = size/0x10;
				
				packFolders = new SqPack_Folder[numFolders];
				
				for (int i = 0; i < numFolders; i++)
				{
					ref.seek(offset + (i * 16)); //Every folder offset header is 16 bytes
					
					int id = ref.readInt();
					int fileIndexOffset = ref.readInt();
					int folderSize = ref.readInt();
					int numFiles = folderSize/0x10;
					ref.readInt(); //Skip												
					
					packFolders[i] = new SqPack_Folder(id, numFiles, fileIndexOffset);															
					
					packFolders[i].readFiles(ref);
				}
				
				ref.close();				
			}
			else{
				ref.close();
				throw new IOException("Not a SqPack file");			
			}		
	}
	
	public SqPack_Folder[] getPackFolders(){
		return packFolders;
	}
	
	public void displayIndexInfo(){
		for (int i = 0; i < getPackFolders().length; i++){
			System.out.println("Folder: " + String.format("%X", getPackFolders()[i].getId() & 0xFFFFFFFF));
			System.out.println("Num files: " + getPackFolders()[i].getFiles().length);
			System.out.println("Files: ");
			for (int j = 0; j < getPackFolders()[i].getFiles().length;j++){
				System.out.println("\t" + String.format("%X", getPackFolders()[i].getFiles()[j].id ) + " @offset " + String.format("%X", getPackFolders()[i].getFiles()[j].dataoffset));
			}
		}
	}
}
