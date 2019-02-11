package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

class SaveOnlyMode(val enabledIcon: Bitmap?, val enabledWarning: String?, val disabledIcon: Bitmap?, val disabledWarning: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Bitmap::class.java.classLoader),
            parcel.readString(),
            parcel.readParcelable(Bitmap::class.java.classLoader),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(enabledIcon, flags)
        parcel.writeString(enabledWarning)
        parcel.writeParcelable(disabledIcon, flags)
        parcel.writeString(disabledWarning)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SaveOnlyMode> {
        override fun createFromParcel(parcel: Parcel): SaveOnlyMode {
            return SaveOnlyMode(parcel)
        }

        override fun newArray(size: Int): Array<SaveOnlyMode?> {
            return arrayOfNulls(size)
        }
    }


}