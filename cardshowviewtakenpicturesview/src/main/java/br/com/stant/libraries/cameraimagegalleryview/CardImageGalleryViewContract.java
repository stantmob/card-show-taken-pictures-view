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

    void setOnSavedCardListener(OnSavedCardListener mOnSavedCardListener);

    void registerActivityForCamera();

    void pickPictureToFinishAction(View view);

    void dispatchTakePictureOrPickGalleryIntent();

    // Begin Component
    void setImagesQuantityLimit(Integer limitQuantity, OnReachedOnTheImageCountLimit onReachedOnTheImageCountLimit);

    void updateCurrentAndLimitImagesQuantityText(Integer currentQuantity);
    void addImageOnActivityResult(ActivityResult result);
    void checkIfHasImages();

    void showPreviewPicDialog(CardShowTakenImage cardShowTakenImage, Integer photoPosition, OnCaptionSavedCallback onCaptionSavedCallback);

    void closePreviewPicDialog(View View);

    void showEditStateViewConfiguration(View view);

    void showNormalStateViewConfiguration();

    void saveImageStateViewConfiguration(View view);

    void cancelEditImagesStateViewConfiguration(View view);

    void blockEditStateViewConfiguration();

    void unblockEditStateViewConfiguration();

    void setCardStateEnum(CardShowTakenPictureStateEnum cardStateEnum);

    void ifNoImagesShowEditStateViewConfigurationOnInit();

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

    interface CardShowTakenCompressedCallback {
        void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath);

        void onError(String message);
    }
}
