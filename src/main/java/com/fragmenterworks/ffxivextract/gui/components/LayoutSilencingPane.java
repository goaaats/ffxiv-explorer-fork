package com.fragmenterworks.ffxivextract.gui.components;

import javax.swing.*;
import java.awt.*;

public class LayoutSilencingPane extends JComponent {
    private JLabel message = new JLabel();

    public LayoutSilencingPane() {
        //  Set glass pane properties

        setOpaque(false);
        Color base = UIManager.getColor("inactiveCaptionBorder");
        Color background = new Color(base.getRed(), base.getGreen(), base.getBlue(), 128);
        setBackground(background);
        setLayout(new BorderLayout());
    }

    public void setChild(JComponent child) {
        removeAll();
        add(child, BorderLayout.CENTER);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
    }
}