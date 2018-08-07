package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import java.util.ArrayList;

import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;

public interface CameraContract {
    void closeCamera();
    void setPhotos(ArrayList<CameraPhoto> photos);
}
