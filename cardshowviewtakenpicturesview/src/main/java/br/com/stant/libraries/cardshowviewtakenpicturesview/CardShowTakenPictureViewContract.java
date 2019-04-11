package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

/**
 * Created by denisvieira on 08/06/17.
 */

public interface CardShowTakenPictureViewContract {
    void pickPictureToFinishAction(View view);

    void dispatchTakePictureOrPickGalleryIntent();

    void addImageOnActivityResult(int requestCode, int resultCode, Intent data);

    void checkIfHasImages();

    void showPreviewPicDialog(CardShowTakenImage cardShowTakenImage);

    void closePreviewPicDialog(View View);

    void showEditStateViewConfiguration(View view);

    void showNormalStateViewConfiguration();

    void saveImageStateViewConfiguration(View view);

    void cancelEditImagesStateViewConfiguration(View view);

    void blockEditStateViewConfiguration();

    void unblockEditStateViewConfiguration();

    void setCardStateEnum(CardShowTakenPictureStateEnum cardStateEnum);

    void ifNoImagesShowEditStateViewConfigurationOnInit();

    void setImagesQuantityLimit(Integer limitQuantity, OnReachedOnTheImageCountLimit onReachedOnTheImageCountLimit);

    boolean hasImages();

    boolean hasImageByIdentifier(String identifier);

    void setCardImages(List<CardShowTakenImage> cardShowTakenImages);

    void addCardImages(List<CardShowTakenImage> cardShowTakenImages);

    List<CardShowTakenImage> getCardImages();

    List<CardShowTakenImage> getCardImagesAsAdded();

    List<CardShowTakenImage> getCardImagesAsRemoved();

    interface OnSavedCardListener {
        void onSaved(List<CardShowTakenImage> currentImages, List<CardShowTakenImage> imagesAsAdded, List<CardShowTakenImage> imagesAsRemoved);

        void onCancel();
    }

    interface OnReachedOnTheImageCountLimit {
        void onReached();
    }

    void setOnSavedCardListener(OnSavedCardListener mOnSavedCardListener);

    interface CardShowTakenCompressedCallback {
        void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath);

        void onError(String message);
    }


}
