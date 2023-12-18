package br.com.stant.libraries.cameraimagegalleryview.components;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;

import java.io.File;
import java.util.ArrayList;

import br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cameraimagegalleryview.model.Theme;
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.SaveOnlyMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.AppPermissions;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;

public class Camera {
    public static final String KEY_LIMIT_IMAGES = "limit_images";
    public static final String KEY_IMAGE_LIST_SIZE = "image_list_size";
    public static final String KEY_IMAGE_CAMERA_LIST = "image_camera_list";
    public static final String KEY_IS_MULTIPLE_GALLERY_SELECTION = "is_multiple_gallery_selection";
    public static final String KEY_SAVE_ONLY_MODE = "save_only_mode";
    public static final String KEY_IS_CAPTION_ENABLED = "is_caption_enabled";

    private static SaveOnlyMode mSaveOnlyMode;
    private static CardImageGalleryViewContract.OnReachedOnTheImageCountLimit mOnReachedOnTheImageCountLimit;
    private static Integer mImagesQuantityLimit;

    private Intent cameraIntent;
    private ActivityResultLauncher<Intent> openCamera;
    private ImageGenerator imageGenerator;
    private File mSdCardTempImagesDirectory;
    private Context mContext;
    private Activity mActivity;
    private CardShowTakenImageInjection mCardShowTakenImages;

    public Camera(Context mContext, Activity mActivity) {
        this.mSdCardTempImagesDirectory = getPrivateTempDirectory(mContext);
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mCardShowTakenImages = CardShowTakenImageInjection.getCardShowTakenPictureInjection();
    }

    public void pickPictureToFinishAction() {
        if (notAtTheImageCountLimit()) {
            openPickGalleryIntent();
        } else if (mOnReachedOnTheImageCountLimit != null) {
            mOnReachedOnTheImageCountLimit.onReached();
        }
    }

    private boolean notAtTheImageCountLimit() {
        if (mImagesQuantityLimit != null) {
            return mCardShowTakenImages.getAll().size() != mImagesQuantityLimit;
        }

        return true;
    }

    private void openPickGalleryIntent() {
        if (!AppPermissions.hasPermissionsOn((mActivity))) {
            AppPermissions.requestPermissionsFor(mActivity);
        } else {
            dispatchTakePictureOrPickGalleryIntent();
        }
    }

    public void dispatchTakePictureOrPickGalleryIntent() {

        cameraIntent.putExtra(KEY_LIMIT_IMAGES, mImagesQuantityLimit);
        cameraIntent.putExtra(KEY_IMAGE_LIST_SIZE, mCardShowTakenImages.getAll().size());
        cameraIntent.putExtra(KEY_IS_MULTIPLE_GALLERY_SELECTION, true);
        cameraIntent.putExtra(KEY_SAVE_ONLY_MODE, mSaveOnlyMode);
        cameraIntent.putExtra(KEY_IS_CAPTION_ENABLED, true);

        openCamera.launch(cameraIntent);
    }

    public void addImageOnActivityResult(ActivityResult result) {
        imageGenerator = new ImageGenerator(mContext);
        Intent data = result.getData();
        if (result.getResultCode() == Activity.RESULT_OK && data != null) {

            ArrayList<CameraPhoto> cameraImages = (ArrayList<CameraPhoto>) data.getSerializableExtra(KEY_IMAGE_CAMERA_LIST);

            for (CameraPhoto cameraImage : cameraImages) {
                String localImage = cameraImage.getLocalImageFilename();
                File mImageDirectory = new File(mSdCardTempImagesDirectory.toString() + "/" + localImage);

                imageGenerator.generateCardShowTakenImageFromCamera(mImageDirectory,
                        new CardShowTakenPictureViewContract.CardShowTakenCompressedCallback() {
                            @Override
                            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                CardShowTakenImage cardShowTakenImage = new CardShowTakenImage(
                                        imageFilename, tempImagePath, cameraImage.getCreatedAt(),
                                        cameraImage.getUpdatedAt(), cameraImage.getCaption());

                                mCardShowTakenImages.addImage(cardShowTakenImage);
                            }

                            @Override
                            public void onError(String message) {
                                //tratamento de erro para implementar
                            }
                        });
            }
        }
    }


    public static SaveOnlyMode getmSaveOnlyMode() {
        return mSaveOnlyMode;
    }

    public static void setmSaveOnlyMode(SaveOnlyMode mSaveOnlyMode) {
        Camera.mSaveOnlyMode = mSaveOnlyMode;
    }

    public static CardImageGalleryViewContract.OnReachedOnTheImageCountLimit getmOnReachedOnTheImageCountLimit() {
        return mOnReachedOnTheImageCountLimit;
    }

    public static void setmOnReachedOnTheImageCountLimit(CardImageGalleryViewContract.OnReachedOnTheImageCountLimit mOnReachedOnTheImageCountLimit) {
        Camera.mOnReachedOnTheImageCountLimit = mOnReachedOnTheImageCountLimit;
    }

    public static Integer getmImagesQuantityLimit() {
        return mImagesQuantityLimit;
    }

    public static void setmImagesQuantityLimit(Integer mImagesQuantityLimit) {
        Camera.mImagesQuantityLimit = mImagesQuantityLimit;
    }

    public void setCameraIntent(Intent cameraIntent) {
        this.cameraIntent = cameraIntent;
    }

    public void setOpenCamera(ActivityResultLauncher<Intent> openCamera) {
        this.openCamera = openCamera;
    }
}
