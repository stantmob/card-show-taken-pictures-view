package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.view.View;

import java.util.ArrayList;

import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.CardContract;

public interface CameraContract extends CardContract{
    void closeCamera();
    void setPhotos(ArrayList<CameraPhoto> photos);
    void takePicture();
    void returnImagesToCardShowTakenPicturesView();
    void showPreviewPicDialog(CameraPhoto cameraPhoto);
    void closePreviewPicDialog(View View);
}
