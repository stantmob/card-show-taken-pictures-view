package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class SaveOnlyMode implements Parcelable {

    private Bitmap enabledIcon;
    private String enabledWarning;
    private Bitmap disabledIcon;
    private String disabledWarning;

    public SaveOnlyMode(Bitmap enabledIcon, String enabledWarning, Bitmap disabledIcon, String disabledWarning) {
        this.enabledIcon     = enabledIcon;
        this.enabledWarning  = enabledWarning;
        this.disabledIcon    = disabledIcon;
        this.disabledWarning = disabledWarning;
    }

    public Bitmap getEnabledIcon() {
        return enabledIcon;
    }

    public String getEnabledWarning() {
        return enabledWarning;
    }

    public Bitmap getDisabledIcon() {
        return disabledIcon;
    }

    public String getDisabledWarning() {
        return disabledWarning;
    }

    protected SaveOnlyMode(Parcel in) {
        enabledIcon     = in.readParcelable(Bitmap.class.getClassLoader());
        enabledWarning  = in.readString();
        disabledIcon    = in.readParcelable(Bitmap.class.getClassLoader());
        disabledWarning = in.readString();
    }

    public static final Creator<SaveOnlyMode> CREATOR = new Creator<SaveOnlyMode>() {
        @Override
        public SaveOnlyMode createFromParcel(Parcel in) {
            return new SaveOnlyMode(in);
        }

        @Override
        public SaveOnlyMode[] newArray(int size) {
            return new SaveOnlyMode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(enabledIcon, i);
        parcel.writeString(enabledWarning);
        parcel.writeParcelable(disabledIcon, i);
        parcel.writeString(disabledWarning);
    }


}
