package com.fragmenterworks.ffxivextract.gui.components;

import com.fragmenterworks.ffxivextract.models.CMP_File;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CMP_View extends JPanel {

    private final CMP_File currentCMP;
    private final JLabel canvas = new JLabel();

    public CMP_View(CMP_File file) {
        setBorder(new TitledBorder(null, "CMP File", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        currentCMP = file;

        setLayout(new BorderLayout(0, 0));

        add(canvas, BorderLayout.CENTER);

        int w = 8;
        int h = 5580;

        int type = BufferedImage.TYPE_INT_ARGB;

        BufferedImage image = new BufferedImage(30, h, type);

        int drawY = 0;

        boolean pushX = false;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((y * w) + x) < file.getColors().size()) {
                    image.setRGB(x, y, file.getColors().get((y * w) + x));
                }
            }
        }

        Image scaled = image.getScaledInstance(100, 500, Image.SCALE_DEFAULT);

        canvas.setIcon(new ImageIcon(image));

    }

}
