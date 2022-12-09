package com.fragmenterworks.ffxivextract.helpers;

public class HashFinding_Utils {

    //TODO rework this? idk
    public static void findExhHashes() {
//        Utils.getGlobalLogger().info("Opening root.exl...");
//
//        byte[] rootData = null;
//        SqPack_IndexFile index = null;
//        try {
//            index = new SqPack_IndexFile(Constants.datPath + "\\game\\sqpack\\ffxiv\\0a0000.win32.index", true);
//            rootData = index.extractFile("exd/root.exl");
//        } catch (IOException e) {
//            Utils.getGlobalLogger().error("", e);
//        }
//
//        if (rootData == null)
//            return;
//
//        Utils.getGlobalLogger().info("Loaded root.exl...");
//
//        try {
//            InputStream in = new ByteArrayInputStream(rootData);
//            StringBuilder sBuilder = new StringBuilder();
//
//            HashDatabase.beginConnection();
//            try {
//                HashDatabase.setAutoCommit(false);
//            } catch (SQLException e1) {
//                Utils.getGlobalLogger().error(e1);
//            }
//            boolean pastHeader = false;
//            boolean readingString = false;
//            while (true) {
//                int b = in.read();
//
//                //Get Past Header
//                if (!pastHeader && b == 0x0D) {
//                    int b2 = in.read();
//                    if (b2 == 0x0A) {
//                        pastHeader = true;
//                        readingString = true;
//                        continue;
//                    }
//                    continue;
//                } else if (!pastHeader)
//                    continue;
//
//                if (b == -1)
//                    break;
//
//                if (readingString && b == ',') {
//                    readingString = false;
//                    for (String code : EXHF_File.languageCodes) {
//                        String exdName = String.format("exd/%s_0%s.exd", sBuilder.toString(), code);
//                        HashDatabase.addPath(exdName);
//                    }
//                    String exhName = String.format("exd/%s.exh", sBuilder.toString());
//                    HashDatabase.addPath(exhName);
//                    sBuilder.setLength(0);
//                } else if (!readingString && b == 0x0A) {
//                    readingString = true;
//                } else if (readingString)
//                    sBuilder.append((char) b);
//
//            }
//            try {
//                HashDatabase.commit();
//            } catch (SQLException e) {
//                Utils.getGlobalLogger().error("", e);
//            }
//            HashDatabase.closeConnection();
//            in.close();
//
//        } catch (IOException e1) {
//            Utils.getGlobalLogger().error(e1);
//        }
//
//        Utils.getGlobalLogger().info("Done searching for EXD/EXHs.");
    }

    public static void findMusicHashes() {
//        Utils.getGlobalLogger().info("Opening bgm.exh...");
//
//        byte[] exhData = null;
//        SqPack_IndexFile index = null;
//        try {
//            index = new SqPack_IndexFile(Constants.datPath + "\\game\\sqpack\\ffxiv\\0a0000.win32.index", true);
//            exhData = index.extractFile("exd/bgm.exh");
//        } catch (IOException e) {
//            Utils.getGlobalLogger().error("", e);
//        }
//
//        if (exhData == null)
//            return;
//
//        EXDF_View viewer = null;
//        try {
//            viewer = new EXDF_View(index, "exd/bgm.exh", new EXHF_File(exhData));
//        } catch (IOException e2) {
//            Utils.getGlobalLogger().error(e2);
//        }
//
//        if (viewer == null)
//            return;
//
//        Utils.getGlobalLogger().info("bgm.exh loaded.");
//
//        HashDatabase.beginConnection();
//        try {
//            HashDatabase.setAutoCommit(false);
//        } catch (SQLException e1) {
//            Utils.getGlobalLogger().error(e1);
//        }
//        for (int i = 1; i < viewer.getTable().getRowCount(); i++) {
//            String path = String.format("%s", viewer.getTable().getValueAt(i, 1));
//
//            if (path == null || path.isEmpty())
//                continue;
//
//            String archive = path.contains("ex1") ? "0c0100" : "0c0000";
//
//            HashDatabase.addPath(path);
//        }
//
//        try {
//            HashDatabase.commit();
//        } catch (SQLException e) {
//            Utils.getGlobalLogger().error("", e);
//        }
//        HashDatabase.closeConnection();
//
//        Utils.getGlobalLogger().info("Done searching for BGM.");
    }

    public static void findMapHashes() {
//        try {
//            byte[] exhData = null;
//            SqPack_IndexFile index = null;
//            try {
//                index = new SqPack_IndexFile(Constants.datPath + "\\game\\sqpack\\ffxiv\\0a0000.win32.index", true);
//                exhData = index.extractFile("exd/map.exh");
//            } catch (IOException e) {
//                Utils.getGlobalLogger().error("", e);
//            }
//
//            if (exhData == null)
//                return;
//
//            EXHF_File exhfFile = new EXHF_File(exhData);
//            EXDF_View mapView = new EXDF_View(index, "exd/map.exh", exhfFile);
//
//            HashDatabase.beginConnection();
//            try {
//                HashDatabase.setAutoCommit(false);
//            } catch (SQLException e1) {
//                Utils.getGlobalLogger().error(e1);
//            }
//            for (int i = 0; i < mapView.getTable().getRowCount(); i++) {
//                String map = ((String) mapView.getTable().getValueAt(i, 6));
//
//                if (map == null || map.isEmpty())
//                    continue;
//
//                String mapFolder = "ui/map/" + map;
//
//                if (!((String) mapView.getTable().getValueAt(i, 6)).contains("/"))
//                    continue;
//
//                String[] split = ((String) mapView.getTable().getValueAt(i, 6)).split("/");
//
//                HashDatabase.addPath(mapFolder + "/" + split[0] + split[1] + "_m.tex");
//                HashDatabase.addPath(mapFolder + "/" + split[0] + split[1] + "_s.tex");
//                HashDatabase.addPath(mapFolder + "/" + split[0] + split[1] + "m_m.tex");
//                HashDatabase.addPath(mapFolder + "/" + split[0] + split[1] + "m_s.tex");
//                HashDatabase.addPath(mapFolder + "/" + split[0] + split[1] + "d.tex");
//
//                Utils.getGlobalLogger().info("Added maps for {}/{}{}", mapFolder, split[0], split[1]);
//            }
//            try {
//                HashDatabase.commit();
//            } catch (SQLException e) {
//                Utils.getGlobalLogger().error("", e);
//            }
//            HashDatabase.closeConnection();
//
//        } catch (IOException e) {
//            Utils.getGlobalLogger().error("", e);
//        }
    }

    public static void getModelsFromModelChara(String path) {
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(path));
//
//            while (true) {
//                String in = reader.readLine();
//                if (in == null)
//                    break;
//                String[] split = in.split(",");
//
//                int id = Integer.parseInt(split[1]);
//                int type = Integer.parseInt(split[3]);
//                int model = Integer.parseInt(split[4]);
//                int variant = Integer.parseInt(split[5]);
//
//                String typePath = "";
//                String imcPath = null, modelPath = null, skelPath = null;
//
//                if (type != 3)
//                    type = 20;
//
//                switch (type) {
//                    case 3:
//                        typePath = "chara/monster/m";
//                        imcPath = String.format("%s%04d/obj/body/b%04d/b%04d.imc", typePath, id, model, model);
//                        modelPath = String.format("%s%04d/obj/body/b%04d/model/m%04db%04d.mdl", typePath, id, model, id, model);
//
//                        HashDatabase.addPath(imcPath);
//                        HashDatabase.addPath(modelPath);
//
//                        skelPath = String.format("%s%04d/skeleton/base/b%04d/eid_m%04db%04d.eid", typePath, id, model, id, model);
//                        HashDatabase.addPath(skelPath);
//                        skelPath = String.format("%s%04d/skeleton/base/b%04d/skl_m%04db%04d.sklp", typePath, id, model, id, model);
//                        HashDatabase.addPath(skelPath);
//                        skelPath = String.format("%s%04d/skeleton/base/b%04d/skl_m%04db%04d.sklb", typePath, id, model, id, model);
//                        skelPath = String.format("%s%04d/animation/a%04d/bt_common/resident/monster.pap", typePath, id, 0);
//                        HashDatabase.addPath(skelPath);
//                        skelPath = String.format("%s%04d/animation/a%04d/bt_common/event/event_wandering_action.pap", typePath, id, 0);
//                        HashDatabase.addPath(skelPath);
//                        skelPath = String.format("%s%04d/animation/a%04d/bt_common/mon_sp/m%04d/mon_sp001.pap", typePath, id, 0, id);
//                        HashDatabase.addPath(skelPath);
//                        break;
//                    case 4:
//                        typePath = "chara/demihuman/d";
//                        imcPath = String.format("%s%04d/obj/equipment/e%04d/e%04d.imc", typePath, id, model, model);
//                        HashDatabase.addPath(imcPath);
//
//                        modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_met.mdl", typePath, id, model, id, model);
//                        HashDatabase.addPath(modelPath);
//                        modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_top.mdl", typePath, id, model, id, model);
//                        HashDatabase.addPath(modelPath);
//                        modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_dwn.mdl", typePath, id, model, id, model);
//                        HashDatabase.addPath(modelPath);
//                        modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_sho.mdl", typePath, id, model, id, model);
//                        HashDatabase.addPath(modelPath);
//                        break;
//                }
//            }
//            reader.close();
//        } catch (FileNotFoundException e) {
//            Utils.getGlobalLogger().error("", e);
//        } catch (IOException e) {
//            Utils.getGlobalLogger().error("", e);
//        }
    }

    public static void getModels(String path) {
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(path));
//            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "out.txt"));
//
//            while (true) {
//                String in = reader.readLine();
//                if (in == null)
//                    break;
//                String[] split = in.split(":");
//
//                String model1 = split[0];
//                String model2 = split[1];
//
//                //Model1
//                if (!model1.equals("0, 0, 0, 0")) {
//                    String[] model1Split = model1.split(",");
//                    int section1 = Integer.parseInt(model1Split[3]);
//                    int modelNum1 = Integer.parseInt(model1Split[2]);
//                    int variant1 = Integer.parseInt(model1Split[1]);
//
//                    String type1 = null;
//
//                    if (section1 < 30)
//                        type1 = "chara/accessory/";
//                    else if (section1 < 3)
//                        type1 = "chara/equipment/";
//                    // TODO wat?
////                    else if (section1 < 3)
////                        type1 = "chara/weapon/";
//
//                    String imcPath1 = "";
//                    String modelPath1 = "";
//                    String materialPath1 = "";
//                    String texturePath1 = "";
//                }
//                //Model2
//                if (!model2.equals("0, 0, 0, 0")) {
//                    String[] model2Split = model2.split(",");
//                    int section2 = Integer.parseInt(model2Split[3]);
//                    int modelNum2 = Integer.parseInt(model2Split[2]);
//                    int variant2 = Integer.parseInt(model2Split[1]);
//
//                    String type2 = null;
//
//                    if (section2 < 30)
//                        type2 = "chara/accessory/";
//                    else if (section2 < 3)
//                        type2 = "chara/equipment/";
//                    // TODO wat?
////                    else if (section2 < 3)
////                        type2 = "chara/weapon/";
//
//                    String imcPath2 = "%s%04d.imc";
//                    String modelPath2 = "%s%04d.mdl";
//                    String materialPath2 = "texture/a";
//                    String texturePath2 = "texture/a";
//                }
//            }
//            reader.close();
//            writer.close();
//        } catch (FileNotFoundException e) {
//            Utils.getGlobalLogger().error("", e);
//        } catch (IOException e) {
//            Utils.getGlobalLogger().error("", e);
//        }
    }

    public static void openEveryModel() {
//        try {
//            SqPack_IndexFile currentIndex = new SqPack_IndexFile("c:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\040000.win32.index", true);
//
//            for (int i = 0; i < currentIndex.getPackFolders().length; i++) {
//                SqPack_Folder folder = currentIndex.getPackFolders()[i];
//                for (int j = 0; j < folder.getFiles().length; j++) {
//                    if (folder.getFiles()[j].getName().contains(".mdl")) {
//                        Utils.getGlobalLogger().info("=> Getting model {}", folder.getFiles()[j].getName());
//                        Model m = new Model(folder.getName() + "/" + folder.getFiles()[j].getName(), currentIndex, currentIndex.extractFile(folder.getFiles()[j].dataoffset, null), currentIndex.getEndian());
//                        for (int x = 0; x < m.getNumVariants(); x++)
//                            m.loadVariant(x);
//                    }
//                }
//            }
//
//        } catch (IOException e2) {
//            Utils.getGlobalLogger().error(e2);
//        }
    }

    public static void findStains() {
//        try {
//            SqPack_IndexFile currentIndex = new SqPack_IndexFile("e:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\040000.win32.index", true);
//
//            for (int i = 0; i < currentIndex.getPackFolders().length; i++) {
//                SqPack_Folder folder = currentIndex.getPackFolders()[i];
//                String folderName = folder.getName();
//                if (folderName.contains("equipment") && folderName.contains("material/v")) {
//                    String newfolder = folderName + "/staining";
//                    //Check if exists
//                    int folderHash = HashDatabase.computeCRC(newfolder.getBytes(), 0, newfolder.getBytes().length);
//                    for (int j = 0; j < currentIndex.getPackFolders().length; j++) {
//                        SqPack_Folder folder2 = currentIndex.getPackFolders()[j];
//                        if (folder2.getId() == folderHash) {
//
//                            for (int y = 0; y < folder2.getFiles().length; y++) {
//                                SqPack_File file = folder2.getFiles()[y];
//
//                                if (!file.getName().endsWith(".tex") && !file.getName().endsWith(".mtrl"))
//                                    continue;
//
//                                if (file.getName().contains(".tex")) {
//                                    for (int x = 1; x <= 85; x++)
//                                        HashDatabase.addPath(newfolder + "/" + file.getName().replace(".tex", String.format("_s%04d.tex", x)));
//
//                                    for (int x = 101; x <= 120; x++)
//                                        HashDatabase.addPath(newfolder + "/" + file.getName().replace(".tex", String.format("_s%04d.tex", x)));
//                                } else if (file.getName().contains(".mtrl")) {
//                                    for (int x = 1; x <= 85; x++)
//                                        HashDatabase.addPath(newfolder + "/" + file.getName().replace(".mtrl", String.format("_s%04d.mtrl", x)));
//
//                                    for (int x = 101; x <= 120; x++)
//                                        HashDatabase.addPath(newfolder + "/" + file.getName().replace(".mtrl", String.format("_s%04d.mtrl", x)));
//                                }
//                            }
//
//                        }
//                    }
//                }
//            }
//
//        } catch (IOException e2) {
//            Utils.getGlobalLogger().error(e2);
//        }
    }
}
