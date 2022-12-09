package com.fragmenterworks.ffxivextract.helpers;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.Main;
import com.fragmenterworks.ffxivextract.models.Texture_File;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Created by Roze on 2017-06-17.
 *
 * @author Roze
 */
public class FileTools {

    static {
        if (Constants.datPath == null) {
            Preferences prefs = Preferences.userNodeForPackage(Main.class);
            Constants.datPath = prefs.get(Constants.PREF_DAT_PATH, null);
        }
    }

    public static BufferedImage getIcon(String sqPakPath, int iconID) {
        if (sqPakPath == null) {
            sqPakPath = Constants.datPath;
        }
        String iconPath = String.format("ui/icon/%06d/%06d.tex", iconID - (iconID % 1000), iconID);
        Utils.getGlobalLogger().debug("IconPath: {}, iconID: {}", iconPath, iconID);
        BufferedImage bi = getTexture(sqPakPath, iconPath);
        return bi;
    }

    public static byte[] getBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            Utils.getGlobalLogger().error("Error reading file: {}", filePath, e);
        }
        return new byte[0];
    }

    public static byte[] peek(String filePath, int offset, int length) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            byte[] data = new byte[length];
            fis.skip(offset);
            fis.read(data);
            fis.close();
            return data;
        } catch (IOException e) {
            Utils.getGlobalLogger().error("Error reading file: {}", filePath, e);
        }
        return new byte[0];
    }

    public static byte[] getRaw(String sqPakPath, String path) {
        if (sqPakPath == null) {
            sqPakPath = Constants.datPath;
        }
        String lowerpath = path.toLowerCase();
        String dat = getDatByPath(lowerpath);

        if (!sqPakPath.endsWith(File.separator)) {
            sqPakPath += File.separator;
        }

        if (!sqPakPath.endsWith("\\game\\sqpack\\ffxiv\\")) {
            sqPakPath += "\\game\\sqpack\\ffxiv\\";
        }

        SqPackIndexFile index = null;
        try {
            index = SqPackIndexFile.read(sqPakPath + dat + ".index");
        } catch (IOException e) {
            Utils.getGlobalLogger().error("Error reading index file: {}", sqPakPath + dat + ".index", e);
        }

        if (index != null) {
            try {
                return index.extractFile(lowerpath);
            } catch (Exception e) {
                Utils.getGlobalLogger().error("", e);
            }
        }
        return new byte[0];
    }

    private static String getDatByPath(final String lowerpath) {
        String root = lowerpath.substring(0, lowerpath.indexOf("/"));
        String dat;
        switch (root) {
            case "exd": {
                dat = "0a0000";
                break;
            }
            case "game_script": {
                dat = "0b0000";
                break;
            }
            case "music": {
                dat = "0c0000";
                break;
            }
            case "common": {
                dat = "000000";
                break;
            }
            case "bgcommon": {
                dat = "010000";
                break;
            }
            case "bg": {
                dat = "020000";
                break;
            }
            case "cut": {
                dat = "030000";
                break;
            }
            case "chara": {
                dat = "040000";
                break;
            }
            case "shader": {
                dat = "050000";
                break;
            }
            case "ui": {
                dat = "060000";
                break;
            }
            case "sound": {
                dat = "070000";
                break;
            }
            case "vfx": {
                dat = "080000";
                break;
            }
            default:
                throw new RuntimeException("Unknown root: " + root);
        }
        dat += ".win32";
        return dat;
    }

    public static BufferedImage getTexture(String path) {
        return getTexture(null, path);
    }

    public static BufferedImage getTexture(String sqPakPath, String path) {
        if (sqPakPath == null) {
            sqPakPath = Constants.datPath;
        }
        byte[] data = getRaw(sqPakPath, path);

        //TODO: Random ULD stuff can be little-endian, no?
        Texture_File tf = new Texture_File(data, ByteOrder.LITTLE_ENDIAN);
        try {
            BufferedImage bf = tf.decode(0, new HashMap<>());
            return bf;
        } catch (ImageDecoding.ImageDecodingException e) {
            Utils.getGlobalLogger().error("", e);
        }
        return null;
    }
}
