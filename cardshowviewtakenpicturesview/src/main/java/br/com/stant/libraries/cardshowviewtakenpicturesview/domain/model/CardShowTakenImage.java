package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model;

import android.graphics.Bitmap;

/**
 * Created by stant on 10/07/17.
 */

public class CardShowTakenImage {

    private String identifier;
    private Bitmap imageBitmap;
    private String imagePath;

    public CardShowTakenImage(Bitmap imageBitmap) {

        this.imageBitmap = imageBitmap;
        this.identifier = null;
        this.imagePath = null;
    }

    public CardShowTakenImage(String imagePath) {
        this.imagePath = imagePath;
        this.identifier = null;
        this.imageBitmap = null;
    }

    public CardShowTakenImage(String identifier, Bitmap imageBitmap) {
        this.identifier = identifier;
        this.imageBitmap = imageBitmap;
        this.imagePath = null;
    }

    public CardShowTakenImage(Bitmap imageBitmap, String imagePath) {
        this.imagePath = imagePath;
        this.imageBitmap = imageBitmap;
        this.identifier = null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public String getImagePath() {
        return imagePath;
    }
}
