package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class COHDEntryType_Frame extends COHDEntryType {

    public final int refTitleTextBox;
    public final int refSubtitleTextBox;
    public final int refCloseButton;
    public final int refSettingButton;
    public final int refHelpButton;
    public final int refMagnifyButton;
    public final int refTitleBar;

    public COHDEntryType_Frame(final ByteBuffer data) {
        super(data);
        this.refTitleTextBox = data.getInt();
        this.refSubtitleTextBox = data.getInt();
        this.refCloseButton = data.getInt();
        this.refSettingButton = data.getInt();
        this.refHelpButton = data.getInt();
        this.refMagnifyButton = data.getInt();
        this.refTitleBar = data.getInt();
    }

    @Override
    public String toString() {
        return "COHDEntryType_Frame{" +
                "refTitleTextBox=" + refTitleTextBox +
                ", refSubtitleTextBox=" + refSubtitleTextBox +
                ", refCloseButton=" + refCloseButton +
                ", refSettingButton=" + refSettingButton +
                ", refHelpButton=" + refHelpButton +
                ", refMagnifyButton=" + refMagnifyButton +
                ", getRefTitleBar=" + refTitleBar +
                "} " + super.toString();
    }
}
