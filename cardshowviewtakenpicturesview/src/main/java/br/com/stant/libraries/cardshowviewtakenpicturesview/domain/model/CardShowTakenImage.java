package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import android.graphics.Bitmap;

/**
 * Created by stant on 10/07/17.
 */

public class CardShowTakenImage {

    private String identifier;
    private String remoteImageUrl;
    private String localImageFilename;
    private String tempImagePathToShow;
    private Bitmap bitmapImageFromIntentPath;

    public CardShowTakenImage(Bitmap bitmapImageFromIntentPath, String localImageFilename, String tempImagePathToShow) {
        this.bitmapImageFromIntentPath = bitmapImageFromIntentPath;
        this.localImageFilename = localImageFilename;
        this.tempImagePathToShow = tempImagePathToShow;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl, String localImageFilename) {
        this.identifier = identifier;
        this.remoteImageUrl = remoteImageUrl;
        this.localImageFilename = localImageFilename;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl) {
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

    public void setLocalImageFilename(String localImageFilename) {
        this.localImageFilename = localImageFilename;
    }

    public void setTempImagePathToShow(String tempImagePathToShow) {
        this.tempImagePathToShow = tempImagePathToShow;
    }
}
