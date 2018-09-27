package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import io.reactivex.schedulers.Schedulers;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFileSync;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_PREFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_SUFFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.rotateImage;
import static io.reactivex.Observable.just;

public class ImageGenerator {

    public static final Integer fromCameraBack  = 1;
    public static final Integer fromGallery     = 2;
    public static final Integer fromCameraFront = 3;

    private Context mContext;

    public ImageGenerator(Context context) {
        this.mContext = context;
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

        final File file = new File(getFile() + "/" + photoTaken.getName());

        getBitmapFromFile(photoTaken.getAbsolutePath(), 2,
                (bitmap) -> cardShowTakenCompressedCallback.onSuccess(bitmap, photoTaken.getName(), file.toString())
        );
    }

    public void generateCardShowTakenImageFromImageGallery(Uri data, Integer photoType,
                                                           CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        File photoTaken = new File(ImageViewFileUtil.getFile().toString());

        try {
            photoTaken = ImageViewFileUtil.from(mContext, data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = getBitmapFromFileSync(photoTaken.getAbsolutePath(), 600);
        File tempImagePathToShow = createTempImageFileToShow(bitmap, photoType, null);
        cardShowTakenCompressedCallback.onSuccess(bitmap, tempImagePathToShow.getName(), tempImagePathToShow.toString());
    }

    private File createTempImageFileToShow(Bitmap bitmap, Integer typePhoto, Integer orientation) {
        String uuid = UUID.randomUUID().toString();
        File file   = new File(ImageViewFileUtil.getFile().toString() + "/" + JPG_FILE_PREFIX + uuid + JPG_FILE_SUFFIX);

        Bitmap scaledBitmap = ImageDecoder.scaleBitmap(bitmap, 1000);
        final int quality   = ImageDecoder.getImageQualityPercent(scaledBitmap);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            if (typePhoto.equals(fromCameraBack) || typePhoto.equals(fromCameraFront)) {
                saveInPictures(bitmap, orientation, uuid);

                try {
                    rotateImage(scaledBitmap, orientation).compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
                } catch (OutOfMemoryError outOfMemoryError) {
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
                }
            } else if (typePhoto.equals(fromGallery)) {
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void saveInPictures(Bitmap bitmap, Integer orientation, String uuid){
        try {
            subscribeSaveImageInPicturesThread(rotateImage(bitmap, orientation), uuid);
        } catch (OutOfMemoryError outOfMemoryError) {
            subscribeSaveImageInPicturesThread(bitmap, uuid);
        }
    }

    private void subscribeSaveImageInPicturesThread(Bitmap bitmap, String uuid) {
        just(MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bitmap, "stant", uuid))
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }


}
