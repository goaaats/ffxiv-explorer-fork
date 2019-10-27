package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.ShaderIdHelper;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.shaders.*;
import com.fragmenterworks.ffxivextract.storage.HashDatabase;
import com.jogamp.opengl.GL3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Material extends Game_File {

    String materialPath;

    //Data
    private int fileSize;
    private int colorTableSize;
    private int stringSectionSize;
    private int shaderStringOffset;
    private short numPaths;
    private short numMaps;
    private short numColorSets;
    private short numUnknown;

    private String[] stringArray;

    private Texture_File diffuse;
    private Texture_File mask;
    private Texture_File normal;
    private Texture_File specular;
    private Texture_File colorSet;

    private Unknown1[] unknownList1;
    private Unknown2[] unknownList2;
    private Parameter[] parameterList;

    private byte[] colorSetData;

    private int unknownDataSize;
    private byte[] unknownData;

    //Rendering
    private boolean shaderReady = false;

    private Shader shader;

    //Rendering
    final int[] textureIds = new int[5];

    //Constructor grabs info about material
    public Material(byte[] data, ByteOrder endian) {
        this(null, null, data, endian);
    }

    //Constructor grabs info and texture files
    public Material(String folderPath, SqPack_IndexFile currentIndex, byte[] data, ByteOrder endian) {
        super(endian);

        if (data == null)
            return;

        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(endian);
        bb.getInt();
        fileSize = bb.getShort();
        colorTableSize = bb.getShort();
        stringSectionSize = bb.getShort();
        shaderStringOffset = bb.getShort();
        numPaths = bb.get();
        numMaps = bb.get();
        numColorSets = bb.get();
        numUnknown = bb.get();

        //Load Strings
        stringArray = new String[numPaths + numMaps + numColorSets + 1];
        byte[] stringBuffer = new byte[stringSectionSize];
        bb.position(bb.position() + (4 * (numPaths + numMaps + numColorSets)));
        bb.get(stringBuffer);

        int stringCounter = 0;
        int start = 0, end = 0;
        for (int i = 0; i < stringBuffer.length; i++) {
            if (stringBuffer[i] == 0) {
                if (stringCounter >= stringArray.length)
                    break;
                stringArray[stringCounter] = new String(stringBuffer, start, end - start);
                start = end + 1;
                stringCounter++;
            }
            end++;
        }

        //Load Textures
        if (folderPath != null && currentIndex != null) {
            for (int i = 0; i < numPaths; i++) {
                String s = stringArray[i];

                if (!s.contains("/")) {
                    Utils.getGlobalLogger().debug("Couldn't load {}", s);
                    continue;
                }

                String folderName = s.substring(0, s.lastIndexOf("/"));
                String fileString = s.substring(s.lastIndexOf("/") + 1);

                HashDatabase.addPathToDB(s, currentIndex.getName());

                try {
                    byte[] extracted = currentIndex.extractFile(folderName, fileString);
                    if (extracted == null)
                        continue;

                    if ((fileString.endsWith("_d.tex") || fileString.contains("catchlight")) && diffuse == null)
                        diffuse = new Texture_File(extracted, endian);
                    else if (fileString.endsWith("_n.tex") && normal == null)
                        normal = new Texture_File(extracted, endian);
                    else if (fileString.endsWith("_s.tex") && specular == null)
                        specular = new Texture_File(extracted, endian);
                    else if (fileString.endsWith("_m.tex"))
                        mask = new Texture_File(extracted, endian);
                    else
                        colorSet = new Texture_File(extracted, endian);

                } catch (FileNotFoundException e) {
                    Utils.getGlobalLogger().error(e);
                } catch (IOException e) {
                    Utils.getGlobalLogger().error(e);
                }

            }
        }

        //This is for the new material file setup. ALso bgcolorchange doesn't have a table color but can't find where it says that.
        if (colorSet == null && colorTableSize >= 512) {
            bb.position(16 + (4 * (numPaths + numMaps + numColorSets)) + stringBuffer.length);
            if (bb.getInt() == 0x0)
                return;
            colorSetData = new byte[512];
            bb.get(colorSetData);
        }

        //Shader links and unknowns start here
        bb.position(16 + (4 * (numPaths + numMaps + numColorSets)) + stringBuffer.length + colorTableSize + numUnknown);

        unknownDataSize = bb.getShort();
        int count1 = bb.getShort();
        int count2 = bb.getShort();
        int count3 = bb.getShort();
        bb.getShort();
        bb.getShort();

        unknownData = new byte[unknownDataSize];
        unknownList1 = new Unknown1[count1];
        unknownList2 = new Unknown2[count2];
        parameterList = new Parameter[count3];

        for (int i = 0; i < unknownList1.length; i++)
            unknownList1[i] = new Unknown1(bb.getInt(), bb.getInt());
        for (int i = 0; i < unknownList2.length; i++)
            unknownList2[i] = new Unknown2(bb.getInt(), bb.getShort(), bb.getShort());
        for (int i = 0; i < parameterList.length; i++)
            parameterList[i] = new Parameter(bb.getInt(), bb.getShort(), bb.getShort(), bb.getInt());

        bb.get(unknownData);
    }

    public void loadShader(GL3 gl) {
        //Weird case
        if (stringArray[stringArray.length - 1] == null) {
            int x = stringArray.length - 1;
            do {
                x--;
                stringArray[stringArray.length - 1] = stringArray[x];
            } while (stringArray[x] == null && x >= 0);
        }

        //Load Shader
        String shaderName = "";
        for (int i = 0; i < stringArray.length; i++) {
            if (stringArray[i].contains(".shpk")) {
                shaderName = stringArray[i];
                break;
            }
        }

        try {
            if (shaderName.equals("character.shpk"))
                shader = new CharacterShader(gl);
            else if (shaderName.equals("hair.shpk"))
                shader = new HairShader(gl);
            else if (shaderName.equals("iris.shpk"))
                shader = new IrisShader(gl);
            else if (shaderName.equals("skin.shpk"))
                shader = new SkinShader(gl);
            else if (shaderName.equals("bg.shpk") || shaderName.equals("bgcolorchange.shpk"))
                shader = new BGShader(gl);
            else
                shader = new DefaultShader(gl);
        } catch (IOException e) {
            Utils.getGlobalLogger().error(e);
        }

        shaderReady = true;
    }

    public Shader getShader() {
        return shader;
    }

    public boolean isShaderReady() {
        return shaderReady;
    }

    public Texture_File getDiffuseMapTexture() {
        return diffuse;
    }

    public Texture_File getMaskTexture() {
        return mask;
    }

    public Texture_File getNormalMapTexture() {
        return normal;
    }

    public Texture_File getSpecularMapTexture() {
        return specular;
    }

    public Texture_File getColorSetTexture() {
        return colorSet;
    }

    public byte[] getColorSetData() {
        return colorSetData;
    }

    public int[] getGLTextureIds() {
        return textureIds;
    }

    class Unknown1 {
        final int unknown1;
        final int unknown2;

        Unknown1(int unknown1, int unknown2) {
            this.unknown1 = unknown1;
            this.unknown2 = unknown2;
        }
    }

    class Unknown2 {
        final int unknown1;
        final short offset;
        final short size;

        Unknown2(int unknown1, short offset, short size) {
            this.unknown1 = unknown1;
            this.offset = offset;
            this.size = size;
        }
    }

    class Parameter {
        final int id;
        final short unknown1;
        final short unknown2;
        final int index;

        Parameter(int id, short unknown1, short unknown2, int index) {
            this.id = id;
            this.unknown1 = unknown1;
            this.unknown2 = unknown2;
            this.index = index;

            Utils.getGlobalLogger().debug("Shader name: {}", ShaderIdHelper.getName(id));
        }
    }
}
