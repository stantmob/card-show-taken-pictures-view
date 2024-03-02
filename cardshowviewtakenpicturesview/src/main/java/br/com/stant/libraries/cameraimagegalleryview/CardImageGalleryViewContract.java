package br.com.stant.libraries.cameraimagegalleryview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import androidx.activity.result.ActivityResult;

import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.callbacks.OnCaptionSavedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public interface CardImageGalleryViewContract {
    String KEY_IMAGE_LIST_GALLERY = "image_list";
    String KEY_IMAGE_FULL_SCREEN = "image";

    void registerActivityForCamera();

    void setImagesQuantityLimit(Integer limitQuantity, OnReachedOnTheImageCountLimit onReachedOnTheImageCountLimit);

    void updateCurrentAndLimitImagesQuantityText(Integer currentQuantity);

    void setCardStateEnum(CardShowTakenPictureStateEnum cardStateEnum);

    void setCardImages(List<CardShowTakenImage> cardShowTakenImages);

    boolean hasImages();

    boolean hasImageByIdentifier(String identifier);

    List<CardShowTakenImage> getCardAllImages();

    List<CardShowTakenImage> getCardImagesAsAdded();

    List<CardShowTakenImage> getCardImagesAsUpdated();

    List<CardShowTakenImage> getCardImagesAsRemoved();

    interface OnSavedCardListener {
        void onSaved(List<CardShowTakenImage> currentImages, List<CardShowTakenImage> imagesAsAdded, List<CardShowTakenImage> imagesAsRemoved);

        void onCancel();
    }

    interface OnReachedOnTheImageCountLimit {
        void onReached();
    }
}
