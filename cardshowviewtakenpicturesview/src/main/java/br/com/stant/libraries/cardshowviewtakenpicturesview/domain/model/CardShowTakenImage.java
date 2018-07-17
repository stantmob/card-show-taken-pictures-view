package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by stant on 10/07/17.
 */

public class CardShowTakenImage {

    private String identifier;
    private String remoteImageUrl;
    private String localImageFilename;
    private String tempImagePathToShow;
    private Bitmap bitmapImageFromIntentPath;
    private Date createdAt;
    private Date updatedAt;

    public CardShowTakenImage(Bitmap bitmapImageFromIntentPath, String localImageFilename,
                              String tempImagePathToShow, Date createdAt, Date updatedAt) {
        this.bitmapImageFromIntentPath = bitmapImageFromIntentPath;
        this.localImageFilename = localImageFilename;
        this.tempImagePathToShow = tempImagePathToShow;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl, String localImageFilename,
                              Date createdAt, Date updatedAt) {
        this.identifier = identifier;
        this.remoteImageUrl = remoteImageUrl;
        this.localImageFilename = localImageFilename;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl, Date createdAt, Date updatedAt) {
        this.identifier = identifier;
        this.remoteImageUrl = remoteImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setLocalImageFilename(String localImageFilename) {
        this.localImageFilename = localImageFilename;
    }

    public void setTempImagePathToShow(String tempImagePathToShow) {
        this.tempImagePathToShow = tempImagePathToShow;
    }
}
