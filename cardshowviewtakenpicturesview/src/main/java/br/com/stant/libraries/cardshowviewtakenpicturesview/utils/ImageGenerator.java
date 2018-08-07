package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ImageGenerator {

    private static final String TEMP_IMAGE_BASE_NAME = "card_show_taken_picture_temp_image";
    private CardContract mCardContract;
    private Context mContext;
    private File compressedImage = null;
    private File mPhoto;

    public ImageGenerator(Context context, File photo, CardContract cardContract) {
        this.mPhoto        = photo;
        this.mContext      = context;
        this.mCardContract = cardContract;
    }

    @SuppressLint("CheckResult")
    public void generateCardShowTakenImageFromCamera(File photoTaken,
                                                      Activity activity,
                                                      CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        if (photoTaken == null) {
            return;
        }

        new Compressor(mContext)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .compressToFileAsFlowable(photoTaken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                    compressedImage = file;

                    Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                    String tempImagePathToShow = createTempImageFileToShow(bitmapImageFromIntentPath, activity);

                    cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, photoTaken.getName(), tempImagePathToShow);
                }, Throwable::printStackTrace);


    }

    @SuppressLint("CheckResult")
    public void generateCardShowTakenImageFromImageGallery(File photoTaken, Intent data, Activity activity, CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        try {
            mPhoto = PhotoViewFileUtil.from(mContext, data.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Compressor(mContext)
                .setDestinationDirectoryPath(PhotoViewFileUtil.getFile().getAbsolutePath())
                .compressToFileAsFlowable(mPhoto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                    compressedImage = file;

                    new Compressor(mContext)
                            .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES).getAbsolutePath())
                            .compressToFileAsFlowable(compressedImage)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(file1 -> {
                                compressedImage = file1;

                                Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                                String tempImagePathToShow = createTempImageFileToShow(bitmapImageFromIntentPath, activity);

                                cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, mPhoto.getName(), tempImagePathToShow);
                            }, Throwable::printStackTrace);

                }, Throwable::printStackTrace);

    }

    private String createTempImageFileToShow(Bitmap bitmap, Activity activity) {
        String indexTempImage = mCardContract.getItemCount() + 1 + "";

        return MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, TEMP_IMAGE_BASE_NAME + indexTempImage, null);
    }

}
