package br.com.stant.libraries.card_show_taken_pictures_view.models;

import java.io.Serializable;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

/**
 * Created by stant on 04/07/17.
 */

public class Gallery implements Serializable {

    private final String mId;
    private final List<CardShowTakenImage> mImages;

    public Gallery(String mId, List<CardShowTakenImage> mImages) {
        this.mId = mId;
        this.mImages = mImages;
    }

    public String getId() {
        return mId;
    }

    public List<CardShowTakenImage> getImages() {
        return mImages;
    }
}
