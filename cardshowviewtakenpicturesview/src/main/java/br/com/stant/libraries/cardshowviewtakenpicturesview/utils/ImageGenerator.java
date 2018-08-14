package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewImagesAdapter;

public class ImageGenerator {

    private static final String TEMP_IMAGE_BASE_NAME = "card_show_taken_picture_temp_image";
    private CardContract mCardContract;
    private Context mContext;
    private File compressedImage = null;
    private File mPhoto;

    public ImageGenerator(Context context, File photo, CardContract cardContract) {
        this.mPhoto = photo;
        this.mContext = context;
        this.mCardContract = cardContract;
    }

    public void generateCardShowTakenImageFromCamera(File photoTaken, Activity activity,
                                                     CardShowTakenPictureViewImagesAdapter imagesAdapter,
                                                     CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        if (photoTaken == null) {
            return;
        }

        Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(photoTaken.getAbsolutePath());
        String tempImagePathToShow = createTempImageFileToShow(bitmapImageFromIntentPath, activity, imagesAdapter);

        cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, photoTaken.getName(), tempImagePathToShow);
    }

    public void generateCardShowTakenImageFromImageGallery(File photoTaken,
                                                           Intent data,
                                                           Activity activity,
                                                           CardShowTakenPictureViewImagesAdapter imagesAdapter,
                                                           CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        try {
            photoTaken = PhotoViewFileUtil.from(mContext, data.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(photoTaken.getAbsolutePath());
        String tempImagePathToShow = createTempImageFileToShow(bitmapImageFromIntentPath, activity, imagesAdapter);

        cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, photoTaken.getName(), tempImagePathToShow);

    }

    private String createTempImageFileToShow(Bitmap bitmap, Activity activity, CardShowTakenPictureViewImagesAdapter imagesAdapter) {
        String indexTempImage = imagesAdapter.getItemCount() + 1 + "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);

        return MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                bitmap, TEMP_IMAGE_BASE_NAME + indexTempImage, null);
    }


}
