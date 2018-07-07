package com.fragmenterworks.ffxivextract.gui.components;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.helpers.JOrbisPlayer;
import com.fragmenterworks.ffxivextract.helpers.MSADPCM_Decode;
import com.fragmenterworks.ffxivextract.models.IGraphicsElement;
import com.fragmenterworks.ffxivextract.models.SCD_File;
import com.fragmenterworks.ffxivextract.models.SCD_File.SCD_Sound_Info;
import com.fragmenterworks.ffxivextract.models.TextureSet;
import com.fragmenterworks.ffxivextract.models.ULD_File;
import com.fragmenterworks.ffxivextract.models.uldStuff.renderer.ULD_File_Renderer;

public class ULD_View extends JPanel {
    ULD_File file;


    private final static Map<Integer, Class<? extends ULD_File_Renderer.GraphicsElement>> graphicsTypes    = new HashMap<>();
    private final static Map<Integer, Class<? extends ULD_File_Renderer.UIComponent>>     uiComponentTypes = new HashMap<>();

    static {
        graphicsTypes.put(1, ULD_File_Renderer.GraphicsContainer.class);
        graphicsTypes.put(2, ULD_File_Renderer.GraphicsImage.class);
        graphicsTypes.put(3, ULD_File_Renderer.GraphicsTextBox.class);
        graphicsTypes.put(4, ULD_File_Renderer.GraphicsMultiImage.class);

        uiComponentTypes.put(1, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(2, ULD_File_Renderer.CoFrame.class );
        uiComponentTypes.put(3, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(4, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(5, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(6, ULD_File_Renderer.CoSlider.class );
        uiComponentTypes.put(7, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(8, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(9, ULD_File_Renderer.CoList.class );
        uiComponentTypes.put(10, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(11, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(12, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(13, ULD_File_Renderer.CoScrollbar.class );
        uiComponentTypes.put(14, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(15, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(16, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(17, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(18, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(19, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(20, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(21, ULD_File_Renderer.NullUIComponent.class );
        uiComponentTypes.put(22, ULD_File_Renderer.NullUIComponent.class );
    }

    final private Map<Integer, BufferedImage>   images      = new HashMap<>();
    final private Map<Integer, TextureSet>      textureSets = new HashMap<>();
    final private Map<Integer, ULD_File_Renderer.GraphicsElement> graphics    = new HashMap<>();
    final private Map<Integer, IGraphicsElement> nodesByAccessor = new HashMap<>();

    private int width;
    private int height;
    public static boolean PAINT_DEBUG = false;

    public ULD_View(ULD_File uldFile) {

        setLayout(new BorderLayout(0, 0));

        file = uldFile;

        JPanel pnlFileList = new JPanel();
        pnlFileList.setBorder(new TitledBorder(null, "Rendered Contents",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(pnlFileList, BorderLayout.CENTER);
        pnlFileList.setLayout(new BoxLayout(pnlFileList, BoxLayout.X_AXIS));

        //JScrollPane scrollPane = new JScrollPane();
        //pnlFileList.add(scrollPane);

        try {

            System.out.println(uldFile);
            ULD_File_Renderer renderer = new ULD_File_Renderer(Constants.datPath + "\\game\\sqpack\\ffxiv", uldFile);

            JLabel lblPic = new JLabel();
            lblPic.setIcon(new ImageIcon(renderer.getImage(0, 0)));
            lblPic.addMouseListener(renderer);
            lblPic.addMouseMotionListener(renderer);
            lblPic.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    renderer.getImage(0, 0);
                    lblPic.repaint();
                }
            });

            pnlFileList.add(lblPic);
            pnlFileList.setVisible(true);
            lblPic.setVisible(true);

        } catch ( Exception e ) {
            e.printStackTrace();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String formattedText = "Failed to render ULD:\n" + sw.toString();
            JLabel lbl = new JLabel("<html>" + formattedText.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");

            pnlFileList.add(lbl);
            pnlFileList.setVisible(true);
            lbl.setVisible(true);
        }


        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentShown(ComponentEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void componentResized(ComponentEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void componentHidden(ComponentEvent arg0) {

            }
        });
    }
}
