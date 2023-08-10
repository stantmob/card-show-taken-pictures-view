package br.com.stant.libraries.cameraimagegalleryview;

import android.content.Intent;
import android.view.View;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;

public interface CardImageGalleryViewContract {

    void pickPictureToFinishAction(View view);

    void dispatchTakePictureOrPickGalleryIntent();

    void addImageOnActivityResult(int requestCode, int resultCode, Intent data);

    void setImagesQuantityLimit(Integer limitQuantity, CardShowTakenPictureViewContract.OnReachedOnTheImageCountLimit onReachedOnTheImageCountLimit);
}
