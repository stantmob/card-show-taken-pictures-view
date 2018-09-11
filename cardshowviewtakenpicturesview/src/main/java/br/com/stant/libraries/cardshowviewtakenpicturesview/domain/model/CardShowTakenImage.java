package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import android.graphics.Bitmap;
import android.provider.MediaStore;

import java.util.Date;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewImagesAdapter;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getFile;

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
        this.localImageFilename        = localImageFilename;
        this.tempImagePathToShow       = tempImagePathToShow;
        this.createdAt                 = createdAt;
        this.updatedAt                 = updatedAt;
    }

    public CardShowTakenImage(String identifier, String remoteImageUrl, String localImageFilename,
                              Date createdAt, Date updatedAt) {
        this.identifier         = identifier;
        this.remoteImageUrl     = remoteImageUrl;
        this.localImageFilename = localImageFilename;
        this.createdAt          = createdAt;
        this.updatedAt          = updatedAt;
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

    public boolean hasOnlyRemoteUrl() {
        return remoteImageUrl != null && !remoteImageUrl.isEmpty();
    }

    public boolean hasLocalImage() {
        return localImageFilename != null && !localImageFilename.isEmpty();
    }


}
