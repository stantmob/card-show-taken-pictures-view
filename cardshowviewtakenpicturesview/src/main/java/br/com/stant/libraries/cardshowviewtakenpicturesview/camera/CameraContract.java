package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.view.View;

import java.util.ArrayList;

import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.callbacks.OnCaptionSavedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;

public interface CameraContract {
    void closeCamera();

    void setPhotos(ArrayList<CameraPhoto> photos);

    void takePicture();

    void returnImagesToCardShowTakenPicturesView();

    void showPreviewPicDialog(CameraPhoto cameraPhoto, OnCaptionSavedCallback onCaptionSavedCallback);

    void closePreviewPicDialog(View View);

    void saveCaption(View view);


}
