package com.fragmenterworks.ffxivextract.models;

class ContentType3Container {

    public final int[] chunkDecompressedSizes = new int[11];
    public final int[] chunkSizes = new int[11];
    public final int[] chunkOffsets = new int[11];

    public final int[] chunkStartBlockIndex = new int[11];
    public final int[] chunkNumBlocks = new int[11];

    public short[] blockSizes;

    public short numMeshes;
    public short numMaterials;

}
