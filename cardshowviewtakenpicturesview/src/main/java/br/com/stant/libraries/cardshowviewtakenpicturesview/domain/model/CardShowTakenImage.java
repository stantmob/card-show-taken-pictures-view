package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import android.graphics.Bitmap;
import java.util.Date;
import java.util.Objects;

public class CardShowTakenImage {

    private String identifier;
    private String remoteImageUrl;
    private String localImageFilename;
    private String tempImagePathToShow;
    private Bitmap bitmapImageFromIntentPath;
    private Date createdAt;
    private Date updatedAt;
    private String caption;
    private int order;

    public CardShowTakenImage(Bitmap bitmapImageFromIntentPath, String localImageFilename,
                              String tempImagePathToShow, Date createdAt, Date updatedAt, String caption) {
        this.bitmapImageFromIntentPath = bitmapImageFromIntentPath;
        this.localImageFilename        = localImageFilename;
        this.tempImagePathToShow       = tempImagePathToShow;
        this.createdAt                 = createdAt;
        this.updatedAt                 = updatedAt;
        this.caption                   = caption;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl, String localImageFilename,
                              int order, Date createdAt, Date updatedAt, String caption) {
        this.identifier         = identifier;
        this.remoteImageUrl     = remoteImageUrl;
        this.localImageFilename = localImageFilename;
        this.order              = order;
        this.createdAt          = createdAt;
        this.updatedAt          = updatedAt;
        this.caption            = caption;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl, Date createdAt, Date updatedAt) {
        this.identifier     = identifier;
        this.remoteImageUrl = remoteImageUrl;
        this.createdAt      = createdAt;
        this.updatedAt      = updatedAt;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTempImagePathToShow() {
        return tempImagePathToShow;
    }

    public String getLocalImageFilename() {
        return localImageFilename;
    }

    public String getRemoteImageUrl() {
        return remoteImageUrl;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public int getOrder() { return order; }

    public boolean hasOnlyRemoteUrl() {
        return !hasLocalImage() && !hasTempPathToShow() && remoteImageUrl != null && !remoteImageUrl.isEmpty();
    }

    private boolean hasTempPathToShow() {
        return tempImagePathToShow != null && !tempImagePathToShow.isEmpty();
    }

    public boolean hasLocalImage() {
        return localImageFilename != null && !localImageFilename.isEmpty();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardShowTakenImage)) return false;
        CardShowTakenImage that = (CardShowTakenImage) o;
        if (identifier != null) {
            return Objects.equals(identifier, that.identifier);
        }
        if (localImageFilename != null) {
            return Objects.equals(localImageFilename, that.localImageFilename);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, localImageFilename);
    }

}
