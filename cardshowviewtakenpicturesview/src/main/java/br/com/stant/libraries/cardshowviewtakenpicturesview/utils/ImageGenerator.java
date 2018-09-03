package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPEG_FILE_SUFFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getFile;

public class ImageGenerator {

    public static final Integer fromCameraBack  = 1;
    public static final Integer fromGallery     = 2;
    public static final Integer fromCameraFront = 3;

    private CardContract mCardContract;
    private Context mContext;
    private File mCompressedImage = null;

    public ImageGenerator(Context context, CardContract cardContract) {
        this.mContext      = context;
        this.mCardContract = cardContract;
    }

    public void generateCardShowTakenImageFromCamera(Bitmap bitmap, Integer photoType, Integer orientation,
                                                     CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        File tempImagePathToShow = createTempImageFileToShow(bitmap, photoType, orientation);

        cardShowTakenCompressedCallback.onSuccess(bitmap, tempImagePathToShow.getName(), tempImagePathToShow.toString());
    }

    public void generateCardShowTakenImageFromCamera(File photoTaken,
                                                     CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        if (photoTaken == null) {
            return;
        }

        Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(photoTaken.getAbsolutePath());

        File file = new File(getFile() + "/" + photoTaken.getName());

        cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, photoTaken.getName(), file.toString());
    }

    public void generateCardShowTakenImageFromImageGallery(Uri data, Integer photoType,
                                                           CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        File photoTaken = new File(ImageViewFileUtil.getFile().toString());

        try {
            photoTaken = ImageViewFileUtil.from(mContext, data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(photoTaken.getAbsolutePath());
        File tempImagePathToShow         = createTempImageFileToShow(bitmapImageFromIntentPath, photoType, null);

        cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, tempImagePathToShow.getName(), tempImagePathToShow.toString());
    }

    private File createTempImageFileToShow(Bitmap bitmap, Integer typePhoto, Integer orientation) {
        String uuid = UUID.randomUUID().toString();
        File file = new File(ImageViewFileUtil.getFile().toString() + "/" + uuid + JPEG_FILE_SUFFIX);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            if (typePhoto.equals(fromCameraBack)) {
                rotateImage(bitmap, orientation, uuid).compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            } else if (typePhoto.equals(fromGallery)){
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, fileOutputStream);
                saveInPictures(bitmap, uuid);
            } else if (typePhoto.equals(fromCameraFront)){
                rotateImage(bitmap, orientationFromFront(orientation), uuid).compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void saveInPictures(Bitmap bitmap, String uuid){
        MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bitmap, "stant", uuid);
    }


    private Bitmap rotateImage(Bitmap source, float angle, String uuid) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        saveInPictures(bitmap, uuid);

        return bitmap;
    }

    private Integer orientationFromFront(int orientation){
        if (orientation == 0){
            return orientation;
        } else if (orientation == 90){
            return orientation + 180;
        } else if (orientation == 180){
            return orientation;
        } else if (orientation == 270){
            return orientation + 180;
        }

        return 0;
    }

}
