package ca.fraggergames.ffxivextract.models;

import ca.fraggergames.ffxivextract.models.SqPack_DatFile.Data_Block;
import ca.fraggergames.ffxivextract.models.SqPack_DatFile.TextureBlocks;

public class ContentType4Container {
	
	public TextureBlocks blocks[];
	public long blockOffsets[];

	public ContentType4Container(int blockCount) {
		blocks = new TextureBlocks[blockCount];
		blockOffsets = new long[blockCount];
	}


}
