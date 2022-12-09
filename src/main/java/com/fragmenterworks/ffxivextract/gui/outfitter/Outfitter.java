package com.fragmenterworks.ffxivextract.gui.outfitter;

import com.bric.swing.ColorPicker;
import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.gui.components.ModelCharacterRenderer;
import com.fragmenterworks.ffxivextract.gui.components.OpenGL_View;
import com.fragmenterworks.ffxivextract.gui.modelviewer.ItemChooserDialog;
import com.fragmenterworks.ffxivextract.gui.modelviewer.ModelItemEntry;
import com.fragmenterworks.ffxivextract.gui.modelviewer.ModelViewerWindow;
import com.fragmenterworks.ffxivextract.helpers.EXDDef;
import com.fragmenterworks.ffxivextract.helpers.SparseArray;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.Model;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;
import com.fragmenterworks.ffxivextract.paths.database.HashDatabase;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

class Outfitter extends JPanel {

    private ModelViewerWindow parent;

    private final ArrayList<ModelItemEntry>[] entries = new ArrayList[22];

    private final SparseArray<String> slots = new SparseArray<String>();

    private final SparseArray<String> charIds = new SparseArray<String>();

    //UI
    OpenGL_View view3D;
    private final FPSAnimator animator;

    //Builder
    private int currentBody = 0;
    private int currentFace = 1;
    private int currentFaceOptions = 0;
    private int currentHair = 1;
    private float[] currentHairColor = Constants.defaultHairColor;
    private float[] currentHairHighlightsColor = Constants.defaultHairColor;
    private float[] currentEyeColor = Constants.defaultEyeColor;

    private int currentWeap1Item = -1;
    private int currentWeap2Item = -1;
    private int currentHeadItem = -1;
    private int currentBodyItem = -1;
    private int currentHandsItem = -1;
    private int currentPantsItem = -1;
    private int currentFeetItem = -1;
    private int currentNeckItem = -1;
    private int currentEaringItem = -1;
    private int currentBracletItem = -1;
    private int currentRing1Item = -1;
    private int currentRing2Item = -1;

    private Color hairColor = new Color(Constants.defaultHairColor[0], Constants.defaultHairColor[1], Constants.defaultHairColor[2], Constants.defaultHairColor[3]);
    private Color highlightColor = new Color(Constants.defaultHighlightColor[0], Constants.defaultHighlightColor[1], Constants.defaultHighlightColor[2], Constants.defaultHighlightColor[3]);
    private Color eyeColor = new Color(Constants.defaultEyeColor[0], Constants.defaultEyeColor[1], Constants.defaultEyeColor[2], Constants.defaultEyeColor[3]);

    //Render Stuff
    private ModelCharacterRenderer renderer;

    private boolean leftMouseDown = false;
    private boolean rightMouseDown = false;

    private int lastOriginX, lastOriginY;
    private int lastX, lastY;

    private final SqPackIndexFile modelIndexFile;
    private final EXDF_View itemView;

    public Outfitter(SqPackIndexFile modelIndex, EXDF_View itemView) {
        this.modelIndexFile = modelIndex;
        this.itemView = itemView;

        //Fill the Equipment Slots
        slots.append(-1, "--Equipment Slot--");
        slots.append(1, "One-Handed Weapon");
        slots.append(13, "Two-Handed Weapon");
        slots.append(2, "Offhand");
        slots.append(3, "Head");
        slots.append(4, "Body");
        slots.append(5, "Hands");
        slots.append(7, "Legs");
        slots.append(8, "Feet");
        slots.append(9, "Earings");
        slots.append(10, "Necklace");
        slots.append(11, "Wrists");
        slots.append(12, "Rings");

        slots.append(15, "Body + Head");
        slots.append(16, "All - Head");
        //slots.append(17, "Soulstone");
        slots.append(18, "Legs + Feet");
        slots.append(19, "All");
        slots.append(20, "Body + Hands");
        slots.append(21, "Body + Legs + Feet");

        slots.append(0, "Non-Equipment");

        //Fill the char ids
        charIds.append(1, "Midlander Male");
        charIds.append(2, "Midlander  Female");
        charIds.append(3, "Highlander Male");
        charIds.append(4, "Highlander Female");
        charIds.append(5, "Elezen Male");
        charIds.append(6, "Elezen Female");
        charIds.append(7, "Miqo'te Male");
        charIds.append(8, "Miqo'te Female");
        charIds.append(9, "Roegadyn Male");
        charIds.append(10, "Roegadyn  Female");
        charIds.append(11, "Lalafell Male");
        charIds.append(12, "Lalafell Female");
        charIds.append(13, "Au Ra Male");
        charIds.append(14, "Au Ra Female");

        Arrays.fill(entries, new ArrayList<ModelItemEntry>());

        setLayout(new BorderLayout(0, 0));

        JSplitPane splitPane = new JSplitPane();

        JPanel panel_1 = new JPanel();
        add(splitPane, BorderLayout.CENTER);
        panel_1.setLayout(new BorderLayout(0, 0));

        JPanel panel_2 = new JPanel();
        //panel_1.add(panel_2, BorderLayout.NORTH);

        JLabel lblInfoAndControls = new JLabel("Info and controls go here");
        panel_2.add(lblInfoAndControls);

        JPanel panel_3 = new JPanel();
        panel_1.add(panel_3, BorderLayout.CENTER);
        panel_3.setLayout(new BorderLayout(0, 0));

        JPanel panel_9 = new JPanel();
        add(panel_9, BorderLayout.WEST);
        panel_9.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        panel_9.add(scrollPane);

        JPanel panel = new JPanel();
        //scrollPane.setViewportView(panel);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.setLayout(new BorderLayout(0, 0));

        JPanel panel_5 = new JPanel();
        panel.add(panel_5, BorderLayout.NORTH);
        panel_5.setBorder(new TitledBorder(null, "Appearance", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.Y_AXIS));

        final JComboBox cmbBodyStyle = new JComboBox();
        cmbBodyStyle.setAlignmentX(Component.LEFT_ALIGNMENT);

        cmbBodyStyle.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selected = cmbBodyStyle.getSelectedIndex();
                    currentBody = charIds.keyAt(selected);

                    loadBodyModel(-1);
                    loadHairModel(currentHair);
                    loadHeadModel(currentFace);

                    loadEquipModel(-1, 1, currentWeap1Item);
                    loadEquipModel(-1, 2, currentWeap2Item);
                    loadEquipModel(-1, 3, currentHeadItem);
                    loadEquipModel(-1, 4, currentBodyItem);
                    loadEquipModel(-1, 5, currentHandsItem);
                    loadEquipModel(-1, 7, currentPantsItem);
                    loadEquipModel(-1, 8, currentFeetItem);
                    loadEquipModel(-1, 10, currentNeckItem);
                    loadEquipModel(-1, 9, currentEaringItem);
                    loadEquipModel(-1, 11, currentBracletItem);
                    loadEquipModel(-1, 12, currentRing1Item);
                    loadEquipModel(-1, 50, currentRing2Item);

                }
            }
        });

        panel_5.add(cmbBodyStyle);

        JPanel panel_4 = new JPanel();
        panel_4.setAlignmentX(Component.LEFT_ALIGNMENT);
        FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
        flowLayout_1.setAlignment(FlowLayout.LEFT);
        panel_5.add(panel_4);

        JLabel lblFace = new JLabel("Face");
        panel_4.add(lblFace);

        SpinnerModel sm = new SpinnerNumberModel(1, 0, 9999, 1);
        SpinnerModel sm2 = new SpinnerNumberModel(1, 0, 9999, 1);

        final JSpinner spnFace = new JSpinner();
        spnFace.setModel(sm);
        spnFace.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                currentFace = (Integer) spnFace.getValue();
                loadHeadModel(currentFace);
            }
        });
        panel_4.add(spnFace);

        JButton btnFaceOptions = new JButton("Face Options");
        panel_4.add(btnFaceOptions);
        btnFaceOptions.setEnabled(false);
        btnFaceOptions.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panel_7 = new JPanel();
        panel_7.setAlignmentX(Component.LEFT_ALIGNMENT);
        FlowLayout flowLayout = (FlowLayout) panel_7.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        panel_5.add(panel_7);

        JLabel lblHair = new JLabel("Hair ");
        panel_7.add(lblHair);

        final JSpinner spnHair = new JSpinner();
        spnHair.setModel(sm2);
        spnHair.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                currentHair = (Integer) spnHair.getValue();
                loadHairModel(currentHair);
            }
        });
        panel_7.add(spnHair);

        JLabel lblColor = new JLabel("Color: ");
        panel_7.add(lblColor);

        final JPanel btnHairColor = new JPanel();
        btnHairColor.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        btnHairColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int count = mouseEvent.getClickCount();
                if (count == 1) {
                    Color newColor = ColorPicker.showDialog(
                            Outfitter.this.parent, hairColor);
                    hairColor = newColor;
                    btnHairColor.setBackground(hairColor);
                    renderer.setHairColor(hairColor.getColorComponents(null));
                }
            }
        });
        btnHairColor.setBackground(hairColor);
        panel_7.add(btnHairColor);

        JLabel lblHighlights = new JLabel("Highlights:");
        panel_7.add(lblHighlights);

        final JPanel btnHighlightColor = new JPanel();
        btnHighlightColor.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        btnHighlightColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int count = mouseEvent.getClickCount();
                if (count == 1) {
                    Color newColor = ColorPicker.showDialog(
                            Outfitter.this.parent, highlightColor);
                    highlightColor = newColor;
                    btnHighlightColor.setBackground(highlightColor);
                    renderer.setHighlightColor(highlightColor.getColorComponents(null));
                }
            }
        });
        btnHighlightColor.setBackground(highlightColor);
        panel_7.add(btnHighlightColor);

        JPanel panel_8 = new JPanel();
        panel_8.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel_5.add(panel_8);
        panel_8.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JLabel lblEye = new JLabel("Eye Color:");
        panel_8.add(lblEye);

        final JPanel btnEyeColor = new JPanel();
        btnEyeColor.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        btnEyeColor.setBackground(new Color(75, 54, 27));
        btnEyeColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int count = mouseEvent.getClickCount();
                if (count == 1) {
                    Color newColor = ColorPicker.showDialog(
                            Outfitter.this.parent, eyeColor);
                    eyeColor = newColor;
                    btnEyeColor.setBackground(eyeColor);
                    renderer.setEyeColor(eyeColor.getColorComponents(null));
                }
            }
        });
        btnEyeColor.setBackground(eyeColor);
        panel_8.add(btnEyeColor);

        JPanel panel_17 = new JPanel();
        panel.add(panel_17, BorderLayout.CENTER);
        panel_17.setLayout(new BorderLayout(0, 0));

        JPanel panel_6 = new JPanel();
        panel_17.add(panel_6, BorderLayout.NORTH);
        panel_6.setBorder(new TitledBorder(null, "Equipment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_6.setLayout(new GridLayout(6, 2, 0, 0));

        JButton btnMainhand = new JButton("Main Hand");
        panel_6.add(btnMainhand);
        btnMainhand.setActionCommand("main");
        btnMainhand.addActionListener(equipListener);

        JButton btnOffhand = new JButton("Off hand");
        panel_6.add(btnOffhand);
        btnOffhand.setActionCommand("off");
        btnOffhand.addActionListener(equipListener);

        JButton btnHead = new JButton("Head");
        panel_6.add(btnHead);
        btnHead.setActionCommand("head");
        btnHead.addActionListener(equipListener);

        JButton btnNeck = new JButton("Neck");
        panel_6.add(btnNeck);
        btnNeck.setActionCommand("neck");
        btnNeck.addActionListener(equipListener);

        JButton btnBody = new JButton("Body");
        panel_6.add(btnBody);
        btnBody.setActionCommand("body");
        btnBody.addActionListener(equipListener);

        JButton btnEarring = new JButton("Earring");
        panel_6.add(btnEarring);
        btnEarring.setActionCommand("ear");
        btnEarring.addActionListener(equipListener);

        JButton btnHands = new JButton("Hands");
        panel_6.add(btnHands);
        btnHands.setActionCommand("gloves");
        btnHands.addActionListener(equipListener);

        JButton btnWrist = new JButton("Wrist");
        panel_6.add(btnWrist);
        btnWrist.setActionCommand("wrist");
        btnWrist.addActionListener(equipListener);


        JButton btnLegs = new JButton("Legs");
        panel_6.add(btnLegs);
        btnLegs.setActionCommand("pants");
        btnLegs.addActionListener(equipListener);

        JButton btnLRing = new JButton("L. Ring");
        panel_6.add(btnLRing);
        btnLRing.setActionCommand("lring");
        btnLRing.addActionListener(equipListener);


        JButton btnFeet = new JButton("Feet");
        panel_6.add(btnFeet);
        btnFeet.setActionCommand("shoes");
        btnFeet.addActionListener(equipListener);
        JButton btnRRing = new JButton("R. Ring");
        panel_6.add(btnRRing);
        btnRRing.setActionCommand("rring");
        btnRRing.addActionListener(equipListener);

        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities(glProfile);
        final GLCanvas glcanvas = new GLCanvas(glcapabilities);
        renderer = new ModelCharacterRenderer();
        glcanvas.addGLEventListener(renderer);
        animator = new FPSAnimator(glcanvas, 30);
        animator.start();
        glcanvas.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (leftMouseDown) {
                    renderer.pan((e.getX() - lastX), (e.getY() - lastY));
                    lastX = e.getX();
                    lastY = e.getY();
                }
                if (rightMouseDown) {
                    renderer.rotate(e.getX() - lastX, e.getY() - lastY);
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }
        });
        glcanvas.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1)
                    leftMouseDown = false;
                if (e.getButton() == MouseEvent.BUTTON3)
                    rightMouseDown = false;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    leftMouseDown = true;
                    lastOriginX = e.getX();
                    lastOriginY = e.getY();
                    lastX = lastOriginX;
                    lastY = lastOriginY;
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    rightMouseDown = true;
                    lastOriginX = e.getX();
                    lastOriginY = e.getY();
                    lastX = lastOriginX;
                    lastY = lastOriginY;
                }
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
            }
        });
        addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                renderer.zoom(-notches);
            }
        });

        splitPane.setLeftComponent(panel);
        splitPane.setRightComponent(panel_1);
        splitPane.setDividerLocation(220);

        panel_3.add(glcanvas, BorderLayout.CENTER);

        for (int i = 0; i < charIds.size(); i++)
            cmbBodyStyle.addItem(charIds.valueAt(i));
    }

    private void loadBodyModel(int id) {

        if (id == -1) {
            renderer.setModel(0, null);
            return;
        }

        String modelPath = null;
        byte[] modelData = null;

        int characterNumber = currentBody * 100 + 1;

        try {
            modelPath = String.format("chara/human/c%04d/obj/body/b%04d/model/c%04db%04d_top.mdl", characterNumber, id, characterNumber, id);
            modelData = modelIndexFile.extractFile(modelPath);
        } catch (Exception e) {
            Utils.getGlobalLogger().error("", e);
        }

        if (modelData != null) {
            HashDatabase.addPath(modelPath);

            Model model = new Model(modelPath, modelIndexFile, modelData, modelIndexFile.getEndian());
            model.loadVariant(-1);
            renderer.setModel(0, model);
        } else
            renderer.setModel(0, null);

    }

    private void loadHairModel(int id) {

        if (id <= 0) {
            renderer.setModel(2, null);
            return;
        }

        String modelPath = null;
        byte[] modelData = null;

        int characterNumber = currentBody * 100 + 1;

        try {
            modelPath = String.format("chara/human/c%04d/obj/hair/h%04d/model/c%04dh%04d_hir.mdl", characterNumber, id, characterNumber, id);
            modelData = modelIndexFile.extractFile(modelPath);
        } catch (Exception e) {
            Utils.getGlobalLogger().error("", e);
        }

        if (modelData != null) {
            HashDatabase.addPath(modelPath);

            Model model = new Model(modelPath, modelIndexFile, modelData, modelIndexFile.getEndian());
            model.loadVariant(-1);
            renderer.setModel(2, model);
        } else
            renderer.setModel(2, null);

    }

    private void loadHeadModel(int id) {

        if (id <= 0) {
            renderer.setModel(1, null);
            return;
        }

        String modelPath = null;
        byte[] modelData = null;

        int characterNumber = currentBody * 100 + 1;

        try {
            modelPath = String.format("chara/human/c%04d/obj/face/f%04d/model/c%04df%04d_fac.mdl", characterNumber, id, characterNumber, id);
            modelData = modelIndexFile.extractFile(modelPath);
        } catch (Exception e) {
            Utils.getGlobalLogger().error("", e);
        }

        if (modelData != null) {
            HashDatabase.addPath(modelPath);

            Model model = new Model(modelPath, modelIndexFile, modelData, modelIndexFile.getEndian());
            model.loadVariant(-1);
            renderer.setModel(1, model);
        } else
            renderer.setModel(1, null);

    }

    private void loadTailModel(int id) {

        if (id == -1)
            return;

        String modelPath = null;
        byte[] modelData = null;

        int characterNumber = currentBody * 100 + 1;

        try {
            modelPath = String.format("chara/human/c%04d/obj/tail/t%04d/model/c%04dt%04d_til.mdl", characterNumber, id, characterNumber, id);
            modelData = modelIndexFile.extractFile(modelPath);
        } catch (Exception e) {
            Utils.getGlobalLogger().error("", e);
        }

        if (modelData != null) {
            HashDatabase.addPath(modelPath);

            Model model = new Model(modelPath, modelIndexFile, modelData, modelIndexFile.getEndian());
            model.loadVariant(-1);
            renderer.setModel(0, model);
        } else
            renderer.setModel(0, null);

    }

    private void loadEquipModel(int charNumberOverride, int modelSlot, int selected) {

        int slot;
        String modelPath = null;
        byte[] modelData = null;

        ModelItemEntry currentItem = null;
        if (selected != -1) {
            try {
                int i = selected;
                String[] model1Split = ((String) itemView.getTable().getValueAt(i, EXDDef.INDEX_ITEM_MODEL1)).split(",");
                String[] model2Split = ((String) itemView.getTable().getValueAt(i, EXDDef.INDEX_ITEM_MODEL1)).split(",");

                slot = (Integer) itemView.getTable().getValueAt(i, EXDDef.INDEX_ITEM_SLOT);

                String name = (String) itemView.getTable().getValueAt(i, EXDDef.INDEX_ITEM_NAME);
                int id = Integer.parseInt(model1Split[0].trim());

                boolean isWeap = false;
                if (slot == 0 || slot == 1 || slot == 2 || slot == 13)
                    isWeap = true;

                int model = !isWeap ? Integer.parseInt(model1Split[2].trim()) : Integer.parseInt(model1Split[1].trim());
                int varient = !isWeap ? Integer.parseInt(model1Split[1].trim()) : Integer.parseInt(model1Split[2].trim());

                int type = slot;

                currentItem = new ModelItemEntry(name, id, model, varient, type);
            } catch (Exception e) {
                Utils.getGlobalLogger().error("", e);
                return;
            }
        } else //Load small clothes
        {
            slot = modelSlot;
            currentItem = new ModelItemEntry("SmallClothes", 0, 0, 0, slot);
        }

        if (modelSlot == 50)
            slot = 50;

        if (currentItem == null)
            return;

        int characterNumber = ((charNumberOverride == -1 ? currentBody * 100 + 1 : charNumberOverride));

        try {

            switch (slot) {
                case 13:
                case 0:
                case 1:
                case 2:
                    modelPath = String.format("chara/weapon/w%04d/obj/body/b%04d/model/w%04db%04d.mdl", currentItem.id, currentItem.model, currentItem.id, currentItem.model);
                    break;
                case 3:
                    modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_met.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 4:
                case 21:
                case 20:
                case 19:
                case 16:
                case 15:
                    modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_top.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 5:
                    modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_glv.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 6:
                case 7:
                case 18:
                    modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_dwn.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 8:
                    modelPath = String.format("chara/equipment/e%04d/model/c%04de%04d_sho.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 9:
                    modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_ear.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 10:
                    modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_nek.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 11:
                    modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_wrs.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 12:
                    modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_ril.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
                case 50:
                    modelPath = String.format("chara/accessory/a%04d/model/c%04da%04d_rir.mdl", currentItem.id, characterNumber, currentItem.id);
                    break;
            }

            if (modelPath == null) {
                Utils.getGlobalLogger().error("Couldn't build model path.");
                return;
            }

            modelData = modelIndexFile.extractFile(modelPath);
        } catch (Exception e) {
            Utils.getGlobalLogger().error("", e);
        }

        if (modelData == null && (characterNumber != 101 && characterNumber != 201)) {
            Utils.getGlobalLogger().info("Model for charId {} not detected, falling back to {} Hyur model.", String.format("%04d", characterNumber), currentBody % 2 == 0 ? "female" : "male");
            loadEquipModel(fallback(characterNumber), modelSlot, selected);
            return;
        }

        if (modelSlot == 50)
            modelSlot = 13;

        if (modelData != null) {
            HashDatabase.addPath(modelPath);

            Model model = new Model(modelPath, modelIndexFile, modelData, modelIndexFile.getEndian());
            model.loadVariant(currentItem.varient == 0 ? 1 : currentItem.varient);
            renderer.setModel(2 + modelSlot, model);
        } else
            renderer.setModel(2 + modelSlot, null);

    }

    private int fallback(int characterCode) {
        if (characterCode == 1201) {
            return 1101;
        }
        if (currentBody % 2 == 0)
            return 201;
        else
            return 101;

    }

    private final ActionListener equipListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("main")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 1);
                if (chosen != currentWeap1Item && chosen != -2) {
                    currentWeap1Item = chosen;
                    loadEquipModel(-1, 1, currentWeap1Item);
                    if (currentWeap1Item == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentWeap1Item, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("offhand")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 2);
                if (chosen != currentWeap2Item && chosen != -2) {
                    currentWeap2Item = chosen;
                    loadEquipModel(-1, 2, currentWeap2Item);
                    if (currentWeap2Item == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentWeap2Item, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("head")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 3);
                if (chosen != currentHeadItem && chosen != -2) {
                    currentHeadItem = chosen;
                    loadEquipModel(-1, 3, currentHeadItem);
                    if (currentHeadItem == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentHeadItem, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("body")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 4);
                if (chosen != currentBodyItem && chosen != -2) {
                    currentBodyItem = chosen;
                    loadEquipModel(-1, 4, currentBodyItem);
                    if (currentBodyItem == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentBodyItem, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("gloves")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 5);
                if (chosen != currentHandsItem && chosen != -2) {
                    currentHandsItem = chosen;
                    loadEquipModel(-1, 5, currentHandsItem);
                    if (currentHandsItem == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentHandsItem, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("pants")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 7);
                if (chosen != currentPantsItem && chosen != -2) {
                    currentPantsItem = chosen;
                    loadEquipModel(-1, 7, currentPantsItem);
                    if (currentPantsItem == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentPantsItem, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("shoes")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 8);
                if (chosen != currentFeetItem && chosen != -2) {
                    currentFeetItem = chosen;
                    loadEquipModel(-1, 8, currentFeetItem);
                    if (currentFeetItem == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentFeetItem, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("neck")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 10);
                if (chosen != currentNeckItem && chosen != -2) {
                    currentNeckItem = chosen;
                    loadEquipModel(-1, 10, currentNeckItem);
                    if (currentNeckItem == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentNeckItem, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("ear")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 9);
                if (chosen != currentEaringItem && chosen != -2) {
                    currentEaringItem = chosen;
                    loadEquipModel(-1, 9, currentEaringItem);
                    if (currentEaringItem == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentEaringItem, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("wrist")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 11);
                if (chosen != currentBracletItem && chosen != -2) {
                    currentBracletItem = chosen;
                    loadEquipModel(-1, 11, currentBracletItem);
                    if (currentBracletItem == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentBracletItem, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("lring")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 12);
                if (chosen != currentRing1Item && chosen != -2) {
                    currentRing1Item = chosen;
                    loadEquipModel(-1, 12, currentRing1Item);
                    if (currentRing1Item == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentRing1Item, EXDDef.INDEX_ITEM_NAME));
                }
            } else if (e.getActionCommand().equals("rring")) {
                int chosen = ItemChooserDialog.showDialog(parent, itemView, 12);
                if (chosen != currentRing2Item && chosen != -2) {
                    currentRing2Item = chosen;
                    loadEquipModel(-1, 50, currentRing2Item);
                    if (currentRing2Item == -1)
                        ((JButton) e.getSource()).setToolTipText("NONE");
                    else
                        ((JButton) e.getSource()).setToolTipText((String) itemView.getTable().getValueAt(currentRing2Item, EXDDef.INDEX_ITEM_NAME));
                }
            }
        }
    };

}
