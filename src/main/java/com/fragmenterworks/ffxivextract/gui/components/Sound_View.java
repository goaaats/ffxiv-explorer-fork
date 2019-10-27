package com.fragmenterworks.ffxivextract.gui.components;

import com.fragmenterworks.ffxivextract.helpers.JOrbisPlayer;
import com.fragmenterworks.ffxivextract.helpers.MSADPCM_Decode;
import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.SCD_File;
import com.fragmenterworks.ffxivextract.models.SCD_File.SCD_Sound_Info;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class Sound_View extends JPanel {
    private final JTable tblSoundEntyList;
    private final SCD_File file;

    private final JOrbisPlayer oggPlayer = new JOrbisPlayer();

    public Sound_View(SCD_File scdFile) {

        setLayout(new BorderLayout(0, 0));

        file = scdFile;

        JPanel pnlFileList = new JPanel();
        pnlFileList.setBorder(new TitledBorder(null, "SCD Contents",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(pnlFileList, BorderLayout.CENTER);
        pnlFileList.setLayout(new BoxLayout(pnlFileList, BoxLayout.X_AXIS));

        JScrollPane scrollPane = new JScrollPane();
        pnlFileList.add(scrollPane);

        tblSoundEntyList = new JTable();
        tblSoundEntyList.setShowVerticalLines(false);
        scrollPane.setViewportView(tblSoundEntyList);
        tblSoundEntyList.setModel(new SCDTableModel(scdFile));
        tblSoundEntyList.getColumnModel().getColumn(4).setPreferredWidth(79);
        tblSoundEntyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSoundEntyList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {


                if (!e.getValueIsAdjusting()) {

                    SCD_Sound_Info info = file.getSoundInfo(tblSoundEntyList.getSelectedRow());
                    if (info != null) {
                        oggPlayer.stop();

                        if (info.dataType == 0x0C) {
                            final byte[] header = file.getADPCMHeader(tblSoundEntyList.getSelectedRow());
                            final byte[] body = file.getADPCMData(tblSoundEntyList.getSelectedRow());
                            new Thread() {

                                @Override
                                public void run() {
                                    playMsAdpcm(header, body);
                                }
                            }.start();
                        } else if (info.dataType == 0x06) {
                            final byte[] body = file.getConverted(tblSoundEntyList.getSelectedRow());
                            new Thread() {

                                @Override
                                public void run() {
                                    playOgg(body);
                                }
                            }.start();
                        } else {

                        }
                    }
                }
            }
        });

        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentShown(ComponentEvent arg0) {
            }

            @Override
            public void componentResized(ComponentEvent arg0) {
            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
            }

            @Override
            public void componentHidden(ComponentEvent arg0) {
                oggPlayer.stop();
            }
        });
    }

    class SCDTableModel extends AbstractTableModel {

        final SCD_File file;
        final String[] columns = {"Index", "File Size", "Data Type", "Frequency",
                "Num Channels", "Loop Start", "Loop End"};

        SCDTableModel(SCD_File file) {
            this.file = file;
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public int getRowCount() {
            return file.getNumEntries();
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            SCD_Sound_Info info = file.getSoundInfo(rowIndex);

            if (info == null) {
                if (columnIndex == 0)
                    return rowIndex;
                else if (columnIndex == 1)
                    return "Empty entry";
                else
                    return "N/A";
            }

            if (columnIndex == 0)
                return rowIndex;
            else if (columnIndex == 1)
                return info.fileSize;
            else if (columnIndex == 2) {
                switch (info.dataType) {
                    case 0x06:
                        return "OGG";
                    case 0x0C:
                        return "MS-ADPCM";
                }
            } else if (columnIndex == 3)
                return info.frequency;
            else if (columnIndex == 4)
                return info.numChannels;
            else if (columnIndex == 5)
                return info.loopStart;
            else if (columnIndex == 6)
                return info.loopEnd;
            else
                return "";
            return "";
        }

    }

    public void stopPlayback() {
        oggPlayer.stop();
    }

    private void playOgg(byte[] body) {
        Utils.getGlobalLogger().info("Trying to play {} bytes...", body.length);
        oggPlayer.play(new ByteArrayInputStream(body));
    }

    private void playMsAdpcm(byte[] header, byte[] body) {
        if (header == null || body == null)
            return;

        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(file.getEndian());
        bb.getShort();
        int channels = bb.getShort();
        int rate = bb.getInt();
        bb.getInt();
        int blockAlign = bb.getShort();
        int bitsPerSample = bb.getShort();

        // Creates an AudioFormat object and a DataLine.Info object.
        AudioFormat audioFormat = new AudioFormat((float) rate, 16, channels,
                true, false);
        DataLine.Info datalineInfo = new DataLine.Info(SourceDataLine.class,
                audioFormat, AudioSystem.NOT_SPECIFIED);

        SourceDataLine outputLine;

        // Check if the line is supported.
        if (!AudioSystem.isLineSupported(datalineInfo)) {
            Utils.getGlobalLogger().error("Audio output line is not supported.");
            return;
        }

        /*
         * Everything seems to be alright. Let's try to open a line with the
         * specified format and start the source data line.
         */
        try {
            outputLine = (SourceDataLine) AudioSystem.getLine(datalineInfo);
            outputLine.open(audioFormat);
        } catch (LineUnavailableException exception) {
            Utils.getGlobalLogger().error("The audio output line could not be opened due to resource restrictions.", exception);
            return;
        } catch (IllegalStateException exception) {
            Utils.getGlobalLogger().error("The audio output line is already open.", exception);
            return;
        } catch (SecurityException exception) {
            Utils.getGlobalLogger().error("The audio output line could not be opened due to security restrictions.", exception);
            return;
        }

        // Start it.
        outputLine.start();

        int bufferSize = MSADPCM_Decode.getBufferSize(body.length, channels, blockAlign, bitsPerSample);

        if (bufferSize % 4 != 0)
            bufferSize += bufferSize % 4;

        byte[] outputBuffer = new byte[bufferSize];

        MSADPCM_Decode.decode(body, outputBuffer, body.length, channels, blockAlign);

        outputLine.write(outputBuffer, 0, outputBuffer.length);

        outputLine.close();
    }

}
