package com.fragmenterworks.ffxivextract.gui.components;

import com.fragmenterworks.ffxivextract.models.PAP_File;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class PAP_View extends JPanel {

    private final PAP_File currentPAP;
    private final JList lstAnimationNames;

    public PAP_View(PAP_File file) {
        setBorder(new TitledBorder(null, "Animations", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        currentPAP = file;

        setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);

        lstAnimationNames = new JList();
        scrollPane.setViewportView(lstAnimationNames);
        lstAnimationNames.setModel(new AbstractListModel() {

            public int getSize() {
                return currentPAP.getAnimationNames().length;
            }

            public Object getElementAt(int index) {
                return currentPAP.getAnimationNames()[index];
            }
        });


    }

}
