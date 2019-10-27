package com.fragmenterworks.ffxivextract.models.uldStuff;

import java.nio.ByteBuffer;

/**
 *
 */
public class COHDEntryType_Scrollbar extends COHDEntryType {

    public final int refTrack;
    public final int refUpButton;
    public final int refDownButton;
    public final int refValueTextBox;

    public COHDEntryType_Scrollbar(final ByteBuffer data) {
        super(data);
        this.refTrack = data.getInt();
        this.refUpButton = data.getInt();
        this.refDownButton = data.getInt();
        this.refValueTextBox = data.getInt();
    }

    @Override
    public String toString() {
        return "COHDType_Scrollbar{" +
                "refTrack=" + refTrack +
                ", refUpButton=" + refUpButton +
                ", refDownButton=" + refDownButton +
                ", refValueTextBox=" + refValueTextBox +
                "} " + super.toString();
    }
}
