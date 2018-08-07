package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import android.graphics.Bitmap;

import java.util.Date;

public class CameraPhoto {

    private String identifier;
    private String remoteImageUrl;
    private String localImageFilename;
    private String tempImagePathToShow;
    private Bitmap bitmapImageFromIntentPath;

    public CameraPhoto(String identifier, String remoteImageUrl, String localImageFilename, String tempImagePathToShow, Bitmap bitmapImageFromIntentPath) {
        this.identifier                = identifier;
        this.remoteImageUrl            = remoteImageUrl;
        this.localImageFilename        = localImageFilename;
        this.tempImagePathToShow       = tempImagePathToShow;
        this.bitmapImageFromIntentPath = bitmapImageFromIntentPath;
    }

    public CameraPhoto(String identifier, String remoteImageUrl) {
        this.identifier = identifier;
        this.remoteImageUrl = remoteImageUrl;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getRemoteImageUrl() {
        return remoteImageUrl;
    }

    public String getLocalImageFilename() {
        return localImageFilename;
    }

    public String getTempImagePathToShow() {
        return tempImagePathToShow;
    }

    public Bitmap getBitmapImageFromIntentPath() {
        return bitmapImageFromIntentPath;
    }

    public void setTempImagePathToShow(String tempImagePathToShow) {
        this.tempImagePathToShow = tempImagePathToShow;
    }

}
