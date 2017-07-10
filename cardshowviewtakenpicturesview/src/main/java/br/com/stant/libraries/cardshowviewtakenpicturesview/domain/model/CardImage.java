package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import android.graphics.Bitmap;

/**
 * Created by stant on 10/07/17.
 */

public class CardImage {

    private Bitmap imageBitmap;
    private String imageUrl;
    private String imageFilename;

    public CardImage(Bitmap imageBitmap, String imageUrl, String imageFilename) {
        this.imageBitmap = imageBitmap;
        this.imageUrl = imageUrl;
        this.imageFilename = imageFilename;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }
}
