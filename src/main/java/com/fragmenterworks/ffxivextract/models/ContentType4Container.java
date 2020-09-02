package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.models.SqPack_DatFile.TextureBlocks;

class ContentType4Container {

    private final TextureBlocks[] blocks;
    private final long[] blockOffsets;

    public ContentType4Container(int blockCount) {
        blocks = new TextureBlocks[blockCount];
        blockOffsets = new long[blockCount];
    }


}
